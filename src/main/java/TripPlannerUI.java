import model.*;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.io.*;
import java.net.URL;
import java.nio.file.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * A TripPlannerUI osztály egy grafikus felhasználói felületet biztosít az utazás tervezéséhez.
 */
public class TripPlannerUI extends JFrame {

    /**
     * A kezdő megálló kiválasztásához használt legördülő lista.
     */
    private final JComboBox<Stop> startStopComboBox;

    /**
     * A cél megálló kiválasztásához használt legördülő lista.
     */
    private final JComboBox<Stop> endStopComboBox;

    /**
     * Az utazás megtervezéséhez használt gomb.
     */
    private final JButton planTripButton;

    /**
     * A dátum kiválasztásához használt spinner.
     */
    private final JSpinner dateSpinner;

    /**
     * Az indulási idő kiválasztásához használt spinner.
     */
    private final JSpinner timeSpinner;

    /**
     * A TripPlanner példány, amely az útvonaltervezésért felelős.
     */
    private final TripPlanner tripPlanner;

    private final List<Route> routes;
    private final List<Trip> trips;

    /**
     * Konstruktor, amely inicializálja a felhasználói felületet és a szükséges adatokat.
     *
     * @param stops     A rendelkezésre álló megállók listája.
     * @param stopTimes A megállóidők listája.
     * @param pathways  Az aluljárók listája.
     * @param trips     A járatok listája.
     * @param routes    A vonalak listája.
     * @throws RuntimeException ha a feed_info nem beolvasható.
     */
    public TripPlannerUI(List<Stop> stops, List<StopTime> stopTimes, List<Pathway> pathways, List<Trip> trips, List<Route> routes) {
        // Csak a szülő állomásokat tartalmazó megállók szűrése
        List<Stop> parentStations = new ArrayList<>();
        for (Stop stop : stops) {
            if (stop.getParentStation().isEmpty()) {
                if (parentStations.stream().anyMatch(ps ->
                        ps.getStopName().equals(stop.getStopName()) && TripPlanner.haversine(ps.getStopLat(), ps.getStopLon(), stop.getStopLat(), stop.getStopLon()) < 200)) {
                    continue;
                }
                parentStations.add(stop);
            }
        }

        parentStations.sort(Comparator.comparing(Stop::getStopName, String.CASE_INSENSITIVE_ORDER));

        // TripPlanner inicializálása
        this.tripPlanner = new TripPlanner(stops, stopTimes, pathways, trips, routes);
        this.routes = routes;
        this.trips = trips;

        // Keret beállítása
        setTitle("GTFS Trip Planner");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        ImageIcon img = new ImageIcon("icon.png");
        setIconImage(img.getImage());

        // Komponensek inicializálása
        startStopComboBox = new AutoCompleteComboBox(parentStations);
        endStopComboBox = new AutoCompleteComboBox(parentStations);

        planTripButton = new JButton("Plan Trip");

        dateSpinner = new JSpinner(new SpinnerDateModel());
        timeSpinner = new JSpinner(new SpinnerDateModel());

        // Spinner-ek konfigurálása
        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd"));
        timeSpinner.setEditor(new JSpinner.DateEditor(timeSpinner, "HH:mm"));

        // Elrendezés beállítása
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

        panel.add(new JLabel()); // Üres cella
        panel.add(planTripButton);

        add(panel);

        planTripButton.addActionListener(_ -> {
            try {
                planTrip();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Betölti a GTFS adatcsomag `feed_info.txt` fájljából a feed kezdési és záró dátumát.
     *
     * @return Egy LocalDate tömb, amely tartalmazza a feed kezdési és záró dátumát.
     *         Az első elem a kezdési dátum (feed_start_date), a második a záró dátum (feed_end_date).
     * @throws IOException Ha a fájl nem érhető el, üres, vagy a formátum nem megfelelő.
     **/
    private static LocalDate[] loadFeedInfo() throws IOException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        try (BufferedReader reader = new BufferedReader(new FileReader("./budapest_gtfs/feed_info.txt"))) {
            reader.readLine(); // Fejléc kihagyása
            String line = reader.readLine();
            if (line != null) {
                String[] fields = line.split(",");
                LocalDate feedStartDate = LocalDate.parse(fields[4], formatter);
                LocalDate feedEndDate = LocalDate.parse(fields[5], formatter);
                return new LocalDate[]{feedStartDate, feedEndDate};
            } else {
                throw new IOException("Feed info file is empty.");
            }
        }
    }

    /**
     * Az utazás megtervezését végző metódus.
     */
    private void planTrip() throws IOException {
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

        // Dátum és idő lekérése a spinner-ekből
        Date selectedDate = (Date) dateSpinner.getValue();
        Date selectedTime = (Date) timeSpinner.getValue();

        LocalDate[] feedDates = loadFeedInfo();

        // Átalakítás LocalDate és LocalTime típusra
        LocalDate date = selectedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalTime departureTime = selectedTime.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();

        if (date.isBefore(feedDates[0]) || date.isAfter(feedDates[1])) {
            JOptionPane.showMessageDialog(this,
                    "Selected date is outside the feed validity period (" + feedDates[0] + " to " + feedDates[1] + ").",
                    "Invalid Date",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }
        // Gomb letiltása a többszöri kattintás megelőzésére
        planTripButton.setEnabled(false);

        // Betöltő ablak megjelenítése
        LoadingDialog loadingDialog = new LoadingDialog(this);
        SwingUtilities.invokeLater(() -> loadingDialog.setVisible(true));

        // Háttérben futó folyamat indítása
        SwingWorker<List<TripPlanLeg>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<TripPlanLeg> doInBackground() {
                // Utazástervező meghívása
                return tripPlanner.findOptimalPath(startStop.getStopId(), endStop.getStopId(), date, departureTime);
            }

            @Override
            protected void done() {
                try {
                    // Utazási terv lekérése
                    List<TripPlanLeg> tripPlan = get();

                    // Betöltő ablak elrejtése
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

        // A háttérfolyamat végrehajtása külön szálon
        worker.execute();
    }

    /**
     * GTFS adatok letöltése és kicsomagolása.
     *
     * @throws IOException Ha hiba történik a letöltés vagy kicsomagolás során.
     */
    public static void downloadAndExtractGtfsData() throws IOException {
        String url = "https://bkk.hu/gtfs/budapest_gtfs.zip";
        String zipFilePath = "./budapest_gtfs.zip";
        String destDir = "./budapest_gtfs/";

        if (!Files.exists(Paths.get(destDir))) {
            System.out.println("GTFS data not found. Downloading...");

            // Zip fájl letöltése
            Path target = Paths.get(zipFilePath);
            try (InputStream in = new URL(url).openStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }

            // Zip fájl kicsomagolása
            System.out.println("Extracting GTFS data...");
            unzip(zipFilePath, destDir);

            // Zip fájl törlése a kicsomagolás után
            Files.delete(target);
            System.out.println("GTFS data extracted successfully.");
        }
    }

    /**
     * Zip fájl kicsomagolása.
     *
     * @param zipFilePath A zip fájl elérési útja.
     * @param destDir     A célkönyvtár elérési útja.
     * @throws IOException Ha hiba történik a kicsomagolás során.
     */
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
                    // Könyvtárak létrehozása az adott bejegyzéshez
                    File parent = newFile.getParentFile();
                    if (!parent.isDirectory() && !parent.mkdirs()) {
                        throw new IOException("Failed to create directory " + parent);
                    }

                    // Fájl tartalmának írása
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

    /**
     * Új fájl létrehozása a célkönyvtárban.
     *
     * @param destinationDir A célkönyvtár.
     * @param zipEntry       A zip bejegyzés.
     * @return Az új fájl objektum.
     * @throws IOException Ha hiba történik a fájl létrehozása során.
     */
    private static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("A bejegyzés kívül esik a célkönyvtáron: " + zipEntry.getName());
        }

        return destFile;
    }

    /**
     * A fő metódus, amely elindítja a felhasználói felületet.
     *
     * @param args Parancssori argumentumok.
     * @throws Exception Ha hiba történik a GTFS adatok betöltése során.
     */
    public static void main(String[] args) throws Exception {
        downloadAndExtractGtfsData();
        // GTFS adatok betöltése
        String gtfsFolderPath = "./budapest_gtfs/";

        List<Stop> stops = GtfsLoader.loadStops(gtfsFolderPath + "stops.txt");
        List<StopTime> stopTimes = GtfsLoader.loadStopTimes(gtfsFolderPath + "stop_times.txt");
        List<Pathway> pathways = GtfsLoader.loadPathways(gtfsFolderPath + "pathways.txt");
        List<Trip> trips = GtfsLoader.loadTrips(gtfsFolderPath + "trips.txt");
        List<Route> routes = GtfsLoader.loadRoutes(gtfsFolderPath + "routes.txt");
        Map<String, List<LocalDate>> serviceDatesMap = GtfsLoader.loadCalendarDates(gtfsFolderPath + "calendar_dates.txt");

        // Szolgáltatási dátumok hozzárendelése az utazásokhoz
        for (Trip trip : trips) {
            List<LocalDate> serviceDates = serviceDatesMap.getOrDefault(trip.getServiceId(), new ArrayList<>());
            trip.setServiceDates(serviceDates);
        }

        // Felhasználói felület létrehozása és megjelenítése
        SwingUtilities.invokeLater(() -> {
            TripPlannerUI ui = new TripPlannerUI(stops, stopTimes, pathways, trips, routes);
            ui.setVisible(true);
        });
    }
}
