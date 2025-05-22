package com.ravisharma.playbackmusic.utils

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.ravisharma.playbackmusic.MainActivity
import com.ravisharma.playbackmusic.R
import com.ravisharma.playbackmusic.databinding.InfoBinding
import com.ravisharma.playbackmusic.data.olddb.model.Song
import java.util.concurrent.TimeUnit

fun Context.showSongInfo(song: Song) {
    val binding = InfoBinding.inflate(LayoutInflater.from(this))
    val builder = AlertDialog.Builder(this)
    builder.setView(binding.root)
    binding.infoTitle.text = song.title
    binding.infoArtist.text = song.artist
    binding.infoAlbum.text = song.album
    binding.infoComposer.text = song.composer
    binding.infoDuration.text = String.format("%d:%02d",
            TimeUnit.MILLISECONDS.toMinutes(song.duration),
            TimeUnit.MILLISECONDS.toSeconds(song.duration) -
                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(song.duration)))
    binding.infoLocation.text = song.data

    val dialog = builder.create()
    dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    dialog.window!!.attributes.windowAnimations = R.style.DialogAnimation_2
    dialog.show()
}

fun Context.showToast(msg: String) {
    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}

fun Activity.openFragment(fragment: Fragment) {
    (this as MainActivity).hideHomePanel()
    val fragmentTransaction = supportFragmentManager.beginTransaction()
    fragmentTransaction.apply {
        replace(R.id.container, fragment)
        addToBackStack(null)
        commit()
    }
}