package com.example.TechCareServices;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AdminUsersActivity extends AppCompatActivity {

    private RecyclerView rvUsersList;
    private ProgressBar userLoader;
    private EditText etSearchUser;
    private Button btnSearchUser;
    private TextView tvNoResults;
    private FirebaseFirestore db;
    private String currentAdminId;

    private final List<Map<String, Object>> userListMaster = new ArrayList<>();
    private final List<Map<String, Object>> userListFiltered = new ArrayList<>();
    private UserAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_users);

        db = FirebaseFirestore.getInstance();
        currentAdminId = FirebaseAuth.getInstance().getUid();

        rvUsersList = findViewById(R.id.rvUsersList);
        userLoader = findViewById(R.id.userLoader);
        etSearchUser = findViewById(R.id.etSearchUser);
        btnSearchUser = findViewById(R.id.btnSearchUser);
        tvNoResults = findViewById(R.id.tvNoResults);
        ImageView btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        rvUsersList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UserAdapter(userListFiltered, currentAdminId, this::showDeleteConfirmation);
        rvUsersList.setAdapter(adapter);

        btnSearchUser.setOnClickListener(v -> performSearch(etSearchUser.getText().toString().trim()));

        fetchAllUsersFromFirestore();
    }

    private void fetchAllUsersFromFirestore() {
        userLoader.setVisibility(View.VISIBLE);
        db.collection("users")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    userLoader.setVisibility(View.GONE);
                    userListMaster.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Map<String, Object> data = doc.getData();
                        data.put("userId", doc.getId());
                        userListMaster.add(data);
                    }
                    userListFiltered.clear();
                    userListFiltered.addAll(userListMaster);
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    userLoader.setVisibility(View.GONE);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showDeleteConfirmation(Map<String, Object> userData) {
        String userId = (String) userData.get("userId");
        String userName = (String) userData.get("name");

        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_user_title)
                .setMessage(getString(R.string.delete_user_confirm, userName))
                .setPositiveButton(R.string.yes, (dialog, which) -> deleteUserCompletely(userId))
                .setNegativeButton(R.string.no, null)
                .show();
    }

    private void deleteUserCompletely(String userId) {
        userLoader.setVisibility(View.VISIBLE);
        WriteBatch batch = db.batch();

        // Delete user doc
        batch.delete(db.collection("users").document(userId));

        // Delete all bookings for this user
        db.collection("bookings")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        batch.delete(doc.getReference());
                    }

                    batch.commit().addOnSuccessListener(aVoid -> {
                        userLoader.setVisibility(View.GONE);
                        Toast.makeText(this, R.string.msg_delete_success, Toast.LENGTH_SHORT).show();
                        fetchAllUsersFromFirestore();
                    }).addOnFailureListener(e -> {
                        userLoader.setVisibility(View.GONE);
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                });
    }

    private void performSearch(String query) {
        userListFiltered.clear();
        if (query.isEmpty()) {
            userListFiltered.addAll(userListMaster);
            tvNoResults.setVisibility(View.GONE);
        } else {
            String lowerQuery = query.toLowerCase();
            for (Map<String, Object> user : userListMaster) {
                String name = String.valueOf(user.get("name")).toLowerCase();
                if (name.contains(lowerQuery)) userListFiltered.add(user);
            }
            tvNoResults.setVisibility(userListFiltered.isEmpty() ? View.VISIBLE : View.GONE);
        }
        adapter.notifyDataSetChanged();
    }

    // --- ADAPTER CLASS ---
    private static class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
        private final List<Map<String, Object>> list;
        private final String currentAdminId;
        private final OnDeleteClickListener deleteListener;

        interface OnDeleteClickListener {
            void onDeleteClick(Map<String, Object> userData);
        }

        UserAdapter(List<Map<String, Object>> list, String adminId, OnDeleteClickListener listener) {
            this.list = list;
            this.currentAdminId = adminId;
            this.deleteListener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_common_data_card, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Map<String, Object> userData = list.get(position);
            String userId = (String) userData.get("userId");
            String name = String.valueOf(userData.get("name"));
            String email = String.valueOf(userData.get("email"));
            String role = String.valueOf(userData.get("role"));

            holder.tvMainTitle.setText(name);
            holder.tvSubTitle.setText("Email: " + email);
            holder.tvServiceType.setText(role.toUpperCase());

            // --- USER LIST SPECIFIC UI LOGIC ---
            // Hide everything except the title, subtitle, role, and the new Delete Button
            holder.tvDetailInfo.setVisibility(View.GONE);
            holder.tvAddress.setVisibility(View.GONE);
            holder.tvStatusBadge.setVisibility(View.GONE);

            // Make the button visible ONLY here
            holder.btnUpdate.setVisibility(View.VISIBLE);
            holder.btnUpdate.setText("Delete User");
            holder.btnUpdate.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFD32F2F)); // Red

            // Prevent Admin from deleting themselves
            if (userId != null && userId.equals(currentAdminId)) {
                holder.btnUpdate.setEnabled(false);
                holder.btnUpdate.setAlpha(0.5f);
                holder.btnUpdate.setText("System (Self)");
            } else {
                holder.btnUpdate.setEnabled(true);
                holder.btnUpdate.setAlpha(1.0f);
                holder.btnUpdate.setOnClickListener(v -> deleteListener.onDeleteClick(userData));
            }
        }

        @Override
        public int getItemCount() { return list.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvServiceType, tvMainTitle, tvSubTitle, tvDetailInfo, tvAddress, tvStatusBadge;
            Button btnUpdate;
            ViewHolder(View itemView) {
                super(itemView);
                tvServiceType = itemView.findViewById(R.id.tvServiceType);
                tvMainTitle = itemView.findViewById(R.id.tvMainTitle);
                tvSubTitle = itemView.findViewById(R.id.tvSubTitle);
                tvDetailInfo = itemView.findViewById(R.id.tvDetailInfo);
                tvAddress = itemView.findViewById(R.id.tvAddress);
                tvStatusBadge = itemView.findViewById(R.id.tvStatusBadge);
                btnUpdate = itemView.findViewById(R.id.btnUpdate);
            }
        }
    }
}