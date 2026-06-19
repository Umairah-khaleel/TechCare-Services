package com.example.TechCareServices;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private ImageView ivMenu;
    private Button btnBrowse, btnLogoutBig;
    private CardView cardSupportTips; // Requirement 5 reference

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Initialize all views
        ivMenu = findViewById(R.id.ivMenu);
        btnBrowse = findViewById(R.id.btnBrowseServices);
        btnLogoutBig = findViewById(R.id.btnLogoutBig);
        cardSupportTips = findViewById(R.id.cardSupportTips); // Added this to match your XML

        CardView cardMobile = findViewById(R.id.cardMobile);
        CardView cardLaptop = findViewById(R.id.cardLaptop);
        CardView cardTV = findViewById(R.id.cardTV);
        CardView cardAC = findViewById(R.id.cardAC);
        CardView cardFridge = findViewById(R.id.cardFridge);
        CardView cardWasher = findViewById(R.id.cardWasher);

        // 2. Setup Hamburger Popup Menu
        ivMenu.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(MainActivity.this, ivMenu);
            popup.getMenu().add("Home");
            popup.getMenu().add("Browse Services");
            popup.getMenu().add("My Bookings");
            popup.getMenu().add("My Notices/Notifications"); // Updated name
            popup.getMenu().add("Support & Device Care"); // Added Requirement 5 to your list
            popup.getMenu().add("Edit Profile");
            popup.getMenu().add("Logout");

            popup.setOnMenuItemClickListener(item -> {
                switch (item.getTitle().toString()) {
                    case "Home":
                        Toast.makeText(MainActivity.this, "Home clicked", Toast.LENGTH_SHORT).show();
                        return true;
                    case "Browse Services":
                        startActivity(new Intent(MainActivity.this, ServiceBrowserActivity.class));
                        return true;
                    case "My Bookings":
                        startActivity(new Intent(MainActivity.this, BookingHistoryActivity.class));
                        return true;
                    case "My Notices/Notifications": // Updated logic to match
                        startActivity(new Intent(MainActivity.this, AdminNoticesActivity.class));
                        return true;
                    case "Support & Device Care": // Logic to open your new screen
                        startActivity(new Intent(MainActivity.this, SupportTipsActivity.class));
                        return true;
                    case "Edit Profile":
                        startActivity(new Intent(MainActivity.this, EditProfileActivity.class));
                        return true;
                    case "Logout":
                        showLogoutConfirmation();
                        return true;
                    default:
                        return false;
                }
            });
            popup.show();
        });

        // 3. Setup Support Card Click (From the Dashboard)
        cardSupportTips.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, SupportTipsActivity.class));
        });

        // 4. Setup Big Bottom Buttons
        btnBrowse.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, ServiceBrowserActivity.class));
        });

        btnLogoutBig.setOnClickListener(v -> {
            showLogoutConfirmation();
        });

        // 5. Setup Device Category Cards (Kept exactly the same)
        cardMobile.setOnClickListener(v -> {
            Toast.makeText(this, "Mobile Repair clicked", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MainActivity.this, MobileRepairActivity.class));
        });

        cardLaptop.setOnClickListener(v -> {
            Toast.makeText(this, "Laptop Repair clicked", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MainActivity.this, LaptopRepairActivity.class));
        });

        cardTV.setOnClickListener(v -> {
            Toast.makeText(this, "TV Repair clicked", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MainActivity.this, TVRepairActivity.class));
        });

        cardAC.setOnClickListener(v -> {
            Toast.makeText(this, "AC Repair clicked", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MainActivity.this, ACRepairActivity.class));
        });

        cardFridge.setOnClickListener(v -> {
            Toast.makeText(this, "Fridge Repair clicked", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MainActivity.this, FridgeRepairActivity.class));
        });

        cardWasher.setOnClickListener(v -> {
            Toast.makeText(this, "Washing Machine Repair clicked", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MainActivity.this, WasherRepairActivity.class));
        });
    }

    // 6. Shared Logout Function
    private void showLogoutConfirmation() {
        new androidx.appcompat.app.AlertDialog.Builder(MainActivity.this)
                .setTitle("Logout")
                .setMessage("Do you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    FirebaseAuth.getInstance().signOut();
                    Toast.makeText(MainActivity.this, "Logged out", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();
                })
                .setNegativeButton("No", null)
                .show();
    }
}