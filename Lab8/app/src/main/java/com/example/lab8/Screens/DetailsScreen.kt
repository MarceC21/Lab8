package com.example.lab8.Screens

import android.content.Intent
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.lab8.DatabaseProvider
import com.example.lab8.Data.Favorite
import com.example.lab8.Data.PhotoEntity
import com.example.lab8.remote.PexelsPhoto
import com.example.lab8.remote.PexelsService
import com.example.lab8.remote.toEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch

// --- Mapper: PhotoEntity -> PexelsPhoto ---
fun PhotoEntity.toPexelsPhoto(): PexelsPhoto {
    return PexelsPhoto(
        id = id,
        width = 0,
        height = 0,
        url = url,
        photographer = author,
        photographerUrl = "",
        photographerId = 0,
        avgColor = null,
        src = com.example.lab8.remote.PexelsSrc(
            original = url,
            large2x = null,
            large = null,
            medium = url,
            small = null,
            portrait = null,
            landscape = null,
            tiny = null
        ),
        liked = false,
        alt = description
    )
}

@Composable
fun DetailsScreen(navController: NavController, photoId: Long?) {
    val context = LocalContext.current
    val db = remember { DatabaseProvider.getDatabase(context) }
    val dao = db.appDao()
    val api = remember { PexelsService.api }

    var photo by remember { mutableStateOf<PexelsPhoto?>(null) }
    var isFavorite by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(photoId) {
        if (photoId != null) {
            try {
                // 1️⃣ Buscar primero en Room
                val cached = withContext(Dispatchers.IO) { dao.getPhotoById(photoId) }
                if (cached != null) {
                    photo = cached.toPexelsPhoto()
                } else {
                    // 2️⃣ Si no hay cache → cargar de red
                    val response = withContext(Dispatchers.IO) { api.getPhotoById(photoId) }
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body != null) {
                            photo = body
                            // 3️⃣ Guardar en Room para cache futuro
                            withContext(Dispatchers.IO) {
                                dao.insertPhotos(listOf(body.toEntity(query = "detail", page = 0)))
                            }
                        }
                    }
                }

                // 4️⃣ Verificar favorito
                isFavorite = withContext(Dispatchers.IO) { dao.isFavorite(photoId) }

            } catch (e: Exception) {
                Log.e("DetailsScreen", "Error al cargar detalles", e)
            }
        }
    }

    // --- UI ---
    photo?.let { p ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AsyncImage(
                model = p.src.large ?: p.src.original,
                contentDescription = p.alt,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            )

            Spacer(Modifier.height(12.dp))
            Text("Título: ${p.alt ?: "Sin título"}", style = MaterialTheme.typography.titleMedium)
            Text("Autor: ${p.photographer}")
            Text("Likes: ${p.mockLikes}")

            Spacer(Modifier.height(16.dp))

            // Botón compartir
            Button(onClick = {
                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, p.url)
                    type = "text/plain"
                }
                context.startActivity(Intent.createChooser(sendIntent, "Compartir imagen"))
            }) {
                Text("Compartir")
            }

            Spacer(Modifier.height(12.dp))

            // Botón favorito
            Button(onClick = {
                scope.launch {
                    if (photoId != null) {
                        if (isFavorite) {
                            dao.deleteFavorite(Favorite(photoId))
                            isFavorite = false
                        } else {
                            dao.insertFavorite(Favorite(photoId))
                            isFavorite = true
                        }
                    }
                }
            }) {
                Text(if (isFavorite) "Quitar de favoritos" else "Agregar a favoritos")
            }

            Spacer(Modifier.height(12.dp))

            Button(onClick = { navController.popBackStack() }) {
                Text("Volver")
            }
        }
    } ?: Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}
