package com.example.expenseapp;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddExpenseDialog extends DialogFragment {

    private EditText descriptionInput, amountInput, dateInput;
    private Calendar selectedDate = Calendar.getInstance();

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_add_expense, null);

        descriptionInput = view.findViewById(R.id.descriptionInput);
        amountInput = view.findViewById(R.id.amountInput);
        dateInput = view.findViewById(R.id.dateInput);

        // Set up the date picker
        dateInput.setOnClickListener(v -> showDatePicker());

        builder.setView(view)
                .setTitle("Add Expense")
                .setPositiveButton("Add", (dialog, which) -> addExpense())
                .setNegativeButton("Cancel", (dialog, which) -> dismiss());

        return builder.create();
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    selectedDate.set(year, month, dayOfMonth);
                    updateDateInput();
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void updateDateInput() {
        String dateFormat = "MM/dd/yyyy"; // You can change the format as needed
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat, Locale.US);
        dateInput.setText(sdf.format(selectedDate.getTime()));
    }

    private void addExpense() {
        String description = descriptionInput.getText().toString().trim();
        String amountStr = amountInput.getText().toString().trim();
        String date = dateInput.getText().toString().trim();

        if (description.isEmpty() || amountStr.isEmpty() || date.isEmpty()) {
            Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            Toast.makeText(getContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }
        String userId = auth.getCurrentUser().getUid(); // Safe to call now

        double amount = Double.parseDouble(amountStr);
        DatabaseReference expensesRef = FirebaseDatabase.getInstance().getReference("expenses");
        String expenseId = expensesRef.push().getKey();

        Expense expense = new Expense(expenseId, description, amount, date);
        expensesRef.child(userId).child(expenseId).setValue(expense)
                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Expense added", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to add expense", Toast.LENGTH_SHORT).show());
    }
}