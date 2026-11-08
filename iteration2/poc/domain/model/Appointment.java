package domain.model;

import java.time.LocalDateTime;

public final class Appointment {
    private final String appointmentId;
    private LocalDateTime dateTime;
    private String status = "REQUESTED";
    private final String serviceType;
    private LocalDateTime arrivalTime;
    private String arrivalStatus = "NOT_ARRIVED";
    private final Patient patient;
    private AppointmentSlot slot;

    public Appointment(String appointmentId, Patient patient, String serviceType,
                       LocalDateTime dateTime, AppointmentSlot slot) {
        this.appointmentId = appointmentId;
        this.patient = patient;
        this.serviceType = serviceType;
        this.dateTime = dateTime;
        this.slot = slot;
    }

    public void confirm() { status = "CONFIRMED"; }
    public void reschedule(AppointmentSlot newSlot) { slot.release(); slot = newSlot; dateTime = newSlot.startTime(); newSlot.reserve(this); }
    public void cancel() { status = "CANCELLED"; slot.release(); }
    public void registerArrival(LocalDateTime at) { arrivalTime = at; arrivalStatus = "ARRIVED"; }
    public boolean isEligibleForConsultation() { return status.equals("CONFIRMED") && arrivalStatus.equals("ARRIVED"); }
    public boolean isBillable() { return !patient.coverageType().equalsIgnoreCase("RAMQ"); }
    public void complete() { status = "COMPLETED"; }
    public String appointmentId() { return appointmentId; }
    public LocalDateTime dateTime() { return dateTime; }
    public String status() { return status; }
    public String serviceType() { return serviceType; }
    public LocalDateTime arrivalTime() { return arrivalTime; }
    public String arrivalStatus() { return arrivalStatus; }
    public Patient patient() { return patient; }
    public AppointmentSlot slot() { return slot; }
}
