// PayrollCalculator.java - Computes Gross and Net Salary
public class PayrollCalculator {
    // Calculates Gross Salary based on hours worked and hourly rate
    public static double calculateGrossSalary(double hoursWorked, double hourlyRate) {
        return hoursWorked * hourlyRate;
    }
    
    // Calculates Net Salary after deductions
    public static double calculateNetSalary(double grossSalary, double deductions) {
        return grossSalary - deductions;
    }
}
