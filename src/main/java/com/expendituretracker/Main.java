package com.expendituretracker;

import java.io.IOException;
public class Main {

    public static void main(String[] args) throws IOException {
        ExpenseManager.loadExpenses();
        System.out.println("Hello and welcome!");
        ExpenseManager.showMenu();
    }
}