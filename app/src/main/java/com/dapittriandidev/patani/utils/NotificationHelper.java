package com.dapittriandidev.patani.utils;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.dapittriandidev.patani.R;
import com.dapittriandidev.patani.fragments.DashboardPembeli;

public class NotificationHelper {

    public static boolean hasNotificationPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED;
        }
        return true; // Izin otomatis diberikan pada versi sebelum Android 13
    }

    public static void showNotification(Context context, String title, String message, Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.w("NotificationHelper", "Permission for notifications not granted");
                return; // Menghentikan jika izin belum diberikan
            }
        }

        try {
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "default_channel")
                    .setSmallIcon(R.drawable.shopping_bag) // Ikon notifikasi
                    .setContentTitle(title)
                    .setContentText(message)
                    .setAutoCancel(true)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent);

            NotificationManagerCompat manager = NotificationManagerCompat.from(context);
            manager.notify((int) System.currentTimeMillis(), builder.build());
        } catch (SecurityException e) {
            Log.e("NotificationHelper", "Failed to show notification due to missing permission", e);
        }
    }


    public static void sendNotification(Context context, String token, String title, String message) {
        // Logika ini untuk notifikasi lokal,
        // Jika menggunakan Firebase Cloud Messaging, tambahkan logika pengiriman FCM di sini
        Intent intent = new Intent(context, DashboardPembeli.class);
        showNotification(context, title, message, intent);
    }
}

