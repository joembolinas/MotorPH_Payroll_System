package com.motorph.payroll;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class CsvReader {
    public static List<Employee> fetchEmployeesFromCsv(String url) throws IOException {
        List<Employee> employees = new ArrayList<>();
        int lineNumber = 0;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(new URL(url).openStream()))) {
            br.readLine(); // Skip header
            String line;
            
            while ((line = br.readLine()) != null) {
                lineNumber++;
                try {
                    String[] data = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
                    if (data.length >= 19) {
                        employees.add(new Employee(data));
                    }
                } catch (IllegalArgumentException e) {
                    System.err.printf("Skipping invalid data at line %d: %s%n", lineNumber, e.getMessage());
                }
            }
        } catch (IOException e) {
            throw new IOException("Failed to read employee data: " + e.getMessage());
        }
        
        if (employees.isEmpty()) {
            throw new IOException("No valid employee records found in CSV");
        }
        return employees;
    }
}