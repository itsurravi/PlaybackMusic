package com.ravisharma.playbackmusic.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ravisharma.playbackmusic.R
import com.ravisharma.playbackmusic.adapters.ArtistAdapter.ArtistViewHolder
import com.ravisharma.playbackmusic.model.Artist

class ArtistAdapter(var c: Context, private var artistList: ArrayList<Artist>) : RecyclerView.Adapter<ArtistViewHolder>() {
    private lateinit var onClick: OnArtistClicked

    fun setList(artistList: ArrayList<Artist>) {
        this.artistList = artistList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtistViewHolder {
        val v = LayoutInflater.from(c).inflate(R.layout.adapter_artist, parent, false)
        return ArtistViewHolder(v)
    }

    override fun onBindViewHolder(holder: ArtistViewHolder, position: Int) {
        val currArtist = artistList[position]
        holder.artistName.text = currArtist.artistName
        holder.numberOfSongs.text = currArtist.numberOfTracks
        holder.albumBox.setOnClickListener { onClick.onArtistClick(position) }
    }

    override fun getItemCount(): Int {
        return artistList.size
    }

    inner class ArtistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var artistName: TextView = itemView.findViewById(R.id.artistTitle)
        var numberOfSongs: TextView = itemView.findViewById(R.id.numberOfSongs)
        var albumBox: LinearLayout = itemView.findViewById(R.id.artistbox)
    }

    //make interface like this
    interface OnArtistClicked {
        fun onArtistClick(position: Int)
    }

    fun setOnClick(onClick: OnArtistClicked) {
        this.onClick = onClick
    }

}