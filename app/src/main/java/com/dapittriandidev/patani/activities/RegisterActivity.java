package com.dapittriandidev.patani.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.dapittriandidev.patani.R;
import com.dapittriandidev.patani.activities.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    private EditText namaLengkapRegister, emailRegister, passwordRegister, alamat, tanggalLahir, pekerjaan;
    private Spinner spinPeran;
    private TextView btnContinuLogin;
    private Button btnRegister;
    private ProgressBar progressBarLogin;

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(Color.TRANSPARENT);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        // Inisialisasi Firebase
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Hubungkan komponen dengan XML
        namaLengkapRegister = findViewById(R.id.namaLengkapRegister);
        emailRegister = findViewById(R.id.emailRegister);
        passwordRegister = findViewById(R.id.passwordRegister);
        alamat = findViewById(R.id.edtAlamat);
        tanggalLahir = findViewById(R.id.edtDate);
        pekerjaan = findViewById(R.id.edtPekerjaan);
        spinPeran = findViewById(R.id.spinPeran);
        btnRegister = findViewById(R.id.btnRegister);
        btnContinuLogin = findViewById(R.id.btnContinuLogin);
        progressBarLogin = findViewById(R.id.progressBarLogin);

        // Atur Spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.peran_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinPeran.setAdapter(adapter);

        // Aksi untuk memilih tanggal lahir
        tanggalLahir.setOnClickListener(v -> showDatePicker());

        // Aksi Register
        btnRegister.setOnClickListener(v -> registerUser());
        btnContinuLogin.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, MainActivity.class));
            finish();
        });
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, yearSelected, monthSelected, dayOfMonth) -> {
                    String date = dayOfMonth + "/" + (monthSelected + 1) + "/" + yearSelected;
                    tanggalLahir.setText(date);
                },
                year, month, day
        );

        // Batasan waktu (Opsional)
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void registerUser() {
        String namaLengkap = namaLengkapRegister.getText().toString().trim();
        String email = emailRegister.getText().toString().trim();
        String password = passwordRegister.getText().toString().trim();
        String alamatTinggal = alamat.getText().toString().trim();
        String pekerjaanUser = pekerjaan.getText().toString().trim();
        String ttl = tanggalLahir.getText().toString().trim();
        String peran = spinPeran.getSelectedItem().toString();

        if (TextUtils.isEmpty(namaLengkap)) {
            namaLengkapRegister.setError("Nama Lengkap wajib diisi");
            return;
        }
        if (TextUtils.isEmpty(email)) {
            emailRegister.setError("Email wajib diisi");
            return;
        }
        if (TextUtils.isEmpty(password) || password.length() < 6) {
            passwordRegister.setError("Password minimal 6 karakter");
            return;
        }
        if (TextUtils.isEmpty(alamatTinggal)) {
            alamat.setError("Alamat wajib diisi");
            return;
        }
        if (TextUtils.isEmpty(pekerjaanUser)) {
            pekerjaan.setError("Pekerjaan wajib diisi");
            return;
        }
        if (TextUtils.isEmpty(ttl)) {
            tanggalLahir.setError("Tanggal lahir wajib diisi");
            return;
        }

        progressBarLogin.setVisibility(View.VISIBLE);

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            String userId = user.getUid();
                            Map<String, Object> userData = new HashMap<>();
                            userData.put("userId", userId);
                            userData.put("namaLengkap", namaLengkap);
                            userData.put("email", email);
                            userData.put("peran", peran);
                            userData.put("alamatUser", alamatTinggal);
                            userData.put("pekerjaan", pekerjaanUser);
                            userData.put("TTL", ttl);

                            firestore.collection("users").document(userId)
                                    .set(userData)
                                    .addOnSuccessListener(aVoid -> {
                                        progressBarLogin.setVisibility(View.GONE);
                                        Toast.makeText(RegisterActivity.this, "Register Berhasil", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        progressBarLogin.setVisibility(View.GONE);
                                        Toast.makeText(RegisterActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } else {
                        progressBarLogin.setVisibility(View.GONE);
                        Toast.makeText(RegisterActivity.this, "Register Gagal: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}
