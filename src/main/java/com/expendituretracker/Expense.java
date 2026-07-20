package com.expendituretracker;

import com.expendituretracker.Category;
import java.time.LocalDate;

public class Expense {
    private Category category;
    private double amount;
    private String note;
    private final LocalDate date;

    Expense (Category category, double amount) {
        setCategory(category);
        setAmount(amount);
        setNote("None");
        this.date = LocalDate.now();
    }

    Expense (Category category, double amount, String note) {
        setCategory(category);
        setAmount(amount);
        setNote(note);
        this.date = LocalDate.now();
    }

    Expense (Category category, double amount, String note, LocalDate date) {
        setCategory(category);
        setAmount(amount);
        setNote(note);
        this.date = date;
    }

    public Category getCategory() { return category; }

    public double getAmount() {
        return amount;
    }

    public String getNote() {
        return note;
    }

    public LocalDate getDate() { return date; }

    public void setCategory(Category category) {
        if (category == null) {
            throw new IllegalArgumentException("Invalid category");
        }
        this.category = category;
    }

    public void setAmount(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        this.amount = amount;
    }

    public void setNote(String note) {
        if (note == null) {
            this.note = "None";
        } else if (note.length() <= 100) {
            note = note.replace(",", " ");
            this.note = note;
        } else {
            throw new IllegalArgumentException("Note too long.");
        }
    }

    @Override
    public String toString() {
        return String.format(
                "%-15s : ₹%10.2f : %s : %s",
                category,
                amount,
                date,
                note
        );
    }
}
