package com.dapittriandidev.patani.adapters;

import static io.reactivex.rxjava3.core.Single.error;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.dapittriandidev.patani.R;
import com.dapittriandidev.patani.activities.KelolaProdukActivity;
import com.dapittriandidev.patani.models.Produk;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;

public class ProdukAdapter extends RecyclerView.Adapter<ProdukAdapter.ProdukViewHolder> {

    private final Context context;
    private final List<Produk> produkList;
    private final OnItemClickListener onItemClickListener;
    private final boolean isForPetani;

    public void updateData(List<Produk> newProdukList) {
        if (newProdukList != null) {
            this.produkList.clear();
            this.produkList.addAll(newProdukList);
            notifyDataSetChanged();
        }
    }


    // Interface untuk menangani klik item
    public interface OnItemClickListener {
        void onItemClick(Produk produk);
        void onEditClick(Produk produk);
        void onDeleteClick(Produk produk);
//        void onAddToCartClick(Produk produk);
        void onBuyClick(Produk produk);
//        void onBuyClick(Produk produk);
//        void onAddToCartClick(Produk produk);
    }

    // Constructor
    public ProdukAdapter(Context context, List<Produk> produkList, OnItemClickListener onItemClickListener, boolean isForPetani) {
        this.context = context;
        this.produkList = produkList;
        this.onItemClickListener = onItemClickListener;
        this.isForPetani = isForPetani;
    }

    @NonNull
    @Override
    public ProdukViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutId = isForPetani ? R.layout.item_produk_tani : R.layout.item_produk;
        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);
        return new ProdukViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProdukViewHolder holder, int position) {
        Produk produk = produkList.get(position);
        if (produk == null) {
            Log.e("ProdukAdapter", "Produk pada posisi " + position + " adalah null.");
            return;
        }
        if (produk != null) {
            holder.namaProduk.setText(produk.getNama());
            Number harga = produk.getHarga();
            if (harga != null) {
                holder.hargaProduk.setText(String.format("Rp %,d", harga.longValue())); // Menggunakan longValue() jika tipe data Long
            } else {
                holder.hargaProduk.setText("Rp 0");
            }
            holder.jenisProduk.setText(produk.getJenis());

            // Memuat gambar dengan Glide
            Glide.with(context)
                    .load(produk.getImageProduct())
                    .listener(new RequestListener<Drawable>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                    Log.e("GlideError", "Error loading image", e);
                    return false;
                }

                @Override
                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                    return false;
                }
            })// URL gambar dari data
                    .placeholder(R.drawable.greenbean)    // Gambar sementara
                    .error(R.drawable.bug_report)         // Gambar pengganti jika gagal
                    .into(holder.gambarProduk);
        } else {
            Log.e("ProdukAdapter", "Produk pada posisi " + position + " adalah null.");
        }

        if (isForPetani) {
            // Konfigurasi untuk petani (edit dan hapus)
            holder.btnEdit.setVisibility(View.VISIBLE);
            holder.btnDelete.setVisibility(View.VISIBLE);
            holder.btnEdit.setOnClickListener(v -> onItemClickListener.onEditClick(produk));
            holder.btnDelete.setOnClickListener(v -> onItemClickListener.onDeleteClick(produk));
        } else {
            holder.cardViewProduk.setOnClickListener(v -> onItemClickListener.onItemClick(produk));
//            holder.btnBuy.setOnClickListener(v -> onItemClickListener.onBuyClick(produk));
//            holder.ibInputToCart.setOnClickListener(v -> onItemClickListener.onAddToCartClick(produk));
        }

    }

    @Override
    public int getItemCount() {
        return produkList.size();
    }

    // ViewHolder
    public static class ProdukViewHolder extends RecyclerView.ViewHolder {
        public TextView namaProduk;
        public TextView hargaProduk;
        public TextView jenisProduk;
        CardView cardViewProduk;
        public ImageView gambarProduk;
        Button btnEdit,btnDelete, btnBuy;
        ImageButton ibInputToCart;

        public ProdukViewHolder(@NonNull View itemView) {
            super(itemView);
            cardViewProduk = itemView.findViewById(R.id.cardViewProduk);
            namaProduk = itemView.findViewById(R.id.tvProductName);
            jenisProduk = itemView.findViewById(R.id.tvJenisProduk); // Harus sesuai dengan XML
            hargaProduk = itemView.findViewById(R.id.tvProductPrice);
            gambarProduk = itemView.findViewById(R.id.ivProductImage);
            btnEdit = itemView.findViewById(R.id.btnUpdate);
            btnDelete = itemView.findViewById(R.id.btnHapus);
//            btnBuy = itemView.findViewById(R.id.btnBuy);
//            ibInputToCart = itemView.findViewById(R.id.ibInputToCart);
        }
    }
}
