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

class PlaylistAdapter(private val context: Context, private val playlistArrayList: List<String>) : RecyclerView.Adapter<PlaylistHolder>() {

    private lateinit var onClicked: OnPlaylistClicked
    private lateinit var onLongClicked: OnPlaylistLongClicked

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistHolder {
        val v = LayoutInflater.from(context).inflate(R.layout.adapter_playlist, parent, false)
        return PlaylistHolder(v)
    }

    override fun onBindViewHolder(holder: PlaylistHolder, position: Int) {
        val playList = playlistArrayList[position]
        holder.playlistName.text = playList
        if (context is MainActivity) {
            when (position) {
                0 -> {
                    holder.playlistIcon.setImageResource(R.drawable.ic_recent_timer)
                }
                1 -> {
                    holder.playlistIcon.setImageResource(R.drawable.ic_favtrack_playlist)
                }
                else -> {
                    holder.playlistIcon.setImageResource(R.drawable.ic_created_playlist)
                }
            }
        }
        holder.playlistBox.setOnClickListener { onClicked.onPlaylistClick(position) }

        holder.playlistBox.setOnLongClickListener {
            onLongClicked.onPlaylistLongClick(position)
            return@setOnLongClickListener true
        }
    }

    override fun getItemCount(): Int {
        return playlistArrayList.size
    }

    inner class PlaylistHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var playlistIcon: ImageView = itemView.findViewById(R.id.playlistIcon)
        var playlistName: TextView = itemView.findViewById(R.id.playlistTitle)
        var playlistBox: LinearLayout = itemView.findViewById(R.id.playlistBox)
    }

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