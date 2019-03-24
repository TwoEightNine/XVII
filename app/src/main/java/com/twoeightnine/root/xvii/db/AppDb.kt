package com.twoeightnine.root.xvii.db

import android.annotation.SuppressLint
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.twoeightnine.root.xvii.accounts.db.AccountsDao
import com.twoeightnine.root.xvii.accounts.models.Account
import com.twoeightnine.root.xvii.dialogs2.db.DialogsDao
import com.twoeightnine.root.xvii.dialogs2.models.Dialog
import com.twoeightnine.root.xvii.lg.Lg
import com.twoeightnine.root.xvii.utils.applyCompletableSchedulers
import io.reactivex.Completable

@Database(entities = [Dialog::class, Account::class], version = 4)
abstract class AppDb : RoomDatabase() {

    abstract fun dialogsDao(): DialogsDao

    abstract fun accountsDao(): AccountsDao

    @SuppressLint("CheckResult")
    fun clearAsync() {
        Completable.fromCallable {
            clearAllTables()
        }
                .compose(applyCompletableSchedulers())
                .subscribe({}) {
                    it.printStackTrace()
                    Lg.wtf("[app db] error clearing: ${it.message}")
                }
    }

    companion object {

        fun buildDatabase(context: Context) =
                Room.databaseBuilder(context.applicationContext,
                        AppDb::class.java, "xvii_room.db")
                        .fallbackToDestructiveMigration()
                        .build()
    }
}