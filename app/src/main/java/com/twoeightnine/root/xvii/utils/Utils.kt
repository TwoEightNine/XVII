package com.twoeightnine.root.xvii.utils

import android.annotation.TargetApi
import android.app.*
import android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
import android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE
import android.content.*
import android.content.Intent.*
import android.content.pm.PackageManager
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.graphics.drawable.Icon
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ClickableSpan
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.background.longpoll.models.events.OnlineEvent
import com.twoeightnine.root.xvii.background.longpoll.receivers.RestarterBroadcastReceiver
import com.twoeightnine.root.xvii.background.longpoll.services.NotificationService
import com.twoeightnine.root.xvii.chatowner.ChatOwnerActivity
import com.twoeightnine.root.xvii.chats.messages.chat.usual.ChatActivity
import com.twoeightnine.root.xvii.crypto.md5
import com.twoeightnine.root.xvii.crypto.prime.PrimeGeneratorJobIntentService
import com.twoeightnine.root.xvii.crypto.prime.PrimeGeneratorService
import com.twoeightnine.root.xvii.dialogs.models.Dialog
import com.twoeightnine.root.xvii.lg.Lg
import com.twoeightnine.root.xvii.managers.Prefs
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import okhttp3.ResponseBody
import java.io.*
import java.text.DecimalFormat
import java.util.regex.Pattern

private const val REGEX_MENTION = "(\\[id\\d{1,9}\\|[^\\]]+\\])"

fun pxFromDp(context: Context, dp: Int): Int {
    return (dp * context.resources.displayMetrics.density).toInt()
}

fun dpFromPx(context: Context, px: Int): Int {
    return (px / context.resources.displayMetrics.density).toInt()
}

fun isOnline(): Boolean {
    val cm = App.context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    return cm.activeNetworkInfo?.isConnectedOrConnecting ?: false
}

fun showToast(context: Context?, message: String, duration: Int = Toast.LENGTH_SHORT) {
    if (context == null) return

    val toast = Toast.makeText(context, message, duration)
    toast.view.findViewById<TextView>(android.R.id.message)?.apply {
        typeface = SANS_SERIF_LIGHT
    }
    toast.show()
}

fun showToast(context: Context?, @StringRes text: Int, duration: Int = Toast.LENGTH_SHORT) {
    showToast(context, context?.getString(text) ?: "", duration)
}

fun showError(context: Context?, text: String?) {
    showAlert(context, text)
}

