package com.example.TechCareServices;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminSendNoticesActivity extends AppCompatActivity {

    private EditText etTitle, etMessage;
    private Spinner spinnerTarget, spinnerUsers;
    private LinearLayout layoutIndividual, layoutMessage;
    private Button btnPost, btnHistory;
    private ProgressBar loader;
    private ImageView btnBack;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private final List<String> userDisplayList = new ArrayList<>();
    private final List<String> userEmails = new ArrayList<>();
    private String adminName = "Admin";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_send_notices);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Initialize Views
        etTitle = findViewById(R.id.etNoticeTitle);
        etMessage = findViewById(R.id.etNoticeMessage);
        spinnerTarget = findViewById(R.id.spinnerTargetType);
        spinnerUsers = findViewById(R.id.spinnerUserList);
        layoutIndividual = findViewById(R.id.layoutIndividualUser);
        layoutMessage = findViewById(R.id.layoutMessageFields);
        btnPost = findViewById(R.id.btnPostNotice);
        btnHistory = findViewById(R.id.btnViewHistory);
        loader = findViewById(R.id.noticeLoader);
        btnBack = findViewById(R.id.btnBack);

        fetchAdminName();
        fetchCustomers();

        btnBack.setOnClickListener(v -> finish());

        spinnerTarget.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) { // Choose Target
                    layoutIndividual.setVisibility(View.GONE);
                    layoutMessage.setVisibility(View.GONE);
                } else if (position == 1) { // All Customers (Notice)
                    layoutIndividual.setVisibility(View.GONE);
                    layoutMessage.setVisibility(View.VISIBLE);
                } else if (position == 2) { // Individual User (Notification)
                    layoutIndividual.setVisibility(View.VISIBLE);
                    layoutMessage.setVisibility(View.VISIBLE);
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        btnPost.setOnClickListener(v -> validateAndSend());

        btnHistory.setOnClickListener(v -> {
            startActivity(new Intent(this, ViewSentNoticesActivity.class));
        });
    }

    private void fetchAdminName() {
        if (auth.getCurrentUser() != null) {
            String uid = auth.getCurrentUser().getUid();
            db.collection("users").document(uid).get().addOnSuccessListener(doc -> {
                if (doc.exists() && doc.getString("name") != null) {
                    adminName = doc.getString("name");
                }
            });
        }
    }

    private void fetchCustomers() {
        db.collection("users").whereEqualTo("role", "Customer").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        userDisplayList.clear();
                        userEmails.clear();
                        userDisplayList.add("-- Select Customer --");
                        userEmails.add("none");

                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            String name = doc.getString("name");
                            String email = doc.getString("email");
                            userDisplayList.add(name + " (" + email + ")");
                            userEmails.add(email);
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                                android.R.layout.simple_spinner_item, userDisplayList);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerUsers.setAdapter(adapter);
                    }
                });
    }

    private void validateAndSend() {
        String title = etTitle.getText().toString().trim();
        String msg = etMessage.getText().toString().trim();
        int targetPos = spinnerTarget.getSelectedItemPosition();

        if (title.isEmpty() || msg.isEmpty()) {
            Toast.makeText(this, "Please fill title and message", Toast.LENGTH_SHORT).show();
            return;
        }

        String targetEmail = "All";

        if (targetPos == 2) {
            int userPos = spinnerUsers.getSelectedItemPosition();
            if (userPos <= 0) {
                Toast.makeText(this, "Please select a customer", Toast.LENGTH_SHORT).show();
                return;
            }
            targetEmail = userEmails.get(userPos);
        }

        sendNoticeToFirestore(title, msg, targetEmail);
    }

    private void sendNoticeToFirestore(String title, String msg, String targetEmail) {
        loader.setVisibility(View.VISIBLE);
        btnPost.setEnabled(false);

        Map<String, Object> notice = new HashMap<>();
        notice.put("title", title);
        notice.put("message", msg);
        notice.put("senderName", adminName);
        notice.put("target", targetEmail);
        notice.put("timestamp", FieldValue.serverTimestamp());

        // Using the updated collection name
        db.collection("notices_notifications").add(notice)
                .addOnSuccessListener(doc -> {
                    loader.setVisibility(View.GONE);

                    // Specific logic for success messages
                    if (targetEmail.equals("All")) {
                        Toast.makeText(this, getString(R.string.msg_notice_success), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, getString(R.string.msg_notification_user_success), Toast.LENGTH_SHORT).show();
                    }

                    finish();
                })
                .addOnFailureListener(e -> {
                    loader.setVisibility(View.GONE);
                    btnPost.setEnabled(true);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}