package com.motorph.payroll;

public class PayrollCalculator {
    private static final double OVERTIME_RATE = 1.25;
    private static final double REGULAR_HOURS_PER_WEEK = 40;

    public static double calculateGrossSalary(double hoursWorked, double hourlyRate) {
        if (hoursWorked <= REGULAR_HOURS_PER_WEEK) {
            return hoursWorked * hourlyRate;
        }
        
        double regularPay = REGULAR_HOURS_PER_WEEK * hourlyRate;
        double overtimeHours = hoursWorked - REGULAR_HOURS_PER_WEEK;
        double overtimePay = overtimeHours * hourlyRate * OVERTIME_RATE;
        
        return regularPay + overtimePay;
    }
}