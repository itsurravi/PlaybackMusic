package com.ravisharma.playbackmusic.adapters

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.ravisharma.playbackmusic.R
import com.ravisharma.playbackmusic.model.Song
import java.util.concurrent.TimeUnit

class SongAdapter(private var songs: ArrayList<Song>, var c: Context) : RecyclerView.Adapter<SongAdapter.ViewHolder>() {

    //declare interface
    private lateinit var onClick: OnItemClicked
    private lateinit var onLongClick: OnItemLongClicked

    fun setList(songList: ArrayList<Song>) {
        songs = songList
        notifyDataSetChanged()
    }

    //make interface like this
    interface OnItemClicked {
        fun onItemClick(position: Int)
    }

    interface OnItemLongClicked {
        fun onItemLongClick(position: Int)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var title: TextView = itemView.findViewById(R.id.song_title)
        var artist: TextView = itemView.findViewById(R.id.song_artist)
        var durView: TextView = itemView.findViewById(R.id.duration)
        var songart: ImageView = itemView.findViewById(R.id.song_art)
        var songbox: RelativeLayout = itemView.findViewById(R.id.song_box)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.adap_song, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currSong = songs[position]
        val title = holder.title
        val artist = holder.artist
        val duration = holder.durView
        val art = holder.songart
        title.text = currSong.title
        artist.text = currSong.artist
        duration.text = String.format("%d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(currSong.duration),
                TimeUnit.MILLISECONDS.toSeconds(currSong.duration) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(currSong.duration)))

        /*Song art code here*/
        val requestOptions = RequestOptions()
        requestOptions.placeholder(R.drawable.logo)
        requestOptions.error(R.drawable.logo)
        Glide.with(c)
                .setDefaultRequestOptions(requestOptions)
                .load(Uri.parse(currSong.art))
                .into(art)
        holder.songbox.setOnClickListener { onClick.onItemClick(position) }
        holder.songbox.setOnLongClickListener {
            onLongClick.onItemLongClick(position)
            return@setOnLongClickListener true
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

}