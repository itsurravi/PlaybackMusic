package com.ravisharma.playbackmusic.new_work.ui.extensions

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.view.LayoutInflater
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import coil.load
import coil.transform.RoundedCornersTransformation
import com.ravisharma.playbackmusic.R
import com.ravisharma.playbackmusic.data.db.model.tables.Song
import com.ravisharma.playbackmusic.databinding.AlertListBinding
import com.ravisharma.playbackmusic.databinding.InfoBinding

sealed class LongItemClick(val title: String) {
    object Play : LongItemClick("Play")
    object SinglePlay : LongItemClick("Play This Only")

    //    object PlayNext : LongItemClick("Play Next")
    object AddToQueue : LongItemClick("Add To Queue")
    object AddToPlaylist : LongItemClick("Add To Playlist")

    //    object Delete : LongItemClick("Delete")
    object Share : LongItemClick("Share")
    object Details : LongItemClick("Details")
}

fun Context.onSongLongPress(song: Song, itemClick: (LongItemClick) -> Unit) {
    val items = listOf(
        LongItemClick.Play.title,
        LongItemClick.SinglePlay.title,
//        LongItemClick.PlayNext.title,
        LongItemClick.AddToQueue.title,
        LongItemClick.AddToPlaylist.title,
//        LongItemClick.Delete.title,
        LongItemClick.Share.title,
        LongItemClick.Details.title,
    )
    val ad = ArrayAdapter(this, R.layout.adapter_alert_list, items)

    val alertList = AlertListBinding.inflate(LayoutInflater.from(this))
    alertList.apply {
        songArt.load(Uri.parse(song.artUri)) {
            error(R.drawable.logo)
            crossfade(true)
            transformations(RoundedCornersTransformation(20f))
        }

        title.text = song.title
        list.adapter = ad
    }

    val dialog = AlertDialog.Builder(this)
    dialog.setView(alertList.root)

    val alertDialog = dialog.create()
    alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    alertDialog.window!!.attributes.windowAnimations = R.style.DialogAnimation_2
    alertDialog.show()

    alertList.list.onItemClickListener =
        AdapterView.OnItemClickListener { parent, view, itemPosition, id ->
            when (items[itemPosition]) {
                LongItemClick.Play.title -> itemClick(LongItemClick.Play)
                LongItemClick.SinglePlay.title -> itemClick(LongItemClick.SinglePlay)
//                LongItemClick.PlayNext.title -> itemClick(LongItemClick.PlayNext)
                LongItemClick.AddToQueue.title -> itemClick(LongItemClick.AddToQueue)
                LongItemClick.AddToPlaylist.title -> itemClick(LongItemClick.AddToPlaylist)
//                LongItemClick.Delete.title -> itemClick(LongItemClick.Delete)
                LongItemClick.Share.title -> itemClick(LongItemClick.Share)
                LongItemClick.Details.title -> itemClick(LongItemClick.Details)
            }
            alertDialog.dismiss()
        }
}

fun Context.shareSong(location: String) {
    val intent = Intent(Intent.ACTION_SEND)
    intent.type = "audio/*"
    val uri = Uri.parse(location)
    intent.putExtra(Intent.EXTRA_STREAM, uri)
    startActivity(Intent.createChooser(intent, "Share Via"))
}

fun Context.showSongInfo(song: Song) {
    val binding = InfoBinding.inflate(LayoutInflater.from(this))
    val builder = AlertDialog.Builder(this)
    builder.setView(binding.root)
    binding.infoArt.load(Uri.parse(song.artUri)) {
        error(R.drawable.logo)
        crossfade(true)
        transformations(RoundedCornersTransformation(20f))
    }
    binding.infoTitle.text = song.title
    binding.infoArtist.text = song.artist
    binding.infoAlbum.text = song.album
    binding.infoComposer.text = song.composer
    binding.infoDuration.text = song.durationFormatted
    binding.infoLocation.text = song.location

    val dialog = builder.create()
    dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    dialog.window!!.attributes.windowAnimations = R.style.DialogAnimation_2
    dialog.show()
}