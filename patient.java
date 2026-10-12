
public class Patient {

    private final int patientId;
    private final String name;
    private final int age;
    private final String symptoms;

    public Patient(int patientId, String name, int age, String symptoms) {

        if (patientId <= 0) {
            throw new IllegalArgumentException(
                "Patient ID must be a positive integer."
            );
        }

        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "Patient name cannot be empty."
            );
        }

        String cleanedName = name.trim();

        if (cleanedName.length() > 100) {
            throw new IllegalArgumentException(
                "Patient name cannot exceed 100 characters."
            );
        }

        if (!cleanedName.matches(
                "[\\p{L}][\\p{L} .'-]*")) {
            throw new IllegalArgumentException(
                "Enter a valid name using letters and normal name punctuation."
            );
        }

        if (age < 0 || age > 130) {
            throw new IllegalArgumentException(
                "Age must be between 0 and 130."
            );
        }

        if (symptoms == null || symptoms.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "Symptoms cannot be empty."
            );
        }

        String cleanedSymptoms = symptoms.trim();

        if (cleanedSymptoms.length() > 2000) {
            throw new IllegalArgumentException(
                "Symptoms cannot exceed 2000 characters."
            );
        }

        this.patientId = patientId;
        this.name = cleanedName;
        this.age = age;
        this.symptoms = cleanedSymptoms;
    }

    public int getPatientId() {
        return patientId;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public String getSymptoms() {
        return symptoms;
    }
}
