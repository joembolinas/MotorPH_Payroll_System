# MotorPH Payroll System - Documentation

## Table of Contents

1. System Overview
2. Logical Design
3. Calculation Logic & Formulas
4. System Flow Diagram
5. UML Class Diagram
6. Data Flow Diagram
7. Components Interaction
8. Business Rules

## System Overview

The MotorPH Payroll System is a comprehensive Java application designed to manage employee information, calculate payroll, and generate reports for MotorPH company. The system enables employee data management, payroll processing, and report generation.

Key features include:
- Employee search and listing
- Attendance tracking and reporting
- Payroll calculation with overtime and allowances
- Automated deduction calculation (SSS, PhilHealth, Pag-IBIG, withholding tax)
- Report generation (payslips, weekly and monthly summaries)

The application loads employee and attendance data from online CSV sources and processes this information to calculate pay and deductions according to Philippine payroll regulations.

## Logical Design

The MotorPH Payroll System is structured around several key modules:

### 1. Data Management Module
- Handles loading and parsing of employee and attendance data from CSV sources
- Stores data in memory as lists of string arrays
- Provides search and retrieval functionality

### 2. Payroll Processing Module
- Calculates gross pay based on regular hours and overtime
- Computes pro-rated allowances
- Applies mandatory deductions
- Determines final net pay

### 3. Reports Module
- Generates payslips for individual employees
- Creates weekly and monthly summary reports
- Formats and displays attendance records

### 4. User Interface Module
- Implements a console-based menu system
- Handles user input validation
- Displays formatted output

### 5. Utility Module
- Contains helper methods for date parsing, calculations, and formatting
- Implements the SSS contribution table
- Provides reusable functionality across modules

## Calculation Logic & Formulas

### Gross Pay Calculation

```
GrossPay = RegularPay + OvertimePay

Where:
- RegularPay = RegularHours × HourlyRate
- OvertimePay = OvertimeHours × HourlyRate × OVERTIME_RATE (1.25)
- RegularHours = Min(TotalHours, REGULAR_HOURS_PER_DAY)
- OvertimeHours = Max(0, TotalHours - REGULAR_HOURS_PER_DAY)
```

### Allowance Calculation

```
ProRatedAllowance = (FullMonthlyAllowance / WORK_DAYS_PER_MONTH) × EffectiveWorkingDays

Where:
- FullMonthlyAllowance = RiceSubsidy + PhoneAllowance + ClothingAllowance
- EffectiveWorkingDays = Min(ActualWorkingDays, WORK_DAYS_PER_MONTH)
- ActualWorkingDays = Number of weekdays in the selected period
```

### Deduction Calculation

```
TotalDeductions = SSS + PhilHealth + PagIBIG + WithholdingTax

Where:
- SSS = Based on SSS contribution table (determined by salary bracket)
- PhilHealth = (GrossPay × 0.03) / 2 (employee's share)
- PagIBIG = GrossPay × 0.02
- TaxableIncome = GrossPay - (SSS + PhilHealth + PagIBIG)
- WithholdingTax = Based on progressive tax table
```

### Net Pay Calculation

```
NetPay = GrossPay - TotalDeductions + TotalAllowances
```

### Progressive Tax Table

```
If TaxableIncome ≤ 2,083: Tax = 0
If 2,083 < TaxableIncome ≤ 33,333: Tax = (TaxableIncome - 2,083) × 0.20
If 33,333 < TaxableIncome ≤ 66,667: Tax = 6,250 + (TaxableIncome - 33,333) × 0.25
If 66,667 < TaxableIncome ≤ 166,667: Tax = 14,583.33 + (TaxableIncome - 66,667) × 0.30
If 166,667 < TaxableIncome ≤ 666,667: Tax = 44,583.33 + (TaxableIncome - 166,667) × 0.32
If TaxableIncome > 666,667: Tax = 204,583.33 + (TaxableIncome - 666,667) × 0.35
```

