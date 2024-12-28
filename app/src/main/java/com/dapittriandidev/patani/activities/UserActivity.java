package com.dapittriandidev.patani.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.dapittriandidev.patani.NotificationsActivity;
import com.dapittriandidev.patani.R;
import com.dapittriandidev.patani.fragments.DashboardPembeli;
import com.dapittriandidev.patani.utils.BottomNavigationUtil;
import com.dapittriandidev.patani.utils.SharedPrefManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class UserActivity extends AppCompatActivity {
    private TextView tvNamaPengguna, tvRole, tvEmail, tvTtl, tvAlamat, profileImage;
    private Button btnEditProfile;
    private ImageView btnLogout;
    FirebaseAuth auth;
    FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(Color.TRANSPARENT);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        // Inisialisasi View
        tvNamaPengguna = findViewById(R.id.tvNamaPengguna);
        tvEmail = findViewById(R.id.tvEmail);
        tvRole = findViewById(R.id.tvRole);
        tvTtl = findViewById(R.id.tvTtl);
        tvAlamat = findViewById(R.id.tvAlamat);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnLogout = findViewById(R.id.btnLogout);

        // Ambil UID pengguna yang sedang login
        String userId = auth.getCurrentUser().getUid();

        // Ambil data dari Firestore
        getUserData(userId);

        BottomNavigationView bottomNavigationView;
        bottomNavigationView = findViewById(R.id.bottomNavigationUser);

        // Set listener untuk item navigasi
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                // Navigasi berdasarkan item yang dipilih
                if (itemId == R.id.homePembeli) {
                    Intent intent = new Intent(UserActivity.this, DashboardPembeli.class);
                    startActivity(intent);
                    return true;
                } else if (itemId == R.id.pesanan) {
                    Intent intent = new Intent(UserActivity.this, PesananActivity.class);
                    startActivity(intent);
                    return true;
                } else if (itemId == R.id.notification) {
                    Intent intent = new Intent(UserActivity.this, NotificationsActivity.class);
                    startActivity(intent);
                    return true;
                } else if (itemId == R.id.user) {
//                    Intent intent = new Intent(UserActivity.this, UserActivity.class);
//                    startActivity(intent);
                    return true;
                } else {
                    return false;
                }
            }
        });

        // Optionally, set the default selected item (e.g., Home)
        bottomNavigationView.setSelectedItemId(R.id.user);

        btnEditProfile.setOnClickListener(v -> showEditProfileDialog());

        btnLogout.setOnClickListener(v -> {
            SharedPrefManager sharedPrefManager = SharedPrefManager.getInstance(this);
            sharedPrefManager.clearSession();

            // Logout dari Firebase Auth
            FirebaseAuth.getInstance().signOut();

            // Navigasi ke halaman login
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }

    private void showEditProfileDialog() {
        // Buat dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edit_profile, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.show();

        // Inisialisasi View dalam dialog
        TextInputEditText etName = dialogView.findViewById(R.id.etName);
        TextInputEditText etEmail = dialogView.findViewById(R.id.etEmail);
        TextInputEditText etRole = dialogView.findViewById(R.id.etRole);
        TextInputEditText etBirthDate = dialogView.findViewById(R.id.etBirthDate);
        TextInputEditText etAddress = dialogView.findViewById(R.id.etAddress);

        // Pre-fill data
        etName.setText(tvNamaPengguna.getText().toString());
        etEmail.setText(tvEmail.getText().toString());
        etRole.setText(tvRole.getText().toString());
        etBirthDate.setText(tvTtl.getText().toString());
        etAddress.setText(tvAlamat.getText().toString());

        // Simpan data ke Firestore
        Button btnSave = dialogView.findViewById(R.id.btnSave);
        btnSave.setOnClickListener(v1 -> {
            String name = etName.getText().toString();
            String email = etEmail.getText().toString();
            String role = etRole.getText().toString();
            String birthDate = etBirthDate.getText().toString();
            String address = etAddress.getText().toString();

            // Validasi data (opsional)
            if (name.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Nama dan email harus diisi!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Update data ke Firestore
            updateUserData(name, email, role, birthDate, address);

            dialog.dismiss();
        });
    }

    private void updateUserData(String name, String email, String role, String birthDate, String address) {
        String userId = auth.getCurrentUser().getUid();

        Map<String, Object> userUpdates = new HashMap<>();
        userUpdates.put("namaLengkap", name);
        userUpdates.put("email", email);
        userUpdates.put("peran", role);
        userUpdates.put("TTL", birthDate);
        userUpdates.put("alamatUser", address);

        db.collection("users").document(userId)
                .update(userUpdates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Profil berhasil diperbarui!", Toast.LENGTH_SHORT).show();

                    // Update UI
                    tvNamaPengguna.setText(name);
                    tvEmail.setText(email);
                    tvRole.setText(role);
                    tvTtl.setText(birthDate);
                    tvAlamat.setText(address);
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Gagal memperbarui profil!", Toast.LENGTH_SHORT).show());
    }


    private void getUserData(String userId) {
        db.collection("users").document(userId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            // Ambil data dari Firestore dan set ke TextView
                            String name = document.getString("namaLengkap");
                            String email = document.getString("email");
                            String role = document.getString("peran");
                            String birthDate = document.getString("TTL");
                            String address = document.getString("alamatUser");
                            String pekerjaan =document.getString("pekerjaan");

                            tvNamaPengguna.setText(name != null ? name : "Nama Tidak Ditemukan");
                            tvEmail.setText(email != null ? email : "Email Tidak Ditemukan");
                            tvRole.setText(role != null ? role : "Peran Tidak Ditemukan");
                            tvTtl.setText(birthDate != null ? birthDate : "TTL Tidak Ditemukan");
                            tvAlamat.setText(address != null ? address : "Alamat Tidak Ditemukan");
                        } else {
                            Toast.makeText(this, "Data pengguna tidak ditemukan!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Gagal mengambil data pengguna!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Kesalahan: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}