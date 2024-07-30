package com.example.myapplication;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;


public class SettingFragment extends PreferenceFragmentCompat {


    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        // 這行代碼只會在首次運行時設置默認值
        PreferenceManager.setDefaultValues(getActivity(), R.xml.preferences, false);

        // 可以添加一些邏輯來檢查開關的當前狀態
        SwitchPreferenceCompat vibrationPref = findPreference(getString(R.string.vibration));
        if (vibrationPref != null) {
            vibrationPref.setOnPreferenceChangeListener((preference, newValue) -> {
                // 這裡可以添加當開關狀態改變時的邏輯
                boolean isEnabled = (Boolean) newValue;
                // 例如：Log.d("SettingFragment", "Vibration enabled: " + isEnabled);
                return true; // 返回 true 表示允許更改
            });
        }
    }

}