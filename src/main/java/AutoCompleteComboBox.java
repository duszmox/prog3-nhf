import model.Stop;

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
            @Override
            public void keyReleased(KeyEvent e) {
                // Ignore navigation keys
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_UP:
                    case KeyEvent.VK_DOWN:
                    case KeyEvent.VK_LEFT:
                    case KeyEvent.VK_RIGHT:
                    case KeyEvent.VK_ENTER:
                    case KeyEvent.VK_ESCAPE:
                        return;
                }

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
                        if (!filteredItems.isEmpty()) {
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
