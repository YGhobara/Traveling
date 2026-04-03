package com.example.traveling.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.traveling.R;

public class LandingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);

        Button btnStartAdventure = findViewById(R.id.btn_register);
        Button btnLogin = findViewById(R.id.btn_login);

        btnStartAdventure.setOnClickListener(v ->
                startActivity(new Intent(LandingActivity.this, MainActivity.class)));

        btnLogin.setOnClickListener(v ->
                startActivity(new Intent(LandingActivity.this, AuthActivity.class)));
    }
}