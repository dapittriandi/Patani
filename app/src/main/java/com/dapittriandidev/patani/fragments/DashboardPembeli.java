package com.dapittriandidev.patani.fragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.res.Configuration;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.SearchView;

import com.dapittriandidev.patani.NotificationsActivity;
import com.dapittriandidev.patani.R;
import com.dapittriandidev.patani.ViewModel.ProdukViewModel;
import com.dapittriandidev.patani.activities.DetailProductActivity;
import com.dapittriandidev.patani.activities.KeranjangActivity;
import com.dapittriandidev.patani.activities.MainActivity;
import com.dapittriandidev.patani.activities.PesananActivity;
import com.dapittriandidev.patani.activities.PesananMasuk;
import com.dapittriandidev.patani.activities.UserActivity;
import com.dapittriandidev.patani.activities.listener.ProdukListener;
import com.dapittriandidev.patani.adapters.ProdukAdapter;
import com.dapittriandidev.patani.models.Produk;
import com.dapittriandidev.patani.utils.BottomNavigationUtil;
import com.dapittriandidev.patani.utils.SharedPrefManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class DashboardPembeli extends Fragment implements ProdukAdapter.OnItemClickListener {

    private RecyclerView recyclerViewProduk;
    private ProdukViewModel produkViewModel;
    private ProdukAdapter produkAdapter;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private TextView cartItemCount;
    private BottomNavigationView bottomNavigationView;
    private ProdukListener produkListener;
    private ProgressBar progressBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard_pembeli, container, false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getActivity().getWindow();
            window.setStatusBarColor(Color.TRANSPARENT);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        SearchView searchView = view.findViewById(R.id.searchViewProduct);
        searchView.setIconifiedByDefault(false); // Agar keyboard muncul langsung
//        searchView.requestFocus(); // Berikan fokus ke SearchView
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(searchView, InputMethodManager.SHOW_IMPLICIT);
        bottomNavigationView = view.findViewById(R.id.bottomNavigationBuy);
        progressBar = view.findViewById(R.id.progressBar);

        // Set listener untuk item navigasi
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                // Navigasi berdasarkan item yang dipilih
                if (itemId == R.id.homePembeli) {
//                    Intent intent = new Intent(requireActivity(), HomePembeliActivity.class);
////                    startActivity(intent);
                    return true;
                } else if (itemId == R.id.pesanan) {
                    Intent intent = new Intent(requireActivity(), PesananActivity.class);
                    startActivity(intent);
                    return true;
                } else if (itemId == R.id.notification) {
                    Intent intent = new Intent(requireActivity(), NotificationsActivity.class);
                    startActivity(intent);
                    return true;
                } else if (itemId == R.id.user) {
                    Intent intent = new Intent(requireActivity(), UserActivity.class);
                    startActivity(intent);
                    return true;
                } else {
                    return false;
                }
            }
        });

        // Optionally, set the default selected item (e.g., Home)
        bottomNavigationView.setSelectedItemId(R.id.homePembeli);

        produkListener = new ProdukListener(getContext());
        produkListener.listenForNewProducts();

        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        produkViewModel = new ViewModelProvider(this).get(ProdukViewModel.class);

        ImageView btnCart = view.findViewById(R.id.ivViewCart);
        cartItemCount = view.findViewById(R.id.cartItemCount);

        recyclerViewProduk = view.findViewById(R.id.rvProducts);
        int spanCount = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ? 3 : 2;
        recyclerViewProduk.setLayoutManager(new GridLayoutManager(getContext(), spanCount));

        produkAdapter = new ProdukAdapter(getContext(), new ArrayList<>(), this, false);
        recyclerViewProduk.setAdapter(produkAdapter);
