package com.twoeightnine.root.xvii.utils

import android.util.Log
import com.twoeightnine.root.xvii.managers.Lg
import com.twoeightnine.root.xvii.model.*
import io.reactivex.Flowable
import io.realm.Realm
import io.realm.Sort

object CacheHelper {

    private val tag = "cache"

    private fun lg(text: String) {
        Log.i(tag, text)
    }

    private fun saveUsers(users: MutableList<User>) {
        val realm = Realm.getDefaultInstance()
        realm.beginTransaction()
        users.forEach {
            realm.copyToRealmOrUpdate(UserDb(it))
        }
        realm.commitTransaction()
    }

    fun saveUsersAsync(users: MutableList<User>) {
        Flowable.fromCallable({ saveUsers(users) })
                .compose(applySchedulers())
                .subscribe({ lg("save users ${users.map { it.id }.joinToString(separator = " ")}") })
    }

    fun saveUserAsync(user: User) {
        saveUsersAsync(mutableListOf(user))
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

    private fun saveGroups(groups: MutableList<Group>) {
        val realm = Realm.getDefaultInstance()
        realm.beginTransaction()
        groups.forEach {
            realm.copyToRealmOrUpdate(GroupDb(it))
        }
        realm.commitTransaction()
    }

    fun saveGroupsAsync(groups: MutableList<Group>) {
        Flowable.fromCallable({ saveGroups(groups) })
                .compose(applySchedulers())
                .subscribe({ lg("save groups ${groups.map { it.id }.joinToString(separator = " ")}") })
    }

    fun saveGroupAsync(group: Group) {
        saveGroupsAsync(mutableListOf(group))
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

    /**
     * returns existing users + ids of users to request from server
     */
    private fun getUsers(userIds: MutableList<Int>): Pair<MutableList<User>, String> {
        val realm = Realm.getDefaultInstance()
        val users = mutableListOf<User>()
        for (pos in userIds.indices.reversed()) {
            val userId = userIds[pos]
            val userDb = realm.where(UserDb::class.java)
                    .equalTo("id", userId)
                    .findFirst()
            if (userDb != null) {
                users.add(User(userDb))
                userIds.removeAt(pos)
            }
        }
        realm.where(UserDb::class.java)
                .greaterThan("id", 2000000000)
                .findAll()
                .forEach {
                    users.add(User(it))
                }
        return Pair(users, userIds.joinToString(separator = ","))
    }

    fun getUsersAsync(userIds: MutableList<Int>, callback: (Pair<MutableList<User>, String>) -> Unit) {
        Flowable.fromCallable { getUsers(userIds) }
                .compose(applySchedulers())
                .subscribe {
                    lg("get users ${userIds.joinToString(separator = " ")}")
                    callback.invoke(it)
                }
    }

    private fun getAllUsers(): HashMap<Int, User> {
        val realm = Realm.getDefaultInstance()
        val result = hashMapOf<Int, User>()
        realm.where(UserDb::class.java)
                .findAll()
                .forEach {
                    result.put(it.id, User(it))
                }
        return result
    }

    fun getAllUsersAsync(callback: (HashMap<Int, User>) -> Unit) {
        Flowable.fromCallable({ getAllUsers() })
                .compose(applySchedulers())
                .subscribe({
                    lg("get all users")
                    callback.invoke(it)
                })
    }

    /**
     * returns existing groups + ids of groups to request from server
     */
    private fun getGroups(groupIds: MutableList<Int>): Pair<MutableList<Group>, String> {
        val realm = Realm.getDefaultInstance()
        val groups = mutableListOf<Group>()
        for (pos in groupIds.indices.reversed()) {
            val groupId = groupIds[pos]
            val groupDb = realm.where(GroupDb::class.java)
                    .equalTo("id", groupId)
                    .findFirst()
            if (groupDb != null) {
                groups.add(Group(groupDb))
                groupIds.removeAt(pos)
            }
        }
        return Pair(groups, groupIds.joinToString(separator = ","))
    }

    fun getGroupsAsync(groupIds: MutableList<Int>, callback: (Pair<MutableList<Group>, String>) -> Unit) {
        Flowable.fromCallable({ getGroups(groupIds) })
                .compose(applySchedulers())
                .subscribe({
                    lg("get groups ${groupIds.joinToString(separator = " ")}")
                    callback.invoke(it)
                })
    }

    private fun getDialogs(): MutableList<Message> {
        val realm = Realm.getDefaultInstance()
        return realm.where(MessageDb::class.java)
                .findAllSorted("id", Sort.DESCENDING)
                .where()
                .distinct("peerId")
                .filterIndexed { index, _ -> index < 20 }
                .map { Message(it) }
                .toMutableList()
    }

    fun getDialogsAsync(callback: (MutableList<Message>) -> Unit) {
        Flowable.fromCallable({ getDialogs() })
                .compose(applySchedulers())
                .subscribe({
                    lg("get dialogs")
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

    private fun deleteAllGroups() {
        val realm = Realm.getDefaultInstance()
        realm.beginTransaction()
        realm.where(GroupDb::class.java)
                .findAll()
                .deleteAllFromRealm()
        realm.commitTransaction()
    }

    fun deleteAllGroupsAsync() {
        Flowable.fromCallable({ deleteAllGroups() })
                .compose(applySchedulers())
                .subscribe({ lg("delete groups") })
    }

    private fun deleteAllUsers() {
        val realm = Realm.getDefaultInstance()
        realm.beginTransaction()
        realm.where(UserDb::class.java)
                .findAll()
                .deleteAllFromRealm()
        realm.commitTransaction()
    }

    fun deleteAllUsersAsync() {
        Flowable.fromCallable({ deleteAllUsers() })
                .compose(applySchedulers())
                .subscribe({ lg("delete users") })
    }

    private fun deleteDialog(chatId: Int) {
        val realm = Realm.getDefaultInstance()
        realm.beginTransaction()
        if (chatId > 2000000000) {
            realm.where(MessageDb::class.java)
                    .equalTo("chatId", chatId - 2000000000)
                    .findAll()
                    .deleteAllFromRealm()
        } else {
            realm.where(MessageDb::class.java)
                    .equalTo("userId", chatId)
                    .equalTo("chatId", 0)
                    .findAll()
                    .deleteAllFromRealm()
        }
        realm.commitTransaction()
    }

    fun deleteDialogAsync(chatId: Int) {
        Flowable.fromCallable({ deleteDialog(chatId) })
                .compose(applySchedulers())
                .subscribe()
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

    fun logUsers() {
        val realm = Realm.getDefaultInstance()
        realm.where(UserDb::class.java)
                .findAll()
                .forEach {
                    Lg.i(it.toString())
                }
    }

    fun logMessages() {
        val realm = Realm.getDefaultInstance()
        realm.where(MessageDb::class.java)
                .findAll()
                .forEach {
                    Lg.i(it.toString())
                }
    }

    fun logGroups() {
        val realm = Realm.getDefaultInstance()
        realm.where(GroupDb::class.java)
                .findAll()
                .forEach {
                    Lg.i(it.toString())
                }
    }

}


