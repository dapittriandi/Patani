package com.dapittriandidev.patani.ViewModel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.dapittriandidev.patani.models.Produk;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ProdukViewModel extends ViewModel {
    private MutableLiveData<List<Produk>> produkList = new MutableLiveData<>();
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    public ProdukViewModel() {
        loadProdukData();
    }

    private void loadProdukData() {
        firestore.collection("products")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Produk> produkListTemp = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Produk produk = new Produk();
                        produk.setNama(document.getString("nama"));
                        produk.setJenis(document.getString("jenis"));
                        Double harga = document.getDouble("harga");
                        if (harga != null) {
                            produk.setHarga(harga);
                        } else {
                            produk.setHarga(0.0);
                        }
                        produkListTemp.add(produk);
                    }
                    produkList.setValue(produkListTemp); // Update LiveData
                })
                .addOnFailureListener(e -> {
                    Log.e("ProdukViewModel", "Gagal memuat data: " + e.getMessage());
                });
    }

    public LiveData<List<Produk>> getProdukList() {
        return produkList; // Mengembalikan LiveData untuk observer
    }

    // Fungsi untuk memperbarui produk list secara manual
    public void setProdukList(List<Produk> produkList) {
        this.produkList.setValue(produkList); // Mengupdate LiveData
    }
}
