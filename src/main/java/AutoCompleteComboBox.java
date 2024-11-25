import model.Stop;

import javax.swing.*;
import java.awt.event.*;
import java.util.List;
import java.util.ArrayList;

/**
 * Az AutoCompleteComboBox osztály egy automatikus kiegészítést biztosító legördülő lista a megállókhoz.
 */
public class AutoCompleteComboBox extends JComboBox<Stop> {

    private final List<Stop> items;

    /**
     * Konstruktor, amely inicializálja az input field-et.
     *
     * @param items A megállók listája.
     */
    public AutoCompleteComboBox(List<Stop> items) {
        super(items.toArray(new Stop[0]));
        this.items = items;
        setEditable(true);
        configureAutoComplete();
    }

    /**
     * Az input field konfigurálása.
     */
    private void configureAutoComplete() {
        JTextField textField = (JTextField) getEditor().getEditorComponent();
        textField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                // Navigációs billentyűk figyelmen kívül hagyása, hogy lehessen velük lefele/felfele menni a listában.
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
