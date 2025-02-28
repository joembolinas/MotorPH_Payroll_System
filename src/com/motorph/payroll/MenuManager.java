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