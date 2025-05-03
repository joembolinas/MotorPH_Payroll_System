package com.motorph.original;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles all payroll-related calculations for the MotorPH payroll system.
 * This class encapsulates the logic for calculating gross pay, net pay, and various deductions
 * such as SSS, PhilHealth, Pag-IBIG, and withholding tax.
 * 
 * The class uses predefined rates and tax brackets as per Philippine regulations
 * to ensure accurate payroll computations.
 */
public class PayrollCalculator {
    
    // Constants for calculation rates
    /**
     * The standard PhilHealth contribution rate (3% of monthly basic salary)
     * This is the total rate, which is split equally between employer and employee
     */
    private static final double PHILHEALTH_RATE = 0.03;
    
    /**
     * Maximum percentage of gross pay that can be deducted for PhilHealth
     * Used to ensure deductions don't exceed reasonable limits
     */
    private static final double PHILHEALTH_CAP_PERCENT = 0.03;
    
    /**
     * The standard Pag-IBIG contribution rate (2% of monthly basic salary)
     * This represents the employee's contribution portion
     */
    private static final double PAGIBIG_RATE = 0.02;
    
    /**
     * Maximum percentage of gross pay that can be deducted for Pag-IBIG
     * Used to ensure deductions don't exceed reasonable limits
     */
    private static final double PAGIBIG_CAP_PERCENT = 0.02;
    
    /**
     * Maximum percentage of gross pay that can be deducted for SSS
     * Used to ensure deductions don't exceed reasonable limits
     */
    private static final double SSS_CAP_PERCENT = 0.10;
    
    /**
     * Maximum percentage of taxable income that can be deducted as withholding tax
     * Used to ensure tax deductions don't exceed reasonable limits
     */
    private static final double TAX_CAP_PERCENT = 0.20;
    
    /**
     * SSS contribution table that maps salary brackets (upper limits) to corresponding
     * contribution amounts. This follows the official SSS contribution table.
     */
    private final Map<Double, Double> sssTable;
    
    /**
     * Creates a new PayrollCalculator with initialized SSS table.
     * The constructor initializes the SSS contribution table which is used
     * to determine the appropriate SSS contribution based on an employee's salary.
     */
    public PayrollCalculator() {
        this.sssTable = initSSSTable();
    }
    
    /**
     * Initializes the SSS contribution table with salary brackets and their corresponding
     * contribution amounts according to the latest SSS contribution schedule.
     * 
     * Each entry in the table represents a salary bracket upper limit and the
     * corresponding SSS contribution amount for that bracket.
     * 
     * @return A map with salary bracket upper limits as keys and contribution amounts as values
     */
    private Map<Double, Double> initSSSTable() {
        Map<Double, Double> table = new HashMap<>();
        // Range: 0-4,249.99 = 180.00
        // This means employees with monthly salary from 0 to 4,249.99 pesos
        // will have an SSS contribution of 180.00 pesos
        table.put(4250.0, 180.0);
        
        // Range: 4,250-4,749.99 = 202.50
        // This means employees with monthly salary from 4,250 to 4,749.99 pesos
        // will have an SSS contribution of 202.50 pesos
        table.put(4750.0, 202.5);
        
        // Range: 4,750-5,249.99 = 225.00
        // This means employees with monthly salary from 4,750 to 5,249.99 pesos
        // will have an SSS contribution of 225.00 pesos
        table.put(5250.0, 225.0);
        
        // Range: 5,250-5,749.99 = 247.50
        // This means employees with monthly salary from 5,250 to 5,749.99 pesos
        // will have an SSS contribution of 247.50 pesos
        table.put(5750.0, 247.5);
        
        // Additional brackets would be added here following the same pattern
        // For brevity, not all brackets are included in this example
        
        // Maximum bracket: 20,750 and up = 945.00
        // This is the maximum SSS contribution for the highest salary bracket
        table.put(Double.MAX_VALUE, 945.0);
        
        return table;
    }
    
    /**
     * Calculates the gross pay based on the total hours worked and the employee's hourly rate.
     * Gross pay is the total compensation before any deductions are applied.
     * 
     * Formula: Gross Pay = Hours Worked × Hourly Rate
     * 
     * @param hoursWorked Total hours worked during the pay period
     * @param hourlyRate Hourly rate of the employee in pesos
     * @return The gross pay amount in pesos
     */
    public double calculateGrossPay(double hoursWorked, double hourlyRate) {
        return hoursWorked * hourlyRate;
    }
    
    /**
     * Calculates the net pay from the gross pay after all mandatory deductions.
     * Net pay is the actual take-home pay after all deductions are subtracted
     * from the gross pay.
     * 
     * Deductions include:
     * - SSS (Social Security System)
     * - PhilHealth (Philippine Health Insurance)
     * - Pag-IBIG (Home Development Mutual Fund)
     * - Withholding Tax
     * 
     * Formula: Net Pay = Gross Pay - (SSS + PhilHealth + Pag-IBIG + Withholding Tax)
     * 
     * @param grossPay The gross pay amount before deductions
     * @return The net pay amount after all deductions (never negative)
     */
    public double calculateNetPay(double grossPay) {
        // Calculate individual deductions with caps to avoid excessive deductions
        // Cap each deduction as a percentage of gross pay to ensure reasonable limits
        double sssDeduction = Math.min(calculateSSSContribution(grossPay), grossPay * SSS_CAP_PERCENT);
        double philHealthDeduction = Math.min(calculatePhilHealthContribution(grossPay), grossPay * PHILHEALTH_CAP_PERCENT); 
        double pagIbigDeduction = Math.min(calculatePagIbigContribution(grossPay), grossPay * PAGIBIG_CAP_PERCENT);
        
        // Calculate taxable income by subtracting non-taxable deductions from gross pay
        double taxableIncome = grossPay - (sssDeduction + philHealthDeduction + pagIbigDeduction);
        
        // Calculate withholding tax with a cap to avoid excessive taxation
        double withholdingTax = Math.min(calculateWithholdingTax(taxableIncome), taxableIncome * TAX_CAP_PERCENT);
        
        // Final net pay calculation: Gross Pay - All Deductions
        double netPay = grossPay - (sssDeduction + philHealthDeduction + pagIbigDeduction + withholdingTax);
        
        // Ensure net pay is never negative (safeguard against calculation errors)
        return Math.max(netPay, 0.0);
    }
    
