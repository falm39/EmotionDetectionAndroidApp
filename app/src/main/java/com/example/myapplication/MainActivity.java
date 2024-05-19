package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import android.os.Bundle;
import android.util.Log;

import com.example.myapplication.SettingsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new CameraFragment()).commit();
        }

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Fragment selectedFragment = null;
            if (itemId == R.id.nav_camera) {
                selectedFragment = new CameraFragment();
                Log.d(TAG, "Camera fragment selected");
            } else if (itemId == R.id.nav_history) {
                selectedFragment = new HistoryFragment();
                Log.d(TAG, "History fragment selected");
            } else if (itemId == R.id.nav_settings) {
                selectedFragment = new SettingsFragment();
                Log.d(TAG, "Settings fragment selected");
            }
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
            return true;
        });
    }
}
