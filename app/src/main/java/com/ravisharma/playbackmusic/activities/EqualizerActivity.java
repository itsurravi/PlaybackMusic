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

        final int sessionId = MainActivity.getInstance().sessionId;
        EqualizerFragment equalizerFragment = EqualizerFragment.newBuilder()
                .setAccentColor(getResources().getColor(R.color.scrollThumb))
                .setAudioSessionId(sessionId)
                .build();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.eqFrame, equalizerFragment)
                .commit();
    }

//    private void saveEqualizerSettings(){
//        if (Settings.equalizerModel != null){
//            EqualizerSettings settings = new EqualizerSettings();
//            settings.bassStrength = Settings.equalizerModel.getBassStrength();
//            settings.presetPos = Settings.equalizerModel.getPresetPos();
//            settings.reverbPreset = Settings.equalizerModel.getReverbPreset();
//            settings.seekbarpos = Settings.equalizerModel.getSeekbarpos();
//
//            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
//
//            Gson gson = new Gson();
//            preferences.edit()
//                    .putString(PREF_KEY, gson.toJson(settings))
//                    .apply();
//        }
//    }
//
//    private void loadEqualizerSettings(){
//        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
//
//        Gson gson = new Gson();
//        EqualizerSettings settings = gson.fromJson(preferences.getString(PREF_KEY, "{}"), EqualizerSettings.class);
//        EqualizerModel model = new EqualizerModel();
//        model.setBassStrength(settings.bassStrength);
//        model.setPresetPos(settings.presetPos);
//        model.setReverbPreset(settings.reverbPreset);
//        model.setSeekbarpos(settings.seekbarpos);
//
//        Settings.isEqualizerEnabled = true;
//        Settings.isEqualizerReloaded = true;
//        Settings.bassStrength = settings.bassStrength;
//        Settings.presetPos = settings.presetPos;
//        Settings.reverbPreset = settings.reverbPreset;
//        Settings.seekbarpos = settings.seekbarpos;
//        Settings.equalizerModel = model;
//    }
}