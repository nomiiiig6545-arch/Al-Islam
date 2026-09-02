package com.example.data.mushaf

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.MainActivity
import com.example.R
import com.example.data.db.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.io.File

/**
 * WorkManager-based background task to reliably download all 549 Mushaf images
 * in the background and store them in the app's internal filesystem (filesDir/mushaf_pages).
 *
 * Emits real-time progress updates via WorkManager `setProgress` and posts an Android
 * notification with an interactive progress bar so the user can track downloads even when the app is in background.
 */
class MushafDownloadWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val WORK_NAME = "mushaf_all_pages_download_work"
        const val TAG = "mushaf_download"

        const val CHANNEL_ID = "mushaf_download_channel"
        const val NOTIFICATION_ID = 4001

        const val KEY_PROGRESS_PERCENT = "progress_percent"
        const val KEY_DOWNLOADED_COUNT = "downloaded_count"
        const val KEY_TOTAL_COUNT = "total_count"
        const val KEY_CURRENT_PAGE = "current_page"
        const val KEY_STORAGE_BYTES = "storage_bytes"
        const val KEY_IS_COMPLETED = "is_completed"
        const val KEY_ERROR_MESSAGE = "error_message"
    }

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val manager = MushafPageDownloadManager.getInstance(context)
        val database = AppDatabase.getDatabase(context)
        val mushafPageDao = database.mushafPageDao()

        createNotificationChannel()

        manager.setDownloadingState(true)

        try {
            val totalPages = IndoPakMushafData.TOTAL_PAGES
            val missingPages = (1..totalPages).filter { !manager.isPageOffline(it) }

            if (missingPages.isEmpty()) {
                manager.refreshOfflineStatus()
                manager.setDownloadingState(false)
                
                setProgress(
                    workDataOf(
                        KEY_PROGRESS_PERCENT to 100,
                        KEY_DOWNLOADED_COUNT to totalPages,
                        KEY_TOTAL_COUNT to totalPages,
                        KEY_CURRENT_PAGE to totalPages,
                        KEY_IS_COMPLETED to true
                    )
                )
                showCompletionNotification(totalPages)
                return@withContext Result.success()
            }

            var currentDownloaded = manager.getOfflinePagesCount()
            var progressPercent = ((currentDownloaded.toFloat() / totalPages) * 100).toInt()

            updateNotificationProgress(currentDownloaded, totalPages, missingPages.firstOrNull() ?: 1)

            setProgress(
                workDataOf(
                    KEY_PROGRESS_PERCENT to progressPercent,
                    KEY_DOWNLOADED_COUNT to currentDownloaded,
                    KEY_TOTAL_COUNT to totalPages,
                    KEY_CURRENT_PAGE to (missingPages.firstOrNull() ?: 1),
                    KEY_STORAGE_BYTES to manager.calculateTotalStorageBytes(),
                    KEY_IS_COMPLETED to false
                )
            )

            // Fast concurrent batch downloads with pause/cancel checks
            val chunks = missingPages.chunked(16)
            for (chunk in chunks) {
                if (isStopped || manager.isCancelled()) {
                    notificationManager.cancel(NOTIFICATION_ID)
                    manager.setDownloadingState(false)
                    return@withContext Result.failure()
                }

                while (manager.isPaused()) {
                    kotlinx.coroutines.delay(300)
                    if (isStopped || manager.isCancelled()) {
                        notificationManager.cancel(NOTIFICATION_ID)
                        manager.setDownloadingState(false)
                        return@withContext Result.failure()
                    }
                }

                val lastPageInChunk = chunk.last()
                manager.setCurrentDownloadingPage(lastPageInChunk)

                coroutineScope {
                    chunk.map { page ->
                        async(Dispatchers.IO) {
                            if (!manager.isPageOffline(page)) {
                                manager.downloadAndSavePage(page)
                            } else true
                        }
                    }.awaitAll()
                }

                currentDownloaded = manager.getOfflinePagesCount()
                progressPercent = ((currentDownloaded.toFloat() / totalPages) * 100).toInt()

                // Update WorkManager progress and notification after each batch
                try {
                    setProgress(
                        workDataOf(
                            KEY_PROGRESS_PERCENT to progressPercent,
                            KEY_DOWNLOADED_COUNT to currentDownloaded,
                            KEY_TOTAL_COUNT to totalPages,
                            KEY_CURRENT_PAGE to lastPageInChunk,
                            KEY_STORAGE_BYTES to manager.calculateTotalStorageBytes(),
                            KEY_IS_COMPLETED to (currentDownloaded >= totalPages)
                        )
                    )
                } catch (_: Exception) {}

                updateNotificationProgress(currentDownloaded, totalPages, lastPageInChunk)
            }

            manager.refreshOfflineStatus()
            manager.setDownloadingState(false)

            val finalCount = manager.getOfflinePagesCount()
            showCompletionNotification(finalCount)

            setProgress(
                workDataOf(
                    KEY_PROGRESS_PERCENT to 100,
                    KEY_DOWNLOADED_COUNT to finalCount,
                    KEY_TOTAL_COUNT to totalPages,
                    KEY_CURRENT_PAGE to totalPages,
                    KEY_STORAGE_BYTES to manager.calculateTotalStorageBytes(),
                    KEY_IS_COMPLETED to true
                )
            )

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            manager.setErrorState(e.localizedMessage ?: "Background download failed")
            manager.setDownloadingState(false)
            notificationManager.cancel(NOTIFICATION_ID)
            Result.retry()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Mushaf Offline Downloads",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows real-time progress while downloading 549 Mushaf pages for offline reading."
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun updateNotificationProgress(downloaded: Int, total: Int, currentPage: Int) {
        val percentage = if (total > 0) ((downloaded.toFloat() / total) * 100).toInt() else 0
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Downloading 16-Line Mushaf ($percentage%)")
            .setContentText("Saving page $currentPage of $total to local storage ($downloaded/$total)")
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setProgress(total, downloaded, false)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun showCompletionNotification(total: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Mushaf Offline Download Complete")
            .setContentText("All $total pages are saved locally. Ready for 100% offline reading in Airplane Mode.")
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}
