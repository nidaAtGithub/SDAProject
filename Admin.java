package UserPackage;

import java.util.*;
import java.sql.*;
import java.sql.Connection;

public class Admin extends UserPackage.User {
    private String url = "jdbc:mysql://localhost:3306/sdaproject";
    private String dbUsername = "root";
    private String dbPassword = "Thinkpad@t430";
    private Boolean approve;
    public Admin(String name, String username, String password, String cnic,String phone, String address) {
        super(name, username, password, cnic,phone, address);
    }
    public Admin(String password, String username)   //admin login
    {
        super(username,password);
    }
    @Override
    public void register() {
        System.out.println("Registering a new Admin...");

        String cnic;
        try (Scanner sc = new Scanner(System.in)) {
            // Use the already set name, username, password, and CNIC
            String name = getName();
            String username = getUsername();
            String password = getPassword();
            cnic = getCnic();
            String phone = getPhone();
            String address = getAddress();
            Connection con = null;

            try {
                // Load the MySQL JDBC driver
                Class.forName("com.mysql.cj.jdbc.Driver");

                // Establish a connection to the database
                con = DriverManager.getConnection(url, dbUsername, dbPassword);
                if (con != null) {
                    System.out.println("Database connected successfully");
                }
                while (true) {

                    if (!isValueUnique(con, "cnic", cnic)) {
                        boolean isValidCnic = false;

                        // Validate CNIC format using regex
                        while (!isValidCnic) {
                            System.out.println("This CNIC is already registered. Please enter a unique CNIC.");
                            cnic = sc.nextLine();

                            // Validate CNIC format using regex
                            if (cnic.matches("^\\d{5}-\\d{7}-\\d{1}$")) {
                                isValidCnic = true;
                            } else {
                                System.out.println("Invalid CNIC format. Please enter in the format xxxxx-xxxxxxx-x.");
                            }
                        }
                    } else {
                        break;
                    }
                }
                while (true) {
                    if (!isValueUnique(con, "username", username)) {
                        System.out.println("This username is already taken. Please enter a unique username.");
                        username = sc.nextLine();
                    } else {
                        break;
                    }
                }

                // Get unique password

                while (true) {
                    if (!isValueUnique(con, "password", password)) {
                        System.out.println("This password is already in use. Please enter a unique password.");
                        password = sc.nextLine();
                    } else {
                        break;
                    }
                }

                // SQL query to insert admin details into the database
                String sql = "INSERT INTO admin (name, username, cnic, password,phone, address) VALUES (?, ?, ?, ?, ?, ?)";

                // Create a prepared statement
                PreparedStatement preparedStatement = con.prepareStatement(sql);
                preparedStatement.setString(1, name);       // Set Name
                preparedStatement.setString(2, username);  // Set Username
                preparedStatement.setString(3, cnic);  // Set Password
                preparedStatement.setString(4, password);      // Set CNIC
                preparedStatement.setString(5, phone);      // Set CNIC
                preparedStatement.setString(6, address);      // Set CNIC

                // Execute the insert statement
                int rowsInserted = preparedStatement.executeUpdate();
                if (rowsInserted > 0) {
                    System.out.println("Admin registered successfully!");
                }

                // Close the prepared statement
                preparedStatement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                // Close the connection in the finally block to ensure it's closed
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

    @Override
    public void login() throws SQLException {
        try (Scanner sc = new Scanner(System.in)) {
            boolean isLoggedIn = false;

            while (!isLoggedIn) {
                System.out.print("Enter Username: ");
                String username = sc.nextLine();

                System.out.print("Enter Password: ");
                String password = sc.nextLine();

                try (Connection con = DriverManager.getConnection(url, dbUsername, dbPassword)) {
                    System.out.println("Database connected successfully");

                    // Debug: Check connection and username
                    System.out.println("Attempting to fetch admin with username: " + username);

                    // SQL query to select the admin with the provided username
                    String sql = "SELECT * FROM admin WHERE username = ?";
                    try (PreparedStatement preparedStatement = con.prepareStatement(sql)) {
                        preparedStatement.setString(1, username);

                        try (ResultSet resultSet = preparedStatement.executeQuery()) {
                            if (resultSet.next()) {
                                String storedPassword = resultSet.getString("password");
                                if (storedPassword.equals(password)) {
                                    System.out.println("Login successful!");
                                    // Proceed with admin functionality
                                } else {
                                    System.out.println("Invalid password. Please try again.");
                                }
                            } else {
                                System.out.println("Admin with the given username does not exist.");
                            }
                        }
                    }

                } catch (SQLException e) {
                    System.out.println("SQL error occurred during fetching info.");
                    e.printStackTrace();
                }

                // Retry prompt if login fails
                if (!isLoggedIn) {
                    System.out.print("Do you want to try again? (yes/no): ");
                    String retry = sc.nextLine();
                    if (!retry.equalsIgnoreCase("yes")) {
                        System.out.println("Exiting login...");
                        isLoggedIn = true;
                        break;
                    }
                }
            }

            // After successful login
            if (isLoggedIn) {
                UserPackage.ElectionSetup electionSetup = new UserPackage.ElectionSetup();
                electionSetup.setupElection(this.getCnic());

                // Navigate to Main menu for further actions
                System.out.println("\nRedirecting to the Main menu...");
                Main.main(null); // Call Main menu
            }
        }
    }


    private boolean isValueUnique(Connection con, String column, String value) {
        // Use the `admin` table to check for uniqueness of admin details
        String checkSql = "SELECT COUNT(*) FROM admin WHERE " + column + " = ?";
        try (PreparedStatement checkStatement = con.prepareStatement(checkSql)) {
            checkStatement.setString(1, value);
            ResultSet resultSet = checkStatement.executeQuery();
            resultSet.next();
            return resultSet.getInt(1) == 0; // Return true if count is 0 (unique)
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    public static void manageWithdraw()
    {
        String url = "jdbc:mysql://localhost:3306/sdaproject";
        String dbUsername = "root";
        String dbPassword = "dunkin123";
        Connection con = null;
        try (Scanner scanner = new Scanner(System.in)) {
            try {
                // Load the MySQL JDBC driver
                Class.forName("com.mysql.cj.jdbc.Driver");

                // Establish a connection to the database
                con = DriverManager.getConnection(url, dbUsername, dbPassword);
                if (con != null) {

                    System.out.println("Database connected successfully");
                }

                // Retrieve pending withdrawal requests
                String sql = "SELECT id, cnic FROM withdrawrequests WHERE status = 'Pending'";
                PreparedStatement preparedStatement = con.prepareStatement(sql);
                ResultSet rs = preparedStatement.executeQuery();

                // Display pending requests
                while (rs.next()) {
                    System.out.println("HI*********");
                    int requestId = rs.getInt("id");
                    String cnic = rs.getString("cnic");

                    System.out.println("Pending request for CNIC: " + cnic);
                    System.out.println("Do you approve this withdrawal? (yes/no)");

                    String adminResponse = scanner.nextLine();

                    if (adminResponse.equalsIgnoreCase("yes")) {
                        // Update status to 'Approved'
                        String updateSQL = "UPDATE withdrawrequests SET status = 'Approved' WHERE id = ?";
                        PreparedStatement updateStatement = con.prepareStatement(updateSQL);
                        updateStatement.setInt(1, requestId);
                        int rowsUpdated = updateStatement.executeUpdate();
                        if (rowsUpdated > 0) {
                            System.out.println("Withdrawal request for CNIC " + cnic + " has been approved.");
                            System.out.println("HI*********");
                            // Delete the candidate from the Candidate table
                            String deleteSQL = "DELETE FROM Candidate WHERE cnic = ?";
                            PreparedStatement deleteStatement = con.prepareStatement(deleteSQL);
                            deleteStatement.setString(1, cnic);
                            int rowsDeleted = deleteStatement.executeUpdate();
                            if (rowsDeleted > 0) {
                                System.out.println("Candidate with CNIC " + cnic + " has been deleted.");
                            } else {
                                System.out.println("Failed to delete the candidate with CNIC " + cnic + ".");
                            }
                            deleteStatement.close();
                        } else {
                            System.out.println("Failed to approve withdrawal request.");
                        }
                        updateStatement.close();
                    } else {
                        // Update status to 'Rejected'
                        String updateSQL = "UPDATE withdrawrequests SET status = 'Rejected' WHERE id = ?";
                        PreparedStatement updateStatement = con.prepareStatement(updateSQL);
                        updateStatement.setInt(1, requestId);
                        int rowsUpdated = updateStatement.executeUpdate();
                        if (rowsUpdated > 0) {
                            System.out.println("Withdrawal request for CNIC " + cnic + " has been rejected.");
                        } else {
                            System.out.println("Failed to reject withdrawal request.");
                        }
                        updateStatement.close();
                    }
                }

                rs.close();
                preparedStatement.close();
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
    public Boolean getApprove() {
        return approve;
    }
    public void setApprove(Boolean approve) {
        this.approve = approve;
    }
    public boolean verifyCredentials() {
        try (Scanner sc = new Scanner(System.in)) {
            Connection con = null;

            try {
                System.out.print("Enter Username: ");
                String username = sc.nextLine();

                System.out.print("Enter Password: ");
                String password = sc.nextLine();

                // Load the MySQL JDBC driver
                Class.forName("com.mysql.cj.jdbc.Driver");

                // Establish a connection to the database
                con = DriverManager.getConnection(url, dbUsername, dbPassword);
                if (con != null) {
                    System.out.println("Database connected successfully");
                }

                // SQL query to select the admin with the provided username
                String sql = "SELECT * FROM admin WHERE username = ?";

                // Create a prepared statement
                PreparedStatement preparedStatement = con.prepareStatement(sql);
                preparedStatement.setString(1, username);

                // Execute the query and get the result
                ResultSet rs = preparedStatement.executeQuery();

                // Check if the admin exists
                if (rs.next()) {
                    String storedPassword = rs.getString("password");  // Get the stored password

                    // Compare the entered password with the stored password
                    if (password.equals(storedPassword)) {
                        System.out.println("Login successful!");
                        return true; // Return true if login is successful
                    } else {
                        System.out.println("Invalid password. Please try again.");
                        return false; // Return false if password is incorrect
                    }
                } else {
                    System.out.println("Admin with the given username does not exist. Please try again.");
                    return false; // Return false if username does not exist
                }

            } catch (SQLException e) {
                e.printStackTrace();
                return false; // Return false in case of SQL exception
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                return false; // Return false if JDBC driver not found
            } finally {
                // Close the connection in the finally block to ensure it's closed
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
}
