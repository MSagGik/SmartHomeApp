package io.github.msaggik.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.core.content.FileProvider
import androidx.palette.graphics.Palette
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val TAG = "UTILS_SMART_HOME"

object ImageFileUtils {}

fun Context.uriImageForApp(imageURI: Uri) : Uri? {
    var imageInputStream: InputStream? = null
    var imageOutputStream: FileOutputStream? = null
    try {
        imageInputStream = contentResolver.openInputStream(imageURI)
        val imageFile = createImageFile() ?: return null

        imageOutputStream = FileOutputStream(imageFile)
        BitmapFactory
            .decodeStream(imageInputStream)
            .compress(Bitmap.CompressFormat.JPEG, 50, imageOutputStream)

        return FileProvider.getUriForFile(
            this,
            "${this.packageName}.fileprovider",
            imageFile
        )
    } catch (e: IOException) {
        Log.e(TAG, "[Utils] Error processing image", e)
    } finally {
        imageInputStream?.close()
        imageOutputStream?.close()
    }
    return null
}

fun Context.refreshImageUri(imageURI: Uri?) : Uri? {
    deleteImageFile(imageURI)
    val imageFile = createImageFile()
    return imageFile?.let { file ->
        FileProvider.getUriForFile(
            this,
            "${this.packageName}.fileprovider",
            file
        ) ?: null
    }
}

fun Context.deleteImageFile(imageUri: Uri?) {
    imageUri?.let { uri ->
        when (uri.scheme) {
            "file" -> {
                val fileToDelete = uri.path?.let { File(it) }
                if (fileToDelete != null && fileToDelete.exists()) {
                    val deleted = fileToDelete.delete()
                    if (deleted) {
                        Log.d(TAG, "File ${fileToDelete.name} successfully deleted")
                    } else {
                        Log.e(TAG, "[Utils] Failed to delete file ${fileToDelete.name}")
                    }
                } else {
                    Log.d(TAG, "Previous file missing or path is null")
                }
            }
            "content" -> {
                try {
                    val rowsDeleted = contentResolver.delete(uri, null, null)
                    if (rowsDeleted > 0) {
                        Log.d(TAG, "File successfully deleted via content URI")
                    } else {
                        Log.e(TAG, "[Utils] Failed to delete file via content URI")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "[Utils] Error deleting file via content URI: ${e.message}", e)
                }
            }
            else -> {
                Log.e(TAG, "[Utils] Unsupported URI scheme: ${uri.scheme}")
            }
        }
    } ?: run {
        Log.e(TAG, "[Utils] Provided URI is null")
    }
}

private fun Context.createImageFile(): File? {
    return try {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        File.createTempFile(
            "JPEG_$timeStamp",
            ".jpg",
            storageDir
        )
    } catch (e: IOException) {
        Log.e(TAG, "[Utils] Error creating image file", e)
        null
    }
}

fun Context.extractColorsByUriImage(
    imageUri: Uri,
    reqWidth: Int = 512,
    reqHeight: Int = 512
): List<Int> {
    fun calculateScaleImage(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    fun createBitmapByUriImage(uri: Uri, reqWidth: Int, reqHeight: Int): Bitmap? {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, options)
        }
        options.inSampleSize = calculateScaleImage(options, reqWidth, reqHeight)
        options.inJustDecodeBounds = false
        return contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, options)
        }
    }

    val bitmap = createBitmapByUriImage(imageUri, reqWidth, reqHeight) ?: return emptyList()

    val palette = Palette.Builder(bitmap)
        .addFilter { rgb, hsl -> hsl[2] !in 0.1f..0.9f }
        .maximumColorCount(12)
        .clearFilters()
        .generate()

    return listOfNotNull(
        palette.dominantSwatch?.rgb,
        palette.vibrantSwatch?.rgb,
        palette.mutedSwatch?.rgb,
        palette.darkVibrantSwatch?.rgb,
        palette.lightMutedSwatch?.rgb,
        palette.darkMutedSwatch?.rgb,
        palette.lightVibrantSwatch?.rgb,
    ).take(5)
}