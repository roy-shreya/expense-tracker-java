package com.expendituretracker;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

import static java.util.function.Predicate.not;

public class ExpenseManager {
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
            ResultSet rs = st.executeQuery("SELECT * FROM expenses ORDER BY expense_id");

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

        try (Connection connection = DatabaseConnection.createConnection()) {

            System.out.println("Connected successfully!");
            System.out.println(connection);
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery("SELECT SUM(amount) AS total FROM expenses");
            if (rs.next()) {
                double total = rs.getDouble("total");
                System.out.println("Total Expenses: ₹" + total);
            }
            System.out.println("\nCategory Wise Totals:");
            rs = st.executeQuery("SELECT SUM(amount) as total, category FROM expenses GROUP BY category");
            while(rs.next()){
                System.out.println(rs.getString("category") + " : ₹" + rs.getDouble("total"));
            }
        } catch  (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Returning to previous menu....\n");
    }

    public static void deleteExpenses() {
        try (Connection connection = DatabaseConnection.createConnection()) {

            System.out.println("Connected successfully!");
            System.out.println(connection);
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM expenses");

            if (!rs.next()) {
                System.out.println("There are no expenses to delete...returning to menu");
                return;
            }

            int exNo;
            while (true) {
                System.out.print("Enter the expense id to be delete: ");
                try {
                    exNo = sc.nextInt();
                    if (exNo > 0 ) {
                        String sql = "DELETE FROM expenses WHERE expense_id = ?";
                        PreparedStatement ps = connection.prepareStatement(sql);
                        ps.setInt(1, exNo);
                        int rows = ps.executeUpdate();
                        if(rows > 0) System.out.println("Expense deleted.");
                        else System.out.println("Expense id not found.");
                        break;
                    } else {
                        System.out.println("Invalid expense number.");
                    }
                }
                catch (Exception e){
                    System.out.println("Input should be a number. Try again ");
                    sc.nextLine();
                }
            }
        } catch  (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Returning to main menu! ");
    }

    public static void editExpense() throws IOException {
        try (Connection connection = DatabaseConnection.createConnection()) {

            System.out.println("Connected successfully!");
            System.out.println(connection);
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM expenses");

            if (!rs.next()) {
                System.out.println("There are no expenses to edit...returning to menu");
                return;
            }

            int exNo;
            while (true) {
                System.out.print("Enter expense number to edit: ");
                try {
                    exNo = sc.nextInt();
                    if (exNo > 0) {
                        Category category = getCategoryInput();
                        double amount = getAmountInput();
                        String note = getNoteInput();
                        String sql = "UPDATE expenses SET category = ?, amount = ?, expense_date = ?, notes = ? WHERE expense_id = ?";
                        PreparedStatement ps = connection.prepareStatement(sql);
                        ps.setString(1, category.name());
                        ps.setDouble(2, amount);
                        ps.setDate(3, Date.valueOf(LocalDate.now()));
                        ps.setString(4, note);
                        ps.setInt(5, exNo);
                        int rows = ps.executeUpdate();
                        if(rows > 0) System.out.println("Expense updated");
                        else System.out.println("Expense id not found.");
                        break;
                    }
                    System.out.println("Invalid expense number. Try again.");
                }
                catch (InputMismatchException e) {
                    System.out.println("Please enter a valid number. Try again.");
                    sc.nextLine();
                }
            }
        } catch  (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Returning to previous menu...");
    }

    public static void monthlySummary() {

        double total = 0;

        try (Connection connection = DatabaseConnection.createConnection()) {

            System.out.println("Connected successfully!");
            System.out.println(connection);
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery("SELECT COUNT(*) AS total_count FROM expenses");

            if (rs.next() && rs.getInt("total_count") == 0) {
                System.out.println("There are no expenses...returning to menu");
                return;
            }

            int monthNo;
            while (true) {
                System.out.print("Enter month number (1-12): ");
                try {
                    monthNo = sc.nextInt();
                    if (monthNo >= 1 && monthNo <= 12) {
                        break;
                    }
                    System.out.println("Invalid month number.");
                }
                catch (Exception e){
                    System.out.println("Month should be in numbers.");
                    sc.nextLine();
                }
            }

            String sql = """
                    SELECT
                        TO_CHAR(expense_date, 'Month YYYY') AS month,
                        category, 
                        SUM(amount) AS total_expenses
                    FROM expenses
                    WHERE EXTRACT(MONTH FROM expense_date) = ?
                    GROUP BY 
                        DATE_TRUNC('month', expense_date), 
                        TO_CHAR(expense_date, 'Month YYYY'), 
                        category 
                    ORDER BY category;
                    """;

            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, monthNo);
            rs = ps.executeQuery();

            System.out.println("\nMonthly Summary");
            while (rs.next()) {
                total += rs.getDouble("total_expenses");
                System.out.println(rs.getString("category") + " : ₹" +rs.getDouble("total_expenses"));
            }

            System.out.println("\nTotal: ₹" + total + "\n");

        } catch  (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Returning to main menu! ");
    }

    public static void searchExpense() {
        try (Connection connection = DatabaseConnection.createConnection()) {

            System.out.println("Connected successfully!");
            System.out.println(connection);
            Category catInput = getCategoryInput();
            System.out.println(catInput);

            String sql = "SELECT * FROM expenses where category = ?;";
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, catInput.name());
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                System.out.println("No expense found for " + catInput + "....\n");
                return;
            }

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

        } catch  (Exception e) {
            e.printStackTrace();
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