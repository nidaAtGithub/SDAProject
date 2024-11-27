package UserPackage;

import java.sql.*;
import java.util.Scanner;

public class ManageVoter {
    private static final String url = "jdbc:mysql://localhost:3306/sdaproject";
    private static final String dbUsername = "root";
    private static final String dbPassword = "Thinkpad@t430";

    public void manageVoter(Admin admin) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Manage Voter Options:");
        System.out.println("1. Add Voter");
        System.out.println("2. Update Voter");
        System.out.println("3. Delete Voter");
        System.out.print("Select an option (1-3): ");
        int choice = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        switch (choice) {
            case 1:
                addVoter(scanner);
                break;
            case 2:
                updateVoter(scanner);
                break;
            case 3:
                deleteVoter(scanner);
                break;
            default:
                System.out.println("Invalid choice. Returning to main menu.");
        }
    }

    private void addVoter(Scanner scanner) {
        System.out.print("Enter Voter Name: ");
        String name = scanner.nextLine();
        System.out.print("Enter Voter Username: ");
        String username = scanner.nextLine();
        System.out.print("Enter Voter Password: ");
        String password = scanner.nextLine();
        System.out.print("Enter Voter CNIC (xxxxx-xxxxxxx-x): ");
        String cnic = scanner.nextLine();
        System.out.print("Enter Voter Age: ");
        String age = scanner.nextLine();
        System.out.print("Enter Voter Phone: ");
        String phone = scanner.nextLine();
        System.out.print("Enter Voter Address: ");
        String address = scanner.nextLine();

        // Validate voter information
        if (!isValidVoterInfo(name, username, password, cnic, age, phone, address)) {
            System.out.println("Invalid voter information provided. Please try again.");
            return;
        }

        // Check for duplicate CNIC
        if (isVoterExists(cnic)) {
            System.out.println("Voter with this CNIC already exists. Please use a unique CNIC.");
            return;
        }

        try (Connection connection = DriverManager.getConnection(url, dbUsername, dbPassword)) {
            String sql = "INSERT INTO voter (name, username, password, cnic, age, phone, address) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, name);
                statement.setString(2, username);
                statement.setString(3, password);
                statement.setString(4, cnic);
                statement.setString(5, age);
                statement.setString(6, phone);
                statement.setString(7, address);
                statement.executeUpdate();
                System.out.println("Voter added successfully.");
            }
        } catch (SQLException e) {
            System.out.println("System Error. Please try again later.");
            e.printStackTrace();
        }
    }

    private void updateVoter(Scanner scanner) {
        System.out.print("Enter Voter CNIC to update: ");
        String cnic = scanner.nextLine();

        if (!isVoterExists(cnic)) {
            System.out.println("No voter found with the provided CNIC.");
            return;
        }

        System.out.print("Enter new Voter Name (leave empty to keep current): ");
        String newName = scanner.nextLine();
        System.out.print("Enter new Voter Username (leave empty to keep current): ");
        String newUsername = scanner.nextLine();
        System.out.print("Enter new Voter Password (leave empty to keep current): ");
        String newPassword = scanner.nextLine();
        System.out.print("Enter new Voter Age (leave empty to keep current): ");
        String newAge = scanner.nextLine();
        System.out.print("Enter new Voter Phone (leave empty to keep current): ");
        String newPhone = scanner.nextLine();
        System.out.print("Enter new Voter Address (leave empty to keep current): ");
        String newAddress = scanner.nextLine();

        try (Connection connection = DriverManager.getConnection(url, dbUsername, dbPassword)) {
            String sql = "UPDATE voter SET name = COALESCE(NULLIF(?, ''), name), " +
                    "username = COALESCE(NULLIF(?, ''), username), " +
                    "password = COALESCE(NULLIF(?, ''), password), " +
                    "age = COALESCE(NULLIF(?, ''), age), " +
                    "phone = COALESCE(NULLIF(?, ''), phone), " +
                    "address = COALESCE(NULLIF(?, ''), address) " +
                    "WHERE cnic = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, newName);
                statement.setString(2, newUsername);
                statement.setString(3, newPassword);
                statement.setString(4, newAge);
                statement.setString(5, newPhone);
                statement.setString(6, newAddress);
                statement.setString(7, cnic);
                int rowsAffected = statement.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Voter updated successfully.");
                } else {
                    System.out.println("Failed to update voter. Please try again.");
                }
            }
        } catch (SQLException e) {
            System.out.println("System Error. Please try again later.");
            e.printStackTrace();
        }
    }

    private void deleteVoter(Scanner scanner) {
        System.out.print("Enter Voter CNIC to delete: ");
        String cnic = scanner.nextLine();

        if (!isVoterExists(cnic)) {
            System.out.println("No voter found with the provided CNIC.");
            return;
        }

        try (Connection connection = DriverManager.getConnection(url, dbUsername, dbPassword)) {
            String sql = "DELETE FROM voter WHERE cnic = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, cnic);
                int rowsAffected = statement.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Voter deleted successfully.");
                } else {
                    System.out.println("Failed to delete voter. Please try again.");
                }
            }
        } catch (SQLException e) {
            System.out.println("System Error. Please try again later.");
            e.printStackTrace();
        }
    }

    private boolean isValidVoterInfo(String name, String username, String password, String cnic, String age, String phone, String address) {
        return !name.isEmpty() && !username.isEmpty() && !password.isEmpty() &&
                cnic.matches("^\\d{5}-\\d{7}-\\d{1}$") && !age.isEmpty() &&
                !phone.isEmpty() && !address.isEmpty();
    }

    private boolean isVoterExists(String cnic) {
        try (Connection connection = DriverManager.getConnection(url, dbUsername, dbPassword)) {
            String sql = "SELECT COUNT(*) FROM voter WHERE cnic = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, cnic);
                try (ResultSet resultSet = statement.executeQuery()) {
                    resultSet.next();
                    return resultSet.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.out.println("System Error. Please try again later.");
            e.printStackTrace();
            return false;
        }
    }
}
