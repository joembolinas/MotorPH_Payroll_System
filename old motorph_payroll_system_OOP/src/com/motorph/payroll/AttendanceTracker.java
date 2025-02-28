package com.motorph.payroll; //error palagi sa package name

import java.time.Duration; //import ducation class
import java.time.LocalTime; // import LocalTime class to use as data type
import java.time.format.DateTimeFormatter; // import DateTimeFormatter class to format time
import java.util.HashMap; // import HashMap class to use as data structure for attendance records
import java.util.Map; // import Map class to use as data structure for attendance records

public class AttendanceTracker { // create a class named AttendanceTracker
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("H:mm"); // create a constant variable TIME_FORMATTER to format time/ this line more error prone
    private static final LocalTime OFFICE_START_TIME = LocalTime.of(8, 30); // Office hours start at 8:30 AM
    private static final LocalTime OFFICE_END_TIME = LocalTime.of(17, 30); // Office hours end at 5:30 PM
    private static final int GRACE_PERIOD_MINUTES = 10; // 10-minute palugit 

    // Map to store attendance data (Employee ID -> Login and Logout times)
    private final Map<Integer, AttendanceRecord> attendanceRecords = new HashMap<>(); // initialize a Map data structure to store attendance records

    // Inner class to store login and logout times for an employee
    private static class AttendanceRecord {
        LocalTime loginTime; // create a variable loginTime of type LocalTime
        LocalTime logoutTime; // create a variable logoutTime of type LocalTime

        AttendanceRecord(LocalTime loginTime, LocalTime logoutTime) {
            this.loginTime = loginTime; // assign loginTime to the value of the parameter loginTime
            this.logoutTime = logoutTime; // assign logoutTime to the value of the parameter logoutTime
        }
    }

    // Add attendance record for an employee
    public void addAttendanceRecord(int employeeNumber, String loginTimeStr, String logoutTimeStr) {
        try {
            LocalTime loginTime = LocalTime.parse(loginTimeStr, TIME_FORMATTER);   // parse the loginTimeStr to LocalTime
            LocalTime logoutTime = LocalTime.parse(logoutTimeStr, TIME_FORMATTER); // parse the logoutTimeStr to LocalTime

            // Apply grace period for late logins
            if (loginTime.isAfter(OFFICE_START_TIME.plusMinutes(GRACE_PERIOD_MINUTES))) {
                System.out.printf("Employee %d logged in late at %s%n", employeeNumber, loginTime); // print the employee number and login time
            }

            attendanceRecords.put(employeeNumber, new AttendanceRecord(loginTime, logoutTime)); // add the attendance record to the map
        } catch (Exception e) {
            System.err.printf("Invalid time format for employee %d: %s%n", employeeNumber, e.getMessage()); // print error message if time format is invalid
        }
    }

    // Calculate hours worked for an employee
    public double calculateHoursWorked(int employeeNumber) { // create a method named calculateHoursWorked that takes an int  employeeNumber
        AttendanceRecord record = attendanceRecords.get(employeeNumber); // get the attendance record for the employee number
        if (record == null) {
            System.err.printf("No attendance record found for employee %d%n", employeeNumber); // print error message if no attendance record is found
            return 0.0;
        }

        // Calculate hours worked
        Duration duration = Duration.between(record.loginTime, record.logoutTime); // calculate the duration between login and logout time
        double hoursWorked = duration.toMinutes() / 60.0; // convert 
        System.out.println("Debug: Attendance Record for Employee = " + hoursWorked);

        // Ensure hours worked do not exceed office hours
        if (record.logoutTime.isAfter(OFFICE_END_TIME)) {
            hoursWorked = Duration.between(record.loginTime, OFFICE_END_TIME).toMinutes() / 60.0; // calculate the duration between login and office end time
        }

        return hoursWorked;
    }
}

/*
public class AttendanceTracker {

    private final Map<Integer, Duration> employeeWorkHours;

    public AttendanceTracker() {
        this.employeeWorkHours = new HashMap<>();
    }

    public void processAttendanceData(List<String[]> attendanceRecords, List<Employee> employees) {
        for (String[] record : attendanceRecords) {
            try {
                int employeeNumber = Integer.parseInt(record[0].trim());
                LocalTime loginTime = parseTime(record[4].trim());
                LocalTime logoutTime = parseTime(record[5].trim());

                // Apply 10-minute grace period
                if (loginTime.isAfter(LocalTime.of(8, 10))) {
                    loginTime = LocalTime.of(8, 10);
                }

                Duration workDuration = Duration.between(loginTime, logoutTime);
                employeeWorkHours.merge(employeeNumber, workDuration, Duration::plus);
            } catch (Exception e) {
                System.err.println("Error processing attendance record: " + e.getMessage());
            }
        }
    }

    public Duration getWorkHours(int employeeNumber) {
        return employeeWorkHours.getOrDefault(employeeNumber, Duration.ZERO);
    }

    private LocalTime parseTime(String timeString) {
        String[] parts = timeString.split(":");
        int hour = Integer.parseInt(parts[0]);
        int minute = Integer.parseInt(parts[1]);
        return LocalTime.of(hour, minute);
    }
} */