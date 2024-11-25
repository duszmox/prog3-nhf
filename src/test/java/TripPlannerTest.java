import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class TripPlannerTest {

    private TripPlanner tripPlanner;
    private File stopsFile;
    private File tripsFile;
    private File calendarDatesFile;
    private File stopTimesFile;
    private File pathwaysFile;
    private File routesFile;

    @BeforeEach
    public void setUp() throws Exception {
        stopsFile = createTemporaryFile("stop_id,stop_name,stop_lat,stop_lon,stop_code,location_type,location_sub_type,parent_station,wheelchair_boarding\n"
                + "ST1,Stop 1,47.500366,19.135700,001,,, \n"
                + "ST2,Stop 2,47.501000,19.159000,002,,,");

        tripsFile = createTemporaryFile("route_id,trip_id,service_id,trip_headsign,direction_id,block_id,shape_id,wheelchair_accessible,bikes_allowed\n"
                + "RT1,TR1,SV1,Trip 1,0,,Shape1,1,1");

        calendarDatesFile = createTemporaryFile("service_id,date,exception_type\n"
                + "SV1,20241007,1\nSV1,20241008,1");

        stopTimesFile = createTemporaryFile("trip_id,stop_id,arrival_time,departure_time,stop_sequence,stop_headsign,pickup_type,drop_off_type,shape_dist_traveled\n"
                + "TR1,ST1,08:00:00,08:00:00,1,,0,0,0.0\n"
                + "TR1,ST2,08:01:00,08:10:00,2,,0,0,1.0");

        pathwaysFile = createTemporaryFile("pathway_id,pathway_mode,is_bidirectional,from_stop_id,to_stop_id,traversal_time\n"
                + "P1,1,1,ST1,ST2,1000");

        routesFile = createTemporaryFile("agency_id,route_id,route_short_name,route_long_name,route_type,route_desc,route_color,route_text_color,route_sort_order\n"
                + "AG1,RT1,Route 1,,3,,009EE3,FFFFFF,1");

        List<Stop> stops = GtfsLoader.loadStops(stopsFile.getAbsolutePath());
        List<Trip> trips = GtfsLoader.loadTrips(tripsFile.getAbsolutePath());
        List<StopTime> stopTimes = GtfsLoader.loadStopTimes(stopTimesFile.getAbsolutePath());
        List<Pathway> pathways = GtfsLoader.loadPathways(pathwaysFile.getAbsolutePath());
        List<Route> routes = GtfsLoader.loadRoutes(routesFile.getAbsolutePath());
        Map<String, List<LocalDate>> serviceDatesMap = GtfsLoader.loadCalendarDates(calendarDatesFile.getAbsolutePath());

        for (Trip trip : trips) {
            List<LocalDate> serviceDates = serviceDatesMap.getOrDefault(trip.getServiceId(), new ArrayList<>());
            trip.setServiceDates(serviceDates);
        }

        tripPlanner = new TripPlanner(stops, stopTimes, pathways, trips, routes);
    }

    @Test
    public void testFindOptimalPath() {
        LocalDate date = LocalDate.of(2024, 10, 7);
        LocalTime departureTime = LocalTime.of(7, 54);
        List<TripPlanLeg> tripPlan = tripPlanner.findOptimalPath("ST1", "ST2", date, departureTime);
        assertNotNull(tripPlan);
        assertFalse(tripPlan.isEmpty());
        assertEquals(2, tripPlan.size());

        TripPlanLeg leg = tripPlan.get(0);
        assertEquals(TripPlanLeg.LegType.WAIT, leg.getLegType());
        assertEquals("ST1", leg.getFromStop().getStopId());
        assertEquals("ST1", leg.getToStop().getStopId());
        assertNull(leg.getTripId());
        assertEquals(360, leg.getDuration());

        TripPlanLeg leg2 = tripPlan.get(1);
        assertEquals(TripPlanLeg.LegType.TRANSIT, leg2.getLegType());
        assertEquals("ST1", leg2.getFromStop().getStopId());
        assertEquals("ST2", leg2.getToStop().getStopId());
        assertEquals("TR1", leg2.getTripId());
        assertEquals(60, leg2.getDuration());
    }

    @Test
    public void testGetActiveTripIds() {
        LocalDate date = LocalDate.of(2024, 10, 7);
        Set<String> activeTripIds = tripPlanner.getActiveTripIds(date);
        assertNotNull(activeTripIds);
        assertEquals(1, activeTripIds.size());
        assertTrue(activeTripIds.contains("TR1"));
    }

    @Test
    public void testFilterStopTimes() {
        LocalTime departureTime = LocalTime.of(8, 0);
        List<StopTime> filteredStopTimes = tripPlanner.filterStopTimes(Set.of("TR1"), departureTime);
        assertNotNull(filteredStopTimes);
        assertEquals(2, filteredStopTimes.size());
    }

    @Test
    public void testBuildGraph() {
        List<StopTime> filteredStopTimes = tripPlanner.filterStopTimes(Set.of("TR1"), LocalTime.of(8, 0));
        Map<String, List<TripPlanner.Edge>> graph = tripPlanner.buildGraph(filteredStopTimes, "ST1", "ST2");
        assertNotNull(graph);
        assertTrue(graph.containsKey("ST1"));
        assertTrue(graph.containsKey("ST2"));
    }

    @Test
    public void testAddStopTimeEdges() {
        List<StopTime> filteredStopTimes = tripPlanner.filterStopTimes(Set.of("TR1"), LocalTime.of(8, 0));
        Map<String, List<TripPlanner.Edge>> graph = Collections.synchronizedMap(new HashMap<>());

        tripPlanner.stops.parallelStream().forEach(stop -> graph.put(stop.getStopId(), Collections.synchronizedList(new ArrayList<>())));
        TripPlanner.addStopTimeEdges(filteredStopTimes, graph);
        assertTrue(graph.containsKey("ST1"));
        assertFalse(graph.get("ST1").isEmpty());
    }

    @Test
    public void testAddPathWayEdges() {
        Map<String, List<TripPlanner.Edge>> graph = Collections.synchronizedMap(new HashMap<>());

        tripPlanner.stops.parallelStream().forEach(stop -> graph.put(stop.getStopId(), Collections.synchronizedList(new ArrayList<>())));
        tripPlanner.addPathWayEdges(graph);
        assertTrue(graph.containsKey("ST1"));
        assertTrue(graph.containsKey("ST2"));
        assertFalse(graph.get("ST1").isEmpty());
        assertEquals(1000, graph.get("ST1").getFirst().travelTime);
        assertEquals(1000, graph.get("ST2").getFirst().travelTime);
    }

    @Test
    public void testAddWalkEdges() {
        Map<String, List<TripPlanner.Edge>> graph = new HashMap<>();
        graph.put("ST1", new ArrayList<>());
        graph.put("ST2", new ArrayList<>());
        tripPlanner.addWalkEdges("ST1", "ST2", graph);
        assertTrue(graph.containsKey("ST1"));
        assertEquals(1, graph.get("ST1").size());
        assertEquals(1, graph.get("ST2").size());
        assertEquals(1261, graph.get("ST1").getFirst().travelTime);
        assertEquals(1261, graph.get("ST2").getFirst().travelTime);
    }

    private File createTemporaryFile(String content) throws Exception {
        File tempFile = Files.createTempFile("gtfs_test", ".csv").toFile();
        tempFile.deleteOnExit();
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write(content);
        }
        return tempFile;
    }
}
