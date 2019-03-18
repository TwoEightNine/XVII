package com.twoeightnine.root.xvii.fragments

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import com.twoeightnine.root.xvii.R
import kotlinx.android.synthetic.main.fragment_web.*

class WebFragment : BaseOldFragment() {

    companion object {

        fun newInstance(url: String, title: String = ""): WebFragment {
            val frag = WebFragment()
            frag.url = url
            frag.title = if (title.isNotEmpty()) title else url
            return frag
        }

    }

    private var url: String? = null
    private var title: String? = null

    override fun bindViews(view: View) {
        super.bindViews(view)
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

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        try {
            super.onActivityCreated(savedInstanceState)
            updateTitle(title ?: url ?: "")
        } catch (e: Exception) {
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        menu?.clear()
    }

    override fun getLayout() = R.layout.fragment_web

}