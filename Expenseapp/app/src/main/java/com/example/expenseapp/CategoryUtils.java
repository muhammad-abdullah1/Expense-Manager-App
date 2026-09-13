package com.example.expenseapp;

import android.content.Context;
import androidx.core.content.ContextCompat;

/**
 * Central place that maps an expense category name to the icon and color
 * used to represent it throughout the app (list rows, chips, etc).
 */
public class CategoryUtils {

    public static final String[] ALL_CATEGORIES = {
            "Food", "Transport", "Shopping", "Bills", "Entertainment", "Other"
    };

    public static int getIconRes(String category) {
        if (category == null) return R.drawable.ic_cat_other;
        switch (category) {
            case "Food": return R.drawable.ic_cat_food;
            case "Transport": return R.drawable.ic_cat_transport;
            case "Shopping": return R.drawable.ic_cat_shopping;
            case "Bills": return R.drawable.ic_cat_bills;
            case "Entertainment": return R.drawable.ic_cat_entertainment;
            default: return R.drawable.ic_cat_other;
        }
    }

    public static int getColorRes(String category) {
        if (category == null) return R.color.cat_other;
        switch (category) {
            case "Food": return R.color.cat_food;
            case "Transport": return R.color.cat_transport;
            case "Shopping": return R.color.cat_shopping;
            case "Bills": return R.color.cat_bills;
            case "Entertainment": return R.color.cat_entertainment;
            default: return R.color.cat_other;
        }
    }

    public static int getColor(Context context, String category) {
        return ContextCompat.getColor(context, getColorRes(category));
    }
}
