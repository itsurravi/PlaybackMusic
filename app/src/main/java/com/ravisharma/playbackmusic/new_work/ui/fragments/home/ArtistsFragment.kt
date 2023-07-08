package com.ravisharma.playbackmusic.new_work.ui.fragments.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.ravisharma.playbackmusic.R
import com.ravisharma.playbackmusic.databinding.FragmentArtistBinding

class ArtistsFragment : Fragment(R.layout.fragment_artist) {

    private var _binding: FragmentArtistBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentArtistBinding.bind(view)

        setupFragment()
    }

    private fun setupFragment() {

    }
}