fun rate(context: Context) {
    val uri = Uri.parse("market://details?id=" + context.packageName)
    val goToMarket = Intent(Intent.ACTION_VIEW, uri)
    goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or
            Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
            Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
    try {
        context.startActivity(goToMarket)
    } catch (e: ActivityNotFoundException) {
        context.startActivity(Intent(Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=" + context.packageName)))
    }

}

fun showError(context: Context?, @StringRes text: Int) {
    showError(context, context?.getString(text))
}

fun showAlert(context: Context?, text: String?, onOkPressed: (() -> Unit)? = null) {
    if (context == null) return

    val dialog = AlertDialog.Builder(context)
            .setMessage(text)
            .setPositiveButton(R.string.ok) { _, _ ->
                onOkPressed?.invoke()
            }
            .create()
    dialog.show()
    dialog.stylize()
}

fun getLastSeenText(
        resources: Resources?,
        isOnline: Boolean,
        timeStamp: Int,
        deviceCode: Int,
        withSeconds: Boolean = Prefs.showSeconds
): String {
    if (resources == null) return ""

    val deviceCodeName = OnlineEvent.getDeviceName(resources, deviceCode)
    val time = if (timeStamp == 0) {
        time() - (if (isOnline) 0 else 300)
    } else {
        timeStamp
    }
    val stringRes = if (isOnline) R.string.online_seen else R.string.last_seen
    val lastSeen = resources.getString(stringRes, getTime(time, withSeconds = withSeconds))
    return "$lastSeen $deviceCodeName"
}

fun showConfirm(context: Context?, text: String, callback: (Boolean) -> Unit) {
    if (context == null) return

    val dialog = AlertDialog.Builder(context)
            .setMessage(text)
            .setPositiveButton(R.string.ok) { _, _ -> callback.invoke(true) }
            .setNegativeButton(R.string.cancel) { _, _ -> callback.invoke(false) }
            .create()
    dialog.show()
    dialog.stylize()
}

fun startNotificationService(context: Context) {
    try {
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
        NotificationService.launch(context)
//        } else {
//            NotificationJobIntentService.enqueue(context)
//        }
    } catch (e: Exception) {
        e.printStackTrace()
        Lg.wtf("start service error: ${e.message}")
    }
}


fun startPrimeGenerator(context: Context) {
    try {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            PrimeGeneratorService.launch(context)
        } else {
            PrimeGeneratorJobIntentService.launch(context)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        Lg.wtf("start prime generator error: ${e.message}")
    }
}

fun startNotificationAlarm(context: Context) {
    val intent = Intent(context, RestarterBroadcastReceiver::class.java).apply {
        action = RestarterBroadcastReceiver.RESTART_ACTION
    }
    val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT)
    val alarms = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val tenMinutes = 60 * 1000L * 10
    alarms.setRepeating(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + tenMinutes,
            tenMinutes, pendingIntent
    )
    Lg.i("notification alarm started")
}

inline fun launchActivity(context: Context?,
                          activityClass: Class<out AppCompatActivity>,
                          intentBlock: Intent.() -> Unit = {}) {
    context?.startActivity(Intent(context, activityClass).apply {
        intentBlock()
    })
}

fun copyToClip(text: String) {
    val clipboard = App.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.text = text
}

fun getSize(resources: Resources, bytes: Int): String {
    val twoDecimalForm = DecimalFormat("#.##")
    return when {
        bytes < 1024 -> {
            resources.getString(R.string.bytes, bytes)
        }
        bytes < 1048576 -> {
            resources.getString(R.string.kilobytes, twoDecimalForm.format(bytes.toDouble() / 1024))
        }
        else -> {
            resources.getString(R.string.megabytes, twoDecimalForm.format(bytes.toDouble() / 1048576))
        }
    }
}

fun streamToBytes(input: InputStream): ByteArray {
    try {
        val byteBuffer = ByteArrayOutputStream()
        val buffer = ByteArray(32768)
        var len: Int
        do {
            len = input.read(buffer)
            if (len != -1) {
                byteBuffer.write(buffer, 0, len)
            }
        } while (len != -1)

        return byteBuffer.toByteArray()
    } catch (e: IOException) {
        Lg.wtf("stream to bytes error: ${e.message}")
        e.printStackTrace()
        return "".toByteArray()
    }

}

fun getBytesFromFile(context: Context, fileName: String): ByteArray {
    val file = File(fileName)
    val size = file.length().toInt()
    val bytes = ByteArray(size)
    try {
        val buf = BufferedInputStream(FileInputStream(file))
        buf.read(bytes, 0, bytes.size)
        buf.close()
    } catch (e: IOException) {
        showError(context, "${e.message}")
        e.printStackTrace()
    }

    return bytes
}

fun writeBytesToFile(context: Context, bytes: ByteArray, fileName: String): String {
    val dir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) //context.cacheDir
    val file = File(dir, fileName)
    try {
        val out = FileOutputStream(file.absolutePath)
        out.write(bytes)
        out.close()
    } catch (e: IOException) {
        e.printStackTrace()
    }
    return file.absolutePath
}

