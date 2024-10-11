package com.ravisharma.playbackmusic.new_work.ui.extensions

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import coil.load
import coil.transform.RoundedCornersTransformation
import com.ravisharma.playbackmusic.R
import com.ravisharma.playbackmusic.data.db.model.tables.Song
import com.ravisharma.playbackmusic.databinding.InfoBinding
import com.ravisharma.playbackmusic.databinding.LayoutGenericBottomSheetBinding
import com.ravisharma.playbackmusic.new_work.ui.bottomsheet.ActionBottomSheet

sealed class LongItemClick(val title: String) {
    data object Play : LongItemClick("Play")
    data object SinglePlay : LongItemClick("Play This Only")

    //    object PlayNext : LongItemClick("Play Next")
    data object AddToQueue : LongItemClick("Add To Queue")
    data object AddToPlaylist : LongItemClick("Add To Playlist")
    data object RemoveFromList : LongItemClick("Remove From List")

    //    object Delete : LongItemClick("Delete")
    data object Share : LongItemClick("Share")
    data object Details : LongItemClick("Details")
}

fun Context.showToast(msg: String) {
    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}

fun FragmentActivity.onPlaylistSongLongPress(song: Song, itemClick: (LongItemClick) -> Unit) {
    val items = listOf(
        LongItemClick.Play.title,
        LongItemClick.SinglePlay.title,
        LongItemClick.AddToQueue.title,
        LongItemClick.RemoveFromList.title,
        LongItemClick.Share.title,
        LongItemClick.Details.title
    )
    val ad = ArrayAdapter(this, R.layout.adapter_alert_list, items)
    val bottomSheet = ActionBottomSheet(
        layoutResId = R.layout.layout_generic_bottom_sheet,
        onViewCreatedCallback = { view, bundle, sheetView ->
            val binding = LayoutGenericBottomSheetBinding.bind(view)
            binding.apply {
                ivArt.load(Uri.parse(song.artUri)) {
                    error(R.drawable.logo)
                    crossfade(true)
                    transformations(RoundedCornersTransformation(20f))
                }
                tvTitle.text = song.title
                tvSubtitle.text = song.artist
                listView.adapter = ad
            }
            binding.listView.onItemClickListener =
                AdapterView.OnItemClickListener { _, _, itemPosition, _ ->
                    when (items[itemPosition]) {
                        LongItemClick.Play.title -> itemClick(LongItemClick.Play)
                        LongItemClick.SinglePlay.title -> itemClick(LongItemClick.SinglePlay)
                        LongItemClick.AddToQueue.title -> itemClick(LongItemClick.AddToQueue)
                        LongItemClick.RemoveFromList.title -> itemClick(LongItemClick.RemoveFromList)
                        LongItemClick.Share.title -> itemClick(LongItemClick.Share)
                        LongItemClick.Details.title -> itemClick(LongItemClick.Details)
                    }
                    sheetView.dismiss()
                }
        }
    )
    bottomSheet.show(this.supportFragmentManager, "onPlaylistSongLongPress")

    /*val alertList = AlertListBinding.inflate(LayoutInflater.from(this))
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
                LongItemClick.AddToQueue.title -> itemClick(LongItemClick.AddToQueue)
                LongItemClick.RemoveFromList.title -> itemClick(LongItemClick.RemoveFromList)
                LongItemClick.Share.title -> itemClick(LongItemClick.Share)
                LongItemClick.Details.title -> itemClick(LongItemClick.Details)
            }
            alertDialog.dismiss()
        }*/
}

fun FragmentActivity.onSongLongPress(song: Song, itemClick: (LongItemClick) -> Unit) {
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
    val bottomSheet = ActionBottomSheet(
        layoutResId = R.layout.layout_generic_bottom_sheet,
        onViewCreatedCallback = { view, bundle, sheetView ->
            val bottomSheetBinding = LayoutGenericBottomSheetBinding.bind(view)
            bottomSheetBinding.apply {
                ivArt.load(Uri.parse(song.artUri)) {
                    error(R.drawable.logo)
                    crossfade(true)
                    transformations(RoundedCornersTransformation(20f))
                }
                tvTitle.text = song.title
                tvSubtitle.text = song.artist
                listView.adapter = ad
            }
            bottomSheetBinding.listView.onItemClickListener =
                AdapterView.OnItemClickListener { _, _, itemPosition, _ ->
                    when (items[itemPosition]) {
                        LongItemClick.Play.title -> itemClick(LongItemClick.Play)
                        LongItemClick.SinglePlay.title -> itemClick(LongItemClick.SinglePlay)
//                        LongItemClick.PlayNext.title -> itemClick(LongItemClick.PlayNext)
                        LongItemClick.AddToQueue.title -> itemClick(LongItemClick.AddToQueue)
                        LongItemClick.AddToPlaylist.title -> itemClick(LongItemClick.AddToPlaylist)
//                        LongItemClick.Delete.title -> itemClick(LongItemClick.Delete)
                        LongItemClick.Share.title -> itemClick(LongItemClick.Share)
                        LongItemClick.Details.title -> itemClick(LongItemClick.Details)
                    }
                    sheetView.dismiss()
                }

        }
    )
    bottomSheet.show(this.supportFragmentManager, "onSongLongPress")
    /*val alertList = AlertListBinding.inflate(LayoutInflater.from(this))
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
        }*/
}

fun Context.shareSong(location: String) {
    val intent = Intent(Intent.ACTION_SEND)
    intent.type = "audio/*"
    val uri = Uri.parse(location)
    intent.putExtra(Intent.EXTRA_STREAM, uri)
    startActivity(Intent.createChooser(intent, "Share Via"))
}

fun FragmentActivity.showSongInfo(song: Song) {
    val bottomSheet = ActionBottomSheet(
        layoutResId = R.layout.info,
        onViewCreatedCallback = { view, bundle, sheetView ->
            val binding = InfoBinding.bind(view)
            binding.apply {
                infoArt.load(Uri.parse(song.artUri)) {
                    error(R.drawable.logo)
                    crossfade(true)
                    transformations(RoundedCornersTransformation(20f))
                }
                infoTitle.text = song.title
                infoArtist.text = song.artist
                infoAlbum.text = song.album
                infoComposer.text = song.composer
                infoDuration.text = song.durationFormatted
                infoLocation.text = song.location
            }
        }
    )
    bottomSheet.show(this.supportFragmentManager, "showSongInfo")
    /*val binding = InfoBinding.inflate(LayoutInflater.from(this))
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
    dialog.show()*/
}