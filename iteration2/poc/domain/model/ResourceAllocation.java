package domain.model;

import java.time.LocalDateTime;

public final class ResourceAllocation {
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private final int quantity;
    private String status = "ALLOCATED";

    ResourceAllocation(LocalDateTime startTime, LocalDateTime endTime, int quantity) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.quantity = quantity;
    }

    public boolean overlaps(LocalDateTime start, LocalDateTime end) {
        return status.equals("ALLOCATED") && start.isBefore(endTime) && startTime.isBefore(end);
    }

    public void release() { status = "RELEASED"; }

    LocalDateTime startTime() { return startTime; }
    LocalDateTime endTime() { return endTime; }
    int quantity() { return quantity; }
}
