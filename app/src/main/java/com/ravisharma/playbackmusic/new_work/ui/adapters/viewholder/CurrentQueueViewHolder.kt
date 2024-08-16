package com.ravisharma.playbackmusic.new_work.ui.adapters.viewholder

import android.annotation.SuppressLint
import android.net.Uri
import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.request.CachePolicy
import coil.transform.RoundedCornersTransformation
import com.ravisharma.playbackmusic.R
import com.ravisharma.playbackmusic.data.db.model.tables.Song
import com.ravisharma.playbackmusic.databinding.AdapNowPlayingBinding
import com.ravisharma.playbackmusic.utils.StartDragListener

class CurrentQueueViewHolder(
    private var dragListener: StartDragListener,
    val binding: AdapNowPlayingBinding
) :
    RecyclerView.ViewHolder(binding.root) {

    @SuppressLint("ClickableViewAccessibility")
    fun bind(
        song: Song,
        onItemClick: ((Song, Int) -> Unit)? = null,
        onItemLongClick: ((Song, Int) -> Unit)? = null
    ) {
        binding.apply {
            songTitle.text = song.title
            songArtist.text = song.artist
            duration.text = song.durationFormatted
            songArt.load(Uri.parse(song.artUri)) {
                error(R.drawable.logo)
                crossfade(true)
                diskCachePolicy(CachePolicy.ENABLED)
                memoryCachePolicy(CachePolicy.ENABLED)
                transformations(RoundedCornersTransformation(20f))
            }

            songBox.apply {
                setOnClickListener {
                    onItemClick?.let {
                        it(song, bindingAdapterPosition)
                    }
                }
                setOnLongClickListener {
                    onItemLongClick?.let {
                        it(song, bindingAdapterPosition)
                    }
                    return@setOnLongClickListener true
                }
            }
            ivOrder.setOnTouchListener { v, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    dragListener.requestDrag(this@CurrentQueueViewHolder)
                }
                true
            }
        }
    }
}