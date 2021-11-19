package com.ravisharma.playbackmusic.fragments

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.ravisharma.playbackmusic.MainActivity.Companion.instance
import com.ravisharma.playbackmusic.R
import com.ravisharma.playbackmusic.adapters.PlaylistAdapter
import com.ravisharma.playbackmusic.adapters.PlaylistAdapter.OnPlaylistClicked
import com.ravisharma.playbackmusic.adapters.PlaylistAdapter.OnPlaylistLongClicked
import com.ravisharma.playbackmusic.databinding.FragmentPlaylistBinding
import com.ravisharma.playbackmusic.fragments.viewmodels.PlaylistFragmentViewModel
import com.ravisharma.playbackmusic.model.Song
import com.ravisharma.playbackmusic.utils.alert.AlertClickListener
import com.ravisharma.playbackmusic.utils.alert.PlaylistAlert
import com.ravisharma.playbackmusic.utils.openFragment
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class PlaylistFragment : Fragment(), OnPlaylistClicked, OnPlaylistLongClicked,
    View.OnClickListener {

    private var playListArrayList: MutableList<String> = ArrayList()

    private lateinit var playlistAdapter: PlaylistAdapter
    private val viewModel: PlaylistFragmentViewModel by viewModels()
    private var adView: AdView? = null

    private lateinit var binding: FragmentPlaylistBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPlaylistBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(v: View, savedInstanceState: Bundle?) {
        super.onViewCreated(v, savedInstanceState)

        initRecyclerView()

        loadBanner2()

        binding.apply {
            cardRecentAdded.setOnClickListener(this@PlaylistFragment)
            cardLastPlayed.setOnClickListener(this@PlaylistFragment)
            cardMostPlayed.setOnClickListener(this@PlaylistFragment)
            btnAddNewPlaylist.setOnClickListener(this@PlaylistFragment)
        }
    }

    private fun loadBanner2() {
        adView = AdView(requireContext())
        adView!!.adUnitId = getString(R.string.playlistFragId)
        binding.bannerContainerPlaylist.addView(adView)

        val adRequest = AdRequest.Builder().build()
        val adSize = AdSize.BANNER
        adView!!.adSize = adSize
        adView!!.loadAd(adRequest)
    }

    private fun initRecyclerView() {
        playlistAdapter = PlaylistAdapter(requireActivity(), playListArrayList).apply {
            setOnPlaylistClick(this@PlaylistFragment)
            setOnPlaylistLongClick(this@PlaylistFragment)
        }

        binding.playlistRecycler.apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            itemAnimator = DefaultItemAnimator()
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
            adapter = playlistAdapter
        }
    }

    private fun setUpArrayList() {
        viewModel.getAllPlaylists(requireContext()).observe(viewLifecycleOwner, { strings ->
            playListArrayList.clear()
            playListArrayList.add(getString(R.string.favTracks))
            playListArrayList.addAll(strings!!)
            playlistAdapter.notifyDataSetChanged()
        })
    }

    override fun onPlaylistClick(position: Int) {
        val bundle = Bundle()
        bundle.putString("argType", PLAYLIST)
        bundle.putString("actName", playListArrayList[position])
        openFragment(bundle)
    }

    private fun openFragment(bundle: Bundle) {
        val fragment = CategorySongFragment()
        fragment.arguments = bundle
        requireActivity().openFragment(fragment)
    }

    override fun onClick(view: View) {
        if (binding.btnAddNewPlaylist == view) {
            showCreateUpdatePlaylistDialog(true, null)
            return
        }
        val bundle = Bundle()
        when(view) {
            binding.cardRecentAdded -> {
                bundle.putString("argType", "Recent Added")
                bundle.putString("actName", "Recent Added")
            }
            binding.cardLastPlayed -> {
                bundle.putString("argType", "Last Played")
                bundle.putString("actName", "Last Played")
            }
            binding.cardMostPlayed -> {
                bundle.putString("argType", "Most Played")
                bundle.putString("actName", "Most Played")
            }
        }
        openFragment(bundle)
    }

    override fun onPlaylistLongClick(position: Int) {
        if (position == 0) {
            return
        }
        val items = resources.getStringArray(R.array.longPressItemsPlaylist)
        val ad = ArrayAdapter(requireContext(), R.layout.adapter_alert_list, items)
        val v = LayoutInflater.from(context).inflate(R.layout.alert_playlist, null)
        val lv = v.findViewById<ListView>(R.id.list)
        val tv = v.findViewById<TextView>(R.id.title)
        tv.text = playListArrayList[position]
        lv.adapter = ad
        val dialog = AlertDialog.Builder(
            requireContext()
        )
        dialog.setView(v)
        val alertDialog = dialog.create()
        alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.window!!.attributes.windowAnimations = R.style.DialogAnimation_2
        alertDialog.show()
        lv.onItemClickListener = OnItemClickListener { parent, view, i, id ->
            when (i) {
                0 -> {
                    val list = viewModel.getPlaylist(playListArrayList[position])
                    if (list != null && list.isNotEmpty()) {
                        val songList = ArrayList<Song>()
                        for ((_, _, song) in list) {
                            songList.add(song)
                        }
                        if (songList.size > 0) {
                            instance!!.onFragmentItemClick(0, songList, false)
                        }
                    } else {
                        Toast.makeText(context, "Playlist is Empty", Toast.LENGTH_SHORT).show()
                    }
                }
                1 -> showCreateUpdatePlaylistDialog(false, playListArrayList[position])
                2 -> {
                    viewModel.removePlaylist(playListArrayList[position])
                    setUpArrayList()
                }
            }
            alertDialog.dismiss()
        }
    }

    private fun showCreateUpdatePlaylistDialog(createList: Boolean, oldPlaylistName: String?) {
        val listener = AlertClickListener { newPlaylistName ->
            if (createList) {
                viewModel.createNewPlaylist(newPlaylistName)
            } else {
                viewModel.renamePlaylist(oldPlaylistName!!, newPlaylistName)
            }
            setUpArrayList()
        }
        val alert = PlaylistAlert(context, listener)
        if (createList) {
            alert.showCreateListAlert()
        } else {
            alert.showUpdateListAlert(oldPlaylistName)
        }
    }

    override fun onResume() {
        super.onResume()
        setUpArrayList()
    }

    override fun onDestroy() {
        if (adView != null) {
            adView!!.destroy()
        }
        super.onDestroy()
    }
}