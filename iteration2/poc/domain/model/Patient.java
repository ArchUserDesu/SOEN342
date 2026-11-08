package domain.model;

import java.time.LocalDate;

public final class Patient {
    private final String patientId;
    private final String firstName;
    private final String lastName;
    private final String insuranceNumber;
    private final String coverageType;
    private final LocalDate dateOfBirth;
    private final String phoneNumber;

    public Patient(String patientId, String firstName, String lastName, String insuranceNumber,
                   String coverageType, LocalDate dateOfBirth, String phoneNumber) {
        this.patientId = patientId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.insuranceNumber = insuranceNumber;
        this.coverageType = coverageType;
        this.dateOfBirth = dateOfBirth;
        this.phoneNumber = phoneNumber;
    }

    public boolean matches(String searchCriteria) {
        return patientId.equalsIgnoreCase(searchCriteria)
                || firstName.equalsIgnoreCase(searchCriteria)
                || lastName.equalsIgnoreCase(searchCriteria);
    }

    public String patientId() { return patientId; }
    public String firstName() { return firstName; }
    public String lastName() { return lastName; }
    public String insuranceNumber() { return insuranceNumber; }
    public String coverageType() { return coverageType; }
    public LocalDate dateOfBirth() { return dateOfBirth; }
    public String phoneNumber() { return phoneNumber; }
}
