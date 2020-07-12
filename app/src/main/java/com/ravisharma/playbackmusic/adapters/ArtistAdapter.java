package com.ravisharma.playbackmusic.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ravisharma.playbackmusic.model.Artist;
import com.ravisharma.playbackmusic.R;

import java.util.ArrayList;

public class ArtistAdapter extends RecyclerView.Adapter<ArtistAdapter.ArtistViewHolder> {

    Context c;
    private ArrayList<Artist> artistList;
    private OnArtistClicked onClick;

    public ArtistAdapter(Context c, ArrayList<Artist> artistList) {
        this.c = c;
        this.artistList = artistList;

    }

    public void setList(ArrayList<Artist> artistList) {
        this.artistList = artistList;
    }

    @NonNull
    @Override
    public ArtistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(c).inflate(R.layout.adapter_artist, parent, false);
        return new ArtistViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ArtistViewHolder holder, final int position) {
        Artist currArtist = artistList.get(position);

        holder.artistName.setText(currArtist.getArtistName());
        holder.numberOfSongs.setText(currArtist.getNumberOfTracks());

        holder.albumBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClick.onArtistClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return artistList.size();
    }



    public class ArtistViewHolder extends RecyclerView.ViewHolder {
        TextView artistName;
        TextView numberOfSongs;
        LinearLayout albumBox;

        public ArtistViewHolder(View itemView) {
            super(itemView);
            artistName = itemView.findViewById(R.id.artistTitle);
            numberOfSongs = itemView.findViewById(R.id.numberOfSongs);
            albumBox = itemView.findViewById(R.id.artistbox);
        }
    }

    //make interface like this
    public interface OnArtistClicked {
        void onArtistClick(int position);
    }

    public void setOnClick(OnArtistClicked onClick) {
        this.onClick = onClick;
    }
}
