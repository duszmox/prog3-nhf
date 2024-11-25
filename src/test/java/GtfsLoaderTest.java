import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class GtfsLoaderTest {

    private File stopsFile;
    private File tripsFile;
    private File calendarDatesFile;
    private File stopTimesFile;
    private File pathwaysFile;
    private File routesFile;

    @BeforeEach
    public void setUp() throws Exception {
        stopsFile = createTemporaryFile("stop_id,stop_name,stop_lat,stop_lon,stop_code,location_type,location_sub_type,parent_station,wheelchair_boarding\n"
                + "ST1,Stop 1,47.500366,19.135700,001,,,");

        tripsFile = createTemporaryFile("route_id,trip_id,service_id,trip_headsign,direction_id,block_id,shape_id,wheelchair_accessible,bikes_allowed\n"
                + "RT1,TR1,SV1,Trip 1,0,,Shape1,1,1");

        calendarDatesFile = createTemporaryFile("service_id,date,exception_type\n"
                + "SV1,20241007,1\nSV1,20241008,1");

        stopTimesFile = createTemporaryFile("trip_id,stop_id,arrival_time,departure_time,stop_sequence,stop_headsign,pickup_type,drop_off_type,shape_dist_traveled\n"
                + "TR1,ST1,08:00:00,08:00:00,1,,0,0,0.0");

        pathwaysFile = createTemporaryFile("pathway_id,pathway_mode,is_bidirectional,from_stop_id,to_stop_id,traversal_time\n"
                + "P1,1,1,ST1,ST2,90");

        routesFile = createTemporaryFile("agency_id,route_id,route_short_name,route_long_name,route_type,route_desc,route_color,route_text_color,route_sort_order\n"
                + "AG1,RT1,Route 1,,3,,009EE3,FFFFFF,1");
    }

    @Test
    public void testLoadStops() throws Exception {
        List<Stop> stops = GtfsLoader.loadStops(stopsFile.getAbsolutePath());
        assertEquals(1, stops.size());
        assertEquals("ST1", stops.get(0).getStopId());
        assertEquals("Stop 1", stops.get(0).getStopName());
    }

    @Test
    public void testLoadTrips() throws Exception {
        List<Trip> trips = GtfsLoader.loadTrips(tripsFile.getAbsolutePath());
        assertEquals(1, trips.size());
        assertEquals("TR1", trips.get(0).getTripId());
        assertEquals("Trip 1", trips.get(0).getTripHeadsign().orElse(null));
    }

    @Test
    public void testLoadCalendarDates() throws Exception {
        Map<String, List<LocalDate>> calendarDates = GtfsLoader.loadCalendarDates(calendarDatesFile.getAbsolutePath());
        assertTrue(calendarDates.containsKey("SV1"));
        assertEquals(2, calendarDates.get("SV1").size());
        assertEquals(LocalDate.of(2024, 10, 7), calendarDates.get("SV1").get(0));
    }

    @Test
    public void testLoadStopTimes() throws Exception {
        List<StopTime> stopTimes = GtfsLoader.loadStopTimes(stopTimesFile.getAbsolutePath());
        assertEquals(1, stopTimes.size());
        assertEquals("TR1", stopTimes.get(0).getTripId());
        assertEquals("ST1", stopTimes.get(0).getStopId());
    }

    @Test
    public void testLoadPathways() throws Exception {
        List<Pathway> pathways = GtfsLoader.loadPathways(pathwaysFile.getAbsolutePath());
        assertEquals(1, pathways.size());
        assertEquals("P1", pathways.get(0).getPathwayId());
        assertEquals(1, pathways.get(0).getPathwayMode());
    }

    @Test
    public void testLoadRoutes() throws Exception {
        List<Route> routes = GtfsLoader.loadRoutes(routesFile.getAbsolutePath());
        assertEquals(1, routes.size());
        assertEquals("RT1", routes.get(0).getRouteId());
        assertEquals("Route 1", routes.get(0).getRouteShortName());
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
