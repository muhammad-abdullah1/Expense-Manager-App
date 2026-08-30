package com.example.expenseapp;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class PersonalExpenseFragment extends Fragment {

    private ListView personalExpenseListView;
    private FloatingActionButton addPersonalExpenseFab;
    private TextView personalTotalCostTextView;
    private DatabaseReference personalExpensesRef;
    private ArrayList<String> personalExpenseList;
    private ArrayAdapter<String> personalExpenseAdapter;
    private double totalCost = 0.0;
    private Calendar selectedDate = Calendar.getInstance(); // For date picker

    @Override

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_personal_expense, container, false);

        // Initialize UI elements
        addPersonalExpenseFab = view.findViewById(R.id.addPersonalExpenseFab);
        personalTotalCostTextView = view.findViewById(R.id.personalTotalCostTextView);

        // Initialize Firebase
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if (userId == null) {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            requireActivity().finish(); // Close the activity
            return view;
        }
        personalExpensesRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("personalExpenses");

        // Initialize RecyclerView
        RecyclerView personalExpenseRecyclerView = view.findViewById(R.id.personalExpenseRecyclerView);
        personalExpenseRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize adapter
        FirebaseRecyclerOptions<Expense> options = new FirebaseRecyclerOptions.Builder<Expense>()
                .setQuery(personalExpensesRef, Expense.class)
                .build();

        ExpenseAdapter expenseAdapter = new ExpenseAdapter(options, getContext(), personalExpensesRef, personalTotalCostTextView, totalCost);
        personalExpenseRecyclerView.setAdapter(expenseAdapter);

        // Start listening for data
        expenseAdapter.startListening();

        // Handle FAB click to add expense
        addPersonalExpenseFab.setOnClickListener(v -> showAddPersonalExpenseDialog());

        return view;
    }

    private void loadPersonalExpenses() {
        personalExpensesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                personalExpenseList.clear();
                totalCost = 0.0; // Reset total cost

                if (snapshot.exists()) {
                    Log.d("PersonalExpenseFragment", "Number of expenses: " + snapshot.getChildrenCount());
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Expense expense = dataSnapshot.getValue(Expense.class);
                        if (expense != null) {
                            Log.d("PersonalExpenseFragment", "Expense retrieved: " + expense.getDescription() + " - $" + expense.getAmount());

                            // Format the date as "day month name" (e.g., "15 Oct")
                            String formattedDate = formatDate(expense.getDate());

                            // Include the formatted date in the display
                            personalExpenseList.add(expense.getDescription() + " - $" + expense.getAmount() + " (" + formattedDate + ")");
                            totalCost += expense.getAmount(); // Update total cost
                        } else {
                            Log.e("PersonalExpenseFragment", "Invalid expense data: " + dataSnapshot.toString());
                        }
                    }
                } else {
                    Log.d("PersonalExpenseFragment", "No expenses found in Firebase");
                    Toast.makeText(getContext(), "No expenses found", Toast.LENGTH_SHORT).show();
                }

                // Update the ListView and total cost TextView
                personalExpenseAdapter.notifyDataSetChanged();
                personalTotalCostTextView.setText("Total Cost: $" + String.format("%.2f", totalCost));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("PersonalExpenseFragment", "Failed to load expenses: " + error.getMessage());
                Toast.makeText(getContext(), "Failed to load expenses", Toast.LENGTH_SHORT).show();
            }
        });
    }
    // Helper method to format the date as "day month name" (e.g., "15 Oct")
    private String formatDate(String dateStr) {
        try {
            // Parse the stored date (assuming it's in "MM/dd/yyyy" format)
            SimpleDateFormat inputFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
            Date date = inputFormat.parse(dateStr);

            // Format the date as "day month name" (e.g., "15 Oct")
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM", Locale.US);
            return outputFormat.format(date);
        } catch (Exception e) {
            Log.e("PersonalExpenseFragment", "Error formatting date: " + e.getMessage());
            return dateStr; // Return the original date if formatting fails
        }
    }

    private void showAddPersonalExpenseDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Add Personal Expense");

        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_expense, null);
        builder.setView(dialogView);

        EditText descriptionInput = dialogView.findViewById(R.id.descriptionInput);
        EditText amountInput = dialogView.findViewById(R.id.amountInput);
        EditText dateInput = dialogView.findViewById(R.id.dateInput);

        // Set up the date picker
        dateInput.setOnClickListener(v -> showDatePicker(dateInput));

        builder.setPositiveButton("Add", (dialog, which) -> {
            String description = descriptionInput.getText().toString().trim();
            String amountStr = amountInput.getText().toString().trim();
            String date = dateInput.getText().toString().trim();

            if (description.isEmpty() || amountStr.isEmpty() || date.isEmpty()) {
                Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double amount = Double.parseDouble(amountStr);
                String expenseId = personalExpensesRef.push().getKey();
                Expense expense = new Expense(expenseId, description, amount, date); // Include the date

                personalExpensesRef.child(expenseId).setValue(expense)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(getContext(), "Expense added", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(getContext(), "Failed to add expense", Toast.LENGTH_SHORT).show();
                        });
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Invalid amount", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }

    private void showDatePicker(EditText dateInput) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    selectedDate.set(year, month, dayOfMonth);
                    updateDateInput(dateInput);
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void updateDateInput(EditText dateInput) {
        String dateFormat = "MM/dd/yyyy"; // You can change the format as needed
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat, Locale.US);
        dateInput.setText(sdf.format(selectedDate.getTime()));
    }
}