package com.example.data.mushaf

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.example.data.db.AppDatabase
import com.example.data.db.MushafPageDao
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.atomic.AtomicBoolean

data class MushafDownloadProgress(
    val isDownloading: Boolean = false,
    val isPaused: Boolean = false,
    val isCompleted: Boolean = false,
    val downloadedCount: Int = 0,
    val totalCount: Int = IndoPakMushafData.TOTAL_PAGES,
    val currentDownloadingPage: Int = 0,
    val totalStorageBytes: Long = 0L,
    val error: String? = null
) {
    val progressFraction: Float
        get() = if (totalCount > 0) (downloadedCount.toFloat() / totalCount).coerceIn(0f, 1f) else 0f
    
    val progressPercentage: Int
        get() = (progressFraction * 100).toInt()

    val formattedStorageSize: String
        get() {
            val mb = totalStorageBytes.toDouble() / (1024.0 * 1024.0)
            return if (mb >= 1.0) "%.1f MB".format(mb) else "%d KB".format(totalStorageBytes / 1024)
        }
}

/**
 * High-performance Offline Mushaf Page Storage & WorkManager Download Manager.
 * - Manages background worker execution with WorkManager for robust, resilient downloads of all 549 pages.
 * - Stores all 549 pages in app internal filesystem (filesDir/mushaf_pages), accessible 100% offline in Airplane Mode.
 * - Tracks download status, file paths, and storage sizes reactively via Room database.
 * - Provides live progress data to Jetpack Compose UI and lock-screen/notification channels.
 */
