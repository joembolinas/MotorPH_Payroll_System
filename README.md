# MotorPH Payroll System

## Overview
The MotorPH Payroll System is a comprehensive Java application designed to manage employee information, calculate payroll, and generate reports for MotorPH company. This system provides an easy-to-use interface for HR personnel to process payroll efficiently and accurately.

## Features
- **Employee Management**
  - Search for employees by name or ID
  - View complete employee listings
  - Track employee attendance records
  
- **Payroll Processing**
  - Generate payroll for all employees within a date range
  - Process individual employee payslips
  - Calculate regular and overtime pay
  - Pro-rate allowances based on working days
  
- **Report Generation**
  - Detailed employee payslips
  - Weekly summary reports
  - Monthly summary reports

## Technical Details
- Written in Java
- Fetches data from online CSV sources
- Calculates various deductions (SSS, PhilHealth, Pag-IBIG, Withholding Tax)
- Handles allowances (rice subsidy, phone allowance, clothing allowance)
- Processes overtime with 1.25x rate
- Supports flexible date parsing
- Provides user-friendly console interface

## Requirements
- Java 11 or higher
- Internet connection to access employee and attendance data

## How to Use
1. Compile and run the Java application
2. Navigate the menu using the number keys:
   - 1: Employee Management
   - 2: Payroll Management
   - 3: Reports
   - 4: Exit

### Generating a Payslip
1. Select "Payroll Management" → "Custom Payroll" or "Reports" → "Payslip"
2. Enter the employee number
3. Enter the date range for the payslip
4. Review the detailed breakdown

### Viewing Attendance
1. Select "Employee Management" → "Attendance"
2. Enter the employee number
3. Enter the date range
4. Review the attendance records

## Data Sources
The application retrieves data from the following sources:
- Employee Data: Google Sheets CSV export
- Attendance Records: Google Sheets CSV export

## Project Structure
- `MotorPHPayroll.java`: Main application class
- `PayrollCalculator.java`: Handles calculations for deductions and taxes

## Contributors
- MotorPH Group 1

## Version
1.0 - Initial Release (March 2025)
