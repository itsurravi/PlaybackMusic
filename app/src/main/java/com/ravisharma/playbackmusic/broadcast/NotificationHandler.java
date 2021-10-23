package com.ravisharma.playbackmusic.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ravisharma.playbackmusic.MainActivity;
import com.ravisharma.playbackmusic.R;

/**
 * Created by Ravi Sharma on 12-Feb-18.
 */
public class NotificationHandler extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && intent.getExtras() != null && MainActivity.Companion.getInstance() != null) {
            String as = intent.getExtras().getString(context.getString(R.string.doit));

            if (as.equals(context.getString(R.string.prev))) {
                MainActivity.Companion.getInstance().playPrev();
            } else if (as.equals(context.getString(R.string.playPause))) {
                MainActivity.Companion.getInstance().btnPlayPause();
            } else if (as.equals(context.getString(R.string.next))) {
                MainActivity.Companion.getInstance().playNext();
            } else if (as.equals(context.getString(R.string.favorite))) {
                MainActivity.Companion.getInstance().addToFavPlaylist();
            } else if (as.equals(context.getString(R.string.close))) {
                MainActivity.Companion.getInstance().stopApp();
            }
        }
    }
}