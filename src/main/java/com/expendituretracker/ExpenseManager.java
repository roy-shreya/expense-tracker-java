package com.expendituretracker;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

public class ExpenseManager {
    static ArrayList<Expense> expenses = new ArrayList<>();
    static Scanner sc = new Scanner(System.in);

    public static void showMenu() throws IOException {
            int option = 100;
            Menu[] menuItems = Menu.values();
            Menu menuItemSelected;

            while(option != menuItems.length) {
                for (int i = 0; i < menuItems.length; i++) {
                    System.out.printf("%d. %s%n", i + 1, menuItems[i]);
                }
                while (true) {
                    System.out.print("Enter the number: ");
                    try {
                        option = sc.nextInt();
                        if (option >= 1 && option <= menuItems.length) {
                            menuItemSelected = menuItems[option - 1];
                            System.out.println("Option selected: " + option);
                            System.out.println("Selected: " + menuItemSelected);
                            break;
                        } else {
                            System.out.println("Invalid Menu Option.");
                        }
                    } catch (Exception e) {
                        System.out.println("The input should be a number");
                        sc.nextLine();
                    }
                }

                switch (menuItemSelected) {
                    case ADD_EXPENSE -> addExpense();
                    case VIEW_EXPENSES -> viewExpenses();
                    case TOTAL_EXPENSES -> totalExpenses();
                    case DELETE_EXPENSE -> deleteExpenses();
                    case EDIT_EXPENSE -> editExpense();
                    case MONTHLY_SUMMARY -> monthlySummary();
                    case SEARCH_EXPENSE -> searchExpense();
                    case EXIT -> {
                        System.out.println("Thank you for using the tool!");
                        sc.close();
                    }
                }
            }
    }

    public static void loadExpenses() throws IOException {
        File file = new File("expenses.txt");

        if (!file.exists()) {
            System.out.println("File not found....\n");
            return;
        }

        Scanner reader = new Scanner(file);
        while(reader.hasNextLine()) {
            String line = reader.nextLine();
            String[] parts = line.split(",");
            Expense ex = new Expense(
                    Category.valueOf(parts[0]),
                    Double.parseDouble(parts[1]),
                    parts[2],
                    LocalDate.parse(parts[3])
            );
            expenses.add(ex);
        }
        System.out.println("Load Successful....\n");
        reader.close();
    }

    public static void saveExpenses() throws IOException {
        FileWriter writer = new FileWriter("expenses.txt");

        for (Expense ex : expenses) {
            writer.write(ex.getCategory().name() + "," + ex.getAmount() + "," + ex.getNote() +  "," + ex.getDate() + "\n");
        }
        writer.close();
    }

