package com.ijackey.iMusic.audio

import android.content.Context
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.LoadControl
import androidx.media3.exoplayer.upstream.DefaultAllocator

object AudioConfig {
    
    /**
     * Create optimized audio attributes for high-quality music playback
     */
    fun createAudioAttributes(): AudioAttributes {
        return AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .setAllowedCapturePolicy(C.ALLOW_CAPTURE_BY_ALL)
            .build()
    }
    
    /**
     * Create optimized load control for high-quality audio streaming
     */
    fun createLoadControl(): LoadControl {
        return DefaultLoadControl.Builder()
            .setAllocator(DefaultAllocator(true, 16))
            .setBufferDurationsMs(
                DefaultLoadControl.DEFAULT_MIN_BUFFER_MS,
                DefaultLoadControl.DEFAULT_MAX_BUFFER_MS,
                DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_MS,
                DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS
            )
            .setTargetBufferBytes(-1)
            .setPrioritizeTimeOverSizeThresholds(true)
            .build()
    }
    
    /**
     * Get supported audio formats for high-quality playback
     */
    fun getSupportedAudioFormats(): List<String> {
        return listOf(
            "mp3", "wav", "flac", "aac", "ogg", "m4a", "wma", "opus",
            "mp4", "3gp", "amr", "awb", "wv", "ape", "dts", "ac3"
        )
    }
    
    /**
     * Check if file is a high-quality audio format
     */
    fun isHighQualityFormat(extension: String): Boolean {
        val hqFormats = listOf("flac", "wav", "ape", "dts", "ac3")
        return hqFormats.contains(extension.lowercase())
    }
    
    /**
     * Get recommended buffer size for high-quality audio
     */
    fun getBufferSizeForQuality(fileSize: Long): Int {
        return when {
            fileSize > 50 * 1024 * 1024 -> 8192 * 4 // Files > 50MB
            fileSize > 20 * 1024 * 1024 -> 8192 * 2 // Files > 20MB
            else -> 8192 // Default
        }
    }
}