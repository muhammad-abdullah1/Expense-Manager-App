package com.example.expenseapp;

public class Balance {
    private double owed;
    private double owe;

    // Default constructor required for Firebase
    public Balance() {}

    public Balance(double owed, double owe) {
        this.owed = owed;
        this.owe = owe;
    }

    // Getters and Setters
    public double getOwed() { return owed; }
    public void setOwed(double owed) { this.owed = owed; }

    public double getOwe() { return owe; }
    public void setOwe(double owe) { this.owe = owe; }
}