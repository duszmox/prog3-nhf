import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

public class TransitItineraryWithLines extends JFrame {
    private Map<String, Route> routeMap;
    private List<TripPlanLeg> tripPlan;
    private Map<String, Trip> tripMap;
    private final List<TripPlanLeg> originalTripPlan;
    List<Integer> numberOfStops  = new ArrayList<>();

    private boolean expanded = false;

    public TransitItineraryWithLines(List<TripPlanLeg> tripPlan, Map<String, Route> routeMap, Map<String, Trip> tripMap) {
        this.originalTripPlan = new ArrayList<>(tripPlan); // Copy original list
        this.routeMap = routeMap;
        this.tripPlan = new ArrayList<>(tripPlan); // Work on a modifiable list
        this.tripMap = tripMap;
        setTitle("Transit Itinerary");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(500, 600);
        setLocationRelativeTo(null);

        ImageIcon img = new ImageIcon("icon.png");
        setIconImage(img.getImage());

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(Color.WHITE);

        renderComponents(mainPanel);

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(scrollPane);

        setVisible(true);
    }

    private void renderComponents(JPanel mainPanel) {
        // Create a panel for the back button at the top
        JPanel backButtonPanelTop = new JPanel();
        backButtonPanelTop.setLayout(new BoxLayout(backButtonPanelTop, BoxLayout.X_AXIS));
        backButtonPanelTop.setBackground(Color.WHITE);
        backButtonPanelTop.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        // Add back button at the top right
        JButton backButtonTop = new JButton("Back");
        backButtonTop.setAlignmentX(Component.LEFT_ALIGNMENT);
        backButtonTop.addActionListener(e -> {
            dispose(); // Close the window
        });
        backButtonPanelTop.add(Box.createHorizontalGlue());
        backButtonPanelTop.add(backButtonTop);

        JButton expandButtonTop = new JButton(!expanded ? "Expand" : "Collapse");
        expandButtonTop.setAlignmentX(Component.CENTER_ALIGNMENT);
        expandButtonTop.addActionListener(e -> {
            expanded = !expanded;
            mainPanel.removeAll();
            renderComponents(mainPanel);
            mainPanel.revalidate();
        });
        backButtonPanelTop.add(Box.createHorizontalGlue());
        backButtonPanelTop.add(expandButtonTop);

        // Add export button at the top right
        JButton exportButton = new JButton("Export to TXT");
        exportButton.setAlignmentX(Component.RIGHT_ALIGNMENT);
        exportButton.addActionListener(e -> exportTripPlanToTxt());
        backButtonPanelTop.add(Box.createHorizontalGlue());
        backButtonPanelTop.add(exportButton);

        mainPanel.add(backButtonPanelTop);

        // Define fixed widths for Time and Line sections
        int timeWidth = 60;
        int lineWidth = 30; // Fixed width for the line component
        int spacerWidth = 10; // Spacer between line and details

        List<TripPlanLeg> filteredTripPlanLegs = new ArrayList<>();
        if (!expanded) {
            // Reset tripPlan to a copy of the original list
            tripPlan = new ArrayList<>(originalTripPlan);
            // Filter and combine durations

            for (TripPlanLeg tripPlanLeg : tripPlan) {
                TripPlanLeg prevLeg = null;
                Integer numberOfStop = 0;

                if (!filteredTripPlanLegs.isEmpty()) {
                    prevLeg = filteredTripPlanLegs.get(filteredTripPlanLegs.size() - 1);
                    numberOfStop = numberOfStops.get(numberOfStops.size() - 1);
                }

                boolean hidable = prevLeg != null && Objects.equals(prevLeg.getTripId(), tripPlanLeg.getTripId());
                if (hidable) {
                    // Combine durations and distances, but create a new instance to avoid modifying the original data
                    TripPlanLeg combinedLeg = new TripPlanLeg(prevLeg);
                    combinedLeg.setDuration(prevLeg.getDuration() + tripPlanLeg.getDuration());
                    combinedLeg.setDistance(prevLeg.getDistance() + tripPlanLeg.getDistance());

                    // Replace the previous leg in the filtered list with the combined leg
                    filteredTripPlanLegs.set(filteredTripPlanLegs.size() - 1, combinedLeg);
                    numberOfStops.set(numberOfStops.size() - 1, numberOfStop + 1);
                } else {
                    // Add the new leg to the filtered list
                    filteredTripPlanLegs.add(new TripPlanLeg(tripPlanLeg));
                    numberOfStops.add(1);
                }
            }


            tripPlan = filteredTripPlanLegs;
            for (int i = 0; i < tripPlan.size(); i++) {
                TripPlanLeg leg = tripPlan.get(i);
                Integer nof =  numberOfStops.get(i);
                boolean isFirstLeg = (i == 0);

                // Create and add the panel for each leg
                JPanel legPanel = createStopPanel(leg, isFirstLeg, timeWidth, lineWidth, spacerWidth, nof);
                mainPanel.add(legPanel);
            }
        } else {
            // Expanded view restores original durations
            tripPlan = new ArrayList<>(originalTripPlan);
            for (TripPlanLeg _ : tripPlan) {
                numberOfStops.add(1);
            }
            for (int i = 0; i < originalTripPlan.size(); i++) {
                TripPlanLeg leg = originalTripPlan.get(i);
                Integer nof =  numberOfStops.get(i);
                boolean isFirstLeg = (i == 0);

                // Create and add the panel for each leg
                JPanel legPanel = createStopPanel(leg, isFirstLeg, timeWidth, lineWidth, spacerWidth, nof);
                mainPanel.add(legPanel);
            }
        }


        // Render final stop
        TripPlanLeg lastLeg = tripPlan.getLast();
        JPanel finalStopPanel = createFinalStopPanel(lastLeg, timeWidth, lineWidth, spacerWidth);
        mainPanel.add(finalStopPanel);

        // Create a panel for the back button at the bottom
        JPanel backButtonPanelBottom = new JPanel();
        backButtonPanelBottom.setLayout(new BoxLayout(backButtonPanelBottom, BoxLayout.X_AXIS));
        backButtonPanelBottom.setBackground(Color.WHITE);
        backButtonPanelBottom.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        // Add back button at the bottom right
        JButton backButtonBottom = new JButton("Back");
        backButtonBottom.setAlignmentX(Component.RIGHT_ALIGNMENT);
        backButtonBottom.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose(); // Close the window
            }
        });
        backButtonPanelBottom.add(Box.createHorizontalGlue());
        backButtonPanelBottom.add(backButtonBottom);

        mainPanel.add(backButtonPanelBottom);
    }

    private void exportTripPlanToTxt() {
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("trip_plan.txt"))) {
            for (int i = 0; i < tripPlan.size(); i++) {
                TripPlanLeg leg = tripPlan.get(i);

                TripPlanLeg nextLeg;
                if (i != tripPlan.size() - 1) {
                    nextLeg = tripPlan.get(i+1);
                }
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
                        writer.write(numberOfStops.get(i) + " stations- ");
                    }
                    writer.write(durationMinutes + " Minutes\n");
                    writer.write("|\n");
                } else if (leg.getLegType() == TripPlanLeg.LegType.WALK) {
                    String fromStopName = leg.getFromStop().getStopName();
                    String startTime = leg.getStartTime().format(timeFormatter);
                    double distance = leg.getDistance();
                    long durationMinutes = leg.getDuration() / 60;
                    writer.write("O- " + fromStopName + " - " + startTime + "\n");
                    writer.write("|\n");
                    writer.write("|-- " + durationMinutes + " minute walk (" + String.format("%.0f m", distance) + ")\n");
                    writer.write("|\n");
                } else if (leg.getLegType() == TripPlanLeg.LegType.TRANSFER) {
                    String fromStopName = leg.getFromStop().getStopName();
                    String startTime = leg.getStartTime().format(timeFormatter);
                    writer.write("O- " + fromStopName + " - " + startTime + "\n");
                    writer.write("|\n");
                    writer.write("|-- Transfer - " + (leg.getDuration() / 60) + " minute wait\n");
                    writer.write("|\n");
                }
            }
            // Write the final destination stop
            TripPlanLeg lastLeg = tripPlan.getLast();
            String finalStopName = lastLeg.getToStop().getStopName();
            String endTime = lastLeg.getEndTime().format(timeFormatter);
            writer.write("O-" + finalStopName + " - " + endTime + "\n");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error exporting trip plan: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel createStopPanel(TripPlanLeg leg, boolean isFirstLeg,
                                   int timeWidth, int lineWidth, int spacerWidth, int numberOfStops) {
        JPanel stopPanel = new JPanel();
        stopPanel.setLayout(new BoxLayout(stopPanel, BoxLayout.X_AXIS));
        stopPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        stopPanel.setBackground(Color.WHITE);

        // Time label
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        String time = leg.getStartTime().format(timeFormatter);
        JLabel timeLabel = new JLabel(time);
        timeLabel.setFont(new Font("Arial", Font.BOLD, 14));
        timeLabel.setPreferredSize(new Dimension(timeWidth, 30));
        timeLabel.setMinimumSize(new Dimension(timeWidth, 30));
        timeLabel.setMaximumSize(new Dimension(timeWidth, 30));
        timeLabel.setAlignmentY(Component.TOP_ALIGNMENT);
        stopPanel.add(timeLabel);

        // Spacer between time and line
        stopPanel.add(Box.createRigidArea(new Dimension(spacerWidth, 0)));

        // Line component inside a vertical box to align multiple lines
        JPanel lineContainer = new JPanel();
        lineContainer.setLayout(new BoxLayout(lineContainer, BoxLayout.Y_AXIS));
        lineContainer.setBackground(Color.WHITE);
        lineContainer.setPreferredSize(new Dimension(lineWidth, 50));
        lineContainer.setMaximumSize(new Dimension(lineWidth, Integer.MAX_VALUE));

        LineComponent lineComponent = new LineComponent(leg, isFirstLeg, false);
        lineComponent.setPreferredSize(new Dimension(lineWidth, 50));
        lineComponent.setMaximumSize(new Dimension(lineWidth, Integer.MAX_VALUE));
        lineContainer.add(lineComponent);

        stopPanel.add(lineContainer);

        // Spacer between line and details
        stopPanel.add(Box.createRigidArea(new Dimension(spacerWidth, 0)));

        // Details panel
        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setBackground(Color.WHITE);
        detailsPanel.setAlignmentY(isFirstLeg ? Component.BOTTOM_ALIGNMENT : Component.TOP_ALIGNMENT);
        // Allow details panel to expand
        detailsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        // Stop name
        String stopName = leg.getFromStop().getStopName();
        JLabel stopLabel = new JLabel(stopName);
        stopLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        stopLabel.setAlignmentY(isFirstLeg ? Component.BOTTOM_ALIGNMENT : Component.TOP_ALIGNMENT);

        // Transport details
        String transport = "";
        String details = "";

        if (leg.getLegType() == TripPlanLeg.LegType.TRANSIT) {
            transport = getTransportModeName(leg.getRouteId()) + " " + leg.getRouteShortName() ;
            long durationMinutes = leg.getDuration()/60;
            details = durationMinutes + " min";
            if (numberOfStops != 1) {
                details += ", " + numberOfStops + " stops ";
            }
        } else if (leg.getLegType() == TripPlanLeg.LegType.WALK) {
            transport = "Walking";
            long durationMinutes = leg.getDuration()/60;
            details = durationMinutes + " min, " + String.format("%.0f m", leg.getDistance());
        } else if (leg.getLegType() == TripPlanLeg.LegType.TRANSFER) {
            transport = "Transfer";
            long durationMinutes = leg.getDuration()/60;
            details = durationMinutes + " min wait";
        } else if (leg.getLegType() == TripPlanLeg.LegType.WAIT) {
            transport = "Wait";
            long durationMinutes = leg.getDuration()/60;
            details = durationMinutes + " min wait";
        }

        JLabel transportLabel = new JLabel(transport + (details.isEmpty() ? "" : " - " + details));
        transportLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        transportLabel.setForeground(Color.GRAY);

        detailsPanel.add(stopLabel);
        detailsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        detailsPanel.add(transportLabel);

        stopPanel.add(detailsPanel);

        return stopPanel;
    }

    private JPanel createFinalStopPanel(TripPlanLeg leg, int timeWidth, int lineWidth, int spacerWidth) {
        JPanel stopPanel = new JPanel();
        stopPanel.setLayout(new BoxLayout(stopPanel, BoxLayout.X_AXIS));
        stopPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        stopPanel.setBackground(Color.WHITE);

        // Time label (end time)
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        String time = leg.getEndTime().format(timeFormatter);
        JLabel timeLabel = new JLabel(time);
        timeLabel.setFont(new Font("Arial", Font.BOLD, 14));
        timeLabel.setPreferredSize(new Dimension(timeWidth, 30));
        timeLabel.setMinimumSize(new Dimension(timeWidth, 30));
        timeLabel.setMaximumSize(new Dimension(timeWidth, 30));
        timeLabel.setAlignmentY(Component.TOP_ALIGNMENT);
        stopPanel.add(timeLabel);

        // Spacer between time and line
        stopPanel.add(Box.createRigidArea(new Dimension(spacerWidth, 0)));

        // Line component (only top part since it's the last stop)
        JPanel lineContainer = new JPanel();
        lineContainer.setLayout(new BoxLayout(lineContainer, BoxLayout.Y_AXIS));
        lineContainer.setBackground(Color.WHITE);
        lineContainer.setPreferredSize(new Dimension(lineWidth, 50));
        lineContainer.setMaximumSize(new Dimension(lineWidth, Integer.MAX_VALUE));

        LineComponent lineComponent = new LineComponent(null, false, true);
        lineComponent.setPreferredSize(new Dimension(lineWidth, 50));
        lineComponent.setMaximumSize(new Dimension(lineWidth, Integer.MAX_VALUE));
        lineContainer.add(lineComponent);

        stopPanel.add(lineContainer);

        // Spacer between line and details
        stopPanel.add(Box.createRigidArea(new Dimension(spacerWidth, 0)));

        // Details panel
        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setBackground(Color.WHITE);
        detailsPanel.setAlignmentY(Component.TOP_ALIGNMENT);
        detailsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        // Final stop name
        String stopName = leg.getToStop().getStopName();
        JLabel stopLabel = new JLabel(stopName);
        stopLabel.setFont(new Font("Arial", Font.BOLD, 14));
        stopLabel.setAlignmentY(Component.BOTTOM_ALIGNMENT);

        detailsPanel.add(stopLabel);

        stopPanel.add(detailsPanel);

        return stopPanel;
    }

    private class LineComponent extends JPanel {
        private TripPlanLeg leg;
        private boolean isFirstLeg;
        private boolean isLastLeg;

        public LineComponent(TripPlanLeg leg, boolean isFirstLeg, boolean isLastLeg) {
            this.leg = leg;
            this.isFirstLeg = isFirstLeg;
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

            if (isFirstLeg) {
                yStart = getHeight() / 2;
            }
            if (isLastLeg) {
                yEnd = getHeight() / 2;
            }

            if (leg != null) {
                if (leg.getLegType() == TripPlanLeg.LegType.WALK) {
                    // Dotted line for walking
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
                    // Dashed line for transfer
                    float[] dashPattern = {10, 10};
                    g2.setStroke(new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10, dashPattern, 0));
                    g2.setColor(Color.ORANGE);
                }
                else if (leg.getLegType() == TripPlanLeg.LegType.WAIT) {
                    // Dashed line for transfer
                    float[] dashPattern = {10, 10};
                    g2.setStroke(new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10, dashPattern, 0));
                    g2.setColor(Color.blue);
                }
            } else {
                // Default line for final stop
                g2.setStroke(new BasicStroke(3));
                g2.setColor(Color.GRAY);
            }

            g2.drawLine(x, yStart, x, yEnd);
        }
    }

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
                default -> "Trolley";
            };
        }
        return "Transit";
    }
}
