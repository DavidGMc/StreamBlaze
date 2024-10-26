package com.androidavid.streamblaze

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem

class RadioService : Service() {
    private lateinit var player: ExoPlayer
    private lateinit var localBroadcastManager: LocalBroadcastManager
    private val CHANNEL_ID = "RadioServiceChannel"
    private val NOTIFICATION_ID = 1

    companion object {
        const val ACTION_PLAY = "com.androidavid.streamblaze.action.PLAY"
        const val ACTION_PAUSE = "com.androidavid.streamblaze.action.PAUSE"
        const val ACTION_STOP = "com.androidavid.streamblaze.action.STOP"
        const val ACTION_TOGGLE_PLAY_PAUSE = "com.androidavid.streamblaze.action.TOGGLE_PLAY_PAUSE"
        const val ACTION_STATUS = "com.androidavid.streamblaze.action.STATUS"
        const val EXTRA_IS_PLAYING = "com.androidavid.streamblaze.extra.IS_PLAYING"
    }

    override fun onCreate() {
        super.onCreate()
        player = ExoPlayer.Builder(this).build()
        localBroadcastManager = LocalBroadcastManager.getInstance(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }
        Log.d("RadioService", "Service created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY -> handlePlayAction(intent)
            ACTION_PAUSE -> handlePauseAction()
            ACTION_STOP -> handleStopAction()
            ACTION_TOGGLE_PLAY_PAUSE -> handleToggleAction()
        }
        return START_STICKY
    }

    private fun handlePlayAction(intent: Intent) {
        val url = intent.getStringExtra("url") ?: return
        Log.d("RadioService", "Playing URL: $url")
        player.setMediaItem(MediaItem.fromUri(url))
        player.prepare()
        player.play()
        startForegroundService()
        sendStatusBroadcast(true)
    }

    private fun handlePauseAction() {
        Log.d("RadioService", "Pausing playback")
        player.pause()
        updateNotification(false)
        sendStatusBroadcast(false)
    }

    private fun handleStopAction() {
        Log.d("RadioService", "Stopping service")
        player.stop()
        stopForeground(true)
        stopSelf()
        sendStatusBroadcast(false)
    }

    private fun handleToggleAction() {
        if (player.isPlaying) {
            handlePauseAction()
        } else {
            player.play()
            startForegroundService()
            sendStatusBroadcast(true)
        }
    }

    private fun startForegroundService() {
        try {
            Log.d("RadioService", "Starting foreground service")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    NOTIFICATION_ID,
                    createNotification(true),
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                )
            } else {
                startForeground(NOTIFICATION_ID, createNotification(true))
            }
        } catch (e: Exception) {
            Log.e("RadioService", "Error starting foreground service", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("RadioService", "Service destroyed")
        player.release()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotification(isPlaying: Boolean): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val playPauseIntent = Intent(this, RadioService::class.java).apply {
            action = ACTION_TOGGLE_PLAY_PAUSE
        }
        val playPausePendingIntent = PendingIntent.getService(
            this,
            0,
            playPauseIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, RadioService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            0,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val playPauseAction = NotificationCompat.Action(
            if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play,
            if (isPlaying) "Pause" else "Play",
            playPausePendingIntent
        )

        val stopAction = NotificationCompat.Action(
            R.drawable.ic_stop,
            "Stop",
            stopPendingIntent
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Radio Streaming")
            .setContentText(if (isPlaying) "Reproduciendo tu radio favorita" else "Radio en pausa")
            .setSmallIcon(if (isPlaying) R.drawable.ic_play else R.drawable.ic_pause)
            .setContentIntent(pendingIntent)
            .addAction(playPauseAction)
            .addAction(stopAction)
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle()
                .setShowActionsInCompactView(0, 1))
            .setOngoing(isPlaying)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }

    private fun updateNotification(isPlaying: Boolean) {
        Log.d("RadioService", "Updating notification: isPlaying = $isPlaying")
        val notification = createNotification(isPlaying)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val channelName = "Radio Service Channel"
        val channelDescription = "Channel for Radio Service notifications"
        val importance = NotificationManager.IMPORTANCE_LOW

        val channel = NotificationChannel(CHANNEL_ID, channelName, importance).apply {
            description = channelDescription
            setShowBadge(false)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun sendStatusBroadcast(isPlaying: Boolean) {
        Log.d("RadioService", "Sending status broadcast: isPlaying = $isPlaying")
        val intent = Intent(ACTION_STATUS).apply {
            putExtra(EXTRA_IS_PLAYING, isPlaying)
        }
        localBroadcastManager.sendBroadcast(intent)
    }
}