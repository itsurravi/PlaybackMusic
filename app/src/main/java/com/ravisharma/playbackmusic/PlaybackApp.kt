package com.ravisharma.playbackmusic;

import android.app.Application;

import com.google.android.gms.ads.MobileAds;

import dagger.hilt.android.HiltAndroidApp;

@HiltAndroidApp
public class PlaybackApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        MobileAds.initialize(this, initializationStatus -> {

        });
    }
}
