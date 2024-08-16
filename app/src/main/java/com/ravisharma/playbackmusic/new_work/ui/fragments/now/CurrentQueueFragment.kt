package com.ravisharma.playbackmusic.new_work.ui.fragments.now

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.RoundedCornersTransformation
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.ravisharma.playbackmusic.R
import com.ravisharma.playbackmusic.data.db.model.tables.Song
import com.ravisharma.playbackmusic.databinding.FragmentCurrentQueueBinding
import com.ravisharma.playbackmusic.new_work.ui.adapters.CurrentQueueAdapter
import com.ravisharma.playbackmusic.new_work.utils.NavigationConstant
import com.ravisharma.playbackmusic.new_work.viewmodel.MainViewModel
import com.ravisharma.playbackmusic.utils.StartDragListener
import com.ravisharma.playbackmusic.new_work.ui.extensions.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Collections

@AndroidEntryPoint
class CurrentQueueFragment : Fragment(R.layout.fragment_current_queue), StartDragListener {

    private var _binding: FragmentCurrentQueueBinding? = null
    private val binding get() = _binding!!

    private val mainViewModel: MainViewModel by activityViewModels()

    private var adView: AdView? = null
    private var adUnitId: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentCurrentQueueBinding.bind(view)

        setupFragment()
    }

    private fun setupFragment() {
        adView = AdView(requireContext())
        initViews()
        initObservers()

        adUnitId = getString(R.string.searchActId)
        loadBanner()
    }

    private fun loadBanner() {
        adUnitId?.let { unitId ->
            val adRequest = AdRequest.Builder().build()
            val adSize = AdSize.BANNER
            adView!!.adUnitId = unitId
            adView!!.setAdSize(adSize)

            binding.bannerAd.addView(adView)

            adView!!.loadAd(adRequest)
        }
    }

    private fun initViews() {
        binding.apply {
            val currentQueueAdapter = CurrentQueueAdapter(
                dragListener = this@CurrentQueueFragment,
                onItemClick = ::songClicked,
                onItemRemoveClick = ::removeSongFromQueue
            )

            rvSongList.apply {
                adapter = currentQueueAdapter
                layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
                itemAnimator = DefaultItemAnimator()
                addItemDecoration(
                    DividerItemDecoration(
                        requireContext(),
                        DividerItemDecoration.VERTICAL
                    )
                )
            }.also {
                currentQueueAdapter.submitList(mainViewModel.queue) {
                    it.scrollToPosition(mainViewModel.queue.indexOf(mainViewModel.currentSong.value))
                }
            }
            itemTouchHelper.attachToRecyclerView(rvSongList)
        }
        initClickListeners()
    }

    private fun removeSongFromQueue(song: Song, position: Int) {
        val adapter = (binding.rvSongList.adapter as CurrentQueueAdapter)
        val adapterList = adapter.getCurrentList()
        if(adapterList.size == 1) {
            requireContext().showToast("Last song left in queue")
            return
        }
        val list = adapterList.map {
            it.copy()
        }.toMutableList()
        list.removeAt(position)
        adapter.submitList(list)
        mainViewModel.onSongRemoveFromQueue(position)
    }

    private fun initClickListeners() {
        binding.apply {
            ivBack.setOnClickListener {
                findNavController().popBackStack()
            }
            ivSave.setOnClickListener {
                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Default) {
                    val songLocations = arrayListOf<String>()
                    mainViewModel.queue.forEach {
                        songLocations.add(it.location)
                    }
                    withContext(Dispatchers.Main) {
                        val bundle = Bundle().apply {
                            putStringArrayList(NavigationConstant.AddToPlaylistSongs, songLocations)
                        }
                        findNavController().navigate(R.id.action_to_addToPlaylistFragment, bundle)
                    }
                }
            }
        }
    }

    private fun initObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            launch {
                mainViewModel.currentSong.collect {
                    updateCurrentSongInfo(it)
                }
            }
        }
    }

    private fun updateCurrentSongInfo(currentSong: Song?) {
        binding.apply {
            currentSong?.let {
                songTitle.text = it.title
                songArtist.text = it.artist
                songDuration.text = it.durationFormatted

                songArt.load(Uri.parse(it.artUri)) {
                    placeholder(R.drawable.logo)
                    error(R.drawable.logo)
                    transformations(RoundedCornersTransformation(10f))
                    crossfade(true)
                }
            }
        }
    }

    private fun songClicked(song: Song, position: Int) {
        mainViewModel.playerHelper.seekTo(position, 0)
    }

    override fun requestDrag(viewHolder: RecyclerView.ViewHolder) {
        itemTouchHelper.startDrag(viewHolder)
    }

    private val itemTouchHelper by lazy {
        val simpleCallback: ItemTouchHelper.SimpleCallback =
            object : ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP or
                        ItemTouchHelper.DOWN, 0
            ) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    val adapter = (recyclerView.adapter as CurrentQueueAdapter)
                    val fromPosition = viewHolder.bindingAdapterPosition
                    val toPosition = target.bindingAdapterPosition
                    mainViewModel.onSongDrag(fromPosition, toPosition)
                    val list = adapter.getCurrentList().toMutableList()
                    Collections.swap(list, fromPosition, toPosition)
                    adapter.submitList(list)

                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

                }
            }
        ItemTouchHelper(simpleCallback)
    }

}