import javax.swing.*;
import java.awt.*;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import model.Route;

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

        // Define fixed widths for Time and Details sections
        int timeWidth = 60;
        int detailsWidth = 300;

        // Loop through the tripPlan and add itinerary items
        for (int i = 0; i < tripPlan.size(); i++) {
            TripPlanLeg leg = tripPlan.get(i);
            boolean isLastLeg = (i == tripPlan.size() - 1);
            boolean isFirstLeg = (i == 0);

            // Create the panel for the leg
            JPanel legPanel = createStopPanel(leg, isFirstLeg, isLastLeg, timeWidth, detailsWidth);
            mainPanel.add(legPanel);
        }

        // Add the final destination stop
        TripPlanLeg lastLeg = tripPlan.get(tripPlan.size() - 1);
        JPanel finalStopPanel = createFinalStopPanel(lastLeg, timeWidth, detailsWidth);
        mainPanel.add(finalStopPanel);

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        add(scrollPane);

        setVisible(true);
    }

    private JPanel createStopPanel(TripPlanLeg leg, boolean isFirstLeg, boolean isLastLeg, int timeWidth, int detailsWidth) {
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
        timeLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        stopPanel.add(timeLabel);

        // Spacer between time and line
        stopPanel.add(Box.createRigidArea(new Dimension(10, 0)));

        // Line component
        LineComponent lineComponent = new LineComponent(leg, isFirstLeg, isLastLeg);
        lineComponent.setPreferredSize(new Dimension(30, 50));
        lineComponent.setMaximumSize(new Dimension(20, Integer.MAX_VALUE));
        stopPanel.add(lineComponent);

        // Spacer between line and details
        stopPanel.add(Box.createRigidArea(new Dimension(10, 0)));

        // Details panel
        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setBackground(Color.WHITE);
        detailsPanel.setPreferredSize(new Dimension(detailsWidth, 50));
        detailsPanel.setMaximumSize(new Dimension(detailsWidth, Integer.MAX_VALUE));

        JLabel stopLabel = new JLabel(leg.getFromStop().getStopName());
        stopLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        String transport = "";
        String details = "";

        if (leg.getMode() == EdgeType.TRANSIT) {
            transport = getTransportModeName(leg.getRouteId()) + " " + leg.getRouteShortName();
            long durationMinutes = Duration.between(leg.getStartTime(), leg.getEndTime()).toMinutes();
            details = durationMinutes + " min";
        } else if (leg.getMode() == EdgeType.WALK || leg.getMode() == EdgeType.PATHWAY) {
            transport = "Walking";
            long durationMinutes = Duration.between(leg.getStartTime(), leg.getEndTime()).toMinutes();
            details = durationMinutes + " min, " + String.format("%.0f m", leg.getDistance());
        } else {
            transport = leg.getMode().toString();
        }

        JLabel transportLabel = new JLabel(transport + (details.isEmpty() ? "" : " - " + details));
        transportLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        transportLabel.setForeground(Color.GRAY);

        detailsPanel.add(stopLabel);
        detailsPanel.add(transportLabel);

        stopPanel.add(detailsPanel);

        return stopPanel;
    }

    private JPanel createFinalStopPanel(TripPlanLeg leg, int timeWidth, int detailsWidth) {
        JPanel stopPanel = new JPanel();
        stopPanel.setLayout(new BoxLayout(stopPanel, BoxLayout.X_AXIS));
        stopPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        stopPanel.setBackground(Color.WHITE);

        // Time label
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        String time = leg.getEndTime().format(timeFormatter);
        JLabel timeLabel = new JLabel(time);
        timeLabel.setFont(new Font("Arial", Font.BOLD, 14));
        timeLabel.setPreferredSize(new Dimension(timeWidth, 30));
        timeLabel.setMinimumSize(new Dimension(timeWidth, 30));
        timeLabel.setMaximumSize(new Dimension(timeWidth, 30));
        timeLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        stopPanel.add(timeLabel);

        // Spacer between time and line
        stopPanel.add(Box.createRigidArea(new Dimension(10, 0)));

        // Line component
        LineComponent lineComponent = new LineComponent(null, false, true);
        lineComponent.setPreferredSize(new Dimension(30, 50));
        lineComponent.setMaximumSize(new Dimension(20, Integer.MAX_VALUE));
        stopPanel.add(lineComponent);

        // Spacer between line and details
        stopPanel.add(Box.createRigidArea(new Dimension(10, 0)));

        // Details panel
        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setBackground(Color.WHITE);
        detailsPanel.setPreferredSize(new Dimension(detailsWidth, 50));
        detailsPanel.setMaximumSize(new Dimension(detailsWidth, Integer.MAX_VALUE));

        JLabel stopLabel = new JLabel(leg.getToStop().getStopName());
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

            // Draw line between top and bottom
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

            if (leg != null && (leg.getMode() == EdgeType.WALK || leg.getMode() == EdgeType.PATHWAY)) {
                // Dotted line for walking
                float[] dashPattern = {5, 5};
                g2.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10, dashPattern, 0));
                g2.setColor(Color.GRAY);
            } else {
                // Solid line for transit or final stop
                g2.setStroke(new BasicStroke(2));
                if (leg != null && routeMap.containsKey(leg.getRouteId())) {
                    g2.setColor(routeMap.get(leg.getRouteId()).getColor());
                } else {
                    g2.setColor(Color.GRAY);
                }
            }

            g2.drawLine(x, yStart, x, yEnd);
        }
    }

    private String getTransportModeName(String routeId) {
        Route route = routeMap.get(routeId);
        if (route != null) {
            int routeType = route.getRouteType();
            switch (routeType) {
                case 0:
                    return "Tram";
                case 1:
                    return "Subway";
                case 2:
                    return "Rail";
                case 3:
                    return "Bus";
                case 4:
                    return "Ferry";
                case 5:
                    return "Cable Car";
                case 6:
                    return "Gondola";
                case 7:
                    return "Funicular";
                default:
                    return "Transit";
            }
        }
        return "Transit";
    }
}