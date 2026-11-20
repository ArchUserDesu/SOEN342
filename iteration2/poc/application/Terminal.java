package application;

import domain.model.*;

import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public final class Terminal {
    private Appointment selectedAppointment;
    private final List<Patient> patients;
    private final List<HealthcareProfessional> professionals;
    private final List<Appointment> appointments = new ArrayList<>();
    private final List<Schedule> schedules;
    private final List<Resource> resources;
    private List<String> lastImportErrors = new ArrayList<>();

    public Terminal(List<Patient> patients, List<HealthcareProfessional> professionals,
                    List<Schedule> schedules, List<Resource> resources) {
        this.patients = patients;
        this.professionals = new ArrayList<>(professionals);
        this.schedules = new ArrayList<>(schedules);
        this.resources = resources;
    }

    public Appointment requestAppointment(String patientId, String serviceType, LocalDateTime dateTime) {
        Patient patient = null;
        for (Patient currentPatient : patients) {
            if (currentPatient.patientId().equals(patientId)) {
                patient = currentPatient;
                break;
            }
        }
        if (patient == null) {
            throw new IllegalStateException("Patient not found");
        }

        InternalHealthcareProfessional professional = null;
        for (HealthcareProfessional currentProfessional : professionals) {
            if (currentProfessional instanceof InternalHealthcareProfessional) {
                InternalHealthcareProfessional internal =
                        (InternalHealthcareProfessional) currentProfessional;
                if (internal.canProvide(serviceType)) {
                    professional = internal;
                    break;
                }
            }
        }
        if (professional == null) {
            throw new IllegalStateException("No professional provides this service");
        }

        List<AppointmentSlot> availableSlots =
                professional.findAvailability(dateTime, dateTime);
        if (availableSlots.isEmpty()) {
            throw new IllegalStateException("No available slot at the requested time");
        }
        AppointmentSlot slot = availableSlots.get(0);

        if (resources.isEmpty()) {
            throw new IllegalStateException("No resources configured");
        }
        for (Resource resource : resources) {
            if (!resource.isAvailable(slot.startTime(), slot.endTime(), 1)) {
                throw new IllegalStateException("Resource unavailable");
            }
        }

        Appointment appointment = new Appointment(
                "APT-" + (appointments.size() + 1), patient, serviceType, dateTime, slot);
        appointment.confirm();
        slot.reserve(appointment);
        appointments.add(appointment);
        selectedAppointment = appointment;
        return appointment;
    }

    public List<Appointment> getAppointmentsForPatient(String patientId) {
        List<Appointment> patientAppointments = new ArrayList<>();

        for (Appointment appointment : appointments) {
            if (appointment.patient().patientId().equals(patientId)) {
                patientAppointments.add(appointment);
            }
        }

        return patientAppointments;
    }

    public List<AppointmentSlot> makeAvailability(String professionalId, LocalDate from, LocalDate to,
                                                  String pattern, LocalTime start, LocalTime end) {
        InternalHealthcareProfessional professional = findInternalProfessional(professionalId);
        if (professional == null) {
            throw new IllegalStateException("Professional not found");
        }
        if (professional.schedules().isEmpty()) {
            professional.addSchedule(new Schedule("SCH-" + professional.professionalId(),
                    "Availability", professional));
        }
        return professional.schedules().get(0).makeAvailability(from, to, pattern, start, end);
    }

    // professionalId and specialty are optional filters (null or blank to ignore)
    public List<AppointmentSlot> searchAvailability(String professionalId, String specialty,
                                                    LocalDate from, LocalDate to) {
        LocalDateTime rangeStart = from.atStartOfDay();
        LocalDateTime rangeEnd = to.atTime(LocalTime.MAX);
        List<AppointmentSlot> found = new ArrayList<>();
        for (HealthcareProfessional currentProfessional : professionals) {
            if (!(currentProfessional instanceof InternalHealthcareProfessional)) continue;
            InternalHealthcareProfessional professional = (InternalHealthcareProfessional) currentProfessional;
            if (professionalId != null && !professionalId.isBlank()
                    && !professional.professionalId().equals(professionalId)) continue;
            if (specialty != null && !specialty.isBlank() && !professional.canProvide(specialty)) continue;
            found.addAll(professional.findAvailability(rangeStart, rangeEnd));
        }
        found.sort((a, b) -> a.startTime().compareTo(b.startTime()));
        return found;
    }

    // Import professional availability from a CSV file, return number of slots imported
    public int importAvailabilityFromCSV(String filePath) {
        AvailabilityCsvImporter importer = new AvailabilityCsvImporter(professionals, schedules);
        lastImportErrors = new ArrayList<>();
        try {
            int imported = importer.importFrom(filePath);
            lastImportErrors = new ArrayList<>(importer.errors());
            return imported;
        } catch (IOException | InvalidPathException error) {
            throw new IllegalStateException("Could not read file: " + error.getMessage());
        }
    }

    // Rows rejected by most recent import
    public List<String> lastImportErrors() {
        return new ArrayList<>(lastImportErrors);
    }

    // Export all professional availability to CSV file, return number of rows written
    public int exportAvailabilityToCSV(String filePath) {
        try {
            return new AvailabilityCsvExporter(professionals).exportTo(filePath);
        } catch (IOException | InvalidPathException error) {
            throw new IllegalStateException("Could not write file: " + error.getMessage());
        }
    }


    private InternalHealthcareProfessional findInternalProfessional(String professionalId) {
        for (HealthcareProfessional currentProfessional : professionals) {
            if (currentProfessional instanceof InternalHealthcareProfessional
                    && currentProfessional.professionalId().equals(professionalId)) {
                return (InternalHealthcareProfessional) currentProfessional;
            }
        }
        return null;
    }
}
