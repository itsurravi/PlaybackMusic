package com.ravisharma.playbackmusic.fragments

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.ravisharma.playbackmusic.R
import com.ravisharma.playbackmusic.fragments.viewmodels.CategorySongViewModel
import com.ravisharma.playbackmusic.adapters.SongAdapter
import com.ravisharma.playbackmusic.database.repository.PlaylistRepository
import com.ravisharma.playbackmusic.databinding.ActivityCategorySongBinding
import com.ravisharma.playbackmusic.databinding.PlaylistsLayoutBinding
import com.ravisharma.playbackmusic.model.Song
import com.ravisharma.playbackmusic.provider.SongsProvider
import com.ravisharma.playbackmusic.utils.addNextSongToPlayingList
import com.ravisharma.playbackmusic.utils.addSongToPlayingList
import com.ravisharma.playbackmusic.utils.longclick.LongClickItems
import com.ravisharma.playbackmusic.utils.showSongInfo
import java.util.*

const val QUERY_ALBUM = "Album"
const val QUERY_ARTIST = "Artist"
const val PLAYLIST = "Playlist"

class CategorySongFragment : Fragment(), SongAdapter.OnItemClicked, SongAdapter.OnItemLongClicked {

    private var adView: AdView? = null
    private var id: String? = null
    private var actName: String? = null
    private var category: String? = null
    private var adUnitId: String? = null

    private lateinit var repository: PlaylistRepository

    private var songList: ArrayList<Song> = ArrayList()

    private lateinit var binding: ActivityCategorySongBinding
    private lateinit var playlistBinding: PlaylistsLayoutBinding

    private lateinit var viewModel: CategorySongViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            when (it.getString("argType")) {
                QUERY_ALBUM -> {
                    id = it.getString("albumId")
                    actName = it.getString("actName")
                    category = QUERY_ALBUM
                    adUnitId = getString(R.string.AlbumSongsActId)
                }
                QUERY_ARTIST -> {
                    id = it.getString("artistId")
                    actName = it.getString("actName")
                    category = QUERY_ARTIST
                    adUnitId = getString(R.string.artistSongsActId)
                }
                PLAYLIST -> {
                    actName = it.getString("actName")
                    adUnitId = getString(R.string.playlistFragId)
                }
                else -> {
                    actName = it.getString("actName")
                    when (actName) {
                        "Recent Added" -> adUnitId = getString(R.string.recentSongsActId)
                        "Last Played" -> adUnitId = getString(R.string.albumFragId)
                        "Most Played" -> adUnitId = getString(R.string.artistFragId)
                    }
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = ActivityCategorySongBinding.inflate(inflater, container, false)
        playlistBinding = binding.playlistLayout

        viewModel = ViewModelProvider(this).get(CategorySongViewModel::class.java)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adView = AdView(context)

        initRecyclerView()

        loadBanner()

        playlistBinding.imageBack1.setOnClickListener {
            activity!!.onBackPressed()
        }

        playlistBinding.imageBack2.setOnClickListener {
            activity!!.onBackPressed()
        }
    }

    private fun loadBanner() {
        val adRequest = AdRequest.Builder().build()
        val adSize = AdSize.BANNER
        adView!!.adUnitId = adUnitId
        adView!!.adSize = adSize

        binding.bannerContainerRecentActivity.addView(adView)

        adView!!.loadAd(adRequest)
    }

    private fun initRecyclerView() {
        playlistBinding.songList.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            itemAnimator = DefaultItemAnimator()
            addItemDecoration(DividerItemDecoration(this.context, DividerItemDecoration.VERTICAL))
            adapter = SongAdapter(context).apply {
                setOnClick(this@CategorySongFragment)
                setOnLongClick(this@CategorySongFragment)
            }
        }
        category?.let {
            viewModel.getCategorySongs(it, id!!, activity!!.contentResolver).observe(this, { songs ->
                songList.clear()
                songList.addAll(songs!!)
                setUpLayout()
            })

            return
        }

        when (actName) {
            "Recent Added" -> {
                SongsProvider.songListByDate.observe(this, { songs ->
                    songList.clear()
                    songList.addAll(songs!!)
                    setUpLayout()
                })
            }
            "Last Played" -> {
                viewModel.getLastPlayedSongsList(context!!).observe(this, { lastPlayedList ->
                    songList.clear()
                    for ((song) in lastPlayedList) {
                        songList.add(song)
                    }
                    setUpLayout()
                })
            }
            "Most Played" -> {
                viewModel.getMostPlayedSongsList(context!!).observe(this, { mostPlayedList ->
                    songList.clear()
                    for ((song) in mostPlayedList) {
                        songList.add(song)
                    }
                    setUpLayout()
                })
            }
            else -> {
                repository = PlaylistRepository(context)
                repository.getPlaylistSong(actName!!).observe(this, { playlists ->
                    songList.clear()
                    for ((_, _, song) in playlists) {
                        songList.add(song)
                    }
                    if (actName == getString(R.string.favTracks) && songList.size > 0) {
                        songList.reverse()
                    }
                    setUpLayout()
                })
            }

        }
    }

