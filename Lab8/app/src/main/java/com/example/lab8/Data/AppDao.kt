package com.example.lab8.Data

import androidx.paging.PagingSource
import androidx.room.*

@Dao
interface AppDao {


    // ------- Photos (para Paging) -------
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhotos(photos: List<PhotoEntity>)

    @Query("DELETE FROM photos WHERE query = :query")
    suspend fun clearPhotosForQuery(query: String)

    @Query("SELECT * FROM photos WHERE query = :query ORDER BY page ASC, id ASC")
    fun pagingSourceByQuery(query: String): PagingSource<Int, PhotoEntity>

    @Query("SELECT * FROM photos WHERE id = :photoId")
    suspend fun getPhotoById(photoId: Long): PhotoEntity?

    @Transaction
    suspend fun upsertSearch(query: String) {
        deleteSearchByQuery(query)
        insertSearch(Search(query = query, searchedAt = System.currentTimeMillis()))
    }

    // ------- RemoteKeys -------
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRemoteKeys(remoteKeys: RemoteKeys)

    @Query("SELECT * FROM remote_keys WHERE query = :query")
    suspend fun remoteKeysByQuery(query: String): RemoteKeys?

    @Query("DELETE FROM remote_keys WHERE query = :query")
    suspend fun clearRemoteKeysForQuery(query: String)

    // ------- Favorites -------

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: Favorite)

    @Delete
    suspend fun deleteFavorite(favorite: Favorite)

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE photoId = :id)")
    suspend fun isFavorite(id: Long): Boolean

    @Query("SELECT * FROM favorites ORDER BY addedAt DESC")
    fun getAllFavorites(): PagingSource<Int, Favorite>

    // ------- Search -------
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearch(search: Search)

    @Query("DELETE FROM search WHERE query = :query")
    suspend fun deleteSearchByQuery(query: String)

    @Query("SELECT * FROM search ORDER BY searchedAt DESC LIMIT :limit")
    suspend fun getLastSearches(limit: Int = 10): List<Search>
}
