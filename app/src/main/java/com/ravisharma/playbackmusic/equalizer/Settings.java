package com.ravisharma.playbackmusic.equalizer;

public class Settings {
    public static boolean isEqualizerEnabled = true;
    public static boolean isEqualizerReloaded = true;
    public static int[] seekbarpos = new int[5];
    public static int presetPos;
    public static short reverbPreset = -1, bassStrength = -1,virtualizerStrength = -1;
    public static EqualizerModel equalizerModel;
    public static boolean isEditing = false;
}
