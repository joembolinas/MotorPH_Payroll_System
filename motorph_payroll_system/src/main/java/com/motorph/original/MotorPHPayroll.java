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

public class MotorPHPayroll {

    // Constants
    private static final double OVERTIME_RATE = 1.25; // 25% additional pay for overtime
    private static final int REGULAR_HOURS_PER_DAY = 8;
    private static final int WORK_DAYS_PER_MONTH = 21;
    private static final LocalTime LATE_THRESHOLD = LocalTime.of(8, 10); // 8:10 AM

    // Date and time format constants
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
    private static final int HOURLY_RATE_COL = 18;

    // Attendance record column indices
    private static final int ATT_EMP_ID_COL = 0;
    private static final int ATT_DATE_COL = 3;
    private static final int ATT_TIME_IN_COL = 4;
    private static final int ATT_TIME_OUT_COL = 5;

    private static final Map<Double, Double> SSS_TABLE = initSSSTable();
    private static final PayrollCalculator payrollCalculator = new PayrollCalculator();
    
    // In-memory storage for posted payrolls
    private static final Map<String, Map<String, Object>> postedPayrolls = new HashMap<>();

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
     * Initializes and populates the SSS contribution table with salary brackets and their
     * corresponding contribution amounts. This table is used to determine the appropriate
     * SSS contribution based on an employee's salary.
     *
     * @return A map with salary bracket upper limits as keys and contribution amounts as values
     */
    private static Map<Double, Double> initSSSTable() {
        Map<Double, Double> sssTable = new HashMap<>();
        // Range: 0-4,249.99 = 180.00
        sssTable.put(4250.0, 180.0);
        // Range: 4,250-4,749.99 = 202.50
        sssTable.put(4750.0, 202.5);
        // Range: 4,750-5,249.99 = 225.00
        sssTable.put(5250.0, 225.0);
        // For higher salary brackets, additional entries can be added
        // Range: 5,250-5,749.99 = 247.50
        sssTable.put(5750.0, 247.5);
        // Range: 5,750-6,249.99 = 270.00
        sssTable.put(6250.0, 270.0);
        // Range: 6,250-6,749.99 = 292.50
        sssTable.put(6750.0, 292.5);
        // Range: 6,750-7,249.99 = 315.00
        sssTable.put(7250.0, 315.0);
        // Range: 7,250-7,749.99 = 337.50
        sssTable.put(7750.0, 337.5);
        // Range: 7,750-8,249.99 = 360.00
        sssTable.put(8250.0, 360.0);
        // Range: 8,250-8,749.99 = 382.50
        sssTable.put(8750.0, 382.5);
        // Range: 8,750-9,249.99 = 405.00
        sssTable.put(9250.0, 405.0);
        // Range: 9,250-9,749.99 = 427.50
        sssTable.put(9750.0, 427.5);
        // Range: 9,750-10,249.99 = 450.00
        sssTable.put(10250.0, 450.0);
        // Range: 10,250-10,749.99 = 472.50
        sssTable.put(10750.0, 472.5);
        // Range: 10,750-11,249.99 = 495.00
        sssTable.put(11250.0, 495.0);
        // Adding more brackets to cover higher salaries
        sssTable.put(11750.0, 517.5);  // 11,250-11,749.99
        sssTable.put(12250.0, 540.0);  // 11,750-12,249.99
        sssTable.put(12750.0, 562.5);  // 12,250-12,749.99
        sssTable.put(13250.0, 585.0);  // 12,750-13,249.99
        sssTable.put(13750.0, 607.5);  // 13,250-13,749.99
        sssTable.put(14250.0, 630.0);  // 13,750-14,249.99
        sssTable.put(14750.0, 652.5);  // 14,250-14,749.99
        sssTable.put(15250.0, 675.0);  // 14,750-15,249.99
        sssTable.put(15750.0, 697.5);  // 15,250-15,749.99
        sssTable.put(16250.0, 720.0);  // 15,750-16,249.99
        sssTable.put(16750.0, 742.5);  // 16,250-16,749.99
        sssTable.put(17250.0, 765.0);  // 16,750-17,249.99
        sssTable.put(17750.0, 787.5);  // 17,250-17,749.99
        sssTable.put(18250.0, 810.0);  // 17,750-18,249.99
        sssTable.put(18750.0, 832.5);  // 18,250-18,749.99
        sssTable.put(19250.0, 855.0);  // 18,750-19,249.99
        sssTable.put(19750.0, 877.5);  // 19,250-19,749.99
        sssTable.put(20250.0, 900.0);  // 19,750-20,249.99
        sssTable.put(20750.0, 922.5);  // 20,250-20,749.99
        sssTable.put(Double.MAX_VALUE, 945.0);  // 20,750 and up

        return sssTable;
    }

