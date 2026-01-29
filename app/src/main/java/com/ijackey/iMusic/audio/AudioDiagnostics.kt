package com.ijackey.iMusic.audio

import android.content.Context
import android.media.MediaMetadataRetriever
import android.util.Log
import java.io.File

object AudioDiagnostics {
    
    private const val TAG = "AudioDiagnostics"
    
    /**
     * Diagnose audio file for playback issues
     */
    fun diagnoseAudioFile(filePath: String): AudioFileInfo {
        val file = File(filePath)
        val info = AudioFileInfo(filePath)
        
        if (!file.exists()) {
            info.issues.add("File does not exist")
            return info
        }
        
        if (!file.canRead()) {
            info.issues.add("File cannot be read - permission issue")
            return info
        }
        
        info.fileSize = file.length()
        info.extension = file.extension.lowercase()
        
        // Check file size
        if (info.fileSize == 0L) {
            info.issues.add("File is empty (0 bytes)")
            return info
        }
        
        if (info.fileSize < 1024) {
            info.issues.add("File is too small (${info.fileSize} bytes) - possibly corrupted")
        }
        
        // Try to extract metadata
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(filePath)
            
            info.duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
            info.bitrate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)?.toIntOrNull() ?: 0
            info.sampleRate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CAPTURE_FRAMERATE)?.toIntOrNull() ?: 0
            info.mimeType = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE) ?: "unknown"
            
            // Check for high-quality indicators
            if (info.bitrate > 320000) {
                info.isHighQuality = true
                info.qualityNotes.add("High bitrate: ${info.bitrate / 1000}kbps")
            }
            
            if (info.extension in listOf("flac", "wav", "ape")) {
                info.isLossless = true
                info.qualityNotes.add("Lossless format: ${info.extension.uppercase()}")
            }
            
            // Check for potential issues
            if (info.duration == 0L) {
                info.issues.add("Duration is 0 - file may be corrupted or unsupported format")
            }
            
            if (info.bitrate == 0) {
                info.issues.add("Cannot determine bitrate - file may be corrupted")
            }
            
            if (info.mimeType == "unknown") {
                info.issues.add("Unknown MIME type - format may not be supported")
            }
            
        } catch (e: Exception) {
            info.issues.add("Cannot read metadata: ${e.message}")
            Log.e(TAG, "Error reading metadata for $filePath", e)
        } finally {
            try {
                retriever.release()
            } catch (e: Exception) {
                Log.w(TAG, "Error releasing MediaMetadataRetriever", e)
            }
        }
        
        return info
    }
    
    /**
     * Log detailed audio file information
     */
    fun logAudioFileInfo(info: AudioFileInfo) {
        Log.d(TAG, "=== Audio File Diagnostics ===")
        Log.d(TAG, "File: ${info.filePath}")
        Log.d(TAG, "Size: ${info.fileSize} bytes (${info.fileSize / 1024 / 1024}MB)")
        Log.d(TAG, "Extension: ${info.extension}")
        Log.d(TAG, "Duration: ${info.duration}ms (${info.duration / 1000}s)")
        Log.d(TAG, "Bitrate: ${info.bitrate}bps (${info.bitrate / 1000}kbps)")
        Log.d(TAG, "Sample Rate: ${info.sampleRate}Hz")
        Log.d(TAG, "MIME Type: ${info.mimeType}")
        Log.d(TAG, "High Quality: ${info.isHighQuality}")
        Log.d(TAG, "Lossless: ${info.isLossless}")
        
        if (info.qualityNotes.isNotEmpty()) {
            Log.d(TAG, "Quality Notes:")
            info.qualityNotes.forEach { Log.d(TAG, "  - $it") }
        }
        
        if (info.issues.isNotEmpty()) {
            Log.w(TAG, "Issues Found:")
            info.issues.forEach { Log.w(TAG, "  - $it") }
        }
        
        Log.d(TAG, "==============================")
    }
    
    /**
     * Get recommended ExoPlayer settings for this file
     */
    fun getRecommendedSettings(info: AudioFileInfo): Map<String, Any> {
        val settings = mutableMapOf<String, Any>()
        
        // Buffer size based on file size and quality
        val bufferSize = when {
            info.isLossless -> 16384 * 4
            info.isHighQuality -> 16384 * 2
            info.fileSize > 20 * 1024 * 1024 -> 16384
            else -> 8192
        }
        settings["bufferSize"] = bufferSize
        
        // Decoder priority
        settings["preferSoftwareDecoder"] = info.issues.isNotEmpty()
        
        // Audio session handling
        settings["handleAudioFocus"] = true
        settings["handleAudioBecomingNoisy"] = true
        
        return settings
    }
}

data class AudioFileInfo(
    val filePath: String,
    var fileSize: Long = 0L,
    var extension: String = "",
    var duration: Long = 0L,
    var bitrate: Int = 0,
    var sampleRate: Int = 0,
    var mimeType: String = "",
    var isHighQuality: Boolean = false,
    var isLossless: Boolean = false,
    val issues: MutableList<String> = mutableListOf(),
    val qualityNotes: MutableList<String> = mutableListOf()
)