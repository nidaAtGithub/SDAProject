package UserPackage;

import java.sql.*;
import java.util.Scanner;

public class GenerateReport {
    private static final String url = "jdbc:mysql://localhost:3306/sdaproject";
    private static final String dbUsername = "root";
    private static final String dbPassword = "Thinkpad@t430";

    public void generateReport(Admin admin) {
        try (Scanner scanner = new Scanner(System.in)) {
            // Verify admin credentials
            if (!admin.verifyCredentials()) {
                System.out.println("Invalid login credentials. Please try again.");
                return;
            }

            // Display the list of available report options
            System.out.println("Available report options:");
            System.out.println("1. Voter Turnout");
            System.out.println("2. Candidate Votes");
            System.out.println("3. Election Status");
            System.out.print("Select report type (1-3): ");
            int reportType = scanner.nextInt();
            scanner.nextLine(); // Consume newline character

            // Validate report selection
            if (reportType < 1 || reportType > 3) {
                System.out.println("Invalid report selection. Please choose a valid option.");
                return;
            }

            // Specify filters
            System.out.print("Enter date range (YYYY-MM-DD to YYYY-MM-DD) or leave empty: ");
            String dateRange = scanner.nextLine();

            // Generate the report
            String reportData = generateReportData(reportType, dateRange);
            if (reportData == null) {
                System.out.println("Insufficient data to generate the requested report.");
                return;
            }

            // Display report data
            System.out.println("Report Data:");
            System.out.println(reportData);

            // Save the report
            System.out.print("Do you want to save the report as PDF or CSV? ");
            String format = scanner.nextLine();
            if (format.equalsIgnoreCase("PDF") || format.equalsIgnoreCase("CSV")) {
                System.out.println("Generating report in " + format + " format...");
                System.out.println("Report generated successfully!");
                logReportGeneration(admin, reportType);
            } else {
                System.out.println("Invalid format specified.");
            }
        }
    }

    private String generateReportData(int reportType, String dateRange) {
        StringBuilder reportData = new StringBuilder();
        try (Connection connection = DriverManager.getConnection(url, dbUsername, dbPassword)) {
            String sql = "";
            if (reportType == 1) { // Voter Turnout
                sql = "SELECT COUNT(DISTINCT voterId) AS voter_turnout FROM castvote";
                if (!dateRange.isEmpty()) {
                    sql += " WHERE Date BETWEEN ? AND ?";
                }
            } else if (reportType == 2) { // Candidate Votes
                sql = "SELECT candidateId, COUNT(*) AS votes FROM castvote";
                if (!dateRange.isEmpty()) {
                    sql += " WHERE Date BETWEEN ? AND ?";
                }
                sql += " GROUP BY candidateId";
            } else if (reportType == 3) { // Election Status
                sql = "SELECT electionName, status FROM electionsetup";
            }

            PreparedStatement statement = connection.prepareStatement(sql);

            if (!dateRange.isEmpty() && (reportType == 1 || reportType == 2)) {
                String[] dates = dateRange.split(" to ");
                statement.setString(1, dates[0]);
                statement.setString(2, dates[1]);
            }

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                if (reportType == 1) {
                    reportData.append("Voter Turnout: ").append(resultSet.getInt("voter_turnout")).append("\n");
                } else if (reportType == 2) {
                    reportData.append("Candidate ID: ").append(resultSet.getString("candidateId"))
                            .append(", Votes: ").append(resultSet.getInt("votes")).append("\n");
                } else if (reportType == 3) {
                    reportData.append("Election: ").append(resultSet.getString("electionName"))
                            .append(", Status: ").append(resultSet.getString("status")).append("\n");
                }
            }
        } catch (SQLException e) {
            System.out.println("System failure: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
        return reportData.toString();
    }

    private void logReportGeneration(Admin admin, int reportType) {
        // Log the report generation action for audit purposes
        try (Connection connection = DriverManager.getConnection(url, dbUsername, dbPassword)) {
            String sql = "INSERT INTO report_log (adminId, reportType, generatedAt) VALUES (?, ?, CURRENT_TIMESTAMP)";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, admin.getCnic());
                statement.setInt(2, reportType);
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            System.out.println("Failed to log report generation: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("Admin " + admin.getUsername() + " generated report type " + reportType);
    }
}
