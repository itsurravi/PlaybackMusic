package com.ravisharma.playbackmusic.Broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ravisharma.playbackmusic.MainActivity;
import com.ravisharma.playbackmusic.R;

/**
 * Created by Ravi Sharma on 02-Mar-18.
 */

public class Timer extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (MainActivity.getInstance() != null) {
            MainActivity.getInstance().onDestroy();
        }
        System.exit(0);
    }
}
