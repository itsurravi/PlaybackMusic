package com.ravisharma.playbackmusic.fragments

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.*
import coil.load
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.material.snackbar.Snackbar
import com.ravisharma.playbackmusic.MainActivity.Companion.instance
import com.ravisharma.playbackmusic.MainActivityViewModel
import com.ravisharma.playbackmusic.R
import com.ravisharma.playbackmusic.activities.AddToPlaylistActivity
import com.ravisharma.playbackmusic.adapters.NowPlayingAdapter
import com.ravisharma.playbackmusic.databinding.AlertListBinding
import com.ravisharma.playbackmusic.databinding.FragmentNowPlayingBinding
import com.ravisharma.playbackmusic.model.Playlist
import com.ravisharma.playbackmusic.model.Song
import com.ravisharma.playbackmusic.prefrences.PrefManager
import com.ravisharma.playbackmusic.provider.SongsProvider.Companion.songListByName
import com.ravisharma.playbackmusic.utils.*
import com.ravisharma.playbackmusic.utils.alert.AlertClickListener
import com.ravisharma.playbackmusic.utils.alert.PlaylistAlert
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.collections.ArrayList

@AndroidEntryPoint
class NowPlayingFragment : Fragment(), NowPlayingAdapter.OnItemClicked,
    StartDragListener {

    private lateinit var binding: FragmentNowPlayingBinding

    private lateinit var nowPlayingAdapter: NowPlayingAdapter
    private lateinit var itemTouchHelper: ItemTouchHelper

    private var playingList: ArrayList<Song> = ArrayList()
    private lateinit var playingSong: Song

    private var adView: AdView? = null

    private var curpos = -1

    private val viewModel: MainActivityViewModel by viewModels()

    @Inject
    lateinit var manage: PrefManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNowPlayingBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        curpos = curPlayingSongPosition.value!!

        initRecyclerView()

        setObservers()

        binding.imgBack.setOnClickListener {
            activity?.onBackPressed()
        }

        binding.imgSave.setOnClickListener {
            showCreateListAlert()
        }
        loadBanner()
    }

    private fun loadBanner() {
        val adView = AdView(requireContext())
        adView.adUnitId = getString(R.string.nowPlayingActId)
        binding.bannerContainerNowPlaying.addView(adView)

        val adRequest = AdRequest.Builder().build()
        val adSize = AdSize.BANNER
        adView.adSize = adSize
        adView.loadAd(adRequest)
    }

    private fun setObservers() {
        viewModel.getPlayingList().observe(viewLifecycleOwner) { songs ->
            playingList = songs
            nowPlayingAdapter.setList(playingList)
            if (swiped || fileDelete) {
                Log.d("Playing", "Swiped")
                if (playingList.size > 0) {
                    setPlayingList(playingList)
                    val position = playingList.indexOf(playingSong)
                    setSongPosition(position)
                } else {
                    if (songListByName.value!!.size == 0) {
                        Toast.makeText(
                            requireContext(),
                            "No Song Left in Storage",
                            Toast.LENGTH_SHORT
                        ).show()
                        lifecycleScope.launch {
                            manage.putBooleanPref(requireContext().getString(R.string.Songs), false)
                        }
                        Handler(Looper.getMainLooper()).postDelayed({ System.exit(0) }, 1000)
                    } else {
                        setPlayingList(songListByName.value!!)
                        instance!!.musicSrv!!.setList(songListByName.value!!)
                        instance!!.musicSrv!!.setSong(0)
                        instance!!.musicSrv!!.playSong()
                    }
                }
                swiped = false
                fileDelete = false
            }
            if (moved) {
                setPlayingList(playingList)
                val position = playingList.indexOf(playingSong)
                setSongPosition(position)
                moved = false
            }
        }

        curPlayingSong.observe(viewLifecycleOwner) { song ->
            Log.d("Playing", "Chnaged")
            playingSong = song
            binding.songTitle.text = playingSong.title
            binding.songArtist.text = playingSong.artist
            binding.songDuration.text = String.format(
                "%d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(playingSong.duration),
                TimeUnit.MILLISECONDS.toSeconds(playingSong.duration) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(playingSong.duration))
            )

            /*Song art code here*/
            binding.songArt.load(Uri.parse(playingSong.art)) {
                error(R.drawable.logo)
                crossfade(true)
            }
        }
    }

    private fun initRecyclerView() {
        nowPlayingAdapter = NowPlayingAdapter(this).apply {
            setOnClick(this@NowPlayingFragment)
        }
        binding.songList.apply {
            adapter = nowPlayingAdapter
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            itemAnimator = DefaultItemAnimator()
            addItemDecoration(
                DividerItemDecoration(
                    requireContext(),
                    DividerItemDecoration.VERTICAL
                )
            )
        }
        binding.songList.scrollToPosition(curpos)
        itemTouchHelper = ItemTouchHelper(simpleCallback)
        itemTouchHelper.attachToRecyclerView(binding.songList)
    }

    private fun showCreateListAlert() {
        val listener =
            AlertClickListener { playlistName ->
                viewModel.createNewPlaylist(playlistName)
                addToPlaylist(playlistName)
                Snackbar.make(requireView(), "Playlist Saved", Snackbar.LENGTH_SHORT).show()
            }
        val alert = PlaylistAlert(requireContext(), listener)
        alert.showCreateListAlert()
    }

    private fun addToPlaylist(playListName: String) {
        val list = playingList
        for (s in list) {
            val p = Playlist(0, playListName, s)
            viewModel.addSong(p)
        }
    }

    override fun requestDrag(viewHolder: RecyclerView.ViewHolder?) {
        itemTouchHelper.startDrag(viewHolder!!)
    }

    private var simpleCallback: ItemTouchHelper.SimpleCallback =
        object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or
                    ItemTouchHelper.DOWN, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val fromPosition = viewHolder.adapterPosition
                val toPosition = target.adapterPosition
                Collections.swap(playingList, fromPosition, toPosition)
                moved = true
                nowPlayingAdapter.setList(playingList)
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

            }

            override fun clearView(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ) {
                super.clearView(recyclerView, viewHolder)
                if (moved) {
                    setPlayingList(playingList)
                }
            }

            override fun isLongPressDragEnabled(): Boolean {
                return false
            }

            override fun getSwipeDirs(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ): Int {
                return 0
            }
        }

    override fun onItemClick(position: Int) {
        val onFragmentItemClicked = activity as OnFragmentItemClicked?
        onFragmentItemClicked!!.onFragmentItemClick(position, playingList, true)
    }

    override fun onOptionsClick(mPosition: Int) {
        val ad = ArrayAdapter(
            requireContext(),
            R.layout.adapter_alert_list,
            resources.getStringArray(R.array.longPressNowPlaying)
        )
        val binding = AlertListBinding.inflate(LayoutInflater.from(context))

        binding.songArt.load(Uri.parse(playingList[mPosition].art)) {
            placeholder(R.drawable.logo)
            error(R.drawable.logo)
            crossfade(true)
        }

        binding.title.text = playingList[mPosition].title
        binding.list.adapter = ad

        val dialog = AlertDialog.Builder(requireContext())
        dialog.setView(binding.root)
        val alertDialog = dialog.create()
        alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.window!!.attributes.windowAnimations = R.style.DialogAnimation_2
        alertDialog.show()

        binding.list.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                when (position) {
                    0 -> onItemClick(mPosition)
                    1 -> {
                        val i = Intent(context, AddToPlaylistActivity::class.java)
                        i.putExtra("Song", playingList[mPosition])
                        requireActivity().startActivity(i)
                    }
                    2 -> {
                        val intent = Intent(Intent.ACTION_SEND)
                        intent.type = "audio/*"
                        val uri = Uri.parse(playingList[mPosition].data)
                        intent.putExtra(Intent.EXTRA_STREAM, uri)
                        requireActivity().startActivity(Intent.createChooser(intent, "Share Via"))
                    }
                    3 -> requireContext().showSongInfo(playingList[mPosition])
                }
                alertDialog.dismiss()
            }
    }

    override fun onItemRemove(position: Int) {
        if (playingList[position] == playingSong) {
            if (playingList.size > 0) {
                instance!!.playNext()
            }
        }
        playingList.removeAt(position)
        nowPlayingAdapter.setList(playingList)
        swiped = true
        setPlayingList(playingList)
    }

    override fun onDestroy() {
        if (adView != null) {
            adView?.destroy()
        }
        super.onDestroy()
    }

    interface OnFragmentItemClicked {
        fun onFragmentItemClick(
            position: Int,
            songsArrayList: java.util.ArrayList<Song>,
            nowPlaying: Boolean
        )
    }
}