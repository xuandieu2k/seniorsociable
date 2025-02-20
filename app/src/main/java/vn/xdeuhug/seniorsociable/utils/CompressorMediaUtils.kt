package vn.xdeuhug.seniorsociable.utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.abedelazizshe.lightcompressorlibrary.CompressionListener
import com.abedelazizshe.lightcompressorlibrary.VideoCompressor
import com.abedelazizshe.lightcompressorlibrary.VideoQuality
import com.abedelazizshe.lightcompressorlibrary.config.AppSpecificStorageConfiguration
import com.abedelazizshe.lightcompressorlibrary.config.Configuration
import com.abedelazizshe.lightcompressorlibrary.config.SaveLocation
import com.abedelazizshe.lightcompressorlibrary.config.SharedStorageConfiguration
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.format
import id.zelory.compressor.constraint.quality
import id.zelory.compressor.constraint.resolution
import id.zelory.compressor.constraint.size
import pyxis.uzuki.live.richutilskt.utils.runOnUiThread
import java.io.File

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 25 / 10 / 2023
 */
object CompressorMediaUtils {
    fun compressorVideo(context: Context, listUris: ArrayList<Uri>) {
        VideoCompressor.start(context = context, // => This is required
            listUris, // => Source can be provided as content uris
            isStreamable = false,
            // THIS STORAGE
            sharedStorageConfiguration = SharedStorageConfiguration(
                saveAt = SaveLocation.movies, // => default is movies
                subFolderName = "compressor-videos" // => optional
            ),
            // OR AND NOT BOTH
            appSpecificStorageConfiguration = AppSpecificStorageConfiguration(
                subFolderName = "compressor-videos" // => optional
            ), configureWith = Configuration(
                quality = VideoQuality.MEDIUM,
                isMinBitrateCheckEnabled = true,
                videoBitrateInMbps = 5, /*Int, ignore, or null*/
                disableAudio = false, /*Boolean, or ignore*/
                keepOriginalResolution = false, /*Boolean, or ignore*/
                videoWidth = 360.0, /*Double, ignore, or null*/
                videoHeight = 480.0, /*Double, ignore, or null*/
                videoNames = listOf<String>() /*list of video names, the size should be similar to the passed uris*/
            ), listener = object : CompressionListener {
                override fun onProgress(index: Int, percent: Float) {
                    // Update UI with progress value
                    runOnUiThread {}
                }

                override fun onStart(index: Int) {
                    // Compression start
                }

                override fun onSuccess(index: Int, size: Long, path: String?) {
                    // On Compression success
                }

                override fun onFailure(index: Int, failureMessage: String) {
                    // On Failure
                }

                override fun onCancelled(index: Int) {
                    // On Cancelled
                }

            })
    }

    suspend fun compressorImage(context: Context, imageFile:File): File {
        val compressedImageFile = Compressor.compress(context, imageFile) {
            resolution(1280, 720)
            quality(80)
            format(Bitmap.CompressFormat.PNG)
            size(2_097_152) // 2 MB
        }
        return compressedImageFile
    }

}