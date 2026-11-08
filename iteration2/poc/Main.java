import application.Terminal;
import domain.model.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Patient patient = new Patient("P001", "Emilie", "Lavoie", "LAVE99010101",
                "RAMQ", LocalDate.of(1999, 1, 1), "514-555-0101");

        InternalHealthcareProfessional professional = new InternalHealthcareProfessional(
                "PRO001", "Amelie", "Tremblay", "Dr.", "Family physician", "514-555-0110");
        professional.addSpecialty(new Specialty("SP001", "Family Medicine", "Primary medical care"));

        Schedule schedule = new Schedule("SCH001", "Weekday availability", professional);
        schedule.addSlot(LocalDateTime.of(2026, 11, 10, 9, 0),
                LocalDateTime.of(2026, 11, 10, 9, 30));
        professional.addSchedule(schedule);

        Terminal terminal = new Terminal(
                List.of(patient), List.of(professional), List.of(schedule));

        Scanner input = new Scanner(System.in);

        System.out.println("Available patients:");
        System.out.println(patient.patientId() + " - "
                + patient.firstName() + " " + patient.lastName());
        System.out.print("Choose patient ID: ");
        String patientId = input.nextLine();
        System.out.print("Service type: ");
        String serviceType = input.nextLine();
        System.out.print("Year: ");
        int year = input.nextInt();
        System.out.print("Month: ");
        int month = input.nextInt();
        System.out.print("Day: ");
        int day = input.nextInt();
        System.out.print("Hour: ");
        int hour = input.nextInt();
        System.out.print("Minute: ");
        int minute = input.nextInt();

        LocalDateTime dateTime =
                LocalDateTime.of(year, month, day, hour, minute);

        try {
            terminal.requestAppointment(patientId, serviceType, dateTime);
        } catch (IllegalStateException error) {
            System.out.println("Rejected: " + error.getMessage());
        }

        List<Appointment> appointments =
                terminal.getAppointmentsForPatient(patientId);

        System.out.println("\nAppointments for " + patientId + ":");
        if (appointments.isEmpty()) {
            System.out.println("No appointments");
        } else {
            for (Appointment appointment : appointments) {
                System.out.println(
                        appointment.appointmentId() + " | "
                        + appointment.dateTime() + " | "
                        + appointment.serviceType() + " | "
                        + appointment.status());
            }
        }
    }
}
