package com.srmstudios.srmgallery.ui.home

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import coil.ImageLoader
import com.srmstudios.srmgallery.R
import com.srmstudios.srmgallery.databinding.FragmentHomeBinding
import com.srmstudios.srmgallery.ui.BaseFragment
import com.srmstudios.srmgallery.ui.SRMViewModel
import com.srmstudios.srmgallery.util.GridSpacingItemDecoration
import com.srmstudios.srmgallery.util.PermissionsHelper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>(R.layout.fragment_home),PermissionsHelper.IPermissionsHelper {
    override val bindingInflater: (View) -> FragmentHomeBinding
        get() = FragmentHomeBinding::bind

    @Inject
    lateinit var imageLoader: ImageLoader

    private val viewModel: SRMViewModel by activityViewModels()
    private lateinit var permissionsHelper: PermissionsHelper
    private var adapter: HomeAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.let { activity ->
            permissionsHelper = PermissionsHelper(
                activity,
                listOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                this
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        setupListeners()
    }

    private fun setupViews(){
        adapter = HomeAdapter(imageLoader) { databaseMediaAlbum ->
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToMediaListFragment(
                databaseMediaAlbum.name,
                databaseMediaAlbum.id
            ))
        }

        binding.recyclerViewHome.apply {
            setHasFixedSize(true)
            val spacingInPixels = resources.getDimensionPixelSize(R.dimen.spacing_recycler_view_home)
            addItemDecoration(GridSpacingItemDecoration(2, spacingInPixels, true, 0))
            adapter = this@HomeFragment.adapter
        }

        viewModel.databaseMediaAlbums.observe(viewLifecycleOwner){ mediaAlbums ->
            adapter?.submitList(mediaAlbums.sortedBy { it.name })
            mediaAlbums?.forEach {
                Log.d("BOSS_DK","${it}")
            }
        }

        /*viewModel.databaseMediaList.observe(viewLifecycleOwner){ mediaList ->
            Log.d("BOSS_DK","${mediaList.size}")
        }*/

        if(permissionsHelper.areAllPermissionsGranted()){
            viewModel.loadAllAlbums()
        }else {
            permissionsHelper.requestPermissions()
        }
    }

    /*override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home -> {
                (activity as AppCompatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(false)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }*/

    private fun setupListeners(){

    }

    override fun onAllPermissionsGranted() {
        viewModel.loadAllAlbums()
    }

    override fun onPermissionDenied(permission: String) {
        permissionsHelper.showDialog(
            "Permission needed",
            "$permission permission required",
            "OK"
        ) {
            permissionsHelper.requestPermissionThatWasDeniedByUser(
                permission
            )
        }
    }

    override fun onPermissionDeniedPermanently(permission: String) {
        permissionsHelper.showDialog(
            "Permission needed",
            "$permission permission denied permanently",
            "Settings",
            {
                permissionsHelper.goToPermissionSettings()
            },
            "Cancel",
            {

            })
    }
}














