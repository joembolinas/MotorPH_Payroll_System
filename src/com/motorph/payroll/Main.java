package com.motorph.payroll;


import java.io.IOException;
import java.util.List;

public class Main {
    private static final String CSV_URL = "https://docs.google.com/spreadsheets/d/e/2PACX-1vQXb2izvcFadSUJFaQaUwgFbiyWdzD0YcVmXGBt-KVfkWOt1y0pfuTRw9b3QHfS0FitdmgwKhKN1a0n/pub?output=csv";

    public static void main(String[] args) {
        try {
            List<Employee> employees = CsvReader.fetchEmployeesFromCsv(CSV_URL);
            new MenuManager(employees).displayMainMenu();
        } catch (IOException e) {
            System.err.println("Fatal Error: " + e.getMessage());
            System.err.println("Please check the CSV URL and internet connection.");
            System.exit(1);
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            System.exit(2);
        }
    }
}