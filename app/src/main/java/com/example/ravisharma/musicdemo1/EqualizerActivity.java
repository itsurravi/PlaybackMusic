package com.example.ravisharma.musicdemo1;

import android.media.audiofx.*;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;

public class EqualizerActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener,
        CompoundButton.OnCheckedChangeListener, View.OnClickListener {

    TextView bass_boost_label = null;
    SeekBar bass_boost = null;
    CheckBox enabled = null;
    Button flat = null;

    Equalizer eq = null;
    BassBoost bb = null;

    int min_level = 0;
    int max_level = 100;

    static final int MAX_SLIDERS = 8; // Must match the XML layout
    SeekBar sliders[] = new SeekBar[MAX_SLIDERS];
    TextView slider_labels[] = new TextView[MAX_SLIDERS];
    int num_sliders = 0;
    int sessionId = MainActivity.getInstance().musicSrv.player.getAudioSessionId();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_equalizer);

        flat = (Button)findViewById(R.id.flat);
        flat.setOnClickListener(EqualizerActivity.this);

        enabled = (CheckBox)findViewById(R.id.enabled);
        enabled.setOnCheckedChangeListener (this);
        bass_boost = (SeekBar)findViewById(R.id.bass_boost);
        bass_boost.setOnSeekBarChangeListener(this);
        bass_boost_label = (TextView) findViewById (R.id.bass_boost_label);

        sliders[0] = (SeekBar)findViewById(R.id.slider_1);
        slider_labels[0] = (TextView)findViewById(R.id.slider_label_1);
        sliders[1] = (SeekBar)findViewById(R.id.slider_2);
        slider_labels[1] = (TextView)findViewById(R.id.slider_label_2);
        sliders[2] = (SeekBar)findViewById(R.id.slider_3);
        slider_labels[2] = (TextView)findViewById(R.id.slider_label_3);
        sliders[3] = (SeekBar)findViewById(R.id.slider_4);
        slider_labels[3] = (TextView)findViewById(R.id.slider_label_4);
        sliders[4] = (SeekBar)findViewById(R.id.slider_5);
        slider_labels[4] = (TextView)findViewById(R.id.slider_label_5);
        sliders[5] = (SeekBar)findViewById(R.id.slider_6);
        slider_labels[5] = (TextView)findViewById(R.id.slider_label_6);
        sliders[6] = (SeekBar)findViewById(R.id.slider_7);
        slider_labels[6] = (TextView)findViewById(R.id.slider_label_7);
        sliders[7] = (SeekBar)findViewById(R.id.slider_8);
        slider_labels[7] = (TextView)findViewById(R.id.slider_label_8);

        eq = new Equalizer(0, sessionId);
        if (eq != null) {
            eq.setEnabled(true);
            int num_bands = eq.getNumberOfBands();
            num_sliders = num_bands;
            short r[] = eq.getBandLevelRange();
            min_level = r[0];
            max_level = r[1];
            for (int i = 0; i < num_sliders && i < MAX_SLIDERS; i++) {
                int[] freq_range = eq.getBandFreqRange((short)i);
                sliders[i].setOnSeekBarChangeListener(this);
                slider_labels[i].setText (formatBandLabel (freq_range));
            }
        }
        for (int i = num_sliders ; i < MAX_SLIDERS; i++) {
            sliders[i].setVisibility(View.GONE);
            slider_labels[i].setVisibility(View.GONE);
        }

        bb = new BassBoost (0, sessionId);
        if (bb != null)
        {
        }
        else {
            bass_boost.setVisibility(View.GONE);
            bass_boost_label.setVisibility(View.GONE);
        }

        updateUI();

    }

    public void updateUI () {
        updateSliders();
        updateBassBoost();
        enabled.setChecked (eq.getEnabled());
    }

    public void updateSliders () {
        for (int i = 0; i < num_sliders; i++)
        {
            int level;
            if (eq != null)
                level = eq.getBandLevel ((short)i);
            else
                level = 0;
            int pos = 100 * level / (max_level - min_level) + 50;
            sliders[i].setProgress (pos);
        }
    }

    public void updateBassBoost () {
        if (bb != null)
            bass_boost.setProgress (bb.getRoundedStrength());
        else
            bass_boost.setProgress (0);
    }

    public String formatBandLabel (int[] band) {
        return milliHzToString(band[0]) + "-" + milliHzToString(band[1]);
    }

    public String milliHzToString (int milliHz) {
        if (milliHz < 1000) return "";
        if (milliHz < 1000000)
            return "" + (milliHz / 1000) + "Hz";
        else
            return "" + (milliHz / 1000000) + "kHz";
    }

    public void setFlat () {
        if (eq != null)
        {
            for (int i = 0; i < num_sliders; i++)
            {
                eq.setBandLevel ((short)i, (short)0);
            }
        }

        updateUI();
    }

    @Override
    public void onClick(View v) {
        if (v ==  flat) {
            setFlat();
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView == enabled)
        {
            eq.setEnabled (isChecked);
            if(!isChecked){
                bb.setEnabled(false);
            }
            else{
                bb.setEnabled(true);
            }
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (seekBar == bass_boost)
        {
            bb.setEnabled (progress > 0 ? true : false);
            bb.setStrength ((short)progress); // Already in the right range 0-1000
        }
        else if (eq != null)
        {
            int new_level = min_level + (max_level - min_level) * progress / 100;

            for (int i = 0; i < num_sliders; i++)
            {
                if (sliders[i] == seekBar)
                {
                    eq.setBandLevel ((short)i, (short)new_level);
                    break;
                }
            }
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }


}
