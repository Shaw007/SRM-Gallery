package com.srmstudios.srmgallery.util

import android.net.Uri
import android.widget.ImageView
import coil.ImageLoader
import coil.request.ImageRequest
import com.srmstudios.srmgallery.R
import java.io.File
import java.io.InputStream

fun ImageView.loadThumbnail(imageLoader: ImageLoader, uri: Uri){
    val request = ImageRequest.Builder(this.context)
            .data(uri)
            .target(this)
            .size(300,300)
            .placeholder(R.color.dark_grey)
            .build()
    imageLoader.enqueue(request)
}

fun ImageView.loadFullSizeImage(imageLoader: ImageLoader, uri: Uri){
    val request = ImageRequest.Builder(this.context)
        .data(uri)
        .target(this)
        .build()
    imageLoader.enqueue(request)
}

fun File.copyInputStream(inputStream: InputStream) {
    inputStream.use { input ->
        this.outputStream().use { fileOut ->
            input.copyTo(fileOut)
        }
    }
}