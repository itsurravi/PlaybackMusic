package com.ravisharma.playbackmusic.fragments

import android.content.Context
import android.content.Intent
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
import com.ravisharma.playbackmusic.R
import com.ravisharma.playbackmusic.activities.CategorySongActivity
import com.ravisharma.playbackmusic.adapters.AlbumAdapter
import com.ravisharma.playbackmusic.adapters.AlbumAdapter.OnAlbumClicked
import com.ravisharma.playbackmusic.databinding.FragmentAlbumsBinding
import com.ravisharma.playbackmusic.model.Album
import com.ravisharma.playbackmusic.provider.SongsProvider.Companion.albumList
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView
import java.util.*

class AlbumsFragment : Fragment(), OnAlbumClicked {

    private var albumsList: ArrayList<Album>  = ArrayList()

    private lateinit var binding: FragmentAlbumsBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        // Inflate the layout for this fragment
        binding = FragmentAlbumsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(v: View, savedInstanceState: Bundle?) {
        super.onViewCreated(v, savedInstanceState)

        binding.albumList.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            itemAnimator = DefaultItemAnimator()
            adapter = AlbumAdapter(context, albumsList).apply {
                setOnClick(this@AlbumsFragment)
            }
            addItemDecoration(DividerItemDecoration(this.context, DividerItemDecoration.VERTICAL))
        }
        albumList.observe(this, { albums ->
            if (albums.size > 0) {
                albumsList.clear()
                albumsList.addAll(albums)
                albumsList.sortWith { (_, _, albumName1), (_, _, albumName2) -> albumName1.compareTo(albumName2) }
                binding.albumList.adapter?.notifyDataSetChanged()
            }
        })
    }

    override fun onAlbumClick(position: Int) {
        val i = Intent(context, CategorySongActivity::class.java)
        i.putExtra("albumId", albumsList[position].albumId.toString())
        i.putExtra("actName", albumsList[position].albumName)
        activity!!.startActivityForResult(i, MainActivity.ALBUM_SONGS)
    }
}