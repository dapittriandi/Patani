package com.dapittriandidev.patani.utils;

import android.app.Activity;
import android.content.Intent;

import com.dapittriandidev.patani.NotificationsActivity;
import com.dapittriandidev.patani.R;
import com.dapittriandidev.patani.activities.PesananActivity;
import com.dapittriandidev.patani.activities.PesananMasuk;
import com.dapittriandidev.patani.activities.UserActivity;
import com.dapittriandidev.patani.fragments.DashboardPembeli;
import com.dapittriandidev.patani.fragments.DashboardPetani;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class BottomNavigationUtil {

    public static final int USER_TYPE_PETANI = 1;
    public static final int USER_TYPE_PEMBELI = 2;

    /**
     * Sets up the BottomNavigationView and handles navigation events.
     *
     * @param activity          The current activity context.
     * @param bottomNavigation  The BottomNavigationView to set up.
     * @param selectedMenuItemId The ID of the selected menu item to highlight.
     * @param userType          The type of the user (petani or pembeli).
     */
    public static void setupBottomNavigation(Activity activity, BottomNavigationView bottomNavigation, int selectedMenuItemId, int userType) {
        // Bersihkan menu yang ada
        bottomNavigation.getMenu().clear();

        // Muat menu berdasarkan tipe pengguna
        if (userType == USER_TYPE_PETANI) {
            bottomNavigation.inflateMenu(R.menu.navigation_bottom_selling);
        } else if (userType == USER_TYPE_PEMBELI) {
            bottomNavigation.inflateMenu(R.menu.navigation_bottom);
        }

        // Sorot item yang dipilih
        bottomNavigation.setSelectedItemId(selectedMenuItemId);

        // Tangani navigasi
        bottomNavigation.setOnItemSelectedListener(item -> {
            switch (userType) {
                case USER_TYPE_PETANI:
                    handlePetaniNavigation(activity, item.getItemId(), selectedMenuItemId);
                    break;
                case USER_TYPE_PEMBELI:
                    handlePembeliNavigation(activity, item.getItemId(), selectedMenuItemId);
                    break;
            }
            return true;
        });
    }

    private static void handlePetaniNavigation(Activity activity, int itemId, int selectedMenuItemId) {
        if (itemId == R.id.homePetani && selectedMenuItemId != R.id.homePetani) {
            navigateTo(activity, DashboardPetani.class);
        } else if (itemId == R.id.pesananMasuk && selectedMenuItemId != R.id.pesananMasuk) {
            navigateTo(activity, PesananMasuk.class);
        } else if (itemId == R.id.notifications && selectedMenuItemId != R.id.notifications) {
            navigateTo(activity, PesananActivity.class);
        } else if (itemId == R.id.user && selectedMenuItemId != R.id.user) {
            navigateTo(activity, UserActivity.class);
        }
    }

    private static void handlePembeliNavigation(Activity activity, int itemId, int selectedMenuItemId) {
        if (itemId == R.id.homePembeli && selectedMenuItemId != R.id.homePembeli) {
            navigateTo(activity, DashboardPembeli.class);
        } else if (itemId == R.id.pesanan && selectedMenuItemId != R.id.pesanan) {
            navigateTo(activity, PesananActivity.class);
        } else if (itemId == R.id.notification && selectedMenuItemId != R.id.notification) {
            navigateTo(activity, NotificationsActivity.class);
        } else if (itemId == R.id.user && selectedMenuItemId != R.id.user) {
            navigateTo(activity, UserActivity.class);
        }
    }


    private static void navigateTo(Activity currentActivity, Class<?> targetActivity) {
        Intent intent = new Intent(currentActivity, targetActivity);
        currentActivity.startActivity(intent);
        currentActivity.overridePendingTransition(0, 0);
        currentActivity.finish();
    }
}
