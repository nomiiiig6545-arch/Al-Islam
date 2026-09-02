package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.os.Build
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat.MediaStyle
import com.example.MainActivity
import com.example.R

object AudioNotificationHelper {

    const val CHANNEL_ID = "quran_audio_playback_channel_v2"
    const val CHANNEL_NAME = "Quran Audio Playback"
    const val NOTIFICATION_ID = 1001

    const val ACTION_PLAY = "com.example.action.PLAY"
    const val ACTION_PAUSE = "com.example.action.PAUSE"
    const val ACTION_PREV = "com.example.action.PREV"
    const val ACTION_NEXT = "com.example.action.NEXT"
    const val ACTION_STOP = "com.example.action.STOP"
    const val ACTION_DISMISS = "com.example.action.DISMISS"

    private var cachedBitmap: Bitmap? = null
    private var lastResId: Int = -1

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Controls and status for Quran audio playback"
                setShowBadge(false)
                setSound(null, null)
                enableVibration(false)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun getCircularBitmap(context: Context, resId: Int): Bitmap? {
        if (resId == 0) return null
        if (resId == lastResId && cachedBitmap != null && !cachedBitmap!!.isRecycled) {
            return cachedBitmap
        }
        return try {
            val original = BitmapFactory.decodeResource(context.resources, resId) ?: return null
            val minEdge = minOf(original.width, original.height)
            val output = Bitmap.createBitmap(minEdge, minEdge, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(output)
            val paint = Paint().apply {
                isAntiAlias = true
            }
            val radius = minEdge / 2f
            canvas.drawCircle(radius, radius, radius, paint)
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
            val srcRect = Rect((original.width - minEdge) / 2, (original.height - minEdge) / 2, (original.width + minEdge) / 2, (original.height + minEdge) / 2)
            val dstRect = Rect(0, 0, minEdge, minEdge)
            canvas.drawBitmap(original, srcRect, dstRect, paint)
            cachedBitmap = output
            lastResId = resId
            output
        } catch (e: Exception) {
            null
        }
    }

    fun buildNotification(
        context: Context,
        mediaSession: MediaSessionCompat?,
        surahName: String,
        surahArabic: String,
        reciterName: String,
        isPlaying: Boolean,
        positionMs: Long = 0L,
        durationMs: Long = 0L,
        imageRes: Int = 0
    ): Notification {
        createNotificationChannel(context)

        // Activity Intent to bring app back to foreground when tapped
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            0,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Service Action Intents for Notification Buttons
        val prevIntent = PendingIntent.getService(
            context,
            1,
            Intent(context, AudioPlaybackService::class.java).apply { action = ACTION_PREV },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val playPauseAction = if (isPlaying) ACTION_PAUSE else ACTION_PLAY
        val playPauseIntent = PendingIntent.getService(
            context,
            2,
            Intent(context, AudioPlaybackService::class.java).apply { action = playPauseAction },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val nextIntent = PendingIntent.getService(
            context,
            3,
            Intent(context, AudioPlaybackService::class.java).apply { action = ACTION_NEXT },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = PendingIntent.getService(
            context,
            4,
            Intent(context, AudioPlaybackService::class.java).apply { action = ACTION_STOP },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val dismissIntent = PendingIntent.getService(
            context,
            5,
            Intent(context, AudioPlaybackService::class.java).apply { action = ACTION_DISMISS },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val playPauseIcon = if (isPlaying) {
            android.R.drawable.ic_media_pause
        } else {
            android.R.drawable.ic_media_play
        }
        val playPauseTitle = if (isPlaying) "Pause" else "Play"

        val title = if (surahArabic.isNotBlank()) "$surahName ($surahArabic)" else surahName
        val artBitmap = getCircularBitmap(context, imageRes)

        // Update MediaSessionCompat Metadata and PlaybackState
        mediaSession?.let { session ->
            val metadataBuilder = MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, reciterName)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, "Al-Quran Al-Kareem")
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, title)
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, reciterName)
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, if (durationMs > 0) durationMs else -1L)

            if (artBitmap != null) {
                metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, artBitmap)
                metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ART, artBitmap)
                metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON, artBitmap)
            }
            session.setMetadata(metadataBuilder.build())

            val playbackState = PlaybackStateCompat.Builder()
                .setActions(
                    PlaybackStateCompat.ACTION_PLAY or
                    PlaybackStateCompat.ACTION_PAUSE or
                    PlaybackStateCompat.ACTION_PLAY_PAUSE or
                    PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                    PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                    PlaybackStateCompat.ACTION_SEEK_TO or
                    PlaybackStateCompat.ACTION_STOP
                )
                .setState(
                    if (isPlaying) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED,
                    positionMs,
                    1.0f
                )
                .build()

            session.setPlaybackState(playbackState)
            session.isActive = true
        }

        val mediaStyle = MediaStyle()
            .setShowActionsInCompactView(0, 1, 2)
            .setShowCancelButton(true)
            .setCancelButtonIntent(stopIntent)

        mediaSession?.let {
            mediaStyle.setMediaSession(it.sessionToken)
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(reciterName)
            .setSubText(if (surahArabic.isNotBlank()) surahArabic else "Al-Quran")
            .setContentIntent(contentPendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
            .setPriority(if (isPlaying) NotificationCompat.PRIORITY_DEFAULT else NotificationCompat.PRIORITY_LOW)
            .setOngoing(isPlaying)
            .setOnlyAlertOnce(true)
            .setStyle(mediaStyle)
            .addAction(android.R.drawable.ic_media_previous, "Previous", prevIntent)
            .addAction(playPauseIcon, playPauseTitle, playPauseIntent)
            .addAction(android.R.drawable.ic_media_next, "Next", nextIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop", stopIntent)

        if (!isPlaying) {
            builder.setDeleteIntent(dismissIntent)
        } else {
            builder.setDeleteIntent(null)
        }

        if (artBitmap != null) {
            builder.setLargeIcon(artBitmap)
        }

        val notification = builder.build()
        if (isPlaying) {
            notification.flags = notification.flags or Notification.FLAG_ONGOING_EVENT or Notification.FLAG_NO_CLEAR
        } else {
            notification.flags = notification.flags and Notification.FLAG_ONGOING_EVENT.inv() and Notification.FLAG_NO_CLEAR.inv()
        }

        return notification
    }
}
