package com.twoeightnine.root.xvii.utils

import android.util.Log
import com.twoeightnine.root.xvii.model.Message
import com.twoeightnine.root.xvii.model.MessageDb
import io.reactivex.Flowable
import io.realm.Realm
import io.realm.Sort

object CacheHelper {

    private val tag = "cache"

    private fun lg(text: String) {
        Log.i(tag, text)
    }

    private fun saveMessages(messages: MutableList<Message>) {
        val realm = Realm.getDefaultInstance()
        realm.beginTransaction()
        messages.forEach {
            realm.copyToRealmOrUpdate(MessageDb(it))
        }
        realm.commitTransaction()
    }

    fun saveMessagesAsync(messages: MutableList<Message>) {
        Flowable.fromCallable({ saveMessages(messages) })
                .compose(applySchedulers())
                .subscribe({ lg("save messages ${messages.map { it.id }.joinToString(separator = " ")}") })
    }

    fun saveMessageAsync(message: Message) {
        saveMessagesAsync(mutableListOf(message))
    }

    private fun getMessages(chatId: Int): MutableList<Message> {
        val realm = Realm.getDefaultInstance()
        val res: MutableList<Message>
        if (chatId > 2000000000) {
            res = realm.where(MessageDb::class.java)
                    .equalTo("chatId", chatId - 2000000000)
                    .findAllSorted("id", Sort.DESCENDING)
                    .filterIndexed { index, _ -> index < 20 }
                    .map { Message(it) }
                    .toMutableList()
        } else {
            res = realm.where(MessageDb::class.java)
                    .equalTo("userId", chatId)
                    .equalTo("chatId", 0)
                    .findAllSorted("id", Sort.DESCENDING)
                    .filterIndexed { index, _ -> index < 20 }
                    .map { Message(it) }
                    .toMutableList()
        }
        lg("messages $res")
        return res
    }

    fun getMessagesAsync(chatId: Int, callback: (MutableList<Message>) -> Unit) {
        Flowable.fromCallable({ getMessages(chatId) })
                .compose(applySchedulers())
                .subscribe({
                    lg("get messages $chatId")
                    callback.invoke(it)
                })
    }

    private fun deleteAllMessages() {
        val realm = Realm.getDefaultInstance()
        realm.beginTransaction()
        realm.where(MessageDb::class.java)
                .findAll()
                .deleteAllFromRealm()
        realm.commitTransaction()
    }

    fun deleteAllMessagesAsync() {
        Flowable.fromCallable({ deleteAllMessages() })
                .compose(applySchedulers())
                .subscribe({ lg("delete messages") })
    }

    private fun deleteMessages(mids: MutableList<Int>) {
        val realm = Realm.getDefaultInstance()
        realm.beginTransaction()
        mids.forEach {
            realm.where(MessageDb::class.java)
                    .equalTo("id", it)
                    .findFirst()
                    ?.deleteFromRealm()
        }
        realm.commitTransaction()
    }

    fun deleteMessagesAsync(mids: MutableList<Int>) {
        Flowable.fromCallable({ deleteMessages(mids) })
                .compose(applySchedulers())
                .subscribe()
    }

}


