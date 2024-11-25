import model.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * A TripPlanner osztály felelős az optimális útvonal megtalálásáért két megálló között.
 */
public class TripPlanner {

    final List<Stop> stops;
    final List<StopTime> stopTimes;
    final List<Pathway> pathways;
    final List<Trip> trips;
    final List<Route> routes;

    /**
     * Konstruktor, amely inicializálja az utazástervezőt a szükséges adatokkal.
     *
     * @param stops     A megállók listája.
     * @param stopTimes A megállóidők listája.
     * @param pathways  Az aluljárók listája.
     * @param trips     A járatok listája.
     * @param routes    A vonalak listája.
     */
    public TripPlanner(List<Stop> stops, List<StopTime> stopTimes, List<Pathway> pathways, List<Trip> trips, List<Route> routes) {
        this.stops = stops;
        this.stopTimes = stopTimes;
        this.pathways = pathways;
        this.trips = trips;
        this.routes = routes;
    }

    /**
     * Megkeresi az optimális útvonalat két megálló között adott dátumon és időben.
     *
     * @param startStopId   Az induló megálló azonosítója.
     * @param endStopId     Az érkező megálló azonosítója.
     * @param date          A dátum.
     * @param departureTime Az indulási idő.
     * @return Az utazási terv lépéseinek listája.
     */
    public List<TripPlanLeg> findOptimalPath(String startStopId, String endStopId, LocalDate date, LocalTime departureTime) {
        // 1. lépés: Az adott dátumon közlekedő járatok szűrése
        Set<String> activeTripIds = getActiveTripIds(date);

        // 2. lépés: A megállóidők szűrése egy időablakra
        List<StopTime> filteredStopTimes = filterStopTimes(activeTripIds, departureTime);

        // 3. lépés: A gráf felépítése
        Map<String, List<Edge>> graph = buildGraph(filteredStopTimes, startStopId, endStopId);

        // 4. lépés: A legrövidebb út algoritmus futtatása
        return shortestPath(graph, startStopId, endStopId, departureTime);
    }

    /**
     * Lekéri az aktív járatok azonosítóit adott dátumon.
     *
     * @param date A dátum.
     * @return Az aktív járatok azonosítóinak halmaza.
     */
    Set<String> getActiveTripIds(LocalDate date) {
        Set<String> activeTripIds = new HashSet<>();
        for (Trip trip : trips) {
            if (trip.getServiceDates().contains(date)) {
                activeTripIds.add(trip.getTripId());
            }
        }
        return activeTripIds;
    }

