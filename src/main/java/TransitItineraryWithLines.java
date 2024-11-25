import javax.swing.*;
import java.awt.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import model.Route;
import model.Trip;

/**
 * A TransitItineraryWithLines osztály megjeleníti az utazási tervet grafikus felületen, vonalakkal és részletekkel.
 */
public class TransitItineraryWithLines extends JFrame {
    private final Map<String, Route> routeMap;
    private List<TripPlanLeg> tripPlan;
    private final Map<String, Trip> tripMap;
    private final List<TripPlanLeg> originalTripPlan;
    List<Integer> numberOfStops = new ArrayList<>();

    private boolean expanded = false;

    /**
     * Konstruktor, amely inicializálja az ablakot és megjeleníti az utazási tervet.
     *
     * @param tripPlan Az utazási terv lépései.
     * @param routeMap Útvonalak map-je azonosító alapján.
     * @param tripMap  Utazások map-je azonosító alapján.
     */
    public TransitItineraryWithLines(List<TripPlanLeg> tripPlan, Map<String, Route> routeMap, Map<String, Trip> tripMap) {
        this.originalTripPlan = new ArrayList<>(tripPlan); // Eredeti lista másolása
        this.routeMap = routeMap;
        this.tripPlan = new ArrayList<>(tripPlan); // Módosítható lista
        this.tripMap = tripMap;

        setTitle("Trip plan");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(500, 600);
        setLocationRelativeTo(null);

        ImageIcon img = new ImageIcon("icon.png");
        setIconImage(img.getImage());

        // Menü sáv beállítása
        setUpMenuBar();

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(Color.WHITE);

        renderComponents(mainPanel);

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(scrollPane);

        setVisible(true);
    }

    /**
     * Menü sáv inicializálása.
     */
    private void setUpMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");

        JMenuItem backMenuItem = new JMenuItem("Back");
        backMenuItem.addActionListener(_ -> dispose()); // Ablak bezárása
        fileMenu.add(backMenuItem);

        JMenuItem exportMenuItem = new JMenuItem("Export to TXT");
        exportMenuItem.addActionListener(_ -> exportTripPlanToTxt());
        fileMenu.add(exportMenuItem);

