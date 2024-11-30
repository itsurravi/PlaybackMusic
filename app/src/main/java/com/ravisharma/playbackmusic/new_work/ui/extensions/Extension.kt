package com.ravisharma.playbackmusic.new_work.ui.extensions

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.fragment.app.FragmentActivity
import coil.load
import coil.transform.RoundedCornersTransformation
import com.ravisharma.playbackmusic.R
import com.ravisharma.playbackmusic.data.db.model.PlaylistWithSongCount
import com.ravisharma.playbackmusic.data.db.model.tables.Song
import com.ravisharma.playbackmusic.databinding.InfoBinding
import com.ravisharma.playbackmusic.databinding.LayoutGenericBottomSheetBinding
import com.ravisharma.playbackmusic.new_work.ui.bottomsheet.ActionBottomSheet

sealed class LongItemClick(val title: String, @DrawableRes val icon: Int) {
    data object Play : LongItemClick("Play", R.drawable.ic_baseline_play_24)
    data object SinglePlay : LongItemClick("Play This Only", R.drawable.ic_created_playlist)
    data object AddToQueue : LongItemClick("Add To Queue", R.drawable.ic_music_head)
    data object AddToPlaylist : LongItemClick("Add To Playlist", R.drawable.ic_playlist_add_24)
    data object RemoveFromList : LongItemClick("Remove From List", R.drawable.ic_baseline_close_40)
    data object Rename : LongItemClick("Rename", R.drawable.ic_rename_24)
    data object Delete : LongItemClick("Delete", R.drawable.ic_delete_24)
    data object Share : LongItemClick("Share", R.drawable.ic_share_24)
    data object Details : LongItemClick("Details", R.drawable.ic_info)
}

fun Context.showToast(msg: String) {
    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}

fun FragmentActivity.onPlaylistSongLongPress(song: Song, itemClick: (LongItemClick) -> Unit) {
    val items = listOf(
        LongItemClick.Play,
        LongItemClick.SinglePlay,
        LongItemClick.AddToQueue,
        LongItemClick.RemoveFromList,
        LongItemClick.Share,
        LongItemClick.Details
    )
    val bottomSheet = ActionBottomSheet(
        layoutResId = R.layout.layout_generic_bottom_sheet,
        onViewCreatedCallback = { view, _, sheetView ->
            val binding = LayoutGenericBottomSheetBinding.bind(view)
            binding.apply {
                ivArt.load(Uri.parse(song.artUri)) {
                    error(R.drawable.logo)
                    crossfade(true)
                    transformations(RoundedCornersTransformation(20f))
                }
                tvTitle.text = song.title
                tvSubtitle.text = song.artist
                listView.adapter = DialogListAdapter(items, itemClick = {
                    itemClick(it)
                    sheetView.dismiss()
                })
            }
        }
    )
    bottomSheet.show(this.supportFragmentManager, "onPlaylistSongLongPress")
}

fun FragmentActivity.onSongLongPress(song: Song, itemClick: (LongItemClick) -> Unit) {
    val items = listOf(
        LongItemClick.Play,
        LongItemClick.SinglePlay,
        LongItemClick.AddToQueue,
        LongItemClick.AddToPlaylist,
        LongItemClick.Share,
        LongItemClick.Details,
    )
    val bottomSheet = ActionBottomSheet(
        layoutResId = R.layout.layout_generic_bottom_sheet,
        onViewCreatedCallback = { view, _, sheetView ->
            val bottomSheetBinding = LayoutGenericBottomSheetBinding.bind(view)
            bottomSheetBinding.apply {
                ivArt.load(Uri.parse(song.artUri)) {
                    error(R.drawable.logo)
                    crossfade(true)
                    transformations(RoundedCornersTransformation(20f))
                }
                tvTitle.text = song.title
                tvSubtitle.text = song.artist
                listView.adapter = DialogListAdapter(items, itemClick = {
                    itemClick(it)
                    sheetView.dismiss()
                })
            }
        }
    )
    bottomSheet.show(this.supportFragmentManager, "onSongLongPress")
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
}

fun FragmentActivity.onPlaylistLongPress(
    playlistWithSongCount: PlaylistWithSongCount,
    itemClick: (LongItemClick) -> Unit
) {
    val items = listOf(
        LongItemClick.Play,
        LongItemClick.Rename,
        LongItemClick.Delete,
    )
    val bottomSheet = ActionBottomSheet(
        layoutResId = R.layout.layout_generic_bottom_sheet,
        onViewCreatedCallback = { view, _, sheetView ->
            val bottomSheetBinding = LayoutGenericBottomSheetBinding.bind(view)
            bottomSheetBinding.apply {
                ivArt.setImageResource(R.drawable.ic_music_head)
                tvTitle.text = playlistWithSongCount.playlistName
                tvSubtitle.text = view.resources.getQuantityString(
                    R.plurals.numberOfSongs,
                    playlistWithSongCount.count,
                    playlistWithSongCount.count
                )
                listView.adapter = DialogListAdapter(items, itemClick = {
                    itemClick(it)
                    sheetView.dismiss()
                })
            }
        }
    )
    bottomSheet.show(this.supportFragmentManager, "onPlaylistSongPress")
}