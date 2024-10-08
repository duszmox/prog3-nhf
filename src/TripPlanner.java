import java.time.LocalTime;
import java.util.*;

class TripPlanner {
    private Map<String, Stop> stops = new HashMap<>();
    private Map<String, List<StopTime>> stopTimesByTrip = new HashMap<>();  // Maps trip_id to a list of StopTimes
    private Map<String, List<String>> tripsByStop = new HashMap<>();  // Maps stop_id to a list of trip_ids
    private Map<String, List<Pathway>> pathwaysByStop = new HashMap<>(); // Maps stop_id to a list of Pathways

    public TripPlanner(List<Stop> stopList, List<StopTime> stopTimesList, List<Pathway> pathwayList) {
        // Initialize stops HashMap
        for (Stop stop : stopList) {
            stops.put(stop.getStopId(), stop);
        }

        // Initialize stopTimes and tripsByStop HashMaps
        for (StopTime stopTime : stopTimesList) {
            stopTimesByTrip.computeIfAbsent(stopTime.getTripId(), k -> new ArrayList<>()).add(stopTime);
            tripsByStop.computeIfAbsent(stopTime.getStopId(), k -> new ArrayList<>()).add(stopTime.getTripId());
        }

        // Initialize pathwaysByStop HashMap
        for (Pathway pathway : pathwayList) {
            pathwaysByStop.computeIfAbsent(pathway.getFromStopId(), k -> new ArrayList<>()).add(pathway);
            // If the pathway is bidirectional, also add the reverse direction
            if (pathway.getIsBidirectional() == 1) {
                pathwaysByStop.computeIfAbsent(pathway.getToStopId(), k -> new ArrayList<>()).add(
                        new Pathway(pathway.getPathwayId(), pathway.getPathwayMode(), 1, pathway.getToStopId(), pathway.getFromStopId(), pathway.getTraversalTime()));
            }
        }
    }

    class Node implements Comparable<Node> {
        String stopId;
        int time;  // Minutes since the start of the day

        public Node(String stopId, int time) {
            this.stopId = stopId;
            this.time = time;
        }

        @Override
        public int compareTo(Node other) {
            return Integer.compare(this.time, other.time);
        }
    }

    public List<Stop> findShortestPath(String startStopId, String endStopId, int currentTime) {
        PriorityQueue<Node> pq = new PriorityQueue<>();
        Map<String, Integer> distances = new HashMap<>();  // Maps stop_id to the minimum time to reach it
        Map<String, String> previousStop = new HashMap<>();  // Maps stop_id to the previous stop in the shortest path

        // Initialize distances
        for (String stopId : stops.keySet()) {
            distances.put(stopId, Integer.MAX_VALUE);
        }
        distances.put(startStopId, currentTime);
        pq.add(new Node(startStopId, currentTime));

        // Dijkstra's algorithm
        while (!pq.isEmpty()) {
            Node current = pq.poll();

            if (current.stopId.equals(endStopId)) {
                break;  // We found the shortest path to the end stop
            }

            // Explore trips from the current stop
            if (tripsByStop.containsKey(current.stopId)) {
                for (String tripId : tripsByStop.get(current.stopId)) {
                    List<StopTime> stopTimes = stopTimesByTrip.get(tripId);

                    for (int i = 0; i < stopTimes.size() - 1; i++) {
                        StopTime currentStopTime = stopTimes.get(i);
                        StopTime nextStopTime = stopTimes.get(i + 1);

                        if (currentStopTime.getStopId().equals(current.stopId)) {
                            int departureTime = currentStopTime.getDepartureTime()
                                    .map(LocalTime::toSecondOfDay)
                                    .orElse(0) / 60;
                            int arrivalTime = nextStopTime.getArrivalTime()
                                    .map(LocalTime::toSecondOfDay)
                                    .orElse(0) / 60;

                            if (departureTime >= current.time && arrivalTime < distances.get(nextStopTime.getStopId())) {
                                distances.put(nextStopTime.getStopId(), arrivalTime);
                                previousStop.put(nextStopTime.getStopId(), current.stopId);
                                pq.add(new Node(nextStopTime.getStopId(), arrivalTime));
                            }
                        }
                    }
                }
            }

            // Explore pathways from the current stop
            if (pathwaysByStop.containsKey(current.stopId)) {
                for (Pathway pathway : pathwaysByStop.get(current.stopId)) {
                    int traversalTime = pathway.getTraversalTime().orElse(0) / 60;  // Convert to minutes
                    int newTime = current.time + traversalTime;

                    if (newTime < distances.get(pathway.getToStopId())) {
                        distances.put(pathway.getToStopId(), newTime);
                        previousStop.put(pathway.getToStopId(), current.stopId);
                        pq.add(new Node(pathway.getToStopId(), newTime));

                        System.out.println("Using pathway from stop: " + current.stopId + " to stop: " + pathway.getToStopId() +
                                " with traversal time: " + traversalTime + " minutes.");
                    }
                }
            }

            // **Explore transfers between child and parent stop**
            Stop currentStop = stops.get(current.stopId);
            if (currentStop != null && currentStop.getParentStation().isPresent()) {
                String parentStopId = currentStop.getParentStation().get();
                int transferTime = 1;  // Assume 1 minute transfer time between parent and child stops
                int newTime = current.time + transferTime;

                if (newTime < distances.get(parentStopId)) {
                    distances.put(parentStopId, newTime);
                    previousStop.put(parentStopId, current.stopId);
                    pq.add(new Node(parentStopId, newTime));

                    System.out.println("Transferred from child stop: " + current.stopId + " to parent stop: " + parentStopId + " in " + transferTime + " minute.");
                }
            }

            // Check if we're at a parent stop and allow transfer to the child stops
            if (tripsByStop.containsKey(current.stopId)) {
                for (Stop childStop : stops.values()) {
                    if (childStop.getParentStation().isPresent() && childStop.getParentStation().get().equals(current.stopId)) {
                        int transferTime = 1;  // Assume 1 minute transfer time between parent and child stops
                        int newTime = current.time + transferTime;

                        if (newTime < distances.get(childStop.getStopId())) {
                            distances.put(childStop.getStopId(), newTime);
                            previousStop.put(childStop.getStopId(), current.stopId);
                            pq.add(new Node(childStop.getStopId(), newTime));

                            System.out.println("Transferred from parent stop: " + current.stopId + " to child stop: " + childStop.getStopId() + " in " + transferTime + " minute.");
                        }
                    }
                }
            }
        }

        // Reconstruct the path (if it exists)
        List<Stop> path = new ArrayList<>();
        for (String stopId = endStopId; stopId != null; stopId = previousStop.get(stopId)) {
            Stop stop = stops.get(stopId);
            if (stop == null) {
                System.out.println("Error: Stop not found for stopId: " + stopId);
                return Collections.emptyList();
            }
            path.add(stop);
        }

        Collections.reverse(path);
        if (!path.isEmpty() && path.get(0).getStopId().equals(startStopId)) {
            System.out.println("Path found successfully!");
        } else {
            System.out.println("No valid path found.");
        }
        return path.isEmpty() || !path.get(0).getStopId().equals(startStopId) ? Collections.emptyList() : path;
    }
}