    /**
     * Loads employee data from a CSV file accessible via the provided URL.
     * This method handles CSV parsing with support for quoted fields and skips the header row.
     * For debugging purposes, the first three employee records are printed to the console.
     *
     * @param url The URL of the CSV file containing employee data
     * @return A list of string arrays, where each array represents an employee record
     * @throws IOException If there's an error accessing or parsing the CSV file
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

                // Properly parse CSV with quoted fields
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
                // Add the last field
                fields.add(sb.toString());
                
                String[] data = fields.toArray(new String[0]);
                if (data.length > 0) { // Ensure the row is not empty
                    employees.add(data);
                    
                    // Debug the first few records to see structure
                    if (employees.size() <= 3) {
                        System.out.println("Debug employee #" + employees.size() + ":");
                        debugEmployeeRecord(data);
                    }
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
    private static void employeeManagement(List<String[]> employees, List<String[]> attendanceRecords, Scanner scanner) {
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

    // Payroll Management Menu
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
                    case 2 -> customPayroll(employees, attendanceRecords, scanner);
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
    
    // Reports Menu
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
                    case 1 -> generatePayslipReport(employees, attendanceRecords, scanner);
                    case 2 -> generateWeeklySummary(employees, attendanceRecords);
                    case 3 -> generateMonthlySummary(employees, attendanceRecords);
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

    // Generate Payroll with better formatting
    private static void generatePayroll(List<String[]> employees, List<String[]> attendanceRecords, Scanner scanner) {
        System.out.println("\nGenerate Payroll:");
        System.out.print("Date From (MM/DD/YYYY): ");
        LocalDate startDate = getDateInput(scanner);
        System.out.print("Date To (MM/DD/YYYY): ");
        LocalDate endDate = getDateInput(scanner);

        System.out.println("═════════════════════════════════════════════════════════════════════════════");
        System.out.printf("%-7s %-25s %-13s %-12s %-15s %-15s%n", 
                "Emp#", "Name", "Hours", "Hourly Rate", "Gross Pay", "Net Pay");
        System.out.println("─────────────────────────────────────────────────────────────────────────────");
        
        List<Map<String, Object>> payrollEntries = new ArrayList<>();
        
        for (String[] employee : employees) {
            int empNumber = Integer.parseInt(employee[0]);
            double totalHours = calculateHoursWorked(attendanceRecords, empNumber, startDate, endDate);
            double hourlyRate = extractHourlyRate(employee);
            double grossPay = totalHours * hourlyRate;
            double netPay = payrollCalculator.calculateNetPay(grossPay);
            
            // Format the name properly without birth date
            String fullName = formatEmployeeName(employee);
            
            // Format numbers with commas for thousands
            System.out.printf("%-7s %-25s %13.2f %12.2f %15s %15s%n", 
                    employee[0], 
                    fullName, 
                    totalHours, 
                    hourlyRate, 
                    String.format("%,.2f", grossPay), 
                    String.format("%,.2f", netPay));
            
            // Store payroll entry for potential posting
            Map<String, Object> entry = new HashMap<>();
            entry.put("empNumber", empNumber);
            entry.put("name", fullName);
            entry.put("totalHours", totalHours);
            entry.put("hourlyRate", hourlyRate);
            entry.put("grossPay", grossPay);
            entry.put("netPay", netPay);
            entry.put("startDate", startDate);
            entry.put("endDate", endDate);
            payrollEntries.add(entry);
        }
        
        System.out.println("═════════════════════════════════════════════════════════════════════════════");
        
        System.out.println("\n1. Post Payroll");
        System.out.println("2. Edit Payroll");
        System.out.print("Enter your choice: ");
        
        try {
            int choice = Integer.parseInt(scanner.nextLine());
            if (choice == 1) {
                // Post all payroll entries
                for (Map<String, Object> entry : payrollEntries) {
                    int empNumber = (int) entry.get("empNumber");
                    String key = empNumber + "_" + entry.get("startDate") + "_" + entry.get("endDate");
                    postedPayrolls.put(key, entry);
                }
                System.out.println("Payroll posted successfully.");
            } else if (choice == 2) {
                System.out.println("Edit payroll functionality not yet implemented.");
            } else {
                System.out.println("Invalid choice.");
            }
        } catch (NumberFormatException e) {
            System.err.println("Invalid input. Please enter a number.");
        }
    }
    
    // Custom Payroll for Specific Employee
    private static void customPayroll(List<String[]> employees, List<String[]> attendanceRecords, Scanner scanner) {
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
        
        System.out.print("Date From (MM/DD/YYYY): ");
        LocalDate startDate = getDateInput(scanner);
        System.out.print("Date To (MM/DD/YYYY): ");
        LocalDate endDate = getDateInput(scanner);
        
        double totalHours = calculateHoursWorked(attendanceRecords, empNumber, startDate, endDate);
        double hourlyRate = extractHourlyRate(employee);
        double grossPay = totalHours * hourlyRate;
        double netPay = payrollCalculator.calculateNetPay(grossPay);
        
        String fullName = formatEmployeeName(employee);
        
        System.out.println("\nEmployee No: " + empNumber + " | Name: " + fullName);
        System.out.printf("Total Work Hours: %.2f | Gross Pay: %.2f | Net Pay: %.2f%n", 
                totalHours, grossPay, netPay);
        
        System.out.println("\n1. Post Payroll");
        System.out.println("2. Edit Payroll");
        System.out.print("Enter your choice: ");
        
        try {
            int choice = Integer.parseInt(scanner.nextLine());
            if (choice == 1) {
                // Post this payroll entry
                Map<String, Object> entry = new HashMap<>();
                entry.put("empNumber", empNumber);
                entry.put("name", fullName);
                entry.put("totalHours", totalHours);
                entry.put("hourlyRate", hourlyRate);
                entry.put("grossPay", grossPay);
                entry.put("netPay", netPay);
                entry.put("startDate", startDate);
                entry.put("endDate", endDate);
                
                String key = empNumber + "_" + startDate + "_" + endDate;
                postedPayrolls.put(key, entry);
                System.out.println("Payroll posted successfully.");
            } else if (choice == 2) {
                System.out.println("Edit payroll functionality not yet implemented.");
            } else {
                System.out.println("Invalid choice.");
            }
        } catch (NumberFormatException e) {
            System.err.println("Invalid input. Please enter a number.");
        }
    }
    
    // Generate Payslip Report
    private static void generatePayslipReport(List<String[]> employees, List<String[]> attendanceRecords, Scanner scanner) {
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
        
        System.out.print("Date From (MM/DD/YYYY): ");
        LocalDate startDate = getDateInput(scanner);
        System.out.print("Date To (MM/DD/YYYY): ");
        LocalDate endDate = getDateInput(scanner);
        
        // Check if payslip exists in posted payrolls
        String key = empNumber + "_" + startDate + "_" + endDate;
        Map<String, Object> payrollEntry = postedPayrolls.get(key);
        
        if (payrollEntry != null) {
            // Use the posted payroll information
            System.out.println("\nPayslip:");
            System.out.println("Employee No: " + empNumber + " | Name: " + payrollEntry.get("name"));
            System.out.printf("Total Work Hours: %.2f | Gross Pay: %.2f | Net Pay: %.2f%n", 
                    (double) payrollEntry.get("totalHours"), 
                    (double) payrollEntry.get("grossPay"), 
                    (double) payrollEntry.get("netPay"));
        } else {
            // Calculate on the fly
            double totalHours = calculateHoursWorked(attendanceRecords, empNumber, startDate, endDate);
            double hourlyRate = extractHourlyRate(employee);
            double grossPay = totalHours * hourlyRate;
            double netPay = payrollCalculator.calculateNetPay(grossPay);
            
            String fullName = formatEmployeeName(employee);
            
            System.out.println("\nPayslip:");
            System.out.println("Employee No: " + empNumber + " | Name: " + fullName);
            System.out.printf("Total Work Hours: %.2f | Gross Pay: %.2f | Net Pay: %.2f%n", 
                    totalHours, grossPay, netPay);
        }
    }
    
    // Generate Weekly Summary Report
    private static void generateWeeklySummary(List<String[]> employees, List<String[]> attendanceRecords) {
        System.out.println("\nWeekly Summary Report:");
        System.out.printf("%-10s %-20s %-15s %-10s %-10s%n", 
                "Emp#", "Name", "Total Work Hours", "Net Pay", "Gross Pay");
        
        // In a real implementation, we would iterate through posted payrolls for the week
        // For now, we'll show sample data based on the expected output
        for (String[] employee : employees) {
            int empNumber = Integer.parseInt(employee[0]);
            String fullName = formatEmployeeName(employee);
            
            // Sample weekly hours and pay - in a real implementation, you would calculate this
            // based on the actual attendance records for the current week
            double totalHours = 0.0;
            double netPay = 0.0;
            double grossPay = 0.0;
            
            // Check if this employee has any posted payrolls
            for (Map.Entry<String, Map<String, Object>> entry : postedPayrolls.entrySet()) {
                Map<String, Object> payroll = entry.getValue();
                if ((int) payroll.get("empNumber") == empNumber) {
                    totalHours += (double) payroll.get("totalHours");
                    netPay += (double) payroll.get("netPay");
                    grossPay += (double) payroll.get("grossPay");
                }
            }
            
            System.out.printf("%-10s %-20s %-15.2f %-10.2f %-10.2f%n", 
                    employee[0], fullName, totalHours, netPay, grossPay);
        }
    }
    
    // Generate Monthly Summary Report
    private static void generateMonthlySummary(List<String[]> employees, List<String[]> attendanceRecords) {
        System.out.println("\nMonthly Summary Report:");
        System.out.printf("%-10s %-20s %-15s %-10s %-10s%n", 
                "Emp#", "Name", "Total Work Hours", "Net Pay", "Gross Pay");
        
        // Similar to weekly summary, but for the entire month
        for (String[] employee : employees) {
            int empNumber = Integer.parseInt(employee[0]);
            String fullName = formatEmployeeName(employee);
            
            // Sample monthly hours and pay
            double totalHours = 0.0;
            double netPay = 0.0;
            double grossPay = 0.0;
            
            // Check if this employee has any posted payrolls
            for (Map.Entry<String, Map<String, Object>> entry : postedPayrolls.entrySet()) {
                Map<String, Object> payroll = entry.getValue();
                if ((int) payroll.get("empNumber") == empNumber) {
                    totalHours += (double) payroll.get("totalHours");
                    netPay += (double) payroll.get("netPay");
                    grossPay += (double) payroll.get("grossPay");
                }
            }
            
            // Multiply weekly values by 4 for a rough monthly estimate if no data exists
            if (totalHours == 0) {
                totalHours = 160.0;  // Approx. monthly hours
                // Calculate based on hourly rate
                double hourlyRate = extractHourlyRate(employee);
                grossPay = totalHours * hourlyRate;
                netPay = payrollCalculator.calculateNetPay(grossPay);
            }
            
            System.out.printf("%-10s %-20s %-15.2f %-10.2f %-10.2f%n", 
                    employee[0], fullName, totalHours, netPay, grossPay);
        }
    }
    
    // View Attendance Records
    private static void viewAttendance(List<String[]> employees, List<String[]> attendanceRecords, Scanner scanner) {
        System.out.print("\nEnter Employee No: ");
        int empNumber;
        try {
            empNumber = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid employee number. Please enter a numeric value.");
            return;
        }
        
        System.out.print("Date From (MM/DD/YYYY): ");
        LocalDate startDate = getDateInput(scanner);
        System.out.print("Date To (MM/DD/YYYY): ");
        LocalDate endDate = getDateInput(scanner);
        
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("H:mm");
        
        System.out.printf("%-10s | %-6s | %-6s | %-9s | %-10s%n", 
                "Date", "In", "Out", "Duration", "Remarks");
        
        boolean found = false;
        for (String[] record : attendanceRecords) {
            if (Integer.parseInt(record[EMP_ID_COL]) == empNumber) {
                try {
                    LocalDate recordDate = LocalDate.parse(record[ATT_DATE_COL], DATE_FORMATTER);
                    if (!recordDate.isBefore(startDate) && !recordDate.isAfter(endDate)) {
                        found = true;
                        LocalTime timeIn = LocalTime.parse(record[ATT_TIME_IN_COL], timeFormatter);
                        LocalTime timeOut = LocalTime.parse(record[ATT_TIME_OUT_COL], timeFormatter);
                        
                        double duration = Duration.between(timeIn, timeOut).toMinutes() / 60.0;
                        String remarks = timeIn.isBefore(LATE_THRESHOLD) ? "On Time" : "Late";
                        
                        System.out.printf("%-10s | %-6s | %-6s | %-9.2f | %-10s%n", 
                                recordDate.format(dateFormatter), 
                                timeIn.format(timeFormatter), 
                                timeOut.format(timeFormatter), 
                                duration, 
                                remarks);
                    }
                } catch (DateTimeParseException e) {
                    System.err.println("Error parsing date/time for employee " + empNumber + ": " + e.getMessage());
                }
            }
        }
        
        if (!found) {
            System.out.println("No attendance records found for the specified period.");
        }
    }

    /**
     * Calculates the total hours worked by an employee within a specified date range.
     * @param attendanceRecords List of attendance records
     * @param empNumber The employee's ID number
     * @param startDate The beginning of the date range
     * @param endDate The end of the date range
     * @return Total hours worked
     */
    private static double calculateHoursWorked(List<String[]> attendanceRecords, int empNumber,
            LocalDate startDate, LocalDate endDate) {
        double totalHours = 0;
        
        for (String[] record : attendanceRecords) {
            try {
                if (isRecordForEmployee(record, empNumber) && isRecordInDateRange(record, startDate, endDate)) {
                    totalHours += calculateHoursForRecord(record);
                }
            } catch (Exception e) {
                System.err.println("Error processing attendance record: " + e.getMessage());
            }
        }
        
        return totalHours;
    }

