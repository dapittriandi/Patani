package com.dapittriandidev.patani.activities;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dapittriandidev.patani.R;
import com.dapittriandidev.patani.adapters.ProdukAdapter;
import com.dapittriandidev.patani.models.Produk;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class KelolaProdukActivity extends AppCompatActivity implements ProdukAdapter.OnItemClickListener {
    private RecyclerView rvProductList;
    private Button btnTambahProduk;
    private ImageButton btnBack;
    private ProdukAdapter productAdapter;
    private List<Produk> produkList = new ArrayList<>();
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("Lifecycle", "KelolaProdukActivity onCreate dipanggil");
        setContentView(R.layout.activity_kelola_produk);



        btnBack = findViewById(R.id.back);
        btnBack.setOnClickListener(v -> onBackPressed());

        // Inisialisasi Views
        rvProductList = findViewById(R.id.rvProductItem);
        btnTambahProduk = findViewById(R.id.btnTambahProduk);

        // Konfigurasi RecyclerView
        rvProductList.setLayoutManager(new LinearLayoutManager(this));
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        rvProductList.setLayoutManager(gridLayoutManager);
        productAdapter = new ProdukAdapter(this, produkList, this, true);
        rvProductList.setAdapter(productAdapter);

        // Tombol untuk menambah produk
        btnTambahProduk.setOnClickListener(v -> {
            Intent intent = new Intent(this, TambahProdukActivity.class);
            startActivity(intent);
        });

        createNotificationChannel();
        // Load data produk
        loadProducts();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "default_channel",
                    "Default Channel",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Default notification channel");

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private void loadProducts() {
        try {
            FirebaseUser currentUser = auth.getCurrentUser();
            if (currentUser == null) {
                Log.e("KelolaProdukActivity", "Pengguna tidak terautentikasi!");
                Toast.makeText(this, "Silakan login terlebih dahulu!", Toast.LENGTH_SHORT).show();
                finish(); // Kembali ke aktivitas login
                return;
            }
            String penjualId = auth.getCurrentUser().getUid(); // ID penjual yang sedang login
            Log.d("Firestore", "Memuat produk untuk penjualId: " + penjualId);

            // Menggunakan addSnapshotListener untuk mendengarkan perubahan secara real-time
            firestore.collection("products")
                    .whereEqualTo("penjualId", penjualId)
                    .addSnapshotListener((QuerySnapshot value, FirebaseFirestoreException error) -> {
                        if (error != null) {
                            Log.e("Firestore", "Error saat mendengarkan perubahan data: ", error);
                            return;
                        }

                        // Clear list dan tambahkan data terbaru dari Firestore
                        produkList.clear();
                        if (value != null) {
                            for (QueryDocumentSnapshot document : value) {
                                Produk produk = document.toObject(Produk.class);
                                produkList.add(produk);
                                Log.d("Firestore", "Produk: " + document.getData());
                            }
                        }

                        // Notifikasi perubahan data ke adapter
                        productAdapter.notifyDataSetChanged();
                        Log.d("Firestore", "Produk terbaru: " + produkList.size() + " item");
                    });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onItemClick(Produk produk) {

    }

    @Override
    public void onEditClick(Produk produk) {
            Intent intent = new Intent(this, UbahProdukActivity.class);

            if (produk != null){
                // Mengirim data produk ke TambahProdukActivity
                intent.putExtra("produk_id", produk.getId());
                intent.putExtra("produk_nama", produk.getNama());
                intent.putExtra("produk_deskripsi", produk.getDeskripsi());
                intent.putExtra("produk_kuantitas", produk.getKuantitas());
                intent.putExtra("produk_ketahanan", produk.getKetahanan());
                intent.putExtra("produk_harga", produk.getHarga());
                intent.putExtra("produk_jenis", produk.getJenis());
                intent.putExtra("produk_image", produk.getImageProduct());
            }



            // Memulai TambahProdukActivity
            startActivity(intent);

    }

    @Override
    public void onDeleteClick(Produk produk) {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Hapus Produk")
                .setMessage("Apakah Anda yakin ingin menghapus produk ini?")
                .setPositiveButton("Ya", (dialog, which) -> {
                    // Ambil ID produk untuk dihapus
                    String produkId = produk.getId();
                    // Hapus produk dari Firestore
                    firestore.collection("products")
                            .document(produkId)
                            .delete()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    // Produk berhasil dihapus
                                    Toast.makeText(this, "Produk berhasil dihapus", Toast.LENGTH_SHORT).show();
                                    // Hapus produk dari daftar produk yang ditampilkan
                                    produkList.remove(produk);
                                    productAdapter.notifyDataSetChanged();
                                } else {
                                    // Gagal menghapus produk
                                    Toast.makeText(this, "Gagal menghapus produk", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(e -> {
                                // Menangani kegagalan
                                Toast.makeText(this, "Terjadi kesalahan: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Batal", null)
                .show();
    }

//    @Override
//    public void onAddToCartClick(Produk produk) {
//
//    }

    @Override
    public void onBuyClick(Produk produk) {

    }
}
