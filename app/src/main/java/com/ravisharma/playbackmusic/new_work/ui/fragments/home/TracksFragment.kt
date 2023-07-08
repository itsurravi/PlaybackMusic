package com.ravisharma.playbackmusic.new_work.ui.fragments.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.ravisharma.playbackmusic.R
import com.ravisharma.playbackmusic.databinding.FragmentNameWiseBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TracksFragment : Fragment(R.layout.fragment_name_wise) {

    private var _binding: FragmentNameWiseBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentNameWiseBinding.bind(view)

        setupFragment()
    }

    private fun setupFragment() {

    }
}