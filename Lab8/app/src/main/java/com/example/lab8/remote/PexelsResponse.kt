package com.example.lab8.remote

import com.example.lab8.Data.PhotoEntity
import com.squareup.moshi.Json


data class PexelsResponse(
    val page: Int,
    @Json(name = "per_page") val perPage: Int,
    val photos: List<PexelsPhoto>,
    @Json(name = "next_page") val nextPage: String?
)

data class PexelsPhoto(
    val id: Long,
    val width: Int,
    val height: Int,
    val url: String,
    val photographer: String,
    @Json(name = "photographer_url") val photographerUrl: String,
    @Json(name = "photographer_id") val photographerId: Long,
    @Json(name = "avg_color") val avgColor: String?,
    val src: PexelsSrc,
    val liked: Boolean,
    val alt: String?
) {
    // Mock de likes consistente por ID
    val mockLikes: Int
        get() = (id % 500).toInt()
}

data class PexelsSrc(
    val original: String,
    val large2x: String?,
    val large: String?,
    val medium: String?,
    val small: String?,
    val portrait: String?,
    val landscape: String?,
    val tiny: String?
)

// Extension para mapeo a Entity
fun PexelsPhoto.toEntity(query: String, page: Int): PhotoEntity {
    return PhotoEntity(
        id = id,
        url = src.medium ?: src.large ?: src.original,
        author = photographer,
        description = alt,
        query = query,
        page = page
    )
}

