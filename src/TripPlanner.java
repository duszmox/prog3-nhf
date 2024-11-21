import model.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Duration;
import java.util.*;

public class TripPlanner {

    private final List<Stop> stops;
    private final List<StopTime> stopTimes;
    private final List<Pathway> pathways;
    private final List<Trip> trips;
    private List<Route> routes;

    // Constructor
    public TripPlanner(List<Stop> stops, List<StopTime> stopTimes, List<Pathway> pathways, List<Trip> trips, List<Route> routes) {
        this.stops = stops;
        this.stopTimes = stopTimes;
        this.pathways = pathways;
        this.trips = trips;
        this.routes = routes;
    }

    // Main function to find the optimal path
    public List<TripPlanLeg> findOptimalPath(String startStopId, String endStopId, LocalDate date, LocalTime departureTime) {
        // Step 1: Filter trips operating on the given date
        Set<String> activeTripIds = getActiveTripIds(date);

        // Step 2: Limit stop times to a time window
        List<StopTime> filteredStopTimes = filterStopTimes(activeTripIds, departureTime);

        // Step 3: Build the graph
        Map<String, List<Edge>> graph = buildGraph(filteredStopTimes, startStopId, endStopId);

        // Step 4: Run the shortest path algorithm with transfer limit
        return shortestPath(graph, startStopId, endStopId, departureTime);
    }

    // Function to get active trip IDs on the given date
    private Set<String> getActiveTripIds(LocalDate date) {
        Set<String> activeTripIds = new HashSet<>();
        for (Trip trip : trips) {
            if (trip.getServiceDates().contains(date)) {
                activeTripIds.add(trip.getTripId());
            }
        }
        return activeTripIds;
    }

    // Function to filter stop times within a time window
    private List<StopTime> filterStopTimes(Set<String> activeTripIds, LocalTime departureTime) {
        List<StopTime> filteredStopTimes = new ArrayList<>();
        LocalTime endTime = departureTime.plusHours(2); // 2-hour window

        for (StopTime stopTime : stopTimes) {
            if (activeTripIds.contains(stopTime.getTripId())) {
                Optional<LocalTime> stopDepartureTime = stopTime.getDepartureTime();
                if (stopDepartureTime.isPresent()) {
                    LocalTime time = stopDepartureTime.get();
                    if ((time.equals(departureTime) || time.isAfter(departureTime)) && time.isBefore(endTime)) {
                        filteredStopTimes.add(stopTime);
                    }
                }
            }
        }
        return filteredStopTimes;
    }

    // Function to build the graph
    private Map<String, List<Edge>> buildGraph(List<StopTime> filteredStopTimes, String startStopId, String endStopId) {
        Map<String, List<Edge>> graph = new HashMap<>();

        // Initialize graph nodes
        for (Stop stop : stops) {
            graph.put(stop.getStopId(), new ArrayList<>());
        }

        // Build edges from stop times (scheduled transit)
        Map<String, List<StopTime>> stopTimesByTrip = new HashMap<>();
        for (StopTime stopTime : filteredStopTimes) {
            stopTimesByTrip.computeIfAbsent(stopTime.getTripId(), _ -> new ArrayList<>()).add(stopTime);
        }

        for (List<StopTime> tripStopTimes : stopTimesByTrip.values()) {
            tripStopTimes.sort(Comparator.comparingInt(StopTime::getStopSequence));
            for (int i = 0; i < tripStopTimes.size() - 1; i++) {
                StopTime currentStopTime = tripStopTimes.get(i);
                StopTime nextStopTime = tripStopTimes.get(i + 1);

                if (currentStopTime.getDepartureTime().isPresent() && nextStopTime.getArrivalTime().isPresent()) {
                    String fromStopId = currentStopTime.getStopId();
                    String toStopId = nextStopTime.getStopId();

                    // Calculate travel time in seconds
                    long travelTime = Duration.between(
                            currentStopTime.getDepartureTime().get(),
                            nextStopTime.getArrivalTime().get()
                    ).getSeconds();

                    // Create an edge
                    Edge edge = new Edge(toStopId, travelTime, EdgeType.TRANSIT, currentStopTime.getDepartureTime().get(), currentStopTime.getTripId());
                    graph.get(fromStopId).add(edge);
                }
            }
        }

        // Build walking edges between stops within 500 meters of start and end stops
        Set<String> relevantStopIds = getRelevantStopIds(startStopId, endStopId);

        for (String stopIdA : relevantStopIds) {
            Stop stopA = getStopById(stopIdA);
            for (String stopIdB : relevantStopIds) {
                if (!stopIdA.equals(stopIdB)) {
                    Stop stopB = getStopById(stopIdB);
                    assert stopA != null;
                    assert stopB != null;
                    double distance = haversine(
                            stopA.getStopLat(), stopA.getStopLon(),
                            stopB.getStopLat(), stopB.getStopLon()
                    );
                    if (distance <= 3000) {
                        // Estimate walking time (average speed 5 km/h)
                        long walkingTime = (long) (((distance / 1000) / 5 * 3600) + 0.5);
                        Edge edge = new Edge(stopIdB, walkingTime, EdgeType.WALK, null, null);
                        graph.get(stopIdA).add(edge);
                    }
                }
            }
        }

        // Add pathway edges
        for (Pathway pathway : pathways) {
            String fromStopId = pathway.getFromStopId();
            String toStopId = pathway.getToStopId();
            long traversalTime = pathway.getTraversalTime().orElse(0);

            // Create an edge
            Edge edge = new Edge(toStopId, traversalTime, EdgeType.PATHWAY, null, null);
            graph.get(fromStopId).add(edge);

            // If bidirectional, add the reverse edge
            if (pathway.getIsBidirectional() == 1) {
                Edge reverseEdge = new Edge(fromStopId, traversalTime, EdgeType.PATHWAY, null, null);
                graph.get(toStopId).add(reverseEdge);
            }
        }

        return graph;
    }

