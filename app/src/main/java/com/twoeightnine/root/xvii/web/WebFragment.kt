package com.twoeightnine.root.xvii.web

import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.base.BaseFragment
import com.twoeightnine.root.xvii.utils.setBottomInsetPadding
import kotlinx.android.synthetic.main.fragment_web.*

class WebFragment : BaseFragment() {

    private val url by lazy { arguments?.getString(ARG_URL) }
    private val title by lazy { arguments?.getString(ARG_TITLE) }

    override fun getLayoutId() = R.layout.fragment_web

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                // do your handling codes here, which url is the requested url
                // probably you need to open that url rather than redirect:
                view.loadUrl(url)
                return false // then it is not handled by default action
            }
        }
        url?.also(webView::loadUrl)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        xviiToolbar.title = title ?: url ?: getString(R.string.app_name)
        webView.setBottomInsetPadding()
    }

    companion object {
        const val ARG_URL = "url"
        const val ARG_TITLE = "title"

        fun newInstance(url: String, title: String = ""): WebFragment {
            val frag = WebFragment()
            frag.arguments = createArgs(url, title)
            return frag
        }

        fun createArgs(url: String, title: String = "") = Bundle().apply {
            putString(ARG_URL, url)
            putString(ARG_TITLE, if (title.isNotEmpty()) title else url)
        }
    }

}