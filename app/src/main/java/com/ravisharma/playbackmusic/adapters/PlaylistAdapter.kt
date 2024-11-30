package com.ravisharma.playbackmusic.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ravisharma.playbackmusic.MainActivity
import com.ravisharma.playbackmusic.R
import com.ravisharma.playbackmusic.adapters.PlaylistAdapter.PlaylistHolder
import com.ravisharma.playbackmusic.databinding.AdapterPlaylistBinding

class PlaylistAdapter(private val context: Context, private val playlistArrayList: List<String>) : RecyclerView.Adapter<PlaylistHolder>() {

    private lateinit var onClicked: OnPlaylistClicked
    private lateinit var onLongClicked: OnPlaylistLongClicked

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistHolder {
        val binding = AdapterPlaylistBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PlaylistHolder(binding)
    }

    override fun onBindViewHolder(holder: PlaylistHolder, position: Int) {
        val playList = playlistArrayList[position]
        holder.binding.apply {
            playlistTitle.text = playList
            if (context is MainActivity) {
                when (position) {
                    0 -> {
                        playlistIcon.setImageResource(R.drawable.ic_baseline_favorite_24)
                    }
                    else -> {
                        playlistIcon.setImageResource(R.drawable.ic_created_playlist)
                    }
                }
            }
            playlistBox.apply {
                setOnClickListener { onClicked.onPlaylistClick(position) }
                setOnLongClickListener {
                    onLongClicked.onPlaylistLongClick(position)
                    return@setOnLongClickListener true
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return playlistArrayList.size
    }

    inner class PlaylistHolder(val binding: AdapterPlaylistBinding) : RecyclerView.ViewHolder(binding.root)

    //make interface like this
    interface OnPlaylistClicked {
        fun onPlaylistClick(position: Int)
    }

    interface OnPlaylistLongClicked {
        fun onPlaylistLongClick(position: Int)
    }

    fun setOnPlaylistClick(onClick: OnPlaylistClicked) {
        onClicked = onClick
    }

    fun setOnPlaylistLongClick(onClick: OnPlaylistLongClicked) {
        onLongClicked = onClick
    }

}