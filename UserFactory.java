package UserPackage;

import java.sql.SQLException;
import java.util.Scanner;

public class UserFactory
{
    private static final Scanner sc = new Scanner(System.in);
    public static User createUser(String userType,int ch) throws SQLException {
            if (userType.equalsIgnoreCase("Admin") && ch == 1)
            {
                // For Admin, gather relevant input
                System.out.print("Enter Admin Name: ");
                String name = sc.nextLine();

                System.out.print("Enter Admin Username: ");
                String username = sc.nextLine();

                System.out.print("Enter Admin Password: ");
                String password = sc.nextLine();

                String cnic = "";
                boolean isValidCnic = false;

                // Loop until a valid CNIC is entered
                while (!isValidCnic)
                {
                    System.out.print("Enter Admin CNIC (xxxxx-xxxxxxx-x): ");
                    cnic = sc.nextLine();

                    // Validate CNIC format using regex
                    if (cnic.matches("^\\d{5}-\\d{7}-\\d{1}$")) {
                        isValidCnic = true;
                    } else {
                        System.out.println("Invalid CNIC format. Please enter in the format xxxxx-xxxxxxx-x.");
                    }
                }

                System.out.print("Enter Admin phone: ");
                String phone = sc.nextLine();
                System.out.print("Enter Admin Address: ");
                String address = sc.nextLine();
                // Create and return an Admin instance
                return new Admin(name, username, password, cnic,phone,address);
            }
            else if(userType.equalsIgnoreCase("Admin") && ch == 2)
            {
                System.out.print("Enter Admin Username: ");
                String username = sc.nextLine();

                System.out.print("Enter Admin Password: ");
                String password = sc.nextLine();

                return new Admin(username ,password);
            }

            if(userType.equalsIgnoreCase("Candidate") && ch == 1)
            {
                System.out.print("Enter the Candidate age: ");
                int age = sc.nextInt();

                while(age < 22)
                {
                    System.out.println("Age is not valid");
                    Main.main(null); // Call Main menu
                }
                sc.nextLine();
                System.out.print("Enter candidate Name: ");
                String name = sc.nextLine();

                System.out.print("Enter Candidate Username: ");
                String username = sc.nextLine();

                System.out.print("Enter Candidate Password: ");
                String password = sc.nextLine();

                String cnic = "";
                boolean isValidCnic = false;

                // Loop until a valid CNIC is entered
                while (!isValidCnic)
                {
                    System.out.print("Enter Candidate CNIC (xxxxx-xxxxxxx-x): ");
                    cnic = sc.nextLine();

                    // Validate CNIC format using regex
                    if (cnic.matches("^\\d{5}-\\d{7}-\\d{1}$")) {
                        isValidCnic = true;
                    } else {
                        System.out.println("Invalid CNIC format. Please enter in the format xxxxx-xxxxxxx-x.");
                    }
                }

                System.out.print("Enter Candidate phone: ");
                String phone = sc.nextLine();
                System.out.print("Enter Candidate Address: ");
                String address = sc.nextLine();
                // Create and return an Admin instance
                return new Candidate(name, username, password, cnic,age,phone,address);
            }
            else if(userType.equalsIgnoreCase("Candidate") && ch == 2)
            {
                System.out.print("Enter Candidate Username: ");
                String username = sc.nextLine();

                System.out.print("Enter Candidate Password: ");
                String password = sc.nextLine();

                return new Candidate(password ,username);
            }
            if(userType.equalsIgnoreCase("Voter") && ch == 1)
            {
                System.out.println("Enter the Voter age");
                int age = sc.nextInt();
                while(age < 18)
                {
                    System.out.println("Age is not valid");
                    System.exit(1);
                }
                sc.nextLine();
                System.out.print("Enter Voter Name: ");
                String name = sc.nextLine();

                System.out.print("Enter Voter Username: ");
                String username = sc.nextLine();

                System.out.print("Enter Voter Password: ");
                String password = sc.nextLine();

                String cnic = "";
                boolean isValidCnic = false;

                // Loop until a valid CNIC is entered
                while (!isValidCnic)
                {
                    System.out.print("Enter Voter CNIC (xxxxx-xxxxxxx-x): ");
                    cnic = sc.nextLine();

                    // Validate CNIC format using regex
                    if (cnic.matches("^\\d{5}-\\d{7}-\\d{1}$"))
                    {
                        isValidCnic = true;
                    } else {
                        System.out.println("Invalid CNIC format. Please enter in the format xxxxx-xxxxxxx-x.");
                    }
                }

                System.out.print("Enter Voter phone: ");
                String phone = sc.nextLine();
                System.out.print("Enter Voter Address: ");
                String address = sc.nextLine();
                // Create and return an Admin instance
                return new Voter(name, username, password, cnic ,age,phone,address);
            }
            else if(userType.equalsIgnoreCase("Voter") && ch == 2)
            {
                System.out.print("Enter Voter Username: ");
                String username = sc.nextLine();

                System.out.print("Enter Voter Password: ");
                String password = sc.nextLine();

                return new Voter(password ,username);
            }
            else
            {
                System.out.println("Invalid user type.");
                return null;
            }
    }
}
