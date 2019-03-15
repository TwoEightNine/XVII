package com.twoeightnine.root.xvii.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.twoeightnine.root.xvii.R
import kotlinx.android.synthetic.main.activity_video_viewer.*

class VideoViewerActivity : AppCompatActivity() {

    companion object {

        fun launch(context: Context?, url: String) {
            val intent = Intent(context, VideoViewerActivity::class.java).apply {
                putExtra(URL, url)
            }
            context?.startActivity(intent)
        }

        const val URL = "url"
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_viewer)
        val url = intent?.extras?.getString(URL) ?: return

        webView.settings.javaScriptEnabled = true
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                // do your handling codes here, which url is the requested url
                // probably you need to open that url rather than redirect:
                view.loadUrl(url)
                return false // then it is not handled by default action
            }
        }
        webView.loadUrl(url)
    }

    override fun onPause() {
        super.onPause()
        webView.onPause()
    }

    override fun onResume() {
        super.onResume()
        webView.onResume()
    }
}