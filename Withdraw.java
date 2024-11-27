package UserPackage;
import java.sql.*;

public class Withdraw
{
    private static final String url = "jdbc:mysql://localhost:3306/sdaproject";
    private static final String dbUsername = "root";
    private static final String dbPassword = "Thinkpad@t430";
    public static void withdraw(String cnic) {

        Connection con = null;

        try {
            // Load the MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Establish a connection to the database
            con = DriverManager.getConnection(url, dbUsername, dbPassword);
            if (con != null) {
                System.out.println("Database connected successfully");
            }

            // Check if the candidate with the given CNIC exists
            if (isCandidateExists(con, cnic)) {
                // Prepare the SQL DELETE statement to remove the candidate
                String sql = "DELETE FROM Candidate WHERE cnic = ?";

                // Create a prepared statement for the delete operation
                PreparedStatement preparedStatement = con.prepareStatement(sql);
                preparedStatement.setString(1, cnic);

                // Execute the delete statement
                int rowsDeleted = preparedStatement.executeUpdate();
                if (rowsDeleted > 0) {
                    System.out.println("Candidate with CNIC " + cnic + " has been successfully withdrawn.");
                } else {
                    System.out.println("No candidate found with the given CNIC.");
                }

                // Close the prepared statement
                preparedStatement.close();
            } else {
                System.out.println("No candidate found with the given CNIC.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if (con != null) {
                    con.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // Modify the method to check if CNIC exists in the Candidate table
    private static boolean isCandidateExists(Connection con, String cnic) {
        String checkSql = "SELECT COUNT(*) FROM Candidate WHERE cnic = ?";
        try (PreparedStatement checkStatement = con.prepareStatement(checkSql)) {
            checkStatement.setString(1, cnic);
            ResultSet resultSet = checkStatement.executeQuery();
            resultSet.next();
            return resultSet.getInt(1) > 0; // Return true if count > 0 (exists)
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
