package com.dapittriandidev.patani.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.dapittriandidev.patani.R;
import com.google.firebase.firestore.FirebaseFirestore;

public class UbahProdukActivity extends AppCompatActivity {
    private EditText edtNamaProduk, edtDeskripsiProduk, edtHargaProduk, edtDurabilityProduk, edtKuantitasProduk, edtJenisProduk, edtImageProduct;
    private Button btnSimpan;
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    private int produkKuantitas;
    private String produkId;
    private String produkNama, produkDurability;
    private String produkJenis;
    private String produkDeskripsi;
    private String imageProduct;
    private double produkHarga;
    private boolean isSaving = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ubah_produk);


        // Inisialisasi Views
        edtNamaProduk = findViewById(R.id.edtNamaProduk);
        edtDeskripsiProduk = findViewById(R.id.edtDeskripsiProduk);
        edtHargaProduk = findViewById(R.id.edtHargaProduk);
        edtDurabilityProduk = findViewById(R.id.edtDurabilityProduk);
        edtKuantitasProduk = findViewById(R.id.edtKuantitasProduk);
        edtJenisProduk = findViewById(R.id.edtJenisProduk);
        edtImageProduct = findViewById(R.id.edtImageProduct);
        btnSimpan = findViewById(R.id.btnUpdateProduk);
        ImageView back = findViewById(R.id.back);
        back.setOnClickListener(v -> onBackPressed());

        // Ambil data dari Intent
        produkId = getIntent().getStringExtra("produk_id");
        produkNama = getIntent().getStringExtra("produk_nama");
        produkDeskripsi = getIntent().getStringExtra("produk_deskripsi");
        produkHarga = getIntent().getDoubleExtra("produk_harga", 0);
        produkKuantitas = getIntent().getIntExtra("produk_kuantitas", 0);
        produkDurability = getIntent().getStringExtra("produk_ketahanan");
        produkJenis = getIntent().getStringExtra("produk_jenis");
        imageProduct = getIntent().getStringExtra("produk_image");

        // Menampilkan data produk yang akan diedit
        edtNamaProduk.setText(produkNama);
        edtKuantitasProduk.setText(String.valueOf(produkKuantitas));
        edtDurabilityProduk.setText(produkDurability);
        edtHargaProduk.setText(String.valueOf(produkHarga));
        edtDeskripsiProduk.setText(String.valueOf(produkDeskripsi));
        edtJenisProduk.setText(produkJenis);
        edtImageProduct.setText(imageProduct);

        // Tombol simpan untuk menyimpan perubahan
        btnSimpan.setOnClickListener(v -> {
            if (isSaving) {
                Toast.makeText(this, "Sedang menyimpan produk. Harap tunggu...", Toast.LENGTH_SHORT).show();
                return;
            }
            String nama = edtNamaProduk.getText().toString().trim();
            String jenis = edtJenisProduk.getText().toString().trim();
            String deskripsi = edtDeskripsiProduk.getText().toString().trim();
            int kuantitas = Integer.parseInt(edtKuantitasProduk.getText().toString().trim());
            String ketahanan = edtDurabilityProduk.getText().toString().trim();
            double harga = Double.parseDouble(edtHargaProduk.getText().toString().trim());
            String imageProduct = edtImageProduct.getText().toString().trim();
            isSaving = true; // Set isSaving to true to prevent further clicks

            // Memperbarui data produk di Firestore
            firestore.collection("products")
                    .document(produkId) // ID produk yang ingin diupdate
                    .update("nama", nama, "harga", harga, "jenis", jenis, "deskripsi",deskripsi, "imageProduct", imageProduct, "kuantitas", kuantitas, "ketahanan", ketahanan)
                    .addOnSuccessListener(aVoid -> {
                        isSaving = false; // Set isSaving to true to prevent further clicks
                        Toast.makeText(this, "Produk berhasil diperbarui", Toast.LENGTH_SHORT).show();
                        // Kembali ke KelolaProdukActivity setelah berhasil update
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        isSaving = false; // Set isSaving to true to prevent further clicks
                        Toast.makeText(this, "Gagal memperbarui produk: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });
    }
}
