package com.srmstudios.srmgallery.ui.media_detail

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import coil.ImageLoader
import com.srmstudios.srmgallery.R
import com.srmstudios.srmgallery.databinding.FragmentMediaDetailBinding
import com.srmstudios.srmgallery.ui.BaseFragment
import com.srmstudios.srmgallery.ui.SRMViewModel
import com.srmstudios.srmgallery.util.loadFullSizeImage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MediaDetailFragment: BaseFragment<FragmentMediaDetailBinding>(R.layout.fragment_media_detail) {
    override val bindingInflater: (View) -> FragmentMediaDetailBinding
        get() = FragmentMediaDetailBinding::bind

    @Inject
    lateinit var imageLoader: ImageLoader

    private val viewModel: SRMViewModel by activityViewModels()
    private val args: MediaDetailFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        setupListeners()
    }

    fun setupViews(){
        viewModel.databaseMediaList.observe(viewLifecycleOwner) { mediaList ->
            mediaList.filter { it.id == args.mediaId }?.firstOrNull()?.let { databaseMedia ->
                binding.zoomageView.loadFullSizeImage(
                    imageLoader,
                    databaseMedia.uri
                )
            }
        }
    }

    fun setupListeners(){

    }
}