        menuBar.add(fileMenu);
        setJMenuBar(menuBar);
    }

    /**
     * A komponensek megjelenítése a fő panelen.
     *
     * @param mainPanel A fő panel.
     */
    private void renderComponents(JPanel mainPanel) {
        // Vissza gomb panel a tetején
        JPanel backButtonPanelTop = new JPanel();
        backButtonPanelTop.setLayout(new BoxLayout(backButtonPanelTop, BoxLayout.X_AXIS));
        backButtonPanelTop.setBackground(Color.WHITE);
        backButtonPanelTop.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        // Vissza gomb hozzáadása
        JButton backButtonTop = new JButton("Back");
        backButtonTop.setAlignmentX(Component.LEFT_ALIGNMENT);
        backButtonTop.addActionListener(_ -> {
            dispose(); // Ablak bezárása
        });
        backButtonPanelTop.add(Box.createHorizontalGlue());
        backButtonPanelTop.add(backButtonTop);

        JButton expandButtonTop = new JButton(!expanded ? "Expand" : "Collapse");
        expandButtonTop.setAlignmentX(Component.CENTER_ALIGNMENT);
        expandButtonTop.addActionListener(_ -> {
            expanded = !expanded;
            mainPanel.removeAll();
            renderComponents(mainPanel);
            mainPanel.revalidate();
        });
        backButtonPanelTop.add(Box.createHorizontalGlue());
        backButtonPanelTop.add(expandButtonTop);

        // Export gomb hozzáadása
        JButton exportButton = new JButton("Export to TXT");
        exportButton.setAlignmentX(Component.RIGHT_ALIGNMENT);
        exportButton.addActionListener(_ -> exportTripPlanToTxt());
        backButtonPanelTop.add(Box.createHorizontalGlue());
        backButtonPanelTop.add(exportButton);

        mainPanel.add(backButtonPanelTop);

        // Fix szélességek beállítása
        int timeWidth = 60;
        int lineWidth = 30;
        int spacerWidth = 10;

        List<TripPlanLeg> filteredTripPlanLegs = new ArrayList<>();
        numberOfStops.clear();

        if (!expanded) {
            // Utazási terv resetelése az eredeti listára
            tripPlan = new ArrayList<>(originalTripPlan);

            // Szűrés és időtartamok kombinálása
            for (TripPlanLeg tripPlanLeg : tripPlan) {
                TripPlanLeg prevLeg = null;
                Integer numberOfStop = 0;

                if (!filteredTripPlanLegs.isEmpty()) {
                    prevLeg = filteredTripPlanLegs.get(filteredTripPlanLegs.size() - 1);
                    numberOfStop = numberOfStops.get(numberOfStops.size() - 1);
                }

                boolean hidable = prevLeg != null && Objects.equals(prevLeg.getTripId(), tripPlanLeg.getTripId());
                if (hidable) {
                    // Időtartamok és távolságok kombinálása
                    TripPlanLeg combinedLeg = new TripPlanLeg(prevLeg);
                    combinedLeg.setDuration(prevLeg.getDuration() + tripPlanLeg.getDuration());
                    combinedLeg.setDistance(prevLeg.getDistance() + tripPlanLeg.getDistance());

                    // Előző szakasz cseréje a kombinált szakaszra
                    filteredTripPlanLegs.set(filteredTripPlanLegs.size() - 1, combinedLeg);
                    numberOfStops.set(numberOfStops.size() - 1, numberOfStop + 1);
                } else {
                    // Új szakasz hozzáadása
                    filteredTripPlanLegs.add(new TripPlanLeg(tripPlanLeg));
                    numberOfStops.add(1);
                }
            }

            tripPlan = filteredTripPlanLegs;
            renderStopPanels(mainPanel, timeWidth, lineWidth, spacerWidth, tripPlan);
        } else {
            // Kibontott nézet
            tripPlan = new ArrayList<>(originalTripPlan);
            for (TripPlanLeg ignored : tripPlan) {
                numberOfStops.add(1);
            }
            renderStopPanels(mainPanel, timeWidth, lineWidth, spacerWidth, originalTripPlan);
        }

        // Végső megálló megjelenítése
        TripPlanLeg lastLeg = tripPlan.getLast();
        JPanel finalStopPanel = createFinalStopPanel(lastLeg, timeWidth, lineWidth, spacerWidth);
        mainPanel.add(finalStopPanel);

        // Vissza gomb panel alul
        JPanel backButtonPanelBottom = new JPanel();
        backButtonPanelBottom.setLayout(new BoxLayout(backButtonPanelBottom, BoxLayout.X_AXIS));
        backButtonPanelBottom.setBackground(Color.WHITE);
        backButtonPanelBottom.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        // Vissza gomb hozzáadása alul
        JButton backButtonBottom = new JButton("Back");
        backButtonBottom.setAlignmentX(Component.RIGHT_ALIGNMENT);
        backButtonBottom.addActionListener(_ -> {
            dispose(); // Ablak bezárása
        });
        backButtonPanelBottom.add(Box.createHorizontalGlue());
        backButtonPanelBottom.add(backButtonBottom);

        mainPanel.add(backButtonPanelBottom);
    }

    /**
     * Az utazási terv exportálása TXT fájlba.
     */
    private void exportTripPlanToTxt() {
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("trip_plan.txt"))) {
            for (int i = 0; i < tripPlan.size(); i++) {
                TripPlanLeg leg = tripPlan.get(i);

                if (leg.getLegType() == TripPlanLeg.LegType.TRANSIT) {
                    String fromStopName = leg.getFromStop().getStopName();
                    String startTime = leg.getStartTime().format(timeFormatter);
                    String routeShortName = leg.getRouteShortName();
                    Trip trip = tripMap.get(leg.getTripId());
                    String toStopName = (trip != null) ? trip.getTripHeadsign().orElse("") : "";
                    long durationMinutes = leg.getDuration() / 60;
                    writer.write("O- " + fromStopName + " - " + startTime + "\n");
                    writer.write("|\n");
                    writer.write("|-- (" + routeShortName + " ▶ " + toStopName + ") ");
                    if (!expanded) {
                        int nos = numberOfStops.get(i);
                        writer.write(nos + (nos == 1 ? " stop - " : " stops - "));
                    }
                    writer.write(durationMinutes + " min\n");
                    writer.write("|\n");
                } else if (leg.getLegType() == TripPlanLeg.LegType.WALK) {
                    String fromStopName = leg.getFromStop().getStopName();
                    String startTime = leg.getStartTime().format(timeFormatter);
                    double distance = leg.getDistance();
                    long durationMinutes = leg.getDuration() / 60;
                    writer.write("O- " + fromStopName + " - " + startTime + "\n");
                    writer.write("|\n");
                    writer.write("|-- " + durationMinutes + " min walk (" + String.format("%.0f m", distance) + ")\n");
                    writer.write("|\n");
                } else if (leg.getLegType() == TripPlanLeg.LegType.TRANSFER) {
                    String fromStopName = leg.getFromStop().getStopName();
                    String startTime = leg.getStartTime().format(timeFormatter);
                    writer.write("O- " + fromStopName + " - " + startTime + "\n");
                    writer.write("|\n");
                    writer.write("|-- Transfer - " + (leg.getDuration() / 60) + " min wait\n");
                    writer.write("|\n");
                }
            }
            // Végső megálló írása
            TripPlanLeg lastLeg = tripPlan.get(tripPlan.size() - 1);
            String finalStopName = lastLeg.getToStop().getStopName();
            String endTime = lastLeg.getEndTime().format(timeFormatter);
            writer.write("O- " + finalStopName + " - " + endTime + "\n");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error during trip planning: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * A megálló panelek renderelése.
     *
     * @param mainPanel  A fő panel.
     * @param timeWidth  Az idő oszlop szélessége.
     * @param lineWidth  A vonal oszlop szélessége.
     * @param spacerWidth A spacer szélessége.
     * @param tripPlan   Az utazási terv.
     */
    private void renderStopPanels(JPanel mainPanel, int timeWidth, int lineWidth, int spacerWidth, List<TripPlanLeg> tripPlan) {
        for (int i = 0; i < tripPlan.size(); i++) {
            TripPlanLeg leg = tripPlan.get(i);
            Integer nof = numberOfStops.get(i);

            // Panel létrehozása minden szakaszhoz
            JPanel legPanel = createStopPanel(leg, timeWidth, lineWidth, spacerWidth, nof);
            mainPanel.add(legPanel);
        }
    }

    /**
     * Megálló panel létrehozása egy adott szakaszhoz.
     *
     * @param leg         Az utazási szakasz.
     * @param timeWidth   Az idő oszlop szélessége.
     * @param lineWidth   A vonal oszlop szélessége.
     * @param spacerWidth A spacer szélessége.
     * @param numberOfStops Megállók száma.
     * @return A létrehozott panel.
     */
    private JPanel createStopPanel(TripPlanLeg leg, int timeWidth, int lineWidth, int spacerWidth, int numberOfStops) {
        JPanel stopPanel = createBasePanel();

        // Idő címke
        String time = leg.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm"));
        JLabel timeLabel = createTimeLabel(time, timeWidth);
        stopPanel.add(timeLabel);

        // Spacer
        stopPanel.add(Box.createRigidArea(new Dimension(spacerWidth, 0)));

        // Vonal komponens
        JPanel lineContainer = createLineContainer(lineWidth, new LineComponent(leg, false));
        stopPanel.add(lineContainer);

        // Távtartó
        stopPanel.add(Box.createRigidArea(new Dimension(spacerWidth, 0)));

        // Részletek panel
        JPanel detailsPanel = createDetailsPanel(leg, numberOfStops);
        stopPanel.add(detailsPanel);

        return stopPanel;
    }

    /**
     * Végső megálló panel létrehozása.
     *
     * @param leg         Az utolsó szakasz.
     * @param timeWidth   Az idő oszlop szélessége.
     * @param lineWidth   A vonal oszlop szélessége.
     * @param spacerWidth A spacer szélessége.
     * @return A létrehozott panel.
     */
    private JPanel createFinalStopPanel(TripPlanLeg leg, int timeWidth, int lineWidth, int spacerWidth) {
        JPanel stopPanel = createBasePanel();

        // Idő címke (befejezési idő)
        String time = leg.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm"));
        JLabel timeLabel = createTimeLabel(time, timeWidth);
        stopPanel.add(timeLabel);

        // Távtartó
        stopPanel.add(Box.createRigidArea(new Dimension(spacerWidth, 0)));

        // Vonal komponens (csak felső rész)
        JPanel lineContainer = createLineContainer(lineWidth, new LineComponent(null, true));
        stopPanel.add(lineContainer);

        // Spacer
        stopPanel.add(Box.createRigidArea(new Dimension(spacerWidth, 0)));

        // Részletek panel
        JPanel detailsPanel = getEndStopDetailsPanel(leg);
        stopPanel.add(detailsPanel);

        return stopPanel;
    }

    /**
     * Alap panel létrehozása.
     *
     * @return Az alap panel.
     */
    private JPanel createBasePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        panel.setBackground(Color.WHITE);
        return panel;
    }

    /**
     * Idő címke létrehozása.
     *
     * @param time      Az idő szövege.
     * @param timeWidth Az idő címke szélessége.
     * @return Az idő címke.
     */
    private JLabel createTimeLabel(String time, int timeWidth) {
        JLabel timeLabel = new JLabel(time);
        timeLabel.setFont(new Font("Arial", Font.BOLD, 14));
        timeLabel.setPreferredSize(new Dimension(timeWidth, 30));
        timeLabel.setMinimumSize(new Dimension(timeWidth, 30));
        timeLabel.setMaximumSize(new Dimension(timeWidth, 30));
        timeLabel.setAlignmentY(Component.TOP_ALIGNMENT);
        return timeLabel;
    }

    /**
     * Vonal konténer létrehozása.
     *
     * @param lineWidth     A vonal szélessége.
     * @param lineComponent A vonal komponens.
     * @return A vonal konténer.
     */
    private JPanel createLineContainer(int lineWidth, LineComponent lineComponent) {
        JPanel lineContainer = new JPanel();
        lineContainer.setLayout(new BoxLayout(lineContainer, BoxLayout.Y_AXIS));
        lineContainer.setBackground(Color.WHITE);
        lineContainer.setPreferredSize(new Dimension(lineWidth, 50));
        lineContainer.setMaximumSize(new Dimension(lineWidth, Integer.MAX_VALUE));
        lineComponent.setPreferredSize(new Dimension(lineWidth, 50));
        lineComponent.setMaximumSize(new Dimension(lineWidth, Integer.MAX_VALUE));
        lineContainer.add(lineComponent);
        return lineContainer;
    }

    /**
     * Részletek panel létrehozása.
     *
     * @param leg           Az utazási szakasz.
     * @param numberOfStops Megállók száma.
     * @return A részletek panel.
     */
    private JPanel createDetailsPanel(TripPlanLeg leg, int numberOfStops) {
        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setBackground(Color.WHITE);
        detailsPanel.setAlignmentY(Component.TOP_ALIGNMENT);
        detailsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        // Megálló neve
        String stopName = leg.getFromStop().getStopName();
        JLabel stopLabel = new JLabel(stopName);
        stopLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        stopLabel.setAlignmentY(Component.TOP_ALIGNMENT);

        // Közlekedési részletek
        String transport = getTransportDetails(leg, numberOfStops);
        JLabel transportLabel = new JLabel(transport);
        transportLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        transportLabel.setForeground(Color.GRAY);

        detailsPanel.add(stopLabel);
        detailsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        detailsPanel.add(transportLabel);
        return detailsPanel;
    }

    /**
     * Végső megálló részleteinek panelje.
     *
     * @param leg Az utolsó szakasz.
     * @return A részletek panel.
     */
    private static JPanel getEndStopDetailsPanel(TripPlanLeg leg) {
        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setBackground(Color.WHITE);
        detailsPanel.setAlignmentY(Component.TOP_ALIGNMENT);
        detailsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        // Végső megálló neve
        String stopName = leg.getToStop().getStopName();
        JLabel stopLabel = new JLabel(stopName);
        stopLabel.setFont(new Font("Arial", Font.BOLD, 14));
        stopLabel.setAlignmentY(Component.BOTTOM_ALIGNMENT);

        detailsPanel.add(stopLabel);
        return detailsPanel;
    }

    /**
     * Közlekedési részletek lekérése egy szakaszhoz.
     *
     * @param leg           Az utazási szakasz.
     * @param numberOfStops Megállók száma.
     * @return A közlekedési részletek szövege.
     */
    private String getTransportDetails(TripPlanLeg leg, int numberOfStops) {
        String transport;
        String details;
        long durationMinutes;
        switch (leg.getLegType()) {
            case TRANSIT:
                transport = getTransportModeName(leg.getRouteId()) + " " + leg.getRouteShortName();
                durationMinutes = leg.getDuration() / 60;
                details = durationMinutes + " min";
                if (numberOfStops != 1) {
                    details += ", " + numberOfStops + " stops";
                }
                break;
            case WALK:
                transport = "Walk";
                durationMinutes = leg.getDuration() / 60;
                details = durationMinutes + " min, " + String.format("%.0f m", leg.getDistance());
                break;
            case TRANSFER:
                transport = "Transfer";
                durationMinutes = leg.getDuration() / 60;
                details = durationMinutes + " min wait";
                break;
            case WAIT:
                transport = "Wait";
                durationMinutes = leg.getDuration() / 60;
                details = durationMinutes + " min wait";
                break;
            default:
                throw new IllegalStateException("Unexpected Value: " + leg.getLegType());
        }
        return transport + " - " + details;
    }

    /**
     * Vonal komponens, ami megrajzolja a vonalat a panelen.
     */
    private class LineComponent extends JPanel {
        private final TripPlanLeg leg;
        private final boolean isLastLeg;

        public LineComponent(TripPlanLeg leg, boolean isLastLeg) {
            this.leg = leg;
            this.isLastLeg = isLastLeg;
            setBackground(Color.WHITE);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g;

            int x = getWidth() / 2;
            int yStart = 0;
            int yEnd = getHeight();

            if (isLastLeg) {
                yEnd = getHeight() / 2;
            }

            if (leg != null) {
                if (leg.getLegType() == TripPlanLeg.LegType.WALK) {
                    // Szaggatott vonal gyaloglás esetén
                    float[] dashPattern = {5, 5};
                    g2.setStroke(new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10, dashPattern, 0));
                    g2.setColor(Color.GRAY);
                } else if (leg.getLegType() == TripPlanLeg.LegType.TRANSIT) {
                    g2.setStroke(new BasicStroke(3));
                    if (routeMap != null && routeMap.containsKey(leg.getRouteId())) {
                        g2.setColor(routeMap.get(leg.getRouteId()).getColor());
                    } else {
                        g2.setColor(Color.GRAY);
                    }
                } else if (leg.getLegType() == TripPlanLeg.LegType.TRANSFER) {
                    // Szaggatott vonal átszállás esetén
                    float[] dashPattern = {10, 10};
                    g2.setStroke(new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10, dashPattern, 0));
                    g2.setColor(Color.ORANGE);
                } else if (leg.getLegType() == TripPlanLeg.LegType.WAIT) {
                    // Szaggatott vonal várakozás esetén
                    float[] dashPattern = {10, 10};
                    g2.setStroke(new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10, dashPattern, 0));
                    g2.setColor(Color.BLUE);
                }
            } else {
                // Alapértelmezett vonal a végső megállóhoz
                g2.setStroke(new BasicStroke(3));
                g2.setColor(Color.GRAY);
            }

            g2.drawLine(x, yStart, x, yEnd);
        }
    }

    /**
     * Közlekedési mód nevének lekérése útvonal azonosító alapján.
     *
     * @param routeId Az útvonal azonosítója.
     * @return A közlekedési mód neve.
     */
    private String getTransportModeName(String routeId) {
        Route route = routeMap.get(routeId);
        if (route != null) {
            int routeType = route.getRouteType();
            return switch (routeType) {
                case 0 -> "Tram";
                case 1 -> "Subway";
                case 2 -> "Rail";
                case 3 -> "Bus";
                case 4 -> "Ferry";
                case 5 -> "Cable Car";
                case 6 -> "Gondola";
                case 7 -> "Funicular";
                case 11 -> "Trolley";
                case 109 -> "Suburban Railway";
                default -> "Transit";
            };
        }
        return "Transit";
    }
}
