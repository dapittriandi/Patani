package com.dapittriandidev.patani.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
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
import com.dapittriandidev.patani.fragments.DashboardPetani;
import com.dapittriandidev.patani.models.Pesanan;
import com.dapittriandidev.patani.utils.BottomNavigationUtil;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class PesananMasuk extends AppCompatActivity implements PesananAdapter.OnPesananActionListener {

    private RecyclerView recyclerView;
    private PesananAdapter adapter;
    private List<Pesanan> pesananList;
    private FirebaseFirestore db;
    private boolean isPetani = false; // Default bukan petani
    private String userId;
    private ProgressBar progressBar;
    private TextView emptyMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pesanan_masuk);


        recyclerView = findViewById(R.id.rvPesananMasuk);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        pesananList = new ArrayList<>();
        adapter = new PesananAdapter(pesananList, this, isPetani, this);
        recyclerView.setAdapter(adapter);
        progressBar = findViewById(R.id.progressBar);
        emptyMessage = findViewById(R.id.emptyMessage);

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid(); // Mendapatkan ID pengguna saat ini
        BottomNavigationView bottomNavigationView;
        bottomNavigationView = findViewById(R.id.bottomNavigationOrderIn);

        // Set listener untuk item navigasi
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();


                // Navigasi berdasarkan item yang dipilih
                if (itemId == R.id.homePetani) {
                    Intent intent = new Intent(PesananMasuk.this, DashboardPetani.class);
                    startActivity(intent);
                    return true;
                } else if (itemId == R.id.pesananMasuk) {
//                    Intent intent = new Intent(PesananMasuk.this, PesananActivity.class);
//                    startActivity(intent);
                    return true;
                } else if (itemId == R.id.notification) {
                    Intent intent = new Intent(PesananMasuk.this, NotificationsActivity.class);
                    startActivity(intent);
                    return true;
                } else if (itemId == R.id.user) {
                    Intent intent = new Intent(PesananMasuk.this, UserActivity.class);
                    startActivity(intent);
                    return true;
                } else {
                    return false;
                }
            }
        });

        // Optionally, set the default selected item (e.g., Home)
        bottomNavigationView.setSelectedItemId(R.id.pesananMasuk);

        ImageView btnBack = findViewById(R.id.backPesanan);
        btnBack.setOnClickListener(v -> onBackPressed());
        checkUserRole(); // Memeriksa role pengguna
    }



    private void checkUserRole() {
        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String peran = documentSnapshot.getString("peran"); // Ambil field 'peran' dari dokumen pengguna
                        isPetani = "Petani".equals(peran); // Set isPetani berdasarkan peran

                        if (isPetani) {
                            // Jika pengguna adalah Petani, ambil data pesanan
                            fetchPesananData();
                        } else {
                            Toast.makeText(this, "Anda bukan Petani!", Toast.LENGTH_SHORT).show();
                            finish(); // Keluar dari aktivitas jika bukan Petani
                        }

                        // Update adapter jika perlu
                        adapter.setIsPetani(isPetani);
                        adapter.notifyDataSetChanged();
                    } else {
                        Log.e("PesananMasuk", "User data not found!");
                        Toast.makeText(this, "Data pengguna tidak ditemukan!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("PesananMasuk", "Error fetching user role", e);
                    Toast.makeText(PesananMasuk.this, "Gagal mendapatkan data role pengguna", Toast.LENGTH_SHORT).show();
                });
    }

    private void fetchPesananData() {
        progressBar.setVisibility(View.VISIBLE);

        emptyMessage.setVisibility(View.GONE);

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid(); // Ambil userId penjual yang sedang login

        // Ambil pesanan yang hanya terkait dengan userId penjual
        db.collection("orders")
                .whereEqualTo("petaniId", userId) // Filter berdasarkan petaniId
                .orderBy("orderDate", Query.Direction.DESCENDING) // Urutkan berdasarkan tanggal
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    pesananList.clear();
                    for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                        Pesanan pesanan = snapshot.toObject(Pesanan.class);
                        if (pesanan != null) {
                            pesanan.setProductId(snapshot.getId()); // Simpan ID dokumen untuk referensi
                            pesananList.add(pesanan);
                        }
                    }

                    if (pesananList.isEmpty()) {
                        emptyMessage.setVisibility(View.VISIBLE); // Tampilkan pesan kosong jika tidak ada pesanan
                    } else {
                        emptyMessage.setVisibility(View.GONE); // Sembunyikan pesan kosong jika ada pesanan
                    }
                    adapter.notifyDataSetChanged(); // Perbarui RecyclerView
                })
                .addOnFailureListener(e -> {
                    Log.e("PesananMasuk", "Error fetching pesanan data", e);
                    Toast.makeText(PesananMasuk.this, "Gagal memuat data pesanan", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    emptyMessage.setVisibility(View.GONE);
                });
    }


    @Override
    public void onPesananDiterima(Pesanan pesanan) {
        Log.d("PesananMasuk", "Pesanan diterima: " + pesanan.getOrderId());
        updatePesananStatus(pesanan.getOrderId(), "Diterima");
    }

    @Override
    public void onKonfirmasiPesanan(Pesanan pesanan) {
        Log.d("PesananMasuk", "Pesanan dikonfirmasi: " + pesanan.getOrderId());
        updatePesananStatus(pesanan.getOrderId(), "Dikonfirmasi, dan akan segera di kirim");
    }

    @Override
    public void onKirimPesanan(Pesanan pesanan) {
        Log.d("PesananMasuk", "Pesanan dikirim: " + pesanan.getOrderId());
        updatePesananStatus(pesanan.getOrderId(), "Dikirim");

        // Menampilkan pop-up untuk input estimasi pengiriman
        showEstimasiDialog(pesanan);
    }

    private void showEstimasiDialog(Pesanan pesanan) {
        // Membuat dialog untuk input estimasi
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Masukkan Estimasi Waktu Pengiriman");

        // Menambahkan EditText untuk estimasi waktu
        final EditText estimasiEditText = new EditText(this);
        estimasiEditText.setHint("Contoh: 3 - 4 hari");
        builder.setView(estimasiEditText);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String estimasi = estimasiEditText.getText().toString().trim();
            if (!estimasi.isEmpty()) {
                // Menyimpan estimasi ke Firebase
                updateEstimasiPesanan(pesanan.getOrderId(), estimasi);
            } else {
                Toast.makeText(PesananMasuk.this, "Estimasi tidak boleh kosong!", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Batal", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void updateEstimasiPesanan(String pesananId, String estimasi) {
        Log.d("PesananMasuk", "Mengupdate estimasi pesanan " + pesananId + " menjadi " + estimasi);

        // Memperbarui estimasi pengiriman pada pesanan di Firebase
        db.collection("orders")
                .whereEqualTo("orderId", pesananId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Ambil dokumen pertama yang ditemukan
                        DocumentSnapshot snapshot = queryDocumentSnapshots.getDocuments().get(0);

                        // Perbarui estimasi pengiriman
                        snapshot.getReference().update("estimasi", estimasi)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(PesananMasuk.this, "Estimasi pengiriman diperbarui: " + estimasi, Toast.LENGTH_SHORT).show();
                                    fetchPesananData(); // Refresh data untuk menampilkan estimasi yang diperbarui
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("PesananMasuk", "Gagal memperbarui estimasi pesanan", e);
                                    Toast.makeText(PesananMasuk.this, "Gagal memperbarui estimasi", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        // Pesanan tidak ditemukan
                        Log.e("PesananMasuk", "Pesanan dengan ID " + pesananId + " tidak ditemukan!");
                        Toast.makeText(PesananMasuk.this, "Pesanan tidak ditemukan!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("PesananMasuk", "Gagal mengambil dokumen pesanan", e);
                    Toast.makeText(PesananMasuk.this, "Gagal memuat dokumen pesanan", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onTolakPesanan(Pesanan pesanan) {
        Log.d("PesananMasuk", "Pesanan ditolak: " + pesanan.getOrderId());
        updatePesananStatus(pesanan.getOrderId(), "Ditolak, karena stok habis atau sebagainya");
    }

    @Override
    public void onPesananDibatalkan(Pesanan pesanan) {

    }

    private void updatePesananStatus(String pesananId, String statusBaru) {
        Log.d("PesananMasuk", "Mengupdate status pesanan " + pesananId + " menjadi " + statusBaru);

        db.collection("orders")
                .whereEqualTo("orderId", pesananId) // Gunakan whereEqualTo untuk mencocokkan ID
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot snapshot = queryDocumentSnapshots.getDocuments().get(0); // Ambil dokumen pertama
                        snapshot.getReference().update("statusPesanan", statusBaru)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(PesananMasuk.this, "Status diperbarui menjadi: " + statusBaru, Toast.LENGTH_SHORT).show();
                                    fetchPesananData(); // Refresh data
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("PesananMasuk", "Gagal memperbarui status pesanan", e);
                                    Toast.makeText(PesananMasuk.this, "Gagal memperbarui status pesanan", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Log.e("PesananMasuk", "Pesanan dengan ID " + pesananId + " tidak ditemukan!");
                        Toast.makeText(PesananMasuk.this, "Pesanan tidak ditemukan!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("PesananMasuk", "Gagal mengambil dokumen pesanan", e);
                    Toast.makeText(PesananMasuk.this, "Gagal memuat dokumen pesanan", Toast.LENGTH_SHORT).show();
                });
    }

}
