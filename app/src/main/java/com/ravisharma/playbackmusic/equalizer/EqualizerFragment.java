package com.ravisharma.playbackmusic.equalizer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.ravisharma.playbackmusic.MainActivity;
import com.ravisharma.playbackmusic.R;
import com.ravisharma.playbackmusic.equalizer.model.Settings;

import java.util.ArrayList;

public class EqualizerFragment extends Fragment {

    ImageView backBtn;
    TextView fragTitle;
    SwitchCompat equalizerSwitch;

    int y = 0;

    private ImageView spinnerPresetDropDownIcon, spinnerRevDropDownIcon;

    short numberOfFrequencyBands;
    LinearLayout mLinearLayout;

    SeekBar[] seekBarFinal = new SeekBar[5];

    AnalogController bassController, virtualizerController;

    Spinner presetSpinner, revSpinner;

    FrameLayout equalizerBlocker;

    Context ctx;

    static int themeColor = Color.parseColor("#B24242");

    public EqualizerFragment() {
        // Required empty public constructor
    }

    public static EqualizerFragment newInstance() {
        EqualizerFragment fragment = new EqualizerFragment();
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Settings.isEditing = true;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ctx = context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_equalizer, container, false);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        backBtn = view.findViewById(R.id.equalizer_back_btn);
        fragTitle = view.findViewById(R.id.equalizer_fragment_title);
        equalizerSwitch = view.findViewById(R.id.equalizer_switch);
        spinnerPresetDropDownIcon = view.findViewById(R.id.spinner_preset_dropdown_icon);
        spinnerRevDropDownIcon = view.findViewById(R.id.spinner_rev_dropdown_icon);
        presetSpinner = view.findViewById(R.id.equalizer_preset_spinner);
        revSpinner = view.findViewById(R.id.equalizer_rev_spinner);
        equalizerBlocker = view.findViewById(R.id.equalizerBlocker);
        bassController = view.findViewById(R.id.controllerBass);
        virtualizerController = view.findViewById(R.id.controller3D);
        mLinearLayout = view.findViewById(R.id.equalizerContainer);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null) {
                    getActivity().onBackPressed();
                }
            }
        });

        equalizerSwitch.setChecked(Settings.isEqualizerEnabled);

        equalizerSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Settings.isEqualizerEnabled = isChecked;
                Settings.isEqualizerReloaded = isChecked;
                Settings.equalizerModel.setEqualizerEnabled(isChecked);
                Settings.equalizerModel.setEqualizerReloaded(isChecked);

                MainActivity.Companion.getInstance().mEqualizer.setEnabled(isChecked);
                MainActivity.Companion.getInstance().bassBoost.setEnabled(isChecked);
                MainActivity.Companion.getInstance().virtualizer.setEnabled(isChecked);
                MainActivity.Companion.getInstance().presetReverb.setEnabled(isChecked);

                if (isChecked) {
                    equalizerBlocker.setVisibility(View.INVISIBLE);
                } else {
                    equalizerBlocker.setVisibility(View.VISIBLE);
                }
            }
        });

        spinnerPresetDropDownIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presetSpinner.performClick();
            }
        });

        spinnerRevDropDownIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                revSpinner.performClick();
            }
        });

        if (equalizerSwitch.isChecked()) {
            equalizerBlocker.setVisibility(View.INVISIBLE);
        } else {
            equalizerBlocker.setVisibility(View.VISIBLE);
        }

        bassController.setLabel("BASS");
        virtualizerController.setLabel("3D");

        bassController.circlePaint2.setColor(themeColor);
        bassController.linePaint.setColor(getResources().getColor(R.color.white));
        bassController.invalidate();

        virtualizerController.circlePaint2.setColor(themeColor);
        virtualizerController.linePaint.setColor(getResources().getColor(R.color.white));
        virtualizerController.invalidate();

        int x;
        if (!Settings.isEqualizerReloaded) {
            x = 0;
            if (MainActivity.Companion.getInstance().bassBoost != null) {
                try {
                    x = ((MainActivity.Companion.getInstance().bassBoost.getRoundedStrength() * 19) / 1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (MainActivity.Companion.getInstance().virtualizer != null) {
                try {
                    y = ((MainActivity.Companion.getInstance().virtualizer.getRoundedStrength() * 19) / 1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        } else {
            x = ((Settings.bassStrength * 19) / 1000);
            y = ((Settings.virtualizerStrength * 19) / 1000);

        }
        if (x == 0) {
            bassController.setProgress(1);
        } else {
            bassController.setProgress(x);
        }
        if (y == 0) {
            virtualizerController.setProgress(1);
        } else {
            virtualizerController.setProgress(y);
        }

        bassController.setOnProgressChangedListener(new AnalogController.onProgressChangedListener() {
            @Override
            public void onProgressChanged(int progress) {
                Settings.bassStrength = (short) (((float) 1000 / 19) * (progress));
                if (Settings.bassStrength < 1000) {
                    Settings.bassStrength += 1;
                }
                try {
                    Settings.equalizerModel.setBassStrength(Settings.bassStrength);
                    MainActivity.Companion.getInstance().bassBoost.setStrength(Settings.bassStrength);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        virtualizerController.setOnProgressChangedListener(new AnalogController.onProgressChangedListener() {
            @Override
            public void onProgressChanged(int progress) {
                Settings.virtualizerStrength = (short) (((float) 1000 / 19) * (progress));
                if (Settings.virtualizerStrength < 1000) {
                    Settings.virtualizerStrength += 1;
                }
                try {
                    Settings.equalizerModel.setVirtualizerStrength(Settings.virtualizerStrength);
                    MainActivity.Companion.getInstance().virtualizer.setStrength(Settings.virtualizerStrength);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                y = progress;
            }
        });

        TextView equalizerHeading = new TextView(getContext());
        equalizerHeading.setText(R.string.eq);
        equalizerHeading.setTextSize(20);
        equalizerHeading.setGravity(Gravity.CENTER_HORIZONTAL);

        numberOfFrequencyBands = 5;

        final short lowerEqualizerBandLevel = MainActivity.Companion.getInstance().mEqualizer.getBandLevelRange()[0];
        final short upperEqualizerBandLevel = MainActivity.Companion.getInstance().mEqualizer.getBandLevelRange()[1];

        for (short i = 0; i < numberOfFrequencyBands; i++) {

            final short equalizerBandIndex = i;
            final TextView frequencyHeaderTextView = new TextView(getContext());
            frequencyHeaderTextView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            frequencyHeaderTextView.setGravity(Gravity.CENTER_HORIZONTAL);
            frequencyHeaderTextView.setTextColor(Color.parseColor("#FFFFFF"));
            frequencyHeaderTextView.setText((MainActivity.Companion.getInstance().mEqualizer.getCenterFreq(equalizerBandIndex) / 1000) + "Hz");

            LinearLayout seekBarRowLayout = new LinearLayout(getContext());
            seekBarRowLayout.setOrientation(LinearLayout.VERTICAL);

            TextView lowerEqualizerBandLevelTextView = new TextView(getContext());
            lowerEqualizerBandLevelTextView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            ));
            lowerEqualizerBandLevelTextView.setTextColor(Color.parseColor("#FFFFFF"));
            lowerEqualizerBandLevelTextView.setText((lowerEqualizerBandLevel / 100) + "dB");

            TextView upperEqualizerBandLevelTextView = new TextView(getContext());
            lowerEqualizerBandLevelTextView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            upperEqualizerBandLevelTextView.setTextColor(Color.parseColor("#FFFFFF"));
            upperEqualizerBandLevelTextView.setText((upperEqualizerBandLevel / 100) + "dB");

            SeekBar seekBar = new SeekBar(getContext());
            TextView textView = new TextView(getContext());
            switch (i) {
                case 0:
                    seekBar = view.findViewById(R.id.seekBar1);
                    textView = view.findViewById(R.id.textView1);
                    break;
                case 1:
                    seekBar = view.findViewById(R.id.seekBar2);
                    textView = view.findViewById(R.id.textView2);
                    break;
                case 2:
                    seekBar = view.findViewById(R.id.seekBar3);
                    textView = view.findViewById(R.id.textView3);
                    break;
                case 3:
                    seekBar = view.findViewById(R.id.seekBar4);
                    textView = view.findViewById(R.id.textView4);
                    break;
                case 4:
                    seekBar = view.findViewById(R.id.seekBar5);
                    textView = view.findViewById(R.id.textView5);
                    break;
            }
            seekBarFinal[i] = seekBar;
            seekBar.getProgressDrawable().setColorFilter(new PorterDuffColorFilter(themeColor, PorterDuff.Mode.SRC_IN));
            seekBar.getThumb().setColorFilter(new PorterDuffColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_IN));
            seekBar.setId(i);
            seekBar.setMax(upperEqualizerBandLevel - lowerEqualizerBandLevel);

            textView.setText(frequencyHeaderTextView.getText());
            textView.setTextColor(Color.WHITE);
            textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

            if (Settings.isEqualizerReloaded) {
                seekBar.setProgress(Settings.seekbarpos[i] - lowerEqualizerBandLevel);
            } else {
                seekBar.setProgress(MainActivity.Companion.getInstance().mEqualizer.getBandLevel(equalizerBandIndex) - lowerEqualizerBandLevel);
                Settings.seekbarpos[i] = MainActivity.Companion.getInstance().mEqualizer.getBandLevel(equalizerBandIndex);
                Settings.isEqualizerReloaded = true;
            }

            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    MainActivity.Companion.getInstance().mEqualizer.setBandLevel(equalizerBandIndex, (short) (progress + lowerEqualizerBandLevel));
                    Settings.seekbarpos[seekBar.getId()] = (progress + lowerEqualizerBandLevel);
                    Settings.equalizerModel.getSeekbarpos()[seekBar.getId()] = (progress + lowerEqualizerBandLevel);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    presetSpinner.setSelection(0);
                    Settings.presetPos = 0;
                    Settings.equalizerModel.setPresetPos(0);
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
        }

        setupPresetSpinner();
        setupRevSpinner();
    }

    public void setupPresetSpinner() {
        ArrayList<String> equalizerPresetNames = new ArrayList<>();
        ArrayAdapter<String> equalizerPresetSpinnerAdapter = new ArrayAdapter<>(ctx,
                R.layout.spinner_item,
                equalizerPresetNames);
        equalizerPresetSpinnerAdapter.setDropDownViewResource(R.layout.spinner_drop_down_item);
        equalizerPresetNames.add("Custom");

        for (short i = 0; i < MainActivity.Companion.getInstance().mEqualizer.getNumberOfPresets(); i++) {
            equalizerPresetNames.add(MainActivity.Companion.getInstance().mEqualizer.getPresetName(i));
        }

        presetSpinner.setAdapter(equalizerPresetSpinnerAdapter);

        if (Settings.isEqualizerReloaded && Settings.presetPos != 0) {

            presetSpinner.setSelection(Settings.presetPos);
        }

        presetSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try {
                    if (position != 0) {
                        MainActivity.Companion.getInstance().mEqualizer.usePreset((short) (position - 1));
                        Settings.presetPos = position;
                        short numberOfFreqBands = 5;

                        final short lowerEqualizerBandLevel = MainActivity.Companion.getInstance().mEqualizer.getBandLevelRange()[0];

                        for (short i = 0; i < numberOfFreqBands; i++) {
                            seekBarFinal[i].setProgress(MainActivity.Companion.getInstance().mEqualizer.getBandLevel(i) - lowerEqualizerBandLevel);
                            Settings.seekbarpos[i] = MainActivity.Companion.getInstance().mEqualizer.getBandLevel(i);
                            Settings.equalizerModel.getSeekbarpos()[i] = MainActivity.Companion.getInstance().mEqualizer.getBandLevel(i);
                        }
                    }
                } catch (Exception e) {
                    Toast.makeText(ctx, "Error while updating Equalizer", Toast.LENGTH_SHORT).show();
                }
                Settings.equalizerModel.setPresetPos(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void setupRevSpinner() {
        ArrayList<String> equalizerRevNames = new ArrayList<>();
        ArrayAdapter<String> equalizerRevSpinnerAdapter = new ArrayAdapter<>(ctx,
                R.layout.spinner_item, equalizerRevNames);

        equalizerRevSpinnerAdapter.setDropDownViewResource(R.layout.spinner_drop_down_item);
        equalizerRevNames.add("None");
        equalizerRevNames.add("Small Room");
        equalizerRevNames.add("Medium Room");
        equalizerRevNames.add("Large Room");
        equalizerRevNames.add("Medium Hall");
        equalizerRevNames.add("Large Hall");
        equalizerRevNames.add("Plate");

        revSpinner.setAdapter(equalizerRevSpinnerAdapter);

        if (Settings.isEqualizerReloaded) {

            revSpinner.setSelection(Settings.reverbPreset);
        }

        revSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try {
                    Settings.reverbPreset = (short) position;
                    try {
                        Settings.equalizerModel.setReverbPreset(Settings.reverbPreset);
                        MainActivity.Companion.getInstance().presetReverb.setPreset(Settings.reverbPreset);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    Toast.makeText(ctx, "Error while updating Equalizer", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Settings.isEditing = false;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {
        private int id = -1;

        public Builder setAudioSessionId(int id) {
            this.id = id;
            return this;
        }

        public Builder setAccentColor(int color) {
            themeColor = color;
            return this;
        }

        public EqualizerFragment build() {
            return EqualizerFragment.newInstance();
        }
    }


}