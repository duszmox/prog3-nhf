import model.*;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;
import java.util.List;
import java.io.*;
import java.net.URL;
import java.nio.file.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


public class TripPlannerUI extends JFrame {

    private final JComboBox<Stop> startStopComboBox;
    private final JComboBox<Stop> endStopComboBox;
    private final JButton planTripButton;
    private final JSpinner dateSpinner;
    private final JSpinner timeSpinner;
    private final TripPlanner tripPlanner;
    private final List<Route> routes;
    private final List<Trip> trips;


    public TripPlannerUI(List<Stop> stops, List<StopTime> stopTimes, List<Pathway> pathways, List<Trip> trips, List<Route> routes) {
        // Filter stops to include only parent stations
        List<Stop> parentStations = new ArrayList<>();
        for (Stop stop : stops) {
            if (stop.getParentStation().isEmpty()) {
                parentStations.add(stop);
            }
        }

        parentStations.sort(Comparator.comparing(Stop::getStopName, String.CASE_INSENSITIVE_ORDER));

        // Initialize TripPlanner
        this.tripPlanner = new TripPlanner(stops, stopTimes, pathways, trips, routes);
        this.routes = routes;
        this.trips = trips;
        // Set up the frame
        setTitle("GTFS Trip Planner");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        ImageIcon img = new ImageIcon("icon.png");
        setIconImage(img.getImage());

        // Initialize components
        // If using custom autocomplete
        startStopComboBox = new AutoCompleteComboBox(parentStations);
        endStopComboBox = new AutoCompleteComboBox(parentStations);

        planTripButton = new JButton("Plan Trip");

        dateSpinner = new JSpinner(new SpinnerDateModel());
        timeSpinner = new JSpinner(new SpinnerDateModel());

        // Configure the spinners
        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd"));
        timeSpinner.setEditor(new JSpinner.DateEditor(timeSpinner, "HH:mm"));

        // Set up layout
        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        panel.add(new JLabel("Start Station:"));
        panel.add(startStopComboBox);

        panel.add(new JLabel("End Station:"));
        panel.add(endStopComboBox);

        panel.add(new JLabel("Date:"));
        panel.add(dateSpinner);

        panel.add(new JLabel("Departure Time:"));
        panel.add(timeSpinner);

        panel.add(new JLabel()); // Empty cell
        panel.add(planTripButton);

        add(panel);

        // Add action listener to the button
        planTripButton.addActionListener(_ -> planTrip());
    }

