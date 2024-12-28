package com.dapittriandidev.patani.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dapittriandidev.patani.R;
import com.dapittriandidev.patani.adapters.ProdukAdapter;
import com.dapittriandidev.patani.models.Produk;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Map;

public class DetailProductActivity extends AppCompatActivity implements ProdukAdapter.OnItemClickListener {
    private TextView tvProductName, tvJenisProduk, tvProductPrice, tvKuantitas, tvDeskripsiProduk, tvKetahanan;
    private String produkId, userId;
    private String imageProduct, productName, productDescription, sellerId;
    private int productPrice, productStock;
    private ImageView ivProductImage;
    private Button btnBuy;
    private ImageButton ibInputToCart, btnBack;
    private RecyclerView rvProducts;
    private FirebaseFirestore firestore;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);



        // Inisialisasi UI
        tvProductName = findViewById(R.id.tvProductName);
        tvJenisProduk = findViewById(R.id.tvJenisProduk);
        tvProductPrice = findViewById(R.id.tvProductPrice);
        tvKuantitas = findViewById(R.id.tvKuantitas);
        tvDeskripsiProduk = findViewById(R.id.tvDeskripsiProduk);
        tvKetahanan = findViewById(R.id.tvKetahanan);
        ivProductImage = findViewById(R.id.ivProductImage);
        btnBuy = findViewById(R.id.btnBuy);
        btnBack = findViewById(R.id.back);
        ibInputToCart =findViewById(R.id.ibInputToCart);
        rvProducts = findViewById(R.id.rvProducts);

        firestore = FirebaseFirestore.getInstance();

        // Ambil ID produk dari intent
        produkId = getIntent().getStringExtra("produk_id");
        userId = getIntent().getStringExtra("userId");
        productStock = getIntent().getIntExtra("", 0);

        if (produkId != null) {
            loadProductDetails(produkId);
        }else {
            Toast.makeText(this, "Data produk tidak valid!", Toast.LENGTH_SHORT).show();
            finish();
        }



        btnBack.setOnClickListener(v -> onBackPressed());

        btnBuy.setOnClickListener(v -> showQuantityPopup());

        ibInputToCart.setOnClickListener(v -> {
            if (produkId != null) {
                addToCart(produkId);
            } else if (produkId != null && productStock == 0) {
                Toast.makeText(DetailProductActivity.this, "Stock produk sedang habis", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(DetailProductActivity.this, "ID produk tidak ditemukan", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void showQuantityPopup() {
        // Menampilkan dialog pop-up untuk jumlah pembelian
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.popup_quantity, null);
        dialogBuilder.setView(dialogView);

        EditText edtQuantity = dialogView.findViewById(R.id.edtJumlah);
        Button btnConfirm = dialogView.findViewById(R.id.btnLanjutkan);
        Button btnCancled =  dialogView.findViewById(R.id.btnBatal);

        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();

        btnConfirm.setOnClickListener(v -> {
            String quantityInput = edtQuantity.getText().toString().trim();

            // Validasi input jumlah
            if (quantityInput.isEmpty()) {
                Toast.makeText(this, "Harap masukkan jumlah pembelian", Toast.LENGTH_SHORT).show();
                return;
            }

            int quantity = Integer.parseInt(quantityInput);

            // Validasi kuantitas produk
            if (quantity > productStock) {
                Toast.makeText(this, "Jumlah pembelian melebihi stok produk!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Jika validasi berhasil, lanjutkan ke halaman Checkout
            proceedToCheckout(quantity);
            alertDialog.dismiss();
        });

        btnCancled.setOnClickListener(v -> alertDialog.dismiss());
    }

    private void proceedToCheckout(int quantity) {
        if (productName == null || productPrice == 0 || produkId == null) {
            Toast.makeText(this, "Data produk tidak valid, coba lagi.", Toast.LENGTH_SHORT).show();
            return;
        }

        int totalPrice = quantity * productPrice;

        Intent intent = new Intent(this, CheckoutActivity.class);
        intent.putExtra("productId", produkId);
        intent.putExtra("productName", productName);
        intent.putExtra("productPrice", productPrice);
        intent.putExtra("quantity", quantity);
        intent.putExtra("totalPrice", totalPrice);
        intent.putExtra("penjualId", sellerId);
        intent.putExtra("imageProduct", imageProduct);
        intent.putExtra("userId", userId);
        startActivity(intent);
    }

    private void addToCart(final String produkId) {
        // Menampilkan dialog untuk menyesuaikan kuantitas
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pilih Kuantitas");

        // Input untuk kuantitas
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        builder.setPositiveButton("Tambah", (dialog, which) -> {
            String kuantitasString = input.getText().toString();
            if (!kuantitasString.isEmpty()) {
                int kuantitas = Integer.parseInt(kuantitasString);
                addProductToCart(produkId, kuantitas); // Fungsi untuk menambahkan produk ke keranjang
            } else {
                Toast.makeText(DetailProductActivity.this, "Kuantitas tidak valid", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Batal", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void addProductToCart(String produkId, int kuantitas) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        firestore.collection("products").document(produkId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Produk produk = documentSnapshot.toObject(Produk.class);

                        if (produk != null) {
                            // Cek apakah produk dengan productId sudah ada di keranjang user
                            firestore.collection("carts")
                                    .whereEqualTo("userId", userId)
                                    .whereEqualTo("productId", produkId)
                                    .get()
                                    .addOnSuccessListener(queryDocumentSnapshots -> {
                                        if (!queryDocumentSnapshots.isEmpty()) {
                                            // Jika produk sudah ada, ambil dokumen pertama yang ditemukan
                                            DocumentSnapshot existingCartItem = queryDocumentSnapshots.getDocuments().get(0);

                                            // Ambil kuantitas yang sudah ada
                                            long existingQuantity = existingCartItem.getLong("quantity");
                                            long newQuantity = existingQuantity + kuantitas;

                                            // Update kuantitas yang ada
                                            Map<String, Object> updatedCartItem = new HashMap<>();
                                            updatedCartItem.put("quantity", newQuantity);
                                            updatedCartItem.put("totalPrice", produk.getHarga() * newQuantity); // Update totalPrice

                                            // Update dokumen keranjang dengan kuantitas yang baru
                                            firestore.collection("carts")
                                                    .document(existingCartItem.getId())
                                                    .update(updatedCartItem)
                                                    .addOnSuccessListener(aVoid -> {
                                                        Toast.makeText(DetailProductActivity.this, "Kuantitas diperbarui", Toast.LENGTH_SHORT).show();
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        Toast.makeText(DetailProductActivity.this, "Gagal memperbarui kuantitas", Toast.LENGTH_SHORT).show();
                                                    });
                                        } else {
                                            // Jika produk belum ada, buat cartItem baru
                                            Map<String, Object> cartItem = new HashMap<>();
                                            cartItem.put("userId", userId);
                                            cartItem.put("productId", produkId);
                                            cartItem.put("productName", produk.getNama());
                                            cartItem.put("productPrice", produk.getHarga());
                                            cartItem.put("imageProduct", produk.getImageProduct());
                                            cartItem.put("quantity", kuantitas);
                                            cartItem.put("totalPrice", produk.getHarga() * kuantitas); // Total harga sesuai kuantitas

                                            // Menambahkan cartItem baru ke Firestore
                                            firestore.collection("carts")
                                                    .add(cartItem)
                                                    .addOnSuccessListener(documentReference -> {
                                                        // Mendapatkan cartId yang di-generate oleh Firestore
                                                        String cartId = documentReference.getId(); // ID otomatis dari Firestore

                                                        // Menambahkan cartId ke dalam data cartItem
                                                        Map<String, Object> updatedCartItem = new HashMap<>(cartItem);
                                                        updatedCartItem.put("cartId", cartId); // Menambahkan cartId ke data

                                                        // Update dokumen dengan cartId
                                                        documentReference.update(updatedCartItem)
                                                                .addOnSuccessListener(aVoid -> {
                                                                    Toast.makeText(DetailProductActivity.this, "Produk berhasil ditambahkan ke keranjang", Toast.LENGTH_SHORT).show();
                                                                })
                                                                .addOnFailureListener(e -> {
                                                                    Toast.makeText(DetailProductActivity.this, "Gagal memperbarui cartId", Toast.LENGTH_SHORT).show();
                                                                });
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        Toast.makeText(DetailProductActivity.this, "Gagal menambahkan ke keranjang", Toast.LENGTH_SHORT).show();
                                                    });
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(DetailProductActivity.this, "Gagal mengecek keranjang", Toast.LENGTH_SHORT).show();
                                    });
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(DetailProductActivity.this, "Gagal memuat produk", Toast.LENGTH_SHORT).show();
                });
    }





    private void loadProductDetails(String produkId) {
        firestore.collection("products").document(produkId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Produk produk = documentSnapshot.toObject(Produk.class);

                        if (produk != null) {
                            productName = produk.getNama();
                            productDescription = produk.getDeskripsi();
                            productPrice = produk.getHarga().intValue();
                            productStock = Integer.parseInt(String.valueOf(produk.getKuantitas()));
                            imageProduct = produk.getImageProduct();
                            sellerId = produk.getPenjualId();

                            // Logging
                            System.out.println("Produk dimuat: " + productName + ", Harga: " + productPrice + ", Stok: " + productStock);

                            // Bind data produk ke UI
                            tvProductName.setText(produk.getNama());
                            tvJenisProduk.setText("Jenis: " + produk.getJenis());
                            tvProductPrice.setText("Rp " + formatCurrency(produk.getHarga()));
                            tvKuantitas.setText(String.valueOf(produk.getKuantitas() + "Kg"));
                            tvDeskripsiProduk.setText(produk.getDeskripsi());
                            tvKetahanan.setText(produk.getKetahanan());
                            Glide.with(this).load(produk.getImageProduct()).into(ivProductImage);
                            loadRelatedProducts();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(DetailProductActivity.this, "Gagal memuat detail produk", Toast.LENGTH_SHORT).show();
                });
    }

    private String formatCurrency(double value) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator('.');  // Gunakan titik untuk pemisah ribuan
        symbols.setDecimalSeparator(',');   // Gunakan koma untuk pemisah desimal

        DecimalFormat formatter = new DecimalFormat("#,###.00", symbols);
        return formatter.format(value);
    }

    //menampilkan produk lainnya
    // Tambahkan ini di dalam onCreate() atau method loadRelatedProducts
    private void loadRelatedProducts() {
        // Mengambil data produk lainnya dari Firestore dan menampilkannya di RecyclerView
        Query query = firestore.collection("products").limit(6); // Mengambil 5 produk lainnya
        FirestoreRecyclerOptions<Produk> options = new FirestoreRecyclerOptions.Builder<Produk>()
                .setQuery(query, Produk.class)
                .build();

        // in loadRelatedProducts
        FirestoreRecyclerAdapter<Produk, ProdukAdapter.ProdukViewHolder> adapter = new FirestoreRecyclerAdapter<Produk, ProdukAdapter.ProdukViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ProdukAdapter.ProdukViewHolder holder, int position, @NonNull Produk model) {
                holder.namaProduk.setText(model.getNama());
                holder.hargaProduk.setText("Rp " + formatCurrency(model.getHarga()));
                holder.jenisProduk.setText(model.getJenis());
                // If you want to display an image
                 Glide.with(DetailProductActivity.this).load(model.getImageProduct()).into(holder.gambarProduk);

                // Handle click events for individual products
                holder.itemView.setOnClickListener(v -> onItemClick(model));
            }

            @NonNull
            @Override
            public ProdukAdapter.ProdukViewHolder onCreateViewHolder(@NonNull ViewGroup group, int i) {
                View view = LayoutInflater.from(group.getContext()).inflate(R.layout.item_produk, group, false);
                return new ProdukAdapter.ProdukViewHolder(view);
            }
        };


        rvProducts.setLayoutManager(new GridLayoutManager(this, 2)); // Menampilkan dalam grid
        rvProducts.setAdapter(adapter);
        adapter.startListening();

//        // Mengambil data produk lainnya dari Firestore dan menampilkannya di RecyclerView
//        Query query = firestore.collection("products").limit(5); // Mengambil 5 produk lainnya
//        FirestoreRecyclerOptions<Produk> options = new FirestoreRecyclerOptions.Builder<Produk>()
//                .setQuery(query, Produk.class)
//                .build();
//
//        FirestoreRecyclerAdapter<Produk, ProdukAdapter.ProdukViewHolder> adapter = new FirestoreRecyclerAdapter<Produk, ProdukAdapter.ProdukViewHolder>(options) {
//            @Override
//            protected void onBindViewHolder(@NonNull ProdukAdapter.ProdukViewHolder holder, int position, @NonNull Produk model) {
//                holder.namaProduk.setText(model.getNama());
//                holder.hargaProduk.setText("Rp " + formatCurrency(model.getHarga()));
//                holder.jenisProduk.setText(model.getJenis());
//                // Jika ingin menampilkan gambar
////                Glide.with(DetailProductActivity.this).load(model.getImageUrl()).into(holder.gambarProduk);
//            }
//
//            @NonNull
//            @Override
//            public ProdukAdapter.ProdukViewHolder onCreateViewHolder(@NonNull ViewGroup group, int i) {
//                View view = LayoutInflater.from(group.getContext()).inflate(R.layout.item_produk, group, false);
//                return new ProdukAdapter.ProdukViewHolder(view);
//            }
//        };
//
//        rvProducts.setLayoutManager(new GridLayoutManager(this, 2)); // Menampilkan dalam grid
//        rvProducts.setAdapter(adapter);
//        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Pastikan untuk berhenti mendengarkan saat activity berhenti
        if (rvProducts.getAdapter() != null) {
            ((FirestoreRecyclerAdapter) rvProducts.getAdapter()).stopListening();
        }
    }


    @Override
    public void onItemClick(Produk produk) {
        if (produk.getId() != null) {
            loadProductDetails(produk.getId());
        } else {
            Toast.makeText(this, "ID produk tidak ditemukan", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onEditClick(Produk produk) {

    }

    @Override
    public void onDeleteClick(Produk produk) {

    }

    @Override
    public void onBuyClick(Produk produk) {

    }
}