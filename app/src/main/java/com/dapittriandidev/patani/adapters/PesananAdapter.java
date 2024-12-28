package com.dapittriandidev.patani.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.dapittriandidev.patani.R;
import com.dapittriandidev.patani.models.Pesanan;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PesananAdapter extends RecyclerView.Adapter<PesananAdapter.OrderViewHolder> {

    private List<Pesanan> pesananList;
    private Context context;
    private boolean isPetani;
    private OnPesananActionListener listener;

    public PesananAdapter(List<Pesanan> pesananList, Context context, boolean isPetani, OnPesananActionListener listener) {
        this.pesananList = pesananList;
        this.context = context;
        this.isPetani = isPetani;
        this.listener = listener;
    }

    public void setIsPetani(boolean isPetani) {
        this.isPetani = isPetani;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Memilih layout sesuai dengan status petani atau pembeli
        View view;
        if (isPetani) {
            view = LayoutInflater.from(context).inflate(R.layout.item_pesanan_masuk, parent, false);
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.item_pesanan_saya, parent, false);
        }
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Pesanan pesanan = pesananList.get(position);

        // Format tanggal
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
        String formattedDate = dateFormat.format(pesanan.getOrderDate().toDate());
        holder.codePesanan.setText(pesanan.getOrderId());
        holder.namaPembeli.setText(pesanan.getNamaPenerima());
        holder.title.setText(pesanan.getProductName());
        holder.price.setText("Rp." + formatCurrency(pesanan.getTotalPrice()));
        holder.kuantitas.setText(String.valueOf(pesanan.getQuantity()) + "Kg");
        holder.metodePembayaran.setText(pesanan.getPaymentMethod());
        holder.noHp.setText(pesanan.getNoHP());
        holder.hargaSatuan.setText("Rp." + formatCurrency(pesanan.getProductPrice()));
        holder.alamat.setText(pesanan.getAlamat());
        Date dateOrder = pesanan.getOrderDate().toDate();
        SimpleDateFormat sdfOrder = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());
        holder.tanggalPesanan.setText(sdfOrder.format(dateOrder));
        holder.estimasi.setText(pesanan.getEstimasi() != null ? pesanan.getEstimasi() : "Estimasi sedang disiapkan");
