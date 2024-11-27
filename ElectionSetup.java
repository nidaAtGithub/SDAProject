package UserPackage;

import java.sql.*;
import java.util.Scanner;

public class ElectionSetup {
    private static final String url = "jdbc:mysql://localhost:3306/sdaproject";
    private static final String dbUsername = "root";
    private static final String dbPassword = "Thinkpad@t430";
    private static final Scanner sc = new Scanner(System.in);

    public void setupElection(String cnic) {
        try (Scanner scanner = new Scanner(System.in);
             Connection con = DriverManager.getConnection(url, dbUsername, dbPassword)) {
            System.out.println("Setting Up Election by Admin");

            // Step 1: Collect election setup details
            System.out.print("Enter Election Name: ");
            String electionName = scanner.nextLine();

            System.out.print("Enter Start Date (YYYY-MM-DD): ");
            String startDate = scanner.nextLine();

            System.out.print("Enter End Date (YYYY-MM-DD): ");
            String endDate = scanner.nextLine();

            // Step 2: Validate dates
            if (!validateDates(startDate, endDate)) {
                System.out.println("Invalid dates provided. Start Date must be before End Date.");
                return;
            }

            // Step 3: Fetch adminId from CNIC
            String adminId = getAdminIdByCnic(con, cnic);

            // Step 4: Insert election setup into the database
            String sql = "INSERT INTO electionsetup (electionName, startDate, endDate, adminId, status) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = con.prepareStatement(sql)) {
                stmt.setString(1, electionName);
                stmt.setDate(2, Date.valueOf(startDate));
                stmt.setDate(3, Date.valueOf(endDate));
                stmt.setString(4, adminId); // Use the adminId retrieved from CNIC
                stmt.setString(5, "PENDING"); // Default status
                stmt.executeUpdate();
                System.out.println("Election setup saved successfully.");
            } catch (SQLException e) {
                System.out.println("Error while setting up election: " + e.getMessage());
            }

            // Step 5: Review and update status if approved
            if (reviewElectionSetup(con, electionName)) {
                updateElectionStatus(con, electionName, "APPROVED", true);
            } else {
                System.out.println("Election setup requires modifications based on feedback.");
                updateElectionStatus(con, electionName, "REJECTED", false);
            }

        } catch (SQLException e) {
            System.out.println("Database connection error. Please try again.");
            e.printStackTrace();
        }
    }

    // Get adminId by CNIC
    private String getAdminIdByCnic(Connection con, String cnic) {
        String adminId = null;
        String sql = "SELECT id FROM admin WHERE cnic = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, cnic);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    adminId = rs.getString("id"); // Fetch the admin id
                }
            }
        } catch (SQLException e) {
            System.out.println("Error fetching admin ID: " + e.getMessage());
        }
        return adminId;
    }

    // Validate start and end dates
    private boolean validateDates(String startDate, String endDate) {
        try {
            Date start = Date.valueOf(startDate);
            Date end = Date.valueOf(endDate);
            return !start.after(end);
        } catch (IllegalArgumentException e) {
            return false; // Invalid date format
        }
    }

    // Review election setup
    private boolean reviewElectionSetup(Connection con, String electionName) {
        try {
            String sql = "SELECT COUNT(*) FROM electionsetup WHERE electionName = ? AND status = 'PENDING'";
            try (PreparedStatement stmt = con.prepareStatement(sql)) {
                stmt.setString(1, electionName);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        System.out.println("Reviewing election setup for: " + electionName);
                        return true; // Approve for this example
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error during election setup review.");
            e.printStackTrace();
        }
        return false; // Default to rejection if errors occur
    }

    // Update election status
    private void updateElectionStatus(Connection con, String electionName, String status, boolean isActive) {
        try {
            String sql = "UPDATE electionsetup SET status = ?, isActive = ? WHERE electionName = ?";
            try (PreparedStatement stmt = con.prepareStatement(sql)) {
                stmt.setString(1, status);
                stmt.setBoolean(2, isActive);
                stmt.setString(3, electionName);
                int rowsUpdated = stmt.executeUpdate();
                if (rowsUpdated > 0) {
                    System.out.println("Election status updated to: " + status);
                } else {
                    System.out.println("Failed to update the election status.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Database error occurred while updating the election status.");
            e.printStackTrace();
        }
    }
}
