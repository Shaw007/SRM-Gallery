package com.srmstudios.srmgallery.ui.media_list

import android.os.Bundle
import android.util.Log
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import coil.ImageLoader
import com.srmstudios.srmgallery.R
import com.srmstudios.srmgallery.data.database.entity.DatabaseMedia
import com.srmstudios.srmgallery.databinding.FragmentMediaListBinding
import com.srmstudios.srmgallery.ui.BaseFragment
import com.srmstudios.srmgallery.ui.SRMViewModel
import com.srmstudios.srmgallery.util.FileUtil
import com.srmstudios.srmgallery.util.GridSpacingItemDecoration
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MediaListFragment: BaseFragment<FragmentMediaListBinding>(R.layout.fragment_media_list) {
    override val bindingInflater: (View) -> FragmentMediaListBinding
        get() = FragmentMediaListBinding::bind

    @Inject
    lateinit var imageLoader: ImageLoader

    private val viewModel: SRMViewModel by activityViewModels()
    private val args: MediaListFragmentArgs by navArgs()
    private lateinit var adapter: MediaListAdapter
    private lateinit var selectionTracker: SelectionTracker<DatabaseMedia>
    private var actionModeSelection: ActionMode? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews(savedInstanceState)
        setupListeners()
    }

    private fun setupViews(savedInstanceState: Bundle?){
        adapter = MediaListAdapter(imageLoader) { databaseMedia ->
            findNavController().navigate(MediaListFragmentDirections.actionMediaListFragmentToMediaDetailFragment(
                databaseMedia.id
            ))
        }

        binding.recyclerViewMedia.apply {
            setHasFixedSize(true)
            val spacingInPixels = resources.getDimensionPixelSize(R.dimen.spacing_recycler_view_home)
            addItemDecoration(GridSpacingItemDecoration(2, spacingInPixels, true, 0))
            adapter = this@MediaListFragment.adapter
        }

        selectionTracker = SelectionTracker.Builder<DatabaseMedia>(
            "media_list_adapter_selection",
            binding.recyclerViewMedia,
            MediaListAdapter.MediaListAdapterKeyProvider(adapter),
            MediaListAdapter.MediaListAdapterLookup(binding.recyclerViewMedia),
            StorageStrategy.createParcelableStorage(DatabaseMedia::class.java)
        ).withSelectionPredicate(
            SelectionPredicates.createSelectAnything()
        ).build()
        selectionTracker.onRestoreInstanceState(savedInstanceState)
        adapter.setSelectionTracker(selectionTracker)

        // handle configuration changes for ActionMode
        if(savedInstanceState != null && !selectionTracker.selection.isEmpty){
            handleOnSelectionChanged()
        }
    }

    private fun setupListeners(){
        viewModel.databaseMediaList.observe(viewLifecycleOwner){ mediaList ->
            val albumMedia = mediaList.filter { it.albumId == args.albumId }
            adapter?.submitList(albumMedia)
        }

        selectionTracker.addObserver(
            object : SelectionTracker.SelectionObserver<DatabaseMedia>() {
                override fun onSelectionChanged() {
                    super.onSelectionChanged()
                    handleOnSelectionChanged()
                }
            })
    }

    private fun handleOnSelectionChanged(){
        val items = selectionTracker.selection
        if(items.isEmpty){
            actionModeSelection?.finish()
        }else{
            if (actionModeSelection == null) {
                actionModeSelection = activity?.startActionMode(actionModeSelectionCallback)
            }
        }
        actionModeSelection?.title = getString(R.string.media_selection_count,items.size())
    }

    private fun onSelectMediaItems(){
        context?.let { context ->
            lifecycleScope.launch {
                selectionTracker.selection.forEach { databaseMedia ->
                    val file = FileUtil.getFileFromUri(context, databaseMedia.uri)
                    Log.d("BOSSSS_DK", "${file?.absolutePath}")
                }
            }
        }
    }

    private val actionModeSelectionCallback: ActionMode.Callback =
        object : ActionMode.Callback {
            override fun onCreateActionMode(
                mode: ActionMode,
                menu: Menu
            ): Boolean {
                val inflater = mode.menuInflater
                inflater.inflate(R.menu.menu_media_selection, menu)
                return true
            }

            override fun onPrepareActionMode(
                mode: ActionMode,
                menu: Menu
            ): Boolean {
                return false
            }

            override fun onActionItemClicked(
                mode: ActionMode,
                item: MenuItem
            ): Boolean {
                when(item.itemId){
                    R.id.actionSelect -> {
                        onSelectMediaItems()
                        return true
                    }
                }
                return false
            }

            override fun onDestroyActionMode(mode: ActionMode) {
                actionModeSelection = null
                selectionTracker?.clearSelection()
            }
        }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        selectionTracker?.let {
            selectionTracker?.onSaveInstanceState(outState)
        }
    }
}