//        // Format tanggalPesananDiterima jika tidak null
        if (pesanan.getTanggalPesananDiterima() != null) {
            Date date = pesanan.getTanggalPesananDiterima().toDate();
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());
            holder.statusPesanan.setText(pesanan.getStatusPesanan() + " Pada " + sdf.format(date));
        } else {
            holder.statusPesanan.setText(pesanan.getStatusPesanan());
        }
        // Menampilkan gambar produk (Jika diperlukan)
         Glide.with(context).load(pesanan.getImageProduct()).into(holder.image);

        // Menyesuaikan tombol untuk petani atau pembeli
        if (isPetani) {
            configureForPetani(holder, pesanan);
        } else {
            configureForPembeli(holder, pesanan);
        }
    }

    private String formatCurrency(double value) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator('.');  // Gunakan titik untuk pemisah ribuan
        symbols.setDecimalSeparator(',');   // Gunakan koma untuk pemisah desimal

        DecimalFormat formatter = new DecimalFormat("#,###.00", symbols);
        return formatter.format(value);
    }

    private void configureForPetani(OrderViewHolder holder, Pesanan pesanan) {

        if ("Produk Diterima oleh pembeli, pesanan selesai".equals(pesanan.getStatusPesanan())) {
            holder.konfirmasiProduk.setVisibility(View.GONE);
            holder.kirimProduk.setVisibility(View.GONE);
            holder.cancelPesanan.setVisibility(View.GONE);
//            // Menggunakan Glide untuk memuat gambar ke ImageView
////            String imageUrl = cart.getImageProduct(); // Mendapatkan URL gambar dari model Cart
//            Glide.with(context)
//                    .load(pesanan.getImageProduct())  // URL gambar yang ingin dimuat
//                    .placeholder(R.drawable.backgroud)  // Gambar placeholder saat loading
//                    .error(R.drawable.bug_report)  // Gambar error jika terjadi kesalahan
//                    .into(holder.image);  // Menampilkan gambar di ImageView// Sembunyikan tombol jika statusnya sudah diterima
        } else if ("Ditolak, karena stok habis atau sebagainya".equals(pesanan.getStatusPesanan())) {
            holder.konfirmasiProduk.setVisibility(View.GONE);
            holder.kirimProduk.setVisibility(View.GONE);
            holder.cancelPesanan.setVisibility(View.GONE);
        } else if ("Pesanan dibatalkan oleh pembeli.".equals(pesanan.getStatusPesanan())) {
            holder.konfirmasiProduk.setVisibility(View.GONE);
            holder.kirimProduk.setVisibility(View.GONE);
            holder.cancelPesanan.setVisibility(View.GONE);
        } else {
            holder.konfirmasiProduk.setVisibility(View.VISIBLE);
            holder.kirimProduk.setVisibility(View.VISIBLE);
            holder.cancelPesanan.setVisibility(View.VISIBLE); // Tampilkan tombol jika status pesanan belum diterima
        }


        // Menambahkan listener untuk tombol petani
        holder.konfirmasiProduk.setOnClickListener(v -> listener.onKonfirmasiPesanan(pesanan));
        holder.kirimProduk.setOnClickListener(v -> listener.onKirimPesanan(pesanan));
        holder.cancelPesanan.setOnClickListener(v -> listener.onTolakPesanan(pesanan));
    }

    private void configureForPembeli(OrderViewHolder holder, Pesanan pesanan) {
        // Hide the button if the order status is "Diterima oleh pembeli, pesanan selesai"
        if ("Produk Diterima oleh pembeli, pesanan selesai".equals(pesanan.getStatusPesanan())) {
            holder.produkDiterima.setVisibility(View.GONE);  // Sembunyikan tombol jika statusnya sudah diterima
            holder.batalkanPesanan.setVisibility(View.GONE);
        }  else if ("Ditolak, karena stok habis atau sebagainya".equals(pesanan.getStatusPesanan())) {
            holder.produkDiterima.setVisibility(View.GONE);
            holder.batalkanPesanan.setVisibility(View.GONE);
        } else if ("Pesanan dibatalkan oleh pembeli.".equals(pesanan.getStatusPesanan())) {
            holder.produkDiterima.setVisibility(View.GONE);
            holder.batalkanPesanan.setVisibility(View.GONE);
        } else {
            holder.produkDiterima.setVisibility(View.VISIBLE); // Tampilkan tombol jika status pesanan belum diterima
            holder.batalkanPesanan.setVisibility(View.VISIBLE);
        }

        // Menambahkan listener untuk tombol pembeli
        holder.produkDiterima.setOnClickListener(v -> listener.onPesananDiterima(pesanan));
        holder.batalkanPesanan.setOnClickListener(v -> listener.onPesananDibatalkan(pesanan));
    }

    @Override
    public int getItemCount() {
        return pesananList.size();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {

        TextView namaPembeli, title, price, statusPesanan, estimasi, alamat, noHp, hargaSatuan, tanggalPesanan, metodePembayaran, kuantitas, codePesanan;
        ImageView image;
        Button produkDiterima, konfirmasiProduk, kirimProduk, cancelPesanan, batalkanPesanan;

        public OrderViewHolder(View itemView) {
            super(itemView);
            codePesanan = itemView.findViewById(R.id.tvCodePesanan);
            namaPembeli = itemView.findViewById(R.id.namaPembeli);
            title = itemView.findViewById(R.id.title);
            price = itemView.findViewById(R.id.price);
            statusPesanan = itemView.findViewById(R.id.statusPesanan);
            estimasi = itemView.findViewById(R.id.estimasi);
            alamat = itemView.findViewById(R.id.tvAlamat);
            noHp = itemView.findViewById(R.id.tvNoHp);
            hargaSatuan = itemView.findViewById(R.id.tvHargaPerProduct);
            tanggalPesanan = itemView.findViewById(R.id.tvTglPemesanan);
            metodePembayaran = itemView.findViewById(R.id.tvMetodePembayaran);
            kuantitas = itemView.findViewById(R.id.tvQty);
            image = itemView.findViewById(R.id.image);
            produkDiterima = itemView.findViewById(R.id.produkDiterima);
            konfirmasiProduk = itemView.findViewById(R.id.konfirmasiProduk);
            kirimProduk = itemView.findViewById(R.id.KirimProduk);
            cancelPesanan = itemView.findViewById(R.id.cancelPesanan);
            batalkanPesanan = itemView.findViewById(R.id.produkDibatalkan);

        }
    }

    public interface OnPesananActionListener {
        void onPesananDiterima(Pesanan pesanan);
        void onKonfirmasiPesanan(Pesanan pesanan);
        void onKirimPesanan(Pesanan pesanan);
        void onTolakPesanan(Pesanan pesanan);
        void onPesananDibatalkan(Pesanan pesanan);
    }
}
