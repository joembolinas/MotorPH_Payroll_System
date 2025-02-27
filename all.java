// Main.java
package com.motorph.payroll;

import java.io.IOException;
import java.util.List;

public class Main {
    private static final String EMPLOYEE_CSV_URL = "https://docs.google.com/spreadsheets/d/e/2PACX-1vQXb2izvcFadSUJFaQaUwgFbiyWdzD0YcVmXGBt-KVfkWOt1y0pfuTRw9b3QHfS0FitdmgwKhKN1a0n/pub?gid=436645740&single=true&output=csv";
    private static final String ATTENDANCE_CSV_URL = "https://docs.google.com/spreadsheets/d/e/2PACX-1vQXb2izvcFadSUJFaQaUwgFbiyWdzD0YcVmXGBt-KVfkWOt1y0pfuTRw9b3QHfS0FitdmgwKhKN1a0n/pub?gid=1494987339&single=true&output=csv";

public static void main(String[] args) {
    try {
        List<Employee> employees = CsvReader.fetchEmployeesFromCsv(EMPLOYEE_CSV_URL);
        AttendanceTracker attendanceTracker = new AttendanceTracker();
        CsvReader.loadAttendanceData(ATTENDANCE_CSV_URL, attendanceTracker);
        
        new MenuManager(employees, attendanceTracker).displayMainMenu();
    } catch (IOException e) {
        System.err.println("Error loading data: " + e.getMessage());
        System.exit(1);
        }
    }
}


// CsvReader.java
package com.motorph.payroll;

import java.io.BufferedReader; // import BufferedReader class to read text from a input 
import java.io.IOException; // import IOException class to handle errors
import java.io.InputStreamReader; // import InputStreamReader class to read bytes and decode into text
import java.net.URL; // import URL class to represent a standard Resource Locator
import java.util.ArrayList; // import ArrayList class to use as data structure for employee records and scan
import java.util.List; // import List class to use scan

public class CsvReader {
    public static List<Employee> fetchEmployeesFromCsv(String url) throws IOException {
        List<Employee> employees = new ArrayList<>(); // initialize an ArrayList to store employee records
        int lineNumber = 0;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(new URL(url).openStream()))) {
            br.readLine(); // Skip header // read the first line of the database
            String line;

            while ((line = br.readLine()) != null) { // read the next line of the database
                lineNumber++; // increment 
                try { // try block and catch block to handle exceptions if any error occurs
                    String[] data = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
                    if (data.length >= 19) {
                        employees.add(new Employee(data));
                    }
                } catch (IllegalArgumentException e) { // catch block to handle IllegalArgumentException
                    System.err.printf("Skipping invalid data at line %d: %s%n", lineNumber, e.getMessage());
                }
            }
        } catch (IOException e) { // catch block to handle IOException
            throw new IOException("Failed to read employee data: " + e.getMessage());
        }

        if (employees.isEmpty()) { // check if the list is empty
            throw new IOException("No valid employee records found in CSV");
        }
        System.out.println("Debug: Loaded " + employees.size() + " employees.");
        return employees;
    }

    // New method to read attendance data
    public static void loadAttendanceData(String url, AttendanceTracker tracker) throws IOException {
        int lineNumber = 0; // lineNumber to 0

        try (BufferedReader br = new BufferedReader(new InputStreamReader(new URL(url).openStream()))) {
            br.readLine(); // Skip header
            String line; // variable line

            while ((line = br.readLine()) != null) { // read the next line of the database
                lineNumber++;
                try { // try block and catch block to handle exceptions if any error occurs
                    String[] data = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
                    if (data.length >= 6) { // check if the length of the data is greater than or equal to 6 for attendance data if false skip
                        int employeeNumber = Integer.parseInt(data[0].trim());
                        String loginTime = data[4].trim();
                        String logoutTime = data[5].trim();
                        tracker.addAttendanceRecord(employeeNumber, loginTime, logoutTime); // add attendance record to the tracker
                    }
                } catch (Exception e) {
                    System.err.printf("Skipping invalid attendance data at line %d: %s%n", lineNumber, e.getMessage());
                }
            }
        } catch (IOException e) {
            throw new IOException("Failed to read attendance data: " + e.getMessage());
        }
        System.out.println("Debug: Attendance data loaded.");
    }
}

