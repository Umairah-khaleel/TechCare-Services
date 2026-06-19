package com.example.TechCareServices;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

public class SupportTipsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support_tips);

        ImageView btnBack = findViewById(R.id.btnBack);
        Button btnCall = findViewById(R.id.btnCallSupport);

        // Standard Back Button
        btnBack.setOnClickListener(v -> finish());

        // Contact Support Logic (Requirement 5)
        btnCall.setOnClickListener(v -> {
            String phoneNumber = "+94 764862748"; // Replace with a real number
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + phoneNumber));
            startActivity(intent);
        });
    }
}