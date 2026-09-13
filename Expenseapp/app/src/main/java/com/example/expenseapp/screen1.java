package com.example.expenseapp;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class screen1 extends AppCompatActivity {

    private TextView totalCostTextView;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen1);

        // Initialize UI elements
        //totalCostTextView = findViewById(R.id.totalCostTextView);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // Load default fragment (ProfileFragment)
        loadFragment(new ProfileFragment());

        // Handle bottom navigation
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.profileFragment) {
                loadFragment(new ProfileFragment());
            } else if (itemId == R.id.groupExpenseFragment) {
                loadFragment(new GroupExpenseFragment());
            } else if (itemId == R.id.personalExpenseFragment) {
                loadFragment(new PersonalExpenseFragment());
            }

            return true;
        });
    }

    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragmentContainer, fragment);
        transaction.commit();
    }

    // Method to update the total cost TextView
    public void updateTotalCost(double totalCost) {
        totalCostTextView.setText("Total Cost: $" + String.format("%.2f", totalCost));
    }
}