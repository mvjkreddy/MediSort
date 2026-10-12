
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseManager {

    private static final String URL =
            System.getenv().getOrDefault(
                    "MEDISORT_DB_URL",
                    "jdbc:mysql://localhost:3306/medisort_db"
            );

    private static final String USER =
            System.getenv().getOrDefault(
                    "MEDISORT_DB_USER",
                    "root"
            );

    private static final String PASSWORD =
            System.getenv("MEDISORT_DB_PASSWORD");

    /**
     * Opens a database connection.
     * Credentials are supplied through environment variables.
     */
    public static Connection getConnection() throws SQLException {
        if (PASSWORD == null || PASSWORD.isBlank()) {
            throw new SQLException(
                    "Database password is not configured. "
                    + "Set the MEDISORT_DB_PASSWORD environment variable."
            );
        }

        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /**
     * Saves a validated patient record.
     */
    public void savePatient(
            Patient patient,
            String urgency,
            String department
    ) throws SQLException {

        if (patient == null) {
            throw new IllegalArgumentException(
                    "Patient details cannot be null."
            );
        }

        if (urgency == null || urgency.isBlank()) {
            throw new IllegalArgumentException(
                    "Demo urgency cannot be empty."
            );
        }

        if (department == null || department.isBlank()) {
            throw new IllegalArgumentException(
                    "Recommended department cannot be empty."
            );
        }

        String sql =
                "INSERT INTO patients "
                + "(patient_id, name, age, symptoms, "
                + "demo_urgency, recommended_department) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection connection = getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, patient.getPatientId());
            statement.setString(2, patient.getName());
            statement.setInt(3, patient.getAge());
            statement.setString(4, patient.getSymptoms());
            statement.setString(5, urgency.trim());
            statement.setString(6, department.trim());

            statement.executeUpdate();

            System.out.println("Patient record saved to MySQL.");

        } catch (SQLException ex) {
            if ("23000".equals(ex.getSQLState())) {
                throw new SQLException(
                        "Could not save patient. The patient ID may "
                        + "already exist or a database constraint "
                        + "was violated.",
                        ex.getSQLState(),
                        ex.getErrorCode(),
                        ex
                );
            }

            throw ex;
        }
    }

    /**
     * Displays all patient records in the console.
     */
    public void viewAllPatients() throws SQLException {

        String sql =
                "SELECT patient_id, name, age, symptoms, "
                + "demo_urgency, recommended_department, created_at "
                + "FROM patients ORDER BY created_at DESC";

        try (Connection connection = getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            System.out.println("\n========== SAVED PATIENTS ==========");

            boolean found = false;

            while (result.next()) {
                found = true;
                printPatient(result);
            }

            if (!found) {
                System.out.println("No patient records found.");
            }

            System.out.println("====================================");
        }
    }

    /**
     * Searches for a patient by ID in the console.
     */
    public void searchPatientById(int patientId) throws SQLException {

        if (patientId <= 0) {
            throw new IllegalArgumentException(
                    "Patient ID must be a positive integer."
            );
        }

        String sql =
                "SELECT patient_id, name, age, symptoms, "
                + "demo_urgency, recommended_department, created_at "
                + "FROM patients WHERE patient_id = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, patientId);

            try (ResultSet result = statement.executeQuery()) {

                if (result.next()) {
                    System.out.println("\n========== PATIENT FOUND ==========");
                    printPatient(result);
                    System.out.println("===================================");
                } else {
                    System.out.println(
                            "No patient found with ID: " + patientId
                    );
                }
            }
        }
    }

    /**
     * Prints one patient record.
     */
    private void printPatient(ResultSet result) throws SQLException {

        System.out.println("------------------------------");
        System.out.println(
                "Patient ID   : " + result.getInt("patient_id")
        );
        System.out.println(
                "Name         : " + result.getString("name")
        );
        System.out.println(
                "Age          : " + result.getInt("age")
        );
        System.out.println(
                "Symptoms     : " + result.getString("symptoms")
        );
        System.out.println(
                "Demo Urgency : " + result.getString("demo_urgency")
        );
        System.out.println(
                "Department   : "
                + result.getString("recommended_department")
        );
        System.out.println(
                "Created At   : " + result.getTimestamp("created_at")
        );
    }
}
