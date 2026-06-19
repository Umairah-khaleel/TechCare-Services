package com.example.TechCareServices;

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

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AdminAllBookingsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar loader;
    private EditText etSearch;
    private Button btnSearch;
    private TextView tvNoResults;
    private ImageView btnBack;
    private FirebaseFirestore db;

    private final List<Map<String, Object>> bookingListMaster = new ArrayList<>();
    private final List<Map<String, Object>> bookingListFiltered = new ArrayList<>();
    private BookingAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_all_bookings);

        // Initialize Views
        recyclerView = findViewById(R.id.rvAllBookings);
        loader = findViewById(R.id.loader);
        etSearch = findViewById(R.id.etSearch);
        btnSearch = findViewById(R.id.btnSearch);
        tvNoResults = findViewById(R.id.tvNoResults);
        btnBack = findViewById(R.id.btnBack);
        db = FirebaseFirestore.getInstance();

        btnBack.setOnClickListener(v -> finish());

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BookingAdapter(bookingListFiltered);
        recyclerView.setAdapter(adapter);

        btnSearch.setOnClickListener(v -> {
            String query = etSearch.getText().toString().trim();
            performSearch(query);
        });

        fetchAllBookings();
    }

    private void fetchAllBookings() {
        loader.setVisibility(View.VISIBLE);
        db.collection("bookings")
                .get()
                .addOnCompleteListener(task -> {
                    loader.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        bookingListMaster.clear();
                        if (task.getResult() != null) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                bookingListMaster.add(document.getData());
                            }
                            bookingListFiltered.clear();
                            bookingListFiltered.addAll(bookingListMaster);
                            adapter.notifyDataSetChanged();
                        }
                    } else {
                        Toast.makeText(this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void performSearch(String query) {
        bookingListFiltered.clear();

        if (query.isEmpty()) {
            bookingListFiltered.addAll(bookingListMaster);
            tvNoResults.setVisibility(View.GONE);
        } else {
            String lowerQuery = query.toLowerCase();
            for (Map<String, Object> booking : bookingListMaster) {
                String name = String.valueOf(booking.get("name")).toLowerCase();
                String device = String.valueOf(booking.get("deviceModel")).toLowerCase();

                if (name.contains(lowerQuery) || device.contains(lowerQuery)) {
                    bookingListFiltered.add(booking);
                }
            }

            if (bookingListFiltered.isEmpty()) {
                tvNoResults.setVisibility(View.VISIBLE);
                tvNoResults.setText(getString(R.string.error_no_results_query, query));
            } else {
                tvNoResults.setVisibility(View.GONE);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private static class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.ViewHolder> {
        private final List<Map<String, Object>> list;

        public BookingAdapter(List<Map<String, Object>> list) {
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
            Map<String, Object> data = list.get(position);

            holder.tvServiceType.setText(String.valueOf(data.get("serviceType")));
            holder.tvMainTitle.setText(String.valueOf(data.get("deviceModel")));

            String customerName = data.get("name") != null ? String.valueOf(data.get("name")) : "Unknown";
            holder.tvSubTitle.setText("Customer: " + customerName);

            // Problem
            String problemStr = data.get("problem") != null ? String.valueOf(data.get("problem")) : "No description";

            // Service Method
            String serviceMethodStr = data.get("serviceMethod") != null ? String.valueOf(data.get("serviceMethod")) : "";

            // Visit / Pickup Date
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
            if ("Home Pickup".equalsIgnoreCase(serviceMethodStr) && data.get("address") != null) {
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

            // Status
            String status = String.valueOf(data.get("status"));
            holder.tvStatusBadge.setText(status.toUpperCase());
            if ("Pending".equalsIgnoreCase(status)) {
                holder.tvStatusBadge.setTextColor(android.graphics.Color.RED);
            } else {
                holder.tvStatusBadge.setTextColor(android.graphics.Color.parseColor("#4CAF50"));
            }

            // Created At Timestamp
            if (data.get("createdAt") != null) {
                Object tsObj = data.get("createdAt");
                String createdAtStr = "";
                try {
                    Timestamp ts = (Timestamp) tsObj;
                    java.util.Date date = ts.toDate();
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                    createdAtStr = sdf.format(date);
                } catch (Exception e) {
                    createdAtStr = "";
                }
                holder.tvCreatedAt.setVisibility(View.VISIBLE);
                holder.tvCreatedAt.setText(createdAtStr);
            } else {
                holder.tvCreatedAt.setVisibility(View.GONE);
            }

            holder.btnUpdate.setVisibility(View.GONE);
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
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
