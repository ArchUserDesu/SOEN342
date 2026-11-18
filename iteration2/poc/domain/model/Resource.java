package domain.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public final class Resource {
    private final String resourceId;
    private final String name;
    private final String type;
    private final int capacity;
    private final List<ResourceAllocation> allocations = new ArrayList<>();

    public Resource(String resourceId, String name, String type, int capacity) {
        if (capacity < 1) throw new IllegalArgumentException("Capacity must be positive");
        this.resourceId = resourceId;
        this.name = name;
        this.type = type;
        this.capacity = capacity;
    }

    public boolean isAvailable(LocalDateTime start, LocalDateTime end, int quantity) {
        if (!end.isAfter(start) || quantity < 1 || quantity > capacity) return false;
        TreeMap<LocalDateTime, Integer> changes = new TreeMap<>();
        for (ResourceAllocation allocation : allocations) {
            if (!allocation.overlaps(start, end)) continue;
            LocalDateTime overlapStart = start.isAfter(allocation.startTime()) ? start : allocation.startTime();
            LocalDateTime overlapEnd = end.isBefore(allocation.endTime()) ? end : allocation.endTime();
            changes.merge(overlapStart, allocation.quantity(), Integer::sum);
            changes.merge(overlapEnd, -allocation.quantity(), Integer::sum);
        }
        int occupied = 0;
        for (int change : changes.values()) {
            occupied += change;
            if (occupied + quantity > capacity) return false;
        }
        return true;
    }

    public ResourceAllocation allocate(Appointment appointment, int quantity) {
        LocalDateTime start = appointment.dateTime();
        LocalDateTime end = appointment.slot().endTime();
        if (!isAvailable(start, end, quantity)) {
            throw new IllegalStateException("Resource is unavailable");
        }
        ResourceAllocation allocation = new ResourceAllocation(
                "ALLOC-" + (allocations.size() + 1), appointment, this, start, end, quantity);
        allocations.add(allocation);
        return allocation;
    }

}
