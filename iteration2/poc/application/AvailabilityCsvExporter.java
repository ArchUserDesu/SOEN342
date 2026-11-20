package application;

import domain.model.*;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

// Export the entire availability data to a CSV file

public final class AvailabilityCsvExporter {
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");

    private final List<HealthcareProfessional> professionals;

    public AvailabilityCsvExporter(List<HealthcareProfessional> professionals) {
        this.professionals = professionals;
    }

    // Return number of slot rows written 
    public int exportTo(String filePath) throws IOException {
        int rows = 0;
        try (BufferedWriter writer = Files.newBufferedWriter(Path.of(filePath), StandardCharsets.UTF_8)) {
            writer.write("First Name,Last Name,Title,Specialty,Date,Start,End,Status");
            writer.newLine();
            for (HealthcareProfessional current : professionals) {
                if (!(current instanceof InternalHealthcareProfessional)) continue;
                InternalHealthcareProfessional professional = (InternalHealthcareProfessional) current;
                String specialty = professional.specialties().isEmpty()
                        ? "" : professional.specialties().get(0).name();
                for (Schedule schedule : professional.schedules()) {
                    List<AppointmentSlot> sorted = new ArrayList<>(schedule.slots());
                    sorted.sort(Comparator.comparing(AppointmentSlot::startTime));
                    for (AppointmentSlot slot : sorted) {
                        writer.write(String.join(",",
                                escape(professional.firstName()),
                                escape(professional.lastName()),
                                escape(professional.title()),
                                escape(specialty),
                                slot.startTime().toLocalDate().toString(),
                                slot.startTime().format(TIME),
                                slot.endTime().format(TIME),
                                escape(slot.status())));
                        writer.newLine();
                        rows++;
                    }
                }
            }
        }
        return rows;
    }

    private static String escape(String value) {
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
