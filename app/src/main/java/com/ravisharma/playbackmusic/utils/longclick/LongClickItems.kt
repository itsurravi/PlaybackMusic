package com.ravisharma.playbackmusic.utils.longclick

import android.app.Activity
import android.app.RecoverableSecurityException
import android.content.*
import android.content.IntentSender.SendIntentException
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidx.appcompat.app.AlertDialog
import coil.load
import com.ravisharma.playbackmusic.R
import com.ravisharma.playbackmusic.activities.*
import com.ravisharma.playbackmusic.databinding.AlertListBinding
import com.ravisharma.playbackmusic.model.Song
import com.ravisharma.playbackmusic.provider.SongsProvider.Companion.songListByName
import com.ravisharma.playbackmusic.utils.*
import java.io.File
import java.util.*

class LongClickItems {
    private val context: Context
    private val songList: ArrayList<Song>
    private var position = -1

    lateinit var binding: AlertListBinding
    private var items: Array<String>

    constructor(context: Context, position: Int, songList: ArrayList<Song>) {
        this.context = context
        this.songList = songList
        this.position = position

        items = context.resources.getStringArray(R.array.longPressItems)
        setDialog()
        showDialog(position)
    }

    constructor(context: Context, position: Int, songList: ArrayList<Song>, type: String?) {
        this.context = context
        this.songList = songList
        this.position = position

        items = context.resources.getStringArray(R.array.longPressNowPlaying)
        setDialog()
        showNowPlayingDialog(position)
    }

    private fun setDialog(){
        val ad = ArrayAdapter(context, R.layout.adapter_alert_list, items)
        binding = AlertListBinding.inflate(LayoutInflater.from(context))

        binding.songArt.load(Uri.parse(songList[position].art)) {
            placeholder(R.drawable.logo)
            error(R.drawable.logo)
            crossfade(true)
        }

        binding.title.text = songList[position].title
        binding.list.adapter = ad
    }

    private fun showDialog(mPosition: Int) {
        val dialog = AlertDialog.Builder(context)
        dialog.setView(binding.root)
        val alertDialog = dialog.create()
        alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.window!!.attributes.windowAnimations = R.style.DialogAnimation_2
        alertDialog.show()

        binding.list.onItemClickListener = OnItemClickListener { parent, view, position, id ->
            when (position) {
                0 -> {
                    itemClick(mPosition)
                }
                1 -> {
                    playSingleOnly(songList[mPosition])
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
                    showDeleteSongDialog(songList[mPosition])
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

    private fun showNowPlayingDialog(mPosition: Int) {
        val dialog = AlertDialog.Builder(context)
        dialog.setView(binding.root)
        val alertDialog = dialog.create()
        alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.window!!.attributes.windowAnimations = R.style.DialogAnimation_2
        alertDialog.show()
        binding.list.onItemClickListener = OnItemClickListener { parent, view, position, id ->
            when (position) {
                0 -> itemClick(mPosition)
                1 -> {
                    val i = Intent(context, AddToPlaylistActivity::class.java)
                    i.putExtra("Song", songList[mPosition])
                    context.startActivity(i)
                }
                2 -> {
                    val intent = Intent(Intent.ACTION_SEND)
                    intent.type = "audio/*"
                    val uri = Uri.parse(songList[mPosition].data)
                    intent.putExtra(Intent.EXTRA_STREAM, uri)
                    context.startActivity(Intent.createChooser(intent, "Share Via"))
                }
                3 -> context.showSongInfo(songList[mPosition])
            }
            alertDialog.dismiss()
        }
    }

    private fun showDeleteSongDialog(song: Song) {
        val b = AlertDialog.Builder(context, R.style.AlertDialogCustom)
        b.setTitle(context.getString(R.string.deleteMessage))
        b.setMessage(song.title)
        b.setPositiveButton(context.getString(R.string.yes), DialogInterface.OnClickListener { dialog, which ->
            if (songListByName.value!!.size == 1) {
                context.showToast("Can't Delete Last Song")
                return@OnClickListener
            }
            val musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            val projection = arrayOf(MediaStore.Audio.Media._ID)
            val selection = MediaStore.Audio.Media.DATA + " = ?"
            val selectionArgs = arrayOf(song.data)
            val musicCursor = context.contentResolver.query(musicUri, projection,
                    selection, selectionArgs, null)
            if (musicCursor!!.moveToFirst()) {
                val id = musicCursor.getLong(musicCursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID))
                val uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
                try {
                    val fdelete = File(selectionArgs[0])
                    Log.d("ERRORDELETION", "" + uri)
                    if (fdelete.exists()) {
                        context.contentResolver.delete(uri, null, null)
                        if (position != 1) {
                            updateList(position)
                            position = -1
                        }
                        context.showToast("Deleted")
                    }
                } catch (e: Exception) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        val recoverableSecurityException: RecoverableSecurityException
                        if (e is RecoverableSecurityException) {
                            recoverableSecurityException = e
                            val intentSender = recoverableSecurityException.userAction
                                    .actionIntent.intentSender
                            try {
                                deleteUri = uri
                                (context as Activity).startIntentSenderForResult(intentSender, 20123,
                                        null, 0, 0, 0, null)
                            } catch (ex: SendIntentException) {
                                ex.printStackTrace()
                            }
                        } else {
                            context.showToast("Can't Delete. Try Manually")
                        }
                    }
                    Log.d("ExceptionProblem", e.toString())
                }
            } else {
                context.showToast("Can't Delete. Try Manually")
            }
            musicCursor.close()
            dialog.dismiss()
        }).setNegativeButton(context.getString(R.string.no)) { dialog, which -> dialog.dismiss() }
        val d = b.create()
        d.show()
    }

    private fun itemClick(position: Int) {
        when (context) {
            is NowPlayingActivity -> {
                context.onItemClick(position)
            }
            is SearchActivity -> {
                context.onItemClick(position)
            }
        }
    }

    private fun playSingleOnly(song: Song) {
        val list = arrayListOf(song)
        when (context) {
            is SearchActivity -> {
                context.onItemClick(list)
            }
        }
    }

    private fun updateList(mposition: Int) {
        if (context is NowPlayingActivity) {
            context.updateList(mposition)
        } else if (context is SearchActivity) {
//            ((SearchActivity) context).updateList(mposition);
        }
    }
}