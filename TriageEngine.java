
public class TriageEngine {

    public String assess(Patient patient) {
        String symptoms = patient.getSymptoms().toLowerCase();

        // Educational demonstration rules only.
        if (symptoms.contains("severe chest pain")
                || symptoms.contains("difficulty breathing")) {
            return "CRITICAL";
        }

        if (symptoms.contains("high fever")
                || symptoms.contains("persistent vomiting")) {
            return "HIGH";
        }

        if (symptoms.contains("headache")
                || symptoms.contains("cough")) {
            return "MODERATE";
        }

        return "LOW";
    }

    public String recommendDepartment(Patient patient) {
        String symptoms = patient.getSymptoms().toLowerCase();

        if (symptoms.contains("chest pain")
                || symptoms.contains("difficulty breathing")) {
            return "Emergency";
        }

        if (symptoms.contains("fever")
                || symptoms.contains("cough")
                || symptoms.contains("headache")) {
            return "General Medicine";
        }

        return "General Consultation";
    }
}
