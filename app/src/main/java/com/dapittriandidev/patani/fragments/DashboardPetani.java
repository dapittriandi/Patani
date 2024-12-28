package com.dapittriandidev.patani.fragments;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dapittriandidev.patani.NotificationsActivity;
import com.dapittriandidev.patani.R;
import com.dapittriandidev.patani.activities.KelolaProdukActivity;
import com.dapittriandidev.patani.activities.MainActivity;
import com.dapittriandidev.patani.activities.PesananActivity;
import com.dapittriandidev.patani.activities.PesananMasuk;
import com.dapittriandidev.patani.activities.UbahProdukActivity;
import com.dapittriandidev.patani.activities.UserActivity;
import com.dapittriandidev.patani.adapters.ProdukAdapter;
import com.dapittriandidev.patani.models.Produk;
import com.dapittriandidev.patani.utils.BottomNavigationUtil;
import com.dapittriandidev.patani.utils.SharedPrefManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class DashboardPetani extends Fragment implements ProdukAdapter.OnItemClickListener {
    private ImageView btnManageProduct, btnPesananMasuk, statusPesanan;
    private RecyclerView rvProductList;
    private ProdukAdapter productAdapter;
    private List<Produk> produkList = new ArrayList<>();
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private TextView emptyMessage;
    private ProgressBar progressBar;

    public DashboardPetani() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_dashboard_petani, container, false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getActivity().getWindow();
            window.setStatusBarColor(Color.TRANSPARENT);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        // Inisialisasi RecyclerView
        rvProductList = rootView.findViewById(R.id.rvProductList);
        btnManageProduct = rootView.findViewById(R.id.btnManageProduct);
        btnPesananMasuk = rootView.findViewById(R.id.ivPesananMasuk);
        statusPesanan = rootView.findViewById(R.id.statusPesanan);
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);  // 2 columns
        rvProductList.setLayoutManager(layoutManager);
        productAdapter = new ProdukAdapter(getContext(), produkList, this, true);
        rvProductList.setAdapter(productAdapter);
        progressBar = rootView.findViewById(R.id.progressBar);
        emptyMessage = rootView.findViewById(R.id.emptyMessage);
//        rvProductList.setNestedScrollingEnabled(false);
        ImageView btnLogout = rootView.findViewById(R.id.btnLogout);

        btnLogout.setOnClickListener(v -> {
            SharedPrefManager sharedPrefManager = SharedPrefManager.getInstance(requireContext());
            sharedPrefManager.clearSession();

            // Logout dari Firebase Auth
            FirebaseAuth.getInstance().signOut();

            // Navigasi ke halaman login
            startActivity(new Intent(getContext(), MainActivity.class));
            getActivity().finish();
        });

        // Load data produk secara real-time
        loadProductsRealTime();

        btnManageProduct.setOnClickListener(v -> {
            // Pindah ke activity Kelola Produk
            Intent intent = new Intent(getContext(), KelolaProdukActivity.class);
            startActivity(intent);
        });

        btnPesananMasuk.setOnClickListener(v -> {
            // Pindah ke activity Pesanan MAsuk Produk
            Intent intent = new Intent(getContext(), PesananMasuk.class);
            startActivity(intent);
        });

        statusPesanan.setOnClickListener(v -> {
            // Pindah ke activity Pesanan MAsuk Produk
            Intent intent = new Intent(getContext(), PesananMasuk.class);
            startActivity(intent);
        });

        BottomNavigationView bottomNavigationView = rootView.findViewById(R.id.bottomNavigationDashboardPetani);

        // Set listener untuk item navigasi
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                // Navigasi berdasarkan item yang dipilih
                if (itemId == R.id.homePetani) {
//                    Intent intent = new Intent(requireActivity(), HomePembeliActivity.class);
////                    startActivity(intent);
                    return true;
                } else if (itemId == R.id.pesananMasuk) {
                    Intent intent = new Intent(requireActivity(), PesananMasuk.class);
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
        bottomNavigationView.setSelectedItemId(R.id.homePetani);

        return rootView;
    }

    private void loadProductsRealTime() {
        emptyMessage.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Log.e("DashboardFragment", "Pengguna tidak terautentikasi!");
            Toast.makeText(getContext(), "Silakan login terlebih dahulu!", Toast.LENGTH_SHORT).show();
            return;
        }

        String penjualId = currentUser.getUid(); // ID penjual yang sedang login
//        Log.d("Firestore", "Memuat produk untuk penjualId: " + penjualId);

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
                            Log.d("Firestore", "Data kuantitas: " + document.get("kuantitas"));
                            Log.d("Firestore", "Produk ditemukan: " + produk.getNama());
                        }
                    }

                    if (produkList.isEmpty()) {
                        emptyMessage.setVisibility(View.VISIBLE); // Tampilkan pesan kosong jika tidak ada pesanan
                    } else {
                        emptyMessage.setVisibility(View.GONE); // Sembunyikan pesan kosong jika ada pesanan
                    }

                    // Notifikasi perubahan data ke adapter
                    productAdapter.notifyDataSetChanged();

                    progressBar.setVisibility(View.GONE);
//                    Log.d("Firestore", "Produk terbaru: " + produkList.size() + " item");
                });
    }

    @Override
    public void onItemClick(Produk produk) {
        // Placeholder for future implementation
    }

    @Override
    public void onEditClick(Produk produk) {
        // Implementasi untuk mengedit produk
        Log.d("DashboardFragment", "Edit produk: " + produk.getNama());
        Intent intent = new Intent(getActivity(), UbahProdukActivity.class);

        // Mengirim data produk ke UbahProdukActivity
        intent.putExtra("produk_id", produk.getId());
        intent.putExtra("produk_nama", produk.getNama());
        intent.putExtra("produk_deskripsi", produk.getDeskripsi());
        intent.putExtra("produk_harga", produk.getHarga());
        intent.putExtra("produk_ketahanan", produk.getKetahanan());
        intent.putExtra("produk_kuantitas", produk.getKuantitas());
        intent.putExtra("produk_harga", produk.getHarga());
        intent.putExtra("produk_jenis", produk.getJenis());
        intent.putExtra("produk_image", produk.getImageProduct());

        // Memulai UbahProdukActivity
        startActivity(intent);
    }

    @Override
    public void onDeleteClick(Produk produk) {
        // Implementasi untuk menghapus produk
        Log.d("DashboardFragment", "Hapus produk: " + produk.getNama());
        deleteProduct(produk);
    }

    @Override
    public void onBuyClick(Produk produk) {
        // Placeholder for buy implementation
    }

    private void deleteProduct(Produk produk) {
        firestore.collection("products")
                .document(produk.getId()) // ID produk yang akan dihapus
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Produk berhasil dihapus", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Gagal menghapus produk: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
