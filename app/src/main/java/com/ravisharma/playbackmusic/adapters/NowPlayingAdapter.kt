package com.ravisharma.playbackmusic.adapters

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.ravisharma.playbackmusic.R
import com.ravisharma.playbackmusic.model.Song
import com.ravisharma.playbackmusic.utils.StartDragListener
import java.util.concurrent.TimeUnit

class NowPlayingAdapter(private var songs: ArrayList<Song>, private var c: Context, private var dragListener: StartDragListener) : RecyclerView.Adapter<NowPlayingAdapter.ViewHolder>() {

    //declare interface
    private lateinit var onClick: OnItemClicked

    fun setList(songList: ArrayList<Song>) {
        songs = songList
        notifyDataSetChanged()
    }

    //make interface like this
    interface OnItemClicked {
        fun onItemClick(position: Int)
        fun onOptionsClick(position: Int)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var title: TextView = itemView.findViewById(R.id.song_title)
        var artist: TextView = itemView.findViewById(R.id.song_artist)
        var durView: TextView = itemView.findViewById(R.id.duration)
        var songart: ImageView = itemView.findViewById(R.id.song_art)
        var imgOptions: ImageView = itemView.findViewById(R.id.imgOptions)
        var ivOrder: ImageView = itemView.findViewById(R.id.ivOrder)
        var songbox: RelativeLayout = itemView.findViewById(R.id.song_box)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.adap_now_playing, parent, false)
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
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .into(art)
        holder.songbox.setOnClickListener { onClick.onItemClick(position) }
        holder.imgOptions.setOnClickListener { onClick.onOptionsClick(position) }
        holder.ivOrder.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                dragListener.requestDrag(holder)
            }
            true
        }
    }

    override fun getItemCount() = songs.size

    fun setOnClick(onClick: OnItemClicked) {
        this.onClick = onClick
    }

}