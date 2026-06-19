package com.example.TechCareServices;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvSignUp, tvShowHide;
    private ProgressBar progressBar;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.editTextEmail);
        etPassword = findViewById(R.id.editTextPassword);
        btnLogin = findViewById(R.id.buttonLogin);
        tvSignUp = findViewById(R.id.textViewSignUp);
        tvShowHide = findViewById(R.id.textViewShowHide);
        progressBar = findViewById(R.id.progressBar);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Already logged in
        FirebaseUser user = auth.getCurrentUser();
        if(user != null){
            checkUserRole(user.getUid());
        }

        // Show/Hide password
        tvShowHide.setOnClickListener(v -> {
            if(isPasswordVisible){
                etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                tvShowHide.setText(getString(R.string.show_password));
            } else {
                etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                tvShowHide.setText("Hide");
            }
            isPasswordVisible = !isPasswordVisible;
            etPassword.setSelection(etPassword.getText().length());
        });

        btnLogin.setOnClickListener(v -> loginUser());

        tvSignUp.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
        });
    }

    private void loginUser(){
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if(TextUtils.isEmpty(email)){
            etEmail.setError("Email required");
            return;
        }
        if(TextUtils.isEmpty(password)){
            etPassword.setError("Password required");
            return;
        }
        if(password.length() < 6){
            etPassword.setError("Invalid password");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if(task.isSuccessful()){
                        FirebaseUser user = auth.getCurrentUser();
                        if(user != null){
                            checkUserRole(user.getUid());
                        }
                    } else {
                        Exception e = task.getException();
                        if(e instanceof FirebaseAuthInvalidUserException){
                            etEmail.setError("Invalid email");
                        } else if(e instanceof FirebaseAuthInvalidCredentialsException){
                            etPassword.setError("Incorrect password");
                        } else {
                            Toast.makeText(LoginActivity.this, "Error: "+e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void checkUserRole(String uid) {
        progressBar.setVisibility(View.VISIBLE);
        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(document -> {
                    progressBar.setVisibility(View.GONE);
                    if (document.exists()) {
                        String role = document.getString("role");

                        if (role != null && role.equalsIgnoreCase("Admin")) {
                            Toast.makeText(this, "Admin Login successful!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(LoginActivity.this, AdminHomeActivity.class));
                            finish();
                        } else if (role != null && role.equalsIgnoreCase("Customer")) {
                            Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                        } else {
                            // If role is missing or spelled differently, default to Customer
                            Toast.makeText(this, "Welcome!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                        }
                    } else {
                        Toast.makeText(this, "User record not found. Accessing Home...", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Database error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}