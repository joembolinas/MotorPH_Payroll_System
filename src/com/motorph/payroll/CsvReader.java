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