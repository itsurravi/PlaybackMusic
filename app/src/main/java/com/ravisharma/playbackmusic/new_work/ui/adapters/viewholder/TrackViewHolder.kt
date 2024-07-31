package com.ravisharma.playbackmusic.new_work.ui.adapters.viewholder

import android.net.Uri
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.request.CachePolicy
import coil.transform.RoundedCornersTransformation
import com.ravisharma.playbackmusic.R
import com.ravisharma.playbackmusic.data.db.model.tables.Song
import com.ravisharma.playbackmusic.databinding.AdapSongBinding

class TrackViewHolder(val binding: AdapSongBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bind(
        song: Song,
        onItemClick: ((Song, Int) -> Unit)? = null,
        onItemLongClick: ((Song, Int) -> Unit)? = null
    ) {
        binding.apply {
            songTitle.text = song.title
            songArtist.text = song.artist
            duration.text = song.durationFormatted
            songArt.load(Uri.parse(song.artUri)) {
                placeholder(R.drawable.logo)
                error(R.drawable.logo)
                diskCachePolicy(CachePolicy.ENABLED)
                memoryCachePolicy(CachePolicy.ENABLED)
                transformations(RoundedCornersTransformation(20f))
            }

            songBox.apply {
                setOnClickListener {
                    onItemClick?.let {
                        it(song, bindingAdapterPosition)
                    }
                }
                setOnLongClickListener {
                    onItemLongClick?.let {
                        it(song, bindingAdapterPosition)
                    }
                    return@setOnLongClickListener true
                }
            }
        }
    }
}