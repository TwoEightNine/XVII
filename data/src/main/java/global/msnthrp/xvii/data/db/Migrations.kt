/*
 * xvii - messenger for vk
 * Copyright (C) 2021  TwoEightNine
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package global.msnthrp.xvii.data.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import global.msnthrp.xvii.core.crypto.CryptoUtils
import global.msnthrp.xvii.core.crypto.algorithm.Cipher
import global.msnthrp.xvii.core.utils.toByteArray
import global.msnthrp.xvii.data.session.EncryptedSessionProvider
import global.msnthrp.xvii.data.utils.ContextHolder

object Migrations {

    const val DB_VERSION = 10

    fun getMigrations() = createMigrations().toTypedArray()

    private fun createMigrations(): List<Migration> = arrayListOf<Migration>().apply {

        addMigration(5) {
            execSQL("ALTER TABLE dialogs ADD COLUMN isPinned INTEGER NOT NULL DEFAULT 0")
            execSQL("ALTER TABLE dialogs ADD COLUMN alias TEXT")
        }

        addMigration(6) {
            execSQL("CREATE TABLE stickers (" +
                    "id INTEGER NOT NULL," +
                    "key_words TEXT NOT NULL," +
                    "key_words_custom TEXT NOT NULL," +
                    "pack_name TEXT NOT NULL," +
                    "PRIMARY KEY(id)" +
                    ")")
            execSQL("CREATE TABLE emojis (" +
                    "code TEXT NOT NULL," +
                    "file_name TEXT NOT NULL," +
                    "pack_name TEXT NOT NULL," +
                    "PRIMARY KEY(code)" +
                    ")")
            execSQL("CREATE TABLE sticker_usages (" +
                    "sticker_id INTEGER NOT NULL," +
                    "last_used INTEGER NOT NULL," +
                    "PRIMARY KEY(sticker_id)" +
                    ")")
            execSQL("CREATE TABLE emoji_usages (" +
                    "emoji_code TEXT NOT NULL," +
                    "last_used INTEGER NOT NULL," +
                    "PRIMARY KEY(emoji_code)" +
                    ")")
        }

        addMigration(7) {
            execSQL("CREATE TABLE scheduled_messages (" +
                    "id INTEGER NOT NULL," +
                    "peer_id INTEGER NOT NULL," +
                    "when_ms INTEGER NOT NULL," +
                    "text TEXT NOT NULL," +
                    "attachments TEXT," +
                    "fwd_messages TEXT," +
                    "PRIMARY KEY(id)" +
                    ")")
        }

        addMigration(8) {
            execSQL("CREATE TABLE scheduled_messages_tmp (" +
                    "id INTEGER PRIMARY KEY NOT NULL," +
                    "peer_id INTEGER NOT NULL," +
                    "when_ms INTEGER NOT NULL," +
                    "text TEXT NOT NULL," +
                    "attachments TEXT," +
                    "fwd_messages TEXT" +
                    ")")
            execSQL("DROP TABLE scheduled_messages")
            execSQL("ALTER TABLE scheduled_messages_tmp RENAME TO scheduled_messages")
        }

        addMigration(9) {
            execSQL("CREATE TABLE journal (" +
                    "id INTEGER PRIMARY KEY NOT NULL," +
                    "type INTEGER NOT NULL," +
                    "peer_id INTEGER NOT NULL," +
                    "time_stamp INTEGER NOT NULL," +
                    "message_id INTEGER," +
                    "from_id INTEGER," +
                    "device_code INTEGER," +
                    "last_seen INTEGER," +
                    "text TEXT" +
                    ")")
        }

        addMigration(10) {
            execSQL("CREATE TABLE IF NOT EXISTS `accounts_tmp` (" +
                    "`uid` TEXT NOT NULL, " +
                    "`token` TEXT, " +
                    "`name` TEXT, " +
                    "`photo` TEXT, " +
                    "`isRunning` INTEGER NOT NULL, " +
                    "PRIMARY KEY(`uid`)" +
                    ")")
            query("SELECT * FROM accounts").use { cursor ->
                if (cursor.moveToFirst()) {
                    do {
                        val uid = cursor.getInt(cursor.getColumnIndex("uid"))
                        val token = cursor.getString(cursor.getColumnIndex("token"))
                        val name = cursor.getString(cursor.getColumnIndex("name"))
                        val photo = cursor.getString(cursor.getColumnIndex("photo"))
                        val isRunning = cursor.getInt(cursor.getColumnIndex("isRunning"))

                        val context = ContextHolder.contextProvider.applicationContext
                        val key = EncryptedSessionProvider(context).encryptionKey256
                        val encrypt: ByteArray.() -> String = {
                            Cipher.encrypt(this, key, deterministic = true).let(CryptoUtils::bytesToHex)
                        }

                        val encryptedUid = uid.toByteArray().encrypt()
                        val encryptedToken = token.toByteArray().encrypt()
                        val encryptedName = name.toByteArray().encrypt()
                        val encryptedPhoto = photo.toByteArray().encrypt()

                        execSQL("INSERT INTO accounts_tmp (" +
                                "uid, token, name, photo, isRunning" +
                                ") VALUES (" +
                                "\"$encryptedUid\", " +
                                "\"$encryptedToken\", " +
                                "\"$encryptedName\", " +
                                "\"$encryptedPhoto\", " +
                                "$isRunning" +
                                ")")
                    } while (cursor.moveToNext())
                }

                execSQL("DROP TABLE accounts")
                execSQL("ALTER TABLE accounts_tmp RENAME TO accounts")
            }
        }
    }

    private fun ArrayList<Migration>.addMigration(from: Int, to: Int, migration: SupportSQLiteDatabase.() -> Unit) {
        add(MigrationWrapper(from, to, migration))
    }

    private fun ArrayList<Migration>.addMigration(to: Int, migration: SupportSQLiteDatabase.() -> Unit) {
        add(MigrationWrapper(to - 1, to, migration))
    }

    private class MigrationWrapper(
            from: Int,
            to: Int,
            private val migration: SupportSQLiteDatabase.() -> Unit
    ) : Migration(from, to) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.migration()
        }
    }

}