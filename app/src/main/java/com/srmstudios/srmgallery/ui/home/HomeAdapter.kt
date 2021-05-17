package com.srmstudios.srmgallery.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.ImageLoader
import com.srmstudios.srmgallery.data.database.entity.DatabaseMediaAlbum
import com.srmstudios.srmgallery.databinding.ItemHomeMediaBinding
import com.srmstudios.srmgallery.util.loadThumbnail

class HomeAdapter(
    private val imageLoader: ImageLoader,
    private val itemClickListener: (DatabaseMediaAlbum) -> Unit
):
    ListAdapter<DatabaseMediaAlbum,RecyclerView.ViewHolder>(DIFF_UTIL) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        HomeViewHolder(
            ItemHomeMediaBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val databaseMediaAlbum = getItem(position)
        when(holder){
            is HomeViewHolder -> {
                holder.bind(databaseMediaAlbum)
            }
        }
    }
    
    inner class HomeViewHolder(private val binding: ItemHomeMediaBinding): RecyclerView.ViewHolder(binding.root){
        init {
            binding.root.setOnClickListener {
                if(adapterPosition != RecyclerView.NO_POSITION){
                    itemClickListener(getItem(adapterPosition))
                }
            }
        }

        fun bind(databaseMediaAlbum: DatabaseMediaAlbum){
            binding.apply {
                imgMedia.loadThumbnail(
                    imageLoader,
                    databaseMediaAlbum.thumbnailUri
                )
                txtNameWithCount.text = "${databaseMediaAlbum.name} (${databaseMediaAlbum.itemsCount})"
            }
        }
    }
    
    companion object{
        private val DIFF_UTIL = object : DiffUtil.ItemCallback<DatabaseMediaAlbum>(){
            override fun areItemsTheSame(
                oldItem: DatabaseMediaAlbum,
                newItem: DatabaseMediaAlbum
            ) = oldItem.id == newItem.id

            override fun areContentsTheSame(
                oldItem: DatabaseMediaAlbum,
                newItem: DatabaseMediaAlbum
            ) = oldItem == newItem
        }
    }
}