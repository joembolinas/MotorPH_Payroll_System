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
 * employee information, calculating payroll, and generating reports for MotorPH company.
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
    /**
     * Pay rate constants used for calculating overtime and determining
     * standard working hours and days
     */
    private static final double OVERTIME_RATE = 1.25; // 25% additional pay for overtime
    private static final int REGULAR_HOURS_PER_DAY = 8;
    private static final int WORK_DAYS_PER_MONTH = 21;
    private static final LocalTime LATE_THRESHOLD = LocalTime.of(8, 10); // 8:10 AM

    /**
     * Date and time format constants for standardized parsing and display
     * of dates and times throughout the application
     */
    private static final String DATE_FORMAT_PATTERN = "MM/dd/yyyy";
    private static final String TIME_FORMAT_PATTERN = "H:mm";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT_PATTERN);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern(TIME_FORMAT_PATTERN);

    /**
     * Employee data column indices for accessing specific information
     * from the employee data arrays. These map to the columns in the
     * employee CSV file.
     */
    private static final int EMP_ID_COL = 0;
    private static final int LAST_NAME_COL = 1;
    private static final int FIRST_NAME_COL = 2;
    private static final int POSITION_COL = 11;
    private static final int STATUS_COL = 10;
    private static final int BASIC_SALARY_COL = 13;
    private static final int HOURLY_RATE_COL = 18;

    /**
     * Employee allowance column indices
     */
    private static final int RICE_SUBSIDY_COL = 14;
    private static final int PHONE_ALLOWANCE_COL = 15;
    private static final int CLOTHING_ALLOWANCE_COL = 16;

    /**
     * Attendance record column indices for accessing specific information
     * from the attendance data arrays. These map to the columns in the
     * attendance CSV file.
     */
    private static final int ATT_EMP_ID_COL = 0;
    private static final int ATT_DATE_COL = 3;
    private static final int ATT_TIME_IN_COL = 4;
    private static final int ATT_TIME_OUT_COL = 5;

    /**
     * The SSS (Social Security System) contribution table mapping salary brackets
     * to corresponding contribution amounts
     */
    private static final Map<Double, Double> SSS_TABLE = initSSSTable();
    
    /**
     * PayrollCalculator instance for handling all payroll calculation logic
     */
    private static final PayrollCalculator payrollCalculator = new PayrollCalculator();
    
    /**
     * In-memory storage for posted payrolls, indexed by a composite key of:
     * "employeeId_startDate_endDate"
     */
    private static final Map<String, Map<String, Object>> postedPayrolls = new HashMap<>();

    /**
     * Main entry point for the MotorPH Payroll System.
     * Initializes the application, loads data from online sources,
     * and presents the main menu for user interaction.
     *
     * @param args Command line arguments (not used)
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
            System.err.println("Error loading data: " + e.getMessage()); // Print error message
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

    /**
     * Loads attendance data from a CSV file accessible via the provided URL.
     * This method parses the CSV, skipping the header row, and creates an array
     * of attendance records for processing.
     *
     * Unlike the employee data loader, this method uses simple comma splitting
     * as attendance data typically doesn't contain quoted fields.
     *
     * @param url The URL of the CSV file containing attendance data
     * @return A list of string arrays, where each array represents an attendance record
     * @throws IOException If there's an error accessing or parsing the CSV file or if no valid records are found
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

    /**
     * Displays the employee management menu and handles user interactions for
     * employee-related operations. This includes searching for employees,
     * listing all employees, and viewing attendance records.
     *
     * @param employees The list of employee records
     * @param attendanceRecords The list of attendance records
     * @param scanner Scanner for reading user input
     */
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

    /**
     * Displays the payroll management menu and handles user interactions for
     * payroll-related operations. This includes generating payroll for all employees
     * and creating custom payroll for specific employees.
     *
     * @param employees The list of employee records
     * @param attendanceRecords The list of attendance records
     * @param scanner Scanner for reading user input
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
    
    /**
     * Displays the reports menu and handles user interactions for
     * report generation. This includes payslips, weekly summaries,
     * and monthly summaries.
     *
     * @param employees The list of employee records
     * @param attendanceRecords The list of attendance records
     * @param scanner Scanner for reading user input
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

    /**
     * Generates payroll for all employees within a specified date range.
     * Calculates hours worked, gross pay, and net pay for each employee and
     * displays a formatted payroll report. Also provides options to post
     * or edit the payroll.
     *
     * @param employees The list of employee records
     * @param attendanceRecords The list of attendance records
     * @param scanner Scanner for reading user input
     */
    private static void generatePayroll(List<String[]> employees, List<String[]> attendanceRecords, Scanner scanner) {
        System.out.println("\nGenerate Payroll:");
        System.out.print("Date From (MM/DD/YYYY): ");
        LocalDate startDate = getDateInput(scanner);
        System.out.print("Date To (MM/DD/YYYY): ");
        LocalDate endDate = getDateInput(scanner);

        System.out.println("═════════════════════════════════════════════════════════════════════════════════════════════════════════════");
        System.out.printf("%-7s %-25s %-10s %-10s %-12s %-15s %-15s %-15s%n", 
                "Emp#", "Name", "Reg Hours", "OT Hours", "Hourly Rate", "Gross Pay", "Allowances", "Net Pay");
        System.out.println("─────────────────────────────────────────────────────────────────────────────────────────────────────────────");
        
        List<Map<String, Object>> payrollEntries = new ArrayList<>();
        
        for (String[] employee : employees) {
            int empNumber = Integer.parseInt(employee[0]);
            double hourlyRate = extractHourlyRate(employee);
            
            // Calculate with new methods including overtime
            Map<String, Double> payDetails = getGrossPayDetails(attendanceRecords, empNumber, hourlyRate, startDate, endDate);
            double regularHours = payDetails.get("regularHours");
            double overtimeHours = payDetails.get("overtimeHours");
            double grossPay = payDetails.get("totalPay");
            
            // Get pro-rated allowances
            Map<String, Double> allowanceDetails = getProRatedAllowanceDetails(employee, startDate, endDate);
            double totalAllowances = allowanceDetails.get("totalAllowances");
            double workingDays = allowanceDetails.get("workingDays");
            
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
            
            // Store payroll entry for potential posting
            Map<String, Object> entry = new HashMap<>();
            entry.put("empNumber", empNumber);
            entry.put("name", fullName);
            entry.put("regularHours", regularHours);
            entry.put("overtimeHours", overtimeHours);
            entry.put("totalHours", regularHours + overtimeHours);
            entry.put("hourlyRate", hourlyRate);
            entry.put("grossPay", grossPay);
            entry.put("sumAfterDeductions", sumAfterDeductions);
            entry.put("totalAllowances", totalAllowances);
            entry.put("netPay", netPay);
            entry.put("startDate", startDate);
            entry.put("endDate", endDate);
            entry.put("workingDays", workingDays);
            payrollEntries.add(entry);
        }
        
        System.out.println("═════════════════════════════════════════════════════════════════════════════════════════════════════════════");
        
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
    
    /**
     * Generates a custom payroll report for a specific employee within a date range.
     * Prompts the user for employee number and date range, then calculates and displays
     * the hours worked, gross pay, and net pay for that employee.
     *
     * @param employees The list of employee records
     * @param attendanceRecords The list of attendance records
     * @param scanner Scanner for reading user input
     */
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
        
        double hourlyRate = extractHourlyRate(employee);
        
        // Get detailed pay breakdown with overtime
        Map<String, Double> payDetails = getGrossPayDetails(attendanceRecords, empNumber, hourlyRate, startDate, endDate);
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
        System.out.println("           EMPLOYEE PAYSLIP");
        System.out.println("═══════════════════════════════════════════");
        System.out.println("Employee No: " + empNumber);
        System.out.println("Name: " + fullName);
        System.out.println("Position: " + (employee.length > POSITION_COL ? employee[POSITION_COL] : "N/A"));
        System.out.println("Period: " + startDate.format(DATE_FORMATTER) + " to " + endDate.format(DATE_FORMATTER));
        System.out.println("Working Days: " + (int)workingDays + " of " + WORK_DAYS_PER_MONTH + " days");
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
        System.out.println("ALLOWANCES (Pro-rated for " + (int)workingDays + " days):");
        System.out.printf("Rice Subsidy: ₱%.2f\n", riceSubsidy);
        System.out.printf("Phone Allowance: ₱%.2f\n", phoneAllowance);
        System.out.printf("Clothing Allowance: ₱%.2f\n", clothingAllowance);
        System.out.printf("Total Allowances: ₱%.2f\n", totalAllowances);
        System.out.println("───────────────────────────────────────────");
        System.out.printf("FINAL NET PAY: ₱%.2f\n", netPay);
        System.out.println("═══════════════════════════════════════════");
        
        System.out.println("\n1. Post Payroll");
        System.out.println("2. Edit Payroll");
        System.out.print("Enter your choice: ");
        
        try {
            int choice = Integer.parseInt(scanner.nextLine());
            if (choice == 1) {
                // Post this payroll entry with detailed breakdown
                Map<String, Object> entry = new HashMap<>();
                entry.put("empNumber", empNumber);
                entry.put("name", fullName);
                entry.put("totalHours", regularHours + overtimeHours);
                entry.put("regularHours", regularHours);
                entry.put("overtimeHours", overtimeHours);
                entry.put("hourlyRate", hourlyRate);
                entry.put("regularPay", regularPay);
                entry.put("overtimePay", overtimePay);
                entry.put("grossPay", grossPay);
                entry.put("sumAfterDeductions", sumAfterDeductions);
                entry.put("riceSubsidy", riceSubsidy);
                entry.put("phoneAllowance", phoneAllowance);
                entry.put("clothingAllowance", clothingAllowance);
                entry.put("totalAllowances", totalAllowances);
                entry.put("netPay", netPay);
                entry.put("startDate", startDate);
                entry.put("endDate", endDate);
                entry.put("workingDays", workingDays);
                
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
    
    /**
     * Generates a payslip report for a specific employee within a date range.
     * If a payroll has already been posted for this employee and date range,
     * it uses the posted information. Otherwise, it calculates the payroll
     * information on the fly.
     *
     * @param employees The list of employee records
     * @param attendanceRecords The list of attendance records
     * @param scanner Scanner for reading user input
     */
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
            // Use the posted payroll information with full details
            System.out.println("\n═══════════════════════════════════════════");
            System.out.println("           EMPLOYEE PAYSLIP");
            System.out.println("═══════════════════════════════════════════");
            System.out.println("Employee No: " + empNumber);
            System.out.println("Name: " + payrollEntry.get("name"));
            System.out.println("Position: " + (employee.length > POSITION_COL ? employee[POSITION_COL] : "N/A"));
            System.out.println("Period: " + startDate.format(DATE_FORMATTER) + " to " + endDate.format(DATE_FORMATTER));
            System.out.println("Working Days: " + (int)(double)payrollEntry.get("workingDays") + " of " + WORK_DAYS_PER_MONTH + " days");
            System.out.println("───────────────────────────────────────────");
            System.out.println("HOURS WORKED:");
            System.out.printf("Regular Hours: %.2f\n", (double)payrollEntry.get("regularHours"));
            System.out.printf("Overtime Hours: %.2f\n", (double)payrollEntry.get("overtimeHours"));
            System.out.printf("Total Hours: %.2f\n", (double)payrollEntry.get("totalHours"));
            System.out.println("───────────────────────────────────────────");
            System.out.println("PAY DETAILS:");
            System.out.printf("Hourly Rate: ₱%.2f\n", (double)payrollEntry.get("hourlyRate"));
            System.out.printf("Regular Pay: ₱%.2f\n", (double)payrollEntry.get("regularPay"));
            System.out.printf("Overtime Pay: ₱%.2f\n", (double)payrollEntry.get("overtimePay"));
            System.out.printf("Gross Pay: ₱%.2f\n", (double)payrollEntry.get("grossPay"));
            System.out.println("───────────────────────────────────────────");
            
            double grossPay = (double)payrollEntry.get("grossPay");
            double sumAfterDeductions = (double)payrollEntry.get("sumAfterDeductions");
            
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
            
            System.out.println("ALLOWANCES (Pro-rated for " + (int)(double)payrollEntry.get("workingDays") + " days):");
            System.out.printf("Rice Subsidy: ₱%.2f\n", (double)payrollEntry.get("riceSubsidy"));
            System.out.printf("Phone Allowance: ₱%.2f\n", (double)payrollEntry.get("phoneAllowance"));
            System.out.printf("Clothing Allowance: ₱%.2f\n", (double)payrollEntry.get("clothingAllowance"));
            System.out.printf("Total Allowances: ₱%.2f\n", (double)payrollEntry.get("totalAllowances"));
            System.out.println("───────────────────────────────────────────");
            System.out.printf("FINAL NET PAY: ₱%.2f\n", (double)payrollEntry.get("netPay"));
            System.out.println("═══════════════════════════════════════════");
        } else {
            // Calculate on the fly with full details
            double hourlyRate = extractHourlyRate(employee);
            
            // Get detailed pay breakdown with overtime
            Map<String, Double> payDetails = getGrossPayDetails(attendanceRecords, empNumber, hourlyRate, startDate, endDate);
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
            System.out.println("           EMPLOYEE PAYSLIP");
            System.out.println("═══════════════════════════════════════════");
            System.out.println("Employee No: " + empNumber);
            System.out.println("Name: " + fullName);
            System.out.println("Position: " + (employee.length > POSITION_COL ? employee[POSITION_COL] : "N/A"));
            System.out.println("Period: " + startDate.format(DATE_FORMATTER) + " to " + endDate.format(DATE_FORMATTER));
            System.out.println("Working Days: " + (int)workingDays + " of " + WORK_DAYS_PER_MONTH + " days");
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
            System.out.println("ALLOWANCES (Pro-rated for " + (int)workingDays + " days):");
            System.out.printf("Rice Subsidy: ₱%.2f\n", riceSubsidy);
            System.out.printf("Phone Allowance: ₱%.2f\n", phoneAllowance);
            System.out.printf("Clothing Allowance: ₱%.2f\n", clothingAllowance);
            System.out.printf("Total Allowances: ₱%.2f\n", totalAllowances);
            System.out.println("───────────────────────────────────────────");
            System.out.printf("FINAL NET PAY: ₱%.2f\n", netPay);
            System.out.println("═══════════════════════════════════════════");
            
            // Offer to save this calculation as a posted payroll
            System.out.println("\nWould you like to save this payslip? (y/n): ");
            String saveChoice = scanner.nextLine().trim().toLowerCase();
            if (saveChoice.equals("y") || saveChoice.equals("yes")) {
                // Create payroll entry
                Map<String, Object> entry = new HashMap<>();
                entry.put("empNumber", empNumber);
                entry.put("name", fullName);
                entry.put("totalHours", regularHours + overtimeHours);
                entry.put("regularHours", regularHours);
                entry.put("overtimeHours", overtimeHours);
                entry.put("hourlyRate", hourlyRate);
                entry.put("regularPay", regularPay);
                entry.put("overtimePay", overtimePay);
                entry.put("grossPay", grossPay);
                entry.put("sumAfterDeductions", sumAfterDeductions);
                entry.put("riceSubsidy", riceSubsidy);
                entry.put("phoneAllowance", phoneAllowance);
                entry.put("clothingAllowance", clothingAllowance);
                entry.put("totalAllowances", totalAllowances);
                entry.put("netPay", netPay);
                entry.put("startDate", startDate);
                entry.put("endDate", endDate);
                entry.put("workingDays", workingDays);
                
                postedPayrolls.put(key, entry);
                System.out.println("Payslip saved successfully.");
            }
        }
    }
    
    /**
     * Generates a weekly summary report for all employees.
     * Shows employee number, name, total work hours, net pay, and gross pay
     * based on posted payroll data or estimated values if no posted data exists.
     *
     * @param employees The list of employee records
     * @param attendanceRecords The list of attendance records
     */
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
    
    /**
     * Generates a monthly summary report for all employees.
     * Shows employee number, name, total work hours, net pay, and gross pay
     * based on posted payroll data or estimated values if no posted data exists.
     * If no posted data exists, the system uses an approximation of 160 hours
     * per month for the calculation.
     *
     * @param employees The list of employee records
     * @param attendanceRecords The list of attendance records
     */
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
    
    /**
     * Displays attendance records for a specific employee within a date range.
     * Shows the date, time in, time out, duration (hours worked), and remarks
     * (whether the employee was on time or late) for each day in the range.
     *
     * An employee is considered late if they logged in after 8:10 AM.
     *
     * @param employees The list of employee records
     * @param attendanceRecords The list of attendance records
     * @param scanner Scanner for reading user input
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
        
        System.out.print("Date From (MM/DD/YYYY): ");
        LocalDate startDate = getDateInput(scanner);
        System.out.print("Date To (MM/DD/YYYY): ");
        LocalDate endDate = getDateInput(scanner);
        
        DateTimeFormatter outputDateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        
        System.out.printf("%-10s | %-6s | %-6s | %-9s | %-10s%n", 
                "Date", "In", "Out", "Duration", "Remarks");
        
        boolean found = false;
        for (String[] record : attendanceRecords) {
            if (isRecordForEmployee(record, empNumber)) {
                try {
                    // Use our flexible date parser
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
                    // Silently skip problematic records instead of printing errors
                    continue;
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
                if (isRecordForEmployee(record, empNumber)) {
                    LocalDate recordDate = parseFlexibleDate(record[ATT_DATE_COL]);
                    
                    if (recordDate != null && !recordDate.isBefore(startDate) && !recordDate.isAfter(endDate)) {
                        totalHours += calculateHoursForRecord(record);
                    }
                }
            } catch (Exception e) {
                // Silently skip problematic records
                continue;
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
     * This method handles multiple date formats to ensure compatibility with various CSV sources.
     * 
     * @param record The attendance record
     * @param startDate The start date of the range (inclusive)
     * @param endDate The end date of the range (inclusive)
     * @return true if the record is within the date range, false otherwise
     */
    private static boolean isRecordInDateRange(String[] record, LocalDate startDate, LocalDate endDate) {
        try {
            String dateStr = record[ATT_DATE_COL];
            LocalDate recordDate = parseFlexibleDate(dateStr);
            
            if (recordDate != null) {
                return !recordDate.isBefore(startDate) && !recordDate.isAfter(endDate);
            }
            return false;
        } catch (Exception e) {
            // Silently handle any other parsing errors by excluding the record
            return false;
        }
    }

    /**
     * Helper method to parse dates in multiple formats.
     * Tries different common date formats to maximize compatibility.
     * 
     * @param dateStr The date string to parse
     * @return The parsed LocalDate, or null if parsing fails
     */
    private static LocalDate parseFlexibleDate(String dateStr) {
        // Define multiple formatters for different date patterns
        DateTimeFormatter[] formatters = {
            DateTimeFormatter.ofPattern("MM/dd/yyyy"),  // Standard format: 12/01/2024
            DateTimeFormatter.ofPattern("M/d/yyyy"),    // Flexible format: 6/3/2024
            DateTimeFormatter.ofPattern("M/dd/yyyy"),   // Mixed format: 6/03/2024
            DateTimeFormatter.ofPattern("MM/d/yyyy")    // Mixed format: 06/3/2024
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
        
        return null;  // Could not parse using any method
    }

    /**
     * Calculates the hours worked for a single attendance record.
     * Handles potential parsing errors gracefully.
     * 
     * @param record The attendance record
     * @return Hours worked for this record, or 0 if parsing fails
     */
    private static double calculateHoursForRecord(String[] record) {
        try {
            LocalTime timeIn = LocalTime.parse(record[ATT_TIME_IN_COL], TIME_FORMATTER);
            LocalTime timeOut = LocalTime.parse(record[ATT_TIME_OUT_COL], TIME_FORMATTER);
            
            // Calculate duration in hours
            Duration duration = Duration.between(timeIn, timeOut);
            return duration.toMinutes() / 60.0;
        } catch (DateTimeParseException e) {
            // Silently return 0 for problematic records instead of printing errors
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

    /**
     * Extracts the Rice Subsidy amount from the employee record.
     * This allowance is typically provided as a food assistance benefit.
     * 
     * @param employee The employee record
     * @return The rice subsidy amount, or 0 if not available
     */
    private static double extractRiceSubsidy(String[] employee) {
        try {
            if (employee.length > RICE_SUBSIDY_COL && employee[RICE_SUBSIDY_COL] != null) {
                String subsidyString = employee[RICE_SUBSIDY_COL].replaceAll("[^0-9.]", "").trim();
                if (!subsidyString.isEmpty()) {
                    return Double.parseDouble(subsidyString);
                }
            }
            return 0.0;
        } catch (Exception e) {
            System.err.println("Error extracting rice subsidy: " + e.getMessage());
            return 0.0;
        }
    }

    /**
     * Extracts the Phone Allowance amount from the employee record.
     * This allowance is typically provided to cover communication expenses.
     * 
     * @param employee The employee record
     * @return The phone allowance amount, or 0 if not available
     */
    private static double extractPhoneAllowance(String[] employee) {
        try {
            if (employee.length > PHONE_ALLOWANCE_COL && employee[PHONE_ALLOWANCE_COL] != null) {
                String allowanceString = employee[PHONE_ALLOWANCE_COL].replaceAll("[^0-9.]", "").trim();
                if (!allowanceString.isEmpty()) {
                    return Double.parseDouble(allowanceString);
                }
            }
            return 0.0;
        } catch (Exception e) {
            System.err.println("Error extracting phone allowance: " + e.getMessage());
            return 0.0;
        }
    }

    /**
     * Extracts the Clothing Allowance amount from the employee record.
     * This allowance is typically provided for work attire and uniform expenses.
     * 
     * @param employee The employee record
     * @return The clothing allowance amount, or 0 if not available
     */
    private static double extractClothingAllowance(String[] employee) {
        try {
            if (employee.length > CLOTHING_ALLOWANCE_COL && employee[CLOTHING_ALLOWANCE_COL] != null) {
                String allowanceString = employee[CLOTHING_ALLOWANCE_COL].replaceAll("[^0-9.]", "").trim();
                if (!allowanceString.isEmpty()) {
                    return Double.parseDouble(allowanceString);
                }
            }
            return 0.0;
        } catch (Exception e) {
            System.err.println("Error extracting clothing allowance: " + e.getMessage());
            return 0.0;
        }
    }

    /**
     * Calculates the pro-rated allowances for an employee based on the date range.
     * Monthly allowances are pro-rated according to the number of working days
     * in the specified period compared to the standard working days in a month.
     * 
     * @param employee The employee record
     * @param startDate The start date of the pay period
     * @param endDate The end date of the pay period
     * @return The pro-rated total allowance amount
     */
    private static double calculateProRatedAllowances(String[] employee, LocalDate startDate, LocalDate endDate) {
        // Extract full monthly allowances
        double riceSubsidy = extractRiceSubsidy(employee);
        double phoneAllowance = extractPhoneAllowance(employee);
        double clothingAllowance = extractClothingAllowance(employee);
        double totalMonthlyAllowances = riceSubsidy + phoneAllowance + clothingAllowance;
        
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
        
        // Pro-rate the allowances based on working days in period vs. standard month
        double proRatedAllowances = (totalMonthlyAllowances / WORK_DAYS_PER_MONTH) * totalDays;
        
        return proRatedAllowances;
    }

    /**
     * Gets the individual pro-rated allowances for detailed display.
     * 
     * @param employee The employee record
     * @param startDate The start date of the pay period
     * @param endDate The end date of the pay period
     * @return Map containing pro-rated values for each allowance type
     */
    private static Map<String, Double> getProRatedAllowanceDetails(String[] employee, LocalDate startDate, LocalDate endDate) {
        Map<String, Double> allowances = new HashMap<>();
        
        // Extract full monthly allowances
        double riceSubsidy = extractRiceSubsidy(employee);
        double phoneAllowance = extractPhoneAllowance(employee);
        double clothingAllowance = extractClothingAllowance(employee);
        
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
        
        // Pro-rate each allowance based on working days in period vs. standard month
        double proRateFactor = (double) totalDays / WORK_DAYS_PER_MONTH;
        
        allowances.put("riceSubsidy", riceSubsidy * proRateFactor);
        allowances.put("phoneAllowance", phoneAllowance * proRateFactor);
        allowances.put("clothingAllowance", clothingAllowance * proRateFactor);
        allowances.put("totalAllowances", (riceSubsidy + phoneAllowance + clothingAllowance) * proRateFactor);
        allowances.put("workingDays", (double) totalDays);
        
        return allowances;
    }

    /**
     * Prompts the user to input a date and parses it using a consistent format.
     * If the input is invalid, the method will prompt the user to try again.
     *
     * @param scanner Scanner for reading user input
     * @return A LocalDate object representing the input date
     */
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

    /**
     * Searches for employees based on a user-provided search term.
     * The search is performed on employee number, first name, and last name,
     * and results are displayed in a formatted table.
     *
     * @param employees The list of employee records
     * @param scanner Scanner for reading user input
     */
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
                        employee.length > 11 ? employee[11] : "N/A",
                        employee.length > 10 ? employee[10] : "N/A",
                        extractHourlyRate(employee));
            }
        }
        
        if (!found) {
            System.out.println("No employees found matching your search criteria.");
        }
    }

    /**
     * Displays a formatted list of all employees with their basic information.
     * The list includes employee number, name, position, status, and hourly rate.
     * For positions with long names, the text is truncated with ellipsis (...).
     *
     * @param employees The list of employee records
     */
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

    /**
     * Finds an employee record by their employee number.
     * Returns null if no employee with the given number is found.
     *
     * @param employees The list of employee records
     * @param empNumber The employee number to search for
     * @return The employee record (as a string array) or null if not found
     */
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

    /**
     * Formats an employee's name by combining first name and last name.
     * This method ensures consistent name formatting throughout the application.
     *
     * @param employee The employee record
     * @return The formatted full name (First Name + Last Name)
     */
    private static String formatEmployeeName(String[] employee) {
        // Only use the first and last name fields, ignore date fields
        String firstName = employee[2].trim();
        String lastName = employee[1].trim();
        
        
        // Check if there's a middle name/suffix and add it if present
        if (employee.length > 3 && employee[3] != null && !employee[3].trim().isEmpty()) {
        }
        
        return firstName + " " +  lastName;
    }

    /**
     * Prints detailed information about an employee record for debugging purposes.
     * This helps identify the structure and content of employee records, especially
     * during initial data loading.
     *
     * @param employee The employee record to debug
     */
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

    /**
     * Calculates the gross pay including overtime if applicable.
     */
    private static double calculateGrossPayWithOvertime(List<String[]> attendanceRecords, 
                                               int empNumber, 
                                               double hourlyRate,
                                               LocalDate startDate, 
                                               LocalDate endDate) {
        double totalPay = 0.0;
        
        // Group attendance by date to handle overtime on a daily basis
        Map<LocalDate, List<String[]>> recordsByDate = new HashMap<>();
        
        // Group records by date
        for (String[] record : attendanceRecords) {
            try {
                if (isRecordForEmployee(record, empNumber)) {
                    LocalDate recordDate = parseFlexibleDate(record[ATT_DATE_COL]);
                    
                    if (recordDate != null && !recordDate.isBefore(startDate) && !recordDate.isAfter(endDate)) {
                        if (!recordsByDate.containsKey(recordDate)) {
                            recordsByDate.put(recordDate, new ArrayList<>());
                        }
                        
                        recordsByDate.get(recordDate).add(record);
                    }
                }
            } catch (Exception e) {
                // Silently skip problematic records
                continue;
            }
        }
        
        // Calculate pay for each day, including overtime
        for (Map.Entry<LocalDate, List<String[]>> entry : recordsByDate.entrySet()) {
            double dailyHours = 0.0;
            
            // Sum up hours for this day
            for (String[] record : entry.getValue()) {
                dailyHours += calculateHoursForRecord(record);
            }
            
            // Calculate pay with overtime
            double regularHours = Math.min(dailyHours, REGULAR_HOURS_PER_DAY);
            double overtimeHours = Math.max(0, dailyHours - REGULAR_HOURS_PER_DAY);
            
            double regularPay = regularHours * hourlyRate;
            double overtimePay = overtimeHours * hourlyRate * OVERTIME_RATE;
            
            totalPay += regularPay + overtimePay;
        }
        
        return totalPay;
    }

    /**
     * Creates a detailed breakdown of regular and overtime hours and pay.
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
        
        // Group attendance by date to handle overtime on a daily basis
        Map<LocalDate, List<String[]>> recordsByDate = new HashMap<>();
        
        // Group records by date
        for (String[] record : attendanceRecords) {
            try {
                if (isRecordForEmployee(record, empNumber)) {
                    LocalDate recordDate = parseFlexibleDate(record[ATT_DATE_COL]);
                    
                    if (recordDate != null && !recordDate.isBefore(startDate) && !recordDate.isAfter(endDate)) {
                        if (!recordsByDate.containsKey(recordDate)) {
                            recordsByDate.put(recordDate, new ArrayList<>());
                        }
                        
                        recordsByDate.get(recordDate).add(record);
                    }
                }
            } catch (Exception e) {
                // Silently skip problematic records
                continue;
            }
        }
        
        // Calculate pay for each day, including overtime
        for (Map.Entry<LocalDate, List<String[]>> entry : recordsByDate.entrySet()) {
            double dailyHours = 0.0;
            
            // Sum up hours for this day
            for (String[] record : entry.getValue()) {
                dailyHours += calculateHoursForRecord(record);
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