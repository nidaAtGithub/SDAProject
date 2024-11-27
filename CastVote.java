package UserPackage;

import java.sql.*;
import java.util.Scanner;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class CastVote {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/sdaproject";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Thinkpad@t430";

    public void displayBallotPaper() {

        try (Connection con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String candidateQuery = "SELECT cnic, name FROM Candidate";
            PreparedStatement stmt = con.prepareStatement(candidateQuery);
            ResultSet rs = stmt.executeQuery();

            System.out.println("Ballot Paper:");
            while (rs.next()) {
                String cnic = rs.getString("cnic");
                String name = rs.getString("name");
                System.out.println("CNIC: " + cnic + ", Name: " + name);
            }

            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void castVote() {
        try (Scanner scanner = new Scanner(System.in)) {
            // Display the ballot paper
            displayBallotPaper();
            LocalDate currentDate = LocalDate.now();

            // Define the custom format (yy-MM-dd)
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy-MM-dd");

            // Format the date
            String date = currentDate.format(formatter);

            // Prompt the user for their voter ID (CNIC)
            System.out.println("Please enter your CNIC (Voter ID): ");
            String voterId = scanner.nextLine();

            // Check if the voter exists
            if (!isVoterExist(voterId)) {
                System.out.println("Voter with CNIC " + voterId + " does not exist.");
                return;
            }

            // Check if the voter has already voted
            if (hasVoted(voterId)) {
                System.out.println("You have already cast your vote. Voting more than once is not allowed.");
                return;
            }

            // Prompt the user for the candidate's CNIC to cast the vote
            System.out.println("Please enter the CNIC of the candidate you wish to vote for: ");
            String candidateid = scanner.nextLine();

            try (Connection con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                // Fetch the candidate's name using a JOIN query
                String candidateNameQuery =
                        "SELECT name FROM Candidate WHERE cnic = ?";
                PreparedStatement candidateStmt = con.prepareStatement(candidateNameQuery);
                candidateStmt.setString(1, candidateid);
                ResultSet candidateRs = candidateStmt.executeQuery();

                String candidateName = "";
                if (candidateRs.next()) {
                    candidateName = candidateRs.getString("name");
                } else {
                    System.out.println("Candidate with CNIC " + candidateid + " does not exist.");
                    return;
                }

                // Record vote in castVote table
                String voteInsertQuery = "INSERT INTO castVote (voterid, candidateid, date) VALUES (?, ?, ?)";
                PreparedStatement voteStmt = con.prepareStatement(voteInsertQuery);
                voteStmt.setString(1, voterId);          // Set the voter ID (CNIC)
                voteStmt.setString(2, candidateid);      // Set the candidate's CNIC
                voteStmt.setString(3, date);             // Set the vote date

                int rowsInserted = voteStmt.executeUpdate();
                if (rowsInserted > 0) {
                    System.out.println("Your vote has been successfully cast.");
                } else {
                    System.out.println("Failed to cast your vote. Please try again.");
                }

                // Update the voteresult table with candidate's CNIC and name
                String resultUpdateQuery =
                        "INSERT INTO voteresult (ccnic, cname, numvotes) " +
                                "VALUES (?, ?, ?) " +
                                "ON DUPLICATE KEY UPDATE numvotes = numvotes + 1";
                PreparedStatement resultStmt = con.prepareStatement(resultUpdateQuery);
                resultStmt.setString(1, candidateid);       // Set candidate CNIC
                resultStmt.setString(2, candidateName);     // Set candidate name
                resultStmt.setInt(3, 1);                    // Initially set numvotes to 1

                int rowsUpdated = resultStmt.executeUpdate();
                if (rowsUpdated > 0) {
                    System.out.println("Vote result has been successfully updated.");
                } else {
                    System.out.println("Failed to update the vote result table.");
                }

                voteStmt.close();
                resultStmt.close();
                candidateStmt.close();
                candidateRs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    public boolean isVoterExist(String userId)
    {
        try (Connection con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            // Query to check if the userId exists in any of the user tables: voter, admin, or candidate
            String query = "SELECT COUNT(*) FROM (" +
                    "SELECT cnic FROM voter WHERE cnic = ? " +
                    "UNION ALL " +
                    "SELECT cnic FROM admin WHERE cnic = ? " +
                    "UNION ALL " +
                    "SELECT cnic FROM candidate WHERE cnic = ?) AS combined";

            PreparedStatement stmt = con.prepareStatement(query);
            stmt.setString(1, userId);
            stmt.setString(2, userId);
            stmt.setString(3, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;  // If count is greater than 0, the user exists in at least one table
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean hasVoted(String voterid)
    {

        try (Connection con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            // Query to check if the voter has already voted
            String query = "SELECT COUNT(*) FROM castVote WHERE voterid = ?";
            PreparedStatement stmt = con.prepareStatement(query);
            stmt.setString(1, voterid);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0; // If the count is greater than 0, the voter has voted
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}

