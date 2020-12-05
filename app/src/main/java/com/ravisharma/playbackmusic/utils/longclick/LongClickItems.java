package com.ravisharma.playbackmusic.utils.longclick;

import android.app.Activity;
import android.app.RecoverableSecurityException;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.ravisharma.playbackmusic.R;
import com.ravisharma.playbackmusic.activities.AddToPlaylistActivity;
import com.ravisharma.playbackmusic.activities.AlbumSongsActivity;
import com.ravisharma.playbackmusic.activities.ArtistSongsActivity;
import com.ravisharma.playbackmusic.activities.LastAndMostPlayed;
import com.ravisharma.playbackmusic.activities.NowPlayingActivity;
import com.ravisharma.playbackmusic.activities.RecentAddedActivity;
import com.ravisharma.playbackmusic.activities.SearchActivity;
import com.ravisharma.playbackmusic.model.Song;
import com.ravisharma.playbackmusic.provider.SongsProvider;
import com.ravisharma.playbackmusic.utils.UtilsKt;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class LongClickItems {
    private final Context context;
    private final ArrayList<Song> songList;
    private int position = -1;

    public LongClickItems(Context context, int position, ArrayList<Song> songList) {
        this.context = context;
        this.songList = songList;
        this.position = position;
        showDialog(position);
    }

    public LongClickItems(Context context, int position, ArrayList<Song> songList, String type) {
        this.context = context;
        this.songList = songList;
        this.position = position;
        showNowPlayingDialog(position);
    }

    private void showDialog(final int mposition) {
        String[] items = context.getResources().getStringArray(R.array.longPressItems);
        ArrayAdapter<String> ad = new ArrayAdapter<>(context, R.layout.adapter_alert_list, items);

        View v = LayoutInflater.from(context).inflate(R.layout.alert_list, null);

        ListView lv = v.findViewById(R.id.list);
        TextView tv = v.findViewById(R.id.title);
        ImageView songArt = v.findViewById(R.id.songArt);

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.placeholder(R.drawable.logo);
        requestOptions.error(R.drawable.logo);

        Glide.with(v)
                .setDefaultRequestOptions(requestOptions)
                .load(songList.get(mposition).getArt())
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(songArt);

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
                    case 0: {
                        itemClick(mposition);
                    }
                    break;
                    case 1: {
                        playSingleOnly(songList.get(mposition));
                    }
                    break;
                    case 2: {
                        UtilsKt.addNextSongToPlayingList(songList.get(mposition));
                    }
                    break;
                    case 3: {
                        UtilsKt.addSongToPlayingList(songList.get(mposition));
                    }
                    break;
                    case 4: {
                        Intent i = new Intent(context, AddToPlaylistActivity.class);
                        i.putExtra("Song", songList.get(mposition));
                        context.startActivity(i);
                    }
                    break;
                    case 5: {
                        //Delete Song Code
                        showDeleteSongDialog(songList.get(mposition));
                    }
                    break;
                    case 6: {
                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.setType("audio/*");
                        Uri uri = Uri.parse(songList.get(mposition).getData());
                        intent.putExtra(Intent.EXTRA_STREAM, uri);
                        context.startActivity(Intent.createChooser(intent, "Share Via"));
                    }
                    break;
                    case 7: {
                        songDetails(mposition);
                    }
                    break;
                }
                alertDialog.dismiss();
            }
        });
    }

    private void showNowPlayingDialog(final int mposition) {
        String[] items = context.getResources().getStringArray(R.array.longPressNowPlaying);
        ArrayAdapter<String> ad = new ArrayAdapter<>(context, R.layout.adapter_alert_list, items);

        View v = LayoutInflater.from(context).inflate(R.layout.alert_list, null);

        ListView lv = v.findViewById(R.id.list);
        TextView tv = v.findViewById(R.id.title);
        ImageView songArt = v.findViewById(R.id.songArt);

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.placeholder(R.drawable.logo);
        requestOptions.error(R.drawable.logo);

        Glide.with(v)
                .setDefaultRequestOptions(requestOptions)
                .load(songList.get(mposition).getArt())
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(songArt);


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
//                    case 2:
//                        //Delete Song Code
//                        showDeleteSongDialog(songList.get(mposition));
//                        break;
                    case 2:
                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.setType("audio/*");
                        Uri uri = Uri.parse(songList.get(mposition).getData());
                        intent.putExtra(Intent.EXTRA_STREAM, uri);
                        context.startActivity(Intent.createChooser(intent, "Share Via"));
                        break;
                    case 3:
                        songDetails(mposition);
                        break;
                }
                alertDialog.dismiss();
            }
        });
    }

    private void showDeleteSongDialog(final Song song) {
        AlertDialog.Builder b = new AlertDialog.Builder(context, R.style.AlertDialogCustom);
        b.setTitle(context.getString(R.string.deleteMessage));
        b.setMessage(song.getTitle());
        b.setPositiveButton(context.getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (SongsProvider.Companion.getSongListByName().getValue().size() == 1) {
                    Toast.makeText(context, "Can't Delete Last Song", Toast.LENGTH_SHORT).show();
                    return;
                }
                Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                String[] projection = {MediaStore.Audio.Media._ID};
                String selection = MediaStore.Audio.Media.DATA + " = ?";
                String[] selectionArgs = new String[]{song.getData()};
                Cursor musicCursor = context.getContentResolver().query(musicUri, projection,
                        selection, selectionArgs, null);

                if (musicCursor.moveToFirst()) {
                    long id = musicCursor.getLong(musicCursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
                    Uri deleteUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
                    try {
                        File fdelete = new File(selectionArgs[0]);
                        Log.d("ERRORDELETION", "" + deleteUri);
                        if (fdelete.exists()) {
                            context.getContentResolver().delete(deleteUri, null, null);
                            if (position != 1) {
                                updateList(position);
                                position = -1;
                            }
                            Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            RecoverableSecurityException recoverableSecurityException;
                            if (e instanceof RecoverableSecurityException) {
                                recoverableSecurityException = (RecoverableSecurityException) e;
                                IntentSender intentSender = recoverableSecurityException.getUserAction()
                                        .getActionIntent().getIntentSender();
                                try {
                                    UtilsKt.setDeleteUri(deleteUri);
                                    ((Activity) context).startIntentSenderForResult(intentSender, 20123,
                                            null, 0, 0, 0, null);
                                } catch (IntentSender.SendIntentException ex) {
                                    ex.printStackTrace();
                                }
                            } else {
                                Toast.makeText(context, "Can't Delete. Try Manually", Toast.LENGTH_SHORT).show();
                            }
                        }
                        Log.d("ExceptionProblem", e.toString());
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

    private void playSingleOnly(Song song) {
        ArrayList<Song> list = new ArrayList<>();
        list.add(song);

        if (context instanceof RecentAddedActivity) {
            ((RecentAddedActivity) context).onItemClick(list);
        } else if (context instanceof ArtistSongsActivity) {
            ((ArtistSongsActivity) context).onItemClick(list);
        } else if (context instanceof AlbumSongsActivity) {
            ((AlbumSongsActivity) context).onItemClick(list);
        } else if (context instanceof SearchActivity) {
            ((SearchActivity) context).onItemClick(list);
        }else if (context instanceof LastAndMostPlayed) {
            ((LastAndMostPlayed) context).onItemClick(list);
        }
    }

    private void updateList(int mposition) {

        if (context instanceof RecentAddedActivity) {
//            ((RecentAddedActivity) context).updateList(mposition);
        } else if (context instanceof ArtistSongsActivity) {
            ((ArtistSongsActivity) context).updateList(mposition);
        } else if (context instanceof AlbumSongsActivity) {
            ((AlbumSongsActivity) context).updateList(mposition);
        } else if (context instanceof NowPlayingActivity) {
            ((NowPlayingActivity) context).updateList(mposition);
        } else if (context instanceof SearchActivity) {
//            ((SearchActivity) context).updateList(mposition);
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
