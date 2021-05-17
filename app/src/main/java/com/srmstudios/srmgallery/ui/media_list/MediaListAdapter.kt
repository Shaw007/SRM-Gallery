package com.srmstudios.srmgallery.ui.media_list

import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.ItemKeyProvider
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.ImageLoader
import com.srmstudios.srmgallery.data.database.entity.DatabaseMedia
import com.srmstudios.srmgallery.databinding.ItemMediaListBinding
import com.srmstudios.srmgallery.util.loadThumbnail

class MediaListAdapter(
    private val imageLoader: ImageLoader,
    private val itemClickListener: (DatabaseMedia) -> Unit
):
    ListAdapter<DatabaseMedia, RecyclerView.ViewHolder>(COMPARATOR) {

    private lateinit var selectionTracker: SelectionTracker<DatabaseMedia>

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        MediaViewHolder(
            ItemMediaListBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val databaseMedia = getItem(position)
        when (holder) {
            is MediaViewHolder -> {
                holder.bind(databaseMedia)
            }
        }
    }

    inner class MediaViewHolder(private val binding: ItemMediaListBinding): RecyclerView.ViewHolder(binding.root){
        init {
            binding.root.setOnClickListener {
                if(adapterPosition != RecyclerView.NO_POSITION){
                    itemClickListener(getItem(adapterPosition))
                }
            }
        }

        fun bind(databaseMedia: DatabaseMedia){
            val isSelected = selectionTracker.isSelected(databaseMedia)

            binding.imgMedia.loadThumbnail(
                imageLoader,
                databaseMedia.thumbnailUri
            )
            binding.viewSelection.isVisible = isSelected
        }

        fun getItemDetails(): ItemDetailsLookup.ItemDetails<DatabaseMedia> =
            object : ItemDetailsLookup.ItemDetails<DatabaseMedia>() {
                override fun getPosition(): Int = adapterPosition
                override fun getSelectionKey(): DatabaseMedia? = getItem(adapterPosition)
                // For single tap selection
                //override fun inSelectionHotspot(e: MotionEvent) = true
            }
    }

    //region RecyclerView Selection Tracking

    fun setSelectionTracker(tracker: SelectionTracker<DatabaseMedia>){
        selectionTracker = tracker
    }

    override fun getItemId(position: Int): Long = position.toLong()

    class MediaListAdapterKeyProvider(private val adapter: MediaListAdapter) : ItemKeyProvider<DatabaseMedia>(
        SCOPE_CACHED
    ) {
        override fun getKey(position: Int): DatabaseMedia? =
            adapter.currentList[position]
        override fun getPosition(key: DatabaseMedia): Int =
            adapter.currentList.indexOfFirst {it == key}
    }

    class MediaListAdapterLookup(private val recyclerView: RecyclerView) :
        ItemDetailsLookup<DatabaseMedia>() {
        override fun getItemDetails(event: MotionEvent): ItemDetails<DatabaseMedia>? {
            val view = recyclerView.findChildViewUnder(event.x, event.y)
            if (view != null) {
                return (recyclerView.getChildViewHolder(view) as MediaListAdapter.MediaViewHolder).getItemDetails()
            }
            return null
        }
    }

    //endregion

    companion object{
        private val COMPARATOR = object : DiffUtil.ItemCallback<DatabaseMedia>(){
            override fun areItemsTheSame(
                oldItem: DatabaseMedia,
                newItem: DatabaseMedia
            ) = oldItem.id == newItem.id

            override fun areContentsTheSame(
                oldItem: DatabaseMedia,
                newItem: DatabaseMedia
            ) = oldItem == newItem
        }
    }
}