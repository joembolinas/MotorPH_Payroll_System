// PayrollCalculator.java
package com.motorph.payroll;

public class PayrollCalculator {
    private static final double OVERTIME_RATE = 1.25; // Overtime rate (25% more than regular rate)
    private static final double REGULAR_HOURS_PER_WEEK = 40; // Standard work hours per week

    /**
     * Calculates the gross salary based on hours worked and hourly rate.
     *
     * @param hoursWorked Total hours worked by the employee.
     * @param hourlyRate  Hourly rate of the employee.
     * @return Gross salary after including overtime (if applicable).
     */
    public static double calculateGrossSalary(double hoursWorked, double hourlyRate) {
        System.out.println("Debug: Hours Worked = " + hoursWorked);
        System.out.println("Debug: Hourly Rate = " + hourlyRate);

        if (hoursWorked <= REGULAR_HOURS_PER_WEEK) {
            double grossSalary = hoursWorked * hourlyRate;
            System.out.println("Debug: Regular Pay = " + grossSalary);
            return grossSalary;
        } else {
            double regularPay = REGULAR_HOURS_PER_WEEK * hourlyRate;
            double overtimePay = (hoursWorked - REGULAR_HOURS_PER_WEEK) * hourlyRate * OVERTIME_RATE;
            double grossSalary = regularPay + overtimePay;
            System.out.println("Debug: Regular Pay = " + regularPay);
            System.out.println("Debug: Overtime Pay = " + overtimePay);
            System.out.println("Debug: Gross Salary = " + grossSalary);
            return grossSalary;
        }
    }

    public static double calculateNetSalary(double grossSalary) {
        double sss = DeductionsCalculator.calculateSSS(grossSalary);
        double philHealth = DeductionsCalculator.calculatePhilHealth(grossSalary);
        double pagIBIG = DeductionsCalculator.calculatePagIBIG();
        double tax = DeductionsCalculator.calculateWithholdingTax(grossSalary);

        return grossSalary - (sss + philHealth + pagIBIG + tax);
    }
}