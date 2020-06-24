package com.ravisharma.playbackmusic;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.ravisharma.playbackmusic.Activities.AddToPlaylistActivity;
import com.ravisharma.playbackmusic.Activities.AlbumSongsActivity;
import com.ravisharma.playbackmusic.Activities.ArtistSongsActivity;
import com.ravisharma.playbackmusic.Activities.NowPlayingActivity;
import com.ravisharma.playbackmusic.Activities.RecentAddedActivity;
import com.ravisharma.playbackmusic.Activities.SearchActivity;
import com.ravisharma.playbackmusic.Model.Song;
import com.ravisharma.playbackmusic.Provider.Provider;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class LongClickItems {
    private Context context;
    private ArrayList<Song> songList;

    public LongClickItems(Context context, int position, ArrayList<Song> songList) {
        this.context = context;
        this.songList = songList;
        showDialog(position);
    }

    public LongClickItems(Context context, int position, ArrayList<Song> songList, String type) {
        this.context = context;
        this.songList = songList;
        showNowPlayingDialog(position);
    }

    private void showDialog(final int mposition) {
        String[] items = context.getResources().getStringArray(R.array.longPressItems);
        ArrayAdapter<String> ad = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, items);

        View v = LayoutInflater.from(context).inflate(R.layout.alert_list, null);

        ListView lv = v.findViewById(R.id.list);
        TextView tv = v.findViewById(R.id.title);
        tv.setText(songList.get(mposition).getTitle());
        lv.setAdapter(ad);

        final AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setView(v);

        final AlertDialog alertDialog = dialog.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        alertDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation_2;
        alertDialog.show();


        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        itemClick(mposition);
                        break;
                    case 1:
                        MainActivity.getInstance().addNextSong(songList.get(mposition));
                        break;
                    case 2:
                        MainActivity.getInstance().addToQueue(songList.get(mposition));
                        break;
                    case 3:
                        Intent i = new Intent(context, AddToPlaylistActivity.class);
                        i.putExtra("Song", songList.get(mposition));
                        context.startActivity(i);
                        break;
                    case 4:
                        //Delete Song Code
                        AlertDialog.Builder b = new AlertDialog.Builder(context);
                        b.setTitle(context.getString(R.string.deleteMessage));
                        b.setMessage(songList.get(mposition).getTitle());
                        b.setPositiveButton(context.getString(R.string.yes), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                                String[] projection = {MediaStore.Audio.Media._ID};
                                String selection = MediaStore.Audio.Media.DATA + " = ?";
                                String[] selectionArgs = new String[]{songList.get(mposition).getData()};
                                Cursor musicCursor = context.getContentResolver().query(musicUri, projection,
                                        selection, selectionArgs, null);

                                if (musicCursor.moveToFirst()) {
                                    try {

                                        long id = musicCursor.getLong(musicCursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
                                        Uri deleteUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);

                                        File fdelete = new File(selectionArgs[0]);

                                        if (fdelete.exists()) {
                                            if (fdelete.delete()) {
                                                if (MainActivity.getInstance().getPlayingSong() == songList.get(mposition)) {
                                                    MainActivity.getInstance().songList.remove(mposition);
                                                    MainActivity.getInstance().setServiceList();
                                                    MainActivity.getInstance().playNext();
                                                } else if (MainActivity.getInstance().songList.contains(songList.get(mposition))) {
                                                    MainActivity.getInstance().songList.remove(songList.get(mposition));
                                                    MainActivity.getInstance().setServiceList();
                                                }
                                                context.getContentResolver().delete(deleteUri, null, null);
                                                Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show();
                                                Provider provider = new Provider(MainActivity.getInstance());
                                                provider.execute();
                                                updateList(mposition);
                                            } else {
                                                Toast.makeText(context, "Can't Delete", Toast.LENGTH_SHORT).show();
                                            }
                                        }

                                    } catch (Exception e) {
                                        Toast.makeText(context, "Can't Delete. Try Manually", Toast.LENGTH_SHORT).show();
                                    }

                                } else {
                                    Toast.makeText(context, "Can't Delete. Try Manually", Toast.LENGTH_SHORT).show();
                                }
                                musicCursor.close();
                                dialog.dismiss();
                            }
                        }).setNegativeButton(context.getString(R.string.no), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                        AlertDialog d = b.create();
                        d.show();
                        break;
                    case 5:
                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.setType("audio/*");
                        Uri uri = Uri.parse(songList.get(mposition).getData());
                        intent.putExtra(Intent.EXTRA_STREAM, uri);
                        context.startActivity(Intent.createChooser(intent, "Share Via"));
                        break;
                    case 6:
                        songDetails(mposition);
                        break;
                }
                alertDialog.dismiss();
            }
        });
    }

    private void showNowPlayingDialog(final int mposition) {
        String[] items = context.getResources().getStringArray(R.array.longPressNowPlaying);
        ArrayAdapter<String> ad = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, items);

        View v = LayoutInflater.from(context).inflate(R.layout.alert_list, null);

        ListView lv = v.findViewById(R.id.list);
        TextView tv = v.findViewById(R.id.title);
        tv.setText(songList.get(mposition).getTitle());
        lv.setAdapter(ad);

        final AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setView(v);

        final AlertDialog alertDialog = dialog.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        alertDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation_2;
        alertDialog.show();


        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        itemClick(mposition);
                        break;
                    case 1:
                        Intent i = new Intent(context, AddToPlaylistActivity.class);
                        i.putExtra("Song", songList.get(mposition));
                        context.startActivity(i);
                        break;
                    case 2:
                        //Delete Song Code
                        AlertDialog.Builder b = new AlertDialog.Builder(context);
                        b.setTitle(context.getString(R.string.deleteMessage));
                        b.setMessage(songList.get(mposition).getTitle());
                        b.setPositiveButton(context.getString(R.string.yes), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                                String[] projection = {MediaStore.Audio.Media._ID};
                                String selection = MediaStore.Audio.Media.DATA + " = ?";
                                String[] selectionArgs = new String[]{songList.get(mposition).getData()};
                                Cursor musicCursor = context.getContentResolver().query(musicUri, projection,
                                        selection, selectionArgs, null);

                                if (musicCursor.moveToFirst()) {
                                    try {

                                        long id = musicCursor.getLong(musicCursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
                                        Uri deleteUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);

                                        File fdelete = new File(selectionArgs[0]);

                                        if (fdelete.exists()) {
                                            if (fdelete.delete()) {
                                                if (MainActivity.getInstance().getPlayingSong() == songList.get(mposition)) {
                                                    MainActivity.getInstance().songList.remove(mposition);
                                                    MainActivity.getInstance().setServiceList();
                                                    MainActivity.getInstance().playNext();
                                                } else if (MainActivity.getInstance().songList.contains(songList.get(mposition))) {
                                                    MainActivity.getInstance().songList.remove(songList.get(mposition));
                                                    MainActivity.getInstance().setServiceList();
                                                }
                                                context.getContentResolver().delete(deleteUri, null, null);
                                                Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show();
                                                Provider provider = new Provider(MainActivity.getInstance());
                                                provider.execute();
                                                updateList(mposition);
                                            } else {
                                                Toast.makeText(context, "Can't Delete", Toast.LENGTH_SHORT).show();
                                            }
                                        }

                                    } catch (Exception e) {
                                        Toast.makeText(context, "Can't Delete. Try Manually", Toast.LENGTH_SHORT).show();
                                    }

                                } else {
                                    Toast.makeText(context, "Can't Delete. Try Manually", Toast.LENGTH_SHORT).show();
                                }
                                musicCursor.close();
                                dialog.dismiss();
                            }
                        }).setNegativeButton(context.getString(R.string.no), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                        AlertDialog d = b.create();
                        d.show();
                        break;
                    case 3:
                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.setType("audio/*");
                        Uri uri = Uri.parse(songList.get(mposition).getData());
                        intent.putExtra(Intent.EXTRA_STREAM, uri);
                        context.startActivity(Intent.createChooser(intent, "Share Via"));
                        break;
                    case 4:
                        songDetails(mposition);
                        break;
                }
                alertDialog.dismiss();
            }
        });
    }

    private void itemClick(int position) {
        if (context instanceof RecentAddedActivity) {
            ((RecentAddedActivity) context).onItemClick(position);
        } else if (context instanceof ArtistSongsActivity) {
            ((ArtistSongsActivity) context).onItemClick(position);
        } else if (context instanceof AlbumSongsActivity) {
            ((AlbumSongsActivity) context).onItemClick(position);
        } else if (context instanceof NowPlayingActivity) {
            ((NowPlayingActivity) context).onItemClick(position);
        } else if (context instanceof SearchActivity) {
            ((SearchActivity) context).onItemClick(position);
        }
    }

    private void updateList(int mposition) {

        if (context instanceof RecentAddedActivity) {
            ((RecentAddedActivity) context).updateList(mposition);
        } else if (context instanceof ArtistSongsActivity) {
            ((ArtistSongsActivity) context).updateList(mposition);
        } else if (context instanceof AlbumSongsActivity) {
            ((AlbumSongsActivity) context).updateList(mposition);
        } else if (context instanceof NowPlayingActivity) {
            ((NowPlayingActivity) context).updateList(mposition);
        } else if (context instanceof SearchActivity) {
            ((SearchActivity) context).updateList(mposition);
        }
    }

    private void songDetails(int pos) {
        View v = LayoutInflater.from(context).inflate(R.layout.info, null);
        TextView title, artist, album, composer, duration, location;
        title = v.findViewById(R.id.info_title);
        artist = v.findViewById(R.id.info_artist);
        album = v.findViewById(R.id.info_album);
        composer = v.findViewById(R.id.info_composer);
        duration = v.findViewById(R.id.info_duration);
        location = v.findViewById(R.id.info_location);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(v);

        title.setText(songList.get(pos).getTitle());
        artist.setText(songList.get(pos).getArtist());
        album.setText(songList.get(pos).getAlbum());
        composer.setText(songList.get(pos).getComposer());
        duration.setText((String.format("%d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(songList.get(pos).getDuration()),
                TimeUnit.MILLISECONDS.toSeconds(songList.get(pos).getDuration()) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(songList.get(pos).getDuration())))));
        location.setText(songList.get(pos).getData());

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation_2;
        dialog.show();
    }
}
