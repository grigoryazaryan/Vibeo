package com.android.vibeo;

import android.Manifest;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;

import com.android.vibeo.entity.VibrationData;
import com.android.vibeo.helper.WavHelper;
import com.chibde.visualizer.SquareBarVisualizer;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {
    private final int REQ_RECORD_AUDIO = 210;

    private Vibrator vibrator;
    private MediaPlayer player;
    private SquareBarVisualizer squareBarVisualizer;

    private FloatingActionButton startPauseButton;
    private boolean isPlaying;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        squareBarVisualizer = findViewById(R.id.visualizer);
        // set custom color to the line.
        squareBarVisualizer.setColor(ContextCompat.getColor(this, R.color.custom));
        // define custom number of bars you want in the visualizer between (10 - 256).
        squareBarVisualizer.setDensity(65);
        // Set Spacing
        squareBarVisualizer.setGap(2);

        startPauseButton = findViewById(R.id.button_play_pause);
        startPauseButton.setOnClickListener(v -> {
            if (isPlaying) {
                stop();
            } else {
                start();
            }
        });

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
    }



    @RequiresApi(api = Build.VERSION_CODES.O)
    private void vibrate(VibrationData data) {

//        if (vibrator.hasAmplitudeControl()) {
        VibrationEffect effect = VibrationEffect.createWaveform(data.getTimings(), data.getAmplitudes(), -1);
        vibrator.vibrate(effect);
//        }
    }

    private void start() {
        EasyPermissions.requestPermissions(this, getString(R.string.allow_audio_rec), REQ_RECORD_AUDIO, Manifest.permission.RECORD_AUDIO);
    }

    private void stop() {
        isPlaying = false;
        startPauseButton.setImageResource(R.drawable.ic_play);

        player.stop();
        player.release();

        squareBarVisualizer.release();

        vibrator.cancel();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        if (requestCode == REQ_RECORD_AUDIO) {
            playSoundAndVibrate();
            // Set your media player to the visualizer.
            squareBarVisualizer.setPlayer(player.getAudioSessionId());
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        if (requestCode == REQ_RECORD_AUDIO) {
            playSoundAndVibrate();
        }
    }

    private void playSoundAndVibrate() {
        isPlaying = true;
        startPauseButton.setImageResource(R.drawable.ic_stop);

        player = MediaPlayer.create(this, R.raw.bach);
        player.start();

        try {
            InputStream inputStream = getResources().getAssets().open("bach.wav");
            vibrate(WavHelper.extractVibrationData(inputStream));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
