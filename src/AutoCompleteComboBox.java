import javax.swing.*;
import java.awt.event.*;
import java.util.List;
import java.util.ArrayList;

public class AutoCompleteComboBox extends JComboBox<Stop> {

    private final List<Stop> items;

    public AutoCompleteComboBox(List<Stop> items) {
        super(items.toArray(new Stop[0]));
        this.items = items;
        setEditable(true);
        configureAutoComplete();
    }

    private void configureAutoComplete() {
        JTextField textField = (JTextField) getEditor().getEditorComponent();
        textField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                SwingUtilities.invokeLater(() -> {
                    String text = textField.getText();
                    if (text.isEmpty()) {
                        setModel(new DefaultComboBoxModel<>(items.toArray(new Stop[0])));
                        textField.setText(text);
                        hidePopup();
                    } else {
                        List<Stop> filteredItems = new ArrayList<>();
                        for (Stop item : items) {
                            if (item.getStopName().toLowerCase().contains(text.toLowerCase())) {
                                filteredItems.add(item);
                            }
                        }
                        if (filteredItems.size() > 0) {
                            setModel(new DefaultComboBoxModel<>(filteredItems.toArray(new Stop[0])));
                            textField.setText(text);
                            showPopup();
                        } else {
                            hidePopup();
                        }
                    }
                });
            }
        });
    }
}