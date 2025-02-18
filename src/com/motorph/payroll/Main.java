// Main.java - Entry Point for the Program

import java.util.List;

public class Main {
    public static void main(String[] args) {
        try {
            // Google Sheets CSV Links
            String employeeSheetUrl = "https://docs.google.com/spreadsheets/d/e/2PACX-1vRe4-w2yYtOZpBxFZGP1UZqyKWk053QkCmVxwq9Hiu2LfHU2nVIvCkTTg8rtWQsP-sp31jG6OleREqM/pub?output=csv";
            String attendanceSheetUrl = "https://docs.google.com/spreadsheets/d/e/2PACX-1vTqBrLETQHcACenfV0_VSgV_uEGH5Cne2Vuw-oN2yDGRH5wWS8x8CcAXAV8iSNugtwWB_oVCuOlcFYT/pub?output=csv";

            // Fetch employee data
            List<Employee> employees = GoogleSheetsCSVReader.fetchEmployeesFromGoogleSheets(employeeSheetUrl);
            if (employees.isEmpty()) {
                System.out.println("No employees found.");
                return;
            }

            // Process first employee for demonstration
            Employee emp1 = employees.get(0);
            emp1.displayEmployeeInfo();

            // Fetch attendance data (Assumed format: Array of hours worked per day)
            List<Double> attendanceData = GoogleSheetsCSVReader.fetchAttendanceFromGoogleSheets(attendanceSheetUrl);
            double hoursWorked = AttendanceTracker.getHoursWorked(attendanceData);

            // Generate Payslip
            PayslipGenerator.generatePayslip(emp1, hoursWorked);
        } catch (Exception e) {
            System.err.println("Error processing payroll: " + e.getMessage());
        }
    }
}
