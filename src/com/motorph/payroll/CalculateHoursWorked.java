package com.motorph.payroll;

public class CalculateHoursWorked {
    public static void main(String[] args) {
        // Manually adding employee data
        String[][] employeeData = {
            {"10001", "Garcia", "Manuel III", "8:59", "18:31"},
            {"10002", "Lim", "Antonio", "10:35", "19:44"},
            {"10003", "Aquino", "Bianca Sofia", "10:23", "18:32"}
        };

        System.out.println("Employee # | Name | Hours Worked");
        for (String[] emp : employeeData) {
            String empId = emp[0];
            String name = emp[1] + ", " + emp[2];
            double hoursWorked = calculateHours(emp[3], emp[4]);

            System.out.printf("%s | %s | %.2f hours\n", empId, name, hoursWorked);
        }
    }

    // Method to calculate total hours worked
    public static double calculateHours(String login, String logout) {
        String[] inTime = login.split(":");
        String[] outTime = logout.split(":");

        int inHours = Integer.parseInt(inTime[0]);
        int inMinutes = Integer.parseInt(inTime[1]);

        int outHours = Integer.parseInt(outTime[0]);
        int outMinutes = Integer.parseInt(outTime[1]);

        double totalHours = (outHours + outMinutes / 60.0) - (inHours + inMinutes / 60.0);
        return totalHours;
    }
}
