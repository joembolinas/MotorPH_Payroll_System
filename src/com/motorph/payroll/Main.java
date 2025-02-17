// Main.java - Entry Point for the Program
public class Main {
    public static void main(String[] args) {
        // Create an Employee instance
        Employee emp1 = new Employee();
        
        // Display Employee Information
        System.out.println("Employee Number: " + emp1.employeeNumber);
        System.out.println("Name: " + emp1.firstName + " " + emp1.lastName);
        System.out.println("Birthday: " + emp1.birthday);
        System.out.println("Address: " + emp1.address);
        System.out.println("Phone Number: " + emp1.phoneNumber);
        System.out.println("SSS Number: " + emp1.sssNumber);
        System.out.println("PhilHealth Number: " + emp1.philhealthNumber);
        System.out.println("TIN Number: " + emp1.tinNumber);
        System.out.println("Pag-IBIG Number: " + emp1.pagibigNumber);
        System.out.println("Status: " + emp1.status);
        System.out.println("Position: " + emp1.position);
        System.out.println("Immediate Supervisor: " + emp1.immediateSupervisor);
        System.out.println("Basic Salary: PHP " + emp1.basicSalary);
        System.out.println("Rice Subsidy: PHP " + emp1.riceSubsidy);
        System.out.println("Phone Allowance: PHP " + emp1.phoneAllowance);
        System.out.println("Clothing Allowance: PHP " + emp1.clothingAllowance);
        System.out.println("Gross Semi-Monthly Rate: PHP " + emp1.grossSemiMonthlyRate);
        System.out.println("Hourly Rate: PHP " + emp1.hourlyRate);
    }
}
