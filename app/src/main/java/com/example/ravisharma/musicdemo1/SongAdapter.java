package com.example.ravisharma.musicdemo1;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import android.content.Context;
import android.view.LayoutInflater;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.RelativeLayout;
import android.widget.TextView;


/**
 * Created by Ravi Sharma on 07-Jan-18.
 */

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.ViewHolder> implements Filterable{

    private ArrayList<Song> songs;
    private ArrayList<Song> srchSongs;

    //declare interface
    private OnItemClicked onClick;
    private OnItemLongClicked onLongClick;

    //make interface like this
    public interface OnItemClicked {
        void onItemClick(int position);
    }

    public interface OnItemLongClicked {
        void onItemLongClick(int position);
    }

    public SongAdapter(ArrayList<Song> theSongs){
        songs=theSongs;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView artist;
        TextView durView;
        RelativeLayout songbox;

        public ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.song_title);
            artist = itemView.findViewById(R.id.song_artist);
            durView = itemView.findViewById(R.id.duration);
            songbox = itemView.findViewById(R.id.song_box);
        }
    }

    @Override
    public SongAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.song, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SongAdapter.ViewHolder holder, final int position) {
        Song currSong = songs.get(position);
        TextView title = holder.title;
        TextView artist = holder.artist;
        TextView duration = holder.durView;

        title.setText(currSong.getTitle());
        artist.setText(currSong.getArtist());
        duration.setText((String.format("%d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(currSong.getDuration()),
                TimeUnit.MILLISECONDS.toSeconds(currSong.getDuration()) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(currSong.getDuration())))));
        holder.songbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClick.onItemClick(position);
            }
        });

        holder.songbox.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                onLongClick.onItemLongClick(position);
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                final FilterResults oReturn = new FilterResults();
                final ArrayList<Song> results = new ArrayList<Song>();
                if(srchSongs==null){
                    srchSongs=songs;
                }
                if(constraint!=null){
                    if(srchSongs!=null && srchSongs.size()>0){
                        for(final Song song : srchSongs){
                            if(song.getTitle().toLowerCase().contains(constraint.toString())
                                    || song.getArtist().toLowerCase().contains(constraint.toString()))
                                results.add(song);
                        }
                    }
                    oReturn.values = results;
                }
                return oReturn;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                songs = (ArrayList<Song>) results.values;
                MainActivity.getInstance().musicSrv.setList(songs);
                MainActivity.getInstance().putSongList(songs);
                notifyDataSetChanged();
            }
        };
    }

    public void setOnClick(OnItemClicked onClick)
    {
        this.onClick=onClick;
    }

    public void setOnLongClick(OnItemLongClicked onLongClick) {
        this.onLongClick=onLongClick;
    }
}