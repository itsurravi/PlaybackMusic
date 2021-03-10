package com.ravisharma.playbackmusic.fragments

import android.app.RecoverableSecurityException
import android.content.*
import android.content.IntentSender.SendIntentException
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
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
import com.ravisharma.playbackmusic.MainActivity
import com.ravisharma.playbackmusic.R
import com.ravisharma.playbackmusic.activities.AddToPlaylistActivity
import com.ravisharma.playbackmusic.adapters.SongAdapter
import com.ravisharma.playbackmusic.database.repository.PlaylistRepository
import com.ravisharma.playbackmusic.databinding.ActivityCategorySongBinding
import com.ravisharma.playbackmusic.databinding.AlertListBinding
import com.ravisharma.playbackmusic.databinding.PlaylistsLayoutBinding
import com.ravisharma.playbackmusic.fragments.viewmodels.CategorySongViewModel
import com.ravisharma.playbackmusic.model.Song
import com.ravisharma.playbackmusic.provider.SongsProvider
import com.ravisharma.playbackmusic.provider.SongsProvider.Companion.songListByName
import com.ravisharma.playbackmusic.utils.*
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

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
            fetchCategorySongs(it)
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

    private fun fetchCategorySongs(it: String) {
        viewModel.getCategorySongs(it, id!!, activity!!.contentResolver).observe(this, { songs ->
            songList.clear()
            songList.addAll(songs!!)
            setUpLayout()
        })
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
            context?.let { longClickItem(it, position, songList) }
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

    private fun longClickItem(context: Context, mPosition: Int, songList: ArrayList<Song>) {
        val items = context.resources.getStringArray(R.array.longPressItems)
        val ad = ArrayAdapter(context, R.layout.adapter_alert_list, items)
        val binding = AlertListBinding.inflate(LayoutInflater.from(context))

        val requestOptions = RequestOptions().apply {
            placeholder(R.drawable.logo)
            error(R.drawable.logo)
        }
        Glide.with(binding.root)
                .setDefaultRequestOptions(requestOptions)
                .load(songList[mPosition].art)
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(binding.songArt)
        binding.title.text = songList[mPosition].title
        binding.list.adapter = ad

        val dialog = AlertDialog.Builder(context)
        dialog.setView(binding.root)
        val alertDialog = dialog.create()

        alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.window!!.attributes.windowAnimations = R.style.DialogAnimation_2
        alertDialog.show()

        binding.list.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            when (position) {
                0 -> {
                    onItemClick(mPosition)
                }
                1 -> {
                    val list = arrayListOf(songList[mPosition])
                    val onFragmentItemClicked = activity as OnFragmentItemClicked?
                    onFragmentItemClicked!!.OnFragmentItemClick(0, list, false)
                }
                2 -> {
                    addNextSongToPlayingList(songList[mPosition])
                }
                3 -> {
                    addSongToPlayingList(songList[mPosition])
                }
                4 -> {
                    Intent(context, AddToPlaylistActivity::class.java).apply {
                        putExtra("Song", songList[mPosition])
                        context.startActivity(this)
                    }
                }
                5 -> {
                    showDeleteSongDialog(mPosition)
                }
                6 -> {
                    val intent = Intent(Intent.ACTION_SEND)
                    intent.type = "audio/*"
                    val uri = Uri.parse(songList[mPosition].data)
                    intent.putExtra(Intent.EXTRA_STREAM, uri)
                    context.startActivity(Intent.createChooser(intent, "Share Via"))
                }
                7 -> {
                    context.showSongInfo(songList[mPosition])
                }
            }
            alertDialog.dismiss()
        }
    }

    private val listener = object : DeleteListener {
        override fun onOkClicked() {
            category?.let { fetchCategorySongs(it) }
        }
    }

    private fun showDeleteSongDialog(mPosition: Int) {
        val song: Song = songList[mPosition]

        val b = AlertDialog.Builder(context!!, R.style.AlertDialogCustom)
        b.setTitle(getString(R.string.deleteMessage))
        b.setMessage(song.title)
        b.setPositiveButton(getString(R.string.yes), DialogInterface.OnClickListener { dialog, which ->
            if (songListByName.value!!.size == 1) {
                Toast.makeText(context, "Can't Delete Last Song", Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }
            val musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            val projection = arrayOf(MediaStore.Audio.Media._ID)
            val selection = MediaStore.Audio.Media.DATA + " = ?"
            val selectionArgs = arrayOf(song.data)
            val musicCursor = activity!!.contentResolver.query(musicUri, projection,
                    selection, selectionArgs, null)

            musicCursor?.let {
                if (it.moveToFirst()) {
                    val id = it.getLong(it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID))
                    val uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
                    try {
                        val fdelete = File(selectionArgs[0])
                        if (fdelete.exists()) {
                            activity!!.contentResolver.delete(uri, null, null)
                            updateList(mPosition)
                            Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show()
                            listener.onOkClicked()
                        }
                    } catch (e: Exception) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            val recoverableSecurityException: RecoverableSecurityException
                            if (e is RecoverableSecurityException) {
                                recoverableSecurityException = e as RecoverableSecurityException
                                val intentSender = recoverableSecurityException.userAction
                                        .actionIntent.intentSender
                                try {
                                    deleteUri = uri
                                    startIntentSenderForResult(intentSender, 20123,
                                            null, 0, 0, 0, null)
                                } catch (ex: SendIntentException) {
                                    ex.printStackTrace()
                                }
                            } else {
                                Toast.makeText(context, "Can't Delete. Try Manually", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else {
                    Toast.makeText(context, "Can't Delete. Try Manually", Toast.LENGTH_SHORT).show()
                }
                it.close()
            }
            dialog.dismiss()
        }).setNegativeButton(getString(R.string.no)) { dialog, which -> dialog.dismiss() }
        val d = b.create()
        d.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        listener.onOkClicked()
    }

    private fun updateList(mPosition: Int) {
        val song = songList[mPosition]
        if (song == curPlayingSong.value) {
            MainActivity.getInstance().playNext()
        }
        removeFromPlayingList(song)
    }

    interface DeleteListener {
        fun onOkClicked()
    }

    interface OnFragmentItemClicked {
        fun OnFragmentItemClick(position: Int, songsArrayList: ArrayList<Song>, nowPlaying: Boolean)
    }
}