    /**
     * Checks if an attendance record belongs to the specified employee.
     * @param record The attendance record
     * @param empNumber The employee ID to check for
     * @return true if the record belongs to the employee, false otherwise
     */
    private static boolean isRecordForEmployee(String[] record, int empNumber) {
        try {
            return Integer.parseInt(record[ATT_EMP_ID_COL]) == empNumber;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Checks if an attendance record falls within the specified date range.
     * @param record The attendance record
     * @param startDate The start date of the range (inclusive)
     * @param endDate The end date of the range (inclusive)
     * @return true if the record is within the date range, false otherwise
     */
    private static boolean isRecordInDateRange(String[] record, LocalDate startDate, LocalDate endDate) {
        try {
            LocalDate recordDate = LocalDate.parse(record[ATT_DATE_COL], DATE_FORMATTER);
            return !recordDate.isBefore(startDate) && !recordDate.isAfter(endDate);
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    /**
     * Calculates the hours worked for a single attendance record.
     * @param record The attendance record
     * @return Hours worked for this record
     */
    private static double calculateHoursForRecord(String[] record) {
        try {
            LocalTime timeIn = LocalTime.parse(record[ATT_TIME_IN_COL], TIME_FORMATTER);
            LocalTime timeOut = LocalTime.parse(record[ATT_TIME_OUT_COL], TIME_FORMATTER);
            
            // Calculate duration in hours
            Duration duration = Duration.between(timeIn, timeOut);
            return duration.toMinutes() / 60.0;
        } catch (DateTimeParseException e) {
            System.err.println("Error parsing time: " + e.getMessage());
            return 0.0;
        }
    }

    /**
     * Extracts or calculates the hourly rate for an employee.
     * @param employee The employee record
     * @return The hourly rate
     */
    private static double extractHourlyRate(String[] employee) {
        // Try different methods in sequence
        double rate = extractDirectHourlyRate(employee);
        if (rate > 0) return rate;
        
        rate = calculateRateFromBasicSalary(employee);
        if (rate > 0) return rate;
        
        return getDefaultRateByPosition(employee);
    }

    /**
     * Attempts to extract the direct hourly rate from the employee record.
     * @param employee The employee record
     * @return The extracted hourly rate, or 0 if not available
     */
    private static double extractDirectHourlyRate(String[] employee) {
        try {
            if (employee.length > HOURLY_RATE_COL && employee[HOURLY_RATE_COL] != null) {
                String rateString = employee[HOURLY_RATE_COL].replaceAll("[^0-9.]", "").trim();
                if (!rateString.isEmpty()) {
                    double rate = Double.parseDouble(rateString);
                    if (rate > 0) return rate;
                }
            }
            return 0.0;
        } catch (Exception e) {
            return 0.0;
        }
    }

    /**
     * Calculates hourly rate from the basic salary.
     * @param employee The employee record
     * @return The calculated hourly rate, or 0 if not available
     */
    private static double calculateRateFromBasicSalary(String[] employee) {
        try {
            if (employee.length > BASIC_SALARY_COL && employee[BASIC_SALARY_COL] != null) {
                String salaryString = employee[BASIC_SALARY_COL].replaceAll("[^0-9.]", "").trim();
                if (!salaryString.isEmpty()) {
                    double basicSalary = Double.parseDouble(salaryString);
                    return (basicSalary / WORK_DAYS_PER_MONTH) / REGULAR_HOURS_PER_DAY;
                }
            }
            return 0.0;
        } catch (Exception e) {
            return 0.0;
        }
    }

    /**
     * Gets a default rate based on the employee's position.
     * @param employee The employee record
     * @return A default hourly rate based on position
     */
    private static double getDefaultRateByPosition(String[] employee) {
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
            return 133.93; // Default rate for regular employees
        } catch (Exception e) {
            return 133.93;
        }
    }

    // Get Date Input
    private static LocalDate getDateInput(Scanner scanner) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        while (true) {
            try {
                String input = scanner.nextLine();
                return LocalDate.parse(input, formatter);
            } catch (DateTimeParseException e) {
                System.err.println("Invalid date format. Please use MM/DD/YYYY format.");
                System.out.print("Enter date again: ");
            }
        }
    }

    // Search Employee
    private static void searchEmployee(List<String[]> employees, Scanner scanner) {
        System.out.print("\nEnter search term (name or employee number): ");
        String searchTerm = scanner.nextLine().toLowerCase();
        
        System.out.printf("%-10s %-20s %-20s %-15s %-15s%n", 
                "Emp#", "Name", "Position", "Status", "Hourly Rate");
        
        boolean found = false;
        for (String[] employee : employees) {
            if (employee[0].contains(searchTerm) || 
                employee[1].toLowerCase().contains(searchTerm) || 
                employee[2].toLowerCase().contains(searchTerm)) {
                
                found = true;
                String fullName = formatEmployeeName(employee);
                
                System.out.printf("%-10s %-20s %-20s %-15s %-15.2f%n", 
                        employee[0], 
                        fullName,
                        employee.length > 8 ? employee[8] : "N/A",
                        employee.length > 10 ? employee[10] : "N/A",
                        extractHourlyRate(employee));
            }
        }
        
        if (!found) {
            System.out.println("No employees found matching your search criteria.");
        }
    }

    // List All Employees with correct column mapping
    private static void listAllEmployees(List<String[]> employees) {
        System.out.printf("%-10s %-25s %-20s %-15s %-15s%n",
                "Emp#", "Name", "Position", "Status", "Hourly Rate");
        System.out.println("-".repeat(85));
        
        for (String[] employee : employees) {
            // Format name using the fixed formatEmployeeName method
            String name = formatEmployeeName(employee);
            
            // Get position (column 11)
            String position = employee.length > 11 ? employee[11] : "N/A";
            if (position != null && position.length() > 18) {
                position = position.substring(0, 15) + "...";
            }
            
            // Get status (column 10)
            String status = employee.length > 10 ? employee[10] : "N/A";
            
            // Get hourly rate (column 18)
            double hourlyRate = extractHourlyRate(employee);
            
            System.out.printf("%-10s %-25s %-20s %-15s %-15.2f%n", 
                    employee[0], name, position, status, hourlyRate);
        }
    }

    // Find Employee by ID
    private static String[] findEmployeeById(List<String[]> employees, int empNumber) {
        for (String[] employee : employees) {
            try {
                if (Integer.parseInt(employee[0]) == empNumber) {
                    return employee;
                }
            } catch (NumberFormatException e) {
                System.err.println("Error parsing employee number: " + e.getMessage());
            }
        }
        return null;
    }

    // Improved employee name formatting - remove birth date information
    private static String formatEmployeeName(String[] employee) {
        // Only use the first and last name fields, ignore date fields
        String firstName = employee[2].trim();
        String lastName = employee[1].trim();
        
        
        // Check if there's a middle name/suffix and add it if present
        if (employee.length > 3 && employee[3] != null && !employee[3].trim().isEmpty()) {
        }
        
        return firstName + " " +  lastName;
    }

    // Add this debugging method to print the structure of an employee record
    private static void debugEmployeeRecord(String[] employee) {
        System.out.println("Employee record has " + employee.length + " columns");
        for (int i = 0; i < employee.length; i++) {
            System.out.println("Column " + i + ": " + employee[i]);
        }
        // Try to parse the hourly rate specifically
        if (employee.length > 18) {
            System.out.println("Attempting to parse hourly rate from column 18: " + employee[18]);
            try {
                String rateString = employee[18].replaceAll("[^0-9.]", "").trim();
                double rate = Double.parseDouble(rateString);
                System.out.println("Successfully parsed rate: " + rate);
            } catch (Exception e) {
                System.out.println("Failed to parse rate: " + e.getMessage());
            }
        } else {
            System.out.println("Employee record doesn't have column 18 (hourly rate)");
        }
    }
}