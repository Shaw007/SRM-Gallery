package com.srmstudios.srmgallery.data

import android.content.ContentResolver
import android.content.ContentUris
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.provider.MediaStore
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.srmstudios.srmgallery.data.database.entity.DatabaseMedia
import com.srmstudios.srmgallery.data.database.entity.DatabaseMediaAlbum
import com.srmstudios.srmgallery.util.IMAGES_MEDIA_URI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MediaRepository @Inject constructor(
    private val contentResolver: ContentResolver
){
    private lateinit var contentObserver: ContentObserver

    private val _databaseMediaAlbums = MutableLiveData<List<DatabaseMediaAlbum>>()
    val databaseMediaAlbums: LiveData<List<DatabaseMediaAlbum>> = _databaseMediaAlbums

    private val _databaseMediaList = MutableLiveData<List<DatabaseMedia>>()
    val databaseMediaList: LiveData<List<DatabaseMedia>> = _databaseMediaList

    suspend fun loadAllAlbums() = withContext(Dispatchers.IO){
        val databaseMediaAlbums = mutableListOf<DatabaseMediaAlbum>()
        val databaseMediaList = mutableListOf<DatabaseMedia>()

        val projection = arrayOf(
            MediaStore.Images.Media.BUCKET_ID,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Images.Media.DATA,

            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_ADDED
        )
        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

        contentResolver.query(
            IMAGES_MEDIA_URI,
            projection,
            null,
            null,
            sortOrder
        )?.use { cursor ->
            val bucketIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_ID)
            val bucketNameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)

            val imageIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val imageNameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val imageDateTakenColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)

            var bucketId: Long
            var bucketName: String
            var bucketRelativePath: String
            var imageId: Long
            var imageName: String
            var imageDateTaken: Long

            while (cursor.moveToNext()){
                bucketId = cursor.getLong(bucketIdColumn)
                bucketName = cursor.getString(bucketNameColumn)
                imageId = cursor.getLong(imageIdColumn)
                imageName = cursor.getString(imageNameColumn)
                imageDateTaken = cursor.getLong(imageDateTakenColumn)

                if(databaseMediaAlbums.filter { it.id == bucketId }.isEmpty()){
                    // Add new album here
                    databaseMediaAlbums.add(
                        DatabaseMediaAlbum(
                            bucketId,
                            bucketName,
                            ContentUris.withAppendedId(
                                IMAGES_MEDIA_URI,
                                imageId
                            )
                        )
                    )
                }

                databaseMediaList.add(
                    DatabaseMedia(
                        imageId,
                        imageName,
                        databaseMediaAlbums.filter { it.id == bucketId }[0].id,
                        ContentUris.withAppendedId(
                            IMAGES_MEDIA_URI,
                            imageId
                        ),
                        ContentUris.withAppendedId(
                            IMAGES_MEDIA_URI,
                            imageId
                        ),
                        imageDateTaken
                    )
                )
            }
        }

        databaseMediaAlbums.forEach { databaseMediaAlbum ->
            databaseMediaAlbum.itemsCount =
                databaseMediaList.filter { it.albumId == databaseMediaAlbum.id }.size
        }

        _databaseMediaAlbums.postValue(databaseMediaAlbums)
        _databaseMediaList.postValue(databaseMediaList)
    }

    fun registerImagesMediaObserver(scope: CoroutineScope){
        if(!::contentObserver.isInitialized){
            contentObserver = contentResolver.registerObserver(
                IMAGES_MEDIA_URI
            ){
                scope.launch(Dispatchers.IO) {
                    loadAllAlbums()
                }
            }
        }
    }

    fun unregisterImagesMediaObserver(){
        if(::contentObserver.isInitialized) {
            contentResolver.unregisterContentObserver(contentObserver)
        }
    }
}

/**
 * Convenience extension method to register a [ContentObserver] given a lambda.
 */
private fun ContentResolver.registerObserver(
    uri: Uri,
    observer: (selfChange: Boolean) -> Unit
): ContentObserver {
    val contentObserver = object : ContentObserver(Handler()) {
        override fun onChange(selfChange: Boolean) {
            observer(selfChange)
        }
    }
    registerContentObserver(uri, true, contentObserver)
    return contentObserver
}