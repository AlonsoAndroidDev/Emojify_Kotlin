package com.example.emojify_kotlin

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.IOException
import java.util.jar.Manifest

class MainActivity : AppCompatActivity() {

    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_STORAGE_PERMISSION = 1
    private val FILE_PROVIDER_AUTHORITY = "com.example.android.fileprovider"
    private lateinit var context: Context
    private lateinit var mResultsBitmap: Bitmap
    private var mTempPhotoPath = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        context = applicationContext

        buttonEvents()
    }

    private fun buttonEvents() {
        emojify_button?.setOnClickListener {
            emojifyMe()
        }

        save_button?.setOnClickListener {
            saveMe()
        }

        share_button?.setOnClickListener {
            shareMe()
        }

        clear_button?.setOnClickListener {
            clearImage()
        }
    }

    private fun emojifyMe() {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_STORAGE_PERMISSION
            )
        } else {
            launchCamera()
        }
    }

    private fun launchCamera() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        if (takePictureIntent.resolveActivity(packageManager) != null) {
            var photoFile: File? = null

            try {
                photoFile = BitmapUtils.createTempImageFile(context)
            } catch (e: IOException) {
                e.printStackTrace()
            }

            if (photoFile != null) {
                mTempPhotoPath = photoFile.absolutePath

                var photoUri = FileProvider.getUriForFile(
                    context,
                    FILE_PROVIDER_AUTHORITY,
                    photoFile
                )

                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)

                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            processAndSetImage()
        } else {
            BitmapUtils.deleteImageFile(context, mTempPhotoPath)
        }
    }

    private fun processAndSetImage() {
        emojify_button?.visibility = View.GONE
        title_text_view?.visibility = View.GONE
        clear_button?.show()
        save_button?.show()
        share_button?.show()

        mResultsBitmap = BitmapUtils.resamplePic(context, mTempPhotoPath)
        image_view.setImageBitmap(mResultsBitmap)
    }

    private fun saveMe() {
        BitmapUtils.deleteImageFile(this, mTempPhotoPath)
        BitmapUtils.saveImage(this, mResultsBitmap)
    }

    private fun shareMe() {
        BitmapUtils.deleteImageFile(this, mTempPhotoPath)
        BitmapUtils.saveImage(this, mResultsBitmap)
        BitmapUtils.shareImage(this, mTempPhotoPath)
    }

    private fun clearImage() {
        image_view?.setImageResource(0)
        emojify_button?.visibility = View.VISIBLE
        title_text_view?.visibility = View.VISIBLE
        share_button?.hide()
        save_button?.hide()
        clear_button?.hide()

        BitmapUtils.deleteImageFile(this, mTempPhotoPath)
    }
}
