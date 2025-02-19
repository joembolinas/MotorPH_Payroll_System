package com.motorph.payroll;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class CalculateHoursWorkedTest1 {
    public static void main(String[] args) {
        String filePath = "attendance.csv"; // Ensure the file is in the correct directory

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean firstLine = true; // Skip header row

            System.out.println("Employee # | Name | Hours Worked");

            while ((line = br.readLine()) != null) {
                if (firstLine) { 
                    firstLine = false; 
                    continue; // Skip the header row
                }

                String[] data = line.split(","); // Split by comma
                String empId = data[0].trim();
                String name = data[1].trim() + ", " + data[2].trim();
                String loginTime = data[4].trim();
                String logoutTime = data[5].trim();

                double hoursWorked = calculateHours(loginTime, logoutTime);

                System.out.printf("%s | %s | %.2f hours\n", empId, name, hoursWorked);
            }
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
    }

    // Method to calculate total hours worked
    public static double calculateHours(String login, String logout) {
        String[] inTime = login.split(":");
        String[] outTime = logout.split(":");

        int inHour = Integer.parseInt(inTime[0]);
        int inMinute = Integer.parseInt(inTime[1]);
        int outHour = Integer.parseInt(outTime[0]);
        int outMinute = Integer.parseInt(outTime[1]);

        // Convert to minutes
        int totalInMinutes = (inHour * 60) + inMinute;
        int totalOutMinutes = (outHour * 60) + outMinute;

        // Calculate difference in hours
        return (totalOutMinutes - totalInMinutes) / 60.0;
    }
}