    /**
     * Calculates the SSS contribution based on the employee's gross pay.
     * This method uses the SSS contribution table to determine the appropriate
     * contribution amount based on the salary bracket.
     * 
     * The SSS contribution is a social security deduction that provides benefits
     * for illness, maternity, disability, retirement, and death.
     * 
     * @param grossPay The gross pay amount for the contribution calculation
     * @return The SSS contribution amount in pesos
     */
    public double calculateSSSContribution(double grossPay) {
        // Find the applicable bracket in the SSS table
        double contribution = 0.0;
        
        // Iterate through the SSS table entries (sorted by salary bracket)
        // Find the first bracket where the employee's gross pay is less than the bracket upper limit
        for (Map.Entry<Double, Double> entry : sssTable.entrySet()) {
            if (grossPay < entry.getKey()) {
                contribution = entry.getValue();
                break;
            }
        }
        
        return contribution;
    }
    
    /**
     * Calculates the PhilHealth contribution based on the employee's gross pay.
     * PhilHealth is the national health insurance program of the Philippines.
     * 
     * The total PhilHealth contribution is typically 3% of the monthly basic salary,
     * split equally between the employer and employee (1.5% each).
     * This method calculates only the employee's portion.
     * 
     * @param grossPay The gross pay amount for the contribution calculation
     * @return The employee's PhilHealth contribution amount in pesos
     */
    public double calculatePhilHealthContribution(double grossPay) {
        // PhilHealth is 3% of gross pay, split equally between employer and employee (1.5% each)
        // This calculates the employee's portion only
        return (grossPay * PHILHEALTH_RATE) / 2;
    }
    
    /**
     * Calculates the Pag-IBIG contribution based on the employee's gross pay.
     * Pag-IBIG (Home Development Mutual Fund) is a national savings program and
     * provides affordable housing financing for Filipinos.
     * 
     * The standard employee contribution is 2% of the monthly basic salary.
     * 
     * @param grossPay The gross pay amount for the contribution calculation
     * @return The Pag-IBIG contribution amount in pesos
     */
    public double calculatePagIbigContribution(double grossPay) {
        // Standard Pag-IBIG contribution is 2% of gross pay for the employee
        return grossPay * PAGIBIG_RATE;
    }
    
    /**
     * Calculates the withholding tax based on the employee's taxable income.
     * Withholding tax is computed using the progressive tax table following
     * Philippine tax regulations.
     * 
     * The tax computation follows these steps:
     * 1. Determine which tax bracket the taxable income falls into
     * 2. Apply the corresponding tax formula for that bracket
     * 
     * @param taxableIncome The taxable income amount after non-taxable deductions
     * @return The withholding tax amount in pesos
     */
    public double calculateWithholdingTax(double taxableIncome) {
        // Tax bracket constants according to the Philippine tax table
        // These represent the upper limits of each tax bracket
        final double TAX_BRACKET_1 = 2083.0;    // 0% tax bracket (up to 2,083 pesos)
        final double TAX_BRACKET_2 = 33333.0;   // 20% tax bracket (2,083 to 33,333 pesos)
        final double TAX_BRACKET_3 = 66667.0;   // 25% tax bracket (33,333 to 66,667 pesos)
        final double TAX_BRACKET_4 = 166667.0;  // 30% tax bracket (66,667 to 166,667 pesos)
        final double TAX_BRACKET_5 = 666667.0;  // 32% tax bracket (166,667 to 666,667 pesos)
                                               // 35% tax bracket (over 666,667 pesos)
        
        // Apply progressive tax rates based on which bracket the taxable income falls into
        // Each bracket has a fixed base tax plus a percentage of the excess over the bracket lower limit
        if (taxableIncome <= 0) {
            // No tax for zero or negative taxable income (should not occur in normal circumstances)
            return 0.0;
        } else if (taxableIncome <= TAX_BRACKET_1) {
            // First bracket: 0% tax for income up to 2,083 pesos
            return 0.0;
        } else if (taxableIncome <= TAX_BRACKET_2) {
            // Second bracket: 20% of the excess over 2,083 pesos
            return (taxableIncome - TAX_BRACKET_1) * 0.20;
        } else if (taxableIncome <= TAX_BRACKET_3) {
            // Third bracket: 6,250 pesos plus 25% of the excess over 33,333 pesos
            return 6250 + (taxableIncome - TAX_BRACKET_2) * 0.25;
        } else if (taxableIncome <= TAX_BRACKET_4) {
            // Fourth bracket: 14,583.33 pesos plus 30% of the excess over 66,667 pesos
            return 14583.33 + (taxableIncome - TAX_BRACKET_3) * 0.30;
        } else if (taxableIncome <= TAX_BRACKET_5) {
            // Fifth bracket: 44,583.33 pesos plus 32% of the excess over 166,667 pesos
            return 44583.33 + (taxableIncome - TAX_BRACKET_4) * 0.32;
        } else {
            // Sixth bracket: 204,583.33 pesos plus 35% of the excess over 666,667 pesos
            return 204583.33 + (taxableIncome - TAX_BRACKET_5) * 0.35;
        }
    }
}