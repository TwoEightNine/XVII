package com.twoeightnine.root.xvii.utils

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.util.SparseArray
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.PermissionChecker
import com.twoeightnine.root.xvii.R

class PermissionHelper {

    private var activity: Activity
    private val callbacks = SparseArray<(() -> Unit)?>()
    private var fragment: androidx.fragment.app.Fragment? = null

    /**
     * should be >=0 and use least 16 bits
     * @return random request code
     */
    private val requestCode: Int
        get() = (Math.random() * Integer.MAX_VALUE).toInt() % 65536

    constructor(activity: Activity) {
        this.activity = activity
    }

    constructor(fragment: androidx.fragment.app.Fragment) : this(fragment.activity
            ?: throw Exception()) {
        this.fragment = fragment
    }

    fun doOrRequest(permission: String, @StringRes title: Int,
                    @StringRes detailMessage: Int, onGranted: (() -> Unit)?) {
        doOrRequest(arrayOf(permission), title, detailMessage, onGranted)
    }

    fun doOrRequest(permission: Array<String>, @StringRes title: Int,
                    @StringRes detailMessage: Int, onGranted: (() -> Unit)?) {
        if (hasPermissions(permission)) {
            onGranted?.invoke()
        } else {
            val requestCode = requestCode
            callbacks.append(requestCode, onGranted)
            showRequestDialog(title, detailMessage, permission, requestCode)
        }
    }

    fun request(permission: Array<String>, onGranted: (() -> Unit)?) {
        if (hasPermissions(permission)) return

        val requestCode = requestCode
        callbacks.append(requestCode, onGranted)
        requestPermissions(permission, requestCode)
    }

    private fun showRequestDialog(@StringRes title: Int,
                                  @StringRes detailMessage: Int,
                                  permission: Array<String>,
                                  requestCode: Int) {
        val dialog = AlertDialog.Builder(activity)
                .setTitle(title)
                .setMessage(detailMessage)
                .setPositiveButton(R.string.ok) { _, _ -> requestPermissions(permission, requestCode) }
                .setNegativeButton(R.string.cancel, null)
                .create()
        dialog.show()
        dialog.stylize()
    }

    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        val callback = callbacks.get(requestCode)
        callbacks.remove(requestCode)
        if (callback == null) return

        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            callback.invoke()
        }
    }

    private fun requestPermissions(permission: Array<String>, requestCode: Int) {
        if (fragment != null) {
            fragment?.requestPermissions(permission, requestCode)
        } else {
            ActivityCompat.requestPermissions(activity, permission, requestCode)
        }
    }

    fun hasStoragePermissions() = hasPermissions(arrayOf(READ_STORAGE, WRITE_STORAGE))

    fun hasRecordAudioPermissions() = hasPermissions(arrayOf(RECORD_AUDIO))

    private fun hasPermission(permission: String): Boolean {
        val check = PermissionChecker.checkSelfPermission(activity, permission)
        return check == PackageManager.PERMISSION_GRANTED
    }

    private fun hasPermissions(permissions: Array<String>): Boolean {
        for (permission in permissions) {
            if (!hasPermission(permission)) {
                return false
            }
        }
        return true
    }

    companion object {

        const val READ_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE
        const val WRITE_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE
        const val RECORD_AUDIO = Manifest.permission.RECORD_AUDIO
    }

}