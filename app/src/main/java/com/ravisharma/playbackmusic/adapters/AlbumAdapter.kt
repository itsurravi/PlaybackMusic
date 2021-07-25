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
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.ravisharma.playbackmusic.R
import com.ravisharma.playbackmusic.adapters.AlbumAdapter.AlbumViewHolder
import com.ravisharma.playbackmusic.databinding.AdapterAlbumsBinding
import com.ravisharma.playbackmusic.model.Album
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView

class AlbumAdapter(private var c: Context, private var albumsList: ArrayList<Album>) :
    RecyclerView.Adapter<AlbumViewHolder>(), FastScrollRecyclerView.SectionedAdapter {

    private lateinit var onClick: OnAlbumClicked

    override fun getSectionName(position: Int): String {
        return albumsList[position].albumName[0].toString()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder {
        val binding = AdapterAlbumsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AlbumViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AlbumViewHolder, position: Int) {
        val currAlbum = albumsList[holder.adapterPosition]
        holder.binding.apply {
            albumTitle.text = currAlbum.albumName
            artistTitle.text = currAlbum.albumArtist
            val requestOptions = RequestOptions().apply {
                placeholder(R.drawable.logo)
                error(R.drawable.logo)
            }
            Glide.with(c)
                    .setDefaultRequestOptions(requestOptions)
                    .load(currAlbum.albumArt)
                    .transform(CenterCrop(), RoundedCorners(20))
                    .diskCacheStrategy(DiskCacheStrategy.DATA)
                    .into(albumArt)

            albumBox.setOnClickListener { onClick.onAlbumClick(holder.adapterPosition) }
        }
    }

    override fun getItemCount(): Int {
        return albumsList.size
    }

    inner class AlbumViewHolder(val binding: AdapterAlbumsBinding) : RecyclerView.ViewHolder(binding.root)

    //make interface like this
    interface OnAlbumClicked {
        fun onAlbumClick(position: Int)
    }

    fun setOnClick(onClick: OnAlbumClicked) {
        this.onClick = onClick
    }

}