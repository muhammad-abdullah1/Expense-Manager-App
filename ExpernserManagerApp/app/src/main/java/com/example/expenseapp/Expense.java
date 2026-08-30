package com.example.expenseapp;

import java.util.HashMap;
import java.util.Map;

public class Expense {
    private String id;
    private String description;
    private double amount;
    private String date;
    private String payer;
    private double perPerson;
    private Map<String, Double> splitMembers;
    private long timestamp;

//
private String title;
    // Constructors, getters, setters...

    // Default constructor required for Firebase
    public Expense() {}

    // Constructor for personal expenses
    public Expense(String id, String description, double amount,String date) {
        this.id = id;
        this.description = description;
        this.amount = amount;
        this.date=date;

    }

    // Constructor for group expenses
    public Expense(String id, String description, double amount, String payer, double perPerson) {
        this.id = id;
        this.description = description;
        this.amount = amount;
        this.payer = payer;
        this.perPerson = perPerson;
        this.timestamp = System.currentTimeMillis();
        this.splitMembers = new HashMap<>();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String toString() {
        return title + ": $" + amount;  // Customize how it appears in the list
    }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getPayer() { return payer; }
    public void setPayer(String payer) { this.payer = payer; }

    public double getPerPerson() { return perPerson; }
    public void setPerPerson(double perPerson) { this.perPerson = perPerson; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public Map<String, Double> getSplitMembers() { return splitMembers; }
    public void setSplitMembers(Map<String, Double> splitMembers) { this.splitMembers = splitMembers; }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}