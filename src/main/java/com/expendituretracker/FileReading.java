package com.expendituretracker;

import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;

public class FileReading {
    public static void main(String[] args) throws Exception {

        File file = new File("expenses.txt");

        Scanner reader = new Scanner(file);
        while(reader.hasNextLine()) {
            System.out.println(reader.nextLine());
        }
        reader.close();


        FileWriter writer = new FileWriter("expenses.txt");
        writer.write("Food,250,Lunch");
        writer.close();
    }
}