//        recyclerViewProduk.setNestedScrollingEnabled(false);

        ImageButton btnLogout = view.findViewById(R.id.btnLogout);


        btnLogout.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            SharedPrefManager sharedPrefManager = SharedPrefManager.getInstance(requireContext());
                sharedPrefManager.clearSession();

                // Logout dari Firebase Auth
                FirebaseAuth.getInstance().signOut();

                // Navigasi ke halaman login
                startActivity(new Intent(getContext(), MainActivity.class));
                getActivity().finish();
            progressBar.setVisibility(View.GONE);
        });

        produkViewModel.getProdukList().observe(getViewLifecycleOwner(), produkList -> {
            progressBar.setVisibility(View.VISIBLE);

            if (produkList != null && !produkList.isEmpty()) {
                produkAdapter.updateData(produkList);
                progressBar.setVisibility(View.GONE);

            }
        });

        updateCartBadge();

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            firestore.collection("carts").document(userId).collection("products")
                    .addSnapshotListener((snapshots, e) -> {
                        if (e != null) {
                            Log.w("Keranjang", "Listen failed", e);
                            return;
                        }

                        int jumlahProduk = 0;
                        if (snapshots != null && !snapshots.isEmpty()) {
                            for (QueryDocumentSnapshot document : snapshots) {
                                jumlahProduk += document.getLong("jumlah").intValue();
                            }

                            if (jumlahProduk > 0) {
                                cartItemCount.setVisibility(View.VISIBLE);
                                cartItemCount.setText(jumlahProduk > 99 ? "99+" : String.valueOf(jumlahProduk));
                            } else {
                                cartItemCount.setVisibility(View.GONE);
                            }
                        } else {
                            cartItemCount.setVisibility(View.GONE);
                        }
                    });

            btnCart.setOnClickListener(v -> gotoCart());
        }

        produkViewModel.getProdukList().observe(getViewLifecycleOwner(), produkList -> {
            if (produkList != null && !produkList.isEmpty()) {
                produkAdapter.updateData(produkList);
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchProducts(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchProducts(newText);
                return false;
            }
        });


        loadProdukData();

        checkNotificationPermission();
        return view;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getContext(), "Izin notifikasi diberikan", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Izin notifikasi ditolak", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(getContext(), Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.POST_NOTIFICATIONS}, 100);
        }
    }

    private void updateCartBadge() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Log.e("CartBadge", "Pengguna tidak ditemukan");
            return;
        }

        String userId = currentUser.getUid();

        firestore.collection("carts")
                .whereEqualTo("userId", userId)  // Filter berdasarkan userId
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.e("CartBadge", "Gagal memuat keranjang", e);
                        return;
                    }

                    int jumlahProduk = 0;

                    if (snapshots != null && !snapshots.isEmpty()) {
                        for (QueryDocumentSnapshot document : snapshots) {
                            Long jumlah = document.getLong("quantity");
                            if (jumlah != null) {
                                jumlahProduk += jumlah.intValue();
                            }
                        }
                        Log.d("CartBadge", "Jumlah produk di keranjang: " + jumlahProduk);
                        // Update jumlah produk di cart jika ada perubahan
                        if (jumlahProduk > 0) {
                            cartItemCount.setVisibility(View.VISIBLE);
                            cartItemCount.setText(jumlahProduk > 99 ? "99+" : String.valueOf(jumlahProduk));
                            Log.d("CartBadge", "Visibilitas cartItemCount: VISIBLE");
                        } else {
                            cartItemCount.setVisibility(View.GONE);

                        }
                    } else {
                        cartItemCount.setVisibility(View.GONE);
                        Log.d("CartBadge", "Visibilitas cartItemCount: GONE");
                    }
                });
    }


    private void gotoCart() {
        progressBar.setVisibility(View.VISIBLE);
        Intent intent = new Intent(getContext(), KeranjangActivity.class);
        startActivity(intent);
        progressBar.setVisibility(View.GONE);
    }

    private void loadProdukData() {
        progressBar.setVisibility(View.VISIBLE);
        firestore.collection("products").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Produk> produkList = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Produk produk = new Produk();
                        produk.setId(document.getId());
                        produk.setNama(document.getString("nama"));
                        produk.setJenis(document.getString("jenis"));
                        Double harga = document.getDouble("harga");
                        if (harga != null) {
                            produk.setHarga(harga);
                        } else {
                            produk.setHarga(0.0);
                        }

                        // Ambil URL gambar produk (jika ada)
                        String imageUrl = document.getString("imageProduct");
                        if (!TextUtils.isEmpty(imageUrl)) {
                            produk.setImageProduct(imageUrl);
                        } else {
                            produk.setImageProduct(""); // Default kosong jika tidak ada URL
                        }

                        produkList.add(produk);
                    }
                    produkViewModel.setProdukList(produkList);
                    progressBar.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> Log.e("FirestoreError", "Error loading products", e));
    }

    private void searchProducts(String query) {
        progressBar.setVisibility(View.VISIBLE);
        List<Produk> allProducts = produkViewModel.getProdukList().getValue();
        if (allProducts == null || allProducts.isEmpty()) {
            Toast.makeText(getContext(), "Produk belum dimuat", Toast.LENGTH_SHORT).show();
            return;
        }

        List<Produk> filteredList = new ArrayList<>();
        for (Produk produk : allProducts) {
            if (produk.getNama().toLowerCase().contains(query.toLowerCase()) ||
                    produk.getJenis().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(produk);
            }
            progressBar.setVisibility(View.GONE);
        }


        produkAdapter.updateData(filteredList);
        progressBar.setVisibility(View.GONE);
        if (filteredList.isEmpty()) {
            Toast.makeText(getContext(), "Produk tidak ditemukan", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onItemClick(Produk produk) {
        progressBar.setVisibility(View.VISIBLE);
        if (produk.getId() != null) {
            Intent intent = new Intent(getContext(), DetailProductActivity.class);
            intent.putExtra("produk_id", produk.getId());
            progressBar.setVisibility(View.GONE);
            startActivity(intent);
        } else {
            Toast.makeText(getContext(), "ID produk tidak ditemukan", Toast.LENGTH_SHORT).show();
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
