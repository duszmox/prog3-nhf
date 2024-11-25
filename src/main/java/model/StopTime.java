package model;

import java.time.LocalTime;
import java.util.Optional;

public class StopTime {
    private String tripId;                           // Required
    private String stopId;                           // Required
    private Optional<LocalTime> arrivalTime;         // Optional (LocalTime, handles HH:MM:SS)
    private Optional<LocalTime> departureTime;       // Optional (LocalTime, handles HH:MM:SS)
    private int stopSequence;                        // Required
    private Optional<String> stopHeadsign;           // Optional
    private Optional<Integer> pickupType;            // Optional (0 = regular, 1 = no pickup, etc.)
    private Optional<Integer> dropOffType;           // Optional (0 = regular, 1 = no drop-off, etc.)
    private Optional<Double> shapeDistTraveled;      // Optional (distance along shape)

    // Constructor with all fields
    public StopTime(String tripId, String stopId, Optional<LocalTime> arrivalTime, Optional<LocalTime> departureTime,
                    int stopSequence, Optional<String> stopHeadsign, Optional<Integer> pickupType,
                    Optional<Integer> dropOffType, Optional<Double> shapeDistTraveled) {
        this.tripId = tripId;
        this.stopId = stopId;
        this.arrivalTime = arrivalTime;
        this.departureTime = departureTime;
        this.stopSequence = stopSequence;
        this.stopHeadsign = stopHeadsign;
        this.pickupType = pickupType;
        this.dropOffType = dropOffType;
        this.shapeDistTraveled = shapeDistTraveled;
    }

    // Constructor with only required fields
    public StopTime(String tripId, String stopId, int stopSequence) {
        this(tripId, stopId, Optional.empty(), Optional.empty(), stopSequence,
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
    }

    // Getters and setters
    public String getTripId() { return tripId; }
    public void setTripId(String tripId) { this.tripId = tripId; }

    public String getStopId() { return stopId; }
    public void setStopId(String stopId) { this.stopId = stopId; }

    public Optional<LocalTime> getArrivalTime() { return arrivalTime; }
    public void setArrivalTime(Optional<LocalTime> arrivalTime) { this.arrivalTime = arrivalTime; }

    public Optional<LocalTime> getDepartureTime() { return departureTime; }
    public void setDepartureTime(Optional<LocalTime> departureTime) { this.departureTime = departureTime; }

    public int getStopSequence() { return stopSequence; }
    public void setStopSequence(int stopSequence) { this.stopSequence = stopSequence; }

    public Optional<String> getStopHeadsign() { return stopHeadsign; }
    public void setStopHeadsign(Optional<String> stopHeadsign) { this.stopHeadsign = stopHeadsign; }

    public Optional<Integer> getPickupType() { return pickupType; }
    public void setPickupType(Optional<Integer> pickupType) { this.pickupType = pickupType; }

    public Optional<Integer> getDropOffType() { return dropOffType; }
    public void setDropOffType(Optional<Integer> dropOffType) { this.dropOffType = dropOffType; }

    public Optional<Double> getShapeDistTraveled() { return shapeDistTraveled; }
    public void setShapeDistTraveled(Optional<Double> shapeDistTraveled) { this.shapeDistTraveled = shapeDistTraveled; }
}