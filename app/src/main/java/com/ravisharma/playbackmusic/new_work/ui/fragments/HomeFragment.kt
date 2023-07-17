package com.ravisharma.playbackmusic.new_work.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.ravisharma.playbackmusic.R
import com.ravisharma.playbackmusic.databinding.FragmentHomeBinding
import com.ravisharma.playbackmusic.new_work.ui.adapters.HomePageAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)

        setupFragment()
    }

    private fun setupFragment() {
        initToolbar()
        initPager()
    }

    private fun initToolbar() {
        binding.apply {
            toolbar.title = getString(R.string.app_name)
            toolbar.setTitleTextColor(ContextCompat.getColor(requireContext(), R.color.titleColor))
        }
    }

    private fun initPager() {
        binding.apply {
            val pagerAdapter = HomePageAdapter(requireActivity())
            viewPager.apply {
                offscreenPageLimit = 4
                adapter = pagerAdapter
            }

            TabLayoutMediator(tabs, viewPager) { tab: TabLayout.Tab, position: Int ->
                when (position) {
                    0 -> tab.text = getString(R.string.playlist)
                    1 -> tab.text = getString(R.string.Tracks)
                    2 -> tab.text = getString(R.string.Albums)
                    3 -> tab.text = getString(R.string.Artists)
                }
            }.attach()

            viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
                ) {
                    super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                    fabShuffle.isVisible = position == 1
                }
            })
        }
    }
}