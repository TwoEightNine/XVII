package com.twoeightnine.root.xvii.mvp

import com.twoeightnine.root.xvii.network.ApiService
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.Serializable

open class BasePresenter<V: BaseView>(open var api: ApiService): Serializable {

    open var view: V? = null

    fun <T> applySchedulers(): (t: Flowable<T>) -> Flowable<T> {
        return { flowable: Flowable<T> -> flowable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())}
    }

}