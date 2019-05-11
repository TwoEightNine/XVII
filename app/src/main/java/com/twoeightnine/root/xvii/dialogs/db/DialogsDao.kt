package com.twoeightnine.root.xvii.dialogs.db

import androidx.room.*
import com.twoeightnine.root.xvii.dialogs.models.Dialog
import io.reactivex.Completable
import io.reactivex.Single

@Dao
interface DialogsDao {

    @Query("SELECT * FROM dialogs ORDER BY isPinned DESC, timeStamp DESC")
    fun getDialogs(): Single<List<Dialog>>

    @Query("SELECT * FROM dialogs WHERE :peerId = peerId")
    fun getDialogs(peerId: Int): Single<Dialog>

    @Query("SELECT peerId FROM dialogs WHERE isPinned = 1")
    fun getPinned(): Single<List<Int>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertDialog(dialog: Dialog): Completable

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertDialogs(vararg dialogs: Dialog): Completable

    @Delete
    fun removeDialog(dialog: Dialog): Single<Int>

    @Query("DELETE FROM dialogs where isPinned = 0 and alias = ''")
    fun removeAll(): Completable
}