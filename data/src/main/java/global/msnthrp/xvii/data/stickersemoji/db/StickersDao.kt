package global.msnthrp.xvii.data.stickersemoji.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import global.msnthrp.xvii.data.stickersemoji.model.Sticker
import global.msnthrp.xvii.data.stickersemoji.model.StickerUsage
import io.reactivex.Completable
import io.reactivex.Single

@Dao
interface StickersDao {

    @Query("SELECT * FROM stickers")
    fun getAllStickers(): Single<List<Sticker>>

    @Query("SELECT sticker_id FROM sticker_usages WHERE last_used != 0 ORDER BY last_used DESC LIMIT 30")
    fun getRecentStickers(): Single<List<Int>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun updateStickerUsed(stickerUsage: StickerUsage): Completable

    @Query("UPDATE stickers SET key_words_custom = :customKeyWords WHERE id = :stickerId")
    fun updateStickerKeywords(stickerId: Int, customKeyWords: String): Completable

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveStickers(stickers: List<Sticker>): Completable

    @Query("DELETE FROM stickers")
    fun clearStickers(): Completable
}