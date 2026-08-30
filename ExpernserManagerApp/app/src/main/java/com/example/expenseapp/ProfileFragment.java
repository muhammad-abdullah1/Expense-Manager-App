package com.example.expenseapp;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileFragment extends Fragment {

    private TextView userIdTextView;
    private TextView username;
    private TextView emailTextView;
    private Button logoutButton;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        userIdTextView = view.findViewById(R.id.userIdTextView);
        username = view.findViewById(R.id.username);
        logoutButton = view.findViewById(R.id.logoutButton);
        emailTextView = view.findViewById(R.id.emailTextView);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("users");

        // Get the current user ID
        String userId = mAuth.getCurrentUser().getUid();
        userIdTextView.setText("User ID: " + userId);

        // Retrieve and display the username from Firebase
        mDatabase.child(userId).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.getValue(String.class);
                    username.setText("User Name: " + name);
                } else {
                    username.setText("User Name: Not Found");
                }
            }
            

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                username.setText("Failed to load name");
            }
        });
        mDatabase.child(userId).child("email").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String email = snapshot.getValue(String.class);
                    emailTextView.setText("User Email: " + email);
                } else {
                    emailTextView.setText("User Email: Not Found");
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                username.setText("Failed to load Email");
            }
        });

        // Logout Button
        logoutButton.setOnClickListener(v -> {
            mAuth.signOut();
            requireActivity().finish(); // Close the activity

        });

        return view;
    }
}
