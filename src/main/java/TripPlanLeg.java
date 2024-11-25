import model.Stop;

import java.time.Duration;
import java.time.LocalTime;

public class TripPlanLeg {
    private final LegType legType;          // Type of leg: TRANSIT, WALK, TRANSFER
    private Stop fromStop;            // Starting stop or location
    private Stop toStop;              // Ending stop or location
    private LocalTime startTime;      // Start time of the leg
    private LocalTime endTime;        // End time of the leg
    private String tripId;            // Trip ID for transit legs
    private String routeId;           // Route ID for transit legs
    private String routeShortName;    // Short name of the route
    private String routeLongName;     // Long name of the route
    private double distance;          // Distance in meters for walking legs
    private long duration;            // Duration of the leg in seconds (for transfers)
    private long waitTime;

    // Constructor for TRANSIT and WALK legs
    public TripPlanLeg(LegType legType, Stop fromStop, Stop toStop, LocalTime startTime, LocalTime endTime,
                       String tripId, String routeId, String routeShortName, String routeLongName, double distance, long duration) {
        this.legType = legType;
        this.fromStop = fromStop;
        this.toStop = toStop;
        this.startTime = startTime;
        this.endTime = endTime;
        this.tripId = tripId;
        this.routeId = routeId;
        this.routeShortName = routeShortName;
        this.routeLongName = routeLongName;
        this.distance = distance;
        this.duration = duration;
    }

    // Constructor for TRANSFER legs
    public TripPlanLeg(LegType legType, Stop transferStop, LocalTime startTime, LocalTime endTime, long duration) {
        this.legType = legType;
        this.fromStop = transferStop;
        this.toStop = transferStop;
        this.startTime = startTime;
        this.endTime = endTime;
        this.duration = duration;

    }

    public TripPlanLeg(TripPlanLeg prevLeg) {
        this.legType = prevLeg.getLegType();
        this.fromStop = prevLeg.getFromStop();
        this.toStop = prevLeg.getToStop();
        this.startTime = prevLeg.getStartTime();
        this.endTime = prevLeg.getEndTime();
        this.tripId = prevLeg.getTripId();
        this.routeId = prevLeg.getRouteId();
        this.routeShortName = prevLeg.getRouteShortName();
        this.routeLongName = prevLeg.getRouteLongName();
        this.distance = prevLeg.getDistance();
        this.duration = prevLeg.getDuration();
        this.waitTime = prevLeg.getWaitTime();
    }

    // Getters and Setters

    public Stop getFromStop() {
        return fromStop;
    }

    public LegType getLegType() {
        return legType;
    }

    public void setFromStop(Stop fromStop) {
        this.fromStop = fromStop;
    }

    public Stop getToStop() {
        return toStop;
    }

    public void setToStop(Stop toStop) {
        this.toStop = toStop;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public long getWaitTime() {
        return waitTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public String getRouteShortName() {
        return routeShortName;
    }
    public void setRouteShortName(String routeShortName) {
        this.routeShortName = routeShortName;
    }
    public String getRouteLongName() {
        return routeLongName;
    }
    public void setRouteLongName(String routeLongName) {
        this.routeLongName = routeLongName;
    }

    public enum LegType {
        TRANSIT,
        WALK,
        TRANSFER,
        WAIT
    }
}