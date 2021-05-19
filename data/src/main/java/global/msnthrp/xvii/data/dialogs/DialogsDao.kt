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

package global.msnthrp.xvii.data.dialogs

import androidx.room.*
import io.reactivex.Completable
import io.reactivex.Single

@Dao
interface DialogsDao {

    @Query("SELECT * FROM dialogs ORDER BY isPinned DESC, timeStamp DESC")
    fun getDialogs(): Single<List<Dialog>>

    @Query("SELECT * FROM dialogs WHERE :peerId = peerId")
    fun getDialogs(peerId: Int): Single<Dialog>

    @Query("SELECT * FROM dialogs WHERE peerId IN (:peerIds)")
    fun getDialogsByPeerIds(peerIds: List<Int>): Single<List<Dialog>>

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