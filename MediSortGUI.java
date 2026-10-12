
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MediSortGUI extends JFrame {

    private JTextField idField;
    private JTextField nameField;
    private JTextField ageField;
    private JTextField symptomsField;
    private JTextField searchField;

    private JTable patientTable;
    private DefaultTableModel tableModel;

    private final DatabaseManager database = new DatabaseManager();
    private final TriageEngine engine = new TriageEngine();

    public MediSortGUI() {
        setTitle("MediSort - Healthcare Intelligence System");
        setSize(1000, 650);
        setMinimumSize(new Dimension(800, 500));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout(12, 12));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(20, 45, 78));
        header.setBorder(
                BorderFactory.createEmptyBorder(18, 22, 18, 22)
        );

        JLabel title = new JLabel("MediSort");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("SansSerif", Font.BOLD, 28));

        JLabel subtitle = new JLabel(
                "AI-Assisted Healthcare Triage & Hospital Intelligence"
        );
        subtitle.setForeground(Color.WHITE);
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));

        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 0, 5));
        titlePanel.setOpaque(false);
        titlePanel.add(title);
        titlePanel.add(subtitle);

        header.add(titlePanel, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        // Patient registration form
        JPanel form = new JPanel(new GridLayout(0, 2, 8, 12));
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Register Patient"),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));

        idField = new JTextField();
        nameField = new JTextField();
        ageField = new JTextField();
        symptomsField = new JTextField();

        form.add(new JLabel("Patient ID:"));
        form.add(idField);

        form.add(new JLabel("Full Name:"));
        form.add(nameField);

        form.add(new JLabel("Age:"));
        form.add(ageField);

        form.add(new JLabel("Symptoms:"));
        form.add(symptomsField);

        JButton registerButton = new JButton("Register Patient");
        JButton refreshButton = new JButton("Refresh Records");

        form.add(registerButton);
        form.add(refreshButton);

        // Patient records table
        tableModel = new DefaultTableModel(
                new String[]{
                        "ID", "Name", "Age", "Symptoms",
                        "Demo Urgency", "Department", "Created At"
                }, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        patientTable = new JTable(tableModel);
        patientTable.setRowHeight(25);
        patientTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        patientTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        patientTable.getColumnModel().getColumn(0).setPreferredWidth(70);
        patientTable.getColumnModel().getColumn(1).setPreferredWidth(140);
        patientTable.getColumnModel().getColumn(2).setPreferredWidth(60);
        patientTable.getColumnModel().getColumn(3).setPreferredWidth(200);
        patientTable.getColumnModel().getColumn(4).setPreferredWidth(120);
        patientTable.getColumnModel().getColumn(5).setPreferredWidth(170);
        patientTable.getColumnModel().getColumn(6).setPreferredWidth(160);

        JScrollPane tableScroll = new JScrollPane(patientTable);
        tableScroll.setBorder(
                BorderFactory.createTitledBorder("Patient Records")
        );

        // Search panel
        JPanel searchPanel = new JPanel(
                new FlowLayout(FlowLayout.LEFT)
        );
        searchPanel.setBorder(
                BorderFactory.createTitledBorder("Search Patient")
        );

        searchField = new JTextField(15);
        JButton searchButton = new JButton("Search");
        JButton showAllButton = new JButton("Show All");

        searchPanel.add(new JLabel("Patient ID:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(showAllButton);

        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setBorder(
                BorderFactory.createEmptyBorder(0, 12, 12, 12)
        );

        content.add(form, BorderLayout.NORTH);
        content.add(tableScroll, BorderLayout.CENTER);
        content.add(searchPanel, BorderLayout.SOUTH);

        add(content, BorderLayout.CENTER);

        // Button actions
        registerButton.addActionListener(e -> registerPatient());
        refreshButton.addActionListener(e -> loadPatients(null));
        showAllButton.addActionListener(e -> {
            searchField.setText("");
            loadPatients(null);
        });
        searchButton.addActionListener(e -> searchPatient());

        // Allow Enter to trigger search
        searchField.addActionListener(e -> searchPatient());

        loadPatients(null);
    }

    /**
     * Validates and registers a patient.
     */
    private void registerPatient() {

        String idText = idField.getText().trim();
        String name = nameField.getText().trim();
        String ageText = ageField.getText().trim();
        String symptoms = symptomsField.getText().trim();

        int id;
        int age;

        // Patient ID validation
        try {
            id = Integer.parseInt(idText);

            if (id <= 0) {
                showValidationError(
                        "Patient ID must be a positive integer.",
                        idField
                );
                return;
            }
        } catch (NumberFormatException ex) {
            showValidationError(
                    "Patient ID must be a valid whole number.",
                    idField
            );
            return;
        }

        // Name validation
        if (name.isEmpty()) {
            showValidationError(
                    "Patient name cannot be empty.",
                    nameField
            );
            return;
        }

        if (name.length() > 100) {
            showValidationError(
                    "Patient name cannot exceed 100 characters.",
                    nameField
            );
            return;
        }

        if (!name.matches("[\\p{L}][\\p{L} .'-]*")) {
            showValidationError(
                    "Enter a valid name using letters and normal "
                            + "name punctuation.",
                    nameField
            );
            return;
        }

        // Age validation
        try {
            age = Integer.parseInt(ageText);

            if (age < 0 || age > 130) {
                showValidationError(
                        "Age must be between 0 and 130.",
                        ageField
                );
                return;
            }
        } catch (NumberFormatException ex) {
            showValidationError(
                    "Age must be a valid whole number.",
                    ageField
            );
            return;
        }

        // Symptoms validation
        if (symptoms.isEmpty()) {
            showValidationError(
                    "Please enter the patient's symptoms.",
                    symptomsField
            );
            return;
        }

        if (symptoms.length() > 2000) {
            showValidationError(
                    "Symptoms cannot exceed 2000 characters.",
                    symptomsField
            );
            return;
        }

        try {
            Patient patient = new Patient(
                    id, name, age, symptoms
            );

            String urgency = engine.assess(patient);
            String department = engine.recommendDepartment(patient);

            database.savePatient(patient, urgency, department);

            JOptionPane.showMessageDialog(
                    this,
                    "Patient registered successfully!\n\n"
                            + "Demo urgency: " + urgency + "\n"
                            + "Demo department: " + department + "\n\n"
                            + "These outputs are for educational "
                            + "demonstration only and are not "
                            + "clinical advice.",
                    "Registration Successful",
                    JOptionPane.INFORMATION_MESSAGE
            );

            idField.setText("");
            nameField.setText("");
            ageField.setText("");
            symptomsField.setText("");

            loadPatients(null);

        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Invalid Patient Details",
                    JOptionPane.WARNING_MESSAGE
            );

        } catch (SQLException ex) {

            if ("23000".equals(ex.getSQLState())) {
                JOptionPane.showMessageDialog(
                        this,
                        "This patient ID may already exist, "
                                + "or a database constraint was violated.\n"
                                + "Please verify the details and try again.",
                        "Database Constraint Error",
                        JOptionPane.WARNING_MESSAGE
                );
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Could not save the patient record.\n"
                                + "Check your database connection "
                                + "and configuration.",
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }

            System.err.println(
                    "Patient registration database error: "
                            + ex.getMessage()
            );
        }
    }

    /**
     * Displays an input validation error and focuses the field.
     */
    private void showValidationError(
            String message,
            JTextField field
    ) {
        JOptionPane.showMessageDialog(
                this,
                message,
                "Invalid Input",
                JOptionPane.WARNING_MESSAGE
        );

        field.requestFocusInWindow();
    }

    /**
     * Loads all patient records or a single matching record.
     * Uses the shared DatabaseManager connection configuration.
     */
    private void loadPatients(Integer patientId) {

        String sql =
                "SELECT patient_id, name, age, symptoms, "
                        + "demo_urgency, recommended_department, created_at "
                        + "FROM patients";

        if (patientId != null) {
            sql += " WHERE patient_id = ?";
        }

        sql += " ORDER BY created_at DESC";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            if (patientId != null) {
                statement.setInt(1, patientId);
            }

            try (ResultSet result = statement.executeQuery()) {

                tableModel.setRowCount(0);

                while (result.next()) {
                    tableModel.addRow(new Object[]{
                            result.getInt("patient_id"),
                            result.getString("name"),
                            result.getInt("age"),
                            result.getString("symptoms"),
                            result.getString("demo_urgency"),
                            result.getString("recommended_department"),
                            result.getTimestamp("created_at")
                    });
                }
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Could not load patient records.\n"
                            + "Check your database connection "
                            + "and configuration.",
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );

            System.err.println(
                    "Loading patient records failed: " + ex.getMessage()
            );
        }
    }

    /**
     * Searches for a patient by ID.
     */
    private void searchPatient() {

        String searchText = searchField.getText().trim();

        if (searchText.isEmpty()) {
            showValidationError(
                    "Enter a patient ID to search.",
                    searchField
            );
            return;
        }

        int id;

        try {
            id = Integer.parseInt(searchText);
        } catch (NumberFormatException ex) {
            showValidationError(
                    "Patient ID must be a valid whole number.",
                    searchField
            );
            return;
        }

        if (id <= 0) {
            showValidationError(
                    "Patient ID must be a positive integer.",
                    searchField
            );
            return;
        }

        loadPatients(id);

        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "No patient found with ID: " + id,
                    "Patient Not Found",
                    JOptionPane.INFORMATION_MESSAGE
            );
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MediSortGUI gui = new MediSortGUI();
            gui.setVisible(true);
        });
    }
}
