package domain.model;

import java.time.*;
import java.util.ArrayList;
import java.util.List;

public final class Schedule {
    private final String scheduleId;
    private final String scheduleConstraints;
    private final InternalHealthcareProfessional professional;
    private final List<AppointmentSlot> slots = new ArrayList<>();

    public Schedule(String scheduleId, String scheduleConstraints,
                    InternalHealthcareProfessional professional) {
        this.scheduleId = scheduleId;
        this.scheduleConstraints = scheduleConstraints;
        this.professional = professional;
    }

    public List<AppointmentSlot> findAvailability(LocalDateTime from, LocalDateTime to) {
        List<AppointmentSlot> availableSlots = new ArrayList<>();
        for (AppointmentSlot slot : slots) {
            boolean inRange = !slot.startTime().isBefore(from)
                    && !slot.startTime().isAfter(to);
            if (slot.isAvailable() && inRange) {
                availableSlots.add(slot);
            }
        }
        return availableSlots;
    }

    public boolean allows(LocalDateTime start, LocalDateTime end) { return end.isAfter(start); }

    public void makeAvailability(LocalDate from, LocalDate to, String pattern, LocalTime start, LocalTime end) {
        for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) {
            if (pattern.equalsIgnoreCase("DAILY") || date.getDayOfWeek().name().equalsIgnoreCase(pattern)) {
                addSlot(LocalDateTime.of(date, start), LocalDateTime.of(date, end));
            }
        }
    }

    public AppointmentSlot addSlot(LocalDateTime start, LocalDateTime end) {
        if (!allows(start, end)) throw new IllegalArgumentException("Slot end must be after start");
        AppointmentSlot slot = new AppointmentSlot("SLOT-" + (slots.size() + 1), start, end, this);
        slots.add(slot);
        return slot;
    }

    public String scheduleId() { return scheduleId; }
    public String scheduleConstraints() { return scheduleConstraints; }
    public InternalHealthcareProfessional professional() { return professional; }
    public List<AppointmentSlot> slots() { return slots; }
}
