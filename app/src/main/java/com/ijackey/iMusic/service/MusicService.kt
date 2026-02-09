package com.ijackey.iMusic.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.ijackey.iMusic.MainActivity
import com.ijackey.iMusic.R
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MusicService : MediaSessionService() {
    
    @Inject
    lateinit var exoPlayer: ExoPlayer
    
    private var mediaSession: MediaSession? = null
    
    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "music_playback"
    }
    
    override fun onCreate() {
        super.onCreate()
        
        createNotificationChannel()
        
        mediaSession = MediaSession.Builder(this, exoPlayer)
            .build()
        
        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                android.util.Log.d("MusicService", "onIsPlayingChanged: $isPlaying")
                updateNotification()
            }
            
            override fun onMediaMetadataChanged(mediaMetadata: androidx.media3.common.MediaMetadata) {
                android.util.Log.d("MusicService", "onMediaMetadataChanged: ${mediaMetadata.title}")
                updateNotification()
            }
            
            override fun onMediaItemTransition(mediaItem: androidx.media3.common.MediaItem?, reason: Int) {
                android.util.Log.d("MusicService", "onMediaItemTransition: ${mediaItem?.mediaMetadata?.title}")
                updateNotification()
            }
        })
        
        // 立即创建初始通知
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)
    }
    
    private fun updateNotification() {
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)
    }
    
    private fun createNotification(): Notification {
        val metadata = exoPlayer.mediaMetadata
        val title = metadata.title?.toString() ?: "iMusic"
        val artist = metadata.artist?.toString() ?: "准备播放"
        
        android.util.Log.d("MusicService", "createNotification - title: $title, artist: $artist")
        
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // 获取封面
        val albumArt = try {
            metadata.artworkUri?.path?.let { 
                android.util.Log.d("MusicService", "Loading album art from: $it")
                BitmapFactory.decodeFile(it)
            } ?: BitmapFactory.decodeResource(resources, R.drawable.default_album_art)
        } catch (e: Exception) {
            android.util.Log.e("MusicService", "Error loading album art: ${e.message}")
            BitmapFactory.decodeResource(resources, R.drawable.default_album_art)
        }
        
        // 创建播放/暂停按钮
        val playPauseAction = if (exoPlayer.isPlaying) {
            NotificationCompat.Action(
                R.drawable.ic_pause,
                "暂停",
                createPendingIntent("PAUSE")
            )
        } else {
            NotificationCompat.Action(
                R.drawable.ic_play,
                "播放",
                createPendingIntent("PLAY")
            )
        }
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(artist)
            .setSmallIcon(R.drawable.ic_music_note)
            .setLargeIcon(albumArt)
            .setContentIntent(pendingIntent)
            .setOngoing(exoPlayer.isPlaying)
            .setShowWhen(false)
            .addAction(R.drawable.ic_previous, "上一曲", createPendingIntent("PREVIOUS"))
            .addAction(playPauseAction)
            .addAction(R.drawable.ic_next, "下一曲", createPendingIntent("NEXT"))
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setShowActionsInCompactView(0, 1, 2)
            )
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }
    
    private fun createPendingIntent(action: String): PendingIntent {
        val intent = Intent(this, MusicService::class.java).apply {
            this.action = action
        }
        return PendingIntent.getService(
            this,
            action.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        android.util.Log.d("MusicService", "onStartCommand - action: ${intent?.action}")
        when (intent?.action) {
            "PLAY" -> {
                android.util.Log.d("MusicService", "Play action")
                exoPlayer.play()
            }
            "PAUSE" -> {
                android.util.Log.d("MusicService", "Pause action")
                exoPlayer.pause()
            }
            "NEXT" -> {
                android.util.Log.d("MusicService", "Next action - hasNext: ${exoPlayer.hasNextMediaItem()}")
                if (exoPlayer.hasNextMediaItem()) {
                    exoPlayer.seekToNextMediaItem()
                } else {
                    exoPlayer.seekTo(0, 0)
                }
            }
            "PREVIOUS" -> {
                android.util.Log.d("MusicService", "Previous action - hasPrevious: ${exoPlayer.hasPreviousMediaItem()}")
                if (exoPlayer.hasPreviousMediaItem()) {
                    exoPlayer.seekToPreviousMediaItem()
                } else {
                    exoPlayer.seekTo(exoPlayer.mediaItemCount - 1, 0)
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }
    
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }
    
    override fun onDestroy() {
        mediaSession?.run {
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