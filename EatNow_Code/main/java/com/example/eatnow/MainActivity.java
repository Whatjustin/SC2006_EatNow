package com.example.eatnow;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.eatnow.fragment.ActivitiesFragment;
import com.example.eatnow.fragment.DealsFragment;
import com.example.eatnow.fragment.HomeFragment;
import com.example.eatnow.fragment.LoginFragment;
import com.example.eatnow.fragment.ProfileFragment;
import com.example.eatnow.fragment.SearchFragment;
import com.example.eatnow.model.UserAccount;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/*
    Represents Main Activity.
    Application will load this class.
 */
public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    // Selected Fragment
    Fragment selectedFragment;
    // Store user's account
    UserAccount userAccount;
    // Bottom navigation view to select UI
    BottomNavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Hide the top "EatNow" action bar
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        // Set Layout of Main Activity
        setContentView(R.layout.activity_main);

        // Retrieve Bottom Navigation View
        navigationView = findViewById(R.id.nav_view);
        navigationView.setOnNavigationItemSelectedListener(this);

        // Set to Home Fragment
        navigationView.setSelectedItemId(R.id.nav_home);
    }

    // Navigating to other fragments
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch (id) {
            case R.id.nav_home:
                selectedFragment = new HomeFragment();
                moveToFragment(selectedFragment);
                return true;
            case R.id.nav_search:
                selectedFragment = new SearchFragment();
                moveToFragment(selectedFragment);
                return true;
            case R.id.nav_deals:
                selectedFragment = new DealsFragment();
                moveToFragment(selectedFragment);
                return true;
            case R.id.nav_activities:
                selectedFragment = new ActivitiesFragment();
                moveToFragment(selectedFragment);
                return true;
            case R.id.nav_profile:
                userAccount = UserAccount.getInstance();
                if (userAccount == null) {
                    // If User has yet to register / login
                    selectedFragment = new LoginFragment();
                    moveToFragment(selectedFragment);
                } else {
                    // If User has already logged in
                    selectedFragment = new ProfileFragment();
                    moveToFragment(selectedFragment);
                }
                return true;
        }
        return false;
    }

    // Method to replace fragment
    private void moveToFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

}