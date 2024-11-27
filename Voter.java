package UserPackage;

import java.sql.*;
import java.util.*;
public class Voter extends User
{
    private static final String url = "jdbc:mysql://localhost:3306/sdaproject";
    private static final String dbUsername = "root";
    private static final String dbPassword = "Thinkpad@t430";

    public Voter(String name, String username, String password,String cnic, int age,String phone, String address)
    {
        super(name,username, password,cnic,age,phone,address);
    }
    public Voter(String username, String password)
    {
        super(username,password);
    }
    @Override
    public void register()
    {
        System.out.println("Registering a new voter");
        try (Scanner sc = new Scanner(System.in)) {
            // Use the already set name, username, password, and CNIC
            String name = getName();
            String username = getUsername();
            String password = getPassword();
            String cnic = getCnic();
            int age = getAge();

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

                    if (!isValueUnique(con, "cnic", cnic))
                    {
                        boolean isValidCnic = false;

                        // Validate CNIC format using regex
                        while (!isValidCnic)
                        {
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
                while (true)
                {
                    if (!isValueUnique(con, "username", username))
                    {
                        System.out.println("This username is already taken. Please enter a unique username.");
                        username = sc.nextLine();
                    } else {
                        break;
                    }
                }

                // Get unique password

                while (true)
                {
                    if (!isValueUnique(con, "password", password))
                    {
                        System.out.println("This password is already in use. Please enter a unique password.");
                        password= sc.nextLine();
                    }
                    else
                    {
                        break;
                    }
                }

                // SQL query to insert admin details into the database
                String sql = "INSERT INTO voter (name, username, password, cnic, age,phone,address) VALUES (?, ?, ?, ?, ?,?,?)";

                // Create a prepared statement
                PreparedStatement preparedStatement = con.prepareStatement(sql);
                preparedStatement.setString(1, name);       // Set Name
                preparedStatement.setString(2, username);  // Set Username
                preparedStatement.setString(3, password);  // Set Password
                preparedStatement.setString(4, cnic);      // Set CNIC
                preparedStatement.setInt(5, age);
                preparedStatement.setString(6, phone);
                preparedStatement.setString(7, address);
                // Execute the insert statement
                int rowsInserted = preparedStatement.executeUpdate();
                if (rowsInserted > 0)
                {
                    System.out.println("Voter registered successfully!");
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
    public void login() {
        try (Scanner sc = new Scanner(System.in)) {
            Connection con = null;

            while (true) {
                System.out.print("Enter Username: ");
                String username = sc.nextLine();

                System.out.print("Enter Password: ");
                String password = sc.nextLine();

                try {
                    // Load the MySQL JDBC driver
                    Class.forName("com.mysql.cj.jdbc.Driver");

                    // Establish a connection to the database
                    con = DriverManager.getConnection(url, dbUsername, dbPassword);
                    if (con != null) {
                        System.out.println("Database connected successfully");
                    }

                    // SQL query to select the voter with the provided username
                    String sql = "SELECT * FROM Voter WHERE username = ?";

                    // Create a prepared statement
                    PreparedStatement preparedStatement = con.prepareStatement(sql);
                    preparedStatement.setString(1, username);

                    // Execute the query and get the result
                    ResultSet rs = preparedStatement.executeQuery();

                    // Check if the voter exists
                    if (rs.next()) {
                        String storedPassword = rs.getString("password"); // Get the stored password

                        // Compare the entered password with the stored password
                        if (password.equals(storedPassword)) {
                            System.out.println("Login successful!");
                            break; // Exit the loop if login is successful
                        } else {
                            System.out.println("Invalid password! Please try again.");
                        }
                    } else {
                        System.out.println("Voter with the given username does not exist! Please try again.");
                    }

                    // Close the result set and prepared statement
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

                // Prompt to try again or exit
                System.out.print("Do you want to try again? (yes/no): ");
                String retry = sc.nextLine();
                if (!retry.equalsIgnoreCase("yes")) {
                    System.out.println("Exiting login...");
                    break;
                }
            }
        }
    }

    private boolean isValueUnique(Connection con, String column, String value)
    {
        String checkSql = "SELECT COUNT(*) FROM voter WHERE " + column + " = ?";
        try (PreparedStatement checkStatement = con.prepareStatement(checkSql))
        {
            checkStatement.setString(1, value);
            ResultSet resultSet = checkStatement.executeQuery();
            resultSet.next();
            return resultSet.getInt(1) == 0; // Return true if count is 0 (unique)
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return false;
        }
    }
}
