// Employee.java - Stores Employee Data
class Employee {
    public int employeeNumber;
    public String lastName;
    public String firstName;
    public String birthday;
    public String address;
    public String phoneNumber;
    public String sssNumber;
    public String philhealthNumber;
    public String tinNumber;
    public String pagibigNumber;
    public String status;
    public String position;
    public String immediateSupervisor;
    public double basicSalary;
    public double riceSubsidy;
    public double phoneAllowance;
    public double clothingAllowance;
    public double grossSemiMonthlyRate;
    public double hourlyRate;

    // Constructor for Employee
    public Employee(String[] values) {
        this.employeeNumber = Integer.parseInt(values[0]);
        this.lastName = values[1];
        this.firstName = values[2];
        this.birthday = values[3];
        this.address = values[4];
        this.phoneNumber = values[5];
        this.sssNumber = values[6];
        this.philhealthNumber = values[7];
        this.tinNumber = values[8];
        this.pagibigNumber = values[9];
        this.status = values[10];
        this.position = values[11];
        this.immediateSupervisor = values[12];
        this.basicSalary = Double.parseDouble(values[13]);
        this.riceSubsidy = Double.parseDouble(values[14]);
        this.phoneAllowance = Double.parseDouble(values[15]);
        this.clothingAllowance = Double.parseDouble(values[16]);
        this.grossSemiMonthlyRate = Double.parseDouble(values[17]);
        this.hourlyRate = Double.parseDouble(values[18]);
    }

    // Display employee details
    public void displayEmployeeInfo() {
        System.out.println("Employee Number: " + employeeNumber);
        System.out.println("Name: " + firstName + " " + lastName);
        System.out.println("Position: " + position);
        System.out.println("Basic Salary: PHP " + basicSalary);
        System.out.println("Hourly Rate: PHP " + hourlyRate);
    }
}