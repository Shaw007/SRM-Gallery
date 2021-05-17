package com.srmstudios.srmgallery.data.database.entity

import android.net.Uri
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DatabaseMedia(
    val id: Long,
    val name: String,
    val albumId: Long,
    val thumbnailUri: Uri,
    val uri: Uri,
    val createdTimestamp: Long
): Parcelable