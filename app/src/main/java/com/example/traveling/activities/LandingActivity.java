package com.example.traveling.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.traveling.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

public class LandingActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {
            startActivity(new Intent(LandingActivity.this, MainActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_landing);

        MaterialButton btnStartAdventure = findViewById(R.id.btn_start_adventure);
        MaterialButton btnLogin = findViewById(R.id.btn_login);

        btnStartAdventure.setOnClickListener(v ->
                startActivity(new Intent(LandingActivity.this, MainActivity.class)));

        btnLogin.setOnClickListener(v ->
                startActivity(new Intent(LandingActivity.this, AuthActivity.class)));
    }
}