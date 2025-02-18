import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

// GoogleSheetsCSVReader.java - Reads Employee Data from Google Sheets
class GoogleSheetsCSVReader {
    public static List<Employee> fetchEmployeesFromGoogleSheets(String sheetUrl) {
        List<Employee> employees = new ArrayList<>();

        try {
            URL url = new URL(sheetUrl);
            URLConnection connection = url.openConnection();
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String line;
            boolean firstLine = true;
            while ((line = br.readLine()) != null) {
                if (firstLine) { // Skip header row
                    firstLine = false;
                    continue;
                }

                String[] values = line.split(","); // Split CSV data into fields
                if (values.length >= 19) { // Ensure there are enough fields
                    employees.add(new Employee(values));
                }
            }
            br.close();
        } catch (MalformedURLException e) {
            System.err.println("Invalid URL: " + sheetUrl);
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Error reading from URL: " + sheetUrl);
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Unexpected error occurred");
            e.printStackTrace();
        }
        return employees;
    }

    public static List<Double> fetchAttendanceFromGoogleSheets(String sheetUrl) {
        List<Double> attendanceData = new ArrayList<>();

        try {
            URL url = new URL(sheetUrl);
            URLConnection connection = url.openConnection();
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String line;
            boolean firstLine = true;
            while ((line = br.readLine()) != null) {
                if (firstLine) { // Skip header row
                    firstLine = false;
                    continue;
                }

                String[] values = line.split(","); // Split CSV data into fields
                for (String value : values) {
                    attendanceData.add(Double.parseDouble(value));
                }
            }
            br.close();
        } catch (MalformedURLException e) {
            System.err.println("Invalid URL: " + sheetUrl);
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Error reading from URL: " + sheetUrl);
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Unexpected error occurred");
            e.printStackTrace();
        }
        return attendanceData;
    }
}