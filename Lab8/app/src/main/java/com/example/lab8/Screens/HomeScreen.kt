package com.example.lab8.Screens

package com.example.lab8

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.lab8.Data.*
import com.example.lab8.remote.PexelsRemoteMediator
import com.example.lab8.remote.PexelsService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.paging.*
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.LazyPagingItems

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

    // 🔹 Pager seguro con RemoteMediator
    val pager = remember(searchTrigger) {
        Pager(
            config = PagingConfig(pageSize = 30, enablePlaceholders = false, prefetchDistance = 5),
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

    // 🔹 Guarda búsquedas en historial
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
            // 🔹 Barra de búsqueda
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
                        Icon(Icons.Default.Search, contentDescription = "Buscar")
                    }
                },
                singleLine = true
            )
            var recentSearches by remember { mutableStateOf<List<Search>>(emptyList()) }
            LaunchedEffect(Unit) {
                recentSearches = dao.getLastSearches()
            }

            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                recentSearches.forEach { recent ->
                    Text(
                        text = recent.query,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                query = recent.query
                                searchTrigger = recent.query
                            }
                            .padding(vertical = 4.dp),
                        color = Color.Gray
                    )
                    Divider(color = Color.LightGray)
                }
            }


            // 🔹 Grid con paginación
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
        // ✅ Usa snapshotList para evitar índices fuera de rango
        items(lazyPagingItems.itemSnapshotList.items, key = { it.id }) { entity ->
            PhotoItem(
                photoEntity = entity,
                dao = dao,
                onPhotoClick = {
                    if (entity.id > 0) {
                        navController.navigate("details/${entity.id}")
                    }
                }
            )
        }

        // Footer de carga
        if (lazyPagingItems.loadState.append is LoadState.Loading) {
            item {
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
        onClick = onPhotoClick  // ✅ Usa el onClick nativo del Card
    ) {
        Box(Modifier.fillMaxSize()) {
            AsyncImage(
                model = photoEntity.url,
                contentDescription = photoEntity.description,
                modifier = Modifier.fillMaxSize()
            )

            // Gradiente solo al fondo (no bloquea toques)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
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
        }
    }

}

