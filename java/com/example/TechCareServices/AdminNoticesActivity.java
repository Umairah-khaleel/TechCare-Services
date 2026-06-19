package com.example.TechCareServices;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Arrays;

public class AdminNoticesActivity extends AppCompatActivity {

    private LinearLayout noticesContainer;
    private TextView textEmpty;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Tip: You might want to rename this layout to activity_notices_notifications later
        setContentView(R.layout.activity_admin_notices);

        // Initialize Back Button
        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // Initialize Other Views
        noticesContainer = findViewById(R.id.noticesContainer);
        textEmpty = findViewById(R.id.textEmpty);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseUser user = auth.getCurrentUser();
        if (user == null || user.getEmail() == null) {
            textEmpty.setText("Please login to view notices.");
            textEmpty.setVisibility(View.VISIBLE);
            return;
        }

        loadNotices(user.getEmail());
    }

    private void loadNotices(String userEmail) {
        // Fetching from the new collection name
        // Logic: Get notices where target is "All" OR matches the specific user email
        db.collection("notices_notifications")
                .whereIn("target", Arrays.asList("All", userEmail))
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshots -> {
                    if (querySnapshots.isEmpty()) {
                        textEmpty.setText("No Notices or Notifications Yet");
                        textEmpty.setVisibility(View.VISIBLE);
                        return;
                    }

                    textEmpty.setVisibility(View.GONE);
                    noticesContainer.removeAllViews();

                    for (QueryDocumentSnapshot doc : querySnapshots) {
                        View card = LayoutInflater.from(this)
                                .inflate(R.layout.item_admin_notice_card, noticesContainer, false);

                        TextView textTitle = card.findViewById(R.id.textTitle);
                        TextView textMessage = card.findViewById(R.id.textMessage);
                        TextView textDate = card.findViewById(R.id.textDate);

                        textTitle.setText(doc.getString("title"));
                        textMessage.setText(doc.getString("message"));

                        // Using the timestamp field from Firestore
                        if (doc.getTimestamp("timestamp") != null) {
                            textDate.setText(doc.getTimestamp("timestamp").toDate().toString());
                        }

                        noticesContainer.addView(card);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(AdminNoticesActivity.this,
                                "Error loading updates: " + e.getMessage(),
                                Toast.LENGTH_LONG).show());
    }
}