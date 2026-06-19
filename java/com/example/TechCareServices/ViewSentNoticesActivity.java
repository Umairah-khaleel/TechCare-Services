package com.example.TechCareServices;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ViewSentNoticesActivity extends AppCompatActivity {

    private RecyclerView rvSentNotices;
    private ProgressBar historyLoader;
    private FirebaseFirestore db;
    private final List<Map<String, Object>> noticesList = new ArrayList<>();
    private NoticeAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_sent_notices);

        rvSentNotices = findViewById(R.id.rvSentNotices);
        historyLoader = findViewById(R.id.historyLoader);
        ImageView btnBack = findViewById(R.id.btnBack);
        db = FirebaseFirestore.getInstance();

        btnBack.setOnClickListener(v -> finish());

        rvSentNotices.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NoticeAdapter(noticesList);
        rvSentNotices.setAdapter(adapter);

        fetchNotices();
    }

    private void fetchNotices() {
        historyLoader.setVisibility(View.VISIBLE);

        // Updated collection name to match your new naming convention
        db.collection("notices_notifications")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    historyLoader.setVisibility(View.GONE);
                    noticesList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        noticesList.add(doc.getData());
                    }
                    adapter.notifyDataSetChanged();

                    if (noticesList.isEmpty()) {
                        Toast.makeText(this, "No history found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    historyLoader.setVisibility(View.GONE);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private static class NoticeAdapter extends RecyclerView.Adapter<NoticeAdapter.ViewHolder> {
        private final List<Map<String, Object>> list;

        NoticeAdapter(List<Map<String, Object>> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_common_data_card, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Map<String, Object> data = list.get(position);

            String title = String.valueOf(data.get("title"));
            String sender = String.valueOf(data.get("senderName"));
            String message = String.valueOf(data.get("message"));
            String target = String.valueOf(data.get("target"));

            holder.tvTitle.setText(title);
            holder.tvSender.setText("From: " + sender);
            holder.tvMessage.setText(message);

            // Logic to label the record as Notice or Notification
            if ("All".equalsIgnoreCase(target)) {
                holder.tvTarget.setText("Recipient: All Customers");
                holder.tvStatusBadge.setText("NOTICE");
                holder.tvStatusBadge.setBackgroundResource(android.R.color.holo_blue_dark);
            } else {
                holder.tvTarget.setText("Recipient: " + target);
                holder.tvStatusBadge.setText("NOTIFICATION");
                holder.tvStatusBadge.setBackgroundResource(android.R.color.holo_green_dark);
            }

            holder.tvStatusBadge.setTextColor(android.graphics.Color.WHITE);

            // Clean up card layout
            holder.tvStatusBadge.setVisibility(View.VISIBLE); // Show the type badge
            holder.tvAddress.setVisibility(View.GONE);
            holder.btnUpdate.setVisibility(View.GONE);
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvSender, tvMessage, tvTarget, tvAddress, tvStatusBadge;
            View btnUpdate;

            ViewHolder(View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tvServiceType);
                tvSender = itemView.findViewById(R.id.tvMainTitle);
                tvMessage = itemView.findViewById(R.id.tvSubTitle);
                tvTarget = itemView.findViewById(R.id.tvDetailInfo);
                tvAddress = itemView.findViewById(R.id.tvAddress);
                tvStatusBadge = itemView.findViewById(R.id.tvStatusBadge);
                btnUpdate = itemView.findViewById(R.id.btnUpdate);
            }
        }
    }
}