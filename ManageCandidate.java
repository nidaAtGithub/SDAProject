package UserPackage;

import java.sql.*;
import java.util.Scanner;

public class ManageCandidate {
    private static final String url = "jdbc:mysql://localhost:3306/sdaproject";
    private static final String dbUsername = "root";
    private static final String dbPassword = "Thinkpad@t430";

    @SuppressWarnings("resource")
    public static void manageCandidate(Admin admin) {
        Scanner scanner = new Scanner(System.in);

        // Verify admin credentials
        if (!verifyAdminCredentials(admin)) {
            System.out.println("Invalid login credentials. Please try again.");
            return;
        }

        // Display the list of existing candidates
        displayCandidates();

        // Prompt the admin to select an action
        System.out.println("Select an action:");
        System.out.println("1. Add Candidate");
        System.out.println("2. Update Candidate");
        System.out.println("3. Remove Candidate");
        System.out.print("Enter your choice (1-3): ");
        int choice = scanner.nextInt();
        scanner.nextLine(); // Consume the newline character

        switch (choice) {
            case 1:
                addCandidate(scanner);
                break;
            case 2:
                updateCandidate(scanner);
                break;
            case 3:
                removeCandidate(scanner);
                break;
            default:
                System.out.println("Invalid choice. Please try again.");
        }

        // Log the admin out
        System.out.println("Logging out...");
    }

    private static boolean verifyAdminCredentials(Admin admin) {
        return admin.verifyCredentials();
    }

    private static void displayCandidates() {
        try (Connection connection = DriverManager.getConnection(url, dbUsername, dbPassword)) {
            String sql = "SELECT id, name, cnic, age, phone, address FROM candidate";
            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(sql)) {
                System.out.println("\n--- List of Candidates ---");
                while (resultSet.next()) {
                    System.out.println(
                            "ID: " + resultSet.getInt("id") +
                                    ", Name: " + resultSet.getString("name") +
                                    ", CNIC: " + resultSet.getString("cnic") +
                                    ", Age: " + resultSet.getInt("age") +
                                    ", Phone: " + resultSet.getString("phone") +
                                    ", Address: " + resultSet.getString("address")
                    );
                }
            }
        } catch (SQLException e) {
            System.out.println("System failure: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void addCandidate(Scanner scanner) {
        System.out.print("Enter Candidate Name: ");
        String name = scanner.nextLine();
        System.out.print("Enter Candidate CNIC (xxxxx-xxxxxxx-x): ");
        String cnic = scanner.nextLine();
        System.out.print("Enter Candidate Age: ");
        int age = scanner.nextInt();
        scanner.nextLine(); // Consume newline
        System.out.print("Enter Candidate Phone: ");
        String phone = scanner.nextLine();
        System.out.print("Enter Candidate Address: ");
        String address = scanner.nextLine();

        // Validate the input
        if (!isValidCandidateInfo(name, cnic, age, phone, address)) {
            System.out.println("Invalid candidate information. Please try again.");
            return;
        }

        // Check if the candidate already exists
        if (isCandidateExists(cnic)) {
            System.out.println("Candidate with the given CNIC already exists. Please try a different CNIC.");
            return;
        }

        try (Connection connection = DriverManager.getConnection(url, dbUsername, dbPassword)) {
            String sql = "INSERT INTO candidate (name, cnic, age, phone, address) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, name);
                statement.setString(2, cnic);
                statement.setInt(3, age);
                statement.setString(4, phone);
                statement.setString(5, address);
                int rowsAffected = statement.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Candidate added successfully.");
                } else {
                    System.out.println("Failed to add the candidate. Please try again.");
                }
            }
        } catch (SQLException e) {
            System.out.println("System failure: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void updateCandidate(Scanner scanner) {
        System.out.print("Enter the CNIC of the candidate to update: ");
        String cnic = scanner.nextLine();

        // Check if the candidate exists
        if (!isCandidateExists(cnic)) {
            System.out.println("No candidate found with the provided CNIC.");
            return;
        }

        // Get new details for the candidate
        System.out.print("Enter new Candidate Name (leave empty to keep current): ");
        String newName = scanner.nextLine();
        System.out.print("Enter new Candidate Age (leave empty to keep current): ");
        String newAgeInput = scanner.nextLine();
        System.out.print("Enter new Candidate Phone (leave empty to keep current): ");
        String newPhone = scanner.nextLine();
        System.out.print("Enter new Candidate Address (leave empty to keep current): ");
        String newAddress = scanner.nextLine();

        try (Connection connection = DriverManager.getConnection(url, dbUsername, dbPassword)) {
            String sql = "UPDATE candidate SET " +
                    "name = COALESCE(NULLIF(?, ''), name), " +
                    "age = COALESCE(NULLIF(?, ''), age), " +
                    "phone = COALESCE(NULLIF(?, ''), phone), " +
                    "address = COALESCE(NULLIF(?, ''), address) " +
                    "WHERE cnic = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, newName);
                statement.setString(2, newAgeInput.isEmpty() ? null : newAgeInput);
                statement.setString(3, newPhone);
                statement.setString(4, newAddress);
                statement.setString(5, cnic);
                int rowsAffected = statement.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Candidate updated successfully.");
                } else {
                    System.out.println("Failed to update the candidate. Please try again.");
                }
            }
        } catch (SQLException e) {
            System.out.println("System failure: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void removeCandidate(Scanner scanner) {
        System.out.print("Enter the CNIC of the candidate to remove: ");
        String cnic = scanner.nextLine();

        // Check if the candidate exists
        if (!isCandidateExists(cnic)) {
            System.out.println("No candidate found with the provided CNIC.");
            return;
        }

        try (Connection connection = DriverManager.getConnection(url, dbUsername, dbPassword)) {
            String sql = "DELETE FROM candidate WHERE cnic = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, cnic);
                int rowsAffected = statement.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Candidate removed successfully.");
                } else {
                    System.out.println("Failed to remove the candidate. Please try again.");
                }
            }
        } catch (SQLException e) {
            System.out.println("System failure: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static boolean isValidCandidateInfo(String name, String cnic, int age, String phone, String address) {
        return !name.isEmpty() && cnic.matches("^\\d{5}-\\d{7}-\\d{1}$") && age > 0 &&
                !phone.isEmpty() && !address.isEmpty();
    }

    private static boolean isCandidateExists(String cnic) {
        try (Connection connection = DriverManager.getConnection(url, dbUsername, dbPassword)) {
            String sql = "SELECT COUNT(*) FROM candidate WHERE cnic = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, cnic);
                try (ResultSet resultSet = statement.executeQuery()) {
                    resultSet.next();
                    return resultSet.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.out.println("System failure: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