    // Function to get relevant stop IDs (stops within 500 meters of start and end stops)
    private Set<String> getRelevantStopIds(String startStopId, String endStopId) {
        Set<String> relevantStopIds = new HashSet<>();
        relevantStopIds.add(startStopId);
        relevantStopIds.add(endStopId);

        Stop startStop = getStopById(startStopId);
        Stop endStop = getStopById(endStopId);

        for (Stop stop : stops) {
            if (!stop.getStopId().equals(startStopId) && !stop.getStopId().equals(endStopId)) {
                assert startStop != null;
                double distanceToStart = haversine(
                        startStop.getStopLat(), startStop.getStopLon(),
                        stop.getStopLat(), stop.getStopLon()
                );
                assert endStop != null;
                double distanceToEnd = haversine(
                        endStop.getStopLat(), endStop.getStopLon(),
                        stop.getStopLat(), stop.getStopLon()
                );
                if (distanceToStart <= 3000 || distanceToEnd <= 3000) {
                    relevantStopIds.add(stop.getStopId());
                }
            }
        }
        return relevantStopIds;
    }

    // Helper function to get model.Stop by ID
    private Stop getStopById(String stopId) {
        for (Stop stop : stops) {
            if (stop.getStopId().equals(stopId)) {
                return stop;
            }
        }
        return null;
    }