## System Flow Diagram

```mermaid
flowchart TD
    A[Start] --> B[Load Employee Data]
    B --> C[Load Attendance Data]
    C --> D[Display Main Menu]
    
    D --> |1| E[Employee Management]
    D --> |2| F[Payroll Management]
    D --> |3| G[Reports]
    D --> |4| H[Exit]
    
    E --> E1[Search Employee]
    E --> E2[List All Employees]
    E --> E3[View Attendance]
    E --> E4[Return to Main Menu]
    E4 --> D
    
    F --> F1[Generate All Payroll]
    F --> F2[Custom Payroll]
    F --> F3[Return to Main Menu]
    F3 --> D
    
    G --> G1[Payslip]
    G --> G2[Weekly Summary]
    G --> G3[Monthly Summary]
    G --> G4[Return to Main Menu]
    G4 --> D
    
    F1 --> F1a[Enter Date Range]
    F1a --> F1b[Calculate Pay for All Employees]
    F1b --> F1c[Display Results]
    F1c --> F1d[Return to Payroll Menu]
    F1d --> F
    
    F2 --> F2a[Enter Employee ID]
    F2a --> F2b[Enter Date Range]
    F2b --> F2c[Calculate Pay for Employee]
    F2c --> F2d[Display Detailed Payslip]
    F2d --> F2e[Return to Payroll Menu]
    F2e --> F
    
    G1 --> F2
    
    G2 --> G2a[Enter Date Range]
    G2a --> G2b[Calculate Weekly Summary]
    G2b --> G2c[Display Results]
    G2c --> G2d[Return to Reports Menu]
    G2d --> G
    
    G3 --> G3a[Enter Date Range]
    G3a --> G3b[Calculate Monthly Summary]
    G3b --> G3c[Display Results]
    G3c --> G3d[Return to Reports Menu]
    G3d --> G
    
    H --> I[End]
```

## UML Class Diagram

```mermaid
classDiagram
    class MotorPHPayroll {
        -double OVERTIME_RATE
        -int REGULAR_HOURS_PER_DAY
        -int WORK_DAYS_PER_MONTH
        -LocalTime LATE_THRESHOLD
        -Map~Double, Double~ SSS_TABLE
        -PayrollCalculator payrollCalculator
        +main(String[] args)
        -initSSSTable()
        -loadEmployeesFromCSV(String url)
        -loadAttendanceFromCSV(String url)
        -employeeManagement(List employees, List attendanceRecords, Scanner scanner)
        -payrollManagement(List employees, List attendanceRecords, Scanner scanner)
        -reportsMenu(List employees, List attendanceRecords, Scanner scanner)
        -generatePayroll(List employees, List attendanceRecords, Scanner scanner)
        -generateEmployeePayslip(List employees, List attendanceRecords, Scanner scanner, String title)
        -generateSummaryReport(List employees, List attendanceRecords, String period)
        -viewAttendance(List employees, List attendanceRecords, Scanner scanner)
        -isRecordForEmployee(String[] record, int empNumber)
        -parseFlexibleDate(String dateStr)
        -extractHourlyRate(String[] employee)
        -extractAllowance(String[] employee, int columnIndex)
        -getProRatedAllowanceDetails(String[] employee, LocalDate startDate, LocalDate endDate)
        -getDateInput(Scanner scanner, String prompt)
        -searchEmployee(List employees, Scanner scanner)
        -listAllEmployees(List employees)
        -findEmployeeById(List employees, int empNumber)
        -formatEmployeeName(String[] employee)
        -getGrossPayDetails(List attendanceRecords, int empNumber, double hourlyRate, LocalDate startDate, LocalDate endDate)
    }
    
    class PayrollCalculator {
        -double PHILHEALTH_RATE
        -double PHILHEALTH_CAP_PERCENT
        -double PAGIBIG_RATE
        -double PAGIBIG_CAP_PERCENT
        -double SSS_CAP_PERCENT
        -double TAX_CAP_PERCENT
        -Map~Double, Double~ sssTable
        +PayrollCalculator()
        -initSSSTable()
        +calculateGrossPay(double hoursWorked, double hourlyRate)
        +calculateNetPay(double grossPay)
        +calculateSSSContribution(double grossPay)
        +calculatePhilHealthContribution(double grossPay)
        +calculatePagIbigContribution(double grossPay)
        +calculateWithholdingTax(double taxableIncome)
    }
    
    MotorPHPayroll --> PayrollCalculator : uses
```

