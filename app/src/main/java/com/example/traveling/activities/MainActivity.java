package com.example.traveling.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.traveling.R;
import com.example.traveling.fragments.travelpath.PreferencesFragment;
import com.example.traveling.fragments.travelshare.FeedFragment;
import com.example.traveling.fragments.travelshare.ProfileFragment;
import com.example.traveling.fragments.travelshare.SearchFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.traveling.fragments.travelshare.NewPostFragment;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        if (savedInstanceState == null) {
            loadFragment(new FeedFragment());
        }

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_feed) {
                loadFragment(new FeedFragment());
                return true;
            } else if (itemId == R.id.nav_search) {
                loadFragment(new SearchFragment());
                return true;
            } else if (itemId == R.id.nav_new_post) {
                loadFragment(new NewPostFragment());
                return true;
            }   else if (itemId == R.id.nav_routes) {
                loadFragment(new PreferencesFragment());
                return true;
            } else if (itemId == R.id.nav_profile) {
                loadFragment(new ProfileFragment());
                return true;
            }

            return false;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}