import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class TripPlanView extends JFrame {

    private JTable tripTable;

    public TripPlanView(List<TripPlanLeg> tripPlan) {
        setTitle("model.Trip Plan");
        setSize(800, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Set up the table model
        String[] columnNames = {"Mode", "From", "To", "Start Time", "End Time", "model.Trip/model.Route", "Details"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);

        // Populate the table model
        for (TripPlanLeg leg : tripPlan) {
            String mode = leg.getMode().toString();
            String from = leg.getFromStop().getStopName();
            String to = leg.getToStop().getStopName();
            String startTime = leg.getStartTime().toString();
            String endTime = leg.getEndTime().toString();
            String tripRoute = "";
            String details = "";

            if (leg.getMode() == EdgeType.TRANSIT) {
                tripRoute = "model.Trip: " + leg.getTripId() + ", model.Route: " + leg.getRouteId();
                details = "model.Route Name: " + leg.getRouteShortName() + " - " + leg.getRouteLongName();
            } else if (leg.getMode() == EdgeType.WALK || leg.getMode() == EdgeType.PATHWAY) {
                tripRoute = "Walk";
                details = String.format("Distance: %.2f meters", leg.getDistance());
            }

            tableModel.addRow(new Object[]{mode, from, to, startTime, endTime, tripRoute, details});
        }

        // Set up the table
        tripTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(tripTable);

        // Add components to the frame
        add(scrollPane, BorderLayout.CENTER);
    }
}