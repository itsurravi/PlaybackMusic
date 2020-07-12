package com.ravisharma.playbackmusic.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ravisharma.playbackmusic.R;

import java.util.List;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.PlaylistHolder> {

    private Context context;
    private List<String> playlistArrayList;
    private OnPlaylistClicked onClicked;
    private OnPlaylistLongClicked onLongClicked;

    public PlaylistAdapter(Context context, List<String> playlistArrayList) {
        this.context = context;
        this.playlistArrayList = playlistArrayList;
    }

    @NonNull
    @Override
    public PlaylistHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.adapter_playlist, parent, false);

        return new PlaylistHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistHolder holder, final int position) {
        String playList = playlistArrayList.get(position);

        holder.playlistName.setText(playList);

        if(position==0){
            holder.playlistIcon.setImageResource(R.drawable.ic_recent_timer);
        }
        else if(position ==1){
            holder.playlistIcon.setImageResource(R.drawable.ic_favtrack_playlist);
        }
        else{
            holder.playlistIcon.setImageResource(R.drawable.ic_created_playlist);
        }

        holder.playlistBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClicked.onPlaylistClick(position);
            }
        });

        holder.playlistBox.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                onLongClicked.onPlaylistLongClick(position);
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return playlistArrayList.size();
    }

    class PlaylistHolder extends RecyclerView.ViewHolder {

        ImageView playlistIcon;
        TextView playlistName;
        LinearLayout playlistBox;

        public PlaylistHolder(@NonNull View itemView) {
            super(itemView);
            playlistIcon = itemView.findViewById(R.id.playlistIcon);
            playlistBox = itemView.findViewById(R.id.playlistBox);
            playlistName = itemView.findViewById(R.id.playlistTitle);
        }
    }

    //make interface like this
    public interface OnPlaylistClicked {
        void onPlaylistClick(int position);
    }

    public interface OnPlaylistLongClicked {
        void onPlaylistLongClick(int position);
    }

    public void setOnPlaylistClick(OnPlaylistClicked onClick) {
        this.onClicked = onClick;
    }

    public void setOnPlaylistLongClick(OnPlaylistLongClicked onClick) {
        this.onLongClicked = onClick;
    }
}
