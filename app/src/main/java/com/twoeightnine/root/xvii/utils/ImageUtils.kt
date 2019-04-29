package com.twoeightnine.root.xvii.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.twoeightnine.root.xvii.BuildConfig
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by root on 1/12/17.
 */

class ImageUtils(private val activity: Activity) {

    private var photoFile: File? = null

    fun getPath(requestCode: Int, data: Intent?): String? {
        return when (requestCode) {

            REQUEST_TAKE_PHOTO -> if (photoFile != null && photoFile!!.exists()) {
                photoFile!!.absolutePath
            } else {
                null
            }

            else -> null
        }
    }

    @SuppressLint("SimpleDateFormat")
    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_${timeStamp}_"
        val storageDir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(imageFileName, ".jpg", storageDir)
    }

    fun dispatchTakePictureIntent(frag: androidx.fragment.app.Fragment? = null) {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(activity.packageManager) != null) {
            // Create the File where the photo should go
            photoFile = null
            try {
                photoFile = createImageFile()
            } catch (ex: IOException) {
                // Error occurred while creating the File
                ex.printStackTrace()
            }

            // Continue only if the File was successfully created
            photoFile?.let { photoFile ->
                val pckg = "${BuildConfig.APPLICATION_ID}.provider"
                val photoUri = FileProvider.getUriForFile(activity, pckg, photoFile)
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                val resInfoList = activity.packageManager.queryIntentActivities(takePictureIntent, PackageManager.MATCH_DEFAULT_ONLY)
                for (resolveInfo in resInfoList) {
                    val packageName = resolveInfo.activityInfo.packageName
                    activity.grantUriPermission(packageName, photoUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                if (frag != null) {
                    frag.startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO)
                } else {
                    activity.startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO)
                }
            }
        }
    }

    companion object {

        const val REQUEST_TAKE_PHOTO = 188
    }
}
