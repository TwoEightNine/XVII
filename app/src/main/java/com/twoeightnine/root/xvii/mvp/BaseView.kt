package com.twoeightnine.root.xvii.mvp

interface BaseView {
    fun showLoading()
    fun hideLoading()
    fun showError(error: String)
}