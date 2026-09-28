package com.example.lifefit;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etLoginEmail, etLoginPassword;
    private Button btnLoginSubmit;
    private TextView tvGoToRegister;

    private FirebaseAuth mAuth;
    private LoadingDialog loadingDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        loadingDialog = new LoadingDialog(this);

        // Check if user is already logged in
        if (mAuth.getCurrentUser() != null) {
            navigateToMain();
            return;
        }


        // Initialize views
        initViews();

        // Setup listeners
        setupListeners();
    }

    private void initViews() {
        etLoginEmail = findViewById(R.id.etLoginEmail);
        etLoginPassword = findViewById(R.id.etLoginPassword);
        btnLoginSubmit = findViewById(R.id.btnLoginSubmit);
        tvGoToRegister = findViewById(R.id.tvGoToRegister);
    }

    private void setupListeners() {
        // Switch to Register
        tvGoToRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        });

        // Login button click
        btnLoginSubmit.setOnClickListener(v -> loginUser());
    }

    private void loginUser() {
        String email = etLoginEmail.getText() != null ? etLoginEmail.getText().toString().trim() : "";
        String password = etLoginPassword.getText() != null ? etLoginPassword.getText().toString().trim() : "";

        // Validation
        if (email.isEmpty()) {
            etLoginEmail.setError("Please enter email");
            etLoginEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etLoginPassword.setError("Please enter password");
            etLoginPassword.requestFocus();
            return;
        }


        // Show loading
        loadingDialog.show("Logging In", "Please wait while we sign you in...");
        btnLoginSubmit.setEnabled(false);

        // Sign in with Firebase
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {

                    // Hide loading
                    loadingDialog.dismiss();
                    btnLoginSubmit.setEnabled(true);

                    if (task.isSuccessful()) {
                        // Login success
                        Toast.makeText(LoginActivity.this,
                                "Login successful!",
                                Toast.LENGTH_SHORT).show();
                        navigateToMain();
                    } else {
                        // Login failed
                        btnLoginSubmit.setEnabled(true);
                        btnLoginSubmit.setText("Login");
                        String errorMessage = task.getException() != null ?
                                task.getException().getMessage() : "Login failed";
                        Toast.makeText(LoginActivity.this,
                                "Login failed: " + errorMessage,
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void navigateToMain() {
        Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }

    }
}