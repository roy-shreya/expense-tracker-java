package com.expendituretracker;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class TestDatabaseConnection {

    public static void main(String[] args) {

        try (Connection connection = DatabaseConnection.createConnection()) {

            System.out.println("Connected successfully!");
            System.out.println(connection);
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM expenses");

            while (rs.next()) {
                System.out.println(rs.getString("category") + " - " + rs.getInt("amount"));
            }

        } catch (Exception e) {

            e.printStackTrace();

        }
    }
}