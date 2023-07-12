package com.ravisharma.playbackmusic.new_work.ui.adapters.viewholder

import android.net.Uri
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.RoundedCornersTransformation
import com.ravisharma.playbackmusic.R
import com.ravisharma.playbackmusic.data.db.model.tables.Album
import com.ravisharma.playbackmusic.databinding.AdapterAlbumsBinding

class AlbumViewHolder(val binding: AdapterAlbumsBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bind(album: Album, onItemClick: (Album) -> Unit) {
        binding.apply {
            albumTitle.text = album.name
//            artistTitle.text = album.albumArtist
            albumArt.load(Uri.parse(album.albumArtUri)) {
                placeholder(R.drawable.logo)
                error(R.drawable.logo)
                transformations(RoundedCornersTransformation(20f))
            }

            albumBox.setOnClickListener { onItemClick(album) }
        }
    }
}