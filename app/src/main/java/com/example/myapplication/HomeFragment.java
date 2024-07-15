package com.example.myapplication;

import static android.content.Context.VIBRATOR_SERVICE;
import static androidx.core.content.ContextCompat.getSystemService;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
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
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class HomeFragment extends Fragment implements SensorEventListener {
    Button btnVoice, btnBgm, btnStop, btnPlay,btnPause,btnContinue;
    Spinner voiceSpinner, bgmSpinner;
    ImageView imageView;
    SensorManager sensorManager;
    SeekBar seekBar;
    Sensor sensor;
    Toast toast;
    private float[] gravity = new float[3];
    private static final float ALPHA = 0.2f;
    private boolean isFunctionActive = true;
    private float triggerForce = 0.004f;
    private boolean isSensorActivated = false;
    final List<MediaPlayer> musicList = new ArrayList<>();
    private boolean isBgmPlaying = false;
    private MediaPlayer currentVoicePlayer = null;
    private MediaPlayer currentBgmPlayer = null;
    private String previousBgm = "";
    Vibrator vibrator;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //init
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        init(view);


        //function
        btnVoice.setOnClickListener(v -> {
            if (!voiceSpinner.getSelectedItem().toString().equals("None")) {
                MediaPlayer music;
                music = getVoicePlayer(voiceSpinner.getSelectedItem().toString());
                if (music != null) {
                    musicList.add(music);
                    //Toast.makeText(getActivity(), "make sound", Toast.LENGTH_SHORT).show();
                    music.start();
                }
            }
        });
        btnBgm.setOnClickListener(v -> {
            for (MediaPlayer mediaPlayer : musicList) {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                    mediaPlayer.reset();
                    mediaPlayer.release();
                }
            }
            musicList.clear();
            if (!bgmSpinner.getSelectedItem().toString().equals("None")) {
                MediaPlayer music;
                music = getBgmPlayer(bgmSpinner.getSelectedItem().toString());
                if (music != null) {
                    musicList.add(music);
                    music.start();
                    btnStop.setEnabled(true);
                    music.setOnCompletionListener(mp -> {
                        musicList.remove(mp);
                        mp.release();
                        if (musicList.isEmpty()) {
                            btnStop.setEnabled(false);
                        }
                    });
                }
            }
        });
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                triggerForce = (float) (0.004f + 0.004f * (Math.pow(progress, 4)));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //Toast.makeText(getActivity(),"Progress: "+String.valueOf(triggerForce),Toast.LENGTH_SHORT).show();

            }
        });
        btnPlay.setOnClickListener(v -> {
            isSensorActivated = true;
            btnStop.setEnabled(true);
            btnPause.setEnabled(true);
            new Handler().postDelayed(() -> isFunctionActive = false, 1000);
        });
        btnPause.setOnClickListener(v -> {
            isSensorActivated = false;
            btnContinue.setEnabled(true);
        });
        btnContinue.setOnClickListener(v -> {
            isSensorActivated = true;
            btnContinue.setEnabled(false);
        });
        btnStop.setOnClickListener(v -> {
            isSensorActivated = false;
            for (MediaPlayer mediaPlayer : musicList) {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                    mediaPlayer.reset();
                    mediaPlayer.release();
                }
            }
            musicList.clear();
            btnStop.setEnabled(false);
            btnPause.setEnabled(false);
            btnContinue.setEnabled(false);
            isBgmPlaying = false;
        });
        return view;
    }

    private void init(View view) {

        sensorManager = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        btnVoice = view.findViewById(R.id.btnVoice);
        btnBgm = view.findViewById(R.id.btnBGM);
        btnStop = view.findViewById(R.id.btnStop);
        btnPlay = view.findViewById(R.id.btnPlay);
        btnPause=view.findViewById(R.id.btnPause);
        btnContinue = view.findViewById(R.id.btnContinue);
        voiceSpinner = view.findViewById(R.id.VoiceSpinner);
        bgmSpinner = view.findViewById(R.id.BGMspinner);
        seekBar = view.findViewById(R.id.seekBar);
        imageView = view.findViewById(R.id.imageView);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }


    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    public void onResume() {
        super.onResume();
        // Optionally re-enable sensor here if needed
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (isSensorActivated) {
            float[] values = event.values;
            float[] filteredValues = lowPassFilter(values);
            float x = filteredValues[0];
            float y = filteredValues[1];
            float z = filteredValues[2];
            //Log.d("sensor",String.format("%f,%f,%f,%f",x,y,z, triggerForce));

            // Detect tap based on threshold and time interval
            if (Math.abs(x) >= triggerForce || Math.abs(y) >= triggerForce || Math.abs(z) >= triggerForce) {
                onTableTapped();
            }
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

            // Cancel any existing toast
            if (toast != null) {
                toast.cancel();
            }

            // Voice
            String selectedVoice = voiceSpinner.getSelectedItem().toString();
            String selectedBgm = bgmSpinner.getSelectedItem().toString();
            if (!selectedVoice.equals("None")) {
                MediaPlayer voicePlayer = getVoicePlayer(selectedVoice);
                if (voicePlayer != null) {
                    currentVoicePlayer = voicePlayer;
                    musicList.add(voicePlayer);
                    //Toast.makeText(getActivity(), "Playing voice", Toast.LENGTH_SHORT).show();
                    voicePlayer.start();
                    imageView.setVisibility(View.VISIBLE);
                    shakeView(imageView);
                    voicePlayer.setOnCompletionListener(mp -> {
                        currentVoicePlayer = null;
                        if (!selectedBgm.equals(previousBgm)){
                            stopAndReleaseAllPlayers(); // Stop all audio when voice played
                        }
                        imageView.setVisibility(View.GONE);
                        playBgmIfSelected(selectedBgm);// Play the selected BGM
                    });
                }
            } else {
                if (!selectedBgm.equals(previousBgm)) {
                    stopAndReleaseAllPlayers();
                }
                playBgmIfSelected(selectedBgm); // Play the selected BGM if no voice is selected
            }

            new Handler().postDelayed(() -> isFunctionActive = false, 1000);
        }
    }

    private void playBgmIfSelected(String selectedBgm) {

        // Check if the selected BGM is the same as the currently playing BGM
        if (!selectedBgm.equals("None") && (!isBgmPlaying || !selectedBgm.equals(previousBgm))) {
            // Stop and release current BGM if it's different
            if (currentBgmPlayer != null) {
                currentBgmPlayer.stop();
                currentBgmPlayer.release();
                isBgmPlaying = false;
                currentBgmPlayer = null;
            }

            MediaPlayer bgmPlayer = getBgmPlayer(selectedBgm);
            if (bgmPlayer != null) {
                bgmPlayer.setLooping(true);
                musicList.add(bgmPlayer);
                bgmPlayer.start();
                btnStop.setEnabled(true);
                isBgmPlaying = true;
                currentBgmPlayer = bgmPlayer;
                previousBgm = selectedBgm;
                bgmPlayer.setOnCompletionListener(mp1 -> {
                    isBgmPlaying = false;
                    currentBgmPlayer = null;
                });
            }
        }
    }

    private void stopAndReleaseAllPlayers() {
        for (MediaPlayer player : musicList) {
            if (player != null) {
                player.stop();
                player.release();
            }
        }
        musicList.clear();
        isBgmPlaying = false;
        currentVoicePlayer = null;
        currentBgmPlayer = null;
    }
    public void shakeView(View view) {
        PropertyValuesHolder pvhTranslateX = PropertyValuesHolder.ofFloat(View.TRANSLATION_X, 0, 25, -25, 25, -25, 15, -15, 6, -6, 0);
        PropertyValuesHolder pvhTranslateY = PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, 0, 15, -15, 15, -15, 10, -10, 5, -5, 0);
        long[] timings = new long[] { 5, 30, 50, 70, 100, 100,100,100,50  };
        int[] amplitudes = new int[] { 0,25,50,75,100,125,150,200,255 };
        ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(view, pvhTranslateX, pvhTranslateY);
        animator.setDuration(500); // Duration of the animation in milliseconds
        animator.start();
        vibrator = (Vibrator) getActivity().getSystemService(VIBRATOR_SERVICE);
        VibrationEffect repeatingEffect = VibrationEffect.createWaveform(timings, amplitudes, -1);
        vibrator.vibrate(repeatingEffect);

    }

    private MediaPlayer getVoicePlayer(String voice) {
        switch (voice) {
            case "None":
                imageView.setImageResource(0);
                return null;
            case "Naruhodo-Igiari":
                imageView.setImageResource(R.drawable.igiari);
                return MediaPlayer.create(getActivity(), R.raw.naruhodo_igiari);
            case "Naruhodo-Matta":
                imageView.setImageResource(R.drawable.matta);
                return MediaPlayer.create(getActivity(), R.raw.naruhodo_matta);
            case "Naruhodo-Kurae":
                imageView.setImageResource(R.drawable.kurae);
                return MediaPlayer.create(getActivity(),R.raw.naruhodo_kurae);
            case "Naruhodo-Objection":
                imageView.setImageResource(R.drawable.objection);
                return MediaPlayer.create(getActivity(),R.raw.naruhodo_objection);
            case "Naruhodo-Hold it":
                imageView.setImageResource(R.drawable.holdit);
                return MediaPlayer.create(getActivity(),R.raw.naruhodo_holdit);
            case "Naruhodo-Take that":
                imageView.setImageResource(R.drawable.takethat);
                return MediaPlayer.create(getActivity(),R.raw.naruhodo_takethat);
            case "Table Slam":
                imageView.setImageResource(0);
                return MediaPlayer.create(getActivity(),R.raw.table_slam);
            default:
                throw new IllegalStateException("Unexpected value: " + voice);
        }
    }

    private MediaPlayer getBgmPlayer(String bgm) {
        switch (bgm) {
            case "None":
                return null;
            case "Objection-2001":
                return MediaPlayer.create(getActivity(), R.raw.objection_2001);
            case "Pursuit":
                return MediaPlayer.create(getActivity(), R.raw.pursuit_cornered);
            default:
                throw new IllegalStateException("Unexpected value: " + bgm);
        }
    }
}