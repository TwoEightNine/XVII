package com.twoeightnine.root.xvii.utils

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.support.v4.content.FileProvider
import android.text.TextUtils
import android.util.Log
import com.twoeightnine.root.xvii.BuildConfig
import com.twoeightnine.root.xvii.managers.Lg
import com.twoeightnine.root.xvii.model.Photo
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
        when (requestCode) {
            CHOOSE_PHOTO -> {
                if (data == null)
                    return null
                val selectedImageUri = data.data

                Log.d("testdata", "" + data.extras)

                if (selectedImageUri == null)
                    return null

                val selectedImagePath = FileUtil.getPath(activity, selectedImageUri)
                Log.d("testdata getPath", "" + selectedImagePath)
                return selectedImagePath
            }

            REQUEST_TAKE_PHOTO -> if (photoFile != null && photoFile!!.exists()) {
                return photoFile!!.absolutePath
            } else {
                return null
            }

            REQUEST_MEME -> {
                val path = data!!.getStringExtra(PATH)
                Lg.i("meme: $path")
                if (!TextUtils.isEmpty(path)) {
                    return path
                }
                return null
            }

            REQUEST_STICKER -> {
                val stick = data!!.getStringExtra(STICKER_URL)
                Lg.i("STICKER $stick")
                if (!TextUtils.isEmpty(stick)) {
                    return stick
                }
                return null
            }

            else -> return null
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_${timeStamp}_"
        val storageDir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(
                imageFileName, /* prefix */
                ".jpg", /* suffix */
                storageDir      /* directory */
        )

        // Save a file: path for use with ACTION_VIEW intents
        //        mCurrentPhotoPath = image.getAbsolutePath();
        return image
    }

    fun dispatchTakePictureIntent(frag: Fragment? = null) {
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
            if (photoFile != null) {
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

        val CHOOSE_PHOTO = 187 //in my motherfucking card
        val REQUEST_TAKE_PHOTO = 188
        val REQUEST_MEME = 189
        val REQUEST_STICKER = 190

        val PATH = "path"
        val STICKER_URL = "stickerURL"
    }


}