//
package com.motorph.payroll;

import java.util.List; // import List class to use scan
import java.util.Scanner; // import Scanner class to get user input

public class MenuManager { // create a class named MenuManager
    private final Scanner scanner; // create a variable scanner of type Scanner
    private final List<Employee> employees; // create a variable employees of type List<Employee>
    private final AttendanceTracker attendanceTracker; // create a variable attendanceTracker of type AttendanceTracker

    public MenuManager(List<Employee> employees, AttendanceTracker attendanceTracker) { // create a constructor that takes a List<Employee> and AttendanceTracker as parameters
        this.scanner = new Scanner(System.in); // initialize the scanner
        this.employees = employees; // assign the employees parameter to the employees variable
        this.attendanceTracker = attendanceTracker; // assign the attendanceTracker parameter to the attendanceTracker variable
    }

    // Main menu method
    public void displayMainMenu() {
        while (true) {
            System.out.println("\n=== MotorPH Payroll System ===");     // Print the header
            System.out.println("1. Employee Management");
            System.out.println("2. Payroll Management");
            System.out.println("3. Exit");
            System.out.print("Enter your choice: ");    // Prompt the user for input

            try { // try block to handle exceptions of menu choice
                int choice = Integer.parseInt(scanner.nextLine());
                switch (choice) {
                    case 1 -> displayEmployeeMenu();
                    case 2 -> displayPayrollMenu();
                    case 3 -> {
                        System.out.println("Exiting system...");
                        scanner.close();    // time consuming to fix the error in scanner but it is not necessary (normal pala yung error)
                        System.exit(0);
                    }
                    default -> System.out.println("Invalid choice. Please enter 1-3.");
                }
            } catch (NumberFormatException e) { // catch block to handle NumberFormatException
                System.out.println("Invalid input. Please enter a number.");
            }
        }
    }

