package com.example.data.mushaf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.ConcurrentHashMap

/**
 * High-Performance Local Pre-indexing and L2 Bitmap Cache Layer for Mushaf Pages.
 * 
 * 1. Indexes local page availability (Assets & FilesDir) instantly without network checks.
 * 2. Pre-loads adjacent pages (+/- 2 pages) into an LRU Memory Cache for instant swipe rendering.
 * 3. Bypasses any runtime network validation when local assets/files are present.
 */
object MushafPageCacheManager {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    // Memory Cache: max 64 high-res page Bitmaps in RAM (~100 MB)
    private val memoryCache = object : LruCache<Int, ImageBitmap>(64) {}

    // Index of page locations: PageNumber -> File path or Asset path
    private val localIndex = ConcurrentHashMap<Int, PageLocation>()

    sealed class PageLocation {
        data class LocalFile(val file: File) : PageLocation()
        data class AssetPath(val assetPath: String) : PageLocation()
    }

    private var isIndexed = false

    /**
     * Synchronous memory cache check for immediate 0ms rendering on the first Compose frame.
     */
    fun getMemoryCachedBitmap(pageNumber: Int): ImageBitmap? {
        return memoryCache.get(pageNumber)
    }

    /**
     * Call at app launch or screen init to index all local pages immediately (< 5ms).
     * Integrates Room Database + FilesDir + APK Assets for high-priority zero-latency offline access.
     */
    fun initializeIndex(context: Context) {
        if (isIndexed) return
        try {
            // Delete old non-tajweed cache
            File(context.filesDir, "mushaf_pages").deleteRecursively()
            
            // 1. Scan internal storage directory
            val localDir = File(context.filesDir, "mushaf_tajweed_pages")
            if (localDir.exists() && localDir.isDirectory) {
                localDir.listFiles()?.forEach { file ->
                    val num = file.nameWithoutExtension.removePrefix("page_").removePrefix("page-").toIntOrNull()
                    if (num != null && file.length() > 1000) {
                        localIndex[num] = PageLocation.LocalFile(file)
                    }
                }
            }

            // 2. Query Room Database in background coroutine without blocking
            scope.launch {
                try {
                    val db = com.example.data.db.AppDatabase.getDatabase(context)
                    val dao = db.mushafPageDao()
                    val dbPages = dao.getAllPages()
                    for (entity in dbPages) {
                        if (entity.isDownloaded && !entity.localFilePath.isNullOrEmpty()) {
                            val path = entity.localFilePath
                            if (path.startsWith("asset://")) {
                                val assetPath = path.removePrefix("asset://")
                                if (!localIndex.containsKey(entity.pageNumber)) {
                                    localIndex[entity.pageNumber] = PageLocation.AssetPath(assetPath)
                                }
                            } else {
                                val file = File(path)
                                if (file.exists() && file.length() > 1000) {
                                    localIndex[entity.pageNumber] = PageLocation.LocalFile(file)
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    // Ignore Room DB initialization errors if DB not seeded yet
                }
            }

            // 3. Quick scan APK assets without opening file streams
            val assetDirs = listOf("mushaf_tajweed_pages")
            for (dir in assetDirs) {
                try {
                    val files = context.assets.list(dir) ?: emptyArray()
                    for (f in files) {
                        val num = f.removePrefix("page_").removePrefix("page-").substringBefore(".").toIntOrNull()
                        if (num != null && !localIndex.containsKey(num)) {
                            localIndex[num] = PageLocation.AssetPath("$dir/$f")
                        }
                    }
                } catch (e: Exception) { }
            }
            isIndexed = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Synchronously decodes and returns cached memory bitmap or local asset bitmap immediately on frame 0.
     */
    fun getPageBitmapSync(context: Context, pageNumber: Int): ImageBitmap? {
        val clampedPage = pageNumber.coerceIn(1, 549)

        // Special handling for Page 1: Custom User Quran Title & Cover Page
        if (clampedPage == 1) {
            memoryCache.get(1)?.let { return it }
            try {
                val bm = BitmapFactory.decodeResource(context.resources, com.example.R.drawable.img_mushaf_cover)
                val imgBm = bm?.asImageBitmap()
                if (imgBm != null) {
                    memoryCache.put(1, imgBm)
                    return imgBm
                }
            } catch (_: Exception) {}
        }

        memoryCache.get(clampedPage)?.let { return it }

        val padded3 = "%03d".format(clampedPage)

        // 1. Direct O(1) Check internal storage files
        try {
            val localDir = File(context.filesDir, "mushaf_tajweed_pages")
            for (ext in listOf("jpg", "webp", "png")) {
                val f = File(localDir, "page_$padded3.$ext")
                if (f.exists() && f.length() > 1000) {
                    val bm = BitmapFactory.decodeFile(f.absolutePath)
                    val imgBm = bm?.asImageBitmap()
                    if (imgBm != null) {
                        memoryCache.put(clampedPage, imgBm)
                        localIndex[clampedPage] = PageLocation.LocalFile(f)
                        return imgBm
                    }
                }
            }
        } catch (_: Exception) {}

        // 2. Direct O(1) Check APK Assets without waiting for index or list()
        val assetCandidates = listOf(
            "mushaf_tajweed_pages/page_$padded3.jpg",
            "mushaf_tajweed_pages/page_$padded3.webp"
        )
        for (path in assetCandidates) {
            try {
                context.assets.open(path).use { stream ->
                    val bm = BitmapFactory.decodeStream(stream)
                    val imgBm = bm?.asImageBitmap()
                    if (imgBm != null) {
                        memoryCache.put(clampedPage, imgBm)
                        localIndex[clampedPage] = PageLocation.AssetPath(path)
                        return imgBm
                    }
                }
            } catch (_: Exception) {}
        }

        if (!isIndexed) {
            initializeIndex(context)
        }

        val location = localIndex[clampedPage] ?: return null
        var bm: Bitmap? = null
        try {
            when (location) {
                is PageLocation.LocalFile -> {
                    bm = BitmapFactory.decodeFile(location.file.absolutePath)
                }
                is PageLocation.AssetPath -> {
                    context.assets.open(location.assetPath).use { stream ->
                        bm = BitmapFactory.decodeStream(stream)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val imgBm = bm?.asImageBitmap()
        if (imgBm != null) {
            memoryCache.put(clampedPage, imgBm)
            preloadAdjacentPages(context, clampedPage)
        }
        return imgBm
    }

    /**
     * Synchronously returns cached memory bitmap if ready, or decodes from indexed local disk/assets.
     */
    suspend fun getPageBitmap(context: Context, pageNumber: Int): ImageBitmap? = withContext(Dispatchers.IO) {
        // 1. Memory Cache lookup
        memoryCache.get(pageNumber)?.let { return@withContext it }

        // 2. Direct O(1) Sync Check
        val direct = getPageBitmapSync(context, pageNumber)
        if (direct != null) return@withContext direct

        // Ensure index exists
        if (!isIndexed) {
            initializeIndex(context)
        }

        // 3. Direct Index lookup
        val location = localIndex[pageNumber]
        var bm: Bitmap? = null

        if (location != null) {
            when (location) {
                is PageLocation.LocalFile -> {
                    try {
                        bm = BitmapFactory.decodeFile(location.file.absolutePath)
                    } catch (e: Exception) { e.printStackTrace() }
                }
                is PageLocation.AssetPath -> {
                    try {
                        context.assets.open(location.assetPath).use { stream ->
                            bm = BitmapFactory.decodeStream(stream)
                        }
                    } catch (e: Exception) { e.printStackTrace() }
                }
            }
        }

        val imageBitmap = bm?.asImageBitmap()
        if (imageBitmap != null) {
            memoryCache.put(pageNumber, imageBitmap)
            preloadAdjacentPages(context, pageNumber)
        }
        return@withContext imageBitmap
    }

    /**
     * Background pre-loads surrounding pages so swiping is 100% instant without disk IO.
     */
    fun preloadAdjacentPages(context: Context, currentPage: Int) {
        if (!isIndexed) {
            initializeIndex(context)
        }
        scope.launch {
            val pagesToPreload = listOf(
                currentPage - 1, currentPage + 1,
                currentPage - 2, currentPage + 2,
                currentPage - 3, currentPage + 3,
                currentPage - 4, currentPage + 4
            ).filter { it in 1..549 }

            val imageLoader = coil.Coil.imageLoader(context)

            for (p in pagesToPreload) {
                if (memoryCache.get(p) == null) {
                    val location = localIndex[p]
                    if (location != null) {
                        var bm: Bitmap? = null
                        try {
                            when (location) {
                                is PageLocation.LocalFile -> bm = BitmapFactory.decodeFile(location.file.absolutePath)
                                is PageLocation.AssetPath -> {
                                    context.assets.open(location.assetPath).use { stream ->
                                        bm = BitmapFactory.decodeStream(stream)
                                    }
                                }
                            }
                            bm?.asImageBitmap()?.let { memoryCache.put(p, it) }
                        } catch (e: Exception) { }
                    } else {
                        // Preload into Coil cache ahead of time
                        try {
                            val urls = IndoPakMushafData.get16LinePageImageUrls(context, p)
                            if (urls.isNotEmpty()) {
                                val req = coil.request.ImageRequest.Builder(context)
                                    .data(urls.first())
                                    .size(coil.size.Size.ORIGINAL)
                                    .precision(coil.size.Precision.EXACT)
                                    .diskCachePolicy(coil.request.CachePolicy.ENABLED)
                                    .memoryCachePolicy(coil.request.CachePolicy.ENABLED)
                                    .build()
                                imageLoader.enqueue(req)
                            }
                        } catch (e: Exception) { }
                    }
                }
            }
        }
    }

    /**
     * Call when a new page is downloaded & saved to register it in the index immediately.
     */
    fun registerNewDownloadedPage(pageNumber: Int, file: File) {
        localIndex[pageNumber] = PageLocation.LocalFile(file)
    }

    fun isPageLocallyIndexed(pageNumber: Int): Boolean {
        return localIndex.containsKey(pageNumber)
    }
}
