package com.example.service

import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.ServiceCompat

object QuranAudioBridge {
    var onPlayAction: (() -> Unit)? = null
    var onPauseAction: (() -> Unit)? = null
    var onNextAction: (() -> Unit)? = null
    var onPrevAction: (() -> Unit)? = null
    var onSeekAction: ((Long) -> Unit)? = null
    var onStopAction: (() -> Unit)? = null

    fun startServiceWithState(
        context: Context,
        surahName: String,
        surahArabic: String,
        reciterName: String,
        isPlaying: Boolean,
        positionMs: Long = 0L,
        durationMs: Long = 0L,
        imageRes: Int = 0
    ) {
        val intent = Intent(context, AudioPlaybackService::class.java).apply {
            putExtra("surahName", surahName)
            putExtra("surahArabic", surahArabic)
            putExtra("reciterName", reciterName)
            putExtra("isPlaying", isPlaying)
            putExtra("positionMs", positionMs)
            putExtra("durationMs", durationMs)
            putExtra("imageRes", imageRes)
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && isPlaying) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopService(context: Context) {
        val intent = Intent(context, AudioPlaybackService::class.java).apply {
            action = AudioNotificationHelper.ACTION_STOP
        }
        try {
            context.startService(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

class AudioPlaybackService : Service() {

    private var mediaSession: MediaSessionCompat? = null
    private var isCurrentPlaying: Boolean = false

    override fun onCreate() {
        super.onCreate()
        try {
            mediaSession = MediaSessionCompat(this, "QuranMediaPlaybackSession").apply {
                setFlags(
                    MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or
                    MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
                )
                setCallback(object : MediaSessionCompat.Callback() {
                    override fun onPlay() {
                        QuranAudioBridge.onPlayAction?.invoke()
                    }

                    override fun onPause() {
                        QuranAudioBridge.onPauseAction?.invoke()
                    }

                    override fun onSkipToNext() {
                        QuranAudioBridge.onNextAction?.invoke()
                    }

                    override fun onSkipToPrevious() {
                        QuranAudioBridge.onPrevAction?.invoke()
                    }

                    override fun onSeekTo(pos: Long) {
                        QuranAudioBridge.onSeekAction?.invoke(pos)
                    }

                    override fun onStop() {
                        QuranAudioBridge.onStopAction?.invoke()
                        stopForeground(STOP_FOREGROUND_REMOVE)
                        stopSelf()
                    }
                })
                isActive = true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action

        when (action) {
            AudioNotificationHelper.ACTION_PLAY -> {
                QuranAudioBridge.onPlayAction?.invoke()
            }
            AudioNotificationHelper.ACTION_PAUSE -> {
                QuranAudioBridge.onPauseAction?.invoke()
            }
            AudioNotificationHelper.ACTION_PREV -> {
                QuranAudioBridge.onPrevAction?.invoke()
            }
            AudioNotificationHelper.ACTION_NEXT -> {
                QuranAudioBridge.onNextAction?.invoke()
            }
            AudioNotificationHelper.ACTION_STOP -> {
                isCurrentPlaying = false
                QuranAudioBridge.onStopAction?.invoke()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }
            AudioNotificationHelper.ACTION_DISMISS -> {
                if (!isCurrentPlaying) {
                    QuranAudioBridge.onStopAction?.invoke()
                    ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
                    stopSelf()
                }
                return START_NOT_STICKY
            }
            else -> {
                val surahName = intent?.getStringExtra("surahName") ?: "Surah"
                val surahArabic = intent?.getStringExtra("surahArabic") ?: ""
                val reciterName = intent?.getStringExtra("reciterName") ?: "Reciter"
                val isPlaying = intent?.getBooleanExtra("isPlaying", false) ?: false
                val positionMs = intent?.getLongExtra("positionMs", 0L) ?: 0L
                val durationMs = intent?.getLongExtra("durationMs", 0L) ?: 0L
                val imageRes = intent?.getIntExtra("imageRes", 0) ?: 0

                isCurrentPlaying = isPlaying

                val notification = AudioNotificationHelper.buildNotification(
                    context = this,
                    mediaSession = mediaSession,
                    surahName = surahName,
                    surahArabic = surahArabic,
                    reciterName = reciterName,
                    isPlaying = isPlaying,
                    positionMs = positionMs,
                    durationMs = durationMs,
                    imageRes = imageRes
                )

                if (isPlaying) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        ServiceCompat.startForeground(
                            this,
                            AudioNotificationHelper.NOTIFICATION_ID,
                            notification,
                            ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                        )
                    } else {
                        startForeground(AudioNotificationHelper.NOTIFICATION_ID, notification)
                    }
                } else {
                    ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_DETACH)
                    val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    manager.notify(AudioNotificationHelper.NOTIFICATION_ID, notification)
                }
            }
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            mediaSession?.isActive = false
            mediaSession?.release()
            mediaSession = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
        stopForeground(STOP_FOREGROUND_REMOVE)
    }
}
