package UserPackage;

import java.sql.*;

public class ViewResults
{

    private String DB_URL = "jdbc:mysql://localhost:3306/sdaproject";
    private String DB_USER = "root";
    private String DB_PASSWORD = "Thinkpad@t430";

    public void displayResults() {
        try (Connection con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            // Query to get the candidate's CNIC, name from the Candidate table and number of votes from the voteresult table
            String query = "SELECT c.cnic, c.name, IFNULL(v.numvotes, 0) AS numvotes " +
                    "FROM Candidate c " +
                    "LEFT JOIN voteresult v ON c.cnic = v.ccnic " +
                    "ORDER BY numvotes DESC";
            PreparedStatement stmt = con.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            // Initialize variables to track the winner
            String winnerCnic = "";
            String winnerName = "";
            int maxVotes = 0;

            // Display the results
            System.out.println("Election Results:");
            while (rs.next()) {
                String cnic = rs.getString("cnic");
                String name = rs.getString("name");
                int numVotes = rs.getInt("numvotes");

                // Track the candidate with the maximum votes
                if (numVotes > maxVotes) {
                    maxVotes = numVotes;
                    winnerCnic = cnic;
                    winnerName = name;
                }

                // Display each candidate's results
                System.out.println("CNIC: " + cnic + ", Name: " + name + ", Votes: " + numVotes);
            }

            // Display the winner
            System.out.println("\nWinner of the election:");
            System.out.println("CNIC: " + winnerCnic + ", Name: " + winnerName + ", Votes: " + maxVotes);

            // Close resources
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
