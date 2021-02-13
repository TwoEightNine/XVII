package global.msnthrp.xvii.data.db

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import global.msnthrp.xvii.data.accounts.Account
import global.msnthrp.xvii.data.accounts.AccountsDao
import global.msnthrp.xvii.data.dialogs.Dialog
import global.msnthrp.xvii.data.dialogs.DialogsDao
import global.msnthrp.xvii.data.journal.JournalDao
import global.msnthrp.xvii.data.journal.JournalEntity
import global.msnthrp.xvii.data.scheduled.ScheduledMessage
import global.msnthrp.xvii.data.scheduled.ScheduledMessageDao
import global.msnthrp.xvii.data.stickersemoji.db.EmojisDao
import global.msnthrp.xvii.data.stickersemoji.db.StickersDao
import global.msnthrp.xvii.data.stickersemoji.model.Emoji
import global.msnthrp.xvii.data.stickersemoji.model.EmojiUsage
import global.msnthrp.xvii.data.stickersemoji.model.Sticker
import global.msnthrp.xvii.data.stickersemoji.model.StickerUsage
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets


@Database(entities = [
    Dialog::class, Account::class,
    Sticker::class, Emoji::class,
    StickerUsage::class, EmojiUsage::class,
    ScheduledMessage::class, JournalEntity::class], version = Migrations.DB_VERSION)
abstract class AppDb : RoomDatabase() {

    abstract fun dialogsDao(): DialogsDao

    abstract fun accountsDao(): AccountsDao

    abstract fun stickersDao(): StickersDao

    abstract fun emojisDao(): EmojisDao

    abstract fun scheduledMessagesDao(): ScheduledMessageDao

    abstract fun journalDao(): JournalDao

    @SuppressLint("CheckResult")
    fun clearAsync() {
        Completable.fromCallable {
            clearAllTables()
        }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({}) {
//                    L.tag(TAG)
//                            .throwable(it)
//                            .log("error clearing all tables")
                }
    }

    companion object {

        private const val TAG = "app db"



        fun buildDatabase(context: Context) =
                Room.databaseBuilder(context.applicationContext,
                        AppDb::class.java, "xvii_room.db")
                        .addMigrations(*Migrations.getMigrations())
//                        .fallbackToDestructiveMigration()
                        .addCallback(object : Callback() {
                            override fun onOpen(db: SupportSQLiteDatabase) {
                                super.onCreate(db)
                                fillWithEmojisIfEmpty(context, db)
                            }
                        })
                        .build()

        private fun fillWithEmojisIfEmpty(context: Context, db: SupportSQLiteDatabase) {
            var cursor: Cursor? = null
            var count = 0
            try {
                cursor = db.query("SELECT * FROM emojis")
                count = cursor.count
            } catch (e: Exception) {
//                L.tag(TAG)
//                        .throwable(e)
//                        .log("error getting emojis count")
            } finally {
                cursor?.close()
            }

            if (count == 0) {
                var br: BufferedReader? = null
                try {
                    val inputStream: InputStream = context.assets.open("emojis.sql")
                    br = BufferedReader(InputStreamReader(inputStream, StandardCharsets.UTF_8))

                    var str: String? = br.readLine()
                    while (str != null) {
                        db.execSQL(str)
                        str = br.readLine()
                    }
                } catch (e: Exception) {
//                    L.tag(TAG)
//                            .throwable(e)
//                            .log("error inserting emojis")
                } finally {
                    br?.close()
                }
            }
        }
    }
}