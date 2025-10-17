package com.example.lab8.Data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [PhotoEntity::class, Favorite::class, Search::class, RemoteKeys::class],
    version = 2,
    exportSchema = false //solo para evitar warnings
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao
}

