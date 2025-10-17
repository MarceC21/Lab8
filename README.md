# Lab8
Esta es una aplicación Android que permite buscar, ver y guardar fotos de la API de Pexels, con historial de búsquedas y fotos favoritas. Desarrollada en Jetpack Compose y Room, maneja el estado sin ViewModel.

#Cosas clave

-Modelo de datos: Photo, Favorite y Search. Cada entidad tiene su propio DAO.
-Estado: remember y rememberSaveable para listas de fotos, búsquedas y carga.
-Caché y paginación:
  Room guarda búsquedas y fotos.
  Se cargan primero los datos locales; la API solo se consulta si no hay resultados.
  Paginación manual por páginas de la API.
-Offline: fotos y favoritas disponibles sin conexión; nuevas búsquedas requieren internet.
Trade-offs

Sin ViewModel, el estado en memoria se pierde si la app se destruye.

Room duplica algunos datos, pero mejora velocidad y acceso.

Paginación manual es más simple que usar Paging 3, aunque menos automatizada.

Estructura principal
com.example.lab8
├─ Screens/       # Pantallas: Home, Details, Profile
├─ Database/      # Room: Photo, Favorite, Search, DAO
├─ NavHost.kt     # Navegación
└─ MainActivity.kt

Cómo usar

Inicio: en la pantalla principal se muestran fotos recientes y favoritas.

Buscar fotos: escribe un término en la barra de búsqueda y presiona buscar.

Detalles: selecciona una foto para ver detalles y guardarla como favorita.

Perfil / Favoritos: revisa tus fotos guardadas incluso sin conexión.

Navegación: la app usa navegación con rutas y argumentos (NavHost).
