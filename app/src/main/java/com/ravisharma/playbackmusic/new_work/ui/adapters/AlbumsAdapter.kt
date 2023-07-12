package com.ravisharma.playbackmusic.new_work.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.ravisharma.playbackmusic.data.db.model.tables.Album
import com.ravisharma.playbackmusic.databinding.AdapterAlbumsBinding
import com.ravisharma.playbackmusic.new_work.ui.adapters.viewholder.AlbumViewHolder

class AlbumsAdapter(
    private val onItemClick: (Album) -> Unit
) : RecyclerView.Adapter<AlbumViewHolder>() {

    private val diffUtilCallback = object : DiffUtil.ItemCallback<Album>() {
        override fun areItemsTheSame(oldItemPosition: Album, newItemPosition: Album): Boolean {
            return oldItemPosition.name == newItemPosition.name
        }

        override fun areContentsTheSame(oldItemPosition: Album, newItemPosition: Album): Boolean {
            return oldItemPosition == newItemPosition
        }
    }

    private val mDiffer = AsyncListDiffer(this, diffUtilCallback)

    fun submitList(list: List<Album>) {
        mDiffer.submitList(list)
    }

    fun getCurrentList(): List<Album> {
        return mDiffer.currentList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder {
        return AlbumViewHolder(
            AdapterAlbumsBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: AlbumViewHolder, position: Int) {
        holder.bind(mDiffer.currentList[position], onItemClick = onItemClick)
    }

    override fun getItemCount(): Int = mDiffer.currentList.size
}