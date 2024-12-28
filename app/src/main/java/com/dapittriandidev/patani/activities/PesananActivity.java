package com.dapittriandidev.patani.activities;

import static com.dapittriandidev.patani.R.*;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dapittriandidev.patani.NotificationsActivity;
import com.dapittriandidev.patani.R;
import com.dapittriandidev.patani.adapters.PesananAdapter;
import com.dapittriandidev.patani.fragments.DashboardPembeli;
import com.dapittriandidev.patani.models.Pesanan;
import com.dapittriandidev.patani.utils.BottomNavigationUtil;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class PesananActivity extends AppCompatActivity implements PesananAdapter.OnPesananActionListener {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyStateText;

    private PesananAdapter adapter;
    private ArrayList<Pesanan> pesananList;

    private FirebaseFirestore firestore;
    private boolean isPetani; // Role pengguna

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pesanan);


        // Inisialisasi View
        recyclerView = findViewById(R.id.rvPesananSaya);
        progressBar = findViewById(R.id.progressBar);
        emptyStateText = findViewById(R.id.emptyStateText);

        // Inisialisasi Firestore dan Data
        firestore = FirebaseFirestore.getInstance();
        pesananList = new ArrayList<>();

        BottomNavigationView bottomNavigationView = findViewById(id.bottomNavigationOrder);

        // Set listener untuk item navigasi
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                // Navigasi berdasarkan item yang dipilih
                if (itemId == R.id.homePembeli) {
                    Intent intent = new Intent(PesananActivity.this, DashboardPembeli.class);
                    startActivity(intent);
                    return true;
                } else if (itemId == id.pesanan) {
//                    Intent intent = new Intent(PesananActivity.this, PesananActivity.class);
//                    startActivity(intent);
                    return true;
                } else if (itemId == R.id.notification) {
                    Intent intent = new Intent(PesananActivity.this, NotificationsActivity.class);
                    startActivity(intent);
                    return true;
                } else if (itemId == R.id.user) {
                    Intent intent = new Intent(PesananActivity.this, UserActivity.class);
                    startActivity(intent);
                    return true;
                } else {
                    return false;
                }
            }
        });

        // Optionally, set the default selected item (e.g., Home)
        bottomNavigationView.setSelectedItemId(id.pesanan);

