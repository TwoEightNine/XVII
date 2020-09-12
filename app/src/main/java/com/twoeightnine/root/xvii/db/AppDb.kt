package com.twoeightnine.root.xvii.db

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.twoeightnine.root.xvii.accounts.db.AccountsDao
import com.twoeightnine.root.xvii.accounts.models.Account
import com.twoeightnine.root.xvii.chats.attachments.stickersemoji.db.EmojisDao
import com.twoeightnine.root.xvii.chats.attachments.stickersemoji.db.StickersDao
import com.twoeightnine.root.xvii.chats.attachments.stickersemoji.model.Emoji
import com.twoeightnine.root.xvii.chats.attachments.stickersemoji.model.EmojiUsage
import com.twoeightnine.root.xvii.chats.attachments.stickersemoji.model.Sticker
import com.twoeightnine.root.xvii.chats.attachments.stickersemoji.model.StickerUsage
import com.twoeightnine.root.xvii.dialogs.db.DialogsDao
import com.twoeightnine.root.xvii.dialogs.models.Dialog
import com.twoeightnine.root.xvii.lg.L
import com.twoeightnine.root.xvii.scheduled.core.ScheduledMessage
import com.twoeightnine.root.xvii.scheduled.core.ScheduledMessageDao
import com.twoeightnine.root.xvii.utils.applyCompletableSchedulers
import io.reactivex.Completable
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets


@Database(entities = [
    Dialog::class, Account::class,
    Sticker::class, Emoji::class,
    StickerUsage::class, EmojiUsage::class,
    ScheduledMessage::class], version = 8)
abstract class AppDb : RoomDatabase() {

    abstract fun dialogsDao(): DialogsDao

    abstract fun accountsDao(): AccountsDao

    abstract fun stickersDao(): StickersDao

    abstract fun emojisDao(): EmojisDao

    abstract fun scheduledMessagesDao(): ScheduledMessageDao

    @SuppressLint("CheckResult")
    fun clearAsync() {
        Completable.fromCallable {
            clearAllTables()
        }
                .compose(applyCompletableSchedulers())
                .subscribe({}) {
                    L.tag(TAG)
                            .throwable(it)
                            .log("error clearing all tables")
                }
    }

    companion object {

        private const val TAG = "app db"

        private val MIGRATION_4_5 = object : Migration(4, 5) {

            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE dialogs ADD COLUMN isPinned INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE dialogs ADD COLUMN alias TEXT")
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {

            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE stickers (" +
                        "id INTEGER NOT NULL," +
                        "key_words TEXT NOT NULL," +
                        "key_words_custom TEXT NOT NULL," +
                        "pack_name TEXT NOT NULL," +
                        "PRIMARY KEY(id)" +
                        ")")
                database.execSQL("CREATE TABLE emojis (" +
                        "code TEXT NOT NULL," +
                        "file_name TEXT NOT NULL," +
                        "pack_name TEXT NOT NULL," +
                        "PRIMARY KEY(code)" +
                        ")")
                database.execSQL("CREATE TABLE sticker_usages (" +
                        "sticker_id INTEGER NOT NULL," +
                        "last_used INTEGER NOT NULL," +
                        "PRIMARY KEY(sticker_id)" +
                        ")")
                database.execSQL("CREATE TABLE emoji_usages (" +
                        "emoji_code TEXT NOT NULL," +
                        "last_used INTEGER NOT NULL," +
                        "PRIMARY KEY(emoji_code)" +
                        ")")
            }
        }

        private val MIGRATION_6_7 = object : Migration(6, 7) {

            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE scheduled_messages (" +
                        "id INTEGER NOT NULL," +
                        "peer_id INTEGER NOT NULL," +
                        "when_ms INTEGER NOT NULL," +
                        "text TEXT NOT NULL," +
                        "attachments TEXT," +
                        "fwd_messages TEXT," +
                        "PRIMARY KEY(id)" +
                        ")")
            }
        }

        private val MIGRATION_7_8 = object : Migration(7, 8) {

            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE scheduled_messages_tmp (" +
                        "id INTEGER PRIMARY KEY NOT NULL," +
                        "peer_id INTEGER NOT NULL," +
                        "when_ms INTEGER NOT NULL," +
                        "text TEXT NOT NULL," +
                        "attachments TEXT," +
                        "fwd_messages TEXT" +
                        ")")
                database.execSQL("DROP TABLE scheduled_messages")
                database.execSQL("ALTER TABLE scheduled_messages_tmp RENAME TO scheduled_messages")
            }
        }

        fun buildDatabase(context: Context) =
                Room.databaseBuilder(context.applicationContext,
                        AppDb::class.java, "xvii_room.db")
                        .addMigrations(MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8)
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
                L.tag(TAG)
                        .throwable(e)
                        .log("error getting emojis count")
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
                    L.tag(TAG)
                            .throwable(e)
                            .log("error inserting emojis")
                } finally {
                    br?.close()
                }
            }
        }
    }
}