import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static java.awt.event.KeyEvent.VK_ESCAPE;
import static javax.swing.KeyStroke.getKeyStroke;

/**
 * Class for representing search panel. Search panel is hidden by default.
 *
 * @author Haikal Izzuddin
 *
 */
class SearchPanel extends JPanel implements ActionListener {

    private static final long serialVersionUID = 5455248210301851210L;

    private static final String CLOSE_BUTTON_ACTION_COMMAND = "close_search_panel_button";
    private static final String SEARCH_PANEL_CLOSE_ACTION = "passwordmanager.search_panel.close";

    private final JLabel label;
    private final JTextField criteriaField;
    private final JButton closeButton;
    private final Callback callback;

    /**
     * Creates a new search panel with the given callback object.
     *
     * @param searchCallback the callback used on document updates.
     */
    SearchPanel(Callback searchCallback) {
        super(new BorderLayout());
        setBorder(new EmptyBorder(2, 2, 2, 2));

        this.callback = searchCallback;

        this.label = new JLabel("Find: ", SwingConstants.LEADING);

        this.criteriaField = TextComponentFactory.newTextField();

        if (this.callback != null) {
            this.criteriaField.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void changedUpdate(DocumentEvent e) {
                    callback.call(isEnabled());
                }

                @Override
                public void insertUpdate(DocumentEvent e) {
                    callback.call(isEnabled());
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    callback.call(isEnabled());
                }
            });
        }

        this.closeButton = new JButton();
        this.closeButton.setBorder(new EmptyBorder(0, 2, 0, 2));
        this.closeButton.setActionCommand(CLOSE_BUTTON_ACTION_COMMAND);
        this.closeButton.setFocusable(false);
        this.closeButton.addActionListener(this);

        Action closeAction = new AbstractAction() {
            private static final long serialVersionUID = 2L;

            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        };
        this.closeButton.getInputMap(WHEN_IN_FOCUSED_WINDOW).put(getKeyStroke(VK_ESCAPE, 0), SEARCH_PANEL_CLOSE_ACTION);
        this.closeButton.getActionMap().put(SEARCH_PANEL_CLOSE_ACTION, closeAction);

        add(this.label, BorderLayout.WEST);
        add(this.criteriaField, BorderLayout.CENTER);
        add(this.closeButton, BorderLayout.EAST);

        this.setVisible(false);
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            this.criteriaField.requestFocusInWindow();
        } else {
            this.criteriaField.setText("");
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        this.label.setEnabled(enabled);
        this.criteriaField.setEnabled(enabled);
        this.closeButton.setEnabled(enabled);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        if (CLOSE_BUTTON_ACTION_COMMAND.equals(command)) {
            this.setVisible(false);
        }
    }

    /**
     * Get search criteria.
     *
     * @return get search criteria, non null
     */
    String getSearchCriteria() {
        String criteria = "";
        if (isVisible() && isEnabled()) {
            criteria = this.criteriaField.getText();
            criteria = criteria == null ? "" : criteria.trim();
        }
        return criteria;
    }
}
