package com.example.TechCareServices;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddServiceActivity extends AppCompatActivity {

    private EditText etName, etFee, etTime, etDesc, etImageUrl;
    private Spinner spinnerCategory;
    private Button btnAdd, btnViewAll;
    private ImageView btnBack;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_service);

        db = FirebaseFirestore.getInstance();

        // Initialize Views
        btnBack = findViewById(R.id.btnBack);
        etName = findViewById(R.id.etServiceName);
        etFee = findViewById(R.id.etServiceFee);
        etTime = findViewById(R.id.etEstimateTime);
        etImageUrl = findViewById(R.id.etImageUrl);
        etDesc = findViewById(R.id.etDescription);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        btnAdd = findViewById(R.id.btnUploadService);
        btnViewAll = findViewById(R.id.btnViewAllServices);

        // Back Button functionality
        btnBack.setOnClickListener(v -> finish());

        // Setup Categories from strings.xml resource array
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.service_categories, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);

        // Upload service to Firestore
        btnAdd.setOnClickListener(v -> saveServiceToFirestore());

        // Navigate to Service Browser (Where Admin can delete services)
        btnViewAll.setOnClickListener(v -> {
            Intent intent = new Intent(AddServiceActivity.this, ServiceBrowserActivity.class);
            startActivity(intent);
        });
    }

    private void saveServiceToFirestore() {
        String name = etName.getText().toString().trim();
        String fee = etFee.getText().toString().trim();
        String time = etTime.getText().toString().trim();
        String url = etImageUrl.getText().toString().trim();
        String desc = etDesc.getText().toString().trim();
        String category = spinnerCategory.getSelectedItem().toString();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(fee) || TextUtils.isEmpty(url)) {
            Toast.makeText(this, R.string.msg_fill_required, Toast.LENGTH_SHORT).show();
            return;
        }

        btnAdd.setEnabled(false);

        Map<String, Object> service = new HashMap<>();
        service.put("name", name);
        service.put("category", category);
        service.put("fee", "Rs. " + fee);
        service.put("estimateTime", time);
        service.put("description", desc);
        service.put("imageUrl", url);

        db.collection("services")
                .add(service)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, R.string.msg_service_success, Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnAdd.setEnabled(true);
                    Toast.makeText(this, getString(R.string.error_prefix, e.getMessage()), Toast.LENGTH_SHORT).show();
                });
    }
}