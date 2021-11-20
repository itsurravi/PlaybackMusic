package com.ravisharma.playbackmusic.activities

import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.ravisharma.playbackmusic.R
import com.ravisharma.playbackmusic.activities.viewmodel.AddToPlaylistViewModel
import com.ravisharma.playbackmusic.adapters.PlaylistAdapter
import com.ravisharma.playbackmusic.adapters.PlaylistAdapter.OnPlaylistClicked
import com.ravisharma.playbackmusic.adapters.PlaylistAdapter.OnPlaylistLongClicked
import com.ravisharma.playbackmusic.databinding.ActivityAddToPlaylistBinding
import com.ravisharma.playbackmusic.model.Song
import com.ravisharma.playbackmusic.utils.alert.AlertClickListener
import com.ravisharma.playbackmusic.utils.alert.PlaylistAlert
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class AddToPlaylistActivity : AppCompatActivity(), OnPlaylistClicked, OnPlaylistLongClicked {
    private var adView: AdView? = null
    private var song: Song? = null

    private lateinit var playlistAdapter: PlaylistAdapter
    private lateinit var list: ArrayList<String>

    private val viewModel: AddToPlaylistViewModel by viewModels()

    private lateinit var binding: ActivityAddToPlaylistBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddToPlaylistBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        song = intent.getParcelableExtra("Song")
        list = ArrayList()

        initRecyclerView()
        setUpArrayList()

        binding.imgBack.setOnClickListener { finish() }
        binding.btnAddNewPlaylist.setOnClickListener { showCreateListAlert() }

        adView = AdView(this)
        adView!!.adUnitId = getString(R.string.addToPlaylist)

        binding.bannerContainerAddToPlaylist.addView(adView)
        loadBanner()
    }

    private fun loadBanner() {
        val adRequest = AdRequest.Builder().build()
        val adSize = AdSize.BANNER
        adView!!.adSize = adSize
        adView!!.loadAd(adRequest)
    }

    private fun initRecyclerView() {
        playlistAdapter = PlaylistAdapter(this, list).apply {
            setOnPlaylistClick(this@AddToPlaylistActivity)
            setOnPlaylistLongClick(this@AddToPlaylistActivity)
        }

        binding.playlistRecycler.apply {
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this@AddToPlaylistActivity, RecyclerView.VERTICAL, false)
            itemAnimator = DefaultItemAnimator()
            adapter = playlistAdapter
        }
    }

    private fun setUpArrayList() {
        viewModel.getAllPlaylists().observe(this, { strings ->
            list.clear()
            list.addAll(strings!!)
            playlistAdapter.notifyDataSetChanged()
        })
    }

    override fun onPlaylistClick(position: Int) {
        viewModel.addToPlaylist(this, list[position], song!!)
        finish()
    }

    override fun onPlaylistLongClick(position: Int) {}

    private fun showCreateListAlert() {
        val listener = AlertClickListener {
            viewModel.createNewPlaylist(it)
        }
        val alert = PlaylistAlert(this, listener)
        alert.showCreateListAlert()
    }
}