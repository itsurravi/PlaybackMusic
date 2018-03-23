package com.example.ravisharma.musicdemo1;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Ravi Sharma on 02-Mar-18.
 */

public class Timer extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        MainActivity.getInstance().onDestroy();
        System.exit(0);
    }
}
