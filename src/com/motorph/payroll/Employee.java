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
    }
}