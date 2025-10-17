package com.example.lab8.repository



import androidx.paging.*
import com.example.lab8.Data.AppDatabase
import com.example.lab8.Data.PhotoEntity
import com.example.lab8.remote.PexelsApi
import com.example.lab8.remote.PexelsRemoteMediator
import kotlinx.coroutines.flow.Flow

@OptIn(ExperimentalPagingApi::class)
class PhotoRepository(
    private val database: AppDatabase,
    private val api: PexelsApi
) {

    fun getSearchResults(query: String): Flow<PagingData<PhotoEntity>> {
        val pagingSourceFactory = { database.appDao().pagingSourceByQuery(query) }

        return Pager(
            config = PagingConfig(
                pageSize = 30,
                enablePlaceholders = false,
                prefetchDistance = 5
            ),
            remoteMediator = PexelsRemoteMediator(
                query = query,
                database = database,
                api = api,
                pageSize = 30
            ),
            pagingSourceFactory = pagingSourceFactory
        ).flow
    }

    suspend fun toggleFavorite(photoId: Long) {
        val dao = database.appDao()
        val isFav = dao.isFavorite(photoId)
        if (isFav) {
            dao.deleteFavorite(com.example.lab8.Data.Favorite(photoId))
        } else {
            dao.insertFavorite(com.example.lab8.Data.Favorite(photoId))
        }
    }

    fun getFavorites(): Flow<PagingData<com.example.lab8.Data.Favorite>> {
        return Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = false),
            pagingSourceFactory = { database.appDao().getAllFavorites() }
        ).flow
    }
}
