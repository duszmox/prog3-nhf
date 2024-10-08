import java.util.Optional;

public class Pathway {
    private String pathwayId;                 // Required
    private int pathwayMode;                  // Required (1 = Walkway, 2 = Stairs, etc.)
    private int isBidirectional;              // Required (0 = one-way, 1 = bidirectional)
    private String fromStopId;                // Required
    private String toStopId;                  // Required
    private Optional<Integer> traversalTime;  // Optional (time in seconds)

    // Constructor with all fields (including optional traversal time)
    public Pathway(String pathwayId, int pathwayMode, int isBidirectional,
                   String fromStopId, String toStopId, Optional<Integer> traversalTime) {
        this.pathwayId = pathwayId;
        this.pathwayMode = pathwayMode;
        this.isBidirectional = isBidirectional;
        this.fromStopId = fromStopId;
        this.toStopId = toStopId;
        this.traversalTime = traversalTime;
    }

    // Constructor without optional traversal time
    public Pathway(String pathwayId, int pathwayMode, int isBidirectional,
                   String fromStopId, String toStopId) {
        this(pathwayId, pathwayMode, isBidirectional, fromStopId, toStopId, Optional.empty());
    }

    // Getters and setters
    public String getPathwayId() { return pathwayId; }
    public void setPathwayId(String pathwayId) { this.pathwayId = pathwayId; }

    public int getPathwayMode() { return pathwayMode; }
    public void setPathwayMode(int pathwayMode) { this.pathwayMode = pathwayMode; }

    public int getIsBidirectional() { return isBidirectional; }
    public void setIsBidirectional(int isBidirectional) { this.isBidirectional = isBidirectional; }

    public String getFromStopId() { return fromStopId; }
    public void setFromStopId(String fromStopId) { this.fromStopId = fromStopId; }

    public String getToStopId() { return toStopId; }
    public void setToStopId(String toStopId) { this.toStopId = toStopId; }

    public Optional<Integer> getTraversalTime() { return traversalTime; }
    public void setTraversalTime(Optional<Integer> traversalTime) { this.traversalTime = traversalTime; }
}