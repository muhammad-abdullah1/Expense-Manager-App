package com.example.expenseapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;

public class ExpenseAdapter extends FirebaseRecyclerAdapter<Expense, ExpenseAdapter.ExpenseViewHolder> {

    private Context context;
    private DatabaseReference expensesRef;
    private TextView totalCostTextView;
    private double totalCost;

    // Constructor
    public ExpenseAdapter(@NonNull FirebaseRecyclerOptions<Expense> options, Context context, DatabaseReference expensesRef, TextView totalCostTextView, double totalCost) {
        super(options);
        this.context = context;
        this.expensesRef = expensesRef;
        this.totalCostTextView = totalCostTextView;
        this.totalCost = totalCost;
    }

    // Bind data to the ViewHolder
    @Override
    protected void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position, @NonNull Expense expense) {
        holder.description.setText(expense.getDescription());
        holder.amount.setText(String.format("$%.2f", expense.getAmount()));

        // Handle Delete button click
        holder.deleteButton.setOnClickListener(v -> {
            // Show confirmation dialog
            new AlertDialog.Builder(context)
                    .setTitle("Delete Expense")
                    .setMessage("Are you sure you want to delete this expense?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        // Get the expense ID
                        String expenseId = getRef(position).getKey();

                        // Delete the expense from Firebase
                        if (expenseId != null) {
                            expensesRef.child(expenseId).removeValue()
                                    .addOnSuccessListener(aVoid -> {
                                        // Update the total cost
                                        totalCost -= expense.getAmount();
                                        totalCostTextView.setText("Total Cost: $" + String.format("%.2f", totalCost));

                                        Toast.makeText(context, "Expense deleted", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(context, "Failed to delete expense", Toast.LENGTH_SHORT).show();
                                    });
                        }
                    })
                    .setNegativeButton("No", null)
                    .show();
        });
    }

    // Create the ViewHolder
    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_expense, parent, false);
        return new ExpenseViewHolder(view);
    }

    // ViewHolder class
    public static class ExpenseViewHolder extends RecyclerView.ViewHolder {
        TextView description, amount;
        Button deleteButton;

        public ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            description = itemView.findViewById(R.id.expenseDescription);
            amount = itemView.findViewById(R.id.expenseAmount);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}