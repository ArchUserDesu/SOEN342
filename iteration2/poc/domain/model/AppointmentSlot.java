package domain.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class AppointmentSlot {
    private final String slotId;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private String status = "AVAILABLE";
    private final Schedule schedule;
    private final List<Appointment> appointments = new ArrayList<>();

    public AppointmentSlot(String slotId, LocalDateTime startTime, LocalDateTime endTime, Schedule schedule) {
        this.slotId = slotId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.schedule = schedule;
    }

    public boolean isAvailable() { return status.equals("AVAILABLE"); }
    public void reserve(Appointment appointment) {
        if (!isAvailable()) throw new IllegalStateException("Appointment slot is no longer available");
        appointments.add(appointment);
        status = "RESERVED";
    }
    public void release() { appointments.clear(); status = "AVAILABLE"; }
    public String slotId() { return slotId; }
    public LocalDateTime startTime() { return startTime; }
    public LocalDateTime endTime() { return endTime; }
    public String status() { return status; }
    public Schedule schedule() { return schedule; }
    public List<Appointment> appointments() { return appointments; }
}
