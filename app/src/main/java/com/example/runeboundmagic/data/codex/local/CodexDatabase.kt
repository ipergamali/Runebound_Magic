package com.example.runeboundmagic.data.codex.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        CodexHeroEntity::class,
        CodexInventoryEntity::class,
        CodexInventoryItemEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(CodexTypeConverters::class)
abstract class CodexDatabase : RoomDatabase() {

    abstract fun codexDao(): CodexDao

    companion object {
        @Volatile
        private var instance: CodexDatabase? = null

        fun getInstance(context: Context): CodexDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context): CodexDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                CodexDatabase::class.java,
                "codex.db"
            ).fallbackToDestructiveMigration()
                .build()
        }
    }
}
