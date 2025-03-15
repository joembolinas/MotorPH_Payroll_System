package com.motorph.original;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles all payroll-related calculations for the MotorPH payroll system.
 * This class encapsulates the logic for calculating gross pay, net pay, and various deductions.
 */
public class PayrollCalculator {
    
    // Constants for calculation rates
    private static final double PHILHEALTH_RATE = 0.03;
    private static final double PHILHEALTH_CAP_PERCENT = 0.03;
    private static final double PAGIBIG_RATE = 0.02;
    private static final double PAGIBIG_CAP_PERCENT = 0.02;
    private static final double SSS_CAP_PERCENT = 0.10;
    private static final double TAX_CAP_PERCENT = 0.20;
    
    // SSS contribution table
    private final Map<Double, Double> sssTable;
    
    /**
     * Creates a new PayrollCalculator with initialized SSS table
     */
    public PayrollCalculator() {
        this.sssTable = initSSSTable();
    }
    
    /**
     * Initializes the SSS contribution table with salary brackets
     * @return The SSS contribution table
     */
    private Map<Double, Double> initSSSTable() {
        Map<Double, Double> table = new HashMap<>();
        // Range: 0-4,249.99 = 180.00
        table.put(4250.0, 180.0);
        // Range: 4,250-4,749.99 = 202.50
        table.put(4750.0, 202.5);
        // Range: 4,750-5,249.99 = 225.00
        table.put(5250.0, 225.0);
        // For higher salary brackets, additional entries can be added
        // Range: 5,250-5,749.99 = 247.50
        table.put(5750.0, 247.5);
        // ... add the rest of the brackets
        
        // Maximum bracket
        table.put(Double.MAX_VALUE, 945.0);  // 20,750 and up
        return table;
    }
    
    /**
     * Calculates the gross pay based on hours worked and hourly rate
     * @param hoursWorked Total hours worked
     * @param hourlyRate Hourly rate of the employee
     * @return The gross pay amount
     */
    public double calculateGrossPay(double hoursWorked, double hourlyRate) {
        return hoursWorked * hourlyRate;
    }
    
    /**
     * Calculates the net pay from the gross pay after all deductions
     * @param grossPay The gross pay amount
     * @return The net pay amount
     */
    public double calculateNetPay(double grossPay) {
        // Set minimum values to avoid negative net pay
        double sssDeduction = Math.min(calculateSSSContribution(grossPay), grossPay * SSS_CAP_PERCENT);
        double philHealthDeduction = Math.min(calculatePhilHealthContribution(grossPay), grossPay * PHILHEALTH_CAP_PERCENT); 
        double pagIbigDeduction = Math.min(calculatePagIbigContribution(grossPay), grossPay * PAGIBIG_CAP_PERCENT);
        
        // Calculate taxable income
        double taxableIncome = grossPay - (sssDeduction + philHealthDeduction + pagIbigDeduction);
        
        // Calculate withholding tax
        double withholdingTax = Math.min(calculateWithholdingTax(taxableIncome), taxableIncome * TAX_CAP_PERCENT);
        
        // Final net pay calculation
        double netPay = grossPay - (sssDeduction + philHealthDeduction + pagIbigDeduction + withholdingTax);
        return Math.max(netPay, 0.0); // Ensure net pay is never negative
    }
    
    /**
     * Calculates SSS contribution based on gross pay
     * @param grossPay The gross pay amount
     * @return The SSS contribution amount
     */
    public double calculateSSSContribution(double grossPay) {
        // Find the applicable bracket in the SSS table
        double contribution = 0.0;
        
        for (Map.Entry<Double, Double> entry : sssTable.entrySet()) {
            if (grossPay < entry.getKey()) {
                contribution = entry.getValue();
                break;
            }
        }
        
        return contribution;
    }
    
    /**
     * Calculates PhilHealth contribution
     * @param grossPay The gross pay amount
     * @return The PhilHealth contribution amount
     */
    public double calculatePhilHealthContribution(double grossPay) {
        // PhilHealth is 3% of gross pay, split equally between employer and employee
        return (grossPay * PHILHEALTH_RATE) / 2;
    }
    
    /**
     * Calculates Pag-IBIG contribution
     * @param grossPay The gross pay amount
     * @return The Pag-IBIG contribution amount
     */
    public double calculatePagIbigContribution(double grossPay) {
        // Employee contribution is 2% of gross pay
        return grossPay * PAGIBIG_RATE;
    }
    
    /**
     * Calculates withholding tax based on taxable income
     * @param taxableIncome The taxable income amount
     * @return The withholding tax amount
     */
    public double calculateWithholdingTax(double taxableIncome) {
        // Tax bracket constants
        final double TAX_BRACKET_1 = 2083.0;
        final double TAX_BRACKET_2 = 33333.0;
        final double TAX_BRACKET_3 = 66667.0;
        final double TAX_BRACKET_4 = 166667.0;
        final double TAX_BRACKET_5 = 666667.0;
        
        // Apply progressive tax rates based on taxable income
        if (taxableIncome <= 0) {
            return 0.0;
        } else if (taxableIncome <= TAX_BRACKET_1) {
            return 0.0; // 0% tax for income up to 2,083
        } else if (taxableIncome <= TAX_BRACKET_2) {
            return (taxableIncome - TAX_BRACKET_1) * 0.20; // 20% of the excess over 2,083
        } else if (taxableIncome <= TAX_BRACKET_3) {
            return 6250 + (taxableIncome - TAX_BRACKET_2) * 0.25; // 6,250 plus 25% of the excess over 33,333
        } else if (taxableIncome <= TAX_BRACKET_4) {
            return 14583.33 + (taxableIncome - TAX_BRACKET_3) * 0.30; // 14,583.33 plus 30% of the excess over 66,667
        } else if (taxableIncome <= TAX_BRACKET_5) {
            return 44583.33 + (taxableIncome - TAX_BRACKET_4) * 0.32; // 44,583.33 plus 32% of the excess over 166,667
        } else {
            return 204583.33 + (taxableIncome - TAX_BRACKET_5) * 0.35; // 204,583.33 plus 35% of the excess over 666,667
        }
    }
}