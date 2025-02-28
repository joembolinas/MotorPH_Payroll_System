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