package com.ravisharma.playbackmusic.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.RoundedCornersTransformation
import com.ravisharma.playbackmusic.R
import com.ravisharma.playbackmusic.adapters.AlbumAdapter.AlbumViewHolder
import com.ravisharma.playbackmusic.databinding.AdapterAlbumsBinding
import com.ravisharma.playbackmusic.model.Album
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView

class AlbumAdapter : RecyclerView.Adapter<AlbumViewHolder>(),
    FastScrollRecyclerView.SectionedAdapter {

    private val diffUtilCallback = object : DiffUtil.ItemCallback<Album>() {
        override fun areItemsTheSame(oldItemPosition: Album, newItemPosition: Album): Boolean {
            return oldItemPosition.albumId == newItemPosition.albumId
        }

        override fun areContentsTheSame(oldItemPosition: Album, newItemPosition: Album): Boolean {
            return oldItemPosition == newItemPosition
        }
    }

    private val mDiffer = AsyncListDiffer(this, diffUtilCallback)

    fun submitList(list: List<Album>) {
        mDiffer.submitList(list)
    }

    private lateinit var onClick: OnAlbumClicked

    override fun getSectionName(position: Int): String {
        return mDiffer.currentList[position].albumName[0].toString()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder {
        val binding = AdapterAlbumsBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AlbumViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AlbumViewHolder, position: Int) {
        val currAlbum = mDiffer.currentList[holder.bindingAdapterPosition]
        holder.binding.apply {
            albumTitle.text = currAlbum.albumName
//            artistTitle.text = currAlbum.albumArtist

            albumArt.load(currAlbum.albumArt) {
                placeholder(R.drawable.logo)
                error(R.drawable.logo)
                transformations(RoundedCornersTransformation(20f))
            }

            albumBox.setOnClickListener { onClick.onAlbumClick(holder.bindingAdapterPosition) }
        }
    }

    override fun getItemCount(): Int {
        return mDiffer.currentList.size
    }

    inner class AlbumViewHolder(val binding: AdapterAlbumsBinding) :
        RecyclerView.ViewHolder(binding.root)

    //make interface like this
    interface OnAlbumClicked {
        fun onAlbumClick(position: Int)
    }

    fun setOnClick(onClick: OnAlbumClicked) {
        this.onClick = onClick
    }

}