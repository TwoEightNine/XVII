package global.msnthrp.xvii.data.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migrations {

    const val DB_VERSION = 9

    fun getMigrations() = createMigrations().toTypedArray()

    private fun createMigrations(): List<Migration> = arrayListOf<Migration>().apply {

        addMigration(4, 5) {
            execSQL("ALTER TABLE dialogs ADD COLUMN isPinned INTEGER NOT NULL DEFAULT 0")
            execSQL("ALTER TABLE dialogs ADD COLUMN alias TEXT")
        }

        addMigration(5, 6) {
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

        addMigration(6, 7) {
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

        addMigration(7, 8) {
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

        addMigration(8, 9) {
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
    }

    private fun ArrayList<Migration>.addMigration(from: Int, to: Int, migration: SupportSQLiteDatabase.() -> Unit) {
        add(MigrationWrapper(from, to, migration))
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