class MushafPageDownloadManager(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val isPausedFlag = AtomicBoolean(false)
    private val isCancelledFlag = AtomicBoolean(false)

    private val db: AppDatabase by lazy { AppDatabase.getDatabase(context) }
    private val dao: MushafPageDao by lazy { db.mushafPageDao() }
    private val workManager by lazy { WorkManager.getInstance(context) }

    private val _downloadProgress = MutableStateFlow(MushafDownloadProgress())
    val downloadProgress: StateFlow<MushafDownloadProgress> = _downloadProgress.asStateFlow()

    val pagesDir: File
        get() {
            val dir = File(context.filesDir, "mushaf_tajweed_pages")
            if (!dir.exists()) dir.mkdirs()
            return dir
        }

    init {
        scope.launch {
            extractBundledPagesIfNeeded()
            refreshOfflineStatus()
            observeWorkManagerProgress()
            
            // Automatically schedule background download of all pages on app launch
            val count = getOfflinePagesCount()
            if (count < IndoPakMushafData.TOTAL_PAGES) {
                kotlinx.coroutines.delay(2000)
                startDownloadAll()
            }
        }
    }

    /**
     * Listens to WorkManager unique work status and syncs progress into Compose StateFlow.
     */
    private fun observeWorkManagerProgress() {
        scope.launch {
            workManager.getWorkInfosForUniqueWorkFlow(MushafDownloadWorker.WORK_NAME).collect { workInfos ->
                val workInfo = workInfos.firstOrNull() ?: return@collect
                val progressData = workInfo.progress

                val isRunning = workInfo.state == WorkInfo.State.RUNNING
                val isSucceeded = workInfo.state == WorkInfo.State.SUCCEEDED
                val isCancelled = workInfo.state == WorkInfo.State.CANCELLED

                val currentDownloaded = progressData.getInt(MushafDownloadWorker.KEY_DOWNLOADED_COUNT, getOfflinePagesCount())
                val currentPage = progressData.getInt(MushafDownloadWorker.KEY_CURRENT_PAGE, 0)
                val isDone = progressData.getBoolean(MushafDownloadWorker.KEY_IS_COMPLETED, isSucceeded)
                val storageBytes = progressData.getLong(MushafDownloadWorker.KEY_STORAGE_BYTES, calculateTotalStorageBytes())

                _downloadProgress.update { current ->
                    current.copy(
                        isDownloading = isRunning,
                        isCompleted = isDone || (currentDownloaded >= IndoPakMushafData.TOTAL_PAGES),
                        downloadedCount = currentDownloaded,
                        currentDownloadingPage = if (isRunning) currentPage else 0,
                        totalStorageBytes = storageBytes,
                        isPaused = isPausedFlag.get()
                    )
                }

                if (isSucceeded || isCancelled) {
                    refreshOfflineStatus()
                }
            }
        }
    }

    /**
     * Registers all 549 pre-bundled asset pages into Room Database and cache on app start.
     */
    private suspend fun extractBundledPagesIfNeeded() = withContext(Dispatchers.IO) {
        try {
            for (p in 1..IndoPakMushafData.TOTAL_PAGES) {
                val padded = "%03d".format(p)
                val assetPath = "mushaf_tajweed_pages/page_$padded.jpg"
                try {
                    // Check if exists
                    context.assets.open(assetPath).use {
                        dao.updateDownloadStatus(p, true, "asset://$assetPath", 100000L)
                    }
                } catch (_: Exception) {}
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getLocalPageFile(pageNumber: Int): File? {
        val clamped = pageNumber.coerceIn(1, IndoPakMushafData.TOTAL_PAGES)
        val padded = "%03d".format(clamped)
        
        val extensions = listOf("webp", "png", "jpg")
        for (ext in extensions) {
            val file = File(pagesDir, "page_$padded.$ext")
            if (file.exists() && file.length() > 1000) {
                return file
            }
            val fileNoPad = File(pagesDir, "page_$clamped.$ext")
            if (fileNoPad.exists() && fileNoPad.length() > 1000) {
                return fileNoPad
            }
        }
        return null
    }

    fun isPageOffline(pageNumber: Int): Boolean {
        // 1. Check local filesDir
        if (getLocalPageFile(pageNumber) != null) return true

        // 2. Check bundled APK assets
        val clamped = pageNumber.coerceIn(1, IndoPakMushafData.TOTAL_PAGES)
        val padded = "%03d".format(clamped)
        return try {
            val assetStream = context.assets.open("mushaf_tajweed_pages/page_$padded.jpg")
            assetStream.close()
            true
        } catch (_: Exception) {
            try {
                val assetStream2 = context.assets.open("mushaf_tajweed_pages/page_$padded.webp")
                assetStream2.close()
                true
            } catch (_: Exception) {
                false
            }
        }
    }

    fun getOfflinePagesCount(): Int {
        var count = 0
        for (p in 1..IndoPakMushafData.TOTAL_PAGES) {
            if (isPageOffline(p)) count++
        }
        return count
    }

    fun calculateTotalStorageBytes(): Long {
        var total = 0L
        pagesDir.listFiles()?.forEach { file ->
            if (file.isFile) total += file.length()
        }
        return total
    }

    fun setDownloadingState(isDownloading: Boolean) {
        _downloadProgress.update {
            it.copy(
                isDownloading = isDownloading,
                isPaused = false,
                error = null,
                currentDownloadingPage = if (!isDownloading) 0 else it.currentDownloadingPage
            )
        }
    }

    fun setErrorState(error: String) {
        _downloadProgress.update { it.copy(error = error, isDownloading = false) }
    }

    fun isPaused(): Boolean = isPausedFlag.get()
    fun isCancelled(): Boolean = isCancelledFlag.get()

    fun setCurrentDownloadingPage(page: Int) {
        _downloadProgress.update { it.copy(currentDownloadingPage = page) }
    }

    fun incrementDownloadedCount() {
        val storageBytes = calculateTotalStorageBytes()
        _downloadProgress.update { 
            it.copy(
                downloadedCount = (it.downloadedCount + 1).coerceAtMost(IndoPakMushafData.TOTAL_PAGES),
                totalStorageBytes = storageBytes
            ) 
        }
    }

    fun refreshOfflineStatus() {
        scope.launch {
            val count = getOfflinePagesCount()
            val storageBytes = calculateTotalStorageBytes()
            val isComplete = count >= IndoPakMushafData.TOTAL_PAGES
            _downloadProgress.update {
                it.copy(
                    downloadedCount = count,
                    totalCount = IndoPakMushafData.TOTAL_PAGES,
                    totalStorageBytes = storageBytes,
                    isCompleted = isComplete
                )
            }
        }
    }

    fun isJuzOffline(juzNumber: Int): Boolean {
        val juzIdx = juzNumber.coerceIn(1, 30) - 1
        val startPage = IndoPakMushafData.JUZ_START_PAGES[juzIdx]
        val endPage = if (juzIdx < 29) IndoPakMushafData.JUZ_START_PAGES[juzIdx + 1] - 1 else IndoPakMushafData.TOTAL_PAGES
        
        for (p in startPage..endPage) {
            if (!isPageOffline(p)) return false
        }
        return true
    }

    private var activeDownloadJob: Job? = null

    /**
     * High-speed batch download of all 549 Mushaf pages.
     * Runs high-concurrency coroutines in-app with instant UI updates, and enqueues WorkManager for background execution.
     */
    fun startDownloadAll() {
        isPausedFlag.set(false)
        isCancelledFlag.set(false)

        setDownloadingState(true)

        // 1. Enqueue WorkManager for background persistence
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<MushafDownloadWorker>()
            .setConstraints(constraints)
            .addTag(MushafDownloadWorker.TAG)
            .build()

        workManager.enqueueUniqueWork(
            MushafDownloadWorker.WORK_NAME,
            ExistingWorkPolicy.KEEP,
            workRequest
        )

        // 2. Launch high-speed active coroutine for 0ms delay immediate processing
        activeDownloadJob?.cancel()
        activeDownloadJob = scope.launch(Dispatchers.IO) {
            val totalPages = IndoPakMushafData.TOTAL_PAGES
            val missingPages = (1..totalPages).filter { !isPageOffline(it) }

            if (missingPages.isEmpty()) {
                refreshOfflineStatus()
                setDownloadingState(false)
                return@launch
            }

            val chunks = missingPages.chunked(12)
            for (chunk in chunks) {
                if (isCancelled()) break

                while (isPaused()) {
                    delay(250)
                    if (isCancelled()) break
                }
                if (isCancelled()) break

                val lastPage = chunk.last()
                setCurrentDownloadingPage(lastPage)

                coroutineScope {
                    chunk.map { page ->
                        async(Dispatchers.IO) {
                            if (!isPageOffline(page)) {
                                downloadAndSavePage(page)
                            } else true
                        }
                    }.awaitAll()
                }

                val currentCount = getOfflinePagesCount()
                val storageBytes = calculateTotalStorageBytes()
                _downloadProgress.update {
                    it.copy(
                        downloadedCount = currentCount,
                        totalStorageBytes = storageBytes,
                        isCompleted = currentCount >= totalPages,
                        currentDownloadingPage = lastPage
                    )
                }
            }

            refreshOfflineStatus()
            setDownloadingState(false)
        }
    }

    /**
     * Download a specific Juz for offline use.
     */
    fun downloadJuz(juzNumber: Int) {
        val juzIdx = juzNumber.coerceIn(1, 30) - 1
        val startPage = IndoPakMushafData.JUZ_START_PAGES[juzIdx]
        val endPage = if (juzIdx < 29) IndoPakMushafData.JUZ_START_PAGES[juzIdx + 1] - 1 else IndoPakMushafData.TOTAL_PAGES

        scope.launch(Dispatchers.IO) {
            setDownloadingState(true)
            val pagesToDownload = (startPage..endPage).filter { !isPageOffline(it) }
            val chunks = pagesToDownload.chunked(12)
            for (chunk in chunks) {
                if (isCancelled()) break
                val lastPage = chunk.last()
                setCurrentDownloadingPage(lastPage)
                coroutineScope {
                    chunk.map { p ->
                        async(Dispatchers.IO) {
                            downloadAndSavePage(p)
                        }
                    }.awaitAll()
                }
                refreshOfflineStatus()
            }
            setDownloadingState(false)
            refreshOfflineStatus()
        }
    }

    fun pauseDownload() {
        isPausedFlag.set(true)
        _downloadProgress.update { it.copy(isPaused = true) }
    }

    fun resumeDownload() {
        if (isPausedFlag.get()) {
            isPausedFlag.set(false)
            _downloadProgress.update { it.copy(isPaused = false) }
        } else {
            startDownloadAll()
        }
    }

    fun cancelDownload() {
        isCancelledFlag.set(true)
        activeDownloadJob?.cancel()
        workManager.cancelUniqueWork(MushafDownloadWorker.WORK_NAME)
        _downloadProgress.update {
            it.copy(
                isDownloading = false,
                isPaused = false,
                currentDownloadingPage = 0
            )
        }
        refreshOfflineStatus()
    }

    /**
     * Deletes all downloaded pages from internal storage to free up space.
     */
    fun deleteOfflinePages(onComplete: (() -> Unit)? = null) {
        scope.launch {
            workManager.cancelUniqueWork(MushafDownloadWorker.WORK_NAME)
            pagesDir.listFiles()?.forEach { file ->
                if (file.isFile) file.delete()
            }
            // Retain pre-bundled pages
            extractBundledPagesIfNeeded()
            refreshOfflineStatus()
            withContext(Dispatchers.Main) {
                onComplete?.invoke()
            }
        }
    }

    /**
     * Download single page and cache locally in internal storage & Room database.
     */
    fun downloadSinglePage(pageNumber: Int, onComplete: ((Boolean) -> Unit)? = null) {
        scope.launch {
            val success = downloadAndSavePage(pageNumber)
            refreshOfflineStatus()
            withContext(Dispatchers.Main) {
                onComplete?.invoke(success)
            }
        }
    }

    suspend fun downloadAndSavePage(pageNumber: Int): Boolean = withContext(Dispatchers.IO) {
        val clampedPage = pageNumber.coerceIn(1, IndoPakMushafData.TOTAL_PAGES)
        val padded3 = "%03d".format(clampedPage)

        // Special handling for Page 1: Custom User Quran Title & Cover Page
        if (clampedPage == 1) {
            val targetJpg = File(pagesDir, "page_001.jpg")
            try {
                val bitmap = android.graphics.BitmapFactory.decodeResource(context.resources, com.example.R.drawable.img_mushaf_cover)
                val outputStream = FileOutputStream(targetJpg)
                bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, outputStream)
                outputStream.flush()
                outputStream.close()
                dao.updateDownloadStatus(1, true, targetJpg.absolutePath, targetJpg.length())
                MushafPageCacheManager.registerNewDownloadedPage(1, targetJpg)
                return@withContext true
            } catch (_: Exception) {}
        }

        // 1. Check local storage directory for existing file
        val existingLocalFile = getLocalPageFile(clampedPage)
        if (existingLocalFile != null && existingLocalFile.exists() && existingLocalFile.length() > 1000) {
            dao.updateDownloadStatus(clampedPage, true, existingLocalFile.absolutePath, existingLocalFile.length())
            MushafPageCacheManager.registerNewDownloadedPage(clampedPage, existingLocalFile)
            return@withContext true
        }

        // 2. Check Room Database record
        try {
            val dbPage = dao.getPageByNumber(clampedPage)
            if (dbPage != null && dbPage.isDownloaded && !dbPage.localFilePath.isNullOrEmpty()) {
                if (dbPage.localFilePath.startsWith("asset://")) {
                    val assetPath = dbPage.localFilePath.removePrefix("asset://")
                    try {
                        context.assets.open(assetPath).use {
                            MushafPageCacheManager.initializeIndex(context)
                            return@withContext true
                        }
                    } catch (_: Exception) {}
                } else {
                    val dbFile = File(dbPage.localFilePath)
                    if (dbFile.exists() && dbFile.length() > 1000) {
                        MushafPageCacheManager.registerNewDownloadedPage(clampedPage, dbFile)
                        return@withContext true
                    }
                }
            }
        } catch (_: Exception) {}

        // 3. Check pre-bundled APK Assets
        val assetCandidates = listOf("mushaf_tajweed_pages/page_$padded3.jpg", "mushaf_tajweed_pages/page_$padded3.webp")
        for (assetPath in assetCandidates) {
            try {
                context.assets.open(assetPath).use { stream ->
                    val assetSize = stream.available().toLong()
                    dao.updateDownloadStatus(clampedPage, true, "asset://$assetPath", assetSize)
                    MushafPageCacheManager.initializeIndex(context)
                    return@withContext true
                }
            } catch (_: Exception) {}
        }

        // 4. Download authentic 16-line Colour-Coded Tajweed Quran image
        val targetJpg = File(pagesDir, "page_$padded3.jpg")
        val targetWebp = File(pagesDir, "page_$padded3.webp")
        val tempFile = File(pagesDir, "page_$padded3.tmp")

        val candidateUrls = IndoPakMushafData.get16LinePageImageUrls(context, clampedPage).filter { it.startsWith("http") }

        var downloadedSuccessfully = false

        for (urlStr in candidateUrls) {
            var connection: HttpURLConnection? = null
            var inputStream: InputStream? = null
            var outputStream: FileOutputStream? = null

            try {
                val url = URL(urlStr)
                connection = (url.openConnection() as HttpURLConnection).apply {
                    connectTimeout = 5000
                    readTimeout = 7000
                    instanceFollowRedirects = true
                    setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 13; Mobile) Al-Quran/1.0")
                }

                val code = connection.responseCode
                if (code == HttpURLConnection.HTTP_OK) {
                    inputStream = connection.inputStream
                    outputStream = FileOutputStream(tempFile)

                    val buffer = ByteArray(16384)
                    var bytesRead: Int
                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                    }
                    outputStream.flush()
                    outputStream.close()
                    inputStream.close()

                    if (tempFile.exists() && tempFile.length() > 1000) {
                        if (targetJpg.exists()) targetJpg.delete()
                        if (targetWebp.exists()) targetWebp.delete()
                        tempFile.renameTo(targetJpg)
                        downloadedSuccessfully = true
                        break
                    }
                }
            } catch (_: Exception) {
                tempFile.delete()
            } finally {
                try { outputStream?.close() } catch (_: Exception) {}
                try { inputStream?.close() } catch (_: Exception) {}
                connection?.disconnect()
            }
            if (downloadedSuccessfully) break
        }

        val actualSavedFile = when {
            targetJpg.exists() && targetJpg.length() > 500 -> targetJpg
            targetWebp.exists() && targetWebp.length() > 500 -> targetWebp
            else -> null
        }

        if (downloadedSuccessfully && actualSavedFile != null) {
            dao.updateDownloadStatus(clampedPage, true, actualSavedFile.absolutePath, actualSavedFile.length())
            MushafPageCacheManager.registerNewDownloadedPage(clampedPage, actualSavedFile)
            return@withContext true
        }

        return@withContext false
    }

    private fun generateLocalPageBitmap(clampedPage: Int, targetFile: File): Boolean {
        return try {
            val width = 1080
            val height = 1620
            val bitmap = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.RGB_565)
            val canvas = android.graphics.Canvas(bitmap)

            // Draw parchment background
            val bgPaint = android.graphics.Paint().apply { color = 0xFFFAF6EE.toInt() }
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

            // Outer gold border
            val goldPaint = android.graphics.Paint().apply {
                color = 0xFFC59B27.toInt()
                style = android.graphics.Paint.Style.STROKE
                strokeWidth = 6f
                isAntiAlias = true
            }
            canvas.drawRect(24f, 24f, width - 24f, height - 24f, goldPaint)
            canvas.drawRect(36f, 36f, width - 36f, height - 36f, goldPaint)

            // Inner green border
            val greenBorder = android.graphics.Paint().apply {
                color = 0xFF0F3E28.toInt()
                style = android.graphics.Paint.Style.STROKE
                strokeWidth = 3f
                isAntiAlias = true
            }
            canvas.drawRect(48f, 48f, width - 48f, height - 48f, greenBorder)

            val pageInfo = IndoPakMushafData.getPageInfo(clampedPage)
            val ayahs = com.example.data.mushaf.OfflineQuranDataProvider.getOfflineAyahsForPage(context, clampedPage)
            val pageContent = com.example.data.mushaf.MushafPageLineManager.build16LinePage(clampedPage, ayahs)

            val fontTypeface = android.graphics.Typeface.SERIF

            // Top Header: Right = Surah, Center = Page #, Left = Juz
            val headerPaint = android.graphics.Paint().apply {
                color = 0xFF0F3E28.toInt()
                textSize = 36f
                isAntiAlias = true
                typeface = fontTypeface
            }

            headerPaint.textAlign = android.graphics.Paint.Align.RIGHT
            canvas.drawText(pageInfo.surahNameArabic, width - 70f, 100f, headerPaint)

            headerPaint.textAlign = android.graphics.Paint.Align.LEFT
            canvas.drawText(pageInfo.juzNameArabic, 70f, 100f, headerPaint)

            headerPaint.textAlign = android.graphics.Paint.Align.CENTER
            headerPaint.color = 0xFFC59B27.toInt()
            canvas.drawText(IndoPakMushafData.toArabicDigits(clampedPage), width / 2f, 100f, headerPaint)

            // 16 Lines
            val linePaint = android.graphics.Paint().apply {
                color = 0xFF111827.toInt()
                textSize = 42f
                textAlign = android.graphics.Paint.Align.CENTER
                isAntiAlias = true
                typeface = fontTypeface
            }

            val startY = 160f
            val lineSpacing = (height - 220f) / 16f

            pageContent.lines.take(16).forEachIndexed { idx, line ->
                val y = startY + (idx + 1) * lineSpacing - 15f
                if (line.isHeader) {
                    val rect = android.graphics.RectF(60f, y - 45f, width - 60f, y + 15f)
                    val bannerPaint = android.graphics.Paint().apply { color = 0xFFE8F5E9.toInt() }
                    canvas.drawRoundRect(rect, 12f, 12f, bannerPaint)
                    val borderPaint = android.graphics.Paint().apply {
                        color = 0xFFC59B27.toInt()
                        style = android.graphics.Paint.Style.STROKE
                        strokeWidth = 3f
                    }
                    canvas.drawRoundRect(rect, 12f, 12f, borderPaint)

                    val headerTextPaint = android.graphics.Paint().apply {
                        color = 0xFF0F3E28.toInt()
                        textSize = 38f
                        textAlign = android.graphics.Paint.Align.CENTER
                        isAntiAlias = true
                        typeface = fontTypeface
                    }
                    canvas.drawText(line.text, width / 2f, y - 5f, headerTextPaint)
                } else {
                    canvas.drawText(line.text, width / 2f, y, linePaint)
                }
            }

            FileOutputStream(targetFile).use { out ->
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                    bitmap.compress(android.graphics.Bitmap.CompressFormat.WEBP_LOSSY, 90, out)
                } else {
                    @Suppress("DEPRECATION")
                    bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 92, out)
                }
            }
            bitmap.recycle()
            targetFile.exists() && targetFile.length() > 500
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    companion object {
        @Volatile
        private var instance: MushafPageDownloadManager? = null

        fun getInstance(context: Context): MushafPageDownloadManager {
            return instance ?: synchronized(this) {
                instance ?: MushafPageDownloadManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
