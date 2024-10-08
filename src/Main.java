import java.util.*;

public class Main {
    public static void main(String[] args) throws Exception {
        // Print the current working directory
        System.out.println("Current directory: " + System.getProperty("user.dir"));

        // Define the path to the GTFS files
        String gtfsFolderPath = "./budapest_gtfs/";

        // Load GTFS data
        List<Stop> stops = GtfsLoader.loadStops(gtfsFolderPath + "stops.txt");
        List<StopTime> stopTimes = GtfsLoader.loadStopTimes(gtfsFolderPath + "stop_times.txt");
        List<Pathway> pathways = GtfsLoader.loadPathways(gtfsFolderPath + "pathways.txt");

        // Create the trip planner
        TripPlanner tripPlanner = new TripPlanner(stops, stopTimes, pathways);

        // Plan the trip
        List<Stop> path = tripPlanner.findShortestPath("F04181", "F04526", 480);

        // Print the itinerary
        for (Stop stop : path) {
            System.out.println("Stop: " + stop.getStopName());
        }
        System.out.println(path.size());
    }
}