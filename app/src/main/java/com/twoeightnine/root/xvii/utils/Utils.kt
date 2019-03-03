package com.twoeightnine.root.xvii.utils

import android.app.Activity
import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.*
import android.content.Intent.*
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.SystemClock
import android.provider.MediaStore
import android.support.annotation.LayoutRes
import android.support.annotation.StringRes
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.text.Html
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.squareup.picasso.Picasso
import com.squareup.picasso.RequestCreator
import com.squareup.picasso.Target
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.background.LongPollService
import com.twoeightnine.root.xvii.background.NotificationJobIntentService
import com.twoeightnine.root.xvii.background.NotificationService
import com.twoeightnine.root.xvii.consts.Api
import com.twoeightnine.root.xvii.managers.Lg
import com.twoeightnine.root.xvii.managers.Style
import com.twoeightnine.root.xvii.model.LongPollEvent
import com.twoeightnine.root.xvii.model.Message
import com.twoeightnine.root.xvii.model.User
import com.twoeightnine.root.xvii.model.response.Error
import com.twoeightnine.root.xvii.response.ServerResponse
import com.twoeightnine.root.xvii.utils.crypto.CryptoUtil
import de.hdodenhof.circleimageview.CircleImageView
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import okhttp3.ResponseBody
import java.io.*
import java.security.MessageDigest
import java.text.DecimalFormat
import java.util.regex.Pattern

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

fun showCommon(context: Context?, text: String) {
    if (context == null) return

    val toast = Toast.makeText(context, text, Toast.LENGTH_SHORT)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
        val view = toast.view
        val tvToast = view.findViewById<TextView>(android.R.id.message)
        tvToast.textSize = 17f
        tvToast.setPadding(12, 0, 12, 0)
        tvToast.text = text
        tvToast.setTextColor(ContextCompat.getColor(context, R.color.toolbar_text))
        tvToast.gravity = Gravity.CENTER_HORIZONTAL
        val d = ContextCompat.getDrawable(context, R.drawable.shape_toast)
        Style.forDrawable(d, Style.MAIN_TAG, false)
        view.background = d
    }
    toast.show()
}

fun showCommon(context: Context?, @StringRes text: Int) {
    showCommon(context, context?.getString(text) ?: "")
}

fun showError(context: Context?, text: String) {
    if (context == null) return

    val toast = Toast.makeText(context, text, Toast.LENGTH_SHORT)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
        val view = toast.view
        val tvToast = view.findViewById<TextView>(android.R.id.message)
        tvToast.textSize = 18f
        tvToast.setPadding(12, 0, 12, 0)
        tvToast.text = text
        tvToast.setTextColor(ContextCompat.getColor(context, android.R.color.white))
        tvToast.gravity = Gravity.CENTER_HORIZONTAL
        view.background = ContextCompat.getDrawable(context, R.drawable.shape_toast_red)
    }
    toast.show()
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
    showError(context, context?.getString(text) ?: "")
}

fun startNotificationService(context: Context) {
    try {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            context.startService(Intent(context, NotificationService::class.java))
        } else {
            NotificationJobIntentService.launch(context)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        Lg.wtf("start service error: ${e.message}")
    }
}

fun copyToClip(text: String) {
    val clipboard = App.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.text = text
}

