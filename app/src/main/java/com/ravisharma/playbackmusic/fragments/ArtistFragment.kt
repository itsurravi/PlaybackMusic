package com.ravisharma.playbackmusic.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ravisharma.playbackmusic.MainActivity
import com.ravisharma.playbackmusic.adapters.ArtistAdapter
import com.ravisharma.playbackmusic.adapters.ArtistAdapter.OnArtistClicked
import com.ravisharma.playbackmusic.databinding.FragmentArtistBinding
import com.ravisharma.playbackmusic.model.Artist
import com.ravisharma.playbackmusic.provider.SongsProvider.Companion.artistList
import com.ravisharma.playbackmusic.utils.openFragment
import java.util.*

class ArtistFragment : Fragment(), OnArtistClicked {

    private var artistsList: ArrayList<Artist> = ArrayList()

    private lateinit var binding: FragmentArtistBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        binding = FragmentArtistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(v: View, savedInstanceState: Bundle?) {
        super.onViewCreated(v, savedInstanceState)
        binding.artistList.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            itemAnimator = DefaultItemAnimator()
            adapter = ArtistAdapter(artistsList).apply {
                setOnClick(this@ArtistFragment)
            }
            addItemDecoration(DividerItemDecoration(this.context, DividerItemDecoration.VERTICAL))
        }
        artistList.observe(viewLifecycleOwner, { artists ->
            if (artists.size > 0) {
                artistsList.clear()
                artistsList.addAll(artists)
                artistsList.sortWith { (_, artistName1), (_, artistName2) -> artistName1.compareTo(artistName2) }

                binding.artistList.adapter?.notifyDataSetChanged()
            }
        })
    }

    override fun onArtistClick(position: Int) {
        /*val bundle = Bundle().apply {
            putString("argType", QUERY_ARTIST)
            putString("artistId", artistsList[position].artistId.toString())
            putString("actName", artistsList[position].artistName)
        }

        val fragment = CategorySongFragment()
        fragment.arguments = bundle

        (activity as MainActivity).openFragment(fragment)*/
    }
}