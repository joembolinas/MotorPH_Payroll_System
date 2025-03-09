package com.motorph.original;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MotorPHPayroll {

    // Constants
    private static final double OVERTIME_RATE = 1.25; // 25% additional pay for overtime
    private static final double REGULAR_HOURS_PER_WEEK = 40;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        List<String[]> employees = new ArrayList<>();
        List<String[]> attendanceRecords = new ArrayList<>();

        try {
            // Load employee and attendance data from CSV files
            employees = loadEmployeesFromCSV("https://docs.google.com/spreadsheets/d/e/2PACX-1vQXb2izvcFadSUJFaQaUwgFbiyWdzD0YcVmXGBt-KVfkWOt1y0pfuTRw9b3QHfS0FitdmgwKhKN1a0n/pub?gid=436645740&single=true&output=csv");
            attendanceRecords = loadAttendanceFromCSV("https://docs.google.com/spreadsheets/d/e/2PACX-1vQXb2izvcFadSUJFaQaUwgFbiyWdzD0YcVmXGBt-KVfkWOt1y0pfuTRw9b3QHfS0FitdmgwKhKN1a0n/pub?gid=1494987339&single=true&output=csv");
        } catch (IOException e) {
            System.err.println("Error loading data: " + e.getMessage());
            System.exit(1);
        }

        // Main menu loop
        while (true) {
            System.out.println("\n=== MotorPH Payroll System ===");
            System.out.println("1. Employee Management");
            System.out.println("2. Payroll Management");
            System.out.println("3. Reports");
            System.out.println("4. Exit");
            System.out.print("Enter your choice: ");

            int choice = getIntInput(scanner, 1, 4);

            switch (choice) {
                case 1 -> employeeManagement(employees, attendanceRecords, scanner);
                case 2 -> payrollManagement(employees, attendanceRecords, scanner);
                case 3 -> reportsMenu(employees, attendanceRecords, scanner);
                case 4 -> {
                    System.out.println("Exiting system...");
                    scanner.close();
                    System.exit(0);
                }
            }
        }
    }

    // Employee Management Menu
    private static void employeeManagement(List<String[]> employees, List<String[]> attendanceRecords, Scanner scanner) {
        while (true) {
            System.out.println("\nEmployee Management:");
            System.out.println("1. Search Employee");
            System.out.println("2. List All Employees");
            System.out.println("3. Attendance");
            System.out.println("4. Return to Main Menu");
            System.out.print("Enter your choice: ");

            int choice = getIntInput(scanner, 1, 4);

            switch (choice) {
                case 1 -> searchEmployee(employees, scanner);
                case 2 -> listAllEmployees(employees);
                case 3 -> viewAttendance(attendanceRecords, scanner);
                case 4 -> {
                    return;
                }
            }
        }
    }

    // Search Employee
    private static void searchEmployee(List<String[]> employees, Scanner scanner) {
        System.out.print("\nEnter search term (ID or name): ");
        String searchTerm = scanner.nextLine().trim().toLowerCase();

        List<String[]> results = new ArrayList<>();
        for (String[] employee : employees) {
            if (employee[0].equalsIgnoreCase(searchTerm) || (employee[1] + " " + employee[2]).toLowerCase().contains(searchTerm)) {
                results.add(employee);
            }
        }

        if (results.isEmpty()) {
            System.out.println("No matching employees found.");
        } else {
            System.out.printf("%-10s %-20s %-15s%n", "Emp#", "Name", "Position");
            for (String[] result : results) {
                System.out.printf("%-10s %-20s %-15s%n", result[0], result[1] + " " + result[2], result[11]);
            }
        }
    }

    // List All Employees
    private static void listAllEmployees(List<String[]> employees) {
        System.out.println("\nAll Employees:");
        System.out.printf("%-10s %-20s %-15s%n", "Emp#", "Name", "Position");

        for (String[] employee : employees) {
            System.out.printf("%-10s %-20s %-15s%n", employee[0], employee[1] + " " + employee[2], employee[11]);
        }
    }

    // View Attendance
    private static void viewAttendance(List<String[]> attendanceRecords, Scanner scanner) {
        System.out.print("\nEnter Employee No: ");
        int empNumber = getIntInput(scanner);

        System.out.print("Date From (MM/DD/YYYY): ");
        LocalDate startDate = getDateInput(scanner);

        System.out.print("Date To (MM/DD/YYYY): ");
        LocalDate endDate = getDateInput(scanner);

        System.out.println("\nDate       | In   | Out  | Duration | Remarks");
        for (String[] record : attendanceRecords) {
            if (Integer.parseInt(record[0]) == empNumber) {
                LocalDate recordDate = LocalDate.parse(record[3], DateTimeFormatter.ofPattern("M/d/yyyy"));
                if (!recordDate.isBefore(startDate) && !recordDate.isAfter(endDate)) {
                    try {
                        LocalTime loginTime = LocalTime.parse(record[4], DateTimeFormatter.ofPattern("H:mm"));
                        LocalTime logoutTime = LocalTime.parse(record[5], DateTimeFormatter.ofPattern("H:mm"));
                        Duration duration = Duration.between(loginTime, logoutTime);
                        double hoursWorked = duration.toMinutes() / 60.0;

                        String remarks = loginTime.isAfter(LocalTime.of(8, 10)) ? "Late" : "On Time";

                        System.out.printf("%-10s | %-5s | %-5s | %-8.2f | %s%n",
                                record[3], record[4], record[5], hoursWorked, remarks);
                    } catch (Exception e) {
                        System.err.println("Error parsing time for employee " + empNumber + ": " + e.getMessage());
                    }
                }
            }
        }
    }

    // Payroll Management Menu
    private static void payrollManagement(List<String[]> employees, List<String[]> attendanceRecords, Scanner scanner) {
        while (true) {
            System.out.println("\nPayroll Management:");
            System.out.println("1. Generate Payroll (Calculate All Employees)");
            System.out.println("2. Custom Payroll");
            System.out.println("3. Return to Main Menu");
            System.out.print("Enter your choice: ");

            int choice = getIntInput(scanner, 1, 3);

            switch (choice) {
                case 1 -> generatePayroll(employees, attendanceRecords, scanner);
                case 2 -> customPayroll(employees, attendanceRecords, scanner);
                case 3 -> {
                    return;
                }
            }
        }
    }

    // Generate Payroll for All Employees
    private static void generatePayroll(List<String[]> employees, List<String[]> attendanceRecords, Scanner scanner) {
        System.out.println("\nGenerate Payroll:");

        System.out.print("Date From (MM/DD/YYYY): ");
        LocalDate startDate = getDateInput(scanner);

        System.out.print("Date To (MM/DD/YYYY): ");
        LocalDate endDate = getDateInput(scanner);

        System.out.printf("%-10s %-20s %-15s %-10s %-10s %-10s%n",
                "Emp#", "Name", "Total Work Hours", "Hourly Rate", "Gross Pay", "Net Pay");

        for (String[] employee : employees) {
            int empNumber = Integer.parseInt(employee[0]);
            double totalHours = calculateHoursWorked(attendanceRecords, empNumber, startDate, endDate);
            double hourlyRate = getHourlyRate(employees, empNumber);
            double grossSalary = calculateGrossSalary(totalHours, hourlyRate);
            double netSalary = calculateNetSalary(grossSalary);

            System.out.printf("%-10s %-20s %-15.2f %-10.2f %-10.2f %-10.2f%n",
                    employee[0], employee[1] + " " + employee[2], totalHours, hourlyRate, grossSalary, netSalary);
        }

        System.out.println("\n1. Post Payroll");
        System.out.println("2. Edit Payroll");
        System.out.print("Enter your choice: ");
        int action = getIntInput(scanner, 1, 2);

        if (action == 1) {
            System.out.println("Payroll posted successfully.");
        } else if (action == 2) {
            System.out.println("Editing payroll...");
        }
    }

    // Custom Payroll for Specific Employee
    private static void customPayroll(List<String[]> employees, List<String[]> attendanceRecords, Scanner scanner) {
        System.out.print("\nEnter Employee No: ");
        int empNumber = getIntInput(scanner);

        System.out.print("Date From (MM/DD/YYYY): ");
        LocalDate startDate = getDateInput(scanner);

        System.out.print("Date To (MM/DD/YYYY): ");
        LocalDate endDate = getDateInput(scanner);

        String[] employee = findEmployeeById(employees, empNumber);
        if (employee == null) {
            System.out.println("Employee not found.");
            return;
        }

        double totalHours = calculateHoursWorked(attendanceRecords, empNumber, startDate, endDate);
        double hourlyRate = Double.parseDouble(employee[18].replaceAll("[^\\d.]", ""));
        double grossSalary = calculateGrossSalary(totalHours, hourlyRate);
        double netSalary = calculateNetSalary(grossSalary);

        System.out.printf("\nEmployee No: %s | Name: %s %s%n", employee[0], employee[1], employee[2]);
        System.out.printf("Total Work Hours: %.2f | Net Pay: %.2f | Gross Pay: %.2f%n", totalHours, netSalary, grossSalary);

        System.out.println("\n1. Post Payroll");
        System.out.println("2. Edit Payroll");
        System.out.print("Enter your choice: ");
        int action = getIntInput(scanner, 1, 2);

        if (action == 1) {
            System.out.println("Payroll posted successfully.");
        } else if (action == 2) {
            System.out.println("Editing payroll...");
        }
    }

    // Reports Menu
    private static void reportsMenu(List<String[]> employees, List<String[]> attendanceRecords, Scanner scanner) {
        while (true) {
            System.out.println("\nReports:");
            System.out.println("1. Payslip");
            System.out.println("2. Weekly Summary");
            System.out.println("3. Monthly Summary");
            System.out.println("4. Return to Main Menu");
            System.out.print("Enter your choice: ");

            int choice = getIntInput(scanner, 1, 4);

            switch (choice) {
                case 1 -> generatePayslip(employees, attendanceRecords, scanner);
                case 2 -> generateWeeklySummary(employees, attendanceRecords, scanner);
                case 3 -> generateMonthlySummary(employees, attendanceRecords, scanner);
                case 4 -> {
                    return;
                }
            }
        }
    }

    // Generate Payslip
    private static void generatePayslip(List<String[]> employees, List<String[]> attendanceRecords, Scanner scanner) {
        System.out.print("\nEnter Employee No: ");
        int empNumber = getIntInput(scanner);

        System.out.print("Date From (MM/DD/YYYY): ");
        LocalDate startDate = getDateInput(scanner);

        System.out.print("Date To (MM/DD/YYYY): ");
        LocalDate endDate = getDateInput(scanner);

        String[] employee = findEmployeeById(employees, empNumber);
        if (employee == null) {
            System.out.println("Employee not found.");
            return;
        }

        double totalHours = calculateHoursWorked(attendanceRecords, empNumber, startDate, endDate);
        double hourlyRate = Double.parseDouble(employee[18].replaceAll("[^\\d.]", ""));
        double grossSalary = calculateGrossSalary(totalHours, hourlyRate);
        double netSalary = calculateNetSalary(grossSalary);

        System.out.println("\nPayslip:");
        System.out.printf("Employee No: %s | Name: %s %s%n", employee[0], employee[1], employee[2]);
        System.out.printf("Total Work Hours: %.2f | Net Pay: %.2f | Gross Pay: %.2f%n", totalHours, netSalary, grossSalary);
    }

    // Generate Weekly Summary
    private static void generateWeeklySummary(List<String[]> employees, List<String[]> attendanceRecords, Scanner scanner) {
        System.out.println("\nWeekly Summary Report:");
        System.out.printf("%-10s %-20s %-15s %-10s %-10s%n", "Emp#", "Name", "Total Work Hours", "Net Pay", "Gross Pay");

        for (String[] employee : employees) {
            int empNumber = Integer.parseInt(employee[0]);
            double totalHours = calculateHoursWorked(attendanceRecords, empNumber, LocalDate.now().minusDays(7), LocalDate.now());
            double hourlyRate = Double.parseDouble(employee[18].replaceAll("[^\\d.]", ""));
            double grossSalary = calculateGrossSalary(totalHours, hourlyRate);
            double netSalary = calculateNetSalary(grossSalary);

            System.out.printf("%-10s %-20s %-15.2f %-10.2f %-10.2f%n",
                    employee[0], employee[1] + " " + employee[2], totalHours, netSalary, grossSalary);
        }
    }

    // Generate Monthly Summary
    private static void generateMonthlySummary(List<String[]> employees, List<String[]> attendanceRecords, Scanner scanner) {
        System.out.println("\nMonthly Summary Report:");
        System.out.printf("%-10s %-20s %-15s %-10s %-10s%n", "Emp#", "Name", "Total Work Hours", "Net Pay", "Gross Pay");

        for (String[] employee : employees) {
            int empNumber = Integer.parseInt(employee[0]);
            double totalHours = calculateHoursWorked(attendanceRecords, empNumber, LocalDate.now().minusMonths(1), LocalDate.now());
            double hourlyRate = Double.parseDouble(employee[18].replaceAll("[^\\d.]", ""));
            double grossSalary = calculateGrossSalary(totalHours, hourlyRate);
            double netSalary = calculateNetSalary(grossSalary);

            System.out.printf("%-10s %-20s %-15.2f %-10.2f %-10.2f%n",
                    employee[0], employee[1] + " " + employee[2], totalHours, netSalary, grossSalary);
        }
    }

    // Calculate Hours Worked
    private static double calculateHoursWorked(List<String[]> attendanceRecords, int empNumber, LocalDate startDate, LocalDate endDate) {
        double totalHours = 0;
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("H:mm");

        for (String[] record : attendanceRecords) {
            if (Integer.parseInt(record[0]) == empNumber) {
                LocalDate recordDate = LocalDate.parse(record[3], DateTimeFormatter.ofPattern("M/d/yyyy"));
                if (!recordDate.isBefore(startDate) && !recordDate.isAfter(endDate)) {
                    try {
                        LocalTime loginTime = LocalTime.parse(record[4], timeFormatter);
                        LocalTime logoutTime = LocalTime.parse(record[5], timeFormatter);
                        Duration duration = Duration.between(loginTime, logoutTime);
                        totalHours += duration.toMinutes() / 60.0;
                    } catch (Exception e) {
                        System.err.println("Error parsing time for employee " + empNumber + ": " + e.getMessage());
                    }
                }
            }
        }
        return totalHours;
    }

    // Calculate Gross Salary
    private static double calculateGrossSalary(double hoursWorked, double hourlyRate) {
        return hoursWorked * hourlyRate;
    }

    // Calculate Net Salary
    private static double calculateNetSalary(double grossSalary) {
        double sssDeduction = calculateSSS(grossSalary);
        double philhealthDeduction = calculatePhilHealth(grossSalary);
        double pagibigDeduction = calculatePagIBIG(grossSalary);
        double taxDeduction = calculateWithholdingTax(grossSalary - (sssDeduction + philhealthDeduction + pagibigDeduction));

        return grossSalary - (sssDeduction + philhealthDeduction + pagibigDeduction + taxDeduction);
    }

    private static double calculateSSS(double grossSalary) {
        if (grossSalary < 3250) return 135.00;
        else if (grossSalary <= 3750) return 157.50;
        // Add more ranges as needed
        return 0; // Default case
    }

    private static double calculatePhilHealth(double grossSalary) {
        return grossSalary * 0.03 / 2; // Simplified calculation
    }

    private static double calculatePagIBIG(double grossSalary) {
        return Math.min(grossSalary * 0.02, 100); // Simplified calculation
    }

    private static double calculateWithholdingTax(double taxableIncome) {
        if (taxableIncome <= 20833) return 0;
        else if (taxableIncome <= 33333) return (taxableIncome - 20833) * 0.20;
        else if (taxableIncome <= 66667) return 2500 + (taxableIncome - 33333) * 0.25;
        else if (taxableIncome <= 166667) return 10833 + (taxableIncome - 66667) * 0.30;
        else return 40833.33 + (taxableIncome - 166667) * 0.32;
    }

    // Find Employee by ID
    private static String[] findEmployeeById(List<String[]> employees, int empNumber) {
        for (String[] employee : employees) {
            if (Integer.parseInt(employee[0]) == empNumber) {
                return employee;
            }
        }
        throw new IllegalArgumentException("Employee not found: " + empNumber);
    }

    // Get Hourly Rate
    private static double getHourlyRate(List<String[]> employees, int empNumber) {
        for (String[] employee : employees) {
            if (Integer.parseInt(employee[0]) == empNumber) {
                return Double.parseDouble(employee[18].replaceAll("[^\\d.]", ""));
            }
        }
        throw new IllegalArgumentException("Employee not found: " + empNumber);
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

    // Helper method to get integer input with validation
    private static int getIntInput(Scanner scanner) {
        while (true) {
            try {
                return Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.print("Invalid input. Please enter a valid integer: ");
            }
        }
    }

    // Helper method to get integer input within a range
    private static int getIntInput(Scanner scanner, int min, int max) {
        while (true) {
            int input = getIntInput(scanner);
            if (input >= min && input <= max) {
                return input;
            }
            System.out.printf("Invalid input. Please enter a number between %d and %d: ", min, max);
        }
    }

    // Helper method to get date input with validation
    private static LocalDate getDateInput(Scanner scanner) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("M/d/yyyy");
        while (true) {
            try {
                return LocalDate.parse(scanner.nextLine(), dateFormatter);
            } catch (DateTimeParseException e) {
                System.out.print("Invalid date format. Please enter date in MM/DD/YYYY format: ");
            }
        }
    }
}