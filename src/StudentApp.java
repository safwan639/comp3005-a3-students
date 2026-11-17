import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * COMP 3005 - Assignment 3, Part 1
 * Database Interaction with PostgreSQL and Application Programming
 *
 * A console application that connects to a PostgreSQL database using
 * JDBC Java Database Connectivity and performs CRUD
 * operations on the "students" table:
 *
 *   Create -> addStudent()
 *   Read   -> getAllStudents()
 *   Update -> updateStudentEmail()
 *   Delete -> deleteStudent()
 *
 * A simple text menu lets the user call each function one at a time.
 */
public class StudentApp {

    //------------------------------------------------------------------
    //Database credentials
    //------------------------------------------------------------------
    private static final String URL = "jdbc:postgresql://localhost:5432/students_db";
    private static final String USER = "postgres";
    private static final String PASSWORD = "postgres";

    //One connection is opened when the program starts and is shared
    //by all four CRUD functions. It is closed when the program exits.
    private static Connection conn;

    //Reads the user's menu choices and inputs from the keyboard.
    private static final Scanner scanner = new Scanner(System.in);


    //==================================================================
    //CRUD FUNCTIONS
    //==================================================================

    /**
     *READ: retrieves and displays every record in the students table
     *
     *Uses a Statement with executeQuery() and loops through the
     *ResultSet with rs.next()
     *ORDER BY keeps the rows in student_id order
     */
    public static void getAllStudents() {
        String sql = "SELECT student_id, first_name, last_name, email, enrollment_date "
                   + "FROM students ORDER BY student_id";

        // try-with-resources closes the Statement and ResultSet automatically
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            // Table header
            System.out.printf("%n%-4s %-12s %-12s %-28s %-10s%n",
                    "ID", "First Name", "Last Name", "Email", "Enrolled");
            System.out.println("-".repeat(70));

            int count = 0;
            // Move through the result one row at a time
            while (rs.next()) {
                int id = rs.getInt("student_id");
                String firstName = rs.getString("first_name");
                String lastName = rs.getString("last_name");
                String email = rs.getString("email");
                Date enrolled = rs.getDate("enrollment_date"); // can be NULL

                System.out.printf("%-4d %-12s %-12s %-28s %-10s%n",
                        id, firstName, lastName, email,
                        (enrolled == null) ? "-" : enrolled.toString());
                count++;
            }
            System.out.println(count + " student(s) found.");

        } catch (SQLException e) {
            System.out.println("Error: could not retrieve students. " + e.getMessage());
        }
    }

    /**
     * CREATE: inserts a new student record into the students table.
     *
     * Uses a PreparedStatement with ? placeholders which
     * also protects against SQL injection. student_id is left out of the
     * INSERT because SERIAL generates it automatically.
     *
     * @param firstName      the student's first name (required)
     * @param lastName       the student's last name (required)
     * @param email          the student's email (required, must be unique)
     * @param enrollmentDate the enrollment date as YYYY-MM-DD, or null/blank
     */
    public static void addStudent(String firstName, String lastName,
                                  String email, String enrollmentDate) {
        String sql = "INSERT INTO students (first_name, last_name, email, enrollment_date) "
                   + "VALUES (?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            //fill in the ? placeholders in order (1, 2, 3, 4)
            pstmt.setString(1, firstName);
            pstmt.setString(2, lastName);
            pstmt.setString(3, email);

            //enrollment_date allows NULL, so a blank date is stored as NULL.
            //Otherwise convert the text (YYYY-MM-DD) into a SQL DATE.
            if (enrollmentDate == null || enrollmentDate.isBlank()) {
                pstmt.setNull(4, Types.DATE);
            } else {
                pstmt.setDate(4, Date.valueOf(enrollmentDate));
            }

            //executeUpdate() returns how many rows were inserted
            int rowsInserted = pstmt.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("A new student was inserted successfully!");
            }

        } catch (IllegalArgumentException e) {
            //Thrown by Date.valueOf() when the date is not in YYYY-MM-DD form
            System.out.println("Error: invalid date. Please use the format YYYY-MM-DD.");
        } catch (SQLException e) {
            //e.g. duplicate email (UNIQUE) or missing name/email (NOT NULL)
            System.out.println("Error: could not add student. " + e.getMessage());
        }
    }

    /**
     * UPDATE: changes the email address of the student with the given ID.
     *
     * @param studentId the ID of the student to update
     * @param newEmail  the new email address (must be unique)
     */
    public static void updateStudentEmail(int studentId, String newEmail) {
        String sql = "UPDATE students SET email = ? WHERE student_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newEmail);
            pstmt.setInt(2, studentId);

            // 0 rows updated means no student has this ID
            int rowsUpdated = pstmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Email updated successfully for student ID " + studentId + ".");
            } else {
                System.out.println("No student found with ID " + studentId + ". Nothing was updated.");
            }

        } catch (SQLException e) {
            // e.g. the new email is already used by another student (UNIQUE)
            System.out.println("Error: could not update email. " + e.getMessage());
        }
    }

    /**
     * DELETE: removes the record of the student with the given ID.
     *
     * The WHERE condition makes sure only that one student is deleted
     *
     * @param studentId the ID of the student to delete
     */
    public static void deleteStudent(int studentId) {
        String sql = "DELETE FROM students WHERE student_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, studentId);

            // 0 rows deleted means no student has this ID
            int rowsDeleted = pstmt.executeUpdate();
            if (rowsDeleted > 0) {
                System.out.println("Student ID " + studentId + " was deleted successfully.");
            } else {
                System.out.println("No student found with ID " + studentId + ". Nothing was deleted.");
            }

        } catch (SQLException e) {
            System.out.println("Error: could not delete student. " + e.getMessage());
        }
    }


    // ==================================================================
    // MENU AND INPUT HELPERS
    // ==================================================================

    /** Prints the list of available operations. */
    private static void printMenu() {
        System.out.println();
        System.out.println("========== Student Manager ==========");
        System.out.println("1. Show all students      (getAllStudents)");
        System.out.println("2. Add a student          (addStudent)");
        System.out.println("3. Update a student email (updateStudentEmail)");
        System.out.println("4. Delete a student       (deleteStudent)");
        System.out.println("0. Exit");
        System.out.println("=====================================");
    }

    /**
     * Asks for a line of text. Returns null if the user leaves it blank,
     * so the database's NOT NULL constraint can reject missing values.
     */
    private static String readText(String prompt) {
        System.out.print(prompt);
        String input = scanner.nextLine().trim();
        return input.isEmpty() ? null : input;
    }

    /** Asks for a whole number and keeps asking until one is entered. */
    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a whole number.");
            }
        }
    }

    /** Shows the menu repeatedly and calls the function the user picks. */
    private static void runMenu() {
        while (true) {
            printMenu();
            int choice = readInt("Choose an option: ");

            switch (choice) {
                case 1:
                    getAllStudents();
                    break;

                case 2:
                    String firstName = readText("First name: ");
                    String lastName = readText("Last name: ");
                    String email = readText("Email: ");
                    String date = readText("Enrollment date (YYYY-MM-DD): ");
                    addStudent(firstName, lastName, email, date);
                    break;

                case 3:
                    int idToUpdate = readInt("Student ID to update: ");
                    String newEmail = readText("New email: ");
                    updateStudentEmail(idToUpdate, newEmail);
                    break;

                case 4:
                    int idToDelete = readInt("Student ID to delete: ");
                    deleteStudent(idToDelete);
                    break;

                case 0:
                    System.out.println("Goodbye!");
                    return;

                default:
                    System.out.println("Invalid option. Please choose 0-4.");
            }
        }
    }


    // ==================================================================
    // PROGRAM ENTRY POINT
    // ==================================================================

    /**
     *Loads the PostgreSQL JDBC driver, connects to the databaseh
     *runs the menu, and closes the connection on exit.
     */
    public static void main(String[] args) {
        //Load the PostgreSQL JDBC driver
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("Error: PostgreSQL JDBC driver not found. "
                    + "Put the driver .jar file in the lib folder (see README).");
            return;
        }

        //Connect to the database. try-with-resources closes the
        //connection automatically when the program leaves this block.
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
            conn = connection;
            System.out.println("Connected to PostgreSQL successfully!");
            runMenu();
        } catch (SQLException e) {
            System.out.println("Error: could not connect to the database. " + e.getMessage());
            System.out.println("Check that PostgreSQL is running and that URL, USER and "
                    + "PASSWORD at the top of StudentApp.java are correct.");
        } catch (NoSuchElementException e) {
            //Input stream ended (e.g. the window was closed); exit cleanly
            System.out.println("\nInput closed. Exiting.");
        }
    }
}
