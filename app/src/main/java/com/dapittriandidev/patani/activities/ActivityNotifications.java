package com.dapittriandidev.patani.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.dapittriandidev.patani.NotificationsActivity;
import com.dapittriandidev.patani.R;
import com.dapittriandidev.patani.fragments.DashboardPembeli;
import com.dapittriandidev.patani.utils.BottomNavigationUtil;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ActivityNotifications extends AppCompatActivity {
    private Button btnBack;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(Color.TRANSPARENT);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> onBackPressed());

//        BottomNavigationView bottomNavigationView;
//        bottomNavigationView = findViewById(R.id.bottomNavigationInNotif);
//
//        // Set listener untuk item navigasi
//        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
//            @Override
//            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
//                int itemId = item.getItemId();
//
//                // Navigasi berdasarkan item yang dipilih
//                if (itemId == R.id.homePembeli) {
//                    Intent intent = new Intent(ActivityNotifications.this, DashboardPembeli.class);
//                    startActivity(intent);
//                    return true;
//                } else if (itemId == R.id.pesananSaya) {
//                    Intent intent = new Intent(ActivityNotifications.this, PesananActivity.class);
//                    startActivity(intent);
//                    return true;
//                } else if (itemId == R.id.notification) {
////                    Intent intent = new Intent(ActivityNotifications.this, NotificationsActivity.class);
////                    startActivity(intent);
//                    return true;
//                } else if (itemId == R.id.user) {
//                    Intent intent = new Intent(ActivityNotifications.this, UserActivity.class);
//                    startActivity(intent);
//                    return true;
//                } else {
//                    return false;
//                }
//            }
//        });
//
//        // Optionally, set the default selected item (e.g., Home)
//        bottomNavigationView.setSelectedItemId(R.id.notifications);
    }
}