fun getSize(context: Context, bytes: Int) =
    when {
        bytes < 1024 -> context.getString(R.string.bytes, bytes)
        bytes < 1048576 -> context.getString(R.string.kilobytes, bytes / 1024)
        else -> context.getString(R.string.megabytes, bytes / 1048576)
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

fun md5Raw(plain: ByteArray) = MessageDigest
        .getInstance("MD5")
        .digest(plain)

fun md5(plain: String) = md5Raw(plain.toByteArray())
        .map { Integer.toHexString(it.toInt() and 0xff) }
        .map { if (it.length == 2) it else "0$it" }
        .joinToString(separator = "")

fun sha256Raw(plain: ByteArray) = MessageDigest
        .getInstance("SHA-256")
        .digest(plain)

fun sha256(plain: String) = sha256Raw(plain.toByteArray())
        .map { Integer.toHexString(it.toInt() and 0xff) }
        .map { if (it.length == 2) it else "0$it" }
        .joinToString(separator = "")

fun bytesToHex(bytes: ByteArray) = bytes
        .map { Integer.toHexString(it.toInt() and 0xff) }
        .map { if (it.length == 2) it else "0$it" }
        .joinToString(separator = "")

fun getUiFriendlyHash(hash: String) = hash
        .mapIndexed { index, c -> if (index % 2 == 0) c.toString() else "$c " } // spaces
        .mapIndexed { index, s -> if (index % 16 == 15) "$s\n" else s } // new-lines
        .map { it.toUpperCase() }
        .joinToString(separator = "")

fun equalsDevUids(userId: Int) = Api.ID_SALTS
        .map { md5(userId.toString() + it) }
        .filterIndexed { index, hash -> hash == Api.ID_HASHES[index] }
        .isNotEmpty()

fun simpleUrlIntent(context: Context, url: String?) {
    var url = url ?: return
    if (!url.startsWith("http://") && !url.startsWith("https://")) {
        url = "http://$url"
    }
    val intent = Intent(Intent.ACTION_VIEW)
    intent.data = Uri.parse(url)
    context.startActivity(intent)
}

fun <T> applySchedulers(): (t: Flowable<T>) -> Flowable<T> {
    return { flowable: Flowable<T> -> flowable
                .subscribeOn(io.reactivex.schedulers.Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }
}

fun String.matchesXviiKey() = length > CryptoUtil.PREFIX.length + CryptoUtil.POSTFIX.length &&
        this.substring(0, CryptoUtil.PREFIX.length) == CryptoUtil.PREFIX &&
        this.substring(length - CryptoUtil.POSTFIX.length) == CryptoUtil.POSTFIX


fun <T> Flowable<ServerResponse<T>>.subscribeSmart(response: (T) -> Unit,
                                                     error: (String) -> Unit,
                                                     newtError: (String) -> Unit = error): Disposable {
    return this.compose(applySchedulers())
            .subscribe({
                resp ->
                if (resp.response != null) {
                    response.invoke(resp.response)
                } else if (resp.error != null) {
                    val errorMsg = resp.error.friendlyMessage()
                    val errCode = resp.error.code
                    when (errCode) {
                        Error.TOO_MANY -> {
                            Thread {
                                SystemClock.sleep(330)
                                this.subscribeSmart(response, error)
                            }.start()
                        }
                        else -> error.invoke(errorMsg ?: "null")
                    }

                }
            }, {
                err ->
                newtError.invoke(err.message ?: "null")
            })
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
        val vg = view
        for (i in 0 until vg.childCount) {
            val v = vg.getChildAt(i)
            if (v is LinearLayout) {
                v.setOnClickListener(click)
            }
        }
    }
    return dialog
}

fun restartApp(title: String) {
    showCommon(App.context, title)
    Handler().postDelayed({ restartApp() }, 800L)
}

fun restartApp() {
    val context = App.context
    val mStartActivity = getRestartIntent(context)
    val mPendingIntentId = 123456
    val mPendingIntent = PendingIntent.getActivity(context, mPendingIntentId, mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT)
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + 500, mPendingIntent)

    val notificationManager = context.getSystemService(
            Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.cancelAll()
    System.exit(0)
}

fun removeNotification(context: Context) {
    try {
        (context.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager).cancel(LongPollService.NOTIFICATION)
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

fun getPollFrom(userId: Int, chatId: Int) =
    when {
        chatId != 0 -> 2000000000 + chatId
        userId < 0 -> 1000000000 - userId //group is negative - (-..) == +
        else -> userId
    }

fun getRelation(context: Context, relation: Int): String {
    if (relation == 0)
        return ""
    val relats = context.resources.getStringArray(R.array.relations)
    return relats[relation - 1]
}

fun callIntent(context: Context, number: String) {
    var number = number
    number = number.replace("-", "")
    number = number.replace(" ", "")
    context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + number)))
}

fun addToGallery(context: Context, path: String) {
    try {
        val cv = ContentValues()
        cv.put(MediaStore.Images.Media.TITLE, context.getString(R.string.app_name))
        cv.put(MediaStore.Images.Media.DESCRIPTION, context.getString(R.string.app_name))
        cv.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        cv.put(MediaStore.Images.Media.DATA, path)
        context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv)
        context.contentResolver.notifyChange(Uri.parse("file://" + path), null)
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

fun downloadFile(context: Context, url: String, fileName: String, type: String, listener: (() -> Unit), refresh: Boolean = false, noGallery: Boolean = false) {
    val task = DownloadFileAsyncTask(refresh, noGallery)
    task.listener = {
        addToGallery(context, it)
        listener.invoke()
    }
    task.execute(url, fileName, type)
}

fun downloadSticker(context: Context, url: String, fileName: String, type: String, listener: (() -> Unit)) {
    val task = DownloadFileAsyncTask(false, true)
    task.listener = {
        addToGallery(context, it)
        listener.invoke()
    }
    task.execute(url, fileName, type)
}

fun getPeerId(userId: Int, chatId: Int): Int = if (chatId == 0) userId else 2000000000 + chatId

fun getFromPeerId(peerId: Int) = intArrayOf(if (peerId > 2000000000) 0 else peerId, if (peerId > 2000000000) peerId - 2000000000 else 0)

fun ImageView.loadUrl(url: String?) {
    if (url == null) return
    Picasso.with(App.context)
            .loadUrl(url)
            .into(this)
}

fun CircleImageView.loadPhoto(url: String?) {
    if (url == null) return
    Picasso.with(App.context)
            .load(url)
            .placeholder(R.drawable.placeholder)
            .error(R.drawable.placeholder)
            .into(this)

}

fun Picasso.loadUrl(url: String?): RequestCreator {
    return this
            .load(url)
            .transform(RoundedTransformation())
            .placeholder(R.drawable.placeholder)
            .error(R.drawable.placeholder)

}

fun loadBitmapIcon(url: String?, callback: (Bitmap) -> Unit) {
    Picasso.with(App.context)
            .loadUrl(url)
            .resize(200, 200)
            .centerCrop()
            .into(object : Target {
                override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                    callback.invoke(BitmapFactory.decodeResource(App.context.resources, R.drawable.xvii64))
                }

                override fun onBitmapFailed(errorDrawable: Drawable?) {
                    callback.invoke(BitmapFactory.decodeResource(App.context.resources, R.drawable.xvii64))
                }

                override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                    if (bitmap != null) {
                        callback.invoke(bitmap)
                    } else {
                        loadBitmapIcon(url, callback)
                    }
                }
            })

}

