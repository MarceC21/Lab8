package com.example.lab8.Data

// Entities.kt

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "photos")
data class PhotoEntity(
    @PrimaryKey val id: Long,
    val url: String,
    val author: String,
    val description: String?,
    val query: String,
    val page: Int,
    val insertedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "favorites")
data class Favorite(
    @PrimaryKey val photoId: Long,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "search")
data class Search(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val query: String,
    val searchedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "remote_keys")
data class RemoteKeys(
    @PrimaryKey val query: String,
    val prevKey: Int?,
    val nextKey: Int?,
    val lastUpdated: Long = System.currentTimeMillis()
)
