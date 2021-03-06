package com.twoeightnine.root.xvii.utils

import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsService
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.lg.L
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream


object BrowsingUtils {

    private const val STABLE_PACKAGE = "com.android.chrome"
    private const val BETA_PACKAGE = "com.chrome.beta"
    private const val DEV_PACKAGE = "com.chrome.dev"
    private const val LOCAL_PACKAGE = "com.google.android.apps.chrome"

    private const val TAG = "browsing"

    private var mPackageNameToUse: String? = null

    fun openFile(context: Context?, path: String) {
        context ?: return

        AsyncUtils.onIoThread({
            val file = File(path)
            val bytes = FileInputStream(file).use { it.readBytes() }
            FileOutputStream(file).use { it.write(bytes) }
            FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        }) { uri ->
            openUri(context, uri)
        }
    }

    fun openUri(context: Context?, uri: Uri?) {
        context ?: return
        uri ?: return
        if (uri == Uri.EMPTY) return

        try {
            openCustomTabs(context, uri, withGrantUri = true)
        } catch (e: Exception) {
            L.def().throwable(e)
            openUriIntent(context, uri)
        }
    }

    fun openUrl(context: Context?, url: String?) {
        context ?: return
        url ?: return

        val fixedUrl = getFixedUrl(url)
        val uri = Uri.parse(fixedUrl)

        val browserPackages = getBrowserAppPackages(context)
        val nativePackageName = getNativeAppPackage(context, uri, browserPackages)
        if (nativePackageName != null) {
            openUriIntent(context, uri, nativePackageName)
        } else {
            val packageName = getCustomTabsPackage(context, browserPackages)
            if (packageName != null) {
                openCustomTabs(context, uri, packageName)
            } else {
                openUriIntent(context, uri)
            }
        }
    }

    private fun openCustomTabs(context: Context, uri: Uri, packageName: String? = null, withGrantUri: Boolean = false) {
        CustomTabsIntent.Builder()
                .setToolbarColor(ContextCompat.getColor(context, R.color.background))
                .build()
                .apply {
                    packageName?.also(intent::setPackage)
                    if (withGrantUri) {
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                }
                .launchUrl(context, uri)
    }

    private fun openUriIntent(context: Context, uri: Uri, packageName: String? = null) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = uri
                packageName?.also { `package` = it }
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            L.def().throwable(e)
                    .log("unable to open link")
        }
    }

    private fun getCustomTabsPackage(context: Context, browserPackages: Set<String>? = null): String? {
        if (mPackageNameToUse != null) {
            return mPackageNameToUse
        }

        val pm = context.packageManager
        val packagesSupportingCustomTabs = arrayListOf<String>()

        (browserPackages ?: getBrowserAppPackages(context))
                .forEach { packageName ->
                    val serviceIntent = Intent().apply {
                        action = CustomTabsService.ACTION_CUSTOM_TABS_CONNECTION
                        `package` = packageName
                    }
                    pm.resolveService(serviceIntent, 0)?.also {
                        packagesSupportingCustomTabs.add(packageName)
                    }
                }

        // Now packagesSupportingCustomTabs contains all apps that can handle both VIEW intents
        // and service calls.
        mPackageNameToUse = when {
            packagesSupportingCustomTabs.size == 1 -> packagesSupportingCustomTabs[0]
            STABLE_PACKAGE in packagesSupportingCustomTabs -> STABLE_PACKAGE
            BETA_PACKAGE in packagesSupportingCustomTabs -> BETA_PACKAGE
            DEV_PACKAGE in packagesSupportingCustomTabs -> DEV_PACKAGE
            LOCAL_PACKAGE in packagesSupportingCustomTabs -> LOCAL_PACKAGE
            else -> null
        }
        return mPackageNameToUse
    }

    private fun getNativeAppPackage(context: Context, uri: Uri, browserPackages: Set<String>? = null): String? {

        // Get default VIEW intent handler that can view a web url.
        val activityIntent = Intent(Intent.ACTION_VIEW, uri)

        // Get all apps that can handle VIEW intents.
        val pm = context.packageManager
        val resolvedPackages = pm.queryIntentActivities(activityIntent, 0)
                .toPackageNamesSet()
                .toMutableSet()
        resolvedPackages.removeAll(browserPackages ?: getBrowserAppPackages(context))
        l("found for ${uri.host}: $resolvedPackages")
        return resolvedPackages.firstOrNull()
    }

    private fun getBrowserAppPackages(context: Context): Set<String> {
        // Get default VIEW intent handler that can view a web url.
        val activityIntent = Intent(Intent.ACTION_VIEW, Uri.parse("http://www.test-url.com"))

        // Get all apps that can handle VIEW intents.
        val pm = context.packageManager
        val resolvedPackages = pm.queryIntentActivities(activityIntent, 0)
                .toPackageNamesSet()
        l("found as browsers: $resolvedPackages")
        return resolvedPackages
    }

    private fun getFixedUrl(url: String): String =
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                "https://$url"
            } else {
                url
            }

    private fun List<ResolveInfo>.toPackageNamesSet(): Set<String> =
            map { it.activityInfo.packageName }.toSet()

    private fun l(s: String) {
        L.tag(TAG).log(s)
    }

}