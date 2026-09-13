package com.example.expenseapp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;


public class GroupExpenseFragment extends Fragment {

    private DatabaseReference groupsRef, usersRef;
    private String currentUserId;
    private ListView groupExpenseListView, balanceListView;
    private FloatingActionButton addGroupExpenseFab, createGroupFab, joinGroupFab;
    private ArrayAdapter<String> expenseAdapter, balanceAdapter;
    private ArrayList<String> expenseList = new ArrayList<String>();
    private ArrayList<String> expenseIds = new ArrayList<>();
    private ArrayList<String> balanceList = new ArrayList<>();
    private ProgressDialog progressDialog;
    private TextView groupNameTextView;
    private Spinner groupSpinner;
    private ArrayAdapter<String> groupSpinnerAdapter;
    private List<String> groupNames = new ArrayList<>();
    private List<String> groupCodes = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_group_expense, container, false);

        // Initialize views
        groupNameTextView = view.findViewById(R.id.groupNameTextView);
        groupExpenseListView = view.findViewById(R.id.groupExpenseListView);
        balanceListView = view.findViewById(R.id.balanceListView);
        addGroupExpenseFab = view.findViewById(R.id.addGroupExpenseFab);
        createGroupFab = view.findViewById(R.id.createGroupFab);
        joinGroupFab = view.findViewById(R.id.joinGroupFab);
        groupSpinner = view.findViewById(R.id.groupSpinner);
        groupExpenseListView.setOnItemLongClickListener((parent, view1, position,  id) -> {
            String expenseId = expenseIds.get(position); // Get the expense ID
            showDeleteExpenseDialog(expenseId, position);
            return true;
        });
        // Firebase initialization
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        groupsRef = FirebaseDatabase.getInstance().getReference("groups");
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        // Setup adapters
        setupAdapters();

        // Load user's groups
        loadUserGroups();

        // Handle group actions
        handleGroupActions();

        return view;
    }
    private void showDeleteExpenseDialog(String expenseId, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Delete Expense");
        builder.setMessage("Have you paid the group members?");

        builder.setPositiveButton("Yes", (dialog, which) -> {
            deleteExpense(expenseId, position); // Use the expense ID directly
        });

        builder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());
        builder.show();
    }
    private void deleteExpense(String expenseId, int position) {
        String groupCode = groupCodes.get(groupSpinner.getSelectedItemPosition());
        if (groupCode == null || expenseId == null) {
            Toast.makeText(getContext(), "Invalid group or expense", Toast.LENGTH_SHORT).show();
            return;
        }

        showProgressDialog("Deleting expense...");

        // Fetch the expense details
        groupsRef.child(groupCode).child("expenses").child(expenseId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Expense expense = snapshot.getValue(Expense.class);
                if (expense != null) {
                    // Update balances
                    updateBalancesAfterDeletion(groupCode, expense);

                    // Delete the expense
                    groupsRef.child(groupCode).child("expenses").child(expenseId).removeValue()
                            .addOnSuccessListener(aVoid -> {
                                hideProgressDialog();
                                expenseList.remove(position);
                                expenseIds.remove(position); // Remove the expense ID
                                expenseAdapter.notifyDataSetChanged();
                                Toast.makeText(getContext(), "Expense deleted", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                hideProgressDialog();
                                Toast.makeText(getContext(), "Failed to delete expense", Toast.LENGTH_SHORT).show();
                            });
                } else {
                    hideProgressDialog();
                    Toast.makeText(getContext(), "Expense not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                hideProgressDialog();
                Toast.makeText(getContext(), "Failed to fetch expense details", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void updateBalancesAfterDeletion(String groupCode, Expense expense) {
        double perPerson = expense.getPerPerson();
        Map<String, Double> splitMembers = expense.getSplitMembers();

        for (Map.Entry<String, Double> entry : splitMembers.entrySet()) {
            String memberId = entry.getKey();
            double adjustment = memberId.equals(currentUserId) ?
                    perPerson * (splitMembers.size() - 1) : // Payer's balance adjustment
                    -perPerson; // Other members' balance adjustment

            updateBalance(memberId, groupCode, adjustment);
        }
    }
//    private String extractExpenseId(String expense) {
//        // Extract the expense ID from the expense string
//        // Assuming the expense string is in the format: "description - $amount"
//        // You may need to adjust this based on your actual format
//        String[] parts = expense.split(" - ");
//        if (parts.length > 0) {
//            return parts[0]; // Return the description as the ID (adjust as needed)
//        }
//        return null;
//    }
private void setupAdapters() {
    // Expense Adapter
    expenseAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, expenseList);
    groupExpenseListView.setAdapter(expenseAdapter);

    // Balance Adapter
    balanceAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, balanceList);
    balanceListView.setAdapter(balanceAdapter);

    // Group Spinner Adapter
    groupSpinnerAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, groupNames);
    groupSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    groupSpinner.setAdapter(groupSpinnerAdapter);

    // Handle group selection
    groupSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            String selectedGroupCode = groupCodes.get(position);
            loadGroupData(selectedGroupCode);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            // Do nothing
        }
    });
}
    private void loadUserGroups() {
        usersRef.child(currentUserId).child("groups").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                groupNames.clear();
                groupCodes.clear();

                if (!snapshot.exists()) {
                     //Log.d("GroupExpenseFragment", "No groups found for user");
                    Toast.makeText(getContext(), "No groups found", Toast.LENGTH_SHORT).show();
                    return;
                }

                for (DataSnapshot groupSnap : snapshot.getChildren()) {
                    String groupCode = groupSnap.getKey();
                    if (groupCode != null) {
                        Log.d("GroupExpenseFragment", "Found group code: " + groupCode);
                        groupsRef.child(groupCode).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                String groupName = snapshot.getValue(String.class);
                                if (groupName != null) {
                                    Log.d("GroupExpenseFragment", "Found group name: " + groupName);
                                    groupNames.add(groupName);
                                    groupCodes.add(groupCode);
                                    groupSpinnerAdapter.notifyDataSetChanged();
                                } else {
                                    Log.d("GroupExpenseFragment", "Group name is null for code: " + groupCode);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.e("GroupExpenseFragment", "Failed to load group name: " + error.getMessage());
                                Toast.makeText(getContext(), "Failed to load group name", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("GroupExpenseFragment", "Failed to load groups: " + error.getMessage());
                Toast.makeText(getContext(), "Failed to load groups", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void handleGroupActions() {
        createGroupFab.setOnClickListener(v -> showCreateGroupDialog());
        joinGroupFab.setOnClickListener(v -> showJoinGroupDialog());
        addGroupExpenseFab.setOnClickListener(v -> showAddExpenseDialog());
    }

    private void showCreateGroupDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Create Group");

        final EditText input = new EditText(requireContext());
        builder.setView(input);

        builder.setPositiveButton("Create", (dialog, which) -> {
            String groupName = input.getText().toString().trim();
            if (groupName.isEmpty()) {
                Toast.makeText(getContext(), "Group name cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            showProgressDialog("Creating group...");
            String groupCode = generateGroupCode();
            //Group newGroup = new Group(groupCode, groupName, currentUserId);
            Group newGroup = new Group();
            newGroup.setCode(groupCode);
            newGroup.setName(groupName);  // Explicitly set name
            newGroup.setCreator(currentUserId);

            groupsRef.child(groupCode).setValue(newGroup)
                    .addOnSuccessListener(aVoid -> {
                        hideProgressDialog();
                        joinGroup(groupCode);
                        Toast.makeText(getContext(), "Group created: " + groupCode, Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        hideProgressDialog();
                        Toast.makeText(getContext(), "Failed to create group: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void showJoinGroupDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Join Group");
        final EditText input = new EditText(requireContext());
        builder.setView(input);

        builder.setPositiveButton("Join", (dialog, which) -> {
            String code = input.getText().toString().trim().toUpperCase();
            if (code.length() == 6) {
                validateAndJoinGroup(code);
            } else {
                Toast.makeText(getContext(), "Group code must be 6 characters", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void validateAndJoinGroup(String code) {
       // showProgressDialog("Joining group...");
        groupsRef.child(code).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    joinGroup(code);
                } else {
                    hideProgressDialog();
                    Toast.makeText(getContext(), "Invalid group code", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                hideProgressDialog();
                Toast.makeText(getContext(), "Failed to join group", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void joinGroup(String groupCode) {
        showProgressDialog("Joining group...");

        // Add user to group members
        groupsRef.child(groupCode).child("members").child(currentUserId).setValue(true)
                .addOnSuccessListener(aVoid -> {
                    // Add group to user's groups
                    usersRef.child(currentUserId).child("groups").child(groupCode).setValue(true)
                            .addOnSuccessListener(aVoid1 -> {
                                hideProgressDialog();
                                loadUserGroups(); // Reload groups after joining
                                Toast.makeText(getContext(), "Joined group successfully", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                hideProgressDialog();
                                Toast.makeText(getContext(), "Failed to add group to user's groups", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    hideProgressDialog();
                    Toast.makeText(getContext(), "Failed to add user to group members", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadGroupData(String groupCode) {
        if (groupCode == null) return;

        // Load expenses
        groupsRef.child(groupCode).child("expenses").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                expenseList.clear();
                expenseIds.clear(); // Clear the expense IDs list
                for (DataSnapshot expenseSnap : snapshot.getChildren()) {
                    Expense expense = expenseSnap.getValue(Expense.class);
                    if (expense != null) {
                        expenseList.add(expense.getDescription() + " - $" + expense.getAmount()); // Add description and amount
                        expenseIds.add(expenseSnap.getKey()); // Add expense ID
                    }
                }
                expenseAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load expenses", Toast.LENGTH_SHORT).show();
            }
        });

        // Load balances (unchanged)
        usersRef.child(currentUserId).child("balances").child(groupCode).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                balanceList.clear();
                Balance balance = snapshot.getValue(Balance.class);
                if (balance != null) {
                    balanceList.add("You owe: $" + balance.getOwe());
                    balanceList.add("You are owed: $" + balance.getOwed());
                    balanceAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load balances", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void showAddExpenseDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Add Group Expense");
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_expense, null);

        EditText descriptionInput = view.findViewById(R.id.descriptionInput);
        EditText amountInput = view.findViewById(R.id.amountInput);
        Spinner groupSpinner = view.findViewById(R.id.groupSpinner);
        View dateInputView = view.findViewById(R.id.dateInput);
        if (dateInputView != null && dateInputView.getParent() instanceof View) {
            ((View) dateInputView.getParent()).setVisibility(View.GONE);
        }
        View categoryChipGroup = view.findViewById(R.id.categoryChipGroup);
        if (categoryChipGroup != null) categoryChipGroup.setVisibility(View.GONE);
        View categoryLabel = view.findViewById(R.id.categoryLabel);
        if (categoryLabel != null) categoryLabel.setVisibility(View.GONE);

        ArrayAdapter<String> dialogAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                groupNames
        );
        dialogAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        groupSpinner.setAdapter(dialogAdapter);


        builder.setView(view);
        builder.setPositiveButton("Add", (dialog, which) -> {
            String description = descriptionInput.getText().toString().trim();
            String amountStr = amountInput.getText().toString().trim();
            int selectedPosition = groupSpinner.getSelectedItemPosition();

            if (selectedPosition == AdapterView.INVALID_POSITION) {
                Toast.makeText(getContext(), "No group selected", Toast.LENGTH_SHORT).show();
                return;
            }

            String selectedGroupCode = groupCodes.get(selectedPosition);

            if (description.isEmpty() || amountStr.isEmpty()) {
                Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double amount = Double.parseDouble(amountStr);
                addGroupExpense(selectedGroupCode, description, amount);
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Invalid amount", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }
    private void addGroupExpense(String groupCode, String description, double amount) {
        if (groupCode == null) {
            Toast.makeText(getContext(), "No group selected", Toast.LENGTH_SHORT).show();
            return;
        }

        showProgressDialog("Adding expense...");

        // Get group members
        groupsRef.child(groupCode).child("members").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int totalMembers = (int) snapshot.getChildrenCount();
                if (totalMembers == 0) {
                    hideProgressDialog();
                    Toast.makeText(getContext(), "No members in group", Toast.LENGTH_SHORT).show();
                    return;
                }

                double perPerson = amount / totalMembers;
                String expenseId = groupsRef.child(groupCode).child("expenses").push().getKey();

                // Initialize splitMembers map
                Map<String, Double> splitMembers = new HashMap<>();
                for (DataSnapshot memberSnap : snapshot.getChildren()) {
                    String memberId = memberSnap.getKey();
                    splitMembers.put(memberId, perPerson);
                }

                // Create expense with ALL fields
                Expense expense = new Expense();
                expense.setId(expenseId);
                expense.setDescription(description);
                expense.setAmount(amount);
                expense.setPayer(currentUserId);
                expense.setPerPerson(perPerson);
                expense.setSplitMembers(splitMembers);
                expense.setTimestamp(System.currentTimeMillis());

                // Save to Firebase
                groupsRef.child(groupCode).child("expenses").child(expenseId).setValue(expense)
                        .addOnSuccessListener(aVoid -> {
                            hideProgressDialog();
                            updateMemberBalances(groupCode, perPerson, totalMembers);
                            Toast.makeText(getContext(), "Expense added", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            hideProgressDialog();
                            Toast.makeText(getContext(), "Failed to add expense", Toast.LENGTH_SHORT).show();
                        });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                hideProgressDialog();
                Toast.makeText(getContext(), "Failed to add expense", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void updateMemberBalances(String groupCode, double amountPerPerson, int totalMembers) {
        showProgressDialog("Updating balances...");

        groupsRef.child(groupCode).child("members").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot memberSnap : snapshot.getChildren()) {
                    String memberId = memberSnap.getKey();
                    if (memberId != null) {
                        double adjustment = memberId.equals(currentUserId) ?
                                -amountPerPerson * (totalMembers - 1) : // Payer owes less
                                amountPerPerson; // Others owe more
                        updateBalance(memberId, groupCode, adjustment);
                    }
                }
                hideProgressDialog();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                hideProgressDialog();
                Toast.makeText(getContext(), "Failed to update balances", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateBalance(String userId, String groupCode, double amount) {
        DatabaseReference balanceRef = usersRef.child(userId).child("balances").child(groupCode);
        balanceRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                Balance balance = mutableData.getValue(Balance.class);
                if (balance == null) {
                    balance = new Balance(0, 0);
                }

                if (amount > 0) {
                    balance.setOwed(balance.getOwed() + amount); // User is owed money
                } else {
                    balance.setOwe(balance.getOwe() + Math.abs(amount)); // User owes money
                }

                mutableData.setValue(balance);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                if (error != null) {
                    Toast.makeText(getContext(), "Balance update failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private String generateGroupCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder();
        Random rnd = new Random();
        while (code.length() < 6) {
            int index = (int) (rnd.nextFloat() * chars.length());
            code.append(chars.charAt(index));
        }
        return code.toString();
    }

    private void showProgressDialog(String message) {
        progressDialog = new ProgressDialog(requireContext());
        progressDialog.setMessage(message);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void hideProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}