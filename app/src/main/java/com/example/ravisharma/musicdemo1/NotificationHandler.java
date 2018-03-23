package com.example.ravisharma.musicdemo1;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

/**
 * Created by Ravi Sharma on 12-Feb-18.
 */
public class NotificationHandler extends BroadcastReceiver {

    protected MainActivity act;

    @Override
    public void onReceive(Context context, Intent intent) {
        act = new MainActivity();
        String as = intent.getExtras().getString("do");

        if(as.equals("prev")){
            MainActivity.getInstance().playPrev();
        }
        else if(as.equals("playPause")){
            MainActivity.getInstance().btnplaypause();
        }
        else if(as.equals("next")){
            MainActivity.getInstance().playNext();
        }
        else if(as.equals("close")){
            MainActivity.getInstance().onDestroy();
            System.exit(0);
        }

    }
}