package com.example.lab8.Screens


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.lab8.Data.*
import com.example.lab8.remote.PexelsRemoteMediator
import com.example.lab8.remote.PexelsService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.paging.*
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.LazyPagingItems
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import com.example.lab8.Data.DatabaseProvider
import kotlinx.coroutines.withContext


@OptIn(ExperimentalPagingApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    var query by rememberSaveable { mutableStateOf("nature") }
    var searchTrigger by rememberSaveable { mutableStateOf("nature") }

    val context = LocalContext.current
    val db = remember { DatabaseProvider.getDatabase(context) }
    val dao = db.appDao()
    val api = remember { PexelsService.api }
    val scope = rememberCoroutineScope()

    // Pager con RemoteMediator
    val pager = remember(searchTrigger) {
        Pager(
            config = PagingConfig(
                pageSize = 30,
                enablePlaceholders = false,
                prefetchDistance = 5
            ),
            remoteMediator = PexelsRemoteMediator(
                query = searchTrigger,
                database = db,
                api = api,
                pageSize = 30
            ),
            pagingSourceFactory = { dao.pagingSourceByQuery(searchTrigger) }
        )
    }

    val lazyPagingItems = pager.flow.collectAsLazyPagingItems()

    // Guardar búsqueda en historial
    LaunchedEffect(searchTrigger) {
        if (searchTrigger.isNotBlank()) {
            dao.upsertSearch(searchTrigger)

        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Fotos Pexels") },
                actions = {
                    IconButton(onClick = { navController.navigate("profile") }) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Perfil")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Barra de búsqueda
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Buscar fotos...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                trailingIcon = {
                    IconButton(
                        onClick = {
                            if (query.isNotBlank()) {
                                searchTrigger = query.trim()
                            }
                        },
                        enabled = query.isNotBlank()
                    ) {
                        Icon(Icons.Default.Search, "Buscar")
                    }
                },
                singleLine = true
            )

            // Grid con paginación
            PagingPhotoGrid(
                lazyPagingItems = lazyPagingItems,
                dao = dao,
                navController = navController
            )
        }
    }
}

@Composable
fun PagingPhotoGrid(
    lazyPagingItems: LazyPagingItems<PhotoEntity>,
    dao: AppDao,
    navController: NavController
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(150.dp),
        contentPadding = PaddingValues(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(lazyPagingItems.itemCount, key = { index -> lazyPagingItems[index]?.id ?: index }) { index ->
            val photoEntity = lazyPagingItems[index]
            photoEntity?.let { entity ->
                PhotoItem(
                    photoEntity = entity,
                    dao = dao,
                    onPhotoClick = { navController.navigate("details/${entity.id}") }
                )
            }
        }

        // Footer de carga
        if (lazyPagingItems.loadState.append is LoadState.Loading) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
fun PhotoItem(
    photoEntity: PhotoEntity,
    dao: AppDao,
    onPhotoClick: () -> Unit
) {
    var isFavorite by remember(photoEntity.id) { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(photoEntity.id) {
        isFavorite = withContext(Dispatchers.IO) {
            dao.isFavorite(photoEntity.id)
        }
    }

    Card(
        modifier = Modifier
            .padding(4.dp)
            .fillMaxWidth()
            .aspectRatio(1f),
        onClick = onPhotoClick
    ) {
        Box(Modifier.fillMaxSize()) {
            AsyncImage(
                model = photoEntity.url,
                contentDescription = photoEntity.description,
                modifier = Modifier.fillMaxSize()
            )

            // Overlay autor
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                        )
                    )
                    .padding(8.dp)
            ) {
                Text(
                    text = photoEntity.author,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Favorito
            IconButton(
                onClick = {
                    scope.launch(Dispatchers.IO) {
                        if (isFavorite) dao.deleteFavorite(Favorite(photoEntity.id))
                        else dao.insertFavorite(Favorite(photoEntity.id))
                        isFavorite = !isFavorite
                    }
                },
                modifier = Modifier
                    .padding(4.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorito",
                    tint = if (isFavorite) Color.Red else Color.White
                )
            }
        }
    }
}