    private void planTrip() {
        final Stop startStop;
        final Stop endStop;
        try {
            startStop = (Stop) startStopComboBox.getSelectedItem();
            endStop = (Stop) endStopComboBox.getSelectedItem();
        } catch (ClassCastException e) {
            JOptionPane.showMessageDialog(this, "Please select both start and end stations.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (startStop == null || endStop == null) {
            JOptionPane.showMessageDialog(this, "Please select both start and end stations.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Retrieve the date and time from the spinners
        Date selectedDate = (Date) dateSpinner.getValue();
        Date selectedTime = (Date) timeSpinner.getValue();

        // Convert to LocalDate and LocalTime
        LocalDate date = selectedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalTime departureTime = selectedTime.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();

        // Disable the button to prevent multiple clicks
        planTripButton.setEnabled(false);

        // Create and show the loading dialog
        LoadingDialog loadingDialog = new LoadingDialog(this);
        SwingUtilities.invokeLater(() -> loadingDialog.setVisible(true));

        // Use SwingWorker to run in the background
        SwingWorker<List<TripPlanLeg>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<TripPlanLeg> doInBackground() {
                // Call the TripPlanner
                return tripPlanner.findOptimalPath(startStop.getStopId(), endStop.getStopId(), date, departureTime);
            }

            @Override
            protected void done() {
                try {
                    // Get the trip plan
                    List<TripPlanLeg> tripPlan = get();

                    // Hide the loading dialog
                    loadingDialog.dispose();

                    if (tripPlan.isEmpty()) {
                        JOptionPane.showMessageDialog(TripPlannerUI.this, "No available path found.", "Info", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        Map<String, Route> routeMap = new HashMap<>();
                        for (Route route : routes) {
                            routeMap.put(route.getRouteId(), route);
                        }
                        Map<String, Trip> tripMap = new HashMap<>();
                        for (Trip trip : trips) {
                            tripMap.put(trip.getTripId(), trip);
                        }
                        TransitItineraryWithLines itineraryView = new TransitItineraryWithLines(tripPlan, routeMap, tripMap);
                        itineraryView.setVisible(true);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(TripPlannerUI.this, "An error occurred while planning the trip.", "Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    planTripButton.setEnabled(true);
                }
            }
        };

        // Execute the worker in a separate thread
        worker.execute();
    }


    public static void downloadAndExtractGtfsData() throws IOException {
        String url = "https://bkk.hu/gtfs/budapest_gtfs.zip";
        String zipFilePath = "./budapest_gtfs.zip";
        String destDir = "./budapest_gtfs/";

        if (!Files.exists(Paths.get(destDir))) {
            System.out.println("GTFS data not found. Downloading...");

            // Download the zip file
            Path target = Paths.get(zipFilePath);
            try (InputStream in = new URL(url).openStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }

            // Extract the zip file
            System.out.println("Extracting GTFS data...");
            unzip(zipFilePath, destDir);

            // Delete the zip file after extraction
            Files.delete(target);
            System.out.println("GTFS data extracted successfully.");
        }
    }

    private static void unzip(String zipFilePath, String destDir) throws IOException {
        File dir = new File(destDir);
        if (!dir.exists()) dir.mkdirs();

        byte[] buffer = new byte[1024];
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFilePath))) {
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                File newFile = newFile(dir, zipEntry);
                if (zipEntry.isDirectory()) {
                    if (!newFile.isDirectory() && !newFile.mkdirs()) {
                        throw new IOException("Failed to create directory " + newFile);
                    }
                } else {
                    // Create directories for entry
                    File parent = newFile.getParentFile();
                    if (!parent.isDirectory() && !parent.mkdirs()) {
                        throw new IOException("Failed to create directory " + parent);
                    }

                    // Write file content
                    try (FileOutputStream fos = new FileOutputStream(newFile)) {
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
                zipEntry = zis.getNextEntry();
            }
            zis.closeEntry();
        }
    }

    private static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }

    // Main method to run the GUI
    public static void main(String[] args) throws Exception {
        downloadAndExtractGtfsData();
        // Load GTFS data (similar to your existing code)
        String gtfsFolderPath = "./budapest_gtfs/";

        List<Stop> stops = GtfsLoader.loadStops(gtfsFolderPath + "stops.txt");
        List<StopTime> stopTimes = GtfsLoader.loadStopTimes(gtfsFolderPath + "stop_times.txt");
        List<Pathway> pathways = GtfsLoader.loadPathways(gtfsFolderPath + "pathways.txt");
        List<Trip> trips = GtfsLoader.loadTrips(gtfsFolderPath + "trips.txt");
        List<Route> routes = GtfsLoader.loadRoutes(gtfsFolderPath + "routes.txt");
        Map<String, List<LocalDate>> serviceDatesMap = GtfsLoader.loadCalendarDates(gtfsFolderPath + "calendar_dates.txt");

        // Link service dates with trips
        for (Trip trip : trips) {
            List<LocalDate> serviceDates = serviceDatesMap.getOrDefault(trip.getServiceId(), new ArrayList<>());
            trip.setServiceDates(serviceDates);
        }

        // Create and show the GUI
        SwingUtilities.invokeLater(() -> {
            TripPlannerUI ui = new TripPlannerUI(stops, stopTimes, pathways, trips, routes);
            ui.setVisible(true);
        });
    }
}