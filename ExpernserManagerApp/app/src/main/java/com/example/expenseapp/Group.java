package com.example.expenseapp;

import java.util.HashMap;
import java.util.Map;

public class Group {
    private String code;
    private String name;
    private String creator;
    private Map<String, Boolean> members = new HashMap<>();
    private Map<String, Expense> expenses = new HashMap<>();

    // Default constructor required for Firebase
    public Group() {}

    public Group(String code, String name, String creator) {
        this.code = code;
        this.name = name;
        this.creator = creator;
    }
    // Getters and Setters
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getCreator() { return creator; }
    public void setCreator(String creator) { this.creator = creator; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Map<String, Boolean> getMembers() { return members; }
    public void setMembers(Map<String, Boolean> members) { this.members = members; }

    public Map<String, Expense> getExpenses() { return expenses; }
    public void setExpenses(Map<String, Expense> expenses) { this.expenses = expenses; }
}