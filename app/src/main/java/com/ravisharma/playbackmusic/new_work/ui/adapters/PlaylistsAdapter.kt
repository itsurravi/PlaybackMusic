package com.ravisharma.playbackmusic.new_work.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.ravisharma.playbackmusic.R
import com.ravisharma.playbackmusic.data.db.model.PlaylistWithSongCount
import com.ravisharma.playbackmusic.databinding.AdapterPlaylistBinding

class PlaylistsAdapter(
    private val onClick: ((PlaylistWithSongCount) -> Unit)? = null,
    private val onLongClick: ((PlaylistWithSongCount) -> Unit)? = null
) : RecyclerView.Adapter<PlaylistsAdapter.PlaylistsViewHolder>() {

    private val diffCallback = object : DiffUtil.ItemCallback<PlaylistWithSongCount>() {
        override fun areItemsTheSame(
            oldItem: PlaylistWithSongCount,
            newItem: PlaylistWithSongCount
        ): Boolean {
            return oldItem.playlistId == newItem.playlistId
        }

        override fun areContentsTheSame(
            oldItem: PlaylistWithSongCount,
            newItem: PlaylistWithSongCount
        ): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    private val differList = AsyncListDiffer(this, diffCallback)

    fun submitList(list: List<PlaylistWithSongCount>, callback: Runnable? = null) {
        differList.submitList(list, callback)
    }

    fun getCurrentList(): List<PlaylistWithSongCount> {
        return differList.currentList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistsViewHolder {
        return PlaylistsViewHolder(
            AdapterPlaylistBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: PlaylistsViewHolder, position: Int) {
        holder.bind(differList.currentList[position], onClick, onLongClick)
    }

    override fun getItemCount(): Int = differList.currentList.size

    inner class PlaylistsViewHolder(val binding: AdapterPlaylistBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            playlistWithSongCount: PlaylistWithSongCount,
            onClick: ((PlaylistWithSongCount) -> Unit)?,
            onLongClick: ((PlaylistWithSongCount) -> Unit)?
        ) {
            binding.apply {
                playlistTitle.text = playlistWithSongCount.playlistName
                playlistSongCount.text = itemView.context.resources.getQuantityString(
                    R.plurals.numberOfSongs,
                    playlistWithSongCount.count,
                    playlistWithSongCount.count
                )

                itemView.setOnClickListener {
                    onClick?.let { it1 -> it1(playlistWithSongCount) }
                }
                itemView.setOnLongClickListener {
                    onLongClick?.let { it1 -> it1(playlistWithSongCount) }
                    true
                }
            }
        }
    }
}