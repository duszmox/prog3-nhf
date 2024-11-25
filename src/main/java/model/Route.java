package model;

import java.awt.*;
import java.util.Optional;

public class Route {
    private Optional<String> agencyId;        // Optional
    private String routeId;                   // Required
    private String routeShortName;            // Required
    private Optional<String> routeLongName;   // Optional
    private int routeType;                    // Required
    private Optional<String> routeDesc;       // Optional
    private Optional<String> routeColor;      // Optional (hex code, e.g., #FFFFFF)
    private Optional<String> routeTextColor;  // Optional (hex code, e.g., #000000)
    private Optional<Integer> routeSortOrder; // Optional

    // Constructor with all fields (including optional ones)
    public Route(Optional<String> agencyId, String routeId, String routeShortName,
                 Optional<String> routeLongName, int routeType, Optional<String> routeDesc,
                 Optional<String> routeColor, Optional<String> routeTextColor,
                 Optional<Integer> routeSortOrder) {
        this.agencyId = agencyId;
        this.routeId = routeId;
        this.routeShortName = routeShortName;
        this.routeLongName = routeLongName;
        this.routeType = routeType;
        this.routeDesc = routeDesc;
        this.routeColor = routeColor;
        this.routeTextColor = routeTextColor;
        this.routeSortOrder = routeSortOrder;
    }

    // Constructor with only required fields
    public Route(String routeId, String routeShortName, int routeType) {
        this(Optional.empty(), routeId, routeShortName, Optional.empty(), routeType,
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
    }

    // Getters and setters
    public Optional<String> getAgencyId() { return agencyId; }
    public void setAgencyId(Optional<String> agencyId) { this.agencyId = agencyId; }

    public String getRouteId() { return routeId; }
    public void setRouteId(String routeId) { this.routeId = routeId; }

    public String getRouteShortName() { return routeShortName; }
    public void setRouteShortName(String routeShortName) { this.routeShortName = routeShortName; }

    public Optional<String> getRouteLongName() { return routeLongName; }
    public void setRouteLongName(Optional<String> routeLongName) { this.routeLongName = routeLongName; }

    public int getRouteType() { return routeType; }
    public void setRouteType(int routeType) { this.routeType = routeType; }

    public Optional<String> getRouteDesc() { return routeDesc; }
    public void setRouteDesc(Optional<String> routeDesc) { this.routeDesc = routeDesc; }

    public Optional<String> getRouteColor() { return routeColor; }
    public void setRouteColor(Optional<String> routeColor) { this.routeColor = routeColor; }

    public Color getColor() {
            String cString = "#" + routeColor.orElse("FFFFFF");
            return new Color(
            Integer.valueOf( cString.substring( 1, 3 ), 16 ),
            Integer.valueOf( cString.substring( 3, 5 ), 16 ),
            Integer.valueOf( cString.substring( 5, 7 ), 16 ) ); }

    public Optional<String> getRouteTextColor() { return routeTextColor; }
    public void setRouteTextColor(Optional<String> routeTextColor) { this.routeTextColor = routeTextColor; }

    public Optional<Integer> getRouteSortOrder() { return routeSortOrder; }
    public void setRouteSortOrder(Optional<Integer> routeSortOrder) { this.routeSortOrder = routeSortOrder; }
}