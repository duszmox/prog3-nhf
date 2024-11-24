import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import model.Route; // Ensure this import matches your project structure

public class TransitItineraryWithLines extends JFrame {
    private Map<String, Route> routeMap;

    public TransitItineraryWithLines(List<TripPlanLeg> tripPlan, Map<String, Route> routeMap) {
        this.routeMap = routeMap;
        setTitle("Transit Itinerary");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(500, 600);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(Color.WHITE);

        // Create a panel for the back button at the top
        JPanel backButtonPanel = new JPanel();
        backButtonPanel.setLayout(new BoxLayout(backButtonPanel, BoxLayout.X_AXIS));
        backButtonPanel.setBackground(Color.WHITE);
        backButtonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        // Add back button at the top left
        JButton backButton = new JButton("Back");
        backButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose(); // Close the window
            }
        });
        backButtonPanel.add(Box.createVerticalGlue());
        backButtonPanel.add(backButton);

        mainPanel.add(backButtonPanel);

        // Define fixed widths for Time and Line sections
        int timeWidth = 60;
        int lineWidth = 30; // Fixed width for the line component
        int spacerWidth = 10; // Spacer between line and details

        // Loop through the tripPlan and add itinerary items
        for (int i = 0; i < tripPlan.size(); i++) {
            TripPlanLeg leg = tripPlan.get(i);
            boolean isFirstLeg = (i == 0);

            // Create the panel for the leg
            JPanel legPanel = createStopPanel(leg, isFirstLeg, timeWidth, lineWidth, spacerWidth);
            mainPanel.add(legPanel);
        }

        // Add the final destination stop
        TripPlanLeg lastLeg = tripPlan.get(tripPlan.size() - 1);
        JPanel finalStopPanel = createFinalStopPanel(lastLeg, timeWidth, lineWidth, spacerWidth);
        mainPanel.add(finalStopPanel);

        mainPanel.add(backButtonPanel);

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(scrollPane);

        setVisible(true);
    }

    private JPanel createStopPanel(TripPlanLeg leg, boolean isFirstLeg,
                                   int timeWidth, int lineWidth, int spacerWidth) {
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
        detailsPanel.setAlignmentY(Component.TOP_ALIGNMENT);
        // Allow details panel to expand
        detailsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        // Stop name
        String stopName = leg.getFromStop().getStopName();
        JLabel stopLabel = new JLabel(stopName);
        stopLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        // Transport details
        String transport = "";
        String details = "";

        if (leg.getLegType() == TripPlanLeg.LegType.TRANSIT) {
            transport = getTransportModeName(leg.getRouteId()) + " " + leg.getRouteShortName() ;
            long durationMinutes = leg.getDuration()/60;
            details = durationMinutes + " min ";
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
