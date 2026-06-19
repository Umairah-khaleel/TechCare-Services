package com.example.TechCareServices;

import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class EditProfileActivity extends AppCompatActivity {

    private EditText etName, etCurrentEmail, etNewEmail,
            etOldPassword, etNewPassword, etConfirmPassword;
    private TextView tvShowOld, tvShowNew, tvShowConfirm;
    private Button btnSave;
    private ImageView btnBack;

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FirebaseUser user;

    // Password visibility booleans
    private boolean isOldVisible = false, isNewVisible = false, isConfirmVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        user = auth.getCurrentUser();

        btnBack = findViewById(R.id.btnBack);
        etName = findViewById(R.id.etName);
        etCurrentEmail = findViewById(R.id.etCurrentEmail);
        etNewEmail = findViewById(R.id.etNewEmail);
        etOldPassword = findViewById(R.id.etOldPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnSave = findViewById(R.id.btnSave);

        // Visibility Toggles
        tvShowOld = findViewById(R.id.tvShowOld);
        tvShowNew = findViewById(R.id.tvShowNew);
        tvShowConfirm = findViewById(R.id.tvShowConfirm);

        btnBack.setOnClickListener(v -> finish());

        // Set Click Listeners for Show/Hide
        tvShowOld.setOnClickListener(v -> isOldVisible = togglePassword(etOldPassword, tvShowOld, isOldVisible));
        tvShowNew.setOnClickListener(v -> isNewVisible = togglePassword(etNewPassword, tvShowNew, isNewVisible));
        tvShowConfirm.setOnClickListener(v -> isConfirmVisible = togglePassword(etConfirmPassword, tvShowConfirm, isConfirmVisible));

        loadUserData();

        btnSave.setOnClickListener(v -> startUpdateProcess());
    }

    private boolean togglePassword(EditText editText, TextView textView, boolean isVisible) {
        if (isVisible) {
            editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
            textView.setText(getString(R.string.show_password));
        } else {
            editText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            textView.setText("Hide");
        }
        editText.setSelection(editText.getText().length());
        return !isVisible;
    }

    private void loadUserData() {
        if (user == null) return;
        etCurrentEmail.setText(user.getEmail());

        db.collection("users").document(user.getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        etName.setText(doc.getString("name"));
                    }
                });
    }

    private void startUpdateProcess() {
        if (user == null) return;

        btnSave.setEnabled(false);

        user.reload().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                validateAndAuthenticate();
            } else {
                btnSave.setEnabled(true);
                Toast.makeText(this, "Session expired. Please log out and back in.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void validateAndAuthenticate() {
        String name = etName.getText().toString().trim();
        String newEmail = etNewEmail.getText().toString().trim();
        String oldPass = etOldPassword.getText().toString();
        String newPass = etNewPassword.getText().toString();
        String confirmPass = etConfirmPassword.getText().toString();

        if (TextUtils.isEmpty(name)) {
            etName.setError("Name is required");
            btnSave.setEnabled(true);
            return;
        }

        if (!TextUtils.isEmpty(newEmail) && !Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
            etNewEmail.setError("Invalid email format");
            btnSave.setEnabled(true);
            return;
        }

        if (!TextUtils.isEmpty(newPass) && !newPass.equals(confirmPass)) {
            etConfirmPassword.setError("Passwords do not match");
            btnSave.setEnabled(true);
            return;
        }

        if (!TextUtils.isEmpty(newEmail) || !TextUtils.isEmpty(newPass)) {
            if (TextUtils.isEmpty(oldPass)) {
                etOldPassword.setError("Current password required to change email/password");
                btnSave.setEnabled(true);
                return;
            }

            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), oldPass);
            user.reauthenticate(credential)
                    .addOnSuccessListener(unused -> startChainedUpdate(name, newEmail, newPass))
                    .addOnFailureListener(e -> {
                        btnSave.setEnabled(true);
                        Toast.makeText(this, "Authentication Failed: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                    });
        } else {
            startChainedUpdate(name, null, null);
        }
    }

    private void startChainedUpdate(String name, String newEmail, String newPass) {
        db.collection("users").document(user.getUid())
                .update("name", name)
                .addOnSuccessListener(unused -> {
                    if (!TextUtils.isEmpty(newEmail)) {
                        updateEmailInAuth(newEmail, newPass);
                    } else {
                        handlePasswordChange(newPass);
                    }
                })
                .addOnFailureListener(e -> {
                    btnSave.setEnabled(true);
                    Toast.makeText(this, "Firestore Name Update Failed", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateEmailInAuth(String newEmail, String newPass) {
        user.updateEmail(newEmail)
                .addOnSuccessListener(unused -> {
                    db.collection("users").document(user.getUid())
                            .update("email", newEmail)
                            .addOnSuccessListener(v -> handlePasswordChange(newPass))
                            .addOnFailureListener(e -> handlePasswordChange(newPass));
                })
                .addOnFailureListener(e -> {
                    btnSave.setEnabled(true);
                    Toast.makeText(this, "Email Update Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void handlePasswordChange(String newPass) {
        if (!TextUtils.isEmpty(newPass)) {
            user.updatePassword(newPass)
                    .addOnSuccessListener(unused -> finalizeUpdate())
                    .addOnFailureListener(e -> {
                        btnSave.setEnabled(true);
                        Toast.makeText(this, "Password Update Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            finalizeUpdate();
        }
    }

    private void finalizeUpdate() {
        Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_LONG).show();
        finish();
    }
}