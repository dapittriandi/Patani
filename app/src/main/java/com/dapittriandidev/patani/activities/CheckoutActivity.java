package com.dapittriandidev.patani.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.dapittriandidev.patani.R;
import com.dapittriandidev.patani.models.Cart;
import com.google.firebase.FirebaseApp;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CheckoutActivity extends AppCompatActivity {

    private EditText edtNamaPenerima, edtAlamat, edtNoHp;
    private RadioButton cbMetodePembayaran;
    private TextView tvTotalHarga;
    private Button buttonBayar;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);


        FirebaseApp.initializeApp(this);

        // Inisialisasi UI
        edtNamaPenerima = findViewById(R.id.edtNamaPenerima);
        edtAlamat = findViewById(R.id.edtAlamat);
        edtNoHp = findViewById(R.id.edtNoHp);
        cbMetodePembayaran = findViewById(R.id.cbMetodePembayaran);
        tvTotalHarga = findViewById(R.id.tvTotalHarga);
        buttonBayar = findViewById(R.id.buttonBayar);

        ImageView btnBack = findViewById(R.id.ibBack);
        btnBack.setOnClickListener(v -> onBackPressed());

        // Inisialisasi Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Progress dialog untuk loading
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Memproses pesanan...");
        progressDialog.setCancelable(false);
        boolean isFromCart = getIntent().getBooleanExtra("isFromCart", false);
        if (isFromCart) {
            // Handle cart checkout
            List<Cart> cartList = getIntent().getParcelableArrayListExtra("cartItems");
            double totalPrice = 0;
            for (Cart cart : cartList) {
                totalPrice += cart.getTotalPrice();  // Calculate total price
            }
            tvTotalHarga.setText("Rp." + formatCurrency(totalPrice));
            buttonBayar.setOnClickListener(v -> handleCheckoutFromCart(cartList));
        } else {
            // Total harga dari intent
            int totalPrice = getIntent().getIntExtra("totalPrice", 0);
            tvTotalHarga.setText("Rp." + formatCurrency(totalPrice));

            buttonBayar.setOnClickListener(v -> handleCheckout(totalPrice));
        }
    }

    private String formatCurrency(double value) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator('.');  // Gunakan titik untuk pemisah ribuan
        symbols.setDecimalSeparator(',');   // Gunakan koma untuk pemisah desimal

        DecimalFormat formatter = new DecimalFormat("#,###.00", symbols);
        return formatter.format(value);
    }

    private void handleCheckoutFromCart(List<Cart> cartList) {
        // Validasi input dari pengguna
        String namaPenerima = edtNamaPenerima.getText().toString().trim();
        String alamat = edtAlamat.getText().toString().trim();
        String noHp = edtNoHp.getText().toString().trim();
        String metodePembayaran = cbMetodePembayaran.isChecked() ? "COD" : "";

        if (TextUtils.isEmpty(namaPenerima) || TextUtils.isEmpty(alamat) || TextUtils.isEmpty(noHp) || TextUtils.isEmpty(metodePembayaran)) {
            showError("Harap lengkapi semua data");
            return;
        }

        if (!noHp.matches("\\d{10,13}")) {
            showError("Nomor HP harus terdiri dari 10-13 angka");
            return;
        }

        // Ambil ID user (pembeliId)
        String pembeliId = auth.getCurrentUser().getUid();

        // Tampilkan loading dialog
        progressDialog.show();

        // Iterasi setiap item di keranjang untuk membuat pesanan
        for (Cart cartItem : cartList) {
            String productId = cartItem.getProductId();
            String productName = cartItem.getProductName();
            String imageProduct = cartItem.getImageProduct();
            double productPrice = cartItem.getProductPrice();
            int quantity = cartItem.getQuantity();
            double totalPrice = cartItem.getTotalPrice();

            // Ambil penjualId dari koleksi "products"
            db.collection("products").document(productId).get()
                    .addOnSuccessListener(productSnapshot -> {
                        if (productSnapshot.exists()) {
                            String penjualId = productSnapshot.getString("penjualId");

                            // Buat data pesanan
                            Map<String, Object> orderData = new HashMap<>();
                            orderData.put("orderId", db.collection("orders").document().getId()); // Auto-generate ID
                            orderData.put("petaniId", penjualId);
                            orderData.put("namaPenerima", namaPenerima);
                            orderData.put("alamat", alamat);
                            orderData.put("noHP", noHp);
                            orderData.put("imageProduct", imageProduct);
                            orderData.put("orderDate", Timestamp.now());
                            orderData.put("estimasi", "Sedang di siapkan");
                            orderData.put("paymentMethod", metodePembayaran);
                            orderData.put("productId", productId);
                            orderData.put("productName", productName);
                            orderData.put("productPrice", productPrice);
                            orderData.put("quantity", quantity);
                            orderData.put("statusPesanan", "pending");
                            orderData.put("totalPrice", totalPrice);
                            orderData.put("pembeliId", pembeliId);

                            // Simpan data pesanan ke Firestore
                            db.collection("orders")
                                    .add(orderData)
                                    .addOnSuccessListener(documentReference -> {
                                        // Update stok produk setelah pesanan berhasil dibuat
                                        updateProductStock(productId, quantity);

                                        // Hapus item dari keranjang
                                        db.collection("carts")
                                                .whereEqualTo("userId", pembeliId)
                                                .whereEqualTo("productId", productId)
                                                .get()
                                                .addOnSuccessListener(querySnapshot -> {
                                                    for (QueryDocumentSnapshot document : querySnapshot) {
                                                        document.getReference().delete()
                                                                .addOnSuccessListener(aVoid -> {
                                                                    Log.d("Checkout", "Item removed from cart: " + productId);
                                                                })
                                                                .addOnFailureListener(e -> {
                                                                    Log.e("Checkout", "Failed to remove item from cart: " + e.getMessage());
                                                                });
                                                    }
                                                })
                                                .addOnFailureListener(e -> {
                                                    Log.e("Checkout", "Failed to find item in cart: " + e.getMessage());
                                                });
                                    })
                                    .addOnFailureListener(e -> {
                                        progressDialog.dismiss();
                                        showError("Gagal membuat pesanan: " + e.getMessage());
                                    });
                        } else {
                            progressDialog.dismiss();
                            showError("Produk tidak ditemukan!");
                        }
                    })
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        showError("Gagal mengambil data produk: " + e.getMessage());
                    });
        }

        // Tutup loading dialog setelah semua operasi selesai
        progressDialog.dismiss();
        Toast.makeText(this, "Pesanan berhasil dibuat dan keranjang diperbarui", Toast.LENGTH_SHORT).show();
        finish(); // Tutup aktivitas setelah checkout selesai
    }


    private void handleCheckout(int totalPrice) {
        // Validasi input
        String namaPenerima = edtNamaPenerima.getText().toString().trim();
        String alamat = edtAlamat.getText().toString().trim();
        String noHp = edtNoHp.getText().toString().trim();
        String metodePembayaran = cbMetodePembayaran.isChecked() ? "COD" : "";

        if (TextUtils.isEmpty(namaPenerima) || TextUtils.isEmpty(alamat) || TextUtils.isEmpty(noHp) || TextUtils.isEmpty(metodePembayaran)) {
            showError("Harap lengkapi semua data");
            return;
        }

        if (!noHp.matches("\\d{10,13}")) { // Validasi nomor HP
            showError("Nomor HP harus terdiri dari 10-13 angka");
            return;
        }

        if (auth.getCurrentUser() == null) {
            showError("Anda harus login terlebih dahulu!");
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        // Ambil ID user (pembeliId)
        String pembeliId = auth.getCurrentUser().getUid();

        // Ambil detail produk dari intent
        String productId = getIntent().getStringExtra("productId");
        String imageProduct = getIntent().getStringExtra("imageProduct");
        String productName = getIntent().getStringExtra("productName");
        int productPrice = getIntent().getIntExtra("productPrice", 0);
        int quantity = getIntent().getIntExtra("quantity", 0);

        // Tampilkan loading dialog
        progressDialog.show();

        // Ambil penjualId dari koleksi "products"
        db.collection("products").document(productId).get()
                .addOnSuccessListener(productSnapshot -> {
                    if (productSnapshot.exists()) {
                        String penjualId = productSnapshot.getString("penjualId");

                        // Buat data pesanan
                        Map<String, Object> orderData = new HashMap<>();
                        orderData.put("orderId", db.collection("orders").document().getId()); // Auto-generate ID
                        orderData.put("petaniId", penjualId);
                        orderData.put("namaPenerima", namaPenerima);
                        orderData.put("alamat", alamat);
                        orderData.put("noHP", noHp);
                        orderData.put("orderDate", Timestamp.now());
                        orderData.put("estimasi", "Sedang di siapkan");
                        orderData.put("paymentMethod", metodePembayaran);
                        orderData.put("productId", productId);
                        orderData.put("imageProduct", imageProduct);
                        orderData.put("productName", productName);
                        orderData.put("productPrice", productPrice);
                        orderData.put("quantity", quantity);
                        orderData.put("statusPesanan", "pending");
                        orderData.put("totalPrice", totalPrice);
                        orderData.put("alamat", alamat);
                        orderData.put("pembeliId", pembeliId);

                        // Simpan data pesanan ke Firestore
                        db.collection("orders")
                                .add(orderData)
                                .addOnSuccessListener(documentReference -> updateProductStock(productId, quantity))
                                .addOnFailureListener(e -> {
                                    progressDialog.dismiss();
                                    showError("Gagal membuat pesanan: " + e.getMessage());
                                });
                    } else {
                        progressDialog.dismiss();
                        showError("Produk tidak ditemukan!");
                    }
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    showError("Gagal mengambil data produk: " + e.getMessage());
                });
    }

    private void updateProductStock(String productId, int quantity) {
        db.runTransaction(transaction -> {
            // Ambil data produk
            DocumentReference productDoc = db.collection("products").document(productId);
            DocumentSnapshot productSnapshot = transaction.get(productDoc);

            // Ambil stok sebagai Long
            Long stockLong = productSnapshot.getLong("kuantitas");
            if (stockLong == null) {
                throw new FirebaseFirestoreException("Stok tidak ditemukan dalam dokumen!", FirebaseFirestoreException.Code.ABORTED);
            }

            // Validasi stok
            if (stockLong < quantity) {
                throw new FirebaseFirestoreException("Stok tidak mencukupi", FirebaseFirestoreException.Code.ABORTED);
            }

            // Kurangi stok
            long newStock = stockLong - quantity;
            transaction.update(productDoc, "kuantitas", newStock); // Pastikan update ke Long
            return null;
        }).addOnSuccessListener(aVoid -> {
            progressDialog.dismiss();
            Toast.makeText(this, "Pesanan berhasil dibuat!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(CheckoutActivity.this, PesananActivity.class);
            startActivity(intent);
            finish();
        }).addOnFailureListener(e -> {
            progressDialog.dismiss();
            showError(e.getMessage().contains("Stok tidak mencukupi") ?
                    "Stok produk tidak mencukupi!" :
                    "Gagal mengupdate stok: " + e.getMessage());
        });
    }


    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
