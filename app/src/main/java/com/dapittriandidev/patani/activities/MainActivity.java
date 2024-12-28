package com.dapittriandidev.patani.activities;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.dapittriandidev.patani.R;
import com.dapittriandidev.patani.fragments.DashboardPembeli;
import com.dapittriandidev.patani.fragments.DashboardPetani;
import com.dapittriandidev.patani.utils.Constants;
import com.dapittriandidev.patani.utils.SharedPrefManager;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private EditText emailLogin, passwordLogin;
    private Button btnLogin;
    private TextView btnContinuRegister;
    private ProgressBar progressBarLogin;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private SharedPrefManager sharedPrefManager;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseApp.initializeApp(this);
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(Color.TRANSPARENT);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }


        sharedPrefManager = SharedPrefManager.getInstance(this);

        // Cek jika pengguna sudah login
        if (sharedPrefManager.isLoggedIn()) {
            String role = sharedPrefManager.getRole();
            navigateToDashboard(role);
            return;
        }

        // Inisialisasi komponen UI
        emailLogin = findViewById(R.id.emailLogin);
        passwordLogin = findViewById(R.id.passwordLogin);
        progressBarLogin = findViewById(R.id.progressBarLogin);
        btnLogin = findViewById(R.id.btnLogin);
        btnContinuRegister = findViewById(R.id.btnContinuRegister);

        btnLogin.setOnClickListener(v -> loginUser());
        btnContinuRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
        userTokenAndUserId();
    }

    private void userTokenAndUserId() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            String userIdNew = currentUser.getUid();

            firestore.collection("users").document(userIdNew).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Map<String, Object> updates = new HashMap<>();
                            boolean needsUpdate = false;

                            if (!documentSnapshot.contains("userId")) {
                                updates.put("userId", userIdNew);
                                needsUpdate = true;
                            }
                            if (!documentSnapshot.contains("fcmToken")) {
                                FirebaseMessaging.getInstance().getToken()
                                        .addOnSuccessListener(token -> {
                                            updates.put("fcmToken", token);
                                            firestore.collection("users").document(userIdNew).update(updates)
                                                    .addOnSuccessListener(aVoid -> Log.d("Firestore", "User ID and Token updated"))
                                                    .addOnFailureListener(e -> Log.e("Firestore", "Failed to update data", e));
                                        })
                                        .addOnFailureListener(e -> Log.e("FCM", "Failed to retrieve token", e));
                            }

                            if (needsUpdate && !updates.isEmpty()) {
                                firestore.collection("users").document(userIdNew).update(updates)
                                        .addOnSuccessListener(aVoid -> Log.d("Firestore", "User data updated"))
                                        .addOnFailureListener(e -> Log.e("Firestore", "Failed to update user data", e));
                            }
                        }
                    })
                    .addOnFailureListener(e -> Log.e("Firestore", "Failed to retrieve user data", e));
        }
    }

    private void loginUser() {
        String email = emailLogin.getText().toString().trim();
        String password = passwordLogin.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            emailLogin.setError("Email wajib diisi");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            passwordLogin.setError("Password wajib diisi");
            return;
        }

        progressBarLogin.setVisibility(View.VISIBLE);

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    progressBarLogin.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            String userId = user.getUid();
                            firestore.collection("users").document(userId).get()
                                    .addOnSuccessListener(documentSnapshot -> {
                                        if (documentSnapshot.exists()) {
                                            String role = documentSnapshot.getString("peran");
                                            sharedPrefManager.saveSession(userId, role);
                                            navigateToDashboard(role);
                                        } else {
                                            Toast.makeText(this, "Data pengguna tidak ditemukan!", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(this, "Gagal mengambil data pengguna: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } else {
                        Toast.makeText(this, "Login gagal: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void navigateToDashboard(String role) {
        Intent intent = new Intent(this, DashboardActivity.class);
        intent.putExtra("peran", role);
        startActivity(intent);
        finish();
    }
}