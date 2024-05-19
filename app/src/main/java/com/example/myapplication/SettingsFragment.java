package com.example.myapplication;

import android.os.Bundle;
import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceFragmentCompat;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        // URL ve süre tercihlerini ekleme
        EditTextPreference happyUrlPreference = findPreference("happy_url");
        EditTextPreference sadUrlPreference = findPreference("sad_url");
        EditTextPreference intervalPreference = findPreference("interval_minutes");

        // Girişlerin geçerli olup olmadığını kontrol etme ve özet bilgilerini ayarlama
        if (happyUrlPreference != null) {
            happyUrlPreference.setSummaryProvider(preference -> {
                String text = happyUrlPreference.getText();
                return text == null || text.isEmpty() ? "Belirtilmemiş" : text;
            });

            happyUrlPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                // Burada URL'nin geçerliliğini kontrol edebilirsiniz
                return true;
            });
        }

        if (sadUrlPreference != null) {
            sadUrlPreference.setSummaryProvider(preference -> {
                String text = sadUrlPreference.getText();
                return text == null || text.isEmpty() ? "Belirtilmemiş" : text;
            });

            sadUrlPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                // Burada URL'nin geçerliliğini kontrol edebilirsiniz
                return true;
            });
        }

        if (intervalPreference != null) {
            intervalPreference.setSummaryProvider(preference -> {
                String text = intervalPreference.getText();
                return text == null || text.isEmpty() ? "Belirtilmemiş" : text;
            });

            intervalPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                try {
                    int interval = Integer.parseInt(newValue.toString());
                    return interval > 0; // Sürenin pozitif bir değer olduğundan emin olun
                } catch (NumberFormatException e) {
                    return false;
                }
            });
        }
    }
}
