package com.ravisharma.playbackmusic.new_work.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.ravisharma.playbackmusic.data.db.model.tables.Song
import com.ravisharma.playbackmusic.databinding.AdapSongBinding
import com.ravisharma.playbackmusic.new_work.ui.adapters.viewholder.TrackViewHolder
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView

class TracksAdapter(
    private val onItemClick: ((Song, Int) -> Unit)? = null,
    private val onItemLongClick: ((Song, Int) -> Unit)? = null
) : RecyclerView.Adapter<TrackViewHolder>(), FastScrollRecyclerView.SectionedAdapter {

    private val diffCallback = object : DiffUtil.ItemCallback<Song>() {
        override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }

        override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    private val differList = AsyncListDiffer(this, diffCallback)

    fun submitList(list: List<Song>, callback: Runnable? = null) {
        differList.submitList(list, callback)
    }

    fun getCurrentList(): List<Song> {
        return differList.currentList
    }

    override fun getSectionName(position: Int): String {
        return differList.currentList[position].title[0].toString()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        return TrackViewHolder(
            AdapSongBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        holder.bind(
            song = differList.currentList[position],
            onItemClick = onItemClick,
            onItemLongClick = onItemLongClick
        )
    }

    override fun getItemCount(): Int = differList.currentList.size
}