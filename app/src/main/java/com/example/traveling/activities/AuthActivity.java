package com.example.traveling.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.traveling.R;
import com.example.traveling.fragments.auth.LoginFragment;

public class AuthActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.auth_fragment_container, new LoginFragment())
                    .commit();
        }
    }
}