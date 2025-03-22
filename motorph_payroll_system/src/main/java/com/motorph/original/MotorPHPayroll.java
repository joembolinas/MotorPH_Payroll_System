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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * The MotorPH Payroll System is a comprehensive Java application for managing
 * employee information, calculating payroll, and generating reports for MotorPH
 * company.
 * 
 * This system enables:
 * - Employee data management (search, list, view attendance)
 * - Payroll processing (for all employees or individual employees)
 * - Report generation (payslips, weekly and monthly summaries)
 * 
 * The application loads employee and attendance data from online CSV sources
 * and processes this information to calculate pay and deductions.
 */
public class MotorPHPayroll {

    // Constants
    private static final double OVERTIME_RATE = 1.25;
    private static final int REGULAR_HOURS_PER_DAY = 8;
    private static final int WORK_DAYS_PER_MONTH = 21;
    private static final LocalTime LATE_THRESHOLD = LocalTime.of(8, 10);

    private static final String DATE_FORMAT_PATTERN = "MM/dd/yyyy";
    private static final String TIME_FORMAT_PATTERN = "H:mm";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT_PATTERN);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern(TIME_FORMAT_PATTERN);

    // Employee data column indices
    private static final int EMP_ID_COL = 0;
    private static final int LAST_NAME_COL = 1;
    private static final int FIRST_NAME_COL = 2;
    private static final int POSITION_COL = 11;
    private static final int STATUS_COL = 10;
    private static final int BASIC_SALARY_COL = 13;
    private static final int RICE_SUBSIDY_COL = 14;
    private static final int PHONE_ALLOWANCE_COL = 15;
    private static final int CLOTHING_ALLOWANCE_COL = 16;
    private static final int HOURLY_RATE_COL = 18;

    // Attendance record column indices
    private static final int ATT_EMP_ID_COL = 0;
    private static final int ATT_DATE_COL = 3;
    private static final int ATT_TIME_IN_COL = 4;
    private static final int ATT_TIME_OUT_COL = 5;

    private static final Map<Double, Double> SSS_TABLE = initSSSTable();
    private static final PayrollCalculator payrollCalculator = new PayrollCalculator();

    /**
     * Main entry point for the MotorPH Payroll System.
     */
    public static void main(String[] args) {
        List<String[]> employees = new ArrayList<>();
        List<String[]> attendanceRecords = new ArrayList<>();

        try {
            // Load employee and attendance data from CSV files
            employees = loadEmployeesFromCSV(
                    "https://docs.google.com/spreadsheets/d/e/2PACX-1vRe4-w2yYtOZpBxFZGP1UZqyKWk053QkCmVxwq9Hiu2LfHU2nVIvCkTTg8rtWQsP-sp31jG6OleREqM/pub?output=csv");
            attendanceRecords = loadAttendanceFromCSV(
                    "https://docs.google.com/spreadsheets/d/e/2PACX-1vTqBrLETQHcACenfV0_VSgV_uEGH5Cne2Vuw-oN2yDGRH5wWS8x8CcAXAV8iSNugtwWB_oVCuOlcFYT/pub?output=csv");

        } catch (IOException e) {
            System.err.println("Error loading data: " + e.getMessage());
            System.exit(1);
        }

        // Main menu loop
        Scanner scanner = new Scanner(System.in);
        boolean running = true;
        while (running) {
            System.out.println("\n=== MotorPH Payroll System ===");
            System.out.println("1. Employee Management");
            System.out.println("2. Payroll Management");
            System.out.println("3. Reports");
            System.out.println("4. Exit");
            System.out.print("Enter your choice: ");

            try {
                int choice = Integer.parseInt(scanner.nextLine());
                switch (choice) {
                    case 1 -> employeeManagement(employees, attendanceRecords, scanner);
                    case 2 -> payrollManagement(employees, attendanceRecords, scanner);
                    case 3 -> reportsMenu(employees, attendanceRecords, scanner);
                    case 4 -> {
                        System.out.println("Exiting system...");
                        running = false;
                    }
                    default -> System.out.println("Invalid choice. Please enter 1-4.");
                }
            } catch (NumberFormatException e) {
                System.err.println("Invalid input. Please enter a number.");
            }
        }
        scanner.close();
    }

    /**
     * Initializes the SSS contribution table
     */
    private static Map<Double, Double> initSSSTable() {
        Map<Double, Double> sssTable = new HashMap<>();
        sssTable.put(4250.0, 180.0);
        sssTable.put(4750.0, 202.5);
        sssTable.put(5250.0, 225.0);
        sssTable.put(5750.0, 247.5);
        sssTable.put(6250.0, 270.0);
        sssTable.put(6750.0, 292.5);
        sssTable.put(7250.0, 315.0);
        sssTable.put(7750.0, 337.5);
        sssTable.put(8250.0, 360.0);
        sssTable.put(8750.0, 382.5);
        sssTable.put(9250.0, 405.0);
        sssTable.put(9750.0, 427.5);
        sssTable.put(10250.0, 450.0);
        sssTable.put(10750.0, 472.5);
        sssTable.put(11250.0, 495.0);
        sssTable.put(11750.0, 517.5);
        sssTable.put(12250.0, 540.0);
        sssTable.put(12750.0, 562.5);
        sssTable.put(13250.0, 585.0);
        sssTable.put(13750.0, 607.5);
        sssTable.put(14250.0, 630.0);
        sssTable.put(14750.0, 652.5);
        sssTable.put(15250.0, 675.0);
        sssTable.put(15750.0, 697.5);
        sssTable.put(16250.0, 720.0);
        sssTable.put(16750.0, 742.5);
        sssTable.put(17250.0, 765.0);
        sssTable.put(17750.0, 787.5);
        sssTable.put(18250.0, 810.0);
        sssTable.put(18750.0, 832.5);
        sssTable.put(19250.0, 855.0);
        sssTable.put(19750.0, 877.5);
        sssTable.put(20250.0, 900.0);
        sssTable.put(20750.0, 922.5);
        sssTable.put(Double.MAX_VALUE, 945.0);
        return sssTable;
    }

    /**
     * Loads employee data from a CSV file
     */
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

                // Parse CSV with quoted fields
                List<String> fields = new ArrayList<>();
                StringBuilder sb = new StringBuilder();
                boolean inQuotes = false;

                for (char c : line.toCharArray()) {
                    if (c == '"') {
                        inQuotes = !inQuotes; // Toggle quote state
                    } else if (c == ',' && !inQuotes) {
                        // End of field
                        fields.add(sb.toString());
                        sb = new StringBuilder();
                    } else {
                        sb.append(c);
                    }
                }
                fields.add(sb.toString()); // Add the last field

                String[] data = fields.toArray(new String[0]);
                if (data.length > 0) {
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

    /**
     * Loads attendance data from a CSV file
     */
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
                if (data.length > 0) {
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

    /**
     * Employee management menu
     */
    private static void employeeManagement(List<String[]> employees, List<String[]> attendanceRecords,
            Scanner scanner) {
        while (true) {
            System.out.println("\nEmployee Management:");
            System.out.println("1. Search Employee");
            System.out.println("2. List All Employees");
            System.out.println("3. Attendance");
            System.out.println("4. Return to Main Menu");
            System.out.print("Enter your choice: ");

            try {
                int choice = Integer.parseInt(scanner.nextLine());
                switch (choice) {
                    case 1 -> searchEmployee(employees, scanner);
                    case 2 -> listAllEmployees(employees);
                    case 3 -> viewAttendance(employees, attendanceRecords, scanner);
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

    /**
     * Payroll management menu
     */
    private static void payrollManagement(List<String[]> employees, List<String[]> attendanceRecords, Scanner scanner) {
        while (true) {
            System.out.println("\nPayroll Management:");
            System.out.println("1. Generate Payroll (Calculate All Employees)");
            System.out.println("2. Custom Payroll");
            System.out.println("3. Return to Main Menu");
            System.out.print("Enter your choice: ");

            try {
                int choice = Integer.parseInt(scanner.nextLine());
                switch (choice) {
                    case 1 -> generatePayroll(employees, attendanceRecords, scanner);
                    case 2 -> generateEmployeePayslip(employees, attendanceRecords, scanner, "EMPLOYEE PAYSLIP");
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

    /**
     * Reports menu
     */
    private static void reportsMenu(List<String[]> employees, List<String[]> attendanceRecords, Scanner scanner) {
        while (true) {
            System.out.println("\nReports:");
            System.out.println("1. Payslip");
            System.out.println("2. Weekly Summary");
            System.out.println("3. Monthly Summary");
            System.out.println("4. Return to Main Menu");
            System.out.print("Enter your choice: ");

            try {
                int choice = Integer.parseInt(scanner.nextLine());
                switch (choice) {
                    case 1 -> generateEmployeePayslip(employees, attendanceRecords, scanner, "PAYSLIP REPORT");
                    case 2 -> generateSummaryReport(employees, attendanceRecords, "Weekly");
                    case 3 -> generateSummaryReport(employees, attendanceRecords, "Monthly");
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

    /**
     * Generates payroll for all employees within a specified date range
     */
    private static void generatePayroll(List<String[]> employees, List<String[]> attendanceRecords, Scanner scanner) {
        System.out.println("\nGenerate Payroll:");
        LocalDate startDate = getDateInput(scanner, "Date From (MM/DD/YYYY): ");
        LocalDate endDate = getDateInput(scanner, "Date To (MM/DD/YYYY): ");

        System.out.println(
                "═════════════════════════════════════════════════════════════════════════════════════════════════════════════");
        System.out.printf("%-7s %-25s %-10s %-10s %-12s %-15s %-15s %-15s%n",
                "Emp#", "Name", "Reg Hours", "OT Hours", "Hourly Rate", "Gross Pay", "Allowances", "Net Pay");
        System.out.println(
                "─────────────────────────────────────────────────────────────────────────────────────────────────────────────");

        for (String[] employee : employees) {
            int empNumber = Integer.parseInt(employee[0]);
            double hourlyRate = extractHourlyRate(employee);

            // Calculate with new methods including overtime
            Map<String, Double> payDetails = getGrossPayDetails(attendanceRecords, empNumber, hourlyRate, startDate,
                    endDate);
            double regularHours = payDetails.get("regularHours");
            double overtimeHours = payDetails.get("overtimeHours");
            double grossPay = payDetails.get("totalPay");

            // Get pro-rated allowances
            Map<String, Double> allowanceDetails = getProRatedAllowanceDetails(employee, startDate, endDate);
            double totalAllowances = allowanceDetails.get("totalAllowances");

            double sumAfterDeductions = payrollCalculator.calculateNetPay(grossPay);
            double netPay = sumAfterDeductions + totalAllowances;

            // Format the name properly
            String fullName = formatEmployeeName(employee);

            // Format numbers with commas for thousands
            System.out.printf("%-7s %-25s %10.2f %10.2f %12.2f %15s %15s %15s%n",
                    employee[0],
                    fullName,
                    regularHours,
                    overtimeHours,
                    hourlyRate,
                    String.format("%,.2f", grossPay),
                    String.format("%,.2f", totalAllowances),
                    String.format("%,.2f", netPay));
        }

        System.out.println(
                "═════════════════════════════════════════════════════════════════════════════════════════════════════════════");

        System.out.println("\nPress Enter to return to menu...");
        scanner.nextLine();
    }

    /**
     * Consolidated method for generating payslips and custom payroll
     */
    private static void generateEmployeePayslip(List<String[]> employees, List<String[]> attendanceRecords,
            Scanner scanner, String title) {
        System.out.print("\nEnter Employee No: ");
        int empNumber;
        try {
            empNumber = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid employee number. Please enter a numeric value.");
            return;
        }

        String[] employee = findEmployeeById(employees, empNumber);
        if (employee == null) {
            System.out.println("Employee not found.");
            return;
        }

        LocalDate startDate = getDateInput(scanner, "Date From (MM/DD/YYYY): ");
        LocalDate endDate = getDateInput(scanner, "Date To (MM/DD/YYYY): ");

        double hourlyRate = extractHourlyRate(employee);

        // Get detailed pay breakdown with overtime
        Map<String, Double> payDetails = getGrossPayDetails(attendanceRecords, empNumber, hourlyRate, startDate,
                endDate);
        double regularHours = payDetails.get("regularHours");
        double overtimeHours = payDetails.get("overtimeHours");
        double regularPay = payDetails.get("regularPay");
        double overtimePay = payDetails.get("overtimePay");
        double grossPay = payDetails.get("totalPay");

        // Get pro-rated allowances
        Map<String, Double> allowanceDetails = getProRatedAllowanceDetails(employee, startDate, endDate);
        double riceSubsidy = allowanceDetails.get("riceSubsidy");
        double phoneAllowance = allowanceDetails.get("phoneAllowance");
        double clothingAllowance = allowanceDetails.get("clothingAllowance");
        double totalAllowances = allowanceDetails.get("totalAllowances");
        double workingDays = allowanceDetails.get("workingDays");

        // Calculate deductions and net pay
        double sumAfterDeductions = payrollCalculator.calculateNetPay(grossPay);
        double netPay = sumAfterDeductions + totalAllowances;

        String fullName = formatEmployeeName(employee);

        // Enhanced output with detailed breakdown
        System.out.println("\n═══════════════════════════════════════════");
        System.out.println("           " + title);
        System.out.println("═══════════════════════════════════════════");
        System.out.println("Employee No: " + empNumber);
        System.out.println("Name: " + fullName);
        System.out.println("Position: " + (employee.length > POSITION_COL ? employee[POSITION_COL] : "N/A"));
        System.out.println("Period: " + startDate.format(DATE_FORMATTER) + " to " + endDate.format(DATE_FORMATTER));
        System.out.println("Working Days: " + (int) workingDays + " of " + WORK_DAYS_PER_MONTH + " days");
        System.out.println("───────────────────────────────────────────");
        System.out.println("HOURS WORKED:");
        System.out.printf("Regular Hours: %.2f\n", regularHours);
        System.out.printf("Overtime Hours: %.2f\n", overtimeHours);
        System.out.printf("Total Hours: %.2f\n", regularHours + overtimeHours);
        System.out.println("───────────────────────────────────────────");
        System.out.println("PAY DETAILS:");
        System.out.printf("Hourly Rate: ₱%.2f\n", hourlyRate);
        System.out.printf("Regular Pay: ₱%.2f\n", regularPay);
        System.out.printf("Overtime Pay: ₱%.2f\n", overtimePay);
        System.out.printf("Gross Pay: ₱%.2f\n", grossPay);
        System.out.println("───────────────────────────────────────────");
        System.out.println("DEDUCTIONS:");
        System.out.printf("SSS: ₱%.2f\n", payrollCalculator.calculateSSSContribution(grossPay));
        System.out.printf("PhilHealth: ₱%.2f\n", payrollCalculator.calculatePhilHealthContribution(grossPay));
        System.out.printf("Pag-IBIG: ₱%.2f\n", payrollCalculator.calculatePagIbigContribution(grossPay));
        double withholdingTax = payrollCalculator.calculateWithholdingTax(grossPay -
                payrollCalculator.calculateSSSContribution(grossPay) -
                payrollCalculator.calculatePhilHealthContribution(grossPay) -
                payrollCalculator.calculatePagIbigContribution(grossPay));
        System.out.printf("Withholding Tax: ₱%.2f\n", withholdingTax);
        System.out.printf("Total Deductions: ₱%.2f\n", (grossPay - sumAfterDeductions));
        System.out.println("───────────────────────────────────────────");
        System.out.println("ALLOWANCES (Pro-rated for " + (int) workingDays + " days):");
        System.out.printf("Rice Subsidy: ₱%.2f\n", riceSubsidy);
        System.out.printf("Phone Allowance: ₱%.2f\n", phoneAllowance);
        System.out.printf("Clothing Allowance: ₱%.2f\n", clothingAllowance);
        System.out.printf("Total Allowances: ₱%.2f\n", totalAllowances);
        System.out.println("───────────────────────────────────────────");
        System.out.printf("FINAL NET PAY: ₱%.2f\n", netPay);
        System.out.println("═══════════════════════════════════════════");

        // Pause before returning to menu
        System.out.println("\nPress Enter to return to menu...");
        scanner.nextLine();
    }

    /**
     * Consolidated method for weekly and monthly summary reports
     */
    private static void generateSummaryReport(List<String[]> employees, List<String[]> attendanceRecords, String period) {
        System.out.println("\n" + period + " Summary Report:");

        // Get date range from user
        Scanner scanner = new Scanner(System.in);
        LocalDate startDate = getDateInput(scanner, "Date From (MM/DD/YYYY): ");
        LocalDate endDate = getDateInput(scanner, "Date To (MM/DD/YYYY): ");

        System.out.printf("%-10s %-25s %-15s %-15s %-15s%n",
                "Emp#", "Name", "Total Work Hours", "Net Pay", "Gross Pay");
        System.out.println("-".repeat(85));

        for (String[] employee : employees) {
            int empNumber = Integer.parseInt(employee[0]);
            String fullName = formatEmployeeName(employee);
            double hourlyRate = extractHourlyRate(employee);

            // Calculate directly from attendance records
            Map<String, Double> payDetails = getGrossPayDetails(attendanceRecords, empNumber, hourlyRate, startDate,
                    endDate);
            double totalRegularHours = payDetails.get("regularHours");
            double totalOvertimeHours = payDetails.get("overtimeHours");
            double totalHours = totalRegularHours + totalOvertimeHours;
            double grossPay = payDetails.get("totalPay");

            // Get pro-rated allowances
            Map<String, Double> allowanceDetails = getProRatedAllowanceDetails(employee, startDate, endDate);
            double totalAllowances = allowanceDetails.get("totalAllowances");

            // Calculate net pay
            double sumAfterDeductions = payrollCalculator.calculateNetPay(grossPay);
            double netPay = sumAfterDeductions + totalAllowances;

            System.out.printf("%-10s %-25s %-15.2f %-15.2f %-15.2f%n",
                    employee[0], fullName, totalHours, netPay, grossPay);
        }
    }

    /**
     * Displays attendance records for a specific employee within a date range
     */
    private static void viewAttendance(List<String[]> employees, List<String[]> attendanceRecords, Scanner scanner) {
        System.out.print("\nEnter Employee No: ");
        int empNumber;
        try {
            empNumber = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid employee number. Please enter a numeric value.");
            return;
        }

        LocalDate startDate = getDateInput(scanner, "Date From (MM/DD/YYYY): ");
        LocalDate endDate = getDateInput(scanner, "Date To (MM/DD/YYYY): ");

        DateTimeFormatter outputDateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");

        System.out.printf("%-10s | %-6s | %-6s | %-9s | %-10s%n",
                "Date", "In", "Out", "Duration", "Remarks");

        boolean found = false;
        for (String[] record : attendanceRecords) {
            if (isRecordForEmployee(record, empNumber)) {
                try {
                    // Use flexible date parser
                    LocalDate recordDate = parseFlexibleDate(record[ATT_DATE_COL]);

                    if (recordDate != null && !recordDate.isBefore(startDate) && !recordDate.isAfter(endDate)) {
                        found = true;
                        LocalTime timeIn = LocalTime.parse(record[ATT_TIME_IN_COL], TIME_FORMATTER);
                        LocalTime timeOut = LocalTime.parse(record[ATT_TIME_OUT_COL], TIME_FORMATTER);

                        double duration = Duration.between(timeIn, timeOut).toMinutes() / 60.0;
                        String remarks = timeIn.isBefore(LATE_THRESHOLD) ? "On Time" : "Late";

                        System.out.printf("%-10s | %-6s | %-6s | %-9.2f | %-10s%n",
                                recordDate.format(outputDateFormatter),
                                timeIn.format(TIME_FORMATTER),
                                timeOut.format(TIME_FORMATTER),
                                duration,
                                remarks);
                    }
                } catch (Exception e) {
                    // Silently skip problematic records
                    continue;
                }
            }
        }

        if (!found) {
            System.out.println("No attendance records found for the specified period.");
        }
    }

    /**
     * Checks if an attendance record belongs to the specified employee
     */
    private static boolean isRecordForEmployee(String[] record, int empNumber) {
        try {
            return Integer.parseInt(record[ATT_EMP_ID_COL]) == empNumber;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Helper method to parse dates in multiple formats
     */
    private static LocalDate parseFlexibleDate(String dateStr) {
        // Define multiple formatters for different date patterns
        DateTimeFormatter[] formatters = {
                DateTimeFormatter.ofPattern("MM/dd/yyyy"),
                DateTimeFormatter.ofPattern("M/d/yyyy"),
                DateTimeFormatter.ofPattern("M/dd/yyyy"),
                DateTimeFormatter.ofPattern("MM/d/yyyy")
        };

        // Try each formatter
        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalDate.parse(dateStr, formatter);
            } catch (DateTimeParseException e) {
                // Try next formatter
                continue;
            }
        }

        // If all formatters fail, try manual parsing as a last resort
        try {
            String[] parts = dateStr.split("/");
            if (parts.length == 3) {
                int month = Integer.parseInt(parts[0]);
                int day = Integer.parseInt(parts[1]);
                int year = Integer.parseInt(parts[2]);
                return LocalDate.of(year, month, day);
            }
        } catch (Exception e) {
            // Ignore and return null
        }

        return null; // Could not parse using any method
    }

    /**
     * Extracts the hourly rate for an employee
     */
    private static double extractHourlyRate(String[] employee) {
        // Try direct hourly rate
        try {
            if (employee.length > HOURLY_RATE_COL && employee[HOURLY_RATE_COL] != null) {
                String rateString = employee[HOURLY_RATE_COL].replaceAll("[^0-9.]", "").trim();
                if (!rateString.isEmpty()) {
                    double rate = Double.parseDouble(rateString);
                    if (rate > 0) return rate;
                }
            }
        } catch (Exception e) {
            // Continue to next method
        }

        // Try calculating from basic salary
        try {
            if (employee.length > BASIC_SALARY_COL && employee[BASIC_SALARY_COL] != null) {
                String salaryString = employee[BASIC_SALARY_COL].replaceAll("[^0-9.]", "").trim();
                if (!salaryString.isEmpty()) {
                    double basicSalary = Double.parseDouble(salaryString);
                    return (basicSalary / WORK_DAYS_PER_MONTH) / REGULAR_HOURS_PER_DAY;
                }
            }
        } catch (Exception e) {
            // Continue to default method
        }

        // Default rate by position
        try {
            if (employee.length > POSITION_COL && employee[POSITION_COL] != null) {
                String position = employee[POSITION_COL].toLowerCase();
                if (position.contains("chief") || position.contains("ceo")) {
                    return 535.71;
                } else if (position.contains("manager") || position.contains("head")) {
                    return 313.51;
                } else if (position.contains("team leader")) {
                    return 255.80;
                }
            }
        } catch (Exception e) {
            // Use default rate
        }
        
        return 133.93; // Default rate for regular employees
    }

    /**
     * Extract allowance from employee record
     */
    private static double extractAllowance(String[] employee, int columnIndex) {
        try {
            if (employee.length > columnIndex && employee[columnIndex] != null) {
                String allowanceString = employee[columnIndex].replaceAll("[^0-9.]", "").trim();
                if (!allowanceString.isEmpty()) {
                    return Double.parseDouble(allowanceString);
                }
            }
            return 0.0;
        } catch (Exception e) {
            return 0.0;
        }
    }

    /**
     * Gets the individual pro-rated allowances
     */
    private static Map<String, Double> getProRatedAllowanceDetails(String[] employee, LocalDate startDate,
            LocalDate endDate) {
        Map<String, Double> allowances = new HashMap<>();

        // Extract full monthly allowances
        double riceSubsidy = extractAllowance(employee, RICE_SUBSIDY_COL);
        double phoneAllowance = extractAllowance(employee, PHONE_ALLOWANCE_COL);
        double clothingAllowance = extractAllowance(employee, CLOTHING_ALLOWANCE_COL);

        // Calculate working days in the period (excluding weekends)
        long totalDays = 0;
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            // Check if it's a weekday (not Saturday or Sunday)
            if (currentDate.getDayOfWeek().getValue() <= 5) {
                totalDays++;
            }
            currentDate = currentDate.plusDays(1);
        }

        // Cap the working days at WORK_DAYS_PER_MONTH (21)
        long effectiveDays = Math.min(totalDays, WORK_DAYS_PER_MONTH);
        double proRateFactor = (double) effectiveDays / WORK_DAYS_PER_MONTH;

        allowances.put("riceSubsidy", riceSubsidy * proRateFactor);
        allowances.put("phoneAllowance", phoneAllowance * proRateFactor);
        allowances.put("clothingAllowance", clothingAllowance * proRateFactor);
        allowances.put("totalAllowances", (riceSubsidy + phoneAllowance + clothingAllowance) * proRateFactor);
        allowances.put("workingDays", (double) totalDays);
        allowances.put("effectiveWorkingDays", (double) effectiveDays);

        return allowances;
    }

    /**
     * Prompts for date input with custom message
     */
    private static LocalDate getDateInput(Scanner scanner, String prompt) {
        System.out.print(prompt);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        while (true) {
            try {
                String input = scanner.nextLine();
                return LocalDate.parse(input, formatter);
            } catch (DateTimeParseException e) {
                System.err.println("Invalid date format. Please use MM/DD/YYYY format.");
                System.out.print(prompt);
            }
        }
    }

    /**
     * Searches for employees based on a search term
     */
    private static void searchEmployee(List<String[]> employees, Scanner scanner) {
        System.out.print("\nEnter search term (name or employee number): ");
        String searchTerm = scanner.nextLine().toLowerCase();

        System.out.printf("%-10s %-20s %-20s %-15s %-15s%n",
                "Emp#", "Name", "Position", "Status", "Hourly Rate");

        boolean found = false;
        for (String[] employee : employees) {
            // Skip header row if present
            if (employee[0].equals("Employee #") || employee[0].equalsIgnoreCase("id")) {
                continue;
            }

            // Convert relevant fields to lowercase for case-insensitive search
            String empId = employee[EMP_ID_COL].toLowerCase();
            String lastName = employee[LAST_NAME_COL].toLowerCase();
            String firstName = employee[FIRST_NAME_COL].toLowerCase();
            String middleName = employee.length > 3 && employee[3] != null ? employee[3].toLowerCase() : "";

            // Check if the search term matches any of the fields
            if (empId.contains(searchTerm) ||
                    lastName.contains(searchTerm) ||
                    firstName.contains(searchTerm) ||
                    middleName.contains(searchTerm)) {
                found = true;

                String name = formatEmployeeName(employee);
                String position = employee.length > POSITION_COL ? employee[POSITION_COL] : "N/A";
                String status = employee.length > STATUS_COL ? employee[STATUS_COL] : "N/A";
                double hourlyRate = extractHourlyRate(employee);

                System.out.printf("%-10s %-20s %-20s %-15s %-15.2f%n",
                        employee[EMP_ID_COL], name, position, status, hourlyRate);
            }
        }

        if (!found) {
            System.out.println("No employees found matching your search criteria.");
        }
    }

    /**
     * Displays a formatted list of all employees
     */
    private static void listAllEmployees(List<String[]> employees) {
        System.out.printf("%-10s %-25s %-20s %-15s %-15s%n",
                "Emp#", "Name", "Position", "Status", "Hourly Rate");
        System.out.println("-".repeat(85));

        for (String[] employee : employees) {
            // Format name
            String name = formatEmployeeName(employee);

            // Get position
            String position = employee.length > POSITION_COL ? employee[POSITION_COL] : "N/A";
            if (position != null && position.length() > 18) {
                position = position.substring(0, 15) + "...";
            }

            // Get status
            String status = employee.length > STATUS_COL ? employee[STATUS_COL] : "N/A";

            // Get hourly rate
            double hourlyRate = extractHourlyRate(employee);

            System.out.printf("%-10s %-25s %-20s %-15s %-15.2f%n",
                    employee[EMP_ID_COL], name, position, status, hourlyRate);
        }
    }

    /**
     * Finds an employee record by ID
     */
    private static String[] findEmployeeById(List<String[]> employees, int empNumber) {
        for (String[] employee : employees) {
            try {
                if (Integer.parseInt(employee[EMP_ID_COL]) == empNumber) {
                    return employee;
                }
            } catch (NumberFormatException e) {
                // Skip this record if ID is not a number
                continue;
            }
        }
        return null;
    }

    /**
     * Format employee name
     */
    private static String formatEmployeeName(String[] employee) {
        String firstName = employee[FIRST_NAME_COL].trim();
        String lastName = employee[LAST_NAME_COL].trim();
        return firstName + " " + lastName;
    }

    /**
     * Calculates gross pay details including overtime
     */
    private static Map<String, Double> getGrossPayDetails(List<String[]> attendanceRecords,
            int empNumber,
            double hourlyRate,
            LocalDate startDate,
            LocalDate endDate) {
        Map<String, Double> payDetails = new HashMap<>();
        double totalRegularHours = 0.0;
        double totalOvertimeHours = 0.0;
        double totalRegularPay = 0.0;
        double totalOvertimePay = 0.0;

        // Group records by date to handle overtime on a daily basis (not cumulatively)
        // This ensures overtime is calculated correctly when an employee works
        // more than the regular hours in a single day
        Map<LocalDate, List<String[]>> recordsByDate = new HashMap<>();

        // Group each attendance record by its date for the specified employee
        // This allows us to sum up all hours worked on a specific day
        for (String[] record : attendanceRecords) {
            try {
                if (isRecordForEmployee(record, empNumber)) {
                    LocalDate recordDate = parseFlexibleDate(record[ATT_DATE_COL]);
                    if (recordDate != null && !recordDate.isBefore(startDate) && !recordDate.isAfter(endDate)) {
                        // computeIfAbsent creates a new list if the date isn't already in the map
                        recordsByDate.computeIfAbsent(recordDate, k -> new ArrayList<>()).add(record);
                    }
                }
            } catch (Exception e) {
                // Skip problematic records to ensure the system doesn't crash
                continue;
            }
        }

        // Process each day separately for proper overtime calculation
        // Overtime is calculated only for hours exceeding REGULAR_HOURS_PER_DAY on a single day
        for (Map.Entry<LocalDate, List<String[]>> entry : recordsByDate.entrySet()) {
            double dailyHours = 0.0;

            // Sum up hours for this day
            for (String[] record : entry.getValue()) {
                try {
                    LocalTime timeIn = LocalTime.parse(record[ATT_TIME_IN_COL], TIME_FORMATTER);
                    LocalTime timeOut = LocalTime.parse(record[ATT_TIME_OUT_COL], TIME_FORMATTER);
                    double hours = Duration.between(timeIn, timeOut).toMinutes() / 60.0;
                    dailyHours += hours;
                } catch (Exception e) {
                    // Skip problematic records
                    continue;
                }
            }

            // Calculate pay with overtime
            double regularHours = Math.min(dailyHours, REGULAR_HOURS_PER_DAY);
            double overtimeHours = Math.max(0, dailyHours - REGULAR_HOURS_PER_DAY);

            double regularPay = regularHours * hourlyRate;
            double overtimePay = overtimeHours * hourlyRate * OVERTIME_RATE;

            totalRegularHours += regularHours;
            totalOvertimeHours += overtimeHours;
            totalRegularPay += regularPay;
            totalOvertimePay += overtimePay;
        }

        payDetails.put("regularHours", totalRegularHours);
        payDetails.put("overtimeHours", totalOvertimeHours);
        payDetails.put("regularPay", totalRegularPay);
        payDetails.put("overtimePay", totalOvertimePay);
        payDetails.put("totalPay", totalRegularPay + totalOvertimePay);

        return payDetails;
    }
}