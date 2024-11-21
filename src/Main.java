import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws Exception {
        // Load GTFS data (as in your existing code)
        String gtfsFolderPath = "./budapest_gtfs/";

        List<Stop> stops = GtfsLoader.loadStops(gtfsFolderPath + "stops.txt");
        List<StopTime> stopTimes = GtfsLoader.loadStopTimes(gtfsFolderPath + "stop_times.txt");
        List<Pathway> pathways = GtfsLoader.loadPathways(gtfsFolderPath + "pathways.txt");
        List<Trip> trips = GtfsLoader.loadTrips(gtfsFolderPath + "trips.txt");
        Map<String, List<LocalDate>> serviceDatesMap = GtfsLoader.loadCalendarDates(gtfsFolderPath + "calendar_dates.txt");

        // Link service dates with trips
        for (Trip trip : trips) {
            List<LocalDate> serviceDates = serviceDatesMap.getOrDefault(trip.getServiceId(), new ArrayList<>());
            trip.setServiceDates(serviceDates);
        }

        // Create a TripPlanner instance
        TripPlanner tripPlanner = new TripPlanner(stops, stopTimes, pathways, trips);

        // Define start and end stops, date, and departure time
        String startStopId = "CS056233"; // Replace with actual stop ID
        String endStopId = "CSF01232";     // Replace with actual stop ID
        LocalDate date = LocalDate.of(2024, 11, 30); // Example date
        LocalTime departureTime = LocalTime.of(8, 0); // 8:00 AM

        // Find the optimal path
        List<Stop> path = tripPlanner.findOptimalPath(startStopId, endStopId, date, departureTime);

        // Print the itinerary
        for (Stop stop : path) {
            System.out.println("Stop: " + stop.getStopName() + " " + stop.getParentStation() + " " + stop.getStopId());
        }
        System.out.println("Total stops: " + path.size());
    }
}
