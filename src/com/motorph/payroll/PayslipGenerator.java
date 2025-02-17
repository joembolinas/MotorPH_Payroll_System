// PayslipGenerator.java - Generates Employee Payslips
public class PayslipGenerator {
    // Displays Payslip in Console
    public static void generatePayslip(String name, String position, double grossSalary, double netSalary) {
        System.out.println("================================");
        System.out.println(" Payslip for " + name); // Employee Name
        System.out.println(" Position: " + position); // Job Position
        System.out.println(" Gross Salary: PHP " + grossSalary); // Gross Salary
        System.out.println(" Net Salary: PHP " + netSalary); // Net Salary
        System.out.println("================================");
    }
}
