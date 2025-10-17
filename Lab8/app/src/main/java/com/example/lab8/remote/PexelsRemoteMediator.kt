package com.example.lab8.remote

import androidx.paging.ExperimentalPagingApi
import androidx.paging.RemoteMediator
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.room.withTransaction
import com.example.lab8.Data.AppDatabase
import com.example.lab8.Data.PhotoEntity
import com.example.lab8.Data.RemoteKeys
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalPagingApi::class)
class PexelsRemoteMediator(
    private val query: String,
    private val database: AppDatabase,
    private val api: PexelsApi,
    private val pageSize: Int = 30
) : RemoteMediator<Int, PhotoEntity>() {

    private val dao = database.appDao()

    override suspend fun initialize(): InitializeAction {
        // Decide si refrescar o no (ejemplo: 1h)
        val keys = dao.remoteKeysByQuery(query)
        val cacheTimeout = TimeUnit.MILLISECONDS.convert(1, TimeUnit.HOURS)
        return if (keys == null || System.currentTimeMillis() - keys.lastUpdated > cacheTimeout) {
            InitializeAction.LAUNCH_INITIAL_REFRESH
        } else {
            InitializeAction.SKIP_INITIAL_REFRESH
        }
    }

    override suspend fun load(loadType: LoadType, state: PagingState<Int, PhotoEntity>): MediatorResult {
        try {
            // Determinar pagina a cargar
            val page = when (loadType) {
                LoadType.REFRESH -> 1
                LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
                LoadType.APPEND -> {
                    val remoteKeys = dao.remoteKeysByQuery(query)
                    remoteKeys?.nextKey ?: 1
                }
            }

            val response = api.searchPhotos(query = query, page = page, perPage = pageSize)
            if (!response.isSuccessful) {
                return MediatorResult.Error(Exception("HTTP ${response.code()} ${response.message()}"))
            }
            val body = response.body() ?: return MediatorResult.Error(Exception("Empty body"))

            val photos = body.photos.map { p ->
                PhotoEntity(
                    id = p.id,
                    url = p.src.medium ?: p.src.large ?: p.src.original,
                    author = p.photographer,
                    description = p.alt,
                    query = query,
                    page = page
                )
            }

            // DB transaction: insertar photos y remote keys
            database.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    // limpiar cache viejo
                    dao.clearRemoteKeysForQuery(query)
                    dao.clearPhotosForQuery(query)
                }

                // insertar photos
                dao.insertPhotos(photos)

                // calcular nextKey
                val nextKey = if (body.nextPage.isNullOrEmpty()) {
                    null
                } else {
                    page + 1
                }

                dao.insertRemoteKeys(RemoteKeys(query = query, prevKey = if (page == 1) null else page - 1, nextKey = nextKey, lastUpdated = System.currentTimeMillis()))
            }

            val endOfPaginationReached = body.nextPage.isNullOrEmpty()
            return MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (e: Exception) {
            return MediatorResult.Error(e)
        }
    }
}
