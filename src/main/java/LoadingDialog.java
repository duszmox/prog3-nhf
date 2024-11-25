import javax.swing.*;
import java.awt.*;

/**
 * A LoadingDialog osztály egy töltő ablakot jelenít meg az útvonal tervezése közben.
 */
public class LoadingDialog extends JDialog {

    /**
     * Konstruktor, amely inicializálja a töltő ablakot.
     *
     * @param parent A szülő JFrame ablak.
     */
    public LoadingDialog(JFrame parent) {
        super(parent, "Loading..", false); // Non-modal dialog

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
        setResizable(false);
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
    }
}
