package com.ravisharma.playbackmusic.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ravisharma.playbackmusic.MainActivity;

/**
 * Created by Ravi Sharma on 02-Mar-18.
 */

public class Timer extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (MainActivity.Companion.getInstance() != null) {
            MainActivity.Companion.getInstance().onDestroy();
        }
        System.exit(0);
    }
}
