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
import android.widget.AdapterView.OnItemClickListener
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.ravisharma.playbackmusic.MainActivity.Companion.instance
import com.ravisharma.playbackmusic.R
import com.ravisharma.playbackmusic.activities.AddToPlaylistActivity
import com.ravisharma.playbackmusic.adapters.SongAdapter
import com.ravisharma.playbackmusic.databinding.FragmentNameWiseBinding
import com.ravisharma.playbackmusic.model.Song
import com.ravisharma.playbackmusic.provider.SongsProvider.Companion.songListByName
import com.ravisharma.playbackmusic.utils.*
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView
import java.io.File
import java.util.*

class NameWise : Fragment(), SongAdapter.OnItemClicked, SongAdapter.OnItemLongClicked {
    private var songList: ArrayList<Song> = ArrayList()
    var recyclerView: FastScrollRecyclerView? = null

    private lateinit var binding: FragmentNameWiseBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentNameWiseBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(v: View, savedInstanceState: Bundle?) {
        super.onViewCreated(v, savedInstanceState)
        val songAdapter = SongAdapter().apply {
            setOnClick(this@NameWise)
            setOnLongClick(this@NameWise)
        }

        binding.songList.apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            itemAnimator = DefaultItemAnimator()
            adapter = songAdapter
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }

        songListByName.observe(viewLifecycleOwner, { songs ->
            if (songs.size > 0) {
                songList.clear()
                songList.addAll(songs)
                songAdapter.setList(songList)
            }
        })
    }

    override fun onItemClick(position: Int) {
        val onFragmentItemClicked = activity as OnFragmentItemClicked?
        onFragmentItemClicked!!.onFragmentItemClick(position, songList, false)
    }

    override fun onItemLongClick(position: Int) {
        val items = resources.getStringArray(R.array.longPressItems)
        val ad = ArrayAdapter(requireContext(), R.layout.adapter_alert_list, items)
        val li = LayoutInflater.from(activity)
        val v = li.inflate(R.layout.alert_list, null)
        val lv = v.findViewById<ListView>(R.id.list)
        val tv = v.findViewById<TextView>(R.id.title)
        val songArt = v.findViewById<ImageView>(R.id.songArt)

        songArt.load(Uri.parse(songList[position].art)) {
            placeholder(R.drawable.logo)
            error(R.drawable.logo)
        }

        tv.text = songList[position].title
        lv.adapter = ad

        val dialog = AlertDialog.Builder(requireContext())
        dialog.setView(v)

        val alertDialog = dialog.create()
        alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.window!!.attributes.windowAnimations = R.style.DialogAnimation_2
        alertDialog.show()

        lv.onItemClickListener = OnItemClickListener { parent, view, itemPosition, id ->
            when (itemPosition) {
                0 -> {
                    val onFragmentItemClicked = activity as OnFragmentItemClicked?
                    onFragmentItemClicked!!.onFragmentItemClick(position, songList, false)
                }
                1 -> {
                    val singleSong = ArrayList<Song>()
                    singleSong.add(songList[position])
                    val itemClicked = activity as OnFragmentItemClicked?
                    itemClicked!!.onFragmentItemClick(0, singleSong, false)
                }
                2 -> {
                    addNextSongToPlayingList(songList[position])
                }
                3 -> {
                    addSongToPlayingList(songList[position])
                }
                4 -> {
                    val i = Intent(context, AddToPlaylistActivity::class.java)
                    i.putExtra("Song", songList[position])
                    startActivity(i)
                }
                5 -> {
                    // Delete Song Code
                    val b = AlertDialog.Builder(
                        requireContext(), R.style.AlertDialogCustom
                    )
                    b.setTitle(getString(R.string.deleteMessage))
                    b.setMessage(songList[position].title)
                    b.setPositiveButton(getString(R.string.yes),
                        DialogInterface.OnClickListener { dialog, which ->
                            if (songList.size == 1) {
                                Toast.makeText(
                                    context,
                                    "Can't Delete Last Song",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@OnClickListener
                            }
                            val musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                            val projection = arrayOf(MediaStore.Audio.Media._ID)
                            val selection = MediaStore.Audio.Media.DATA + " = ?"
                            val selectionArgs = arrayOf(
                                songList[position].data
                            )
                            val musicCursor = requireActivity().contentResolver.query(
                                musicUri, projection,
                                selection, selectionArgs, null
                            )
                            if (musicCursor!!.moveToFirst()) {
                                val id =
                                    musicCursor.getLong(musicCursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID))
                                val deleteUri: Uri = ContentUris.withAppendedId(
                                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                    id
                                )
                                try {
                                    val fdelete = File(selectionArgs[0])
                                    if (fdelete.exists()) {
                                        requireActivity().contentResolver.delete(deleteUri, null, null)
                                        updateList(position)
                                        Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT)
                                            .show()
                                    }
                                } catch (e: Exception) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                        val recoverableSecurityException: RecoverableSecurityException
                                        if (e is RecoverableSecurityException) {
                                            recoverableSecurityException = e
                                            val intentSender =
                                                recoverableSecurityException.userAction
                                                    .actionIntent.intentSender
                                            try {
                                                DELETE_URI = deleteUri
                                                startIntentSenderForResult(
                                                    intentSender, 20123,
                                                    null, 0, 0, 0, null
                                                )
                                            } catch (ex: SendIntentException) {
                                                ex.printStackTrace()
                                            }
                                        } else {
                                            Toast.makeText(
                                                context,
                                                "Can't Delete. Try Manually",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                }
                            } else {
                                Toast.makeText(
                                    context,
                                    "Can't Delete. Try Manually",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            musicCursor.close()
                            dialog.dismiss()
                        })
                        .setNegativeButton(getString(R.string.no)) { dialog, which -> dialog.dismiss() }
                    val d = b.create()
                    d.show()
                }
                6 -> {
                    val intent = Intent(Intent.ACTION_SEND)
                    intent.type = "audio/*"
                    val uri = Uri.parse(songList[position].data)
                    intent.putExtra(Intent.EXTRA_STREAM, uri)
                    requireActivity().startActivity(Intent.createChooser(intent, "Share Via"))
                }
                7 -> {
                    requireContext().showSongInfo(songList[position])
                }
            }
            alertDialog.dismiss()
        }
    }

    interface OnFragmentItemClicked {
        fun onFragmentItemClick(position: Int, songsArrayList: ArrayList<Song>, nowPlaying: Boolean)
    }

    private fun updateList(position: Int) {
        val song = songList[position]
        if (song == curPlayingSong.value) {
            instance!!.playNext()
        }
        removeFromPlayingList(song)
    }
}