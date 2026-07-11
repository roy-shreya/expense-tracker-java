package com.expendituretracker;

public enum Menu {
    ADD_EXPENSE("Add Expenses"),
    VIEW_EXPENSES("View Expenses"),
    TOTAL_EXPENSES("Total Expenses"),
    DELETE_EXPENSE("Delete Expense"),
    EDIT_EXPENSE("Edit Expense"),
    MONTHLY_SUMMARY("Monthly Summary"),
    SEARCH_EXPENSE("Search Expense"),
    EXIT("Exit");

    private final String displayName;

    Menu(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString(){
        return displayName;
    }
}
