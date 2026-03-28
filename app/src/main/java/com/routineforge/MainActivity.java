package com.routineforge;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.routineforge.fragments.CalendarFragment;
import com.routineforge.fragments.HomeFragment;
import com.routineforge.fragments.ProfileFragment;
import com.routineforge.utils.AlarmScheduler;
import com.routineforge.utils.PrefsManager;

public class MainActivity extends AppCompatActivity {

    private HomeFragment homeFragment;
    private CalendarFragment calendarFragment;
    private ProfileFragment profileFragment;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check onboarding
        if (!PrefsManager.getInstance(this).isOnboardingDone()) {
            startActivity(new Intent(this, OnboardingActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);
        AlarmScheduler.createNotificationChannel(this);

        homeFragment = new HomeFragment();
        calendarFragment = new CalendarFragment();
        profileFragment = new ProfileFragment();

        bottomNav = findViewById(R.id.bottom_nav);
        loadFragment(homeFragment);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                loadFragment(homeFragment);
                return true;
            } else if (id == R.id.nav_calendar) {
                loadFragment(calendarFragment);
                return true;
            } else if (id == R.id.nav_profile) {
                loadFragment(profileFragment);
                return true;
            }
            return false;
        });

        // If opened from notification
        if (getIntent() != null) {
            int taskId = getIntent().getIntExtra("task_id", -1);
            if (taskId != -1) {
                bottomNav.setSelectedItemId(R.id.nav_home);
            }
        }
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, fragment);
        ft.commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh home when returning
        if (homeFragment != null && homeFragment.isAdded()) {
            homeFragment.refresh();
        }
    }
}
