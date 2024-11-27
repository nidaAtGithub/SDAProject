package UserPackage;

import java.util.*;
import java.sql.*;

public class UpdateInfo
{
    private static final String url = "jdbc:mysql://localhost:3306/sdaproject";
    private static final String dbUsername = "root";
    private static final String dbPassword = "Thinkpad@t430";
    public static void updatePersonalInfo(UserPackage.User user, Scanner sc, String userType) {


        Connection con = null;

        try {
            // Load the MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Establish a connection to the database
            con = DriverManager.getConnection(url, dbUsername, dbPassword);
            if (con != null) {
                System.out.println("Database connected successfully");
            }

            System.out.println("1.  Update Address");
            System.out.println("2.  Update Phone");
            System.out.println("3.  Update Password");
            System.out.println("4.  Update Username");
            System.out.println("5.  Update Age");
            System.out.print("Enter your choice: ");
            int ch = sc.nextInt();
            sc.nextLine(); // Consume newline character

            String tableName = ""; // Initialize with an empty table name

            // Determine the table based on the userType
            switch (userType.toLowerCase()) {
                case "voter":
                    tableName = "Voter";
                    break;
                case "candidate":
                    tableName = "Candidate";
                    break;
                case "admin":
                    tableName = "Admin";
                    break;
                default:
                    System.out.println("Invalid user type.");
                    return;
            }

            String sql = null;
            PreparedStatement preparedStatement = null;

            switch (ch) {
                case 1:
                    System.out.print("Enter new address: ");
                    String address = sc.nextLine();
                    sql = "UPDATE " + tableName + " SET address = ? WHERE username = ?";
                    preparedStatement = con.prepareStatement(sql);
                    preparedStatement.setString(1, address);
                    preparedStatement.setString(2, user.getUsername());
                    break;

                case 2:
                    System.out.print("Enter new phone: ");
                    String phone = sc.nextLine();
                    sql = "UPDATE " + tableName + " SET phone = ? WHERE username = ?";
                    preparedStatement = con.prepareStatement(sql);
                    preparedStatement.setString(1, phone);
                    preparedStatement.setString(2, user.getUsername());
                    break;

                case 3:
                    System.out.print("Enter new password: ");
                    String password = sc.nextLine();
                    sql = "UPDATE " + tableName + " SET password = ? WHERE username = ?";
                    preparedStatement = con.prepareStatement(sql);
                    preparedStatement.setString(1, password);
                    preparedStatement.setString(2, user.getUsername());
                    break;

                case 4:
                    System.out.print("Enter new username: ");
                    String username = sc.nextLine();
                    sql = "UPDATE " + tableName + " SET username = ? WHERE username = ?";
                    preparedStatement = con.prepareStatement(sql);
                    preparedStatement.setString(1, username);
                    preparedStatement.setString(2, user.getUsername());
                    user.setUsername(username); // Update username in the user object
                    break;

                case 5:
                    System.out.print("Enter new age: ");
                    int age = sc.nextInt();
                    sql = "UPDATE " + tableName + " SET age = ? WHERE username = ?";
                    preparedStatement = con.prepareStatement(sql);
                    preparedStatement.setInt(1, age);
                    preparedStatement.setString(2, user.getUsername());
                    break;

                default:
                    System.out.println("Not a valid option.");
                    return;
            }

            // Execute the update statement
            int rowsUpdated = preparedStatement.executeUpdate();
            if (rowsUpdated > 0)
            {
                System.out.println("Information updated successfully in " + tableName + " table.");
            } else {
                System.out.println("Failed to update information.");
            }

            // Close the prepared statement
            if (preparedStatement != null) {
                preparedStatement.close();
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
}