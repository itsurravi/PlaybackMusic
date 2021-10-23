package com.ravisharma.playbackmusic.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.ravisharma.playbackmusic.MainActivity;
import com.ravisharma.playbackmusic.R;
import com.ravisharma.playbackmusic.equalizer.EqualizerFragment;

public class EqualizerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_equalizer);

        final int sessionId = MainActivity.Companion.getInstance().sessionId;
        EqualizerFragment equalizerFragment = EqualizerFragment.newBuilder()
                .setAccentColor(getResources().getColor(R.color.scrollThumb))
                .setAudioSessionId(sessionId)
                .build();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.eqFrame, equalizerFragment)
                .commit();
    }
}