package com.android.vibeo.helper;

import android.util.Log;

import com.android.vibeo.entity.VibrationData;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import androidx.annotation.NonNull;

import static com.android.vibeo.helper.Utils.meanOfArray;

/**
 * Created by Grigory Azaryan on 11/30/19.
 */

// Special thanks to this person
// https://mindtherobot.com/blog/580/android-audio-play-a-wav-file-on-an-audiotrack/

public class WavHelper {
    private final static String TAG = "WavHelper";

    public static final String RIFF_HEADER = "RIFF";
    public static final String WAVE_HEADER = "WAVE";
    public static final String FMT_HEADER = "fmt ";
    public static final String DATA_HEADER = "data";

    public static final int HEADER_SIZE = 44;

    public static final String CHARSET = "ASCII";

    protected static void checkFormat(boolean assertion, String message) throws IOException {
        if (!assertion) {
            throw new IOException(message);
        }
    }

    public static class WavInfo {
        int sampleRate;
        int bits;
        int dataSize;
        int channels;

        public WavInfo(int rate, int bits, int channels, int dataSize) {
            this.sampleRate = rate;
            this.bits = bits;
            this.channels = channels;
            this.dataSize = dataSize;

        }

        public int getSampleRate() {
            return sampleRate;
        }

        public int getBits() {
            return bits;
        }

        public int getDataSize() {
            return dataSize;
        }

        public int getDurationMillis() {
            return (int) (dataSize * 1000L / sampleRate / (bits / 8) / channels);
        }

        @NonNull
        @Override
        public String toString() {
            return "dataSize " + dataSize
                    + ", samples per sec " + sampleRate
                    + ", bytes per sample " + bits / 8
                    + ", channels " + channels
                    + ", duration " + getDurationMillis();
        }
    }

    public static WavInfo readHeader(InputStream wavStream) throws IOException {

        ByteBuffer buffer = ByteBuffer.allocate(HEADER_SIZE);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        wavStream.read(buffer.array(), buffer.arrayOffset(), buffer.capacity());

        buffer.rewind();
        buffer.position(buffer.position() + 20);

        int format = buffer.getShort();

        checkFormat(format == 1, "Unsupported encoding: " + format); // 1 means
        // Linear
        // PCM
        int channels = buffer.getShort();

//        checkFormat(channels == 1 || channels == 2, "Unsupported channels: " + channels);

        int rate = buffer.getInt();

//        checkFormat(rate <= 48000 && rate >= 11025, "Unsupported rate: " + rate);

        buffer.position(buffer.position() + 6);

        int bits = buffer.getShort();
        //checkFormat(bits == 16, "Unsupported bits: " + bits);

        int dataSize = 0;

        while (buffer.getInt() != 0x61746164) { // "data" marker
            int size = buffer.getInt();
            wavStream.skip(size);

            buffer.rewind();
            wavStream.read(buffer.array(), buffer.arrayOffset(), 8);
            buffer.rewind();
        }

        dataSize = buffer.getInt();

        checkFormat(dataSize > 0, "wrong datasize: " + dataSize);

        return new WavInfo(rate, bits, channels, dataSize);
    }

    public static byte[] readWavPcm(WavInfo info, InputStream stream) throws IOException {

        byte[] data = new byte[info.getDataSize()];
        stream.read(data, 0, data.length);

        return data;
    }

    public static byte[] readWavPcm(InputStream stream) throws IOException {

        WavInfo info = readHeader(stream);

        return readWavPcm(info, stream);
    }

    public static VibrationData extractVibrationData(InputStream input) throws IOException {
        WavHelper.WavInfo info = WavHelper.readHeader(input);
        Log.v(TAG, info.toString());

        byte[] raw = WavHelper.readWavPcm(info, input);
        for (int i = 0; i < raw.length; i++) {
            if (raw[i] < 0) {
                raw[i] = 0;
//                    raw[i] = (byte) -(raw[i] + 1);
            }
        }
//            Log.v(TAG, Arrays.toString(raw));

        int globalMean = meanOfArray(raw);

        int fragmentsPerSecond = 20;
        int fragmentsDurationMillis = 1000 / fragmentsPerSecond;
        int fragmentsCount = info.getDurationMillis() / fragmentsDurationMillis;
        int itemsPerFragment = raw.length / fragmentsCount;
        int[] fragments = new int[fragmentsCount];
        long[] timings = new long[fragmentsCount];
        for (int i = 0; i < fragments.length; i++) {
            byte[] fragment = Arrays.copyOfRange(raw, i * itemsPerFragment, (i + 1) * itemsPerFragment);
            int mean = meanOfArray(fragment);
            fragments[i] = mean < globalMean ? 0 : mean;
            timings[i] = fragmentsDurationMillis;
        }

//            Log.v(TAG, Arrays.toString(fragments));

        return new VibrationData(timings, fragments);
    }

}
