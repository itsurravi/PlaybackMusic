package com.ravisharma.playbackmusic.adapters

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.Nullable
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.ravisharma.playbackmusic.R
import com.ravisharma.playbackmusic.databinding.AdapSongBinding
import com.ravisharma.playbackmusic.model.Song
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView
import java.util.concurrent.TimeUnit

class CategoryAdapter(private var c: Context) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    private var songs: ArrayList<Song> = ArrayList()

    //declare interface
    private lateinit var onClick: OnItemClicked
    private lateinit var onLongClick: OnItemLongClicked

    //make interface like this
    interface OnItemClicked {
        fun onItemClick(position: Int)
    }

    interface OnItemLongClicked {
        fun onItemLongClick(position: Int)
    }

    fun setList(songList: ArrayList<Song>) {
        val diffCallback = DiffCallback(songs, songList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        diffResult.dispatchUpdatesTo(this)
        songs.clear()
        songs.addAll(songList)
    }

    class ViewHolder(val binding: AdapSongBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = AdapSongBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currSong = songs[holder.adapterPosition]
        holder.binding.apply {
            songTitle.text = currSong.title
            songArtist.text = currSong.artist
            duration.text = String.format("%d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(currSong.duration),
                TimeUnit.MILLISECONDS.toSeconds(currSong.duration) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(currSong.duration)))

            val requestOptions = RequestOptions().apply {
                placeholder(R.drawable.logo)
                error(R.drawable.logo)
            }
            Glide.with(c)
                .setDefaultRequestOptions(requestOptions)
                .load(Uri.parse(currSong.art))
                .transform(CenterCrop(), RoundedCorners(20))
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .into(songArt)
            songBox.apply {
                setOnClickListener { onClick.onItemClick(holder.adapterPosition) }
                setOnLongClickListener {
                    onLongClick.onItemLongClick(holder.adapterPosition)
                    return@setOnLongClickListener true
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return songs.size
    }

    fun setOnClick(onClick: OnItemClicked) {
        this.onClick = onClick
    }

    fun setOnLongClick(onLongClick: OnItemLongClicked) {
        this.onLongClick = onLongClick
    }

    inner class DiffCallback(private val oldList: ArrayList<Song>, private val newList: ArrayList<Song>) : DiffUtil.Callback() {
        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].id == newList[newItemPosition].id
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}