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

    // pattern: DAILY, MONTHLY (same day of month as from), or day name(s) such as "MONDAY" or "MONDAY,WEDNESDAY"
    public List<AppointmentSlot> makeAvailability(LocalDate from, LocalDate to, String pattern, LocalTime start, LocalTime end) {
        if (to.isBefore(from)) throw new IllegalArgumentException("End date must not be before start date");
        List<LocalDateTime[]> ranges = new ArrayList<>();
        for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) {
            if (matches(date, from, pattern)) {
                LocalDateTime rangeStart = LocalDateTime.of(date, start);
                LocalDateTime rangeEnd = LocalDateTime.of(date, end);
                if (!allows(rangeStart, rangeEnd)) throw new IllegalArgumentException("Slot end must be after start");
                if (overlapsExistingSlot(rangeStart, rangeEnd)) throw new IllegalStateException("Conflicting availability on " + date);
                ranges.add(new LocalDateTime[]{rangeStart, rangeEnd});
            }
        }
        List<AppointmentSlot> created = new ArrayList<>();
        for (LocalDateTime[] range : ranges) {
            created.add(addSlot(range[0], range[1]));
        }
        return created;
    }

    private boolean matches(LocalDate date, LocalDate from, String pattern) {
        String normalized = pattern.trim().toUpperCase();
        if (normalized.equals("DAILY")) return true;
        if (normalized.equals("MONTHLY")) return date.getDayOfMonth() == from.getDayOfMonth();
        for (String day : normalized.split(",")) {
            if (date.getDayOfWeek().name().equals(day.trim())) return true;
        }
        return false;
    }

    private boolean overlapsExistingSlot(LocalDateTime start, LocalDateTime end) {
        for (AppointmentSlot slot : slots) {
            if (start.isBefore(slot.endTime()) && slot.startTime().isBefore(end)) return true;
        }
        return false;
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
