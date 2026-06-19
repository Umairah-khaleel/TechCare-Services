package com.example.TechCareServices;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddAdminActivity extends AppCompatActivity {

    private EditText etName, etEmail;
    private Button btnCreate;
    private ProgressBar loader;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private final String DEFAULT_PASSWORD = "admin123";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_admin);

        etName = findViewById(R.id.etAdminName);
        etEmail = findViewById(R.id.etAdminEmail);
        btnCreate = findViewById(R.id.btnCreateAdmin);
        loader = findViewById(R.id.adminLoader);
        ImageView btnBack = findViewById(R.id.btnBack);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        btnBack.setOnClickListener(v -> finish());
        btnCreate.setOnClickListener(v -> validateAndRegisterAdmin());
    }

    private void validateAndRegisterAdmin() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            etName.setError(getString(R.string.full_name) + " is required");
            etName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            etEmail.setError(getString(R.string.email) + " is required");
            etEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError(getString(R.string.error_invalid_email));
            etEmail.requestFocus();
            return;
        }

        createAdminInFirebase(name, email);
    }

    private void createAdminInFirebase(String name, String email) {
        loader.setVisibility(View.VISIBLE);
        btnCreate.setEnabled(false);

        // --- FIX FOR AUTO-LOGOUT ---
        FirebaseOptions options = FirebaseApp.getInstance().getOptions();

        // Use a temporary variable to initialize the final variable
        FirebaseApp tempApp;
        try {
            tempApp = FirebaseApp.initializeApp(getApplicationContext(), options, "SecondaryAdminApp");
        } catch (Exception e) {
            tempApp = FirebaseApp.getInstance("SecondaryAdminApp");
        }

        // Declare this as final so it can be used inside the lambdas
        final FirebaseApp finalSecondaryApp = tempApp;
        FirebaseAuth secondaryAuth = FirebaseAuth.getInstance(finalSecondaryApp);

        secondaryAuth.createUserWithEmailAndPassword(email, DEFAULT_PASSWORD)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().getUser() != null) {
                        String newAdminUid = task.getResult().getUser().getUid();
                        // Pass the final app instance to the next method
                        saveAdminToFirestore(newAdminUid, name, email, finalSecondaryApp);
                    } else {
                        loader.setVisibility(View.GONE);
                        btnCreate.setEnabled(true);

                        if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                            etEmail.setError(getString(R.string.error_email_taken));
                            etEmail.requestFocus();
                        } else {
                            String error = task.getException() != null ? task.getException().getMessage() : "Failed to create admin";
                            Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                        }

                        // Delete secondary app instance
                        finalSecondaryApp.delete();
                    }
                });
    }

    private void saveAdminToFirestore(String uid, String name, String email, final FirebaseApp secondaryAppInstance) {
        Map<String, Object> adminData = new HashMap<>();
        adminData.put("name", name);
        adminData.put("email", email);
        adminData.put("role", "Admin");

        db.collection("users").document(uid)
                .set(adminData)
                .addOnSuccessListener(unused -> {
                    loader.setVisibility(View.GONE);
                    Toast.makeText(this, getString(R.string.msg_admin_created), Toast.LENGTH_LONG).show();

                    // Cleanup the secondary app instance
                    secondaryAppInstance.delete();
                    finish();
                })
                .addOnFailureListener(e -> {
                    loader.setVisibility(View.GONE);
                    btnCreate.setEnabled(true);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                    // Cleanup on failure
                    secondaryAppInstance.delete();
                });
    }
}