package application;

import domain.model.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class Terminal {
    private Appointment selectedAppointment;
    private final List<Patient> patients;
    private final List<HealthcareProfessional> professionals;
    private final List<Appointment> appointments = new ArrayList<>();
    private final List<Schedule> schedules;

    public Terminal(List<Patient> patients, List<HealthcareProfessional> professionals,
                    List<Schedule> schedules) {
        this.patients = patients;
        this.professionals = professionals;
        this.schedules = schedules;
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

}
