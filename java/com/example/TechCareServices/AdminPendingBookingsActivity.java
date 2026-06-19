package com.example.TechCareServices;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class AdminPendingBookingsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar loader;
    private EditText etSearch;
    private Button btnSearch;
    private TextView tvNoResults;
    private ImageView btnBack;
    private FirebaseFirestore db;

    private final List<BookingModel> pendingListMaster = new ArrayList<>();
    private final List<BookingModel> pendingListFiltered = new ArrayList<>();
    private BookingAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_pending_bookings);

        recyclerView = findViewById(R.id.rvPendingBookings);
        loader = findViewById(R.id.loader);
        etSearch = findViewById(R.id.etSearch);
        btnSearch = findViewById(R.id.btnSearch);
        tvNoResults = findViewById(R.id.tvNoResults);
        btnBack = findViewById(R.id.btnBack);
        db = FirebaseFirestore.getInstance();

        btnBack.setOnClickListener(v -> finish());

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BookingAdapter(pendingListFiltered);
        recyclerView.setAdapter(adapter);

        btnSearch.setOnClickListener(v -> {
            String query = etSearch.getText().toString().trim();
            performSearch(query);
        });

        fetchPendingBookings();
    }

    private void fetchPendingBookings() {
        loader.setVisibility(View.VISIBLE);

        // Fetch statuses: Pending, Under Repair, Ready for Pickup, and Cancelled
        db.collection("bookings")
                .whereIn("status", Arrays.asList("Pending", "Under Repair", "Ready for Pickup", "Cancelled"))
                .get()
                .addOnCompleteListener(task -> {
                    loader.setVisibility(View.GONE);
                    if (task.isSuccessful() && task.getResult() != null) {
                        pendingListMaster.clear();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            pendingListMaster.add(new BookingModel(doc.getId(), doc.getData()));
                        }
                        performSearch("");
                    } else {
                        Toast.makeText(this, "Failed to load bookings", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void performSearch(String query) {
        pendingListFiltered.clear();

        if (query.isEmpty()) {
            pendingListFiltered.addAll(pendingListMaster);
            tvNoResults.setVisibility(View.GONE);
        } else {
            String lowerQuery = query.toLowerCase();
            for (BookingModel item : pendingListMaster) {
                String name = String.valueOf(item.data.get("name")).toLowerCase();
                String device = String.valueOf(item.data.get("deviceModel")).toLowerCase();

                if (name.contains(lowerQuery) || device.contains(lowerQuery)) {
                    pendingListFiltered.add(item);
                }
            }

            if (pendingListFiltered.isEmpty()) {
                tvNoResults.setVisibility(View.VISIBLE);
                tvNoResults.setText(getString(R.string.error_no_results_query, query));
            } else {
                tvNoResults.setVisibility(View.GONE);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private static class BookingModel {
        String id;
        Map<String, Object> data;
        BookingModel(String id, Map<String, Object> data) {
            this.id = id;
            this.data = data;
        }
    }

    private class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.ViewHolder> {
        private final List<BookingModel> list;

        public BookingAdapter(List<BookingModel> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_common_data_card, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            BookingModel item = list.get(position);
            Map<String, Object> data = item.data;

            // Service Type & Device Model
            holder.tvServiceType.setText(String.valueOf(data.get("serviceType")));
            holder.tvMainTitle.setText(String.valueOf(data.get("deviceModel")));

            // Customer Name
            String customerName = data.get("name") != null ? String.valueOf(data.get("name")) : "Unknown";
            holder.tvSubTitle.setText("Customer: " + customerName);

            // Problem, Service Method & Visit/Pickup Date
            String problemStr = data.get("problem") != null ? String.valueOf(data.get("problem")) : "No description";
            String serviceMethodStr = data.get("serviceMethod") != null ? String.valueOf(data.get("serviceMethod")) : "";
            String visitDateLabel = "Visit Date";
            String visitDate = data.get("visitDate") != null ? String.valueOf(data.get("visitDate")) : "N/A";
            if ("Home Pickup".equalsIgnoreCase(serviceMethodStr)) {
                visitDateLabel = "Pickup Date";
            }

            String timeSlotStr = data.get("timeSlot") != null ? String.valueOf(data.get("timeSlot")) : "N/A";

            holder.tvDetailInfo.setText(Html.fromHtml(
                    "<b>Problem:</b> " + problemStr + "<br>" +
                            "<b>Service Method:</b> " + serviceMethodStr + "<br>" +
                            "<b>" + visitDateLabel + ":</b> " + visitDate + "<br>" +
                            "<b>Time Slot:</b> " + timeSlotStr
            ));

            // Address only if Home Pickup
            if ("Home Pickup".equalsIgnoreCase(serviceMethodStr) && data.containsKey("address") && data.get("address") != null) {
                String address = String.valueOf(data.get("address")).trim();
                if (address.isEmpty() || address.equals("\"\"")) {
                    holder.tvAddress.setVisibility(View.GONE);
                } else {
                    holder.tvAddress.setVisibility(View.VISIBLE);
                    holder.tvAddress.setText("Address: " + address);
                }
            } else {
                holder.tvAddress.setVisibility(View.GONE);
            }

            // Status Badge
            String status = data.get("status") != null ? String.valueOf(data.get("status")) : "Pending";
            holder.tvStatusBadge.setText(status.toUpperCase());

            if ("Cancelled".equalsIgnoreCase(status)) {
                holder.tvStatusBadge.setTextColor(android.graphics.Color.GRAY);
            } else if ("Ready for Pickup".equalsIgnoreCase(status)) {
                holder.tvStatusBadge.setTextColor(android.graphics.Color.parseColor("#1976D2")); // Blue
            } else if ("Under Repair".equalsIgnoreCase(status)) {
                holder.tvStatusBadge.setTextColor(android.graphics.Color.parseColor("#F57C00")); // Orange
            } else {
                holder.tvStatusBadge.setTextColor(android.graphics.Color.RED); // Pending
            }

            // Created At timestamp
            if (data.get("createdAt") != null) {
                Object tsObj = data.get("createdAt");
                String createdAtStr = "";
                try {
                    com.google.firebase.Timestamp ts = (com.google.firebase.Timestamp) tsObj;
                    java.util.Date date = ts.toDate();
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault());
                    createdAtStr = sdf.format(date);
                } catch (Exception e) {
                    createdAtStr = "";
                }
                holder.tvCreatedAt.setVisibility(View.VISIBLE);
                holder.tvCreatedAt.setText(createdAtStr);
            } else {
                holder.tvCreatedAt.setVisibility(View.GONE);
            }

            holder.btnUpdate.setVisibility(View.VISIBLE);
            holder.btnUpdate.setOnClickListener(v -> {
                Intent intent = new Intent(AdminPendingBookingsActivity.this, UpdateStatusActivity.class);
                intent.putExtra("bookingId", item.id);
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvServiceType, tvMainTitle, tvSubTitle, tvDetailInfo, tvStatusBadge, tvAddress, tvCreatedAt;
            Button btnUpdate;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvServiceType = itemView.findViewById(R.id.tvServiceType);
                tvMainTitle = itemView.findViewById(R.id.tvMainTitle);
                tvSubTitle = itemView.findViewById(R.id.tvSubTitle);
                tvDetailInfo = itemView.findViewById(R.id.tvDetailInfo);
                tvStatusBadge = itemView.findViewById(R.id.tvStatusBadge);
                tvAddress = itemView.findViewById(R.id.tvAddress);
                tvCreatedAt = itemView.findViewById(R.id.tvCreatedAt);
                btnUpdate = itemView.findViewById(R.id.btnUpdate);
            }
        }
    }
}
