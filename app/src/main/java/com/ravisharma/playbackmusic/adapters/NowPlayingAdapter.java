package com.ravisharma.playbackmusic.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.ravisharma.playbackmusic.MainActivity;
import com.ravisharma.playbackmusic.model.Song;
import com.ravisharma.playbackmusic.R;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;


/**
 * Created by Ravi Sharma on 07-Jan-18.
 */

public class NowPlayingAdapter extends RecyclerView.Adapter<NowPlayingAdapter.ViewHolder> {

    private ArrayList<Song> songs;
    Context c;

    //declare interface
    private OnItemClicked onClick;

    public void setList(ArrayList<Song> songList) {
        this.songs = songList;
        notifyDataSetChanged();
    }

    //make interface like this
    public interface OnItemClicked {
        void onItemClick(int position);

        void onOptionsClick(int position);
    }

    public NowPlayingAdapter(ArrayList<Song> theSongs, Context c) {
        songs = theSongs;
        this.c = c;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView artist;
        TextView durView;
        ImageView songart;
        ImageView imgOptions;
        RelativeLayout songbox;
        LottieAnimationView animationView;

        public ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.song_title);
            artist = itemView.findViewById(R.id.song_artist);
            durView = itemView.findViewById(R.id.duration);
            songbox = itemView.findViewById(R.id.song_box);
            songart = itemView.findViewById(R.id.song_art);
            imgOptions = itemView.findViewById(R.id.imgOptions);
            animationView = itemView.findViewById(R.id.animation_view);
        }
    }

    @Override
    public NowPlayingAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adap_now_playing, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(NowPlayingAdapter.ViewHolder holder, final int position) {
        Song currSong = songs.get(position);
        TextView title = holder.title;
        TextView artist = holder.artist;
        TextView duration = holder.durView;
        ImageView art = holder.songart;

        title.setText(currSong.getTitle());
        artist.setText(currSong.getArtist());
        duration.setText((String.format("%d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(currSong.getDuration()),
                TimeUnit.MILLISECONDS.toSeconds(currSong.getDuration()) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(currSong.getDuration())))));

        /*Song art code here*/
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.placeholder(R.drawable.logo);
        requestOptions.error(R.drawable.logo);

        Glide.with(c)
                .setDefaultRequestOptions(requestOptions)
                .load(Uri.parse(currSong.getArt()))
                .into(art);

        holder.songbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClick.onItemClick(position);
            }
        });

        holder.imgOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClick.onOptionsClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    public void setOnClick(OnItemClicked onClick) {
        this.onClick = onClick;
    }
}