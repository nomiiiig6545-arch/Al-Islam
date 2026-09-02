package com.example.data.audio

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

sealed class AudioDownloadStatus {
    object NotDownloaded : AudioDownloadStatus()
    data class Downloading(val progress: Float) : AudioDownloadStatus() // 0.0 to 1.0
    data class Paused(val progress: Float, val downloadedBytes: Long) : AudioDownloadStatus()
    object Downloaded : AudioDownloadStatus()
}

class AudioDownloadManager(val context: Context) {

    private val scope = CoroutineScope(Dispatchers.IO)
    private val activeJobs = mutableMapOf<String, Job>()
    private val activeConnections = mutableMapOf<String, HttpURLConnection>()

    // Key: "$reciterId-$surahNumber"
    private val _downloadStates = MutableStateFlow<Map<String, AudioDownloadStatus>>(emptyMap())
    val downloadStates: StateFlow<Map<String, AudioDownloadStatus>> = _downloadStates.asStateFlow()

    fun getKey(reciterId: String, surahNumber: Int): String = "$reciterId-$surahNumber"

    /**
     * App-Private internal storage directory (filesDir).
     * Strictly isolated from external public storage/gallery.
     */
    private fun getAudioDir(reciterId: String): File {
        val dir = File(context.filesDir, "quran_audio/$reciterId")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun getAudioFile(reciterId: String, surahNumber: Int): File {
        val formattedNum = String.format("%03d", surahNumber)
        return File(getAudioDir(reciterId), "$formattedNum.mp3")
    }

    private fun getTmpFile(reciterId: String, surahNumber: Int): File {
        val formattedNum = String.format("%03d", surahNumber)
        return File(getAudioDir(reciterId), "$formattedNum.tmp")
    }

    fun isAudioDownloaded(reciterId: String, surahNumber: Int): Boolean {
        val file = getAudioFile(reciterId, surahNumber)
        return file.exists() && file.length() > 0
    }

    fun getDownloadedCount(reciterId: String): Int {
        val dir = getAudioDir(reciterId)
        if (!dir.exists()) return 0
        val files = dir.listFiles { _, name -> name.endsWith(".mp3") }
        return files?.count { it.length() > 0 } ?: 0
    }

    fun getDownloadedSurahs(reciterId: String): List<Int> {
        val dir = getAudioDir(reciterId)
        if (!dir.exists()) return emptyList()
        val files = dir.listFiles { _, name -> name.endsWith(".mp3") } ?: return emptyList()
        return files.filter { it.length() > 0 }
            .mapNotNull { file ->
                file.nameWithoutExtension.toIntOrNull()
            }
            .sorted()
    }

    fun getDownloadedSurahFileSize(reciterId: String, surahNumber: Int): Long {
        val file = getAudioFile(reciterId, surahNumber)
        return if (file.exists()) file.length() else 0L
    }

    fun getTotalDownloadedAudioBytes(reciterId: String): Long {
        val dir = getAudioDir(reciterId)
        if (!dir.exists()) return 0L
        val files = dir.listFiles { _, name -> name.endsWith(".mp3") } ?: return 0L
        return files.sumOf { it.length() }
    }

    fun deleteDownloadedSurah(reciterId: String, surahNumber: Int): Boolean {
        val file = getAudioFile(reciterId, surahNumber)
        val deleted = if (file.exists()) file.delete() else false
        val key = getKey(reciterId, surahNumber)
        updateState(key, AudioDownloadStatus.NotDownloaded)
        return deleted
    }

    fun deleteAllDownloadedSurahsForReciter(reciterId: String): Int {
        val dir = getAudioDir(reciterId)
        if (!dir.exists()) return 0
        val files = dir.listFiles { _, name -> name.endsWith(".mp3") || name.endsWith(".tmp") } ?: return 0
        var count = 0
        files.forEach { file ->
            val surahNum = file.nameWithoutExtension.toIntOrNull()
            if (file.delete()) {
                count++
                if (surahNum != null) {
                    val key = getKey(reciterId, surahNum)
                    updateState(key, AudioDownloadStatus.NotDownloaded)
                }
            }
        }
        return count
    }

    fun getStatus(reciterId: String, surahNumber: Int): AudioDownloadStatus {
        val key = getKey(reciterId, surahNumber)
        _downloadStates.value[key]?.let { return it }

        if (isAudioDownloaded(reciterId, surahNumber)) {
            return AudioDownloadStatus.Downloaded
        }

        val tmpFile = getTmpFile(reciterId, surahNumber)
        if (tmpFile.exists() && tmpFile.length() > 0) {
            return AudioDownloadStatus.Paused(0f, tmpFile.length())
        }

        return AudioDownloadStatus.NotDownloaded
    }

    fun startOrResumeDownload(
        reciterId: String,
        surahNumber: Int,
        audioUrl: String,
        onComplete: () -> Unit = {}
    ) {
        val key = getKey(reciterId, surahNumber)
        if (activeJobs.containsKey(key)) return // Download in progress already

        // Immediate responsive state update so UI shows download in progress instantly
        val tmpFile = getTmpFile(reciterId, surahNumber)
        val initialBytes = if (tmpFile.exists()) tmpFile.length() else 0L
        val initialProg = if (initialBytes > 0) 0.03f else 0.01f
        updateState(key, AudioDownloadStatus.Downloading(initialProg))

        val job = scope.launch {
            try {
                val file = getAudioFile(reciterId, surahNumber)
                if (file.exists() && file.length() > 0) {
                    updateState(key, AudioDownloadStatus.Downloaded)
                    onComplete()
                    return@launch
                }

                val tmpFile = getTmpFile(reciterId, surahNumber)
                var existingBytes = if (tmpFile.exists()) tmpFile.length() else 0L

                val url = URL(audioUrl)
                val connection = (url.openConnection() as HttpURLConnection).apply {
                    connectTimeout = 6000
                    readTimeout = 10000
                    instanceFollowRedirects = true
                    setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 13; Mobile) Al-Quran-Pro/1.0")
                    setRequestProperty("Accept-Encoding", "identity")
                    setRequestProperty("Connection", "Keep-Alive")
                    if (existingBytes > 0) {
                        setRequestProperty("Range", "bytes=$existingBytes-")
                    }
                }
                activeConnections[key] = connection

                val responseCode = connection.responseCode
                val isPartial = responseCode == HttpURLConnection.HTTP_PARTIAL
                val isOk = responseCode == HttpURLConnection.HTTP_OK

                if (!isPartial && !isOk && responseCode != HttpURLConnection.HTTP_NOT_MODIFIED) {
                    existingBytes = 0L
                    tmpFile.delete()
                }

                val contentLength = connection.contentLengthLong
                val totalBytes = if (isPartial) contentLength + existingBytes else if (isOk) contentLength else -1L

                val initialProgress = if (totalBytes > 0) (existingBytes.toFloat() / totalBytes).coerceIn(0f, 1f) else 0.05f
                updateState(key, AudioDownloadStatus.Downloading(initialProgress))

                val inputStream: InputStream = java.io.BufferedInputStream(connection.inputStream, 131072)
                val outputStream = java.io.BufferedOutputStream(
                    if (isPartial && existingBytes > 0) {
                        FileOutputStream(tmpFile, true)
                    } else {
                        FileOutputStream(tmpFile, false)
                    },
                    131072
                )

                val buffer = ByteArray(131072) // Ultra-high speed 128KB buffer
                var bytesRead: Int
                var currentBytes = if (isPartial) existingBytes else 0L
                var lastProgressUpdate = System.currentTimeMillis()

                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                    currentBytes += bytesRead
                    val now = System.currentTimeMillis()
                    if (now - lastProgressUpdate > 100) {
                        val progress = if (totalBytes > 0) (currentBytes.toFloat() / totalBytes).coerceIn(0f, 1f) else 0.1f
                        updateState(key, AudioDownloadStatus.Downloading(progress))
                        lastProgressUpdate = now
                    }
                }

                outputStream.flush()
                outputStream.close()
                inputStream.close()
                connection.disconnect()

                if (tmpFile.exists() && tmpFile.length() > 0) {
                    tmpFile.renameTo(file)
                    updateState(key, AudioDownloadStatus.Downloaded)
                    onComplete()
                } else {
                    updateState(key, AudioDownloadStatus.NotDownloaded)
                }

            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) {
                    val tmpFile = getTmpFile(reciterId, surahNumber)
                    val downloaded = if (tmpFile.exists()) tmpFile.length() else 0L
                    updateState(key, AudioDownloadStatus.Paused(0f, downloaded))
                } else {
                    e.printStackTrace()
                    val tmpFile = getTmpFile(reciterId, surahNumber)
                    if (tmpFile.exists() && tmpFile.length() > 0) {
                        updateState(key, AudioDownloadStatus.Paused(0f, tmpFile.length()))
                    } else {
                        updateState(key, AudioDownloadStatus.NotDownloaded)
                    }
                }
            } finally {
                activeConnections.remove(key)?.disconnect()
                activeJobs.remove(key)
            }
        }

        activeJobs[key] = job
    }

    fun pauseDownload(reciterId: String, surahNumber: Int) {
        val key = getKey(reciterId, surahNumber)
        activeConnections[key]?.disconnect()
        activeJobs[key]?.cancel()
        activeJobs.remove(key)
        activeConnections.remove(key)

        val tmpFile = getTmpFile(reciterId, surahNumber)
        val downloaded = if (tmpFile.exists()) tmpFile.length() else 0L
        updateState(key, AudioDownloadStatus.Paused(0f, downloaded))
    }

    fun cancelDownload(reciterId: String, surahNumber: Int) {
        val key = getKey(reciterId, surahNumber)
        activeConnections.remove(key)?.disconnect()
        activeJobs.remove(key)?.cancel()

        val tmpFile = getTmpFile(reciterId, surahNumber)
        if (tmpFile.exists()) {
            tmpFile.delete()
        }
        updateState(key, AudioDownloadStatus.NotDownloaded)
    }

    private fun updateState(key: String, status: AudioDownloadStatus) {
        _downloadStates.value = _downloadStates.value + (key to status)
    }
}
