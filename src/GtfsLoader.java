import java.io.FileReader;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import com.opencsv.CSVReader;
import model.*;


public class GtfsLoader {

    public static List<Stop> loadStops(String filePath) throws Exception {
        List<Stop> stops = new ArrayList<>();

        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            String[] line;
            // Skip the header line
            reader.readNext();

            while ((line = reader.readNext()) != null) {
                String stopId = line[0];                      // stop_id
                String stopName = line[1];                    // stop_name (may contain commas, properly handled by OpenCSV)
                Stop stop = getStop(line, stopId, stopName);
                stops.add(stop);
            }
        }
        return stops;
    }

    private static Stop getStop(String[] line, String stopId, String stopName) {
        double stopLat = Double.parseDouble(line[2]); // stop_lat
        double stopLon = Double.parseDouble(line[3]); // stop_lon
        Optional<String> stopCode = Optional.ofNullable(line[4].isEmpty() ? null : line[4]); // stop_code
        Optional<Integer> locationType = Optional.ofNullable(line[5].isEmpty() ? null : Integer.parseInt(line[5])); // location_type
        Optional<String> parentStation = Optional.ofNullable(line[7].isEmpty() ? null : line[7]); // parent_station

        // Create model.Stop object and add to the list
        return new Stop(stopId, stopName, stopLat, stopLon, stopCode, locationType, Optional.empty(), parentStation, Optional.empty());
    }

    public static List<Trip> loadTrips(String filePath) throws Exception {
        List<Trip> trips = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            String[] line;
            // Skip the header
            reader.readNext();
            while ((line = reader.readNext()) != null) {
                String routeId = line[0];                          // route_id
                String tripId = line[1];                           // trip_id
                String serviceId = line[2];                        // service_id
                Optional<String> tripHeadsign = Optional.ofNullable(line[3].isEmpty() ? null : line[3]);  // trip_headsign
                Optional<Integer> directionId = Optional.ofNullable(line[4].isEmpty() ? null : Integer.parseInt(line[4]));  // direction_id
                Optional<String> blockId = Optional.ofNullable(line[5].isEmpty() ? null : line[5]);  // block_id
                Optional<String> shapeId = Optional.ofNullable(line[6].isEmpty() ? null : line[6]);  // shape_id
                Optional<Integer> wheelchairAccessible = Optional.ofNullable(line[7].isEmpty() ? null : Integer.parseInt(line[7]));  // wheelchair_accessible
                Optional<Integer> bikesAllowed = Optional.ofNullable(line[8].isEmpty() ? null : Integer.parseInt(line[8]));  // bikes_allowed

                // Create model.Trip object and add to the list
                Trip trip = new Trip(routeId, tripId, serviceId, tripHeadsign, directionId, blockId, shapeId, wheelchairAccessible, bikesAllowed);
                trips.add(trip);
            }
        }
        return trips;
    }
    public static Map<String, List<LocalDate>> loadCalendarDates(String filePath) throws Exception {
        Map<String, List<LocalDate>> serviceDatesMap = new HashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            String[] line;
            // Skip the header line
            reader.readNext();

            while ((line = reader.readNext()) != null) {
                String serviceId = line[0];                    // service_id
                LocalDate date = LocalDate.parse(line[1], formatter); // date (yyyyMMdd)
                int exceptionType = Integer.parseInt(line[2]); // exception_type (1 = added, 2 = removed)

                // Only consider added service dates
                if (exceptionType == 1) {
                    serviceDatesMap.computeIfAbsent(serviceId, _ -> new ArrayList<>()).add(date);
                }
            }
        }
        return serviceDatesMap;
    }

    public static List<StopTime> loadStopTimes(String filePath) throws Exception {
        List<StopTime> stopTimes = new ArrayList<>();

        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            String[] line;
            // Skip the header
            reader.readNext();

            while ((line = reader.readNext()) != null) {
                String tripId = line[0];                               // trip_id
                String stopId = line[1];                               // stop_id

                // Handle arrival_time (could be over 24:00:00)
                Optional<String> arrivalTimeStr = Optional.ofNullable(line[2].isEmpty() ? null : line[2]);
                Optional<LocalTime> arrivalTime = arrivalTimeStr.map(TimeHelper::parseGtfsTime).map(result -> result.time());

                // Handle departure_time (could be over 24:00:00)
                Optional<String> departureTimeStr = Optional.ofNullable(line[3].isEmpty() ? null : line[3]);
                Optional<LocalTime> departureTime = departureTimeStr.map(TimeHelper::parseGtfsTime).map(result -> result.time());

                int stopSequence = Integer.parseInt(line[4]);           // stop_sequence
                Optional<String> stopHeadsign = Optional.ofNullable(line[5].isEmpty() ? null : line[5]);
                Optional<Integer> pickupType = Optional.ofNullable(line[6].isEmpty() ? null : Integer.parseInt(line[6]));
                Optional<Integer> dropOffType = Optional.ofNullable(line[7].isEmpty() ? null : Integer.parseInt(line[7]));
                Optional<Double> shapeDistTraveled = Optional.ofNullable(line[8].isEmpty() ? null : Double.parseDouble(line[8]));

                // Create model.StopTime object and add to the list
                StopTime stopTime = new StopTime(tripId, stopId, arrivalTime, departureTime, stopSequence, stopHeadsign, pickupType, dropOffType, shapeDistTraveled);
                stopTimes.add(stopTime);
            }
        }
        return stopTimes;
    }


    public static List<Pathway> loadPathways(String filePath) throws Exception {
        List<Pathway> pathways = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            String[] line;
            // Skip the header
            reader.readNext();
            while ((line = reader.readNext()) != null) {
                String pathwayId = line[0];                          // pathway_id
                Pathway pathway = getPathway(line, pathwayId);
                pathways.add(pathway);
            }
        }
        return pathways;
    }

    private static Pathway getPathway(String[] line, String pathwayId) {
        int pathwayMode = Integer.parseInt(line[1]);         // pathway_mode
        int isBidirectional = Integer.parseInt(line[2]);     // is_bidirectional
        String fromStopId = line[3];                         // from_stop_id
        String toStopId = line[4];                           // to_stop_id
        Optional<Integer> traversalTime = Optional.ofNullable(line[5].isEmpty() ? null : Integer.parseInt(line[5]));  // traversal_time

        // Create model.Pathway object and add to the list
        return new Pathway(pathwayId, pathwayMode, isBidirectional, fromStopId, toStopId, traversalTime);
    }

    public static List<Route> loadRoutes(String filePath) throws Exception {
        List<Route> routes = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            String[] line;
            // Skip header line
            reader.readNext();
            while ((line = reader.readNext()) != null) {
                Optional<String> agencyId = Optional.ofNullable(line[0].isEmpty() ? null : line[0]);  // agency_id
                String routeId = line[1];                          // route_id
                String routeShortName = line[2];                   // route_short_name
                Optional<String> routeLongName = Optional.ofNullable(line[3].isEmpty() ? null : line[3]);  // route_long_name
                int routeType = Integer.parseInt(line[4]);          // route_type
                Optional<String> routeDesc = Optional.ofNullable(line[5].isEmpty() ? null : line[5]);  // route_desc
                Optional<String> routeColor = Optional.ofNullable(line[6].isEmpty() ? null : line[6]);  // route_color
                Optional<String> routeTextColor = Optional.ofNullable(line[7].isEmpty() ? null : line[7]);  // route_text_color
                Optional<Integer> routeSortOrder = Optional.ofNullable(line[8].isEmpty() ? null : Integer.parseInt(line[8]));  // route_sort_order

                // Create model.Route object and add it to the list
                Route route = new Route(agencyId, routeId, routeShortName, routeLongName, routeType, routeDesc, routeColor, routeTextColor, routeSortOrder);
                routes.add(route);
            }
        }
        return routes;
    }
}