package com.ravisharma.playbackmusic.activities

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import android.widget.TextView.OnEditorActionListener
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
import com.ravisharma.playbackmusic.activities.viewmodel.SearchViewModel
import com.ravisharma.playbackmusic.adapters.SongAdapter
import com.ravisharma.playbackmusic.adapters.SongAdapter.OnItemLongClicked
import com.ravisharma.playbackmusic.databinding.ActivitySearchBinding
import com.ravisharma.playbackmusic.model.Song
import com.ravisharma.playbackmusic.utils.DELETE_URI
import com.ravisharma.playbackmusic.utils.longclick.LongClickItems
import com.ravisharma.playbackmusic.utils.showToast
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import kotlin.collections.ArrayList

@AndroidEntryPoint
class SearchActivity : AppCompatActivity(), SongAdapter.OnItemClicked, OnItemLongClicked {
    private var adView: AdView? = null
    private var songList: ArrayList<Song> = ArrayList()
    private val viewModel: SearchViewModel by viewModels()
    private var mposition = 0

    private lateinit var binding: ActivitySearchBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(LayoutInflater.from(this))
        setTheme(R.style.SearchTheme)
        setContentView(binding.root)

        initRecyclerView()
        initListeners()

        binding.edSearch.requestFocus()
        val inputMethod = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethod.showSoftInput(binding.edSearch, 0)

        adView = AdView(this)
        adView!!.adUnitId = getString(R.string.searchActId)
        binding.bannerContainerSearch.addView(adView)
        loadBanner()
    }

    private fun loadBanner() {
        val adRequest = AdRequest.Builder().build()
        val adSize = AdSize.BANNER
        adView!!.setAdSize(adSize)
        adView!!.loadAd(adRequest)
    }

    private fun initRecyclerView() {
        binding.songList.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this@SearchActivity, RecyclerView.VERTICAL, false)
            itemAnimator = DefaultItemAnimator()
            addItemDecoration(DividerItemDecoration(this.context, DividerItemDecoration.VERTICAL))
            adapter = SongAdapter().apply {
                setOnClick(this@SearchActivity)
                setOnLongClick(this@SearchActivity)
            }
        }
        viewModel.getSearchList().observe(this) { songs ->
            if (songs.isNotEmpty()) {
                songList.clear()
                songList.addAll(songs)
                (binding.songList.adapter as SongAdapter).setList(songList)
            }
        }
    }

    private fun initListeners() {
        binding.edSearch.setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val name = binding.edSearch.text.toString().trim { it <= ' ' }
                if (name.isEmpty()) {
                    showToast(getString(R.string.notValid))
                    return@OnEditorActionListener false
                }
                performSearch(name)
                return@OnEditorActionListener true
            }
            false
        })
        binding.tvSearch.setOnClickListener(View.OnClickListener {
            val name = binding.edSearch.text.toString().trim { it <= ' ' }
            if (name.isEmpty()) {
                showToast(getString(R.string.notValid))
                return@OnClickListener
            }
            performSearch(name)
        })
        binding.imgBack.setOnClickListener { finish() }
    }

    private fun performSearch(name: String) {
        if (name.length < 3) {
            showToast(getString(R.string.notValidLength))
            return
        }
        binding.edSearch.clearFocus()
        val inputMethod = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethod.hideSoftInputFromWindow(binding.edSearch.windowToken, 0)
        viewModel.search(name, contentResolver)
    }

    override fun onItemClick(position: Int) {
        val i = Intent()
        i.putExtra("position", position)
        i.putExtra("songList", songList)
        setResult(RESULT_OK, i)
        finish()
    }

    override fun onItemLongClick(position: Int) {
        this.mposition = position
        LongClickItems(this, position, songList)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (DELETE_URI != null) {
                contentResolver.delete(DELETE_URI!!, null, null)
                songList.removeAt(mposition)
                (binding.songList.adapter as SongAdapter).setList(songList)
            }
        }
    }

    override fun onDestroy() {
        if (adView != null) {
            adView!!.destroy()
        }
        super.onDestroy()
    }

    fun onItemClick(list: ArrayList<Song>) {
        val i = Intent()
        i.putExtra("position", 0)
        i.putExtra("songList", list)
        setResult(RESULT_OK, i)
        finish()
    }
}