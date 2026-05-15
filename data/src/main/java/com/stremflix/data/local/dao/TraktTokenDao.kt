package com.stremflix.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.stremflix.data.local.entity.TraktTokenEntity

@Dao
interface TraktTokenDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTokens(tokens: TraktTokenEntity)

    @Update
    suspend fun updateTokens(tokens: TraktTokenEntity)

    @Query("SELECT * FROM trakt_tokens WHERE id = 1 LIMIT 1")
    suspend fun getTokens(): TraktTokenEntity?
}