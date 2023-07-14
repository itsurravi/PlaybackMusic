package com.ravisharma.playbackmusic.new_work.ui.adapters.viewholder

import androidx.recyclerview.widget.RecyclerView
import com.ravisharma.playbackmusic.data.db.model.ArtistWithSongCount
import com.ravisharma.playbackmusic.data.db.model.tables.Artist
import com.ravisharma.playbackmusic.databinding.AdapterArtistBinding

class ArtistViewHolder(val binding: AdapterArtistBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bind(artist: ArtistWithSongCount, onItemClick: (ArtistWithSongCount) -> Unit) {
        binding.apply {
            artistTitle.text = artist.name
            numberOfSongs.text = artist.count.toString()

            artistbox.setOnClickListener {
                onItemClick(artist)
            }
        }
    }
}