## Data Flow Diagram

```mermaid
flowchart TD
    DS1[(Employee CSV)] --> P1
    DS2[(Attendance CSV)] --> P1
    
    P1[Load Data] --> D1[Employee Records]
    P1 --> D2[Attendance Records]
    
    D1 --> P2[Search/List Employees]
    D1 --> P3[Process Payroll]
    D2 --> P4[Track Attendance]
    D2 --> P3
    
    P2 --> D3[Search Results]
    P3 --> D4[Payroll Results]
    P4 --> D5[Attendance Records]
    
    D3 --> P5[Display Output]
    D4 --> P5
    D5 --> P5
    
    subgraph User Interface
    P5 --> D6[Console Display]
    end
    
    D6 --> P6[User Input]
    P6 --> P2
    P6 --> P3
    P6 --> P4
```

## Components Interaction

```mermaid
sequenceDiagram
    actor User
    participant UI as User Interface
    participant EM as Employee Manager
    participant PM as Payroll Manager
    participant RM as Report Manager
    participant PC as PayrollCalculator
    participant DS as Data Store
    
    User->>UI: Select option
    
    alt Employee Management
        UI->>EM: Forward request
        EM->>DS: Request employee data
        DS->>EM: Return employee data
        EM->>UI: Display results
        UI->>User: Show formatted output
    
    else Payroll Management
        UI->>PM: Forward request
        PM->>DS: Request employee data
        DS->>PM: Return employee data
        PM->>DS: Request attendance data
        DS->>PM: Return attendance data
        PM->>PC: Calculate payroll
        PC->>PM: Return calculation results
        PM->>UI: Display results
        UI->>User: Show formatted output
    
    else Reports
        UI->>RM: Forward request
        RM->>DS: Request employee data
        DS->>RM: Return employee data
        RM->>DS: Request attendance data
        DS->>RM: Return attendance data
        RM->>PC: Calculate figures for report
        PC->>RM: Return calculation results
        RM->>UI: Display results
        UI->>User: Show formatted output
    end
```

## Business Rules

1. **Employee Classification**
   - Employees are classified by position and status
   - Different positions may have different default hourly rates

2. **Working Hours**
   - Regular working hours are 8 hours per day
   - Hours worked beyond 8 hours in a day are considered overtime
   - Overtime is paid at 1.25 times the regular hourly rate

3. **Allowances**
   - Employees receive rice subsidy, phone allowance, and clothing allowance
   - Allowances are pro-rated based on working days in the pay period
   - Standard working days per month is 21 days (excluding weekends)

4. **Attendance Rules**
   - An employee arriving after 8:10 AM is considered late
   - Attendance records track time in and time out
   - Only weekdays (Monday-Friday) are considered working days

5. **Deduction Rules**
   - SSS contributions follow the official SSS contribution table
   - PhilHealth contribution is 3% of gross pay (1.5% employee share)
   - Pag-IBIG contribution is 2% of gross pay
   - Withholding tax follows the progressive tax table

6. **Pay Calculation**
   - Gross pay includes regular pay and overtime pay
   - Net pay equals gross pay minus deductions plus allowances
   - Hourly rate is calculated from monthly salary if not directly available
   - Pay is calculated on a daily basis to accurately account for overtime