    private fun setUpLayout() {
        playlistBinding.txtPlaylistName1.text = actName
        playlistBinding.txtPlaylistName2.text = actName

        (playlistBinding.songList.adapter as SongAdapter).setList(songList)

        if (songList.size == 0) {
            playlistBinding.noDataFound.noDataLayout.visibility = View.VISIBLE
            playlistBinding.secondLayout.visibility = View.VISIBLE
            playlistBinding.firstLayout.visibility = View.GONE
        } else {
            playlistBinding.noDataFound.noDataLayout.visibility = View.GONE
            playlistBinding.secondLayout.visibility = View.GONE
            playlistBinding.firstLayout.visibility = View.VISIBLE

            val size = songList.size
            val noOfSongs = resources.getQuantityString(R.plurals.numberOfSongs, size, size)
            playlistBinding.noOfSongs.text = noOfSongs

            val requestOptions = RequestOptions().apply {
                error(R.drawable.logo)
            }
            Glide.with(context!!)
                    .setDefaultRequestOptions(requestOptions)
                    .load(Uri.parse(songList[0].art))
                    .diskCacheStrategy(DiskCacheStrategy.DATA)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(playlistBinding.albumArt)
        }
    }

    override fun onDestroyView() {
        if (adView != null) {
            adView!!.destroy()
        }
        super.onDestroyView()
    }

    override fun onItemClick(position: Int) {
        val onFragmentItemClicked = activity as OnFragmentItemClicked?
        onFragmentItemClicked!!.OnFragmentItemClick(position, songList, false)
    }

    override fun onItemLongClick(position: Int) {
        if (category == null
                && actName != "Recent Added"
                && actName != "Last Played"
                && actName != "Most Played"
        ) {
            playlistLongClick(position)
        } else {
            context?.let { LongClickItems(it, position, songList) }
        }
    }

    private fun playlistLongClick(mposition: Int) {
        val items = resources.getStringArray(R.array.longPressItemsRemove)
        val ad = ArrayAdapter(context!!, R.layout.adapter_alert_list, items)
        val v = LayoutInflater.from(context!!).inflate(R.layout.alert_list, null)
        val lv = v.findViewById<ListView>(R.id.list)
        val tv = v.findViewById<TextView>(R.id.title)
        val songArt = v.findViewById<ImageView>(R.id.songArt)
        val requestOptions = RequestOptions().apply {
            placeholder(R.drawable.logo)
            error(R.drawable.logo)
        }
        Glide.with(v)
                .setDefaultRequestOptions(requestOptions)
                .load(songList[mposition].art)
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(songArt)
        tv.text = songList[mposition].title
        lv.adapter = ad
        val dialog = AlertDialog.Builder(context!!)
        dialog.setView(v)
        val alertDialog = dialog.create()
        alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.window!!.attributes.windowAnimations = R.style.DialogAnimation_2
        alertDialog.show()
        lv.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            when (position) {
                0 -> onItemClick(mposition)
                1 -> addNextSongToPlayingList(songList[mposition])
                2 -> addSongToPlayingList(songList[mposition])
                3 ->                         // Delete Song Code
                    repository.removeSong(actName, songList[mposition].id)
                4 -> {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "audio/*"
                        putExtra(Intent.EXTRA_STREAM, Uri.parse(songList[mposition].data))
                    }
                    startActivity(Intent.createChooser(intent, "Share Via"))
                }
                5 -> context!!.showSongInfo(songList[mposition])
            }
            alertDialog.dismiss()
        }
    }

    interface OnFragmentItemClicked {
        fun OnFragmentItemClick(position: Int, songsArrayList: ArrayList<Song>, nowPlaying: Boolean)
    }
}