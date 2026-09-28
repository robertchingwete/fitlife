package com.example.lifefit;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class DashboardActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        bottomNavigation = findViewById(R.id.bottomNavigation);

        // Load default fragment (Home)
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }

        // Setup bottom navigation listener
        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                loadFragment(new HomeFragment());
                return true;
            } else if (itemId == R.id.nav_workouts) {
                loadFragment(new WorkoutsFragment());
                return true;
            } else if (itemId == R.id.nav_create) {
                loadFragment(new CreateFragment());
                return true;
            } else if (itemId == R.id.nav_profile) {
                loadFragment(new ProfileFragment());
                return true;
            }
            return false;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
    // Public method to navigate to Workouts tab (called from HomeFragment)
    public void navigateToWorkoutsTab() {
        bottomNavigation.setSelectedItemId(R.id.nav_workouts);
    }

    // Public method to navigate to Create tab
    public void navigateToCreateTab() {
        bottomNavigation.setSelectedItemId(R.id.nav_create);
    }

    // Public method to navigate to Profile tab
    public void navigateToProfileTab() {
        bottomNavigation.setSelectedItemId(R.id.nav_profile);
    }

    // Public method to navigate to Home tab
    public void navigateToHomeTab() {
        bottomNavigation.setSelectedItemId(R.id.nav_home);
    }
}
