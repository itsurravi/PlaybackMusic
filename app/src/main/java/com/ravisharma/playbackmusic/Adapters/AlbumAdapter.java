package com.ravisharma.playbackmusic.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.ravisharma.playbackmusic.Model.Album;
import com.ravisharma.playbackmusic.R;

import java.util.ArrayList;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.AlbumViewHolder> {

    Context c;
    private ArrayList<Album> albumsList;
    private OnAlbumClicked onClick;

    public AlbumAdapter(Context c, ArrayList<Album> albumsList) {
        this.c = c;
        this.albumsList = albumsList;
    }

    public void setList(ArrayList<Album> albumsList) {
        this.albumsList = albumsList;
    }

    @NonNull
    @Override
    public AlbumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(c).inflate(R.layout.adapter_albums, parent, false);
        return new AlbumViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumViewHolder holder, final int position) {
        Album currAlbum = albumsList.get(position);

        holder.albumTitle.setText(currAlbum.getAlbumName());
        holder.artistTitle.setText(currAlbum.getAlbumArtist());
        /*Song art code here*/
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.placeholder(R.drawable.logo);
        requestOptions.error(R.drawable.logo);

        Glide.with(c)
                .setDefaultRequestOptions(requestOptions)
                .load(currAlbum.getAlbumArt())
                .into(holder.albumArt);

        holder.albumBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClick.onAlbumClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return albumsList.size();
    }


    public class AlbumViewHolder extends RecyclerView.ViewHolder {
        TextView albumTitle;
        TextView artistTitle;
        ImageView albumArt;
        LinearLayout albumBox;

        public AlbumViewHolder(View itemView) {
            super(itemView);
            albumTitle = itemView.findViewById(R.id.albumTitle);
            artistTitle = itemView.findViewById(R.id.artistTitle);
            albumArt = itemView.findViewById(R.id.albumArt);
            albumBox = itemView.findViewById(R.id.albumBox);
        }
    }

    //make interface like this
    public interface OnAlbumClicked {
        void onAlbumClick(int position);
    }

    public void setOnClick(OnAlbumClicked onClick) {
        this.onClick = onClick;
    }
}
