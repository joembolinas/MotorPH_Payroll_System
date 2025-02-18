// PayslipGenerator.java - Generates Employee Payslips
public class PayslipGenerator {
    public static void generatePayslip(Employee employee, double hoursWorked) {
        double hourlyRate = employee.hourlyRate;
        double grossSalary = hoursWorked * hourlyRate;
        double deductions = calculateDeductions(grossSalary);
        double netSalary = grossSalary - deductions;

        System.out.println("Payslip for " + employee.firstName + " " + employee.lastName);
        System.out.println("Hours Worked: " + hoursWorked);
        System.out.println("Gross Salary: PHP " + grossSalary);
        System.out.println("Deductions: PHP " + deductions);
        System.out.println("Net Salary: PHP " + netSalary);
    }

    private static double calculateDeductions(double grossSalary) {
        // Example deduction calculation
        double sss = grossSalary * 0.04;
        double philHealth = grossSalary * 0.015;
        double pagIbig = grossSalary * 0.02;
        double withholdingTax = grossSalary * 0.1;
        return sss + philHealth + pagIbig + withholdingTax;
    }
}
