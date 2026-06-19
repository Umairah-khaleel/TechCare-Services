package com.example.TechCareServices;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;

public class AdminHomeActivity extends AppCompatActivity {

    private ImageView ivMenu;
    private Button btnLogout;
    private CardView cardAll, cardPending, cardCompleted, cardUsers, cardNotices, cardAddAdmin, cardAddService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);

        // Initialize Views
        ivMenu = findViewById(R.id.ivMenu);
        btnLogout = findViewById(R.id.btnLogout);
        cardAll = findViewById(R.id.cardAllBookings);
        cardPending = findViewById(R.id.cardPendingBookings);
        cardCompleted = findViewById(R.id.cardCompletedBookings);
        cardUsers = findViewById(R.id.cardAllUsers);
        cardNotices = findViewById(R.id.cardSendNotices);
        cardAddAdmin = findViewById(R.id.cardAddAdmin);
        cardAddService = findViewById(R.id.cardAddService); // Initialized new card

        // Hamburger Menu Logic
        ivMenu.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(AdminHomeActivity.this, ivMenu);
            popup.getMenu().add(getString(R.string.admin_all_bookings));

            // CHANGED: "Pending Bookings" changed to "Manage Bookings" in the Popup Menu
            popup.getMenu().add(getString(R.string.admin_pending_bookings));

            popup.getMenu().add(getString(R.string.admin_completed_bookings));
            popup.getMenu().add(getString(R.string.admin_view_users));
            popup.getMenu().add(getString(R.string.admin_send_notices));
            popup.getMenu().add(getString(R.string.admin_add_new_admin));
            popup.getMenu().add(getString(R.string.admin_add_service)); // Using string resource for Popup
            popup.getMenu().add(getString(R.string.edit_profile));
            popup.getMenu().add(getString(R.string.nav_logout));

            popup.setOnMenuItemClickListener(item -> {
                String title = item.getTitle().toString();
                if (title.equals(getString(R.string.admin_all_bookings))) {
                    cardAll.performClick();
                } else if (title.equals(getString(R.string.admin_pending_bookings))) {
                    // Logic points to the same card as before (Managing active/pending work)
                    cardPending.performClick();
                } else if (title.equals(getString(R.string.admin_completed_bookings))) {
                    cardCompleted.performClick();
                } else if (title.equals(getString(R.string.admin_view_users))) {
                    cardUsers.performClick();
                } else if (title.equals(getString(R.string.admin_send_notices))) {
                    cardNotices.performClick();
                } else if (title.equals(getString(R.string.admin_add_new_admin))) {
                    cardAddAdmin.performClick();
                } else if (title.equals(getString(R.string.admin_add_service))) {
                    cardAddService.performClick(); // Triggers the card listener below
                } else if (title.equals(getString(R.string.edit_profile))) {
                    Toast.makeText(this, getString(R.string.toast_opening_edit_profile), Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, EditProfileActivity.class));
                } else if (title.equals(getString(R.string.nav_logout))) {
                    showLogoutDialog();
                }
                return true;
            });
            popup.show();
        });

        // CardView Click Listeners
        cardAll.setOnClickListener(v -> {
            Toast.makeText(this, getString(R.string.toast_showing_all), Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, AdminAllBookingsActivity.class));
        });

        cardPending.setOnClickListener(v -> {
            Toast.makeText(this, getString(R.string.toast_showing_pending), Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, AdminPendingBookingsActivity.class));
        });

        cardCompleted.setOnClickListener(v -> {
            Toast.makeText(this, getString(R.string.toast_showing_completed), Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, AdminCompletedBookingsActivity.class));
        });

        cardUsers.setOnClickListener(v -> {
            Toast.makeText(this, getString(R.string.toast_showing_users), Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, AdminUsersActivity.class));
        });

        cardNotices.setOnClickListener(v -> {
            Toast.makeText(this, getString(R.string.toast_opening_notices), Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, AdminSendNoticesActivity.class));
        });

        cardAddAdmin.setOnClickListener(v -> {
            Toast.makeText(this, getString(R.string.toast_opening_admin_mgmt), Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, AddAdminActivity.class));
        });

        // New Card Click Listener with string resource Toast
        cardAddService.setOnClickListener(v -> {
            Toast.makeText(this, getString(R.string.toast_opening_add_service), Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, AddServiceActivity.class));
        });

        btnLogout.setOnClickListener(v -> showLogoutDialog());
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(AdminHomeActivity.this)
                .setTitle(getString(R.string.logout_title))
                .setMessage(getString(R.string.logout_message))
                .setPositiveButton(getString(R.string.yes), (dialog, which) -> {
                    FirebaseAuth.getInstance().signOut();
                    Toast.makeText(this, getString(R.string.toast_logged_out), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(AdminHomeActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton(getString(R.string.no), null)
                .show();
    }
}