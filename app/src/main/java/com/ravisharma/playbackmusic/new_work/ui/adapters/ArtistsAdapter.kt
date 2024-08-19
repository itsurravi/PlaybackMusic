package com.ravisharma.playbackmusic.new_work.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.ravisharma.playbackmusic.data.db.model.ArtistWithSongCount
import com.ravisharma.playbackmusic.databinding.AdapterArtistBinding
import com.ravisharma.playbackmusic.new_work.ui.adapters.viewholder.ArtistViewHolder

class ArtistsAdapter(
    private val onItemClick: (ArtistWithSongCount) -> Unit
) : RecyclerView.Adapter<ArtistViewHolder>() {
    private val diffUtilCallback = object : DiffUtil.ItemCallback<ArtistWithSongCount>() {
        override fun areItemsTheSame(
            oldItemPosition: ArtistWithSongCount,
            newItemPosition: ArtistWithSongCount
        ): Boolean {
            return oldItemPosition.name == newItemPosition.name
        }

        override fun areContentsTheSame(
            oldItemPosition: ArtistWithSongCount,
            newItemPosition: ArtistWithSongCount
        ): Boolean {
            return oldItemPosition == newItemPosition
        }
    }

    private val mDiffer = AsyncListDiffer(this, diffUtilCallback)

    fun submitList(list: List<ArtistWithSongCount>) {
        mDiffer.submitList(list)
    }

    fun getCurrentList(): List<ArtistWithSongCount> {
        return mDiffer.currentList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtistViewHolder {
        return ArtistViewHolder(
            AdapterArtistBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ArtistViewHolder, position: Int) {
        holder.bind(mDiffer.currentList[position], onItemClick)
    }

    override fun getItemCount(): Int = mDiffer.currentList.size
}