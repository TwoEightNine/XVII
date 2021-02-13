package global.msnthrp.xvii.data.journal

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface JournalDao {

    @Query("SELECT * FROM journal")
    fun getAllEvents(): List<JournalEntity>

    @Query("DELETE FROM journal WHERE time_stamp < :recentThresholdTimeStamp")
    fun clearAllExceptRecent(recentThresholdTimeStamp: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertEvent(journalEntity: JournalEntity)

}