package com.example.expenseapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
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

        String category = expense.getCategory();
        holder.categoryIcon.setImageResource(CategoryUtils.getIconRes(category));
        holder.categoryIconCard.setCardBackgroundColor(CategoryUtils.getColor(context, category));

        String date = expense.getDate();
        if (date != null && !date.isEmpty()) {
            holder.meta.setText(category + " \u00b7 " + date);
        } else {
            holder.meta.setText(category);
        }

        // Recalculate the total every time the adapter binds items
        // (this runs whenever data is added, removed, or changed)
        recalculateTotal();
    }

    // Sum up every currently loaded expense and refresh the total TextView
    private void recalculateTotal() {
        double sum = 0.0;
        for (int i = 0; i < getItemCount(); i++) {
            Expense e = getItem(i);
            if (e != null) {
                sum += e.getAmount();
            }
        }
        totalCost = sum;
        totalCostTextView.setText(String.format("$%.2f", totalCost));
    }

    // Also recalc whenever Firebase pushes a fresh snapshot of data
    // (covers the case where the last item is deleted and onBindViewHolder
    // never gets called with any position)
    @Override
    public void onDataChanged() {
        super.onDataChanged();
        recalculateTotal();
    }

    // --- Helpers used by ItemTouchHelper (swipe-to-delete) in the fragment ---

    public Expense getExpenseAt(int position) {
        return getItem(position);
    }

    public DatabaseReference getRefAt(int position) {
        return getRef(position);
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
        TextView description, amount, meta;
        ImageView categoryIcon;
        com.google.android.material.card.MaterialCardView categoryIconCard;

        public ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            description = itemView.findViewById(R.id.expenseDescription);
            amount = itemView.findViewById(R.id.expenseAmount);
            meta = itemView.findViewById(R.id.expenseMeta);
            categoryIcon = itemView.findViewById(R.id.categoryIcon);
            categoryIconCard = itemView.findViewById(R.id.categoryIconCard);
        }
    }
}