    // Employee management menu
    private void displayEmployeeMenu() {
        while (true) {
            System.out.println("\nEmployee Management:");
            System.out.println("1. Search Employee");
            System.out.println("2. List All Employees");
            System.out.println("3. View Hours Worked");
            System.out.println("4. Calculate Gross Salary"); // New option
            System.out.println("5. Return to Main Menu");
            System.out.print("Enter your choice: ");
    
            try {
                int choice = Integer.parseInt(scanner.nextLine());
                switch (choice) {
                    case 1 -> searchEmployee();
                    case 2 -> listAllEmployees();
                    case 3 -> viewHoursWorked();
                    case 4 -> calculateGrossSalary();
                    case 5 -> { return; }
                    default -> System.out.println("Invalid choice. Please enter 1-5.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
    }

    // Search for an employee
    private void searchEmployee() {
        System.out.print("\nEnter search term (ID or name): "); // Prompt the user for input
        String searchTerm = scanner.nextLine().trim().toLowerCase();

        List<Employee> results = employees.stream() // search for the employee
                .filter(e -> String.valueOf(e.getEmployeeNumber()).contains(searchTerm) ||
                        e.getFullName().toLowerCase().contains(searchTerm))
                .toList();

        if (results.isEmpty()) {    // check if the list is empty
            System.out.println("No matching employees found.");
            return;
        }

        System.out.println("\nSearch Results:");    // Print the header
        results.forEach(emp -> {
            System.out.printf("[%d] %s%n", emp.getEmployeeNumber(), emp.getFullName());
        });

        System.out.print("\nEnter employee number to view details (0 to cancel): ");    // Prompt the user for input
        try {
            int empNumber = Integer.parseInt(scanner.nextLine());
            if (empNumber != 0) {
                employees.stream() // search for the employee number
                        .filter(e -> e.getEmployeeNumber() == empNumber) // filter the employee number
                        .findFirst() // find the first employee
                        .ifPresentOrElse( // if the employee is present, display the employee info, else print "Employee not found."
                                Employee::displayEmployeeInfo, // display the employee info
                                () -> System.out.println("Employee not found.") // print "Employee not found."
                        );
            }
        } catch (NumberFormatException e) { // catch block to handle NumberFormatException
            System.out.println("Invalid employee number format.");
        }
    }

    // List all employees
    private void listAllEmployees() { // create a method named listAllEmployees
        System.out.println("\nAll Employees:");
        System.out.printf("%-8s %-25s %-15s%n", "ID", "Name", "Position"); // print the header
        employees.forEach(emp -> System.out.printf("%-8d %-25s %-15s%n", // print the employee details
                emp.getEmployeeNumber(),
                emp.getFullName(),
                emp.getPosition()));
    }

    // View hours worked for an employee
    private void viewHoursWorked() {
        System.out.print("\nEnter employee number: "); // Prompt the user for input
        int empNumber = Integer.parseInt(scanner.nextLine());

        double hoursWorked = attendanceTracker.calculateHoursWorked(empNumber);
        if (hoursWorked > 0) {
            System.out.printf("Employee %d worked %.2f hours.%n", empNumber, hoursWorked); // print the hours worked
        } else { // print "No attendance record found for this employee."
            System.out.println("No attendance record found for this employee."); 
        }
    }

    // Payroll management menu (placeholder)
    private void displayPayrollMenu() {
        System.out.println("\n (soon)"); // for future implementation
    }


private void calculateGrossSalary() {
    System.out.print("\nEnter employee number: ");
    int empNumber = Integer.parseInt(scanner.nextLine());
    
    // Get employee and hours worked
    Employee employee = employees.stream()
            .filter(e -> e.getEmployeeNumber() == empNumber)
            .findFirst()
            .orElse(null);
    
    if (employee == null) {
        System.out.println("Employee not found.");
        return;
    }

    double hoursWorked = attendanceTracker.calculateHoursWorked(empNumber);
    if (hoursWorked <= 0) {
        System.out.println("No valid attendance record found.");
        return;
    }

    // Debug logs
    System.out.println("Debug: Employee Hourly Rate = " + employee.getHourlyRate());
    System.out.println("Debug: Hours Worked = " + hoursWorked);

    // Calculate and display
    double grossSalary = PayrollCalculator.calculateGrossSalary(hoursWorked, employee.getHourlyRate());
    
    System.out.printf("\nGross Salary Calculation for %s:%n", employee.getFullName());
    System.out.printf("Hours Worked: %.2f hrs%n", hoursWorked);
    System.out.printf("Hourly Rate: PHP %.2f%n", employee.getHourlyRate());
    System.out.printf("Gross Salary: PHP %.2f%n", grossSalary);
}

// MenuManager.java
private void calculateNetSalary() {
    System.out.print("\nEnter employee number: ");
    int empNumber = Integer.parseInt(scanner.nextLine());

    Employee employee = employees.stream()
            .filter(e -> e.getEmployeeNumber() == empNumber)
            .findFirst()
            .orElse(null);

    if (employee == null) {
        System.out.println("Employee not found.");
        return;
    }

    double hoursWorked = attendanceTracker.calculateHoursWorked(empNumber);
    if (hoursWorked <= 0) {
        System.out.println("No valid attendance record found.");
        return;
    }

    // Debug logs
    System.out.println("Debug: Employee Hourly Rate = " + employee.getHourlyRate());
    System.out.println("Debug: Hours Worked = " + hoursWorked);

    double grossSalary = PayrollCalculator.calculateGrossSalary(hoursWorked, employee.getHourlyRate());
    double netSalary = PayrollCalculator.calculateNetSalary(grossSalary);

    System.out.printf("\nNet Salary Calculation for %s:%n", employee.getFullName());
    System.out.printf("Gross Salary: PHP %.2f%n", grossSalary);
    System.out.printf("Deductions (SSS: PHP %.2f, PhilHealth: PHP %.2f, Pag-IBIG: PHP %.2f, Tax: PHP %.2f)%n",
            DeductionsCalculator.calculateSSS(grossSalary),
            DeductionsCalculator.calculatePhilHealth(grossSalary),
            DeductionsCalculator.calculatePagIBIG(),
            DeductionsCalculator.calculateWithholdingTax(grossSalary));
    System.out.printf("Net Salary: PHP %.2f%n", netSalary);
} 
}


// Employee.java

package com.motorph.payroll;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class Employee {
    private final int employeeNumber;
    private final String lastName;
    private final String firstName;
    private final LocalDate birthday;
    private final String position;
    private final double hourlyRate;

    // Constructor to initialize Employee object from a string array
    public Employee(String[] data) throws IllegalArgumentException {
        try {
            this.employeeNumber = Integer.parseInt(data[0].trim()); // Parse and assign employee number
            this.lastName = data[1].trim(); // Assign last name
            this.firstName = data[2].trim(); // Assign first name
            this.birthday = parseDate(data[3].trim()); // Parse and assign birthday
            this.position = data[11].trim(); // Assign position
            this.hourlyRate = Double.parseDouble(data[18].trim()); // Parse and assign hourly rate
        } catch (DateTimeParseException | NumberFormatException | ArrayIndexOutOfBoundsException e) {
            // Throw exception if data is invalid
            throw new IllegalArgumentException("Invalid employee data: " + e.getMessage());
        }
    }

    // Method to parse date from string
    private LocalDate parseDate(String dateString) throws DateTimeParseException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d/yyyy"); // Define date format
        return LocalDate.parse(dateString, formatter); // Parse and return LocalDate
    }

    // Getters
    public int getEmployeeNumber() { return employeeNumber; } // Get employee number
    public String getFullName() { return firstName + " " + lastName; } // Get full name
    public LocalDate getBirthday() { return birthday; } // Get birthday
    public String getPosition() { return position; } // Get position
    public double getHourlyRate() { return hourlyRate; } // Get hourly rate
    

    // Method to display employee information
    public void displayEmployeeInfo() {
        System.out.println("\nEmployee Details:"); // Print header
        System.out.println("Number: " + employeeNumber); // Print employee number
        System.out.println("Name: " + getFullName()); // Print full name
        System.out.println("Birthday: " + birthday.format(DateTimeFormatter.ISO_DATE)); // Print birthday
        System.out.println("Position: " + position); // Print position
        System.out.printf("Hourly Rate: PHP %.2f%n", hourlyRate); // Print hourly rate
    } // Close displayEmployeeInfo method
    
    public String getFormattedHourlyRate() {  
        return String.format("PHP %.2f/hour", hourlyRate);  // Get formatted hourly rate
        
    } 
}


//AttendanceTracker.java
package com.motorph.payroll; //error palagi sa package name

import java.time.Duration; //import ducation class
import java.time.LocalTime; // import LocalTime class to use as data type
import java.time.format.DateTimeFormatter; // import DateTimeFormatter class to format time
import java.util.HashMap; // import HashMap class to use as data structure for attendance records
import java.util.Map; // import Map class to use as data structure for attendance records

public class AttendanceTracker { // create a class named AttendanceTracker
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("H:mm"); // create a constant variable TIME_FORMATTER to format time/ this line more error prone
    private static final LocalTime OFFICE_START_TIME = LocalTime.of(8, 30); // Office hours start at 8:30 AM
    private static final LocalTime OFFICE_END_TIME = LocalTime.of(17, 30); // Office hours end at 5:30 PM
    private static final int GRACE_PERIOD_MINUTES = 10; // 10-minute palugit 

    // Map to store attendance data (Employee ID -> Login and Logout times)
    private final Map<Integer, AttendanceRecord> attendanceRecords = new HashMap<>(); // initialize a Map data structure to store attendance records

    // Inner class to store login and logout times for an employee
    private static class AttendanceRecord {
        LocalTime loginTime; // create a variable loginTime of type LocalTime
        LocalTime logoutTime; // create a variable logoutTime of type LocalTime

        AttendanceRecord(LocalTime loginTime, LocalTime logoutTime) {
            this.loginTime = loginTime; // assign loginTime to the value of the parameter loginTime
            this.logoutTime = logoutTime; // assign logoutTime to the value of the parameter logoutTime
        }
    }

    // Add attendance record for an employee
    public void addAttendanceRecord(int employeeNumber, String loginTimeStr, String logoutTimeStr) {
        try {
            LocalTime loginTime = LocalTime.parse(loginTimeStr, TIME_FORMATTER);   // parse the loginTimeStr to LocalTime
            LocalTime logoutTime = LocalTime.parse(logoutTimeStr, TIME_FORMATTER); // parse the logoutTimeStr to LocalTime

            // Apply grace period for late logins
            if (loginTime.isAfter(OFFICE_START_TIME.plusMinutes(GRACE_PERIOD_MINUTES))) {
                System.out.printf("Employee %d logged in late at %s%n", employeeNumber, loginTime); // print the employee number and login time
            }

            attendanceRecords.put(employeeNumber, new AttendanceRecord(loginTime, logoutTime)); // add the attendance record to the map
        } catch (Exception e) {
            System.err.printf("Invalid time format for employee %d: %s%n", employeeNumber, e.getMessage()); // print error message if time format is invalid
        }
    }

    // Calculate hours worked for an employee
    public double calculateHoursWorked(int employeeNumber) { // create a method named calculateHoursWorked that takes an int  employeeNumber
        AttendanceRecord record = attendanceRecords.get(employeeNumber); // get the attendance record for the employee number
        if (record == null) {
            System.err.printf("No attendance record found for employee %d%n", employeeNumber); // print error message if no attendance record is found
            return 0.0;
        }

        // Calculate hours worked
        Duration duration = Duration.between(record.loginTime, record.logoutTime); // calculate the duration between login and logout time
        double hoursWorked = duration.toMinutes() / 60.0; // convert 
        System.out.println("Debug: Attendance Record for Employee = " + hoursWorked);

        // Ensure hours worked do not exceed office hours
        if (record.logoutTime.isAfter(OFFICE_END_TIME)) {
            hoursWorked = Duration.between(record.loginTime, OFFICE_END_TIME).toMinutes() / 60.0; // calculate the duration between login and office end time
        }

        return hoursWorked;
    }
}

//PayrollCalculator.java

// PayrollCalculator.java
package com.motorph.payroll;

public class PayrollCalculator {
    private static final double OVERTIME_RATE = 1.25; // Overtime rate (25% more than regular rate)
    private static final double REGULAR_HOURS_PER_WEEK = 40; // Standard work hours per week

    /**
     * Calculates the gross salary based on hours worked and hourly rate.
     *
     * @param hoursWorked Total hours worked by the employee.
     * @param hourlyRate  Hourly rate of the employee.
     * @return Gross salary after including overtime (if applicable).
     */
    public static double calculateGrossSalary(double hoursWorked, double hourlyRate) {
        System.out.println("Debug: Hours Worked = " + hoursWorked);
        System.out.println("Debug: Hourly Rate = " + hourlyRate);

        if (hoursWorked <= REGULAR_HOURS_PER_WEEK) {
            double grossSalary = hoursWorked * hourlyRate;
            System.out.println("Debug: Regular Pay = " + grossSalary);
            return grossSalary;
        } else {
            double regularPay = REGULAR_HOURS_PER_WEEK * hourlyRate;
            double overtimePay = (hoursWorked - REGULAR_HOURS_PER_WEEK) * hourlyRate * OVERTIME_RATE;
            double grossSalary = regularPay + overtimePay;
            System.out.println("Debug: Regular Pay = " + regularPay);
            System.out.println("Debug: Overtime Pay = " + overtimePay);
            System.out.println("Debug: Gross Salary = " + grossSalary);
            return grossSalary;
        }
    }

    public static double calculateNetSalary(double grossSalary) {
        double sss = DeductionsCalculator.calculateSSS(grossSalary);
        double philHealth = DeductionsCalculator.calculatePhilHealth(grossSalary);
        double pagIBIG = DeductionsCalculator.calculatePagIBIG();
        double tax = DeductionsCalculator.calculateWithholdingTax(grossSalary);

        return grossSalary - (sss + philHealth + pagIBIG + tax);
    }
}

// DeductionsCalculator.java

package com.motorph.payroll;

// DeductionsCalculator.java
public class DeductionsCalculator {
    // SSS Calculation (simplified example)
    public static double calculateSSS(double grossSalary) {
        // Replace with actual SSS table lookup
        return Math.min(grossSalary * 0.045, 1350.0); // Example: 4.5% up to PHP 1350
    }

    // PhilHealth Calculation
    public static double calculatePhilHealth(double grossSalary) {
        return grossSalary * 0.04 / 2; // Employee share (4% total, 2% paid by employee)
    }

    // Pag-IBIG Calculation
    public static double calculatePagIBIG() {
        return 100.0; // Fixed monthly contribution
    }

    // Withholding Tax Calculation (simplified)
    public static double calculateWithholdingTax(double grossSalary) {
        if (grossSalary <= 20833) return 0;
        else if (grossSalary <= 33333) return (grossSalary - 20833) * 0.20;
        else return (grossSalary - 33333) * 0.25 + 2500;
    }
}

//

