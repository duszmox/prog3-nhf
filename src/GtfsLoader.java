import java.io.BufferedReader;
import java.io.FileReader;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.opencsv.CSVReader;


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
                double stopLat = Double.parseDouble(line[2]); // stop_lat
                double stopLon = Double.parseDouble(line[3]); // stop_lon
                Optional<String> stopCode = Optional.ofNullable(line[4].isEmpty() ? null : line[4]); // stop_code
                Optional<Integer> locationType = Optional.ofNullable(line[5].isEmpty() ? null : Integer.parseInt(line[5])); // location_type
                Optional<String> parentStation = Optional.ofNullable(line[6].isEmpty() ? null : line[6]); // parent_station

                // Create Stop object and add to the list
                Stop stop = new Stop(stopId, stopName, stopLat, stopLon, stopCode, locationType, Optional.empty(), parentStation, Optional.empty());
                stops.add(stop);
            }
        }
        return stops;
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

                // Create Trip object and add to the list
                Trip trip = new Trip(routeId, tripId, serviceId, tripHeadsign, directionId, blockId, shapeId, wheelchairAccessible, bikesAllowed);
                trips.add(trip);
            }
        }
        return trips;
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
                Optional<LocalTime> arrivalTime = arrivalTimeStr.map(TimeHelper::parseGtfsTime).map(result -> result.time);

                // Handle departure_time (could be over 24:00:00)
                Optional<String> departureTimeStr = Optional.ofNullable(line[3].isEmpty() ? null : line[3]);
                Optional<LocalTime> departureTime = departureTimeStr.map(TimeHelper::parseGtfsTime).map(result -> result.time);

                int stopSequence = Integer.parseInt(line[4]);           // stop_sequence
                Optional<String> stopHeadsign = Optional.ofNullable(line[5].isEmpty() ? null : line[5]);
                Optional<Integer> pickupType = Optional.ofNullable(line[6].isEmpty() ? null : Integer.parseInt(line[6]));
                Optional<Integer> dropOffType = Optional.ofNullable(line[7].isEmpty() ? null : Integer.parseInt(line[7]));
                Optional<Double> shapeDistTraveled = Optional.ofNullable(line[8].isEmpty() ? null : Double.parseDouble(line[8]));

                // Create StopTime object and add to the list
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
                int pathwayMode = Integer.parseInt(line[1]);         // pathway_mode
                int isBidirectional = Integer.parseInt(line[2]);     // is_bidirectional
                String fromStopId = line[3];                         // from_stop_id
                String toStopId = line[4];                           // to_stop_id
                Optional<Integer> traversalTime = Optional.ofNullable(line[5].isEmpty() ? null : Integer.parseInt(line[5]));  // traversal_time

                // Create Pathway object and add to the list
                Pathway pathway = new Pathway(pathwayId, pathwayMode, isBidirectional, fromStopId, toStopId, traversalTime);
                pathways.add(pathway);
            }
        }
        return pathways;
    }

    public static List<Agency> loadAgencies(String filePath) throws Exception {
        List<Agency> agencies = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            String[] line;
            // Skip the header
            reader.readNext();
            while ((line = reader.readNext()) != null) {
                Optional<String> agencyId = Optional.ofNullable(line[0].isEmpty() ? null : line[0]);  // agency_id
                String agencyName = line[1];                          // agency_name
                String agencyUrl = line[2];                           // agency_url
                String agencyTimezone = line[3];                      // agency_timezone
                Optional<String> agencyLang = Optional.ofNullable(line[4].isEmpty() ? null : line[4]);  // agency_lang
                Optional<String> agencyPhone = Optional.ofNullable(line[5].isEmpty() ? null : line[5]);  // agency_phone

                // Create Agency object and add to the list
                Agency agency = new Agency(agencyId, agencyName, agencyUrl, agencyTimezone, agencyLang, agencyPhone);
                agencies.add(agency);
            }
        }
        return agencies;
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

                // Create Route object and add it to the list
                Route route = new Route(agencyId, routeId, routeShortName, routeLongName, routeType, routeDesc, routeColor, routeTextColor, routeSortOrder);
                routes.add(route);
            }
        }
        return routes;
    }
}