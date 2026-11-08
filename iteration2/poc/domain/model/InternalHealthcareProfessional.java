package domain.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class InternalHealthcareProfessional extends HealthcareProfessional {
    private final List<Specialty> specialties = new ArrayList<>();
    private final List<ClinicLocation> clinicLocations = new ArrayList<>();
    private final List<Schedule> schedules = new ArrayList<>();

    public InternalHealthcareProfessional(String professionalId, String firstName, String lastName,
                                          String title, String professionalInformation, String phoneNumber) {
        super(professionalId, firstName, lastName, title, professionalInformation, phoneNumber);
    }

    public List<AppointmentSlot> findAvailability(LocalDateTime from, LocalDateTime to) {
        List<AppointmentSlot> availableSlots = new ArrayList<>();
        for (Schedule schedule : schedules) {
            availableSlots.addAll(schedule.findAvailability(from, to));
        }
        return availableSlots;
    }

    public boolean canProvide(String serviceType) {
        for (Specialty specialty : specialties) {
            if (specialty.name().equalsIgnoreCase(serviceType)) {
                return true;
            }
        }
        return false;
    }

    public void addSpecialty(Specialty specialty) { specialties.add(specialty); }
    public void addClinicLocation(ClinicLocation location) { clinicLocations.add(location); }
    public void addSchedule(Schedule schedule) { schedules.add(schedule); }
    public List<Specialty> specialties() { return specialties; }
    public List<ClinicLocation> clinicLocations() { return clinicLocations; }
    public List<Schedule> schedules() { return schedules; }
}