    // Shortest path algorithm with transfer limit and detailed leg information
    private List<TripPlanLeg> shortestPath(Map<String, List<Edge>> graph, String startStopId, String endStopId, LocalTime departureTime) {
        // Priority queue to select the next node with the earliest arrival time
        PriorityQueue<NodeEntry> queue = new PriorityQueue<>(Comparator.comparingLong(ne -> ne.earliestArrivalTime));
        queue.add(new NodeEntry(startStopId, departureTime.toSecondOfDay(), null, 0, null, null));

        // Map to keep track of the earliest arrival times
        Map<String, Long> earliestArrivalTimes = new HashMap<>();
        earliestArrivalTimes.put(startStopId, (long) departureTime.toSecondOfDay());

        // Map to reconstruct the path
        Map<String, NodeEntry> previousNodes = new HashMap<>();

        while (!queue.isEmpty()) {
            NodeEntry current = queue.poll();
            String currentStopId = current.stopId;

            // Early exit if we reached the destination
            if (currentStopId.equals(endStopId)) {
                break;
            }

            // Explore neighboring edges
            for (Edge edge : graph.getOrDefault(currentStopId, new ArrayList<>())) {
                String neighborStopId = edge.toStopId;
                long arrivalTimeAtNeighbor;
                int transfers = current.transfers;
                String currentTripId = current.tripId;

                if (edge.type == EdgeType.TRANSIT) {
                    // For transit edges, consider only departures after current time
                    if (edge.departureTime != null && edge.departureTime.toSecondOfDay() >= current.earliestArrivalTime) {
                        arrivalTimeAtNeighbor = edge.departureTime.toSecondOfDay() + edge.travelTime;

                        // Check if transfer is needed
                        if (currentTripId == null || !currentTripId.equals(edge.tripId)) {
                            transfers += 1;
                        }

                        // Skip if transfers exceed limit
                        if (transfers > 4) {
                            continue;
                        }
                    } else {
                        continue; // Can't catch this transit edge
                    }
                } else {
                    // For walking and pathways, start immediately
                    arrivalTimeAtNeighbor = current.earliestArrivalTime + edge.travelTime;
                    // Transfer occurs if switching from transit to walk or pathway
                    if (currentTripId != null) {
                        transfers += 1;
                        currentTripId = null; // Reset trip ID when walking
                    }
                }

                // Check if this is a better arrival time
                if (arrivalTimeAtNeighbor < earliestArrivalTimes.getOrDefault(neighborStopId, Long.MAX_VALUE)) {
                    earliestArrivalTimes.put(neighborStopId, arrivalTimeAtNeighbor);
                    NodeEntry neighborEntry = new NodeEntry(neighborStopId, arrivalTimeAtNeighbor, current, transfers, edge.tripId != null ? edge.tripId : currentTripId, edge);
                    previousNodes.put(neighborStopId, neighborEntry);
                    queue.add(neighborEntry);
                }
            }
        }

        // Reconstruct the path with detailed leg information
        List<TripPlanLeg> tripPlan = new ArrayList<>();
        NodeEntry currentNode = previousNodes.get(endStopId);

        if (currentNode == null) {
            // No path found
            System.out.println("No available path found.");
            return new ArrayList<>();
        }

// Map to get model.Stop objects by stop ID
        Map<String, Stop> stopMap = new HashMap<>();
        for (Stop stop : stops) {
            stopMap.put(stop.getStopId(), stop);
        }

// Map to get model.Trip objects by trip ID
        Map<String, Trip> tripMap = new HashMap<>();
        for (Trip trip : trips) {
            tripMap.put(trip.getTripId(), trip);
        }

// Map to get model.Route objects by route ID
        Map<String, Route> routeMap = new HashMap<>();
        for (Route route : routes) {
            routeMap.put(route.getRouteId(), route);
        }

// Reconstruct the path in reverse order
        while (currentNode.previousNode != null) {
            NodeEntry prevNode = currentNode.previousNode;
            Edge edge = currentNode.edge;

            Stop fromStop = stopMap.get(prevNode.stopId);
            Stop toStop = stopMap.get(currentNode.stopId);

            LocalTime startTime = LocalTime.ofSecondOfDay(prevNode.earliestArrivalTime % 86400);
            LocalTime endTime = LocalTime.ofSecondOfDay(currentNode.earliestArrivalTime % 86400);

            EdgeType mode = edge.type;
            String tripId = edge.tripId;
            String routeId = null;
            String routeShortName = null;
            String routeLongName = null;

            // Get route information from trip ID
            if (tripId != null) {
                Trip trip = tripMap.get(tripId);
                if (trip != null) {
                    routeId = trip.getRouteId();
                    Route route = routeMap.get(routeId);
                    if (route != null) {
                        routeShortName = route.getRouteShortName();
                        routeLongName = route.getRouteLongName().orElse("");
                    }
                }
            }

            long transferTime = 0;
            double distance = 0.0;

            if (mode == EdgeType.WALK || mode == EdgeType.PATHWAY) {
                // Calculate distance for walking legs
                distance = haversine(
                        fromStop.getStopLat(), fromStop.getStopLon(),
                        toStop.getStopLat(), toStop.getStopLon()
                );
            }

            TripPlanLeg leg = new TripPlanLeg(
                    mode,
                    fromStop,
                    toStop,
                    startTime,
                    endTime,
                    tripId,
                    routeId,
                    routeShortName,
                    routeLongName,
                    transferTime,
                    distance
            );

            tripPlan.addFirst(leg); // Add to the beginning since we're reconstructing in reverse
            currentNode = prevNode;
        }

        // Optionally, calculate transfer times between legs
        for (int i = 1; i < tripPlan.size(); i++) {
            TripPlanLeg previousLeg = tripPlan.get(i - 1);
            TripPlanLeg currentLeg = tripPlan.get(i);
            if (previousLeg.getTripId() == null || currentLeg.getTripId() == null) {
                continue;
            }
            if (!previousLeg.getTripId().equals(currentLeg.getTripId())) {
                // Transfer occurred
                long transferTimeInSeconds = Duration.between(previousLeg.getEndTime(), currentLeg.getStartTime()).getSeconds();
                currentLeg.setTransferTime(transferTimeInSeconds);
            }
        }

        return tripPlan;
    }

    // Helper function to get model.Trip by ID
    private Trip getTripById(String tripId) {
        for (Trip trip : trips) {
            if (trip.getTripId().equals(tripId)) {
                return trip;
            }
        }
        return null;
    }


    // Edge class to represent connections between stops
    private static class Edge {
        String toStopId;
        long travelTime; // in seconds
        EdgeType type;
        LocalTime departureTime; // Only for transit edges
        String tripId; // Only for transit edges

        Edge(String toStopId, long travelTime, EdgeType type, LocalTime departureTime, String tripId) {
            this.toStopId = toStopId;
            this.travelTime = travelTime;
            this.type = type;
            this.departureTime = departureTime;
            this.tripId = tripId;
        }
    }

    // Node entry for the priority queue
    private class NodeEntry {
        String stopId;
        long earliestArrivalTime; // in seconds from midnight
        NodeEntry previousNode;
        int transfers;
        String tripId;
        Edge edge;

        NodeEntry(String stopId, long earliestArrivalTime, NodeEntry previousNode, int transfers, String tripId, Edge edge) {
            this.stopId = stopId;
            this.earliestArrivalTime = earliestArrivalTime;
            this.previousNode = previousNode;
            this.transfers = transfers;
            this.tripId = tripId;
            this.edge = edge;
        }
    }

    // Haversine formula to calculate the distance between two points
    public static double haversine(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radius of the Earth in kilometers
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c * 1000; // Convert to meters
    }
}
