package com.stremflix.data.local.dao

import androidx.room.*
import com.stremflix.data.local.entity.MyListEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MyListDao {
    @Query("SELECT * FROM my_list ORDER BY dateAdded DESC")
    fun getAllItems(): Flow<List<MyListEntity>>

    @Query("SELECT * FROM my_list WHERE id = :id")
    suspend fun getItemById(id: String): MyListEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM my_list WHERE id = :id)")
    suspend fun isItemInList(id: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addItem(item: MyListEntity)

    @Query("DELETE FROM my_list WHERE id = :id")
    suspend fun removeItem(id: String)

    @Query("DELETE FROM my_list")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM my_list")
    suspend fun getCount(): Int
}