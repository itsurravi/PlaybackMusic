package com.ravisharma.playbackmusic.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
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
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView
import java.util.*

class AddToPlaylistActivity : AppCompatActivity(), OnPlaylistClicked, OnPlaylistLongClicked {
    private var adView: AdView? = null
    private var song: Song? = null

    private lateinit var playlistAdapter: PlaylistAdapter
    private lateinit var list: ArrayList<String>
    private lateinit var viewModel: AddToPlaylistViewModel

    private lateinit var binding: ActivityAddToPlaylistBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddToPlaylistBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        song = intent.getParcelableExtra("Song")
        list = ArrayList()

        viewModel = ViewModelProvider(this).get(AddToPlaylistViewModel::class.java)

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
        playlistAdapter = PlaylistAdapter(this, list)
        playlistAdapter.setOnPlaylistClick(this)
        playlistAdapter.setOnPlaylistLongClick(this)

        binding.playlistRecycler.apply {
            addItemDecoration(DividerItemDecoration(this.context, DividerItemDecoration.VERTICAL))
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this@AddToPlaylistActivity, RecyclerView.VERTICAL, false)
            itemAnimator = DefaultItemAnimator()
            adapter = playlistAdapter
        }
    }

    private fun setUpArrayList() {
        viewModel.getAllPlaylists(this).observe(this, { strings ->
            list.clear()
            list.addAll(strings!!)
            binding.playlistRecycler.adapter?.notifyDataSetChanged()
        })
    }

    override fun onPlaylistClick(position: Int) {
        viewModel.addToPlaylist(this, list[position], song!!)
        finish()
    }

    override fun onPlaylistLongClick(position: Int) {}
    private fun showCreateListAlert() {
        val listener = AlertClickListener { setUpArrayList() }
        val alert = PlaylistAlert(this, listener)
        alert.showCreateListAlert()
    }
}