    /**
     * Szűri a megállóidőket egy adott időablakra.
     *
     * @param activeTripIds   Az aktív járatok azonosítói.
     * @param departureTime   Az indulási idő.
     * @return A szűrt megállóidők listája.
     */
    List<StopTime> filterStopTimes(Set<String> activeTripIds, LocalTime departureTime) {
        List<StopTime> filteredStopTimes = new ArrayList<>();
        LocalTime endTime = departureTime.plusHours(2); // 2 órás ablak

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

    /**
     * A gráf felépítése az adott megállóidőkből.
     *
     * @param filteredStopTimes A szűrt megállóidők.
     * @param startStopId       Az induló megálló azonosítója.
     * @param endStopId         Az érkező megálló azonosítója.
     * @return A gráf, ahol a csomópontok megállók és az élek a megállóidők, séta lehetőségek és aluljárók.
     */
    Map<String, List<Edge>> buildGraph(List<StopTime> filteredStopTimes, String startStopId, String endStopId) {

        Map<String, List<Edge>> graph = Collections.synchronizedMap(new HashMap<>());

        // Gráf csomópontjainak inicializálása
        stops.parallelStream().forEach(stop -> graph.put(stop.getStopId(), Collections.synchronizedList(new ArrayList<>())));

        addStopTimeEdges(filteredStopTimes, graph);

        addWalkEdges(startStopId, endStopId, graph);

        addPathWayEdges(graph);

        return graph;
    }

    /**
     * Élek hozzáadása a gráfhoz a megállóidőkből.
     *
     * @param filteredStopTimes A szűrt megállóidők.
     * @param graph             A gráf.
     */
    static void addStopTimeEdges(List<StopTime> filteredStopTimes, Map<String, List<Edge>> graph) {
        // Élek felépítése a megállóidőkből
        Map<String, List<StopTime>> stopTimesByTrip = new HashMap<>();
        filteredStopTimes.forEach(stopTime ->
                stopTimesByTrip.computeIfAbsent(stopTime.getTripId(), _ -> new ArrayList<>()).add(stopTime)
        );

        stopTimesByTrip.values().parallelStream().forEach(tripStopTimes -> {
            tripStopTimes.sort(Comparator.comparingInt(StopTime::getStopSequence));
            for (int i = 0; i < tripStopTimes.size() - 1; i++) {
                StopTime currentStopTime = tripStopTimes.get(i);
                StopTime nextStopTime = tripStopTimes.get(i + 1);

                if (currentStopTime.getDepartureTime().isPresent() && nextStopTime.getArrivalTime().isPresent()) {
                    String fromStopId = currentStopTime.getStopId();
                    String toStopId = nextStopTime.getStopId();

                    // Utazási idő kiszámítása másodpercekben
                    long travelTime = Duration.between(
                            currentStopTime.getDepartureTime().get(),
                            nextStopTime.getArrivalTime().get()
                    ).getSeconds();

                    // Él létrehozása
                    Edge edge = new Edge(toStopId, travelTime, EdgeType.TRANSIT, currentStopTime.getDepartureTime().get(), currentStopTime.getTripId());
                    graph.get(fromStopId).add(edge);
                }
            }
        });
    }

    /**
     * Útvonalak hozzáadása a gráfhoz az aluljáró adatokból.
     *
     * @param graph A gráf.
     */
    void addPathWayEdges(Map<String, List<Edge>> graph) {
        pathways.parallelStream().forEach(pathway -> {
            String fromStopId = pathway.getFromStopId();
            String toStopId = pathway.getToStopId();
            long traversalTime = pathway.getTraversalTime().orElse(0);

            // Él létrehozása
            Edge edge = new Edge(toStopId, traversalTime, EdgeType.PATHWAY, null, null);
            graph.get(fromStopId).add(edge);

            // Ha kétirányú, akkor a fordított él hozzáadása
            if (pathway.getIsBidirectional() == 1) {
                Edge reverseEdge = new Edge(fromStopId, traversalTime, EdgeType.PATHWAY, null, null);
                graph.get(toStopId).add(reverseEdge);
            }
        });
    }

    /**
     * Séta élek hozzáadása a gráfhoz a közeli megállók között.
     *
     * @param startStopId Az induló megálló azonosítója.
     * @param endStopId   Az érkező megálló azonosítója.
     * @param graph       A gráf.
     */
    void addWalkEdges(String startStopId, String endStopId, Map<String, List<Edge>> graph) {
        // Gyalogló élek létrehozása a 3000 méteren belüli megállók között
        Set<String> relevantStopIds = getRelevantStopIds(startStopId, endStopId);

        relevantStopIds.parallelStream().forEach(stopIdA -> {
            Stop stopA = getStopById(stopIdA);
            relevantStopIds.parallelStream().forEach(stopIdB -> {
                if (!stopIdA.equals(stopIdB)) {
                    Stop stopB = getStopById(stopIdB);
                    assert stopA != null;
                    assert stopB != null;
                    double distance = haversine(
                            stopA.getStopLat(), stopA.getStopLon(),
                            stopB.getStopLat(), stopB.getStopLon()
                    );
                    if (distance <= 3000) {
                        // Séta idő becslése (átlagos sebesség 5 km/h)
                        long walkingTime = (long) (((distance / 1000) / 5 * 3600));
                        Edge edge = new Edge(stopIdB, walkingTime, EdgeType.WALK, null, null);
                        graph.get(stopIdA).add(edge);
                    }
                }
            });
        });
    }

    /**
     * Releváns megállóazonosítók lekérése (megállók a kezdő és végállomás 3 kilométeres körzetében).
     *
     * @param startStopId Az induló megálló azonosítója.
     * @param endStopId   Az érkező megálló azonosítója.
     * @return A releváns megállóazonosítók halmaza.
     */
    private Set<String> getRelevantStopIds(String startStopId, String endStopId) {
        Set<String> relevantStopIds = ConcurrentHashMap.newKeySet();
        relevantStopIds.add(startStopId);
        relevantStopIds.add(endStopId);

        Stop startStop = getStopById(startStopId);
        Stop endStop = getStopById(endStopId);
        assert startStop != null;
        assert endStop != null;

        double distance = haversine(
                startStop.getStopLat(), startStop.getStopLon(),
                endStop.getStopLat(), endStop.getStopLon()
        );

        double centerLat = (startStop.getStopLat() + endStop.getStopLat()) / 2;
        double centerLon = (startStop.getStopLon() + endStop.getStopLon()) / 2;

        stops.parallelStream()
                .filter(stop -> !stop.getStopId().equals(startStopId) && !stop.getStopId().equals(endStopId))
                .forEach(stop -> {
                    double distanceFromCenter = haversine(centerLat, centerLon, stop.getStopLat(), stop.getStopLon()) - 1000;
                    if (distanceFromCenter <= distance) {
                        relevantStopIds.add(stop.getStopId());
                    }
                });

        return relevantStopIds;
    }

    /**
     * Egy megálló objektum lekérése azonosító alapján.
     *
     * @param stopId A megálló azonosítója.
     * @return A megálló objektum vagy null, ha nem található.
     */
    private Stop getStopById(String stopId) {
        for (Stop stop : stops) {
            if (stop.getStopId().equals(stopId)) {
                return stop;
            }
        }
        return null;
    }

    /**
     * Legrövidebb út algoritmus futtatása az útvonal megtalálásához.
     *
     * @param graph          A gráf.
     * @param startStopId    Az induló megálló azonosítója.
     * @param endStopId      Az érkező megálló azonosítója.
     * @param departureTime  Az indulási idő.
     * @return Az utazási terv lépéseinek listája.
     */
    private List<TripPlanLeg> shortestPath(Map<String, List<Edge>> graph, String startStopId, String endStopId, LocalTime departureTime) {
        PriorityQueue<NodeEntry> queue = new PriorityQueue<>(Comparator.comparingLong(ne -> ne.earliestArrivalTime));
        queue.add(new NodeEntry(startStopId, departureTime.toSecondOfDay(), null, 0, null, null, 0));

        Map<String, Long> earliestArrivalTimes = new HashMap<>();
        earliestArrivalTimes.put(startStopId, (long) departureTime.toSecondOfDay());

        Map<String, NodeEntry> previousNodes = new HashMap<>();

        while (!queue.isEmpty()) {
            NodeEntry current = queue.poll();
            String currentStopId = current.stopId;

            if (currentStopId.equals(endStopId)) {
                break;
            }

            for (Edge edge : graph.getOrDefault(currentStopId, new ArrayList<>())) {
                String neighborStopId = edge.toStopId;
                long arrivalTimeAtNeighbor;
                int transfers = current.transfers;
                String currentTripId = current.tripId;
                long waitTime = 0;

                if (edge.type == EdgeType.TRANSIT) {
                    if (edge.departureTime != null && edge.departureTime.toSecondOfDay() >= current.earliestArrivalTime) {
                        boolean sameTrip = currentTripId != null && currentTripId.equals(edge.tripId);
                        long waitTimeOn = edge.departureTime.toSecondOfDay() - current.earliestArrivalTime;

                        if (sameTrip) {
                            // Ugyanazon az járaton folytatás
                            arrivalTimeAtNeighbor = edge.departureTime.toSecondOfDay() + edge.travelTime;
                        } else {
                            // Átszállási időkorlátok betartása
                            if (waitTimeOn >= 60 && waitTimeOn <= 1200) { // 1 és 20 perc között
                                arrivalTimeAtNeighbor = edge.departureTime.toSecondOfDay() + edge.travelTime;
                                transfers += 1;
                                currentTripId = edge.tripId;
                                waitTime = waitTimeOn;
                            } else {
                                continue; // Nem lehet átszállni
                            }
                        }
                    } else {
                        continue; // Érvénytelen indulási idő
                    }
                } else {
                    // Séta és járat élek esetén
                    arrivalTimeAtNeighbor = current.earliestArrivalTime + edge.travelTime;
                    if (currentTripId != null) {
                        transfers += 1;
                        currentTripId = null;
                    }
                }

                if (arrivalTimeAtNeighbor < earliestArrivalTimes.getOrDefault(neighborStopId, Long.MAX_VALUE)) {
                    earliestArrivalTimes.put(neighborStopId, arrivalTimeAtNeighbor);
                    NodeEntry neighborEntry = new NodeEntry(neighborStopId, arrivalTimeAtNeighbor, current, transfers, currentTripId, edge, waitTime);
                    previousNodes.put(neighborStopId, neighborEntry);
                    queue.add(neighborEntry);
                }
            }
        }

        // Útvonal visszafejtése
        List<TripPlanLeg> tripPlan = new ArrayList<>();
        NodeEntry currentNode = previousNodes.get(endStopId);

        if (currentNode == null) {
            System.out.println("Nem található elérhető útvonal.");
            return new ArrayList<>();
        }

        // Útvonal visszafelé történő összeállítása
        List<NodeEntry> pathNodes = new ArrayList<>();
        while (currentNode.previousNode != null) {
            pathNodes.add(currentNode);
            currentNode = currentNode.previousNode;
        }
        Collections.reverse(pathNodes);

        buildTripPlanFromPath(pathNodes, tripPlan);

        return tripPlan;
    }

    /**
     * Segédfüggvény az utazási terv összeállításához a csomópontokból.
     *
     * @param pathNodes Az útvonal csomópontjai.
     * @param tripPlan  Az utazási terv, amelybe a lépéseket hozzáadjuk.
     */
    private void buildTripPlanFromPath(List<NodeEntry> pathNodes, List<TripPlanLeg> tripPlan) {
        Map<String, Stop> stopMap = stops.parallelStream().collect(Collectors.toConcurrentMap(Stop::getStopId, stop -> stop));
        Map<String, Trip> tripMap = trips.parallelStream().collect(Collectors.toConcurrentMap(Trip::getTripId, trip -> trip));
        Map<String, Route> routeMap = routes.parallelStream().collect(Collectors.toConcurrentMap(Route::getRouteId, route -> route));

        String previousTripId = null;
        TripPlanLeg previousTripPlanLeg = null;

        for (NodeEntry node : pathNodes) {
            NodeEntry prevNode = node.previousNode;
            Edge edge = node.edge;

            Stop fromStop = stopMap.get(prevNode.stopId);
            Stop toStop = stopMap.get(node.stopId);

            LocalTime startTime = LocalTime.ofSecondOfDay(prevNode.earliestArrivalTime % 86400);
            LocalTime endTime = LocalTime.ofSecondOfDay(node.earliestArrivalTime % 86400);

            TripPlanLeg.LegType legType;
            String tripId = edge.tripId;
            String routeId = null;
            String routeShortName = null;
            String routeLongName = null;
            double distance = 0.0;

            if (edge.type == EdgeType.TRANSIT) {
                legType = TripPlanLeg.LegType.TRANSIT;
                Trip trip = tripMap.get(tripId);
                if (trip != null) {
                    routeId = trip.getRouteId();
                    Route route = routeMap.get(routeId);
                    if (route != null) {
                        routeShortName = route.getRouteShortName();
                        routeLongName = route.getRouteLongName().orElse("");
                    }
                }
            } else if (edge.type == EdgeType.WALK || edge.type == EdgeType.PATHWAY) {
                legType = TripPlanLeg.LegType.WALK;
                if (previousTripPlanLeg != null && previousTripPlanLeg.getLegType() == legType) {
                    distance = haversine(
                            previousTripPlanLeg.getFromStop().getStopLat(), previousTripPlanLeg.getFromStop().getStopLon(),
                            toStop.getStopLat(), toStop.getStopLon()
                    );
                    previousTripPlanLeg.setDistance(distance);
                    previousTripPlanLeg.setEndTime(endTime);
                    previousTripPlanLeg.setDuration(previousTripPlanLeg.getDuration() + edge.travelTime);
                    previousTripPlanLeg.setToStop(toStop);
                    continue;
                } else {
                    distance = haversine(
                            fromStop.getStopLat(), fromStop.getStopLon(),
                            toStop.getStopLat(), toStop.getStopLon()
                    );
                }
            } else {
                legType = TripPlanLeg.LegType.WALK; //Backupnak itt a WALK
            }

            if (node.waitTimeBefore > 0) {
                TripPlanLeg transferLeg = new TripPlanLeg(
                        previousTripId != null && tripId != null && !previousTripId.equals(tripId) ? TripPlanLeg.LegType.TRANSFER : TripPlanLeg.LegType.WAIT,
                        fromStop,
                        startTime,
                        endTime.minusSeconds(node.waitTimeBefore),
                        node.waitTimeBefore
                );
                tripPlan.add(transferLeg);
            }

            TripPlanLeg currentTripLeg = new TripPlanLeg(
                    legType,
                    fromStop,
                    toStop,
                    startTime.plusSeconds(node.waitTimeBefore),
                    endTime,
                    tripId,
                    routeId,
                    routeShortName,
                    routeLongName,
                    distance,
                    edge.travelTime
            );
            tripPlan.add(currentTripLeg);

            previousTripPlanLeg = currentTripLeg;
            previousTripId = tripId;
        }
    }

    /**
     * Az él osztály a megállók közötti kapcsolatokat reprezentálja.
     */
    static class Edge {
        String toStopId;
        long travelTime; // másodpercekben
        EdgeType type;
        LocalTime departureTime; // Csak a menetrend szerinti élekhez
        String tripId; // Csak a menetrend szerinti élekhez

        /**
         * Él konstruktor.
         *
         * @param toStopId      Cél megálló azonosítója.
         * @param travelTime    Utazási idő másodpercekben.
         * @param type          Az él típusa.
         * @param departureTime Indulási idő (csak menetrend szerinti élekhez).
         * @param tripId        Utazás azonosítója (csak menetrend szerinti élekhez).
         */
        Edge(String toStopId, long travelTime, EdgeType type, LocalTime departureTime, String tripId) {
            this.toStopId = toStopId;
            this.travelTime = travelTime;
            this.type = type;
            this.departureTime = departureTime;
            this.tripId = tripId;
        }
    }

    /**
     * A csomópont bejegyzés az algoritmusban.
     */
    private static class NodeEntry {
        String stopId;
        long earliestArrivalTime;
        NodeEntry previousNode;
        int transfers;
        String tripId;
        Edge edge;
        long waitTimeBefore;

        /**
         * Csomópont bejegyzés konstruktor.
         *
         * @param stopId             A megálló azonosítója.
         * @param earliestArrivalTime A legkorábbi érkezési idő.
         * @param previousNode       Előző csomópont.
         * @param transfers          Átszállások száma.
         * @param tripId             Utazás azonosítója.
         * @param edge               Él objektum.
         * @param waitTimeBefore     Várakozási idő az él előtt.
         */
        NodeEntry(String stopId, long earliestArrivalTime, NodeEntry previousNode, int transfers, String tripId, Edge edge, long waitTimeBefore) {
            this.stopId = stopId;
            this.earliestArrivalTime = earliestArrivalTime;
            this.previousNode = previousNode;
            this.transfers = transfers;
            this.tripId = tripId;
            this.edge = edge;
            this.waitTimeBefore = waitTimeBefore;
        }
    }

    /**
     * Haversine formula a két pont közötti távolság kiszámításához.
     *
     * @param lat1 Első pont szélessége.
     * @param lon1 Első pont hosszúsága.
     * @param lat2 Második pont szélessége.
     * @param lon2 Második pont hosszúsága.
     * @return A távolság méterben.
     */
    public static double haversine(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // A Föld sugara kilométerben
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c * 1000; // Átváltás méterre
    }
}
