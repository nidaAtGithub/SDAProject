package UserPackage;

import java.sql.*;
import java.util.Scanner;

public class TallyVotes {
    private static final String url = "jdbc:mysql://localhost:3306/sdaproject";
    private static final String dbUsername = "root";
    private static final String dbPassword = "Thinkpad@t430";

    public void tallyVotes(Admin admin) throws SQLException {
        try (Scanner scanner = new Scanner(System.in)) {
            // Verify administrator credentials
            if (!admin.verifyCredentials()) {
                System.out.println("Invalid login credentials. Please try again.");
                return;
            }

            // Step 1: Get available elections for tallying
            System.out.println("Available Elections for Tallying:");
            String[] elections = getAvailableElections();
            if (elections.length == 0) {
                System.out.println("No elections available for tallying.");
                return;
            }

            for (int i = 0; i < elections.length; i++) {
                System.out.println((i + 1) + ". " + elections[i]);
            }

            System.out.print("Select an election to tally (1-" + elections.length + "): ");
            int electionChoice = scanner.nextInt() - 1;
            scanner.nextLine(); // Consume newline

            if (electionChoice < 0 || electionChoice >= elections.length) {
                System.out.println("Invalid selection. Returning to main menu.");
                return;
            }

            String selectedElection = elections[electionChoice];

            // Step 2: Check if the election is still in progress
            try {
                if (isElectionInProgress(selectedElection)) {
                    System.out.println("Voting is still in progress. Tallying can only be performed after the election period ends.");
                    return;
                }
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            // Step 3: Confirm initiation of tallying
            System.out.print("Confirm initiation of tallying for '" + selectedElection + "'? (yes/no): ");
            String confirm = scanner.nextLine();
            if (!confirm.equalsIgnoreCase("yes")) {
                System.out.println("Tallying has been canceled. No data has been saved.");
                return;
            }

            // Step 4: Tally votes for the election
            try {
                int totalVotes = tallyElectionVotes(selectedElection);
                if (totalVotes < 0) {
                    System.out.println("Insufficient data for tallying. Please verify voter and candidate records.");
                    return;
                }

                // Generate and display the tally report
                generateTallyReport(selectedElection, totalVotes);

                // Step 5: Confirm and finalize results
                System.out.print("Confirm and finalize the results? (yes/no): ");
                confirm = scanner.nextLine();
                if (confirm.equalsIgnoreCase("yes")) {
                    markElectionAsClosed(selectedElection);
                    System.out.println("Election '" + selectedElection + "' has been marked as 'Closed'.");
                } else {
                    System.out.println("Tallying has been canceled. No data has been saved.");
                }
            } catch (SQLException e) {
                System.out.println("System Error: Tallying failed. Please retry later.");
                e.printStackTrace(); // Log the error for debugging
            }
        }
    }

    private String[] getAvailableElections() throws SQLException {
        String sql = "SELECT electionName FROM electionsetup WHERE isActive = TRUE AND status = 'COMPLETED'";
        try (Connection conn = DriverManager.getConnection(url, dbUsername, dbPassword);
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            rs.last();
            int rowCount = rs.getRow();
            rs.beforeFirst();

            String[] elections = new String[rowCount];
            int index = 0;
            while (rs.next()) {
                elections[index++] = rs.getString("electionName");
            }
            return elections;
        }
    }

    private boolean isElectionInProgress(String election) throws SQLException {
        String sql = "SELECT COUNT(*) FROM electionsetup WHERE electionName = ? AND isActive = TRUE";
        try (Connection conn = DriverManager.getConnection(url, dbUsername, dbPassword);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, election);
            try (ResultSet rs = pstmt.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0; // Returns true if the election is active
            }
        }
    }

    private int tallyElectionVotes(String election) throws SQLException {
        String sql = "SELECT COUNT(*) AS totalVotes FROM castvote "
                + "JOIN electionsetup ON castvote.electionId = electionsetup.id "
                + "WHERE electionsetup.electionName = ?";
        try (Connection conn = DriverManager.getConnection(url, dbUsername, dbPassword);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, election);
            try (ResultSet rs = pstmt.executeQuery()) {
                rs.next();
                return rs.getInt("totalVotes");
            }
        }
    }

    private void generateTallyReport(String election, int totalVotes) throws SQLException {
        String sql = "SELECT candidate.name AS candidateName, COUNT(*) AS voteCount "
                + "FROM castvote "
                + "JOIN candidate ON castvote.candidateId = candidate.cnic "
                + "JOIN electionsetup ON castvote.electionId = electionsetup.id "
                + "WHERE electionsetup.electionName = ? "
                + "GROUP BY candidate.name";
        try (Connection conn = DriverManager.getConnection(url, dbUsername, dbPassword);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, election);
            try (ResultSet rs = pstmt.executeQuery()) {
                System.out.println("Tally Report for " + election + ":");
                while (rs.next()) {
                    System.out.println("- " + rs.getString("candidateName") + ": " + rs.getInt("voteCount") + " votes");
                }
                System.out.println("Total Votes Counted: " + totalVotes);
            }
        }
    }

    private void markElectionAsClosed(String election) throws SQLException {
        String sql = "UPDATE electionsetup SET isActive = FALSE, status = 'CLOSED' WHERE electionName = ?";
        try (Connection conn = DriverManager.getConnection(url, dbUsername, dbPassword);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, election);
            pstmt.executeUpdate();
        }
    }
}
