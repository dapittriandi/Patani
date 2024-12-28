package com.dapittriandidev.patani.adapters;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dapittriandidev.patani.R;
import com.dapittriandidev.patani.activities.DetailProductActivity;
import com.dapittriandidev.patani.models.Cart;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private List<Cart> cartItems;
    private OnCartItemListener listener;
    private Context context;

    public CartAdapter(Context context,List<Cart> cartItems, OnCartItemListener listener) {
        this.context = context;
        this.cartItems = cartItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_keranjang, parent, false);
        return new CartViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        Cart cart = cartItems.get(position);
        holder.productName.setText(cart.getProductName());
        holder.productPrice.setText("Rp. " + formatCurrency(cart.getProductPrice()));
        holder.productQuantity.setText(cart.getQuantity() + " Kg");
//        holder.totalPrice.setText("Rp. " + cart.getTotalPrice());
        // Menggunakan Glide untuk memuat gambar ke ImageView
        String imageUrl = cart.getImageProduct(); // Mendapatkan URL gambar dari model Cart
        Glide.with(holder.itemView.getContext())
                .load(imageUrl)  // URL gambar yang ingin dimuat
                .placeholder(R.drawable.backgroud)  // Gambar placeholder saat loading
                .error(R.drawable.bug_report)  // Gambar error jika terjadi kesalahan
                .into(holder.imageView);  // Menampilkan gambar di ImageView

        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClicked(cart));

        holder.bind(cart);
        holder.cartItemOption.setOnClickListener(v -> {
            if (cart.getProductId() != null) {
                Intent intent = new Intent(context, DetailProductActivity.class);
                intent.putExtra("produk_id", cart.getProductId());
                context.startActivity(intent);
            } else {
                Toast.makeText(context, "ID produk tidak ditemukan", Toast.LENGTH_SHORT).show();
            }

        });
    }


    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public interface OnCartItemListener {
        void onDeleteClicked(Cart cart);
    }

    private String formatCurrency(double value) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator('.');  // Gunakan titik untuk pemisah ribuan
        symbols.setDecimalSeparator(',');   // Gunakan koma untuk pemisah desimal

        DecimalFormat formatter = new DecimalFormat("#,###.00", symbols);
        return formatter.format(value);
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        private TextView productName, productQuantity, productPrice;
        private LinearLayout cartItemOption;
        private ImageView imageView;
        private Button btnDelete;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            cartItemOption = itemView.findViewById(R.id.cartItemOption);
            productName = itemView.findViewById(R.id.title);
            productQuantity = itemView.findViewById(R.id.kuantitas);
            productPrice = itemView.findViewById(R.id.price);
            imageView = itemView.findViewById(R.id.image);
            btnDelete = itemView.findViewById(R.id.dropProdukFromCart);
        }

        public void bind(Cart cart) {

        }

//        public void bind(Cart cart) {
//            title.setText(cart.getProductName());
//            quantity.setText(cart.getQuantity() + " Kg");
//            price.setText("Rp. " + cart.getTotalPrice());
////            Picasso.get().load(cart.getImageProduct()).into(imageView);
//        }
    }
}


