package com.dapittriandidev.patani.activities.listener;

import com.dapittriandidev.patani.fragments.DashboardPembeli;
import com.dapittriandidev.patani.utils.NotificationHelper;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.DocumentSnapshot;
import androidx.annotation.Nullable;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ProdukListener {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private Context context;

    public ProdukListener(Context context) {
        this.context = context;
    }

    // Mendengarkan perubahan pada koleksi produk
    public void listenForNewProducts() {
        db.collection("products")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Log.w("FirestoreError", "Listen failed.", error);
                            return;
                        }

                        if (value != null && !value.isEmpty()) {
                            // Periksa setiap dokumen produk yang berubah
                            for (DocumentSnapshot document : value.getDocuments()) {
                                String namaProduk = document.getString("nama");
                                // Kirimkan notifikasi lokal untuk produk baru
                                if (namaProduk != null) {
                                    showNotificationForNewProduct(namaProduk);
                                }
                            }
                        }
                    }
                });
    }

    private void showNotificationForNewProduct(String productName) {
        // Buat intent untuk membuka Dashboard Pembeli ketika notifikasi diklik
        Intent intent = new Intent(context, DashboardPembeli.class);

        // Gunakan NotificationHelper untuk menampilkan notifikasi lokal
        NotificationHelper.showNotification(
                context,
                "Produk Baru Tersedia!",
                "Produk baru tersedia: " + productName,
                intent
        );
    }
}
