import java.time.LocalTime;

public class TripPlanLeg {
    private EdgeType mode;          // Mode of transport: TRANSIT, WALK, PATHWAY
    private Stop fromStop;          // Starting stop or location
    private Stop toStop;            // Ending stop or location
    private LocalTime startTime;    // Start time of the leg
    private LocalTime endTime;      // End time of the leg
    private String tripId;          // Trip ID for transit legs
    private String routeId;         // Route ID for transit legs
    private String routeShortName;  // Short name of the route
    private String routeLongName;   // Long name of the route
    private long transferTime;      // Transfer waiting time in seconds
    private double distance;        // Distance in meters for walking legs

    // Updated Constructor
    public TripPlanLeg(EdgeType mode, Stop fromStop, Stop toStop, LocalTime startTime, LocalTime endTime,
                       String tripId, String routeId, String routeShortName, String routeLongName,
                       long transferTime, double distance) {
        this.mode = mode;
        this.fromStop = fromStop;
        this.toStop = toStop;
        this.startTime = startTime;
        this.endTime = endTime;
        this.tripId = tripId;
        this.routeId = routeId;
        this.routeShortName = routeShortName;
        this.routeLongName = routeLongName;
        this.transferTime = transferTime;
        this.distance = distance;
    }

    // Getters and Setters

    public EdgeType getMode() {
        return mode;
    }

    public void setMode(EdgeType mode) {
        this.mode = mode;
    }

    public Stop getFromStop() {
        return fromStop;
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

    public LocalTime getStartTime() {
        return startTime;
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

    public long getTransferTime() {
        return transferTime;
    }

    public void setTransferTime(long transferTime) {
        this.transferTime = transferTime;
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
}
