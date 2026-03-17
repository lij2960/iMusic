package com.ijackey.iMusic.audio

import android.content.Context
import android.util.Log
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object WmaConverter {

    private const val TAG = "WmaConverter"

    /** WMA 及其他 ExoPlayer 不支持的格式 */
    private val UNSUPPORTED_FORMATS = setOf("wma", "ac3", "dts", "ape", "wv")

    fun needsConversion(path: String): Boolean {
        val ext = path.substringAfterLast('.', "").lowercase()
        return ext in UNSUPPORTED_FORMATS
    }

    /**
     * 将不支持的格式转换为 AAC (.m4a)，返回转换后的临时文件路径。
     * 如果转换失败或不需要转换，返回 null。
     */
    suspend fun convertToAac(context: Context, sourcePath: String): String? =
        withContext(Dispatchers.IO) {
            val cacheDir = File(context.cacheDir, "converted_audio").also { it.mkdirs() }
            val outFile = File(cacheDir, File(sourcePath).nameWithoutExtension + ".m4a")

            // 已有缓存直接复用
            if (outFile.exists() && outFile.length() > 0) {
                Log.d(TAG, "Using cached: ${outFile.absolutePath}")
                return@withContext outFile.absolutePath
            }

            Log.d(TAG, "Converting: $sourcePath -> ${outFile.absolutePath}")

            val session = FFmpegKit.execute(
                "-y -i \"$sourcePath\" -c:a aac -b:a 192k -vn \"${outFile.absolutePath}\""
            )

            return@withContext if (ReturnCode.isSuccess(session.returnCode)) {
                Log.d(TAG, "Conversion success: ${outFile.absolutePath}")
                outFile.absolutePath
            } else {
                Log.e(TAG, "Conversion failed: ${session.failStackTrace}")
                outFile.delete()
                null
            }
        }

    /** 清理转换缓存 */
    fun clearCache(context: Context) {
        File(context.cacheDir, "converted_audio").deleteRecursively()
    }
}
