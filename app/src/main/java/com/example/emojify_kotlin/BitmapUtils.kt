package com.example.emojify_kotlin

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.support.v4.content.FileProvider
import android.util.DisplayMetrics
import android.view.WindowManager
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class BitmapUtils {

    val FILE_PROVIDER_AUTHORITY = "com.example.android.fileprovider"

    fun resamplePic(context: Context, imagePath : String) : Bitmap{
        var metrics = DisplayMetrics()
        var manager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        manager.defaultDisplay.getMetrics(metrics)

        var targetH = metrics.heightPixels
        var targetW = metrics.widthPixels

        var bmpOptions = BitmapFactory.Options()
        bmpOptions.inJustDecodeBounds = true
        BitmapFactory.decodeFile(imagePath, bmpOptions)

        var photoW = bmpOptions.outWidth
        var photoH = bmpOptions.outHeight

        var scaleFactor = Math.min(photoW/targetW, photoH/targetH)

        bmpOptions.inJustDecodeBounds = false
        bmpOptions.inSampleSize = scaleFactor

        return BitmapFactory.decodeFile(imagePath)
    }

    fun createTempImageFile(context: Context): File {
        val timeStamp = SimpleDateFormat(
            "yyyyMMdd_HHmmss",
            Locale.getDefault()
        ).format(Date())

        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = context.externalCacheDir

        return File.createTempFile(
            imageFileName, /* prefix */
            ".jpg", /* suffix */
            storageDir      /* directory */
        )
    }

    fun deleteImageFile(context: Context, imagePath: String): Boolean {
        val imageFile = File(imagePath)

        val deleted = imageFile.delete()

        if (!deleted) {
            val errorMessage = context.getString(R.string.error)
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
        }

        return deleted
    }

    private fun galleryAddPic(context: Context, imagePath: String) {
        val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        val f = File(imagePath)
        val contentUri = Uri.fromFile(f)
        mediaScanIntent.data = contentUri
        context.sendBroadcast(mediaScanIntent)
    }

    fun saveImage(context: Context, image: Bitmap): String? {

        var savedImagePath: String? = null

        val timeStamp = SimpleDateFormat(
            "yyyyMMdd_HHmmss",
            Locale.getDefault()
        ).format(Date())
        val imageFileName = "JPEG_$timeStamp.jpg"
        val storageDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/Emojify"
        )
        var success = true
        if (!storageDir.exists()) {
            success = storageDir.mkdirs()
        }

        if (success) {
            val imageFile = File(storageDir, imageFileName)
            savedImagePath = imageFile.absolutePath
            try {
                val fOut = FileOutputStream(imageFile)
                image.compress(Bitmap.CompressFormat.JPEG, 100, fOut)
                fOut.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            galleryAddPic(context, savedImagePath)

            val savedMessage = context.getString(R.string.saved_message, savedImagePath)
            Toast.makeText(context, savedMessage, Toast.LENGTH_SHORT).show()
        }

        return savedImagePath
    }

    fun shareImage(context: Context, imagePath: String) {
        val imageFile = File(imagePath)
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "image/*"
        val photoURI = FileProvider.getUriForFile(context, FILE_PROVIDER_AUTHORITY, imageFile)
        shareIntent.putExtra(Intent.EXTRA_STREAM, photoURI)
        context.startActivity(shareIntent)
    }
}