package com.ravisharma.playbackmusic.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.ravisharma.playbackmusic.BuildConfig;
import com.ravisharma.playbackmusic.R;

public class AboutActivity extends AppCompatActivity {

    TextView appVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        appVersion = findViewById(R.id.appVersion);
        String appVer = BuildConfig.VERSION_NAME;

        appVersion.setText("v".concat(appVer));
    }

    public void finishPage(View view) {
        finish();
    }
}