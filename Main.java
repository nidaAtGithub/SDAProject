package UserPackage;

import java.sql.SQLException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws SQLException {
        Scanner sc = new Scanner(System.in);
        String userType = "";
        int ch = 0;

        // Ask user for the type of user to create
        System.out.print("Enter User Type (Admin, Candidate, Voter): ");
        userType = sc.nextLine(); // Using nextLine here to read the string

        User user = null; // Initialize user variable

        // Ask for registration or login
        System.out.println("1. Register");
        System.out.println("2. Login");
        System.out.print("Select an option (1-2): ");
        ch = sc.nextInt();
        sc.nextLine(); // Consume newline character left by nextInt()

        // Use the factory to create the user
        user = UserPackage.UserFactory.createUser(userType, ch); // Attempt to create user based on input

        while (ch != 1 && ch != 2) {
            System.out.println("Not valid, enter a valid option:");
            System.out.println("1. Register");
            System.out.println("2. Login");
            ch = sc.nextInt();
            sc.nextLine(); // Consume the newline character after nextInt()
            if (ch == 1) {
                user.register();
                break;
            } else if (ch == 2) {
                user.login();
                break;
            } else {
                System.out.println("Invalid choice. Please try again.");
            }
        }

        while (user == null) {
            System.out.print("Invalid user type. Please enter a valid user type: ");
            userType = sc.nextLine(); // Re-prompt for a valid user type
            user = UserFactory.createUser(userType, ch); // Attempt to create user again
        }

        // Process the registration/login
        if (ch == 1) {
            user.register();
        } else if (ch == 2) {
            user.login();
        }

        boolean exit = false;
        while (!exit) {
            System.out.println("\n--- Menu ---");
            System.out.println("1. Update Personal Information");
            System.out.println("2. Withdraw");
            System.out.println("3. Cast Vote");
            System.out.println("4. View Result");

            if (userType.equalsIgnoreCase("admin")) {
                System.out.println("5. Manage Voter");
                System.out.println("6. Manage Candidate");
                System.out.println("7. Tally Votes");
                System.out.println("8. Generate Report");
                System.out.println("9. Create Ballot");
            }

            System.out.println("10. Logout");
            System.out.print("Enter your choice: ");
            int menuChoice = sc.nextInt();
            sc.nextLine(); // Consume newline character after nextInt()

            switch (menuChoice) {
                case 1:
                    // Updating personal information
                    UpdateInfo.updatePersonalInfo(user, sc, userType);
                    break;

                case 2:
                    if (userType.equalsIgnoreCase("candidate")) {
                        Candidate.requestWithdrawal(user.getCnic());
                    } else {
                        System.out.println("Withdrawal option is only available for candidates.");
                    }
                    break;

                case 3:
                    if (userType.equalsIgnoreCase("voter")) {
                        if (CastVote.hasVoted(user.getCnic())) {
                            System.out.println("You have already cast your vote. You can vote only once.");
                        } else {
                            new CastVote().castVote();
                        }
                    } else {
                        System.out.println("Voting is only available for voters.");
                    }
                    break;

                case 4:
                    new ViewResults().displayResults();
                    break;

                case 5:
                    if (userType.equalsIgnoreCase("admin")) {
                        new ManageVoter().manageVoter((Admin) user);
                    } else {
                        System.out.println("Voter management is only available for admins.");
                    }
                    break;

                case 6:
                    if (userType.equalsIgnoreCase("admin")) {
                        ManageCandidate.manageCandidate((Admin) user);
                    } else {
                        System.out.println("Candidate management is only available for admins.");
                    }
                    break;

                case 7:
                    if (userType.equalsIgnoreCase("admin")) {
                        new TallyVotes().tallyVotes((Admin) user);
                    } else {
                        System.out.println("Tallying votes is only available for admins.");
                    }
                    break;

                case 8:
                    if (userType.equalsIgnoreCase("admin")) {
                        new GenerateReport().generateReport((Admin) user);
                    } else {
                        System.out.println("Report generation is only available for admins.");
                    }
                    break;

                case 9:
                    if (userType.equalsIgnoreCase("admin")) {
                        new CreateBallot().createBallot((Admin) user);
                    } else {
                        System.out.println("Creating a ballot is only available for admins.");
                    }
                    break;

                case 10:
                    System.out.println("Logging out...");
                    exit = true; // Exit the loop
                    break;

                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
        sc.close(); // Close the scanner to prevent resource leak
    }
}
