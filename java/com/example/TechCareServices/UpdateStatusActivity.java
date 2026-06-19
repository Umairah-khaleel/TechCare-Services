package com.example.TechCareServices;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

public class UpdateStatusActivity extends AppCompatActivity {

    private EditText etName, etModel, etProblem;
    private Spinner spinnerStatus;
    private Button btnSave;
    private ImageView btnBack;
    private ProgressBar loader;

    private FirebaseFirestore db;
    private String bookingId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_status);

        // Initialize Views
        etName = findViewById(R.id.etUpdateName);
        etModel = findViewById(R.id.etUpdateModel);
        etProblem = findViewById(R.id.etUpdateProblem);
        spinnerStatus = findViewById(R.id.spinnerStatus);
        btnSave = findViewById(R.id.btnSaveChanges);
        btnBack = findViewById(R.id.btnBack);
        loader = findViewById(R.id.updateLoader);

        db = FirebaseFirestore.getInstance();

        // Get the ID passed from the previous activity
        bookingId = getIntent().getStringExtra("bookingId");

        // Back Button Logic
        btnBack.setOnClickListener(v -> finish());

        if (bookingId != null) {
            loadBookingDetails();
        } else {
            Toast.makeText(this, "Error: Booking ID not found", Toast.LENGTH_SHORT).show();
            finish();
        }

        btnSave.setOnClickListener(v -> updateStatus());
    }

    private void loadBookingDetails() {
        loader.setVisibility(View.VISIBLE);
        db.collection("bookings").document(bookingId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    loader.setVisibility(View.GONE);
                    if (documentSnapshot.exists()) {
                        // FIXED: Using "name" instead of "fullName"
                        etName.setText(documentSnapshot.getString("name"));

                        // Already correct: Using "deviceModel"
                        etModel.setText(documentSnapshot.getString("deviceModel"));

                        // FIXED: Using "problem" instead of "problemDescription"
                        etProblem.setText(documentSnapshot.getString("problem"));

                        // Set spinner to current status automatically
                        String currentStatus = documentSnapshot.getString("status");
                        if (currentStatus != null) {
                            @SuppressWarnings("unchecked")
                            ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinnerStatus.getAdapter();
                            if (adapter != null) {
                                int position = adapter.getPosition(currentStatus);
                                if (position >= 0) {
                                    spinnerStatus.setSelection(position);
                                }
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    loader.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed to load details", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateStatus() {
        String newStatus = spinnerStatus.getSelectedItem().toString();
        loader.setVisibility(View.VISIBLE);

        // Update the status in Firestore
        db.collection("bookings").document(bookingId)
                .update("status", newStatus)
                .addOnSuccessListener(aVoid -> {
                    loader.setVisibility(View.GONE);
                    Toast.makeText(this, "Status updated to " + newStatus, Toast.LENGTH_SHORT).show();
                    finish(); // Return to the list
                })
                .addOnFailureListener(e -> {
                    loader.setVisibility(View.GONE);
                    Toast.makeText(this, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}