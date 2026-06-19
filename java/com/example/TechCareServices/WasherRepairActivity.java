package com.example.TechCareServices;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.AdapterView;
import android.widget.PopupMenu;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class WasherRepairActivity extends AppCompatActivity {

    private View ivMenu; // Trigger for dropdown

    EditText editTextName, editTextEmail, editTextPhone, editTextDeviceModel,
            editTextProblem, editTextDate, editTextAddress;
    Spinner spinnerTime, spinnerServiceMethod;
    Button buttonSubmit;

    FirebaseAuth auth;
    FirebaseFirestore db;

    Calendar selectedDate = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_washer_repair);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize Views
        ivMenu = findViewById(R.id.buttonHamburger);
        editTextName = findViewById(R.id.editTextName);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPhone = findViewById(R.id.editTextPhone);
        editTextDeviceModel = findViewById(R.id.editTextDeviceModel);
        editTextProblem = findViewById(R.id.editTextProblem);
        editTextDate = findViewById(R.id.editTextDate);
        editTextAddress = findViewById(R.id.editTextAddress);
        spinnerTime = findViewById(R.id.spinnerTime);
        spinnerServiceMethod = findViewById(R.id.spinnerServiceMethod);
        buttonSubmit = findViewById(R.id.buttonSubmit);

        // Autofill user details
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            editTextEmail.setText(user.getEmail());
            db.collection("users").document(user.getUid())
                    .get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            editTextName.setText(doc.getString("name"));
                        }
                    });
        }

        editTextName.setEnabled(false);
        editTextEmail.setEnabled(false);

        // Time slots
        String[] timeSlots = {
                "Select Time Slot",
                "Morning (9:00 AM - 12:00 PM)",
                "Afternoon (12:00 PM - 3:00 PM)",
                "Evening (3:00 PM - 6:00 PM)"
        };
        ArrayAdapter<String> adapterTime = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, timeSlots);
        adapterTime.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTime.setAdapter(adapterTime);

        // Service Method Spinner
        String[] serviceMethods = {"Select service method", "Home Pickup", "Drop-off at Service Center"};
        ArrayAdapter<String> adapterMethod = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, serviceMethods);
        adapterMethod.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerServiceMethod.setAdapter(adapterMethod);

        spinnerServiceMethod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 1) { // Home Pickup
                    editTextAddress.setVisibility(View.VISIBLE);
                } else {
                    editTextAddress.setVisibility(View.GONE);
                    editTextAddress.setText("");
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        editTextDate.setFocusable(false);
        editTextDate.setOnClickListener(v -> showDatePicker());

        // Setup Dropdown Menu
        ivMenu.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(WasherRepairActivity.this, ivMenu);
            popup.getMenu().add("Home");
            popup.getMenu().add("Browse Services");
            popup.getMenu().add("My Bookings");
            popup.getMenu().add("My Notices/Notifications");
            popup.getMenu().add("Support & Device Care");
            popup.getMenu().add("Edit Profile");
            popup.getMenu().add("Logout");

            popup.setOnMenuItemClickListener(item -> {
                String title = item.getTitle().toString();
                switch (title) {
                    case "Home":
                        startActivity(new Intent(this, MainActivity.class));
                        return true;
                    case "Browse Services":
                        startActivity(new Intent(this, ServiceBrowserActivity.class));
                        return true;
                    case "My Bookings":
                        startActivity(new Intent(this, BookingHistoryActivity.class));
                        return true;
                    case "My Notices/Notifications":
                        startActivity(new Intent(this, AdminNoticesActivity.class));
                        return true;
                    case "Support & Device Care":
                        startActivity(new Intent(this, SupportTipsActivity.class));
                        return true;
                    case "Edit Profile":
                        startActivity(new Intent(this, EditProfileActivity.class));
                        return true;
                    case "Logout":
                        confirmLogout();
                        return true;
                    default:
                        return false;
                }
            });
            popup.show();
        });

        buttonSubmit.setOnClickListener(v -> submitBooking());
    }

    private void showDatePicker() {
        Calendar today = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this, (view, y, m, d) -> {
            selectedDate.set(y, m, d);
            editTextDate.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(selectedDate.getTime()));
        }, today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH));
        dialog.getDatePicker().setMinDate(today.getTimeInMillis());
        dialog.show();
    }

    private void submitBooking() {
        String phone = editTextPhone.getText().toString().trim();
        String deviceModel = editTextDeviceModel.getText().toString().trim();
        String problem = editTextProblem.getText().toString().trim();
        String date = editTextDate.getText().toString().trim();
        String timeSlot = spinnerTime.getSelectedItem().toString();
        String serviceMethod = spinnerServiceMethod.getSelectedItem().toString();
        String address = editTextAddress.getText().toString().trim();

        if (phone.length() != 10) { editTextPhone.setError("10 digits required"); return; }
        if (deviceModel.isEmpty()) { editTextDeviceModel.setError("Enter model"); return; }
        if (date.isEmpty()) { Toast.makeText(this, "Select date", Toast.LENGTH_SHORT).show(); return; }

        if (serviceMethod.equals("Home Pickup") && address.isEmpty()) {
            editTextAddress.setError("Address required for Pickup");
            return;
        }

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        Map<String, Object> booking = new HashMap<>();
        booking.put("userId", user.getUid());
        booking.put("name", editTextName.getText().toString());
        booking.put("email", editTextEmail.getText().toString());
        booking.put("phone", phone);
        booking.put("deviceModel", deviceModel);
        booking.put("problem", problem);
        booking.put("serviceType", "Washing Machine Repair");
        booking.put("serviceMethod", serviceMethod);

        // ✅ Edited part: store null for Drop-off
        if (serviceMethod.equals("Home Pickup")) {
            booking.put("address", address);
        } else {
            booking.put("address", null);
        }

        booking.put("visitDate", date);
        booking.put("timeSlot", timeSlot);
        booking.put("status", "Pending");
        booking.put("createdAt", FieldValue.serverTimestamp());

        db.collection("bookings").add(booking)
                .addOnSuccessListener(doc -> {
                    Toast.makeText(this, "Repair request submitted!", Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void confirmLogout() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Do you want to logout?")
                .setPositiveButton("Yes", (d, w) -> {
                    auth.signOut();
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                })
                .setNegativeButton("No", null)
                .show();
    }
}
