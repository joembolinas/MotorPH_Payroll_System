package com.motorph.payroll;

import java.io.IOException;
import java.util.List;

public class Main {
    private static final String EMPLOYEE_CSV_URL = "https://docs.google.com/spreadsheets/d/e/2PACX-1vRe4-w2yYtOZpBxFZGP1UZqyKWk053QkCmVxwq9Hiu2LfHU2nVIvCkTTg8rtWQsP-sp31jG6OleREqM/pub?output=csv";
    private static final String ATTENDANCE_CSV_URL = "https://docs.google.com/spreadsheets/d/e/2PACX-1vTqBrLETQHcACenfV0_VSgV_uEGH5Cne2Vuw-oN2yDGRH5wWS8x8CcAXAV8iSNugtwWB_oVCuOlcFYT/pub?output=csv";

    public static void main(String[] args) {
        try {
            // Fetch employee data
            List<Employee> employees = CsvReader.fetchEmployeesFromCsv(EMPLOYEE_CSV_URL);

            // Initialize attendance tracker and load attendance data
            AttendanceTracker attendanceTracker = new AttendanceTracker();
            CsvReader.fetchAttendanceData(ATTENDANCE_CSV_URL, attendanceTracker);

            // Start the main menu
            MenuManager menuManager = new MenuManager(employees, attendanceTracker);
            menuManager.displayMainMenu(); // Ensure this method is called correctly
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