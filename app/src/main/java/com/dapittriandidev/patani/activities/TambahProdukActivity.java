package com.dapittriandidev.patani.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.dapittriandidev.patani.R;
import com.dapittriandidev.patani.models.Produk;
import com.dapittriandidev.patani.utils.NotificationHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class TambahProdukActivity extends AppCompatActivity {
    private EditText edtNamaProduk, edtDeskripsiProduk, edtHargaProduk, edtDurabilityProduk, edtKuantitasProduk, edtJenisProduk, edtImageProduct;
    private Button btnSimpanProduk;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth auth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tambah_produk);


        edtNamaProduk = findViewById(R.id.edtNamaProduk);
        edtDeskripsiProduk = findViewById(R.id.edtDeskripsiProduk);
        edtHargaProduk = findViewById(R.id.edtHargaProduk);
        edtDurabilityProduk = findViewById(R.id.edtDurabilityProduk);
        edtKuantitasProduk = findViewById(R.id.edtKuantitasProduk);
        edtJenisProduk = findViewById(R.id.edtJenisProduk);
        edtImageProduct = findViewById(R.id.edtImageProduct);
        btnSimpanProduk = findViewById(R.id.btnSimpanProduk);
        ImageView back = findViewById(R.id.back);
        back.setOnClickListener(v -> onBackPressed());

        btnSimpanProduk.setOnClickListener(v -> saveProduct());
    }

    private boolean isSaving = false;
    private void saveProduct() {
        if (isSaving) {
            Toast.makeText(this, "Sedang menyimpan produk. Harap tunggu...", Toast.LENGTH_SHORT).show();
            return;
        }
            String nama = edtNamaProduk.getText().toString().trim();
            String deskripsi = edtDeskripsiProduk.getText().toString().trim();
            double harga = Double.parseDouble(edtHargaProduk.getText().toString().trim());
            String ketahanan = edtDurabilityProduk.getText().toString().trim();
            String kuantitasProduk = edtKuantitasProduk.getText().toString().trim();
            int kuantitas = Integer.parseInt(kuantitasProduk);
            String jenis = edtJenisProduk.getText().toString().trim();
            String imageProduct = edtImageProduct.getText().toString().trim();

            if (nama.isEmpty() || deskripsi.isEmpty() || harga <= 0 || ketahanan.isEmpty() || kuantitasProduk.isEmpty() || jenis.isEmpty() || imageProduct.isEmpty()) {
                Toast.makeText(this, "Mohon isi semua data produk!", Toast.LENGTH_SHORT).show();
                return;
            }

        isSaving = true; // Set isSaving to true to prevent further clicks
        String penjualId = auth.getCurrentUser().getUid();
        String id = db.collection("products").document().getId();


            Produk produk = new Produk(id, nama, deskripsi, harga, ketahanan, kuantitas, jenis, penjualId, imageProduct); // URL foto belum di masukkan
            db.collection("products")
                    .document(id)
                    .set(produk)
                    .addOnSuccessListener(aVoid -> {
                        isSaving = false; // Reset isSaving after success
                        Toast.makeText(this, "Produk berhasil disimpan!", Toast.LENGTH_SHORT).show();

                        kirimNotifikasiKePembeli(nama);
                        // Setelah produk disimpan, pergi ke halaman kelola produk
                         Intent intent = new Intent(this, KelolaProdukActivity.class);
                         startActivity(intent);
                        finish(); // Tutup activity saat ini
                    })
                    .addOnFailureListener(e -> {
                        isSaving = false; // Reset isSaving after success
                        Toast.makeText(this, "Gagal menyimpan produk: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }

    private void kirimNotifikasiKePembeli(String nama) {
        db.collection("users")
                .whereEqualTo("peran", "Pembeli") // Hanya pembeli
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String buyerToken = document.getString("deviceToken"); // Token perangkat pembeli
                            if (buyerToken != null) {
                                NotificationHelper.sendNotification(
                                        this,
                                        buyerToken,
                                        "Produk Baru Tersedia!",
                                        "Produk baru tersedia: " + nama
                                );
                            }
                        }
                    }
                });
    }
}
