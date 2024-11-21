import java.time.LocalDate;
import java.util.Optional;

import java.util.List;

public class Trip {
    private String routeId;                        // Required
    private String tripId;                         // Required
    private String serviceId;                      // Required
    private List<LocalDate> serviceDates;          // Required for service dates
    private Optional<String> tripHeadsign;         // Optional
    private Optional<Integer> directionId;         // Optional (0 = outbound, 1 = inbound)
    private Optional<String> blockId;              // Optional
    private Optional<String> shapeId;              // Optional
    private Optional<Integer> wheelchairAccessible;// Optional (0 = no info, 1 = accessible, 2 = not accessible)
    private Optional<Integer> bikesAllowed;        // Optional (0 = no info, 1 = allowed, 2 = not allowed)

    public Trip(String routeId, String tripId, String serviceId,
                Optional<String> tripHeadsign, Optional<Integer> directionId,
                Optional<String> blockId, Optional<String> shapeId,
                Optional<Integer> wheelchairAccessible, Optional<Integer> bikesAllowed) {
        this.routeId = routeId;
        this.tripId = tripId;
        this.serviceId = serviceId;
        this.tripHeadsign = tripHeadsign;
        this.directionId = directionId;
        this.blockId = blockId;
        this.shapeId = shapeId;
        this.wheelchairAccessible = wheelchairAccessible;
        this.bikesAllowed = bikesAllowed;
    }

    // Constructor with only required fields
    public Trip(String routeId, String tripId, String serviceId) {
        this(routeId, tripId, serviceId, Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
    }

    // Getter and setter for serviceDates
    public List<LocalDate> getServiceDates() { return serviceDates; }
    public void setServiceDates(List<LocalDate> serviceDates) { this.serviceDates = serviceDates; }


    // Getters and setters
    public String getRouteId() { return routeId; }
    public void setRouteId(String routeId) { this.routeId = routeId; }

    public String getTripId() { return tripId; }
    public void setTripId(String tripId) { this.tripId = tripId; }

    public String getServiceId() { return serviceId; }
    public void setServiceId(String serviceId) { this.serviceId = serviceId; }

    public Optional<String> getTripHeadsign() { return tripHeadsign; }
    public void setTripHeadsign(Optional<String> tripHeadsign) { this.tripHeadsign = tripHeadsign; }

    public Optional<Integer> getDirectionId() { return directionId; }
    public void setDirectionId(Optional<Integer> directionId) { this.directionId = directionId; }

    public Optional<String> getBlockId() { return blockId; }
    public void setBlockId(Optional<String> blockId) { this.blockId = blockId; }

    public Optional<String> getShapeId() { return shapeId; }
    public void setShapeId(Optional<String> shapeId) { this.shapeId = shapeId; }

    public Optional<Integer> getWheelchairAccessible() { return wheelchairAccessible; }
    public void setWheelchairAccessible(Optional<Integer> wheelchairAccessible) { this.wheelchairAccessible = wheelchairAccessible; }

    public Optional<Integer> getBikesAllowed() { return bikesAllowed; }
    public void setBikesAllowed(Optional<Integer> bikesAllowed) { this.bikesAllowed = bikesAllowed; }
}