fun getImageRatio(path: String): Float {
    val options = BitmapFactory.Options()
    options.inJustDecodeBounds = true
    BitmapFactory.decodeFile(path, options)
    val imageHeight = options.outHeight.toFloat()
    val imageWidth = options.outWidth.toFloat()
    return imageWidth / imageHeight
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
            if (out != null) {
                out.close()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }
}

fun getCroppedImagePath(activity: Activity, original: String): String {
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
}

fun showDeleteDialog(context: Context,
                     onDelete: () -> Unit = {},
                     message: String = context.getString(R.string.want_delete)) {
    val dialog = android.support.v7.app.AlertDialog.Builder(context)
            .setMessage(message)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(android.R.string.ok, { _, _ -> onDelete.invoke()})
            .create()
    dialog.show()
    Style.forDialog(dialog)
}

fun hideKeyboard(activity: Activity) {
    val view = activity.currentFocus
    if (view != null) {
        val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
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
            else ->  twoDecimalForm.format(totRam) + (" KB")
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

fun getMessageFromLongPoll(event: LongPollEvent,
                           isShown: Boolean = false): Message {
    val message = Message(
            event.mid,
            event.ts,
            event.userId,
            event.out,
            if (isShown) 1 - event.out else 0,
            event.title,
            event.message,
            null,
            if (event.info?.hasEmojis ?: false) 1 else 0
    )
    if (event.info!!.from != 0) {
        message.userId = event.info!!.from
    }
    if (event.userId > 2000000000) {
        message.chatId = event.userId - 2000000000
    } else if (event.userId > 1000000000) {
        message.userId = 1000000000 - event.userId
    }
    message.body = Html.fromHtml(message.body).toString()
    return message
}


fun getMessageFromLongPollFull(event: LongPollEvent,
                               users: HashMap<Int, User>,
                               isShown: Boolean = false): Message {
    var message = getMessageFromLongPoll(event, isShown)
    message = setMessageTitles(users, message, 0)
    return message
}

fun setMessageTitles(users: HashMap<Int, User>, message: Message, level: Int): Message {
    val user = users[message.userId]
    if (user != null) {
        message.title = user.fullName()
        message.photo = user.photo100
    }
    if (message.fwdMessages != null) {
        val fwd = message.fwdMessages ?: arrayListOf()
        for (i in fwd.indices) {
            fwd[i] = setMessageTitles(users, fwd[i], level + 1)
        }
        message.fwdMessages = fwd
    }
    return message
}

fun writeResponseBodyToDisk(body: ResponseBody, fileName: String): Boolean {
    try {
        val futureStudioIconFile = File(fileName)
        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null

        try {
            val fileReader = ByteArray(4096)
            val fileSize = body.contentLength()
            var fileSizeDownloaded: Long = 0
            inputStream = body.byteStream()
            outputStream = FileOutputStream(futureStudioIconFile)

            while (true) {
                val read = inputStream!!.read(fileReader)
                if (read == -1) {
                    break
                }
                outputStream.write(fileReader, 0, read)
                fileSizeDownloaded += read.toLong()
            }
            outputStream.flush()
            return true

        } catch (e: IOException) {
            e.printStackTrace()
            return false

        } finally {
            if (inputStream != null) {
                inputStream.close()
            }
            if (outputStream != null) {
                outputStream.close()
            }
        }
    } catch (e: IOException) {
        e.printStackTrace()
        return false
    }
}




