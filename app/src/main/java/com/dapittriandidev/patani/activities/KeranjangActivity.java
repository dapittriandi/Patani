package com.dapittriandidev.patani.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dapittriandidev.patani.R;
import com.dapittriandidev.patani.adapters.CartAdapter;
import com.dapittriandidev.patani.models.Cart;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KeranjangActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CartAdapter adapter;
    private List<Cart> cartItems;
    private TextView tvTotalHarga;
    private Button buttonCheckout;
    private ImageButton btnBack;

    private FirebaseFirestore firestore;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_keranjang);



        // Initialize Firebase and views
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        recyclerView = findViewById(R.id.keranjangBelanja);
        tvTotalHarga = findViewById(R.id.tvTotalHarga);
        btnBack = findViewById(R.id.ibBack);
        buttonCheckout = findViewById(R.id.buttonCheckout);

        cartItems = new ArrayList<>();
        adapter = new CartAdapter(this, cartItems, this::deleteCartItem);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Load cart items
        loadCartItems();

        // Set click listeners
        btnBack.setOnClickListener(v -> onBackPressed());
        buttonCheckout.setOnClickListener(v -> proceedToCheckout());
    }

    private void loadCartItems() {
        if (auth.getCurrentUser() == null) {
            Log.e("CartDebug", "User is not authenticated");
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        Log.d("CartDebug", "User ID: " + userId);

        // Firestore query
        firestore.collection("carts")
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        cartItems.clear();
                        double totalHarga = 0;

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Cart cart = document.toObject(Cart.class);
                            cartItems.add(cart);
                            totalHarga += cart.getTotalPrice(); // Assume Cart has a getTotalPrice() method
                        }

                        if (cartItems.isEmpty()) {
                            tvTotalHarga.setText("Rp. 0");
                            Toast.makeText(KeranjangActivity.this, "Keranjang kosong.", Toast.LENGTH_SHORT).show();
                        } else {
                            tvTotalHarga.setText("Rp. " + formatCurrency(totalHarga));
                        }

                        adapter.notifyDataSetChanged();
                        Log.d("CartDebug", "Total items loaded: " + cartItems.size());
                    } else {
                        Log.e("CartDebug", "Error loading cart items", task.getException());
                        Toast.makeText(KeranjangActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deleteCartItem(Cart item) {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = auth.getCurrentUser().getUid();

        firestore.collection("carts")
                .whereEqualTo("userId", userId)
                .whereEqualTo("productId", item.getProductId())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            document.getReference().delete()
                                    .addOnSuccessListener(aVoid -> {
                                        // Hapus item dari cartItems dan beri tahu adapter
                                        cartItems.remove(item);
                                        adapter.notifyDataSetChanged();  // Memberi tahu adapter agar RecyclerView ter-update
                                        Toast.makeText(KeranjangActivity.this, "Item deleted", Toast.LENGTH_SHORT).show();
                                        updateTotalPrice();  // Memperbarui total harga
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("CartDebug", "Error deleting item", e);
                                        Toast.makeText(KeranjangActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } else {
                        Log.e("CartDebug", "Error finding item to delete", task.getException());
                        Toast.makeText(KeranjangActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateTotalPrice() {
        double totalHarga = 0;
        for (Cart item : cartItems) {
            totalHarga += item.getTotalPrice();  // Pastikan Cart memiliki metode getTotalPrice()
        }
        tvTotalHarga.setText("Rp. " + formatCurrency(totalHarga));
    }

    private String formatCurrency(double value) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator('.');  // Gunakan titik untuk pemisah ribuan
        symbols.setDecimalSeparator(',');   // Gunakan koma untuk pemisah desimal

        DecimalFormat formatter = new DecimalFormat("#,###.00", symbols);
        return formatter.format(value);
    }

    private void proceedToCheckout() {
        if (cartItems.isEmpty()) {
            Toast.makeText(this, "Keranjang kosong, tidak dapat checkout", Toast.LENGTH_SHORT).show();
            return;
        }

        // Menghitung total harga
        double totalPrice = 0;
        for (Cart item : cartItems) {
            totalPrice += item.getTotalPrice();
        }

        // Kirimkan data ke CheckoutActivity
        Intent intent = new Intent(this, CheckoutActivity.class);
        intent.putExtra("isFromCart", true);
        intent.putParcelableArrayListExtra("cartItems", new ArrayList<>(cartItems)); // Kirimkan daftar Cart
        intent.putExtra("totalPrice", totalPrice); // Kirimkan total harga
        intent.putExtra("userId", auth.getCurrentUser().getUid()); // Kirimkan userId
        startActivity(intent);
    }

}
