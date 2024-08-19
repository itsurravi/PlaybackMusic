package com.ravisharma.playbackmusic.new_work.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.ravisharma.playbackmusic.data.db.model.tables.Song
import com.ravisharma.playbackmusic.databinding.AdapNowPlayingBinding
import com.ravisharma.playbackmusic.new_work.ui.adapters.viewholder.CurrentQueueViewHolder
import com.ravisharma.playbackmusic.utils.StartDragListener
import java.util.Collections

class CurrentQueueAdapter(
    private var dragListener: StartDragListener,
    private val onItemClick: ((Song, Int) -> Unit)? = null,
    private val onItemLongClick: ((Song, Int) -> Unit)? = null,
    private val onItemRemoveClick: ((Song, Int) -> Unit)? = null
) : RecyclerView.Adapter<CurrentQueueViewHolder>() {

    private val diffCallback = object : DiffUtil.ItemCallback<Song>() {
        override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem.location == newItem.location
        }

        override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    private val differList = AsyncListDiffer(this, diffCallback)

    fun submitList(list: List<Song>, callback: Runnable? = null) {
        differList.submitList(list.map { it.copy() }, callback)
    }

    fun getCurrentList(): List<Song> {
        return differList.currentList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CurrentQueueViewHolder {
        return CurrentQueueViewHolder(
            dragListener,
            AdapNowPlayingBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: CurrentQueueViewHolder, position: Int) {
        holder.bind(
            song = differList.currentList[position],
            onItemClick = onItemClick,
            onItemLongClick = onItemLongClick,
            onItemRemoveClick = onItemRemoveClick
        )
    }

    override fun getItemCount(): Int = differList.currentList.size
}