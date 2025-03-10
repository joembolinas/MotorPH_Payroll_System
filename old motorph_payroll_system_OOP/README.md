# MotorPH_Payroll_System

 Group 2 T2_AY2425


# MotorPH Payroll System - User Manual

## Table of Contents

1. Introduction
2. Getting Started
3. Main Menu
4. Employee Management
5. Payroll Management
6. Reports
7. Troubleshooting

## Introduction

The MotorPH Payroll System is a comprehensive Java-based application designed to manage employee records, attendance tracking, payroll processing, and reporting. This system provides an integrated solution for Human Resources and Accounting departments to efficiently handle payroll-related tasks.

### Key Features:

- Employee search and listing functionality
- Attendance tracking and reporting
- Payroll generation for all employees or individual employees
- Detailed payslip generation
- Weekly and monthly summary reports
- SSS, PhilHealth, and Pag-IBIG contribution calculations
- Tax withholding calculations based on current Philippine tax brackets

## Getting Started

### System Requirements

- Java Runtime Environment (JRE) 11 or higher
- Internet connection (to access employee and attendance data)
- Minimum 4GB RAM recommended

### First-Time Setup

1. Ensure Java is installed on your system
2. Launch the application by running the JAR file or executing the main class
3. The system will automatically connect to the data source and load employee and attendance records

## Main Menu

Upon launching the application, you will be presented with the main menu:

```
=== MotorPH Payroll System ===
1. Employee Management
2. Payroll Management
3. Reports
4. Exit
Enter your choice:
```

Enter the number corresponding to the desired option and press Enter.

## Employee Management

The Employee Management module allows you to search for employees, view employee lists, and check attendance records.

### Search Employee

1. From the Employee Management menu, select option 1 (Search Employee)
2. Enter the search term (employee number, first name, or last name)
3. The system will display matching employee records with their details:
   - Employee Number
   - Name
   - Position
   - Status
   - Hourly Rate

### List All Employees

1. From the Employee Management menu, select option 2 (List All Employees)
2. The system will display all employees with their details in a tabular format

### View Attendance

1. From the Employee Management menu, select option 3 (Attendance)
2. Enter the employee number
3. Enter the date range (From and To) in MM/DD/YYYY format
4. The system will display:
   - Attendance dates
   - Time In and Time Out
   - Duration of work (in hours)
   - Remarks (On Time or Late)

## Payroll Management

The Payroll Management module allows you to generate payroll for all employees or for specific employees.

### Generate Payroll (All Employees)

1. From the Payroll Management menu, select option 1 (Generate Payroll)
2. Enter the date range (From and To) in MM/DD/YYYY format
3. The system will display the payroll for all employees, including:
   - Employee Number
   - Name
   - Total Hours Worked
   - Hourly Rate
   - Gross Pay
   - Net Pay
4. After viewing the payroll, you can:
   - Post the payroll (save it to the system for future reference)
   - Edit the payroll (feature not yet implemented)

### Custom Payroll (Individual Employee)

1. From the Payroll Management menu, select option 2 (Custom Payroll)
2. Enter the employee number
3. Enter the date range (From and To) in MM/DD/YYYY format
4. The system will display:
   - Employee Number and Name
   - Total Work Hours
   - Gross Pay
   - Net Pay
5. You can then choose to post the payroll or edit it

## Reports

The Reports module provides various reports for analysis and documentation.

### Payslip

1. From the Reports menu, select option 1 (Payslip)
2. Enter the employee number
3. Enter the date range (From and To) in MM/DD/YYYY format
4. The system will generate and display a payslip showing:
   - Employee Number and Name
   - Total Work Hours
   - Gross Pay
   - Net Pay

### Weekly Summary

1. From the Reports menu, select option 2 (Weekly Summary)
2. The system will display a weekly summary report for all employees, showing:
   - Employee Number
   - Name
   - Total Work Hours
   - Net Pay
   - Gross Pay

### Monthly Summary

1. From the Reports menu, select option 3 (Monthly Summary)
2. The system will display a monthly summary report with the same details as the weekly report but calculated for the entire month

## Troubleshooting

### Common Issues and Solutions

**Issue**: The system displays "Error loading data" at startup.
**Solution**: Check your internet connection. The system needs to access CSV files stored online.

**Issue**: Incorrect date format error.
**Solution**: Always enter dates in MM/DD/YYYY format (e.g., 03/15/2025).

**Issue**: Employee not found when searching.
**Solution**: Ensure you're entering the correct employee number or name. Search is case-insensitive.

**Issue**: Zero or incorrect hourly rate shown.
**Solution**: The system has fallback mechanisms to determine hourly rates. If the direct rate is unavailable, it calculates based on the employee's basic salary or position.

**Issue**: Attendance records not showing.
**Solution**: Verify that the employee has attendance records for the specified date range.

### How Calculations Are Performed

1. **Hours Worked**: Calculated as the duration between clock-in and clock-out times.
2. **Gross Pay**: Hours worked × Hourly rate.
3. **Net Pay**: Gross pay minus deductions:
   - SSS contribution (based on a contribution table)
   - PhilHealth contribution (3% of monthly basic salary, divided by 2)
   - Pag-IBIG contribution (2% of monthly basic salary)
   - Withholding tax (based on Philippine tax brackets)

### Data Security

All data is processed locally on your machine. The system fetches data from secured online sources but does not transmit any processed information back to external servers.

---

For additional support or to report issues, please contact the IT support team or the system administrator.