    public static void addExpense() throws IOException {
        String addMore = "Y";
        double amount;
        Category category;
        String note;

        while (addMore.equalsIgnoreCase("Yes") || addMore.equalsIgnoreCase("Y")){
            category = getCategoryInput();
            amount = getAmountInput();
            note = getNoteInput();

            try (Connection connection = DatabaseConnection.createConnection()) {

                System.out.println("Connected successfully!");
                System.out.println(connection);
                String sql = "INSERT INTO expenses(category, amount, expense_date, notes) VALUES (?, ?, ?, ?)";

                PreparedStatement ps = connection.prepareStatement(sql);

                ps.setString(1, category.name());
                ps.setDouble(2, amount);
                ps.setDate(3, Date.valueOf(LocalDate.now()));
                ps.setString(4, note);

                int rows = ps.executeUpdate();
                if(rows > 0){
                    System.out.println("Expense inserted.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            System.out.println("Do you want to enter another expense?(Y/N)");
            addMore = sc.next();
        }
        System.out.println("Returning to main menu....\n");
    }

    public static void viewExpenses() {
        try (Connection connection = DatabaseConnection.createConnection()) {

            System.out.println("Connected successfully!");
            System.out.println(connection);
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM expenses");

            if (!rs.next()) {
                System.out.println("There are no expenses...returning to menu");
                return;
            }
            else {
                System.out.print("Do you want to sort?(Y/Yes for Yes): ");
                String sortIp = sc.next();
                if (sortIp.equalsIgnoreCase("Y") || sortIp.equalsIgnoreCase("Yes")) {
                    System.out.println("1. Sort by Category");
                    System.out.println("2. Sort by Amount");
                    System.out.println("3. Sort by Date");
                    while (true) {
                        try {
                            System.out.print("Enter a option: ");
                            int sortOption = sc.nextInt();
                            if (sortOption >= 1 && sortOption <= 3) {
                                System.out.printf("No. %-15s : %-11s : %-10s : %s\n", "Category", "Amount", "Date", "Note");
                                switch (sortOption) {
                                    case 1 -> rs = st.executeQuery("SELECT * FROM expenses ORDER BY category");
                                    case 2 -> rs = st.executeQuery("SELECT * FROM expenses ORDER BY amount");
                                    case 3 -> rs = st.executeQuery("SELECT * FROM expenses ORDER BY expense_date");
                                    default -> System.out.println("Invalid option.");
                                }
                                break;
                            }
                            else {
                                System.out.println("Invalid Menu Option.");
                            }
                        } catch (InputMismatchException e) {
                            System.out.println("Invalid Input.");
                            sc.next();
                        }
                    }


                    System.out.printf("No. %-15s : %-11s : %-10s : %s\n", "Category", "Amount", "Date", "Note");
                    while (rs.next()) {
                        System.out.printf("%-2s. %-15s : ₹%10.2f : %s : %s\n",
                                rs.getInt("expense_id"),
                                rs.getString("category"),
                                rs.getDouble("amount"),
                                rs.getDate("expense_date"),
                                rs.getString("notes")
                        );
                    }
                }
                else {
                    System.out.printf("No. %-15s : %-11s : %-10s : %s\n", "Category", "Amount", "Date", "Note");
                    do {
                        System.out.printf("%2s. %-15s : ₹%10.2f : %s : %s\n",
                                rs.getInt("expense_id"),
                                rs.getString("category"),
                                rs.getDouble("amount"),
                                rs.getDate("expense_date"),
                                rs.getString("notes")
                        );
                    } while (rs.next());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Returning to previous menu....\n");
    }

    public static void totalExpenses(){
        double total = 0.0;
        HashMap<Category, Double> categoryTotals = new HashMap<>();
        for (Expense ex : expenses) {
            total += ex.getAmount();
            categoryTotals.put(ex.getCategory(),
                    categoryTotals.getOrDefault(ex.getCategory(),0.0) + ex.getAmount()
            );
        }

        System.out.println("Total Expenses: ₹" + total);
        System.out.println("\nCategory Wise Totals:");
        for (Category category : categoryTotals.keySet()) {
            System.out.println(category + " : ₹" + categoryTotals.get(category));
        }
    }

    public static void deleteExpenses() {
        if (expenses.isEmpty()) {
            System.out.println("No expenses to delete.");
            return;
        }

        String ip;
        int exNo;
        while (true) {
            System.out.print("Enter the expense number to be delete: ");
            ip = sc.next();
            try {
                exNo = Integer.parseInt(ip);
                if (exNo > 0 && exNo <= expenses.size()) {
                    expenses.remove(exNo - 1);
                    System.out.println("Expense deleted...");
                    saveExpenses();
                    break;
                } else {
                    System.out.println("Invalid expense number.");
                }
            }
            catch (Exception e){
                System.out.println("Input should be a number. Try again ");
            }
        }
        System.out.println("Returning to main menu! ");
    }

    public static void editExpense() throws IOException {
        if (expenses.isEmpty()) {
            System.out.println("No expenses to edit.");
            return;
        }
        viewExpenses();
        int exNo;
        while (true) {
            System.out.print("Enter expense number to edit: ");
            try {
                exNo = sc.nextInt();
                if (exNo > 0 && exNo <= expenses.size()) {
                    Expense ex = expenses.get(exNo - 1);
                    ex.setAmount(getAmountInput());
                    ex.setNote(getNoteInput());
                    System.out.println("Expense updated");
                    saveExpenses();
                    break;
                }
                System.out.println("Invalid expense number. Try again.");
            }
            catch (InputMismatchException e) {
                System.out.println("Please enter a valid number. Try again.");
                sc.nextLine();
            }
        }
        System.out.println("Returning to previous menu...");
    }

    public static void monthlySummary() {
        int monthNo;
        String ip;
        while (true) {
            System.out.print("Enter month number (1-12): ");
            ip = sc.next();
            try {
                monthNo = Integer.parseInt(ip);
                if (monthNo >= 1 && monthNo <= 12) {
                    break;
                }
                System.out.println("Invalid month number.");
            }
            catch (Exception e){
                System.out.println("Month should be in numbers.");
            }
        }
        double total = 0;
        HashMap<Category, Double> categoryTotals = new HashMap<>();

        for (Expense ex : expenses) {
            if (ex.getDate().getMonthValue() == monthNo) {
                total += ex.getAmount();
                categoryTotals.put(
                        ex.getCategory(),
                        categoryTotals.getOrDefault(ex.getCategory(),0.0) + ex.getAmount()
                );
            }
        }

        System.out.println("\nMonthly Summary");
        for (Category category : categoryTotals.keySet()) {
            System.out.println(category + " : ₹" +categoryTotals.get(category)
            );
        }
        System.out.println("Total: ₹" + total + "\n");
    }

    public static void searchExpense() {
        if (expenses.isEmpty()) {
            System.out.println("There are no expenses...returning to menu");
            return;
        }
        int found = 0;
        Category catInput = getCategoryInput();
        System.out.println(catInput);
        for(int i = 0; i < expenses.size(); i++){
            Expense ex = expenses.get(i);
            Category cat = ex.getCategory();
            if (cat == catInput) {
                if (found == 0 ) {
                    System.out.printf("No. %-15s : %-11s : %-10s : %s\n", "Category", "Amount", "Date", "Note");
                    found ++;
                }
                System.out.println((i + 1) + ".  " + ex);
            }
        }
        if (found == 0) {
            System.out.println("No expense found for " + catInput + "....\n");
        }
        System.out.println("Returning to previous menu....\n");
    }

    // Category Input From Enum
    public static Category getCategoryInput() {
        Category[] categories = Category.values();

        for (int i = 0; i < categories.length; i++) {
            System.out.printf("%d. %s%n", i + 1, categories[i]);
        }

        while (true) {
            try {
                System.out.print("Enter category number: ");
                int choice = sc.nextInt();

                if (choice >= 1 && choice <= categories.length) {
                    return categories[choice - 1];
                }

                System.out.println("Invalid category.");
            }
            catch (InputMismatchException e) {
                System.out.println("Please enter a number.");
                sc.nextLine();
            }
        }
    }

    // Amount Input
    public static double getAmountInput() {
        while (true) {
            try {
                System.out.print("Enter Amount(₹): ");
                double amount = sc.nextDouble();
                if (amount > 0) {
                    sc.nextLine();
                    return amount;
                }

                System.out.println("Amount must be positive.");
            }
            catch (InputMismatchException e) {
                System.out.println("Please enter a valid number.");
                sc.nextLine();
            }
        }
    }

    // Note Input
    public static String getNoteInput() {
        System.out.print("Note (optional): ");
        return sc.nextLine().trim();
    }
}