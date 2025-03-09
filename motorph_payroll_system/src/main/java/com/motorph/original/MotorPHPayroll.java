package com.motorph.original;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MotorPHPayroll {

    // Constants
    private static final double OVERTIME_RATE = 1.25;
    private static final int REGULAR_HOURS_PER_WEEK = 40;

    public static void main(String[] args) {
        List<String[]> employees = new ArrayList<>();
        List<String[]> attendanceRecords = new ArrayList<>();

        try {
            // Load employee and attendance data from CSV files
            employees = loadEmployeesFromCSV(
                    "https://docs.google.com/spreadsheets/d/e/2PACX-1vQXb2izvcFadSUJFaQaUwgFbiyWdzD0YcVmXGBt-KVfkWOt1y0pfuTRw9b3QHfS0FitdmgwKhKN1a0n/pub?gid=436645740&single=true&output=csv");
            attendanceRecords = loadAttendanceFromCSV(
                    "https://docs.google.com/spreadsheets/d/e/2PACX-1vQXb2izvcFadSUJFaQaUwgFbiyWdzD0YcVmXGBt-KVfkWOt1y0pfuTRw9b3QHfS0FitdmgwKhKN1a0n/pub?gid=1494987339&single=true&output=csv");

        } catch (IOException e) {
            System.err.println("Error loading data: " + e.getMessage());
            System.exit(1);
        }

        // Main menu loop
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("\n=== MotorPH Payroll System ===");
            System.out.println("1. Employee Management");
            System.out.println("2. Payroll Management");
            System.out.println("3. Exit");
            System.out.print("Enter your choice: ");

            try {
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
            } catch (NumberFormatException e) {
                System.err.println("Invalid input. Please enter a number.");
            }
        }
    }

    // Load Employees from CSV
    private static List<String[]> loadEmployeesFromCSV(String url) throws IOException {
        List<String[]> employees = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new URL(url).openStream()))) {
            String line;
            boolean isHeaderSkipped = false;

            while ((line = br.readLine()) != null) {
                if (!isHeaderSkipped) {
                    isHeaderSkipped = true; // Skip header row
                    continue;
                }

                String[] data = line.split(",");
                if (data.length > 0) { // Ensure the row is not empty
                    employees.add(data);
                }
            }
        } catch (IOException e) {
            throw new IOException("Failed to load employee data: " + e.getMessage());
        }

        if (employees.isEmpty()) {
            throw new IOException("No valid employee records found in CSV.");
        }
        return employees;
    }

    // Load Attendance from CSV
    private static List<String[]> loadAttendanceFromCSV(String url) throws IOException {
        List<String[]> attendanceRecords = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new URL(url).openStream()))) {
            String line;
            boolean isHeaderSkipped = false;

            while ((line = br.readLine()) != null) {
                if (!isHeaderSkipped) {
                    isHeaderSkipped = true; // Skip header row
                    continue;
                }

                String[] data = line.split(",");
                if (data.length > 0) { // Ensure the row is not empty
                    attendanceRecords.add(data);
                }
            }
        } catch (IOException e) {
            throw new IOException("Failed to load attendance data: " + e.getMessage());
        }

        if (attendanceRecords.isEmpty()) {
            throw new IOException("No valid attendance records found in CSV.");
        }
        return attendanceRecords;
    }

    // Employee Management Menu
    private static void employeeManagement(List<String[]> employees, Scanner scanner) {
        while (true) {
            System.out.println("\nEmployee Management:");
            System.out.println("1. Search Employee");
            System.out.println("2. List All Employees");
            System.out.println("3. Return to Main Menu");
            System.out.print("Enter your choice: ");

            try {
                int choice = Integer.parseInt(scanner.nextLine());
                switch (choice) {
                    case 1 -> searchEmployee(employees, scanner);
                    case 2 -> listAllEmployees(employees);
                    case 3 -> {
                        return;
                    }
                    default -> System.out.println("Invalid choice. Please enter 1-3.");
                }
            } catch (NumberFormatException e) {
                System.err.println("Invalid input. Please enter a number.");
            }
        }
    }

    // Payroll Management Menu
    private static void payrollManagement(List<String[]> employees, List<String[]> attendanceRecords, Scanner scanner) {
        while (true) {
            System.out.println("\nPayroll Management:");
            System.out.println("1. Generate Payslip");
            System.out.println("2. Weekly Summary");
            System.out.println("3. Monthly Summary");
            System.out.println("4. Return to Main Menu");
            System.out.print("Enter your choice: ");

            try {
                int choice = Integer.parseInt(scanner.nextLine());
                switch (choice) {
                    case 1 -> generatePayslip(employees, attendanceRecords, scanner);
                    case 2 -> generateWeeklySummary(employees, attendanceRecords, scanner);
                    case 3 -> generateMonthlySummary(employees, attendanceRecords, scanner);
                    case 4 -> {
                        return;
                    }
                    default -> System.out.println("Invalid choice. Please enter 1-4.");
                }
            } catch (NumberFormatException e) {
                System.err.println("Invalid input. Please enter a number.");
            }
        }
    }

    // Generate Payslip
    private static void generatePayslip(List<String[]> employees, List<String[]> attendanceRecords, Scanner scanner) {
        System.out.print("Enter Employee No: ");
        int empNumber = getIntInput(scanner);

        String[] employee = findEmployeeById(employees, empNumber);
        if (employee == null) {
            System.out.println("Employee not found.");
            return;
        }

        System.out.print("Date From (MM/DD/YYYY): ");
        LocalDate startDate = getDateInput(scanner);
        System.out.print("Date To (MM/DD/YYYY): ");
        LocalDate endDate = getDateInput(scanner);

        double totalHours = calculateHoursWorked(attendanceRecords, empNumber, startDate, endDate);
        double hourlyRate = Double.parseDouble(employee[18].replaceAll("[^\\d.]", ""));
        double grossSalary = calculateGrossSalary(totalHours, hourlyRate);
        double netSalary = calculateNetSalary(grossSalary);

        System.out.println("\nPayslip:");
        System.out.printf("Employee No: %s | Name: %s %s%n", employee[0], employee[1], employee[2]);
        System.out.printf("Total Hours Worked: %.2f | Hourly Rate: %.2f%n", totalHours, hourlyRate);
        System.out.printf("Gross Salary: %.2f | Net Salary: %.2f%n", grossSalary, netSalary);

        System.out.println("1. Post Payroll");
        System.out.println("2. Edit Payroll");
        System.out.print("Enter your choice: ");

        try {
            int action = Integer.parseInt(scanner.nextLine());
            if (action == 1) {
                System.out.println("Payroll posted successfully.");
            } else if (action == 2) {
                System.out.println("Edit payroll functionality not yet implemented.");
            } else {
                System.out.println("Invalid choice.");
            }
        } catch (NumberFormatException e) {
            System.err.println("Invalid input. Please enter a number.");
        }
    }

    // Calculate Hours Worked
    private static double calculateHoursWorked(List<String[]> attendanceRecords, int empNumber, LocalDate startDate,
            LocalDate endDate) {
        double totalHours = 0;
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("H:mm");
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("M/d/yyyy");

        for (String[] record : attendanceRecords) {
            if (Integer.parseInt(record[0]) == empNumber) {
                try {
                    LocalDate recordDate = LocalDate.parse(record[3], dateFormatter);
                    if (!recordDate.isBefore(startDate) && !recordDate.isAfter(endDate)) {
                        LocalTime loginTime = LocalTime.parse(record[4], timeFormatter);
                        LocalTime logoutTime = LocalTime.parse(record[5], timeFormatter);
                        Duration duration = Duration.between(loginTime, logoutTime);
                        totalHours += duration.toMinutes() / 60.0;
                    }
                } catch (Exception e) {
                    System.err.println("Error parsing time for employee " + empNumber + ": " + e.getMessage());
                }
            }
        }
        return totalHours;
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

    // Calculate Net Salary
    private static double calculateNetSalary(double grossSalary) {
        // Deductions
        double philHealth = Math.min(grossSalary * 0.03, 1800); // Cap at ₱1,800
        double pagIbig = grossSalary * 0.02; // 2% of gross pay
        double tax = calculateWithholdingTax(grossSalary);

        // Allowances
        double riceSubsidy = 1500; // Fixed allowance
        double phoneAllowance = 1000; // Fixed allowance
        double clothingAllowance = 1000; // Fixed allowance

        // Net salary
        return grossSalary - (philHealth + pagIbig + tax) + (riceSubsidy + phoneAllowance + clothingAllowance);
    }

    // Calculate Withholding Tax
    private static double calculateWithholdingTax(double grossSalary) {
        if (grossSalary <= 20832) {
            return 0;
        } else if (grossSalary <= 33333) {
            return (grossSalary - 20832) * 0.20;
        } else if (grossSalary <= 66667) {
            return 2500 + (grossSalary - 33333) * 0.25;
        } else if (grossSalary <= 166667) {
            return 10833 + (grossSalary - 66667) * 0.30;
        } else {
            return 40833.33 + (grossSalary - 166667) * 0.32;
        }
    }

    // Search Employee
    private static void searchEmployee(List<String[]> employees, Scanner scanner) {
        System.out.print("Enter Employee No or Name: ");
        String query = scanner.nextLine().trim();

        boolean found = false;
        for (String[] employee : employees) {
            if (employee[0].equalsIgnoreCase(query) || employee[1].equalsIgnoreCase(query)
                    || employee[2].equalsIgnoreCase(query)) {
                System.out.printf("Employee No: %s | Name: %s %s | Position: %s%n", employee[0], employee[1],
                        employee[2], employee[11]);
                found = true;
            }
        }

        if (!found) {
            System.out.println("Employee not found.");
        }
    }

    // List All Employees
    private static void listAllEmployees(List<String[]> employees) {
        System.out.printf("%-10s %-20s %-20s %-20s%n", "Emp#", "First Name", "Last Name", "Position");
        for (String[] employee : employees) {
            System.out.printf("%-10s %-20s %-20s %-20s%n", employee[0], employee[1], employee[2], employee[11]);
        }
    }

    // Generate Weekly Summary
    private static void generateWeeklySummary(List<String[]> employees, List<String[]> attendanceRecords,
            Scanner scanner) {
        System.out.print("Enter Week Start Date (MM/DD/YYYY): ");
        LocalDate startDate = getDateInput(scanner);
        LocalDate endDate = startDate.plusDays(6); // End date is 6 days after start date

        System.out.printf("%-10s %-20s %-15s %-10s %-10s%n", "Emp#", "Name", "Total Hours", "Gross Pay", "Net Pay");
        for (String[] employee : employees) {
            int empNumber = Integer.parseInt(employee[0]);
            double totalHours = calculateHoursWorked(attendanceRecords, empNumber, startDate, endDate);
            double hourlyRate = Double.parseDouble(employee[18].replaceAll("[^\\d.]", ""));
            double grossSalary = calculateGrossSalary(totalHours, hourlyRate);
            double netSalary = calculateNetSalary(grossSalary);

            System.out.printf("%-10s %-20s %-15.2f %-10.2f %-10.2f%n", employee[0], employee[1] + " " + employee[2],
                    totalHours, grossSalary, netSalary);
        }
    }

    // Generate Monthly Summary
    private static void generateMonthlySummary(List<String[]> employees, List<String[]> attendanceRecords,
            Scanner scanner) {
        System.out.print("Enter Month Start Date (MM/DD/YYYY): ");
        LocalDate startDate = getDateInput(scanner);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1); // End date is the last day of the month

        System.out.printf("%-10s %-20s %-15s %-10s %-10s%n", "Emp#", "Name", "Total Hours", "Gross Pay", "Net Pay");
        for (String[] employee : employees) {
            int empNumber = Integer.parseInt(employee[0]);
            double totalHours = calculateHoursWorked(attendanceRecords, empNumber, startDate, endDate);
            double hourlyRate = Double.parseDouble(employee[18].replaceAll("[^\\d.]", ""));
            double grossSalary = calculateGrossSalary(totalHours, hourlyRate);
            double netSalary = calculateNetSalary(grossSalary);

            System.out.printf("%-10s %-20s %-15.2f %-10.2f %-10.2f%n", employee[0], employee[1] + " " + employee[2],
                    totalHours, grossSalary, netSalary);
        }
    }

    // Helper: Get Integer Input
    private static int getIntInput(Scanner scanner) {
        while (true) {
            try {
                return Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.err.println("Invalid input. Please enter a valid integer.");
            }
        }
    }

    // Helper: Get Date Input
    private static LocalDate getDateInput(Scanner scanner) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d/yyyy");
        while (true) {
            try {
                return LocalDate.parse(scanner.nextLine(), formatter);
            } catch (Exception e) {
                System.err.println("Invalid date format. Please enter the date in MM/DD/YYYY format.");
            }
        }
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
}