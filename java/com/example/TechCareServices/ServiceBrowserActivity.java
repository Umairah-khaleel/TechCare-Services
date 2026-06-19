package com.example.TechCareServices;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ServiceBrowserActivity extends AppCompatActivity {

    private RecyclerView rvServices;
    private ServiceAdapter adapter;
    private List<Service> fullList;
    private List<Service> displayList;
    private FirebaseFirestore db;
    private EditText etSearch;
    private ImageButton btnSearchAction;
    private ImageView btnBack;
    private String userRole = "Customer"; // Default to Customer for safety

    // Category Layouts
    private LinearLayout filterMobile, filterLaptop, filterTV, filterAC, filterFridge, filterWasher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_browser);

        db = FirebaseFirestore.getInstance();

        // Initialize Views
        btnBack = findViewById(R.id.btnBack);
        etSearch = findViewById(R.id.etSearch);
        btnSearchAction = findViewById(R.id.btnSearchAction);
        rvServices = findViewById(R.id.rvServices);

        filterMobile = findViewById(R.id.filterMobile);
        filterLaptop = findViewById(R.id.filterLaptop);
        filterTV = findViewById(R.id.filterTV);
        filterAC = findViewById(R.id.filterAC);
        filterFridge = findViewById(R.id.filterFridge);
        filterWasher = findViewById(R.id.filterWasher);

        fullList = new ArrayList<>();
        displayList = new ArrayList<>();

        // Back Button functionality
        btnBack.setOnClickListener(v -> finish());

        // Step 1: Check if User is Admin, Step 2: Load the Services
        identifyUserRoleAndLoadData();

        // Search Button Action
        btnSearchAction.setOnClickListener(v -> {
            String query = etSearch.getText().toString().trim();
            performSearch(query);
        });

        // Category Filter Actions
        filterMobile.setOnClickListener(v -> filterByCategory("Mobile Services"));
        filterLaptop.setOnClickListener(v -> filterByCategory("Laptop Services"));
        filterTV.setOnClickListener(v -> filterByCategory("TV Services"));
        filterAC.setOnClickListener(v -> filterByCategory("AC Services"));
        filterFridge.setOnClickListener(v -> filterByCategory("Fridge Services"));
        filterWasher.setOnClickListener(v -> filterByCategory("Washer Services"));
    }

    private void identifyUserRoleAndLoadData() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid != null) {
            // Fetch the role from users collection
            db.collection("users").document(uid).get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    userRole = documentSnapshot.getString("role");
                }
                // Once we have the role, setup adapter and load services
                setupRecyclerView();
                loadServices();
            }).addOnFailureListener(e -> {
                // If role check fails, assume Customer and load anyway
                setupRecyclerView();
                loadServices();
            });
        }
    }

    private void setupRecyclerView() {
        // Pass the list, the user role, and a listener for the delete action
        adapter = new ServiceAdapter(displayList, userRole, this::showDeleteConfirmation);
        rvServices.setLayoutManager(new LinearLayoutManager(this));
        rvServices.setAdapter(adapter);
    }

    private void loadServices() {
        db.collection("services").get().addOnSuccessListener(snapshots -> {
            fullList.clear();
            for (DocumentSnapshot doc : snapshots) {
                Service s = doc.toObject(Service.class);
                if (s != null) {
                    s.setServiceId(doc.getId()); // Crucial: Store doc ID so we can delete it later
                    fullList.add(s);
                }
            }
            displayList.clear();
            displayList.addAll(fullList);
            adapter.notifyDataSetChanged();
        });
    }

    private void showDeleteConfirmation(Service service) {
        // Confirmation Dialog
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_service_title)
                .setMessage(getString(R.string.delete_service_confirm, service.getName()))
                .setPositiveButton(R.string.yes, (dialog, which) -> deleteServiceFromFirestore(service.getServiceId()))
                .setNegativeButton(R.string.no, null)
                .show();
    }

    private void deleteServiceFromFirestore(String serviceId) {
        db.collection("services").document(serviceId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, R.string.msg_service_deleted, Toast.LENGTH_SHORT).show();
                    loadServices(); // Refresh the list after deletion
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error deleting: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void performSearch(String query) {
        displayList.clear();
        for (Service s : fullList) {
            if (s.getName().toLowerCase().contains(query.toLowerCase())) {
                displayList.add(s);
            }
        }
        adapter.notifyDataSetChanged();
        if (displayList.isEmpty()) Toast.makeText(this, "No matching services", Toast.LENGTH_SHORT).show();
    }

    private void filterByCategory(String category) {
        displayList.clear();
        for (Service s : fullList) {
            if (s.getCategory().equalsIgnoreCase(category)) {
                displayList.add(s);
            }
        }
        adapter.notifyDataSetChanged();
        Toast.makeText(this, "Showing " + category, Toast.LENGTH_SHORT).show();
    }
}