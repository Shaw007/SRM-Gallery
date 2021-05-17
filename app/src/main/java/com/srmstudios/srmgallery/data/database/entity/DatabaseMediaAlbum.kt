package com.srmstudios.srmgallery.data.database.entity

import android.net.Uri

data class DatabaseMediaAlbum(
    val id: Long,
    val name: String,
    val thumbnailUri: Uri,
    var itemsCount: Int = 0
)
