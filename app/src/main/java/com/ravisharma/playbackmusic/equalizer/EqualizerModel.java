package com.ravisharma.playbackmusic.equalizer;

import java.io.Serializable;

public class EqualizerModel implements Serializable {
    private boolean isEqualizerEnabled;
    private boolean isEqualizerReloaded;
    private int[] seekbarpos = new int[5];
    private int presetPos;
    private short reverbPreset;
    private short bassStrength;

    public EqualizerModel() {
        isEqualizerEnabled = true;
        isEqualizerReloaded = true;
        reverbPreset = -1;
        bassStrength = -1;
    }

    public boolean isEqualizerEnabled() {
        return isEqualizerEnabled;
    }

    public void setEqualizerEnabled(boolean equalizerEnabled) {
        isEqualizerEnabled = equalizerEnabled;
    }

    public boolean isEqualizerReloaded() {
        return isEqualizerReloaded;
    }

    public void setEqualizerReloaded(boolean equalizerReloaded) {
        isEqualizerReloaded = equalizerReloaded;
    }

    public int[] getSeekbarpos() {
        return seekbarpos;
    }

    public void setSeekbarpos(int[] seekbarpos) {
        this.seekbarpos = seekbarpos;
    }

    public int getPresetPos() {
        return presetPos;
    }

    public void setPresetPos(int presetPos) {
        this.presetPos = presetPos;
    }

    public short getReverbPreset() {
        return reverbPreset;
    }

    public void setReverbPreset(short reverbPreset) {
        this.reverbPreset = reverbPreset;
    }

    public short getBassStrength() {
        return bassStrength;
    }

    public void setBassStrength(short bassStrength) {
        this.bassStrength = bassStrength;
    }
}
