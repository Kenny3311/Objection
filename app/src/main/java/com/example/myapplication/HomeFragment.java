package com.example.myapplication;

import static android.view.KeyCharacterMap.ALPHA;
import static androidx.core.content.ContextCompat.getSystemService;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Parcel;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class HomeFragment extends Fragment implements SensorEventListener {
    Button btnVoice,btnBgm,btnStop,btnPlay;
    Spinner voiceSpinner,bgmSpinner;
    SensorManager sensorManager;
    Sensor sensor;
    private SensorEventListener sensorEventListener;
    Toast toast;
    private float[] gravity = new float[3];
    private static final float ALPHA = 0.2f;
    private boolean isFunctionActive = true;
    private float trigerForce = 0.008f;
    private boolean isSensorActivated =false;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //init
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        init(view);
        final List<MediaPlayer> musicList = new ArrayList<>();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                isFunctionActive = false;
            }
        }, 1000);
        //function
        btnVoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MediaPlayer music;
                switch (voiceSpinner.getSelectedItem().toString()) {
                    case "Igiari":
                        music = MediaPlayer.create(getActivity(), R.raw.igiari);
                        break;
                    case "Matta":
                        music = MediaPlayer.create(getActivity(), R.raw.matta);
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + voiceSpinner.getSelectedItem().toString());
                }
                if (music != null) {
                    musicList.add(music);
                    Toast.makeText(getActivity(), "make sound", Toast.LENGTH_SHORT).show();
                    music.start();
                }
            }
        });
        btnBgm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (MediaPlayer mediaPlayer : musicList) {
                    if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                        mediaPlayer.stop();
                        mediaPlayer.reset();
                        mediaPlayer.release();
                    }
                }
                musicList.clear();
                MediaPlayer music;
                switch (bgmSpinner.getSelectedItem().toString()){
                    case "Objection-2001":
                        music = MediaPlayer.create(getActivity(),R.raw.objection_2001);
                        break;
                    case "Pursuit":
                        music = MediaPlayer.create(getActivity(),R.raw.pursuit_cornered);
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + bgmSpinner.getSelectedItem().toString());
                }
                if (music != null) {
                    musicList.add(music);
                    music.start();
                    btnStop.setEnabled(true);
                    music.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            musicList.remove(mp);
                            mp.release();
                            if (musicList.isEmpty()) {
                                btnStop.setEnabled(false);
                            }
                        }
                    });
                }
            }
        });
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSensorActivated) {
                    disableSensor();
                } else {
                    enableSensor();
                }
            }
        });
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (MediaPlayer mediaPlayer : musicList) {
                    if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                        mediaPlayer.stop();
                        mediaPlayer.reset();
                        mediaPlayer.release();
                    }
                }
                musicList.clear();
                btnStop.setEnabled(false);
            }
        });
        return view;
    }
    private void init(View view){

        sensorManager = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);
        sensor =sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this,sensor,SensorManager.SENSOR_DELAY_NORMAL);
        btnVoice = view.findViewById(R.id.btnVoice);
        btnBgm = view.findViewById(R.id.btnBGM);
        btnStop = view.findViewById(R.id.btnStop);
        btnPlay = view.findViewById(R.id.btnPlay);
        voiceSpinner = view.findViewById(R.id.VoiceSpinner);
        bgmSpinner = view.findViewById(R.id.BGMspinner);

    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(this);
    }
    @Override
    public void onResume() {
        super.onResume();
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }
    private void enableSensor() {
        if (!isSensorActivated) {
            sensorManager.registerListener(sensorEventListener, sensor, SensorManager.SENSOR_DELAY_NORMAL);
            isSensorActivated = true;
        }
    }

    private void disableSensor() {
        if (isSensorActivated) {
            sensorManager.unregisterListener(sensorEventListener);
            isSensorActivated = false;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        float[] values = event.values;
        float[] filteredValues = lowPassFilter(values);
        float x = filteredValues[0];
        float y = filteredValues[1];
        float z = filteredValues[2];
        Log.d("sensor",String.format("%f,%f,%f",x,y,z));
        // Calculate the acceleration magnitude
        float magnitude = (float) Math.sqrt(x * x + y * y + z * z);

        // Detect tap based on threshold and time interval
        if (Math.abs(x) >= trigerForce || Math.abs(y) >= trigerForce || Math.abs(z) >= trigerForce) {
            onTableTapped();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
    private float[] lowPassFilter(float[] input) {
        gravity[0] = ALPHA * gravity[0] + (1 - ALPHA) * input[0];
        gravity[1] = ALPHA * gravity[1] + (1 - ALPHA) * input[1];
        gravity[2] = ALPHA * gravity[2] + (1 - ALPHA) * input[2];

        float[] output = new float[3];
        output[0] = input[0] - gravity[0];
        output[1] = input[1] - gravity[1];
        output[2] = input[2] - gravity[2];
        return output;
    }
    private void onTableTapped() {
        if (!isFunctionActive) {
            // Set the flag to true to prevent reactivation for 1 second
            isFunctionActive = true;
            // Replace this with your desired function
            if (toast != null) {
                toast.cancel();
            }
            toast = Toast.makeText(getActivity(), "Table tapped", Toast.LENGTH_SHORT);
            toast.show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    isFunctionActive = false;
                }
            }, 1000);
        }
    }

}