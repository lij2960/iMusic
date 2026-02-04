package com.ijackey.iMusic.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.ui.PlayerNotificationManager
import com.ijackey.iMusic.MainActivity
import com.ijackey.iMusic.R
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MusicService : MediaSessionService() {
    
    @Inject
    lateinit var exoPlayer: ExoPlayer
    
    private var mediaSession: MediaSession? = null
    private var playerNotificationManager: PlayerNotificationManager? = null
    
    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "music_playback"
    }
    
    override fun onCreate() {
        super.onCreate()
        
        createNotificationChannel()
        
        // 立即启动前台服务
        startForegroundService()
        
        mediaSession = MediaSession.Builder(this, exoPlayer)
            .build()
            
        setupPlayerNotificationManager()
    }
    
    private fun startForegroundService() {
        val notification = createInitialNotification()
        startForeground(NOTIFICATION_ID, notification)
    }
    
    private fun createInitialNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("iMusic")
            .setContentText("音乐服务已启动")
            .setSmallIcon(R.drawable.ic_music_note)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }
    
    private fun setupPlayerNotificationManager() {
        playerNotificationManager = PlayerNotificationManager.Builder(
            this,
            NOTIFICATION_ID,
            CHANNEL_ID
        )
        .setMediaDescriptionAdapter(object : PlayerNotificationManager.MediaDescriptionAdapter {
            override fun getCurrentContentTitle(player: Player): CharSequence {
                return player.mediaMetadata.title ?: "未知歌曲"
            }
            
            override fun createCurrentContentIntent(player: Player): PendingIntent? {
                val intent = Intent(this@MusicService, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                }
                return PendingIntent.getActivity(
                    this@MusicService,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            }
            
            override fun getCurrentContentText(player: Player): CharSequence? {
                return player.mediaMetadata.artist ?: "未知艺术家"
            }
            
            override fun getCurrentLargeIcon(
                player: Player,
                callback: PlayerNotificationManager.BitmapCallback
            ): Bitmap? {
                val artworkUri = player.mediaMetadata.artworkUri
                return if (artworkUri != null) {
                    try {
                        BitmapFactory.decodeFile(artworkUri.path)
                    } catch (e: Exception) {
                        BitmapFactory.decodeResource(resources, R.drawable.default_album_art)
                    }
                } else {
                    BitmapFactory.decodeResource(resources, R.drawable.default_album_art)
                }
            }
        })
        .setNotificationListener(object : PlayerNotificationManager.NotificationListener {
            override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
                stopForeground(true)
                stopSelf()
            }
            
            override fun onNotificationPosted(
                notificationId: Int,
                notification: Notification,
                ongoing: Boolean
            ) {
                if (ongoing) {
                    startForeground(notificationId, notification)
                } else {
                    stopForeground(false)
                }
            }
        })
        .setSmallIconResourceId(R.drawable.ic_music_note)
        .build()
        
        playerNotificationManager?.setPlayer(exoPlayer)
        playerNotificationManager?.setUseRewindAction(false)
        playerNotificationManager?.setUseFastForwardAction(false)
        playerNotificationManager?.setUseNextAction(true)
        playerNotificationManager?.setUsePreviousAction(true)
        playerNotificationManager?.setUsePlayPauseActions(true)
    }
    
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }
    
    override fun onDestroy() {
        playerNotificationManager?.setPlayer(null)
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "音乐播放",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "音乐播放控制"
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
}