package com.motorph.original;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MotorPHPayroll {
    // Constants
    private static final double OVERTIME_RATE = 1.25;
    private static final double REGULAR_HOURS_PER_WEEK = 40;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        List<String[]> employees = new ArrayList<>();
        List<String[]> attendanceRecords = new ArrayList<>();

        // Load employee and attendance data
        try {
            employees = loadEmployeesFromCSV("https://docs.google.com/spreadsheets/d/e/2PACX-1vQXb2izvcFadSUJFaQaUwgFbiyWdzD0YcVmXGBt-KVfkWOt1y0pfuTRw9b3QHfS0FitdmgwKhKN1a0n/pub?gid=436645740&single=true&output=csv");
            attendanceRecords = loadAttendanceFromCSV("https://docs.google.com/spreadsheets/d/e/2PACX-1vQXb2izvcFadSUJFaQaUwgFbiyWdzD0YcVmXGBt-KVfkWOt1y0pfuTRw9b3QHfS0FitdmgwKhKN1a0n/pub?gid=1494987339&single=true&output=csv");
        } catch (IOException e) {
            System.err.println("Error loading data: " + e.getMessage());
            System.exit(1);
        }

        // Main menu
        while (true) {
            System.out.println("\n=== MotorPH Payroll System ===");
            System.out.println("1. Employee Management");
            System.out.println("2. Payroll Management");
            System.out.println("3. Exit");
            System.out.print("Enter your choice: ");

            int choice = Integer.parseInt(scanner.nextLine());
            switch (choice) {
                case 1 -> employeeManagement(employees, scanner);
                case 2 -> payrollManagement(employees, attendanceRecords, scanner);
                case 3 -> {
                    System.out.println("Exiting system...");
                    scanner.close();
                    System.exit(0);
                }
                default -> System.out.println("Invalid choice. Please enter 1-3.");
            }
        }
    }

    // Employee Management
    private static void employeeManagement(List<String[]> employees, Scanner scanner) {
        while (true) {
            System.out.println("\nEmployee Management:");
            System.out.println("1. Search Employee");
            System.out.println("2. List All Employees");
            System.out.println("3. Return to Main Menu");
            System.out.print("Enter your choice: ");

            int choice = Integer.parseInt(scanner.nextLine());
            switch (choice) {
                case 1 -> searchEmployee(employees, scanner);
                case 2 -> listAllEmployees(employees);
                case 3 -> { return; }
                default -> System.out.println("Invalid choice. Please enter 1-3.");
            }
        }
    }

    // Search Employee
    private static void searchEmployee(List<String[]> employees, Scanner scanner) {
        System.out.print("\nEnter search term (ID or name): ");
        String searchTerm = scanner.nextLine().trim().toLowerCase();

        List<String[]> results = new ArrayList<>();
        for (String[] employee : employees) {
            if (employee[0].toLowerCase().contains(searchTerm) || employee[1].toLowerCase().contains(searchTerm)) {
                results.add(employee);
            }
        }

        if (results.isEmpty()) {
            System.out.println("No matching employees found.");
            return;
        }

        System.out.println("\nSearch Results:");
        for (String[] employee : results) {
            System.out.printf("%s | %s | %s%n", employee[0], employee[1], employee[2]);
        }
    }

    // List All Employees
    private static void listAllEmployees(List<String[]> employees) {
        System.out.println("\nAll Employees:");
        System.out.printf("%-8s %-25s %-15s%n", "ID", "Name", "Position");
        for (String[] employee : employees) {
            System.out.printf("%-8s %-25s %-15s%n", employee[0], employee[1], employee[2]);
        }
    }

    // Payroll Management
    private static void payrollManagement(List<String[]> employees, List<String[]> attendanceRecords, Scanner scanner) {
        System.out.println("\nPayroll Management:");
        System.out.print("Enter employee number: ");
        int empNumber = Integer.parseInt(scanner.nextLine());

        // Find employee
        String[] employee = findEmployeeById(employees, empNumber);
        if (employee == null) {
            System.out.println("Employee not found.");
            return;
        }

        // Calculate hours worked
        double hoursWorked = calculateHoursWorked(attendanceRecords, empNumber);
        if (hoursWorked <= 0) {
            System.out.println("No valid attendance record found.");
            return;
        }

        // Clean and parse hourly rate
        String hourlyRateStr = employee[18].replaceAll("[^\\d.]", ""); // Assuming hourly rate is at index 18
        double hourlyRate = Double.parseDouble(hourlyRateStr);

        // Calculate gross salary
        double grossSalary = calculateGrossSalary(hoursWorked, hourlyRate);

        // Display results
        System.out.printf("\nGross Salary Calculation for %s:%n", employee[1]);
        System.out.printf("Hours Worked: %.2f hrs%n", hoursWorked);
        System.out.printf("Hourly Rate: PHP %.2f%n", hourlyRate);
        System.out.printf("Gross Salary: PHP %.2f%n", grossSalary);
    }

    // Calculate Gross Salary
    private static double calculateGrossSalary(double hoursWorked, double hourlyRate) {
        if (hoursWorked <= REGULAR_HOURS_PER_WEEK) {
            return hoursWorked * hourlyRate;
        } else {
            double regularPay = REGULAR_HOURS_PER_WEEK * hourlyRate;
            double overtimePay = (hoursWorked - REGULAR_HOURS_PER_WEEK) * hourlyRate * OVERTIME_RATE;
            return regularPay + overtimePay;
        }
    }

    // Calculate Hours Worked
    private static double calculateHoursWorked(List<String[]> attendanceRecords, int empNumber) {
        double totalHours = 0;
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("H:mm");

        for (String[] record : attendanceRecords) {
            if (Integer.parseInt(record[0]) == empNumber) {
                try {
                    LocalTime loginTime = LocalTime.parse(record[4], timeFormatter);
                    LocalTime logoutTime = LocalTime.parse(record[5], timeFormatter);
                    Duration duration = Duration.between(loginTime, logoutTime);
                    totalHours += duration.toMinutes() / 60.0;
                } catch (DateTimeParseException e) {
                    System.err.println("Error parsing time for employee " + empNumber + ": " + e.getMessage());
                }
            }
        }
        return totalHours;
    }

    // Find Employee by ID
    private static String[] findEmployeeById(List<String[]> employees, int empNumber) {
        for (String[] employee : employees) {
            if (Integer.parseInt(employee[0]) == empNumber) {
                return employee;
            }
        }
        return null;
    }

    // Load Employees from CSV
    private static List<String[]> loadEmployeesFromCSV(String url) throws IOException {
        List<String[]> employees = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new URL(url).openStream()))) {
            br.readLine(); // Skip header
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                employees.add(data);
            }
        }
        return employees;
    }

    // Load Attendance from CSV
    private static List<String[]> loadAttendanceFromCSV(String url) throws IOException {
        List<String[]> attendanceRecords = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new URL(url).openStream()))) {
            br.readLine(); // Skip header
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                attendanceRecords.add(data);
            }
        }
        return attendanceRecords;
    }
}