fun writeToFileFromContentUri(context: Context?, file: File, uri: Uri): Boolean {
    if (context == null) return false
    try {
        val stream = context.contentResolver.openInputStream(uri)
        val output = FileOutputStream(file)
        if (stream == null) return false

        val buffer = ByteArray(4096)
        var read: Int
        while (true) {
            read = stream.read(buffer)
            if (read == -1) break

            output.write(buffer, 0, read)
        }
        output.flush()
        output.close()
        stream.close()
        return true
    } catch (e: FileNotFoundException) {
        Lg.i("Couldn't open stream: " + e.message)
    } catch (e: IOException) {
        Lg.i("IOException on stream: " + e.message)
    }
    return false
}

fun equalsDevUids(userId: Int) = App.ID_SALTS
        .map { md5(userId.toString() + it) }
        .filterIndexed { index, hash -> hash == App.ID_HASHES[index] }
        .isNotEmpty()

fun goHome(context: Context?) {
    context?.startActivity(Intent(ACTION_MAIN).apply {
        addCategory(CATEGORY_HOME)
    })
}

fun simpleUrlIntent(context: Context?, urlArg: String?) {
    context ?: return

    var url = urlArg ?: return
    try {
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "http://$url"
        }
        val intent = Intent(ACTION_VIEW).apply {
            data = Uri.parse(url)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Lg.wtf("unable to open link: ${e.message}")
    }
}

