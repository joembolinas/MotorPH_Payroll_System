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

    public Employee(String[] data) throws IllegalArgumentException {
        try {
            this.employeeNumber = Integer.parseInt(data[0].trim());
            this.lastName = data[1].trim();
            this.firstName = data[2].trim();
            this.birthday = parseDate(data[3].trim());
            this.position = data[11].trim();
            this.hourlyRate = Double.parseDouble(data[18].trim());
        } catch (DateTimeParseException | NumberFormatException | ArrayIndexOutOfBoundsException e) {
            throw new IllegalArgumentException("Invalid employee data: " + e.getMessage());
        }
    }

    private LocalDate parseDate(String dateString) throws DateTimeParseException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d/yyyy");
        return LocalDate.parse(dateString, formatter);
    }

    // Getters
    public int getEmployeeNumber() { return employeeNumber; }
    public String getFullName() { return firstName + " " + lastName; }
    public LocalDate getBirthday() { return birthday; }
    public String getPosition() { return position; }
    public double getHourlyRate() { return hourlyRate; }

    public void displayEmployeeInfo() {
        System.out.println("\nEmployee Details:");
        System.out.println("Number: " + employeeNumber);
        System.out.println("Name: " + getFullName());
        System.out.println("Birthday: " + birthday.format(DateTimeFormatter.ISO_DATE));
        System.out.println("Position: " + position);
        System.out.printf("Hourly Rate: PHP %.2f%n", hourlyRate);
    }
}