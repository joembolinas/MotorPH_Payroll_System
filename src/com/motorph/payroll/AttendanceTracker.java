// AttendanceTracker.java - Tracks Employee Work Hours
import java.util.List;

public class AttendanceTracker {
    // Returns the total hours worked
    public static double getHoursWorked(List<Double> attendanceData) {
        double totalHours = 0.0;
        for (double hours : attendanceData) {
            totalHours += hours;
        }
        return totalHours;
    }
}
