package com.ravisharma.playbackmusic.new_work.ui.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.ravisharma.playbackmusic.new_work.ui.fragments.home.AlbumsFragment
import com.ravisharma.playbackmusic.new_work.ui.fragments.home.ArtistsFragment
import com.ravisharma.playbackmusic.new_work.ui.fragments.home.PlaylistsFragment
import com.ravisharma.playbackmusic.new_work.ui.fragments.home.TracksFragment

class HomePageAdapter(
    fm: FragmentActivity
) : FragmentStateAdapter(fm) {

    override fun getItemCount(): Int {
        return 4
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> PlaylistsFragment.getInstance()
            1 -> TracksFragment.getInstance()
            2 -> AlbumsFragment.getInstance()
            3 -> ArtistsFragment.getInstance()
            else -> PlaylistsFragment.getInstance()
        }
    }
}