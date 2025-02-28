package com.motorph.payroll;

// DeductionsCalculator.java
public class DeductionsCalculator {
    // SSS Calculation (simplified example)
    public static double calculateSSS(double grossSalary) {
        // Replace with actual SSS table lookup
        return Math.min(grossSalary * 0.045, 1350.0); // Example: 4.5% up to PHP 1350
    }

    // PhilHealth Calculation
    public static double calculatePhilHealth(double grossSalary) {
        return grossSalary * 0.04 / 2; // Employee share (4% total, 2% paid by employee)
    }

    // Pag-IBIG Calculation
    public static double calculatePagIBIG() {
        return 100.0; // Fixed monthly contribution
    }

    // Withholding Tax Calculation (simplified)
    public static double calculateWithholdingTax(double grossSalary) {
        if (grossSalary <= 20833) return 0;
        else if (grossSalary <= 33333) return (grossSalary - 20833) * 0.20;
        else return (grossSalary - 33333) * 0.25 + 2500;
    }
}