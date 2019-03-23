package com.twoeightnine.root.xvii.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.twoeightnine.root.xvii.dialogs2.db.DialogsDao
import com.twoeightnine.root.xvii.dialogs2.models.Dialog

@Database(entities = [Dialog::class], version = 3)
abstract class AppDb : RoomDatabase() {

    abstract fun dialogsDao(): DialogsDao

    companion object {

        fun buildDatabase(context: Context) =
                Room.databaseBuilder(context.applicationContext,
                        AppDb::class.java, "xvii_room.db")
                        .fallbackToDestructiveMigration()
                        .build()
    }
}