// Deductions.java - Applies Government Deductions (SSS, PhilHealth, Pag-IBIG, Tax)
public class Deductions {
    // Computes Total Deductions based on Gross Salary
    public static double computeTotalDeductions(double grossSalary) {
        double sss = grossSalary * 0.11; // SSS Deduction (11%)
        double philHealth = grossSalary * 0.035; // PhilHealth Deduction (3.5%)
        double pagIbig = grossSalary * 0.02; // Pag-IBIG Deduction (2%)
        double tax = grossSalary * 0.1; // Income Tax Deduction (10%)
        return sss + philHealth + pagIbig + tax;
    }
}