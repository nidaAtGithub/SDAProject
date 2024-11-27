package UserPackage;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CreateBallot {
    private static final String url = "jdbc:mysql://localhost:3306/sdaproject";
    private static final String dbUsername = "root";
    private static final String dbPassword = "Thinkpad@t430";

    public void createBallot(Admin admin) {
        try (Scanner scanner = new Scanner(System.in)) {
            // Step 1: Verify administrator's identity
            if (!admin.verifyCredentials()) {
                System.out.println("Invalid login credentials. Please try again.");
                return;
            }

            // Step 2: Navigate to "Create Ballot"
            System.out.println("Navigating to Create Ballot section...");

            // Step 3: Retrieve available elections and candidates
            List<String> elections = getAvailableElections();
            if (elections.isEmpty()) {
                System.out.println("No elections available for ballot creation.");
                return;
            }

            // Display available elections
            System.out.println("Available Elections:");
            for (int i = 0; i < elections.size(); i++) {
                System.out.println((i + 1) + ". " + elections.get(i));
            }

            System.out.print("Select an election (1-" + elections.size() + "): ");
            int electionChoice = scanner.nextInt() - 1;
            scanner.nextLine(); // Consume newline

            if (electionChoice < 0 || electionChoice >= elections.size()) {
                System.out.println("Invalid selection. Returning to main menu.");
                return;
            }

            String selectedElection = elections.get(electionChoice);

            // Step 4: Display ballot creation form
            List<String> candidates = getCandidatesForElection(selectedElection);
            if (candidates.isEmpty()) {
                System.out.println("No candidates available for this election.");
                return;
            }

            // Display available candidates
            System.out.println("Available Candidates:");
            for (int i = 0; i < candidates.size(); i++) {
                System.out.println((i + 1) + ". " + candidates.get(i));
            }

            // Step 5: Select candidates for the ballot
            List<String> selectedCandidates = new ArrayList<>();
            System.out.print("Select candidates to include in the ballot (comma-separated numbers): ");
            String[] selectedIndexes = scanner.nextLine().split(",");
            for (String index : selectedIndexes) {
                try {
                    int candidateIndex = Integer.parseInt(index.trim()) - 1;
                    if (candidateIndex >= 0 && candidateIndex < candidates.size()) {
                        selectedCandidates.add(candidates.get(candidateIndex));
                    } else {
                        System.out.println("Candidate index " + (candidateIndex + 1) + " is not valid.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input: " + index);
                }
            }

            // Validate selected candidates
            if (selectedCandidates.isEmpty()) {
                System.out.println("No candidates selected. Please select at least one candidate.");
                return;
            }

            // Step 6: Configure ballot settings
            System.out.print("Enter ballot instructions: ");
            String instructions = scanner.nextLine();
            if (instructions.isEmpty()) {
                System.out.println("Instructions are required. Please provide them.");
                return;
            }

            System.out.print("Enter any restrictions (if any): ");
            String restrictions = scanner.nextLine();

            // Step 7: Create and store the ballot
            try {
                storeBallot(selectedElection, selectedCandidates, instructions, restrictions);
                System.out.println("Ballot created successfully!");
            } catch (SQLException e) {
                System.out.println("System Error: Ballot creation failed. Please try again later.");
                e.printStackTrace(); // Log the error for troubleshooting
                return;
            }

            // Step 8: Confirm successful ballot creation
            System.out.println("Ballot for '" + selectedElection + "' has been created with the following candidates:");
            for (String candidate : selectedCandidates) {
                System.out.println("- " + candidate);
            }

            // Step 9: Log out
            System.out.println("Logging out...");
            scheduleBallotAvailability(selectedElection);
        }
    }

    private List<String> getAvailableElections() {
        List<String> elections = new ArrayList<>();
        String sql = "SELECT electionName FROM electionsetup WHERE isActive = TRUE";

        try (Connection conn = DriverManager.getConnection(url, dbUsername, dbPassword);
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                elections.add(rs.getString("electionName"));
            }
        } catch (SQLException e) {
            System.out.println("Error fetching elections: " + e.getMessage());
        }
        return elections;
    }

    private List<String> getCandidatesForElection(String election) {
        List<String> candidates = new ArrayList<>();
        String sql = "SELECT c.name FROM candidate c "
                + "JOIN election_candidate ec ON c.id = ec.candidateId "
                + "JOIN electionsetup e ON ec.electionId = e.id "
                + "WHERE e.electionName = ?";

        try (Connection conn = DriverManager.getConnection(url, dbUsername, dbPassword);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, election);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    candidates.add(rs.getString("name"));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error fetching candidates: " + e.getMessage());
        }
        return candidates;
    }

    private void storeBallot(String election, List<String> candidates, String instructions, String restrictions) throws SQLException {
        String ballotSql = "INSERT INTO ballots (electionId, instructions, restrictions) VALUES (?, ?, ?)";
        String candidateSql = "INSERT INTO ballot_candidates (ballotId, candidateId) VALUES (?, ?)";

        try (Connection conn = DriverManager.getConnection(url, dbUsername, dbPassword);
             PreparedStatement ballotStmt = conn.prepareStatement(ballotSql, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement candidateStmt = conn.prepareStatement(candidateSql)) {

            // Insert ballot
            int electionId = getElectionId(election);
            ballotStmt.setInt(1, electionId);
            ballotStmt.setString(2, instructions);
            ballotStmt.setString(3, restrictions);
            ballotStmt.executeUpdate();

            // Get the generated ballot ID
            ResultSet rs = ballotStmt.getGeneratedKeys();
            if (rs.next()) {
                int ballotId = rs.getInt(1);

                // Insert selected candidates into the ballot
                for (String candidateName : candidates) {
                    int candidateId = getCandidateId(candidateName);
                    candidateStmt.setInt(1, ballotId);
                    candidateStmt.setInt(2, candidateId);
                    candidateStmt.executeUpdate();
                }
            }
        }
    }

    private int getElectionId(String election) throws SQLException {
        String sql = "SELECT id FROM electionsetup WHERE electionName = ?";
        try (Connection conn = DriverManager.getConnection(url, dbUsername, dbPassword);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, election);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            } else {
                throw new SQLException("Election not found: " + election);
            }
        }
    }

    private int getCandidateId(String candidateName) throws SQLException {
        String sql = "SELECT id FROM candidate WHERE name = ?";
        try (Connection conn = DriverManager.getConnection(url, dbUsername, dbPassword);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, candidateName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            } else {
                throw new SQLException("Candidate not found: " + candidateName);
            }
        }
    }

    private void scheduleBallotAvailability(String election) {
        System.out.println("Ballot for '" + election + "' is scheduled for availability.");
    }
}
