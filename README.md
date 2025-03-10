# MotorPH Payroll System

## Project Overview

The MotorPH Payroll System is a Java-based application designed for comprehensive payroll management for MotorPH company employees. This system provides functionalities for employee information management, attendance tracking, payroll processing, and report generation.

## Features

- **Employee Management**
  - Search employees by name or employee number
  - List all employees with their details
  - View employee attendance records within specified date ranges

- **Payroll Management**
  - Generate payroll for all employees
  - Create custom payroll for specific employees
  - Calculate gross and net pay based on hours worked

- **Reports**
  - Generate individual payslips
  - Produce weekly summary reports
  - Create monthly summary reports

- **Automated Calculations**
  - SSS contributions based on salary brackets
  - PhilHealth contributions (3% of monthly basic salary)
  - Pag-IBIG contributions (2% of monthly basic salary)
  - Withholding tax with progressive tax rates

## Technical Details

- **Language**: Java 17+
- **Data Source**: Online CSV files (Google Sheets published as CSV)
- **Dependencies**: None required (pure Java implementation)

## Installation and Setup

1. **Clone the repository**
   ```
   git clone https://github.com/yourusername/MotorPH_Payroll_System.git
   ```

2. **Compile the Java files**
   ```
   javac -d out src/main/java/com/motorph/original/MotorPHPayroll.java
   ```

3. **Run the application**
   ```
   java -cp out com.motorph.original.MotorPHPayroll
   ```

## Usage Guide

### Main Menu
Upon launching, the system displays the main menu with four options:
1. Employee Management
2. Payroll Management
3. Reports
4. Exit

### Employee Management
- **Search Employee**: Find employees using name or ID
- **List All Employees**: Display all employees with their details
- **Attendance**: View time records for specific employees within a date range

### Payroll Management
- **Generate Payroll**: Calculate payroll for all employees within a date range
- **Custom Payroll**: Calculate payroll for a specific employee within a date range

### Reports
- **Payslip**: Generate detailed payslip for an employee
- **Weekly Summary**: Create a summary report for all employees for the week
- **Monthly Summary**: Create a summary report for all employees for the month

## Data Sources

The system fetches employee and attendance data from online CSV sources:
- Employee data: Google Sheets published as CSV
- Attendance data: Google Sheets published as CSV

## Calculations

### Hours Worked
- Calculated from time in and time out records
- Late arrivals (after 8:10 AM) are flagged in reports

### Salary Components
- **Gross Pay** = Hours Worked × Hourly Rate
- **Net Pay** = Gross Pay − (SSS + PhilHealth + Pag-IBIG + Withholding Tax)

### Deductions
- **SSS**: Based on salary brackets defined in the system
- **PhilHealth**: 3% of monthly basic salary (divided by 2 for semi-monthly)
- **Pag-IBIG**: 2% of monthly basic salary
- **Withholding Tax**: Progressive tax rates based on taxable income

## Project Structure

```
MotorPH_Payroll_System/
├── src/
│   └── main/
│       └── java/
│           └── com/
│               └── motorph/
│                   └── original/
│                       └── MotorPHPayroll.java
└── README.md
```

## Developer Notes

- Employee data is loaded from online CSV sources for ease of updates
- The system maintains in-memory storage of posted payrolls during runtime
- Error handling is implemented for date parsing and numeric inputs
- Debug information is displayed for the first three employee records on startup

## Future Enhancements

- Database integration for persistent storage
- User authentication and role-based access control
- Export capabilities for reports (PDF, Excel)
- Web interface for remote access
- Historical payroll data archiving and retrieval

---

For questions or support, contact the development team.
