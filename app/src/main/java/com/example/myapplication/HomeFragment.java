package com.example.myapplication;

import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class HomeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        Button btnVoice = view.findViewById(R.id.btnVoice);
        Button btnBgm = view.findViewById(R.id.btnBGM);
        Button btnStop = view.findViewById(R.id.btnStop);
        Spinner voiceSpinner = view.findViewById(R.id.VoiceSpinner);
        Spinner bgmSpinner = view.findViewById(R.id.BGMspinner);
        final List<MediaPlayer> musicList = new ArrayList<>();
        btnVoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MediaPlayer music = null;
                switch (voiceSpinner.getSelectedItem().toString()) {
                    case "Igiari":
                        music = MediaPlayer.create(getActivity(), R.raw.igiari);
                        break;
                    case "Matta":
                        music = MediaPlayer.create(getActivity(), R.raw.matta);
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + voiceSpinner.getSelectedItem().toString());
                };
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
                MediaPlayer music = null;
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
}