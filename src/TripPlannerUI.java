import model.*;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;
import java.util.List;

// Uncomment this if using SwingX library
// import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;

public class TripPlannerUI extends JFrame {

    private JComboBox<Stop> startStopComboBox;
    private JComboBox<Stop> endStopComboBox;
    private JButton planTripButton;
    private JSpinner dateSpinner;
    private JSpinner timeSpinner;
    private TripPlanner tripPlanner;
    private JDialog loadingDialog;
    private List<Route> routes;
    private List<Trip> trips;


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

        // Initialize components
        // If using custom autocomplete
        startStopComboBox = new AutoCompleteComboBox(parentStations);
        endStopComboBox = new AutoCompleteComboBox(parentStations);

        // If using SwingX library
        // startStopComboBox = new JComboBox<>(parentStations.toArray(new model.Stop[0]));
        // endStopComboBox = new JComboBox<>(parentStations.toArray(new model.Stop[0]));
        // AutoCompleteDecorator.decorate(startStopComboBox);
        // AutoCompleteDecorator.decorate(endStopComboBox);

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
        planTripButton.addActionListener(e -> planTrip());
    }

    private void planTrip() {
        Stop startStop = (Stop) startStopComboBox.getSelectedItem();
        Stop endStop = (Stop) endStopComboBox.getSelectedItem();

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
        SwingWorker<List<TripPlanLeg>, Void> worker = new SwingWorker<List<TripPlanLeg>, Void>() {
            @Override
            protected List<TripPlanLeg> doInBackground() throws Exception {
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

    private void showLoadingDialog() {
        loadingDialog = new JDialog(this, "Loading", true); // Modal dialog
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel("Calculating the optimal path. Please wait...", JLabel.CENTER), BorderLayout.CENTER);
        loadingDialog.getContentPane().add(panel);
        loadingDialog.setSize(300, 100);
        loadingDialog.setLocationRelativeTo(this);
        loadingDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        loadingDialog.setResizable(false);
        loadingDialog.setVisible(true);
    }

    private void hideLoadingDialog() {
        if (loadingDialog != null) {
            loadingDialog.dispose();
            loadingDialog = null;
        }
    }

    // Main method to run the GUI
    public static void main(String[] args) throws Exception {
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