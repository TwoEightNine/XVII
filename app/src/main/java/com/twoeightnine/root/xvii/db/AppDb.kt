package com.twoeightnine.root.xvii.db

import android.annotation.SuppressLint
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.twoeightnine.root.xvii.accounts.db.AccountsDao
import com.twoeightnine.root.xvii.accounts.models.Account
import com.twoeightnine.root.xvii.dialogs.db.DialogsDao
import com.twoeightnine.root.xvii.dialogs.models.Dialog
import com.twoeightnine.root.xvii.lg.Lg
import com.twoeightnine.root.xvii.utils.applyCompletableSchedulers
import io.reactivex.Completable

@Database(entities = [Dialog::class, Account::class], version = 5)
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

        private val MIGRATION_4_5 = object : Migration(4, 5) {

            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE dialogs ADD COLUMN isPinned INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE dialogs ADD COLUMN alias TEXT")
            }
        }

        fun buildDatabase(context: Context) =
                Room.databaseBuilder(context.applicationContext,
                        AppDb::class.java, "xvii_room.db")
                        .addMigrations(MIGRATION_4_5)
                        .build()
    }
}