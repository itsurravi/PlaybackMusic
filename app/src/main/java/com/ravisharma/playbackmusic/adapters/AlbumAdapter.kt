package com.ravisharma.playbackmusic.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.ravisharma.playbackmusic.R
import com.ravisharma.playbackmusic.adapters.AlbumAdapter.AlbumViewHolder
import com.ravisharma.playbackmusic.model.Album

class AlbumAdapter(private var c: Context, private var albumsList: ArrayList<Album>) : RecyclerView.Adapter<AlbumViewHolder>() {

    private lateinit var onClick: OnAlbumClicked

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder {
        val v = LayoutInflater.from(c).inflate(R.layout.adapter_albums, parent, false)
        return AlbumViewHolder(v)
    }

    override fun onBindViewHolder(holder: AlbumViewHolder, position: Int) {
        val currAlbum = albumsList[position]
        holder.albumTitle.text = currAlbum.albumName
        holder.artistTitle.text = currAlbum.albumArtist
        /*Song art code here*/
        val requestOptions = RequestOptions()
        requestOptions.placeholder(R.drawable.logo)
        requestOptions.error(R.drawable.logo)
        Glide.with(c)
                .setDefaultRequestOptions(requestOptions)
                .load(currAlbum.albumArt)
                .into(holder.albumArt)
        holder.albumBox.setOnClickListener { onClick.onAlbumClick(position) }
    }

    override fun getItemCount(): Int {
        return albumsList.size
    }

    inner class AlbumViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var albumTitle: TextView = itemView.findViewById(R.id.albumTitle)
        var artistTitle: TextView = itemView.findViewById(R.id.artistTitle)
        var albumArt: ImageView = itemView.findViewById(R.id.albumArt)
        var albumBox: LinearLayout = itemView.findViewById(R.id.albumBox)

    }

    //make interface like this
    interface OnAlbumClicked {
        fun onAlbumClick(position: Int)
    }

    fun setOnClick(onClick: OnAlbumClicked) {
        this.onClick = onClick
    }

}