//        BottomNavigationView bottomNavigationView = findViewById(R.id.NavPesananSaya);
//        bottomNavigationView.inflateMenu(R.menu.navigation_bottom);
//        BottomNavigationUtil.setupBottomNavigation(this, bottomNavigationView, R.id.pesananSaya, BottomNavigationUtil.USER_TYPE_PEMBELI);


        ImageView btnBack = findViewById(R.id.backPesanan);
        btnBack.setOnClickListener(v -> onBackPressed());

        // Set LayoutManager dan Adapter RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Check Role Pengguna
        checkUserRole();
    }


    private void checkUserRole() {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) {
            showEmptyState("User ID tidak ditemukan.");
            return;
        }

        // Ambil data role pengguna dari Firestore
        firestore.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        isPetani = "Petani".equals(documentSnapshot.getString("peran"));
                        loadPesananData(); // Muat data pesanan setelah role ditentukan
                    } else {
                        showEmptyState("Data pengguna tidak ditemukan.");
                    }
                })
                .addOnFailureListener(e -> showEmptyState("Gagal memuat data pengguna: " + e.getMessage()));
    }

    private void loadPesananData() {
        progressBar.setVisibility(View.VISIBLE);
        emptyStateText.setVisibility(View.GONE);

        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) {
            showEmptyState("User ID tidak ditemukan.");
            return;
        }

        // Query berdasarkan role pengguna
        firestore.collection("orders")
                .whereEqualTo(isPetani ? "petaniId" : "pembeliId", userId)
                .orderBy("orderDate", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);

                    if (task.isSuccessful() && task.getResult() != null) {
                        QuerySnapshot querySnapshot = task.getResult();
                        pesananList.clear();

                        Log.d("FirestoreDebug", "Query successful. Found " + querySnapshot.size() + " orders.");

                        if (!querySnapshot.isEmpty()) {
                            try {
                                pesananList.addAll(querySnapshot.toObjects(Pesanan.class));
                                Log.d("FirestoreDebug", "Pesanan list parsed successfully: " + pesananList.size() + " items.");

                                if (adapter == null) {
                                    adapter = new PesananAdapter(pesananList, this, isPetani, this);
                                    recyclerView.setAdapter(adapter);
                                    Log.d("FirestoreDebug", "Adapter created and set.");
                                } else {
                                    adapter.notifyDataSetChanged();
                                    Log.d("FirestoreDebug", "Adapter updated with new data.");
                                }
                            } catch (Exception e) {
                                Log.e("FirestoreDebug", "Error parsing documents to Pesanan objects: " + e.getMessage(), e);
                                showEmptyState("Terjadi kesalahan saat memproses data pesanan.");
                            }
                        } else {
                            Log.d("FirestoreDebug", "No orders found for this user.");
                            showEmptyState("Belum ada pesanan untuk pembeli.");
                        }
                    } else {
                        String errorMessage = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                        Log.e("FirestoreDebug", "Query failed: " + errorMessage);
                        showEmptyState("Gagal memuat data pesanan.");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreDebug", "Firestore query failed: " + e.getMessage(), e);
                    showEmptyState("Gagal memuat data pesanan: " + e.getMessage());
                });

    }

    private void showEmptyState(String message) {
        progressBar.setVisibility(View.GONE);
        emptyStateText.setVisibility(View.VISIBLE);
        emptyStateText.setText(message);
    }

    @Override
    public void onPesananDiterima(Pesanan pesanan) {
        // Ambil orderId pesanan yang diterima
        String orderId = pesanan.getOrderId();
        Log.d("Pesanan", "Pesanan ID yang diterima: " + orderId);

        // Mencari pesanan berdasarkan orderId
        firestore.collection("orders")
                .whereEqualTo("orderId", orderId)  // Mencocokkan orderId dengan field dalam dokumen
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        // Jika pesanan ditemukan, update status pesanan
                        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                            // Data yang akan diupdate
                            Map<String, Object> updateData = new HashMap<>();
                            updateData.put("statusPesanan", "Produk Diterima oleh pembeli, pesanan selesai");
                            updateData.put("tanggalPesananDiterima", new Timestamp(new Date())); // Tambahkan field tanggalPesananDiterima

                            // Update dokumen di Firestore
                            firestore.collection("orders").document(document.getId())
                                    .update(updateData)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(PesananActivity.this, "Pesanan diterima", Toast.LENGTH_SHORT).show();
                                        loadPesananData(); // Refresh data untuk memperbarui tampilan
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(PesananActivity.this, "Gagal memperbarui pesanan: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } else {
                        // Jika pesanan tidak ditemukan
                        Toast.makeText(PesananActivity.this, "Pesanan tidak ditemukan dengan ID: " + orderId, Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    // Jika gagal memuat data dari Firestore
                    Toast.makeText(PesananActivity.this, "Gagal memeriksa pesanan: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    @Override
    public void onKonfirmasiPesanan(Pesanan pesanan) {
        // Implementasi logika konfirmasi pesanan
        Toast.makeText(this, "Konfirmasi pesanan belum diimplementasi.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onKirimPesanan(Pesanan pesanan) {
        // Implementasi logika kirim pesanan
        Toast.makeText(this, "Kirim pesanan belum diimplementasi.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onTolakPesanan(Pesanan pesanan) {
        // Implementasi logika tolak pesanan
        Toast.makeText(this, "Tolak pesanan belum diimplementasi.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPesananDibatalkan(Pesanan pesanan) {
        // Ambil orderId pesanan yang diterima
        String orderId = pesanan.getOrderId();
        Log.d("Pesanan", "Pesanan ID yang dibatalkan: " + orderId);

        // Mencari pesanan berdasarkan orderId
        firestore.collection("orders")
                .whereEqualTo("orderId", orderId)  // Mencocokkan orderId dengan field dalam dokumen
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        // Jika pesanan ditemukan, update status pesanan
                        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                            // Data yang akan diupdate
                            Map<String, Object> updateData = new HashMap<>();
                            updateData.put("statusPesanan", "Pesanan dibatalkan oleh pembeli.");
                            updateData.put("tanggalPesananDiterima", new Timestamp(new Date())); // Tambahkan field tanggalPesananDiterima

                            // Update dokumen di Firestore
                            firestore.collection("orders").document(document.getId())
                                    .update(updateData)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(PesananActivity.this, "Pesanan dibatalkan", Toast.LENGTH_SHORT).show();
                                        loadPesananData(); // Refresh data untuk memperbarui tampilan
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(PesananActivity.this, "Gagal memperbarui pesanan: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } else {
                        // Jika pesanan tidak ditemukan
                        Toast.makeText(PesananActivity.this, "Pesanan tidak ditemukan dengan ID: " + orderId, Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    // Jika gagal memuat data dari Firestore
                    Toast.makeText(PesananActivity.this, "Gagal memeriksa pesanan: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
