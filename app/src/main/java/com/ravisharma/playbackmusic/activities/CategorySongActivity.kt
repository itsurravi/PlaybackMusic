package com.ravisharma.playbackmusic.activities

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.ravisharma.playbackmusic.R
import com.ravisharma.playbackmusic.activities.viewmodel.CategorySongViewModel
import com.ravisharma.playbackmusic.adapters.SongAdapter
import com.ravisharma.playbackmusic.adapters.SongAdapter.OnItemLongClicked
import com.ravisharma.playbackmusic.databinding.ActivityCategorySongBinding
import com.ravisharma.playbackmusic.databinding.PlaylistsLayoutBinding
import com.ravisharma.playbackmusic.model.Song
import com.ravisharma.playbackmusic.provider.SongsProvider
import com.ravisharma.playbackmusic.utils.longclick.LongClickItems
import java.util.*

const val QUERY_ALBUM = "Album"
const val QUERY_ARTIST = "Artist"

class CategorySongActivity : AppCompatActivity(), SongAdapter.OnItemClicked, OnItemLongClicked {
    private var adView: AdView? = null
    private var id: String? = null
    private var actName: String? = null
    private var category: String? = null

    private var songList: ArrayList<Song> = ArrayList()

    private lateinit var binding: ActivityCategorySongBinding
    private lateinit var playlistBinding: PlaylistsLayoutBinding

    private lateinit var viewModel: CategorySongViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false)
        window.statusBarColor = Color.TRANSPARENT

        binding = ActivityCategorySongBinding.inflate(LayoutInflater.from(this))
        playlistBinding = binding.playlistLayout

        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(CategorySongViewModel::class.java)

        adView = AdView(this)

        if (intent.hasExtra("albumId")) {
            val albumId = intent.extras!!.getString("albumId")
            val actName = intent.extras!!.getString("actName")
            this.id = albumId
            this.actName = actName
            this.category = QUERY_ALBUM
            adView!!.adUnitId = getString(R.string.AlbumSongsActId)
        } else if (intent.hasExtra("artistId")) {
            val artistId = intent.extras!!.getString("artistId")
            val actName = intent.extras!!.getString("actName")
            this.id = artistId
            this.actName = actName
            this.category = QUERY_ARTIST
            adView!!.adUnitId = getString(R.string.artistSongsActId)
        } else if (intent.hasExtra("actName")) {
            val actName = intent.extras!!.getString("actName")
            this.actName = actName
            when (actName) {
                "Recent Added" -> adView!!.adUnitId = getString(R.string.recentSongsActId)
                "Last Played" -> adView!!.adUnitId = getString(R.string.albumFragId)
                "Most Played" -> adView!!.adUnitId = getString(R.string.artistFragId)
            }
        }

        initRecyclerView()

        binding.bannerContainerRecentActivity.addView(adView)
        loadBanner()
    }

    private fun initRecyclerView() {
        playlistBinding.songList.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this@CategorySongActivity, RecyclerView.VERTICAL, false)
            itemAnimator = DefaultItemAnimator()
            addItemDecoration(DividerItemDecoration(this.context, DividerItemDecoration.VERTICAL))
            adapter = SongAdapter(this@CategorySongActivity).apply {
                setOnClick(this@CategorySongActivity)
                setOnLongClick(this@CategorySongActivity)
            }
        }
        category?.let {
            viewModel.getCategorySongs(it, id!!, contentResolver).observe(this, { songs ->
                songList.clear()
                songList.addAll(songs!!)
                setUpLayout()
            })
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
                viewModel.getLastPlayedSongsList(this).observe(this, { lastPlayedList ->
                    songList.clear()
                    for ((song) in lastPlayedList) {
                        songList.add(song)
                    }
                    setUpLayout()
                })
            }
            "Most Played" -> {
                viewModel.getMostPlayedSongsList(this).observe(this, { mostPlayedList ->
                    songList.clear()
                    for ((song) in mostPlayedList) {
                        songList.add(song)
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
                placeholder(R.drawable.logo)
                error(R.drawable.logo)
            }
            Glide.with(this@CategorySongActivity)
                    .setDefaultRequestOptions(requestOptions)
                    .load(Uri.parse(songList[0].art))
                    .diskCacheStrategy(DiskCacheStrategy.DATA)
                    .into(playlistBinding.albumArt)
        }
    }

    fun finishPage(view: View?) {
        finish()
    }

    private fun setWindowFlag(activity: Activity, bits: Int, on: Boolean) {
        val win = activity.window
        val winParams = win.attributes
        if (on) {
            winParams.flags = winParams.flags or bits
        } else {
            winParams.flags = winParams.flags and bits.inv()
        }
        win.attributes = winParams
    }

    private fun loadBanner() {
        val adRequest = AdRequest.Builder().build()
        val adSize = AdSize.BANNER
        adView!!.adSize = adSize
        adView!!.loadAd(adRequest)
    }

    override fun onItemClick(position: Int) {
        Intent().apply {
            putExtra("position", position)
            putExtra("songList", songList)
            setResult(RESULT_OK, this)
        }
        finish()
    }

    override fun onItemLongClick(position: Int) {
        LongClickItems(this, position, songList)
    }

    fun updateList(mPosition: Int) {
        songList.removeAt(mPosition)
        if (songList.size > 0) {
            (playlistBinding.songList.adapter as SongAdapter).setList(songList)
        } else {
            finish()
        }
    }

    override fun onDestroy() {
        if (adView != null) {
            adView!!.destroy()
        }
        songList.clear()
        super.onDestroy()
    }

    fun longPressOnItemClick(list: ArrayList<Song>) {
        Intent().apply {
            putExtra("position", 0)
            putExtra("songList", list)
            setResult(RESULT_OK, this)
        }
        finish()
    }
}