package model;

import java.util.Optional;

public class Stop {
    private String stopId;                 // Required
    private String stopName;               // Required
    private double stopLat;                // Required
    private double stopLon;                // Required
    private Optional<String> stopCode;     // Optional
    private Optional<Integer> locationType;// Optional (0 = stop, 1 = station, etc.)
    private Optional<String> locationSubType;  // Optional
    private Optional<String> parentStation;    // Optional
    private Optional<Integer> wheelchairBoarding; // Optional (0 = no info, 1 = accessible, 2 = not accessible)

    // Constructor with required and optional fields
    public Stop(String stopId, String stopName, double stopLat, double stopLon,
                Optional<String> stopCode, Optional<Integer> locationType,
                Optional<String> locationSubType, Optional<String> parentStation,
                Optional<Integer> wheelchairBoarding) {
        this.stopId = stopId;
        this.stopName = stopName;
        this.stopLat = stopLat;
        this.stopLon = stopLon;
        this.stopCode = stopCode;
        this.locationType = locationType;
        this.locationSubType = locationSubType;
        this.parentStation = parentStation;
        this.wheelchairBoarding = wheelchairBoarding;
    }

    // Constructor with only required fields
    public Stop(String stopId, String stopName, double stopLat, double stopLon) {
        this(stopId, stopName, stopLat, stopLon, Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
    }

    // Getters and setters
    public String getStopId() { return stopId; }
    public void setStopId(String stopId) { this.stopId = stopId; }

    public String getStopName() { return stopName; }
    public void setStopName(String stopName) { this.stopName = stopName; }

    public double getStopLat() { return stopLat; }
    public void setStopLat(double stopLat) { this.stopLat = stopLat; }

    public double getStopLon() { return stopLon; }
    public void setStopLon(double stopLon) { this.stopLon = stopLon; }

    public Optional<String> getStopCode() { return stopCode; }
    public void setStopCode(Optional<String> stopCode) { this.stopCode = stopCode; }

    public Optional<Integer> getLocationType() { return locationType; }
    public void setLocationType(Optional<Integer> locationType) { this.locationType = locationType; }

    public Optional<String> getLocationSubType() { return locationSubType; }
    public void setLocationSubType(Optional<String> locationSubType) { this.locationSubType = locationSubType; }

    public Optional<String> getParentStation() { return parentStation; }
    public void setParentStation(Optional<String> parentStation) { this.parentStation = parentStation; }

    public Optional<Integer> getWheelchairBoarding() { return wheelchairBoarding; }
    public void setWheelchairBoarding(Optional<Integer> wheelchairBoarding) { this.wheelchairBoarding = wheelchairBoarding; }

    @Override
    public String toString() {
        return getStopName(); // Use the getter method
    }
}