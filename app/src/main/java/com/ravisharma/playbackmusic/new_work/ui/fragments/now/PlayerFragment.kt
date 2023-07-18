package com.ravisharma.playbackmusic.new_work.ui.fragments.now

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.ravisharma.playbackmusic.R
import com.ravisharma.playbackmusic.databinding.FragmentPlayerBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PlayerFragment : Fragment(R.layout.fragment_player) {

    private var _binding: FragmentPlayerBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPlayerBinding.bind(view)

        setupFragment()
    }

    private fun setupFragment() {
        // TODO("Not yet implemented")
    }

}