package com.dapittriandidev.patani.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.dapittriandidev.patani.utils.SharedPrefManager;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Periksa status login
        SharedPrefManager sharedPrefManager = SharedPrefManager.getInstance(this);
        if (sharedPrefManager.isLoggedIn()) {
            Intent intent = new Intent(this, DashboardActivity.class);
            intent.putExtra("peran", sharedPrefManager.getRole());
            startActivity(intent);
        } else {
            startActivity(new Intent(this, MainActivity.class));
        }

        // Tutup SplashActivity
        finish();
    }
}
