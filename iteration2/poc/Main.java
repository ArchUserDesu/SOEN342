import application.Terminal;
import domain.model.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        Patient patient = new Patient("P001", "Emilie", "Lavoie", "LAVE99010101",
                "RAMQ", LocalDate.of(1999, 1, 1), "514-555-0101");

        // System starts fresh, professionals and schedules are loaded with "Import from CSV"
        Terminal terminal = new Terminal(
                List.of(patient), new ArrayList<>(), new ArrayList<>(),
                List.of(new Resource(1)));

        Scanner input = new Scanner(System.in);

        boolean running = true;
        while (running) {
            System.out.println("\n=== Appointment Tracking System ===");
            System.out.println("1. Book an appointment");
            System.out.println("2. Search availability");
            System.out.println("3. Import availabilities from CSV");
            System.out.println("4. Export availabilities to CSV");
            System.out.println("0. Exit");
            System.out.print("Choice: ");
            String choice = input.nextLine().trim();
            try {
                switch (choice) {
                    case "1": bookAppointment(terminal, patient, input); break;
                    case "2": searchAvailability(terminal, input); break;
                    case "3": importCsv(terminal, input); break;
                    case "4": exportCsv(terminal, input); break;
                    case "0": running = false; break;
                    default: System.out.println("Invalid choice.");
                }
            } catch (RuntimeException error) {
                System.out.println("Rejected: " + error.getMessage());
            }
        }
    }

    private static void bookAppointment(Terminal terminal, Patient patient, Scanner input) {
        System.out.println("Available patients:");
        System.out.println(patient.patientId() + " - "
                + patient.firstName() + " " + patient.lastName());
        System.out.print("Choose patient ID: ");
        String patientId = input.nextLine();
        System.out.print("Service type: ");
        String serviceType = input.nextLine();
        System.out.print("Year: ");
        int year = Integer.parseInt(input.nextLine().trim());
        System.out.print("Month: ");
        int month = Integer.parseInt(input.nextLine().trim());
        System.out.print("Day: ");
        int day = Integer.parseInt(input.nextLine().trim());
        System.out.print("Hour: ");
        int hour = Integer.parseInt(input.nextLine().trim());
        System.out.print("Minute: ");
        int minute = Integer.parseInt(input.nextLine().trim());

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

    private static void importCsv(Terminal terminal, Scanner input) {
        System.out.print("CSV file to import: ");
        String path = input.nextLine().trim();

        int imported = terminal.importAvailabilityFromCSV(path);

        System.out.println("Imported " + imported + " availability slot(s).");

        for (String error : terminal.lastImportErrors()) {
            System.out.println("Skipped - " + error);
        }
    }

    private static void exportCsv(Terminal terminal, Scanner input) {
        System.out.print("CSV file to write: ");
        String path = input.nextLine().trim();

        int exported = terminal.exportAvailabilityToCSV(path);

        System.out.println("Exported " + exported
                + " availability slot(s) to " + path);
    }

    private static void searchAvailability(Terminal terminal, Scanner input) {
        System.out.print("From date (yyyy-MM-dd): ");
        LocalDate from = LocalDate.parse(input.nextLine().trim());

        System.out.print("To date (yyyy-MM-dd): ");
        LocalDate to = LocalDate.parse(input.nextLine().trim());

        System.out.print("Specialty (blank for any): ");
        String specialty = input.nextLine().trim();

        List<AppointmentSlot> slots =
                terminal.searchAvailability(null, specialty, from, to);

        if (slots.isEmpty()) {
            System.out.println("No availability found");
        }

        for (AppointmentSlot slot : slots) {
            System.out.println(
                    slot.schedule().professional().getDisplayName()
                    + " | " + slot.startTime()
                    + " - " + slot.endTime().toLocalTime());
        }
    }
}
