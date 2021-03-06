package com.ravisharma.playbackmusic.activities

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
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
import com.ravisharma.playbackmusic.adapters.SongAdapter
import com.ravisharma.playbackmusic.adapters.SongAdapter.OnItemLongClicked
import com.ravisharma.playbackmusic.database.repository.PlaylistRepository
import com.ravisharma.playbackmusic.databinding.ActivityFavoritePlaylistBinding
import com.ravisharma.playbackmusic.model.Song
import com.ravisharma.playbackmusic.utils.addNextSongToPlayingList
import com.ravisharma.playbackmusic.utils.addSongToPlayingList
import com.ravisharma.playbackmusic.utils.showSongInfo
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView
import java.util.*
import kotlin.collections.ArrayList

class PlaylistActivity : AppCompatActivity(), SongAdapter.OnItemClicked, OnItemLongClicked {
    private var adView: AdView? = null
    private var songList: ArrayList<Song> = ArrayList()

    private var playlistName: String? = null
    private var repository: PlaylistRepository? = null

    private lateinit var binding: ActivityFavoritePlaylistBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // In Activity's onCreate() for instance
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false)
        window.statusBarColor = Color.TRANSPARENT

        binding = ActivityFavoritePlaylistBinding.inflate(LayoutInflater.from(this))

        setContentView(binding.root)
        songList = ArrayList()
        repository = PlaylistRepository(this)

        playlistName = intent.getStringExtra("playlistName")
        val txtPlaylistName1 = findViewById<TextView>(R.id.txtPlaylistName1)
        val txtPlaylistName2 = findViewById<TextView>(R.id.txtPlaylistName2)
        txtPlaylistName1.text = playlistName
        txtPlaylistName2.text = playlistName
        initRecyclerView()

        repository!!.getPlaylistSong(playlistName!!).observe(this, { playlists ->
            songList.clear()
            for ((_, _, song) in playlists) {
                songList.add(song)
            }
            if (playlistName == getString(R.string.favTracks) && songList.size > 0) {
                songList.reverse()
            }
            (binding.playlistLayout.songList.adapter as SongAdapter).setList(songList)
            setUpLayout()
        })

        adView = AdView(this)
        adView!!.adUnitId = getString(R.string.playlistActId)
        binding.bannerContainerFav.addView(adView)
        loadBanner()
    }

    private fun loadBanner() {
        val adRequest = AdRequest.Builder().build()
        val adSize = AdSize.BANNER
        adView!!.adSize = adSize
        adView!!.loadAd(adRequest)
    }

    private fun initRecyclerView() {
        binding.playlistLayout.songList.apply {
            setHasFixedSize(true)
            adapter = SongAdapter(this@PlaylistActivity).apply {
                setOnClick(this@PlaylistActivity)
                setOnLongClick(this@PlaylistActivity)
            }
            layoutManager = LinearLayoutManager(this@PlaylistActivity, RecyclerView.VERTICAL, false)
            itemAnimator = DefaultItemAnimator()
            addItemDecoration(DividerItemDecoration(this.context, DividerItemDecoration.VERTICAL))
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

    private fun setUpLayout() {
        if (songList.size == 0) {
            binding.playlistLayout.noDataFound.noDataLayout.visibility = View.VISIBLE
            binding.playlistLayout.secondLayout.visibility = View.VISIBLE
            binding.playlistLayout.firstLayout.visibility = View.GONE
        } else {
            binding.playlistLayout.noDataFound.noDataLayout.visibility = View.GONE
            binding.playlistLayout.secondLayout.visibility = View.GONE
            binding.playlistLayout.firstLayout.visibility = View.VISIBLE

            val size = songList.size
            val noOfSongs = resources.getQuantityString(R.plurals.numberOfSongs, size, size)
            binding.playlistLayout.noOfSongs.text = noOfSongs

            val requestOptions = RequestOptions().apply {
                placeholder(R.drawable.logo)
                error(R.drawable.logo)
            }
            Glide.with(this)
                    .setDefaultRequestOptions(requestOptions)
                    .load(Uri.parse(songList[0].art))
                    .diskCacheStrategy(DiskCacheStrategy.DATA)
                    .into(binding.playlistLayout.albumArt)
        }
    }

    override fun onItemClick(position: Int) {
        val i = Intent()
        i.putExtra("position", position)
        i.putExtra("songList", songList)
        setResult(RESULT_OK, i)
        finish()
    }

    override fun onItemLongClick(mposition: Int) {
        val items = resources.getStringArray(R.array.longPressItemsRemove)
        val ad = ArrayAdapter(this, R.layout.adapter_alert_list, items)
        val v = LayoutInflater.from(this).inflate(R.layout.alert_list, null)
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
        val dialog = AlertDialog.Builder(this)
        dialog.setView(v)
        val alertDialog = dialog.create()
        alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.window!!.attributes.windowAnimations = R.style.DialogAnimation_2
        alertDialog.show()
        lv.onItemClickListener = OnItemClickListener { parent, view, position, id ->
            when (position) {
                0 -> onItemClick(mposition)
                1 -> addNextSongToPlayingList(songList[mposition])
                2 -> addSongToPlayingList(songList[mposition])
                3 ->                         // Delete Song Code
                    repository!!.removeSong(playlistName, songList[mposition].id)
                4 -> {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "audio/*"
                        putExtra(Intent.EXTRA_STREAM, Uri.parse(songList[mposition].data))
                    }
                    startActivity(Intent.createChooser(intent, "Share Via"))
                }
                5 -> this@PlaylistActivity.showSongInfo(songList[mposition])
            }
            alertDialog.dismiss()
        }
    }

    override fun onDestroy() {
        if (adView != null) {
            adView!!.destroy()
        }
        super.onDestroy()
    }
}