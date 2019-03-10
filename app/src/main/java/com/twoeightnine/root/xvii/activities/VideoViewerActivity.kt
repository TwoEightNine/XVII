package com.twoeightnine.root.xvii.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.webkit.WebView
import android.webkit.WebViewClient
import com.twoeightnine.root.xvii.R
import kotlinx.android.synthetic.main.activity_video_viewer.*

class VideoViewerActivity: AppCompatActivity() {

    companion object {

        fun launch(context: Context, url: String) {
            val intent = Intent(context, VideoViewerActivity::class.java)
            intent.putExtra(URL, url)
            context.startActivity(intent)
        }

        val URL = "url"
    }

    private lateinit var url: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_viewer)
        if (intent.extras != null) {
            url = intent.extras.getString(URL)
        } else {
            finish()
        }
        webView.settings.javaScriptEnabled = true
        webView.setWebViewClient(object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                // do your handling codes here, which url is the requested url
                // probably you need to open that url rather than redirect:
                view.loadUrl(url)
                return false // then it is not handled by default action
            }
        })
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