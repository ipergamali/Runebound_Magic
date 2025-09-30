package com.example.runeboundmagic.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [HeroChoiceEntity::class, LobbyInteractionEntity::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(HeroTypeConverter::class)
abstract class HeroChoiceDatabase : RoomDatabase() {

    abstract fun heroChoiceDao(): HeroChoiceDao

    companion object {
        private const val DATABASE_NAME = "hero_choices.db"

        @Volatile
        private var instance: HeroChoiceDatabase? = null

        fun getInstance(context: Context): HeroChoiceDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    HeroChoiceDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instance = it }
            }
        }
    }
}