fun wrapMentions(context: Context, text: CharSequence, addClickable: Boolean = false): SpannableStringBuilder {
    val ssb = SpannableStringBuilder()
    val pattern = Pattern.compile(REGEX_MENTION)
    val matcher = pattern.matcher(text)

    var globalStart = 0
    while (matcher.find()) {
        val mention = matcher.group()
        val start = matcher.start()
        val end = matcher.end()

        val divider = mention.indexOf('|')
        val mentionUi = mention.substring(divider + 1, mention.length - 1)
        val userId = mention.substring(3, divider).toIntOrNull()

        ssb.append(text.substring(globalStart, start))
                .append(mentionUi)
        val tmp = ssb.toString()
        if (userId != null && addClickable) {
            ssb.setSpan(object : ClickableSpan() {
                override fun onClick(widget: View) {
                    ChatOwnerActivity.launch(context, userId)
                }
            }, tmp.length - mentionUi.length, tmp.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        globalStart = end
    }
    ssb.append(text.substring(globalStart))

    return ssb
}

fun <T> applySchedulers(): (t: Flowable<T>) -> Flowable<T> {
    return { flowable: Flowable<T> ->
        flowable
                .subscribeOn(io.reactivex.schedulers.Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }
}

fun <T> applySingleSchedulers(): (t: Single<T>) -> Single<T> {
    return { single: Single<T> ->
        single
                .subscribeOn(io.reactivex.schedulers.Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }
}

fun applyCompletableSchedulers(): (t: Completable) -> Completable {
    return { completable: Completable ->
        completable
                .subscribeOn(io.reactivex.schedulers.Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }
}

fun getContextPopup(context: Context, @LayoutRes layout: Int, listener: (View) -> Unit): AlertDialog {
    val view = View.inflate(context, layout, null)

    val dialog = AlertDialog.Builder(context)
            .setView(view)
            .create()

    val click = { v: View ->
        dialog.dismiss()
        listener.invoke(v)
    }

    if (view is ViewGroup) {
        for (i in 0 until view.childCount) {
            val v = view.getChildAt(i)
            if (v is LinearLayout) {
                v.setOnClickListener(click)
            }
        }
    }
    return dialog
}

fun restartApp(context: Context?, title: String) {
    showToast(context, title)
    Handler().postDelayed({ restartApp(context) }, 400L)
}

fun restartApp(context: Context?) {
    context ?: return

    val mStartActivity = getRestartIntent(context)
    val mPendingIntentId = 123456
    val mPendingIntent = PendingIntent.getActivity(context, mPendingIntentId, mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT)
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent)

    val notificationManager = context.getSystemService(
            Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.cancelAll()
    System.exit(0)
}

fun removeNotification(context: Context) {
    try {
        (context.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager).cancelAll()
    } catch (e: Exception) {
        Lg.i("remove notif: ${e.message}")
    }
}

fun getRestartIntent(context: Context): Intent {
    val defaultIntent = Intent(ACTION_MAIN, null)
    defaultIntent.addFlags(FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_CLEAR_TASK)
    defaultIntent.addCategory(CATEGORY_LAUNCHER)

    val packageName = context.packageName
    val packageManager = context.packageManager
    for (resolveInfo in packageManager.queryIntentActivities(defaultIntent, 0)) {
        val activityInfo = resolveInfo.activityInfo
        if (activityInfo.packageName == packageName) {
            defaultIntent.component = ComponentName(packageName, activityInfo.name)
            return defaultIntent
        }
    }

    throw IllegalStateException("Unable to determine default activity for "
            + packageName
            + ". Does an activity specify the DEFAULT category in its intent filter?")
}

fun getRelation(context: Context?, relation: Int?): String {
    context ?: return ""
    val relations = context.resources.getStringArray(R.array.relations)
    if (relation == null || relation !in 1..relations.size) {
        return ""
    }
    return relations[relation - 1]
}

fun callIntent(context: Context?, num: String?) {
    context ?: return

    var number = num ?: return
    number = number.replace("-", "")
    number = number.replace(" ", "")
    context.startActivity(Intent(ACTION_DIAL, Uri.parse("tel:$number")))
}

fun addToGallery(context: Context, path: String) {
    try {
        val cv = ContentValues()
        cv.put(MediaStore.Images.Media.TITLE, context.getString(R.string.app_name))
        cv.put(MediaStore.Images.Media.DESCRIPTION, context.getString(R.string.app_name))
        cv.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        cv.put(MediaStore.Images.Media.DATA, path)
        context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv)
        context.contentResolver.notifyChange(Uri.parse("file://$path"), null)
    } catch (e: SecurityException) {
        showError(context, R.string.unable_to_add_to_gallery)
    }
}

fun getNameFromUrl(url: String): String {
    val res = url.split("/".toRegex())
            .dropLastWhile { it.isEmpty() }
            .toTypedArray()
    return res[res.size - 1]
}

//fun downloadFile(context: Context, url: String, fileName: String, type: String, listener: (() -> Unit), refresh: Boolean = false, noGallery: Boolean = false) {
//    val task = DownloadFileAsyncTask(refresh, noGallery)
//    task.listener = {
//        addToGallery(context, it)
//        listener.invoke()
//    }
//    task.execute(url, fileName, type)
//}

fun loadBitmapIcon(url: String?, useSquare: Boolean = false, callback: (Bitmap) -> Unit) {
    val uiHandler = Handler(Looper.getMainLooper())
    uiHandler.post {
        val rc = Picasso.get()
                .load(if (url.isNullOrEmpty()) {
                    ColorManager.getPhotoStub()
                } else {
                    url
                })
        if (!useSquare) {
            rc.transform(CircleTransform())
        }
        rc.resize(200, 200)
                .centerCrop()
                .into(object : Target {

                    override fun onBitmapFailed(e: java.lang.Exception?, errorDrawable: Drawable?) {
                        val drawableRes = if (useSquare) {
                            R.drawable.xvii_dark_logo_128_square
                        } else {
                            R.drawable.xvii_dark_logo_128
                        }
                        callback.invoke(BitmapFactory.decodeResource(App.context.resources, drawableRes))
                    }

                    override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                    }

                    override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                        if (bitmap != null) {
                            callback.invoke(bitmap)
                        } else {
                            loadBitmapIcon(url, useSquare, callback)
                        }
                    }
                })
    }

}

fun screenWidth(activity: Activity): Int {
    val displaymetrics = DisplayMetrics()
    activity.windowManager.defaultDisplay.getMetrics(displaymetrics)
    return displaymetrics.widthPixels
}

fun screenHeight(activity: Activity): Int {
    val displaymetrics = DisplayMetrics()
    activity.windowManager.defaultDisplay.getMetrics(displaymetrics)
    return displaymetrics.heightPixels
}

fun saveBmp(fileName: String, bmp: Bitmap) {
    var out: FileOutputStream? = null
    try {
        out = FileOutputStream(fileName)
        bmp.compress(Bitmap.CompressFormat.PNG, 100, out) // bmp is your Bitmap instance
        // PNG is a lossless format, the compression factor (100) is ignored
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        try {
            out?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }
}

fun getCroppedImagePath(activity: Activity, original: String): String? {
    try {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(original, options)
        val ih = options.outHeight
        val iw = options.outWidth
        val sh = screenHeight(activity)
        val sw = screenWidth(activity)
        val sr = sw.toFloat() / sh
        val ir = iw.toFloat() / ih

        var bmp = BitmapFactory.decodeFile(original, BitmapFactory.Options())
        var newW = iw
        var newH = ih
        if (sr < ir) {
            newW = (sr * newH).toInt()
            bmp = Bitmap.createBitmap(bmp, (iw - newW) / 2, 0, newW, newH)
        } else {
            newH = (newW / sr).toInt()
            bmp = Bitmap.createBitmap(bmp, 0, (ih - newH) / 2, newW, newH)
        }
        bmp = Bitmap.createScaledBitmap(bmp, sw, sh, true)
        val fileName = File(activity.filesDir, "chatBack${time() % 10}.png").absolutePath
        saveBmp(fileName, bmp)
        return fileName
    } catch (e: Exception) {
        Lg.wtf("[chat back] error cropping: ${e.message}")
        return null
    }
}

fun showDeleteDialog(context: Context?,
                     message: String? = null,
                     onDelete: () -> Unit = {}
) {
    context ?: return

    val dialog = AlertDialog.Builder(context)
            .setMessage(message ?: context.getString(R.string.want_delete))
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.ok) { _, _ -> onDelete.invoke() }
            .create()
    dialog.show()
    dialog.stylize()
}


fun hideKeyboard(activity: Activity) {
    val view = activity.currentFocus
    if (view != null) {
        val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}

fun showKeyboard(activity: Activity) {
    val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
}

fun getTotalRAM(): String {

    val reader: RandomAccessFile?
    val load: String?
    val twoDecimalForm = DecimalFormat("#.##")
    val totRam: Double
    var lastValue = ""
    try {
        reader = RandomAccessFile("/proc/meminfo", "r")
        load = reader.readLine()

        // Get the Number value from the string
        val p = Pattern.compile("(\\d+)")
        val m = p.matcher(load)
        var value = ""
        while (m.find()) {
            value = m.group(1)
        }
        reader.close()

        totRam = java.lang.Double.parseDouble(value)

        val mb = totRam / 1024.0
        val gb = totRam / 1048576.0
        val tb = totRam / 1073741824.0


        lastValue = when {
            tb > 1 -> twoDecimalForm.format(tb) + (" TB")
            gb > 1 -> twoDecimalForm.format(gb) + (" GB")
            mb > 1 -> twoDecimalForm.format(mb) + (" MB")
            else -> twoDecimalForm.format(totRam) + (" KB")
        }


    } catch (ex: IOException) {
        ex.printStackTrace()
    } finally {
        // Streams.close(reader);
    }

    return lastValue
}

fun shortifyNumber(value: Int): String {
    var num = value.toString()
    if (value > 1000000) {
        val mod = value % 1000000 / 100000
        num = "${value / 1000000}"
        if (mod > 0) {
            num += ".$mod"
        }
        num += "M"
    } else if (value > 10000) {
        num = "${value / 1000}K"
    } else if (value > 1000) {
        val mod = value % 1000 / 100
        num = "${value / 1000}"
        if (mod > 0) {
            num += ".$mod"
        }
        num += "K"
    }
    return num
}

fun writeResponseBodyToDisk(body: ResponseBody, fileName: String): Boolean {
    try {
        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null

        try {
            val fileReader = ByteArray(4096)
            inputStream = body.byteStream()
            outputStream = FileOutputStream(File(fileName))
            while (true) {
                val read = inputStream.read(fileReader)
                if (read == -1) break

                outputStream.write(fileReader, 0, read)
            }
            outputStream.flush()
            return true

        } catch (e: IOException) {
            Lg.wtf("write response to disk error: ${e.message}")
            e.printStackTrace()
            return false

        } finally {
            inputStream?.close()
            outputStream?.close()
        }
    } catch (e: IOException) {
        Lg.wtf("write response to disk error: ${e.message}")
        e.printStackTrace()
        return false
    }
}

fun isInForeground(): Boolean {
    val appProcessInfo = ActivityManager.RunningAppProcessInfo()
    ActivityManager.getMyMemoryState(appProcessInfo)
    return appProcessInfo.importance == IMPORTANCE_FOREGROUND || appProcessInfo.importance == IMPORTANCE_VISIBLE
}

fun createShortcut(context: Context?, dialog: Dialog) {
    val intent = Intent(context, ChatActivity::class.java).apply {
        putExtra(ChatActivity.PEER_ID, dialog.peerId)
        putExtra(ChatActivity.TITLE, dialog.alias ?: dialog.title)
        putExtra(ChatActivity.AVATAR, dialog.photo)
        flags = flags or FLAG_ACTIVITY_CLEAR_TOP
    }
    val name = dialog.alias ?: dialog.title
    Picasso.get()
            .load(dialog.photo)
            .transform(CircleTransform())
            .into(object : Target {
                override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                }

                override fun onBitmapFailed(e: java.lang.Exception?, errorDrawable: Drawable?) {
                    go(null)
                }

                override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                    go(bitmap)
                }

                private fun go(bitmap: Bitmap?) {
                    if (Build.VERSION.SDK_INT >= 25) {
                        createShortcutNew(bitmap)
                    } else {
                        createShortcut(bitmap)
                    }
                }

                private fun createShortcut(bitmap: Bitmap?) {
                    context?.sendBroadcast(Intent().apply {
                        putExtra(EXTRA_SHORTCUT_INTENT, intent)
                        putExtra(EXTRA_SHORTCUT_NAME, dialog)
                        if (bitmap != null) {
                            putExtra(EXTRA_SHORTCUT_ICON, bitmap)
                        } else {
                            putExtra(EXTRA_SHORTCUT_ICON_RESOURCE,
                                    ShortcutIconResource.fromContext(context, R.drawable.xvii_dark_logo_128))
                        }
                        putExtra("duplicate", false)
                        action = "com.android.launcher.action.INSTALL_SHORTCUT"
                    })
                }

                @TargetApi(25)
                private fun createShortcutNew(bitmap: Bitmap?) {
                    val shortcutManager = context?.getSystemService(ShortcutManager::class.java)
                    intent.action = ACTION_VIEW

                    val shortcutInfo = ShortcutInfo.Builder(context, dialog.peerId.toString())
                            .setShortLabel(name)
                            .setIcon(Icon.createWithBitmap(bitmap))
                            .setIntent(intent)
                            .build()
                    shortcutManager?.addDynamicShortcuts(arrayListOf(shortcutInfo))
                    if (Build.VERSION.SDK_INT >= 26) {
                        shortcutManager?.requestPinShortcut(shortcutInfo, null)
                    }
                }
            })
}

fun getUriForFile(context: Context?, file: File): Uri? {
    context ?: return null

    val authority = "${context.applicationContext.packageName}.provider"
    return FileProvider.getUriForFile(context, authority, file)
}

fun isMiui(): Boolean {
    val pm = App.context.packageManager
    val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)

    for (packageInfo in packages) {
        if (packageInfo.packageName.startsWith("com.miui.")) {
            return true
        }
    }
    return false
}

fun isAndroid10OrHigher() = Build.VERSION.SDK_INT >= 29
