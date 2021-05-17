package com.srmstudios.srmgallery.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.srmstudios.srmgallery.data.MediaRepository
import com.srmstudios.srmgallery.data.database.entity.DatabaseMedia
import com.srmstudios.srmgallery.data.database.entity.DatabaseMediaAlbum
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SRMViewModel @Inject constructor(
    private val mediaRepository: MediaRepository
) : ViewModel() {

    init {
        mediaRepository.registerImagesMediaObserver(viewModelScope)
    }

    val databaseMediaAlbums: LiveData<List<DatabaseMediaAlbum>> =
        mediaRepository.databaseMediaAlbums
    val databaseMediaList: LiveData<List<DatabaseMedia>> = mediaRepository.databaseMediaList

    fun loadAllAlbums() = viewModelScope.launch {
        mediaRepository.loadAllAlbums()
    }

    override fun onCleared() {
        super.onCleared()
        mediaRepository.unregisterImagesMediaObserver()
    }
}