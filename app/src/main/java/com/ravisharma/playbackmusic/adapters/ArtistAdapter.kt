package com.ravisharma.playbackmusic.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ravisharma.playbackmusic.adapters.ArtistAdapter.ArtistViewHolder
import com.ravisharma.playbackmusic.databinding.AdapterArtistBinding
import com.ravisharma.playbackmusic.model.Artist
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView

class ArtistAdapter(private var artistList: ArrayList<Artist>) :
    RecyclerView.Adapter<ArtistViewHolder>(), FastScrollRecyclerView.SectionedAdapter {

    private lateinit var onClick: OnArtistClicked

    override fun getSectionName(position: Int): String {
        return artistList[position].artistName[0].toString()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtistViewHolder {
        val binding = AdapterArtistBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ArtistViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ArtistViewHolder, position: Int) {
        val currArtist = artistList[holder.adapterPosition]
        holder.binding.apply {
            artistTitle.text = currArtist.artistName
            numberOfSongs.text = currArtist.numberOfTracks
            artistbox.setOnClickListener { onClick.onArtistClick(holder.adapterPosition) }
        }
    }

    override fun getItemCount(): Int {
        return artistList.size
    }

    inner class ArtistViewHolder(val binding: AdapterArtistBinding) : RecyclerView.ViewHolder(binding.root)

    //make interface like this
    interface OnArtistClicked {
        fun onArtistClick(position: Int)
    }

    fun setOnClick(onClick: OnArtistClicked) {
        this.onClick = onClick
    }
}