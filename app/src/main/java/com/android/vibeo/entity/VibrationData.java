package com.android.vibeo.entity;

/**
 * Created by Grigory Azaryan on 12/1/19.
 */

public class VibrationData {
    private long[] timings;
    private int[] amplitudes;

    public VibrationData(long[] timings, int[] amplitudes) {
        this.timings = timings;
        this.amplitudes = amplitudes;
    }

    public long[] getTimings() {
        return timings;
    }

    public int[] getAmplitudes() {
        return amplitudes;
    }
}
