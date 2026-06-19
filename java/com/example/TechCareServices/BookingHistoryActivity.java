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
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class BookingHistoryActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private LinearLayout bookingContainer;
    private TextView emptyText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_history);

        ImageView ivBack = findViewById(R.id.ivBack);
        ivBack.setOnClickListener(v -> finish());

        Toast.makeText(this, getString(R.string.booking_history_toast), Toast.LENGTH_SHORT).show();

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        bookingContainer = findViewById(R.id.bookingContainer);
        emptyText = findViewById(R.id.textEmpty);

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            emptyText.setVisibility(View.VISIBLE);
            emptyText.setText(getString(R.string.login_to_view_bookings));
            return;
        }

        loadBookings(user.getUid());
    }

    private void loadBookings(String userId) {
        db.collection("bookings")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(querySnapshots -> {

                    if (querySnapshots.isEmpty()) {
                        emptyText.setVisibility(View.VISIBLE);
                        return;
                    }

                    emptyText.setVisibility(View.GONE);

                    for (QueryDocumentSnapshot doc : querySnapshots) {

                        View card = LayoutInflater.from(this)
                                .inflate(R.layout.item_booking_card, bookingContainer, false);

                        TextView textService = card.findViewById(R.id.textService);
                        TextView textDeviceModel = card.findViewById(R.id.textDeviceModel);
                        TextView textServiceMethod = card.findViewById(R.id.textServiceMethod);
                        TextView textProblem = card.findViewById(R.id.textProblem);
                        TextView textDate = card.findViewById(R.id.textDate);
                        TextView textTime = card.findViewById(R.id.textTime);
                        TextView textStatus = card.findViewById(R.id.textStatus);
                        TextView textAddress = card.findViewById(R.id.textAddress);
                        View buttonDelete = card.findViewById(R.id.buttonDelete);

                        String serviceType = doc.getString("serviceType");
                        String deviceModel = doc.getString("deviceModel");
                        String serviceMethod = doc.getString("serviceMethod");
                        String problem = doc.getString("problem");
                        String visitDate = doc.getString("visitDate");
                        String timeSlot = doc.getString("timeSlot");
                        String status = doc.getString("status");
                        String address = doc.getString("address");

                        textService.setText(serviceType);
                        textDeviceModel.setText(getString(R.string.device_model_placeholder) + ": " + deviceModel);
                        textServiceMethod.setText(getString(R.string.service_method_placeholder) + ": " + serviceMethod);
                        textProblem.setText(getString(R.string.booking_problem, problem));
                        textDate.setText(getString(R.string.booking_date, visitDate));
                        textTime.setText(getString(R.string.booking_time, timeSlot));
                        textStatus.setText(getString(R.string.booking_status, status));

                        if (address != null && !address.isEmpty()) {
                            textAddress.setText(getString(R.string.address_placeholder) + ": " + address);
                            textAddress.setVisibility(View.VISIBLE);
                        } else {
                            textAddress.setVisibility(View.GONE);
                        }

                        if ("Pending".equalsIgnoreCase(status)) {
                            buttonDelete.setVisibility(View.VISIBLE);

                            buttonDelete.setOnClickListener(v ->
                                    db.collection("bookings")
                                            .document(doc.getId())
                                            .delete()
                                            .addOnSuccessListener(unused -> {
                                                Toast.makeText(
                                                        BookingHistoryActivity.this,
                                                        getString(R.string.booking_deleted),
                                                        Toast.LENGTH_SHORT
                                                ).show();
                                                bookingContainer.removeView(card);
                                            })
                                            .addOnFailureListener(e ->
                                                    Toast.makeText(
                                                            BookingHistoryActivity.this,
                                                            e.getMessage(),
                                                            Toast.LENGTH_LONG
                                                    ).show())
                            );
                        }

                        bookingContainer.addView(card);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(
                                BookingHistoryActivity.this,
                                e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show());
    }
}
