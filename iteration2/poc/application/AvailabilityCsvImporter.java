package application;

import domain.model.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

// Import professional availabilities from a CSV file.

public final class AvailabilityCsvImporter {
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("H:mm");

    private final List<HealthcareProfessional> professionals;
    private final List<Schedule> schedules;
    private final List<String> errors = new ArrayList<>();

    public AvailabilityCsvImporter(List<HealthcareProfessional> professionals, List<Schedule> schedules) {
        this.professionals = professionals;
        this.schedules = schedules;
    }

    // Import the available slots from the CSV 
    // Skip invalid rows and save the errors
    public int importFrom(String filePath) throws IOException {
        errors.clear();
        int imported = 0;
        try (BufferedReader reader = Files.newBufferedReader(Path.of(filePath), StandardCharsets.UTF_8)) {
            String line;
            int lineNumber = 0;
            boolean firstRow = true;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (lineNumber == 1 && line.startsWith("\uFEFF")) line = line.substring(1); 
                if (line.isBlank()) continue;
                List<String> fields = parseLine(line);
                if (firstRow) {
                    firstRow = false;
                    if (fields.get(0).equalsIgnoreCase("First Name")) continue; // header row
                }
                try {
                    importRow(fields);
                    imported++;
                } catch (IllegalArgumentException | DateTimeException e) {
                    errors.add("Line " + lineNumber + ": " + e.getMessage());
                }
            }
        }
        return imported;
    }

    public List<String> errors() { return errors; }

    private void importRow(List<String> f) {
        if (f.size() < 7) throw new IllegalArgumentException("expected at least 7 columns but found " + f.size());
        String firstName = f.get(0), lastName = f.get(1), title = f.get(2), specialtyName = f.get(3);
        if (firstName.isEmpty() || lastName.isEmpty()) throw new IllegalArgumentException("first and last name are required");

        LocalDate date = LocalDate.parse(f.get(4));
        LocalDateTime start = date.atTime(LocalTime.parse(f.get(5), TIME));
        LocalDateTime end = date.atTime(LocalTime.parse(f.get(6), TIME));
        if (!end.isAfter(start)) throw new IllegalArgumentException("end time must be after start time");

        InternalHealthcareProfessional professional = findProfessional(firstName, lastName, title);
        if (professional != null && overlapsExisting(professional, start, end)) {
            throw new IllegalArgumentException("slot overlaps existing availability of "
                    + professional.getDisplayName() + " on " + date);
        }
        if (professional == null) professional = createProfessional(firstName, lastName, title);

        if (!specialtyName.isEmpty() && !professional.canProvide(specialtyName)) {
            professional.addSpecialty(new Specialty(
                    "SP-" + specialtyName.toUpperCase().replace(' ', '_'), specialtyName, ""));
        }
        scheduleOf(professional).addSlot(start, end);
    }

    private InternalHealthcareProfessional findProfessional(String firstName, String lastName, String title) {
        for (HealthcareProfessional current : professionals) {
            if (current instanceof InternalHealthcareProfessional
                    && current.firstName().equalsIgnoreCase(firstName)
                    && current.lastName().equalsIgnoreCase(lastName)
                    && current.title().equalsIgnoreCase(title)) {
                return (InternalHealthcareProfessional) current;
            }
        }
        return null;
    }

    private InternalHealthcareProfessional createProfessional(String firstName, String lastName, String title) {
        int number = professionals.size() + 1;
        String id;
        do {
            id = String.format("PRO%03d", number++);
        } while (idExists(id));
        InternalHealthcareProfessional professional =
                new InternalHealthcareProfessional(id, firstName, lastName, title, "", "");
        professionals.add(professional);
        return professional;
    }

    private boolean idExists(String id) {
        for (HealthcareProfessional current : professionals) {
            if (current.professionalId().equals(id)) return true;
        }
        return false;
    }

    private Schedule scheduleOf(InternalHealthcareProfessional professional) {
        if (professional.schedules().isEmpty()) {
            Schedule schedule = new Schedule("SCH-" + professional.professionalId(),
                    "Imported availability", professional);
            professional.addSchedule(schedule);
            schedules.add(schedule);
        }
        return professional.schedules().get(0);
    }

    private boolean overlapsExisting(InternalHealthcareProfessional professional, LocalDateTime start, LocalDateTime end) {
        for (Schedule schedule : professional.schedules()) {
            for (AppointmentSlot slot : schedule.slots()) {
                if (start.isBefore(slot.endTime()) && slot.startTime().isBefore(end)) return true;
            }
        }
        return false;
    }

    // Splits one CSV line & supports "quoted, fields"
    static List<String> parseLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') { current.append('"'); i++; }
                    else inQuotes = false;
                } else current.append(c);
            } else if (c == '"') {
                inQuotes = true;
            } else if (c == ',') {
                fields.add(current.toString().trim());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        fields.add(current.toString().trim());
        return fields;
    }
}
