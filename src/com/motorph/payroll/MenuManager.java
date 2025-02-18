package com.motorph.payroll;

import java.util.List;
import java.util.Scanner;

public class MenuManager {
    private final Scanner scanner;
    private final List<Employee> employees;

    public MenuManager(List<Employee> employees) {
        this.scanner = new Scanner(System.in);
        this.employees = employees;
    }

    public void displayMainMenu() {
        while (true) {
            System.out.println("\n=== MotorPH Payroll System ===");
            System.out.println("1. Employee Management");
            System.out.println("2. Payroll Management");
            System.out.println("3. Exit");
            System.out.print("Enter your choice: ");

            try {
                int choice = Integer.parseInt(scanner.nextLine());
                
                switch (choice) {
                    case 1 -> displayEmployeeMenu();
                    case 2 -> displayPayrollMenu();
                    case 3 -> {
                        System.out.println("Exiting system...");
                        scanner.close();
                        System.exit(0);
                    }
                    default -> System.out.println("Invalid choice. Please enter 1-3.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
    }

    private void displayEmployeeMenu() {
        while (true) {
            System.out.println("\nEmployee Management:");
            System.out.println("1. Search Employee");
            System.out.println("2. List All Employees");
            System.out.println("3. Return to Main Menu");
            System.out.print("Enter your choice: ");

            try {
                int choice = Integer.parseInt(scanner.nextLine());
                
                switch (choice) {
                    case 1 -> searchEmployee();
                    case 2 -> listAllEmployees();
                    case 3 -> { return; }
                    default -> System.out.println("Invalid choice. Please enter 1-3.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
    }

    private void searchEmployee() {
        System.out.print("\nEnter search term (ID or name): ");
        String searchTerm = scanner.nextLine().trim().toLowerCase();

        List<Employee> results = employees.stream()
                .filter(e -> String.valueOf(e.getEmployeeNumber()).contains(searchTerm) ||
                        e.getFullName().toLowerCase().contains(searchTerm))
                .toList();

        if (results.isEmpty()) {
            System.out.println("No matching employees found.");
            return;
        }

        System.out.println("\nSearch Results:");
        results.forEach(emp -> {
            System.out.printf("[%d] %s%n", emp.getEmployeeNumber(), emp.getFullName());
        });

        System.out.print("\nEnter employee number to view details (0 to cancel): ");
        try {
            int empNumber = Integer.parseInt(scanner.nextLine());
            if (empNumber != 0) {
                employees.stream()
                        .filter(e -> e.getEmployeeNumber() == empNumber)
                        .findFirst()
                        .ifPresentOrElse(
                                Employee::displayEmployeeInfo,
                                () -> System.out.println("Employee not found.")
                        );
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid employee number format.");
        }
    }

    private void listAllEmployees() {
        System.out.println("\nAll Employees:");
        System.out.printf("%-8s %-25s %-15s%n", "ID", "Name", "Position");
        employees.forEach(emp -> System.out.printf("%-8d %-25s %-15s%n",
                emp.getEmployeeNumber(),
                emp.getFullName(),
                emp.getPosition()));
    }

    private void displayPayrollMenu() {
        System.out.println("\nPayroll Management Module (Under Development)");
        // Implementation placeholder
    }
}