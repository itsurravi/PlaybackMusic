package com.ravisharma.playbackmusic.Broadcast;

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
        String as = intent.getExtras().getString(context.getString(R.string.doit));

        if(as.equals(context.getString(R.string.prev))){
            MainActivity.getInstance().playPrev();
        }
        else if(as.equals(context.getString(R.string.playPause))){
            MainActivity.getInstance().btnplaypause();
        }
        else if(as.equals(context.getString(R.string.next))){
            MainActivity.getInstance().playNext();
        }
        /*else if(as.equals("close")){

            MainActivity.getInstance().onDestroy();
            MainActivity.getInstance().finish();
        }*/

    }
}