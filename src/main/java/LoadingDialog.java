import javax.swing.*;
import java.awt.*;

public class LoadingDialog extends JDialog {

    public LoadingDialog(JFrame parent) {
        super(parent, "Loading", false); // Non-modal dialog

        JLabel loadingLabel = new JLabel("Planning your trip, please wait...");
        loadingLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(loadingLabel, BorderLayout.NORTH);
        contentPanel.add(progressBar, BorderLayout.CENTER);

        add(contentPanel);
        setSize(300, 100);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
    }
}