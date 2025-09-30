package com.example.runeboundmagic.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HeroChoiceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChoice(choice: HeroChoiceEntity)

    @Query("SELECT * FROM hero_choices ORDER BY timestamp DESC LIMIT 1")
    fun observeLastChoice(): Flow<HeroChoiceEntity?>
}
