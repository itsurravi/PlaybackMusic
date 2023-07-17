package com.ravisharma.playbackmusic.new_work.ui.fragments.home.childFragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.ravisharma.playbackmusic.R
import com.ravisharma.playbackmusic.databinding.FragmentPlaylistBinding

class PlaylistsFragment : Fragment(R.layout.fragment_playlist) {

    private var _binding: FragmentPlaylistBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPlaylistBinding.bind(view)

        setupFragment()
    }

    private fun setupFragment() {

    }

    companion object {
        @JvmStatic
        fun getInstance(): PlaylistsFragment {
            return PlaylistsFragment()
        }
    }
}