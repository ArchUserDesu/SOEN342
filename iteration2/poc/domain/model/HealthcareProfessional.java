package domain.model;

public class HealthcareProfessional {
    private final String professionalId;
    private final String firstName;
    private final String lastName;
    private final String title;
    private final String professionalInformation;
    private final String phoneNumber;

    public HealthcareProfessional(String professionalId, String firstName, String lastName,
                                  String title, String professionalInformation, String phoneNumber) {
        this.professionalId = professionalId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.title = title;
        this.professionalInformation = professionalInformation;
        this.phoneNumber = phoneNumber;
    }

    public String getDisplayName() { return title + " " + firstName + " " + lastName; }
    public String professionalId() { return professionalId; }
    public String firstName() { return firstName; }
    public String lastName() { return lastName; }
    public String title() { return title; }
    public String professionalInformation() { return professionalInformation; }
    public String phoneNumber() { return phoneNumber; }
}
