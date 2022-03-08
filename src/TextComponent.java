import javax.swing.*;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.TextAction;
import java.awt.event.*;

import static javax.swing.KeyStroke.getKeyStroke;

final class TextComponentFactory {

    private TextComponentFactory() {
        /* not intended to be instantiated */
    }

    /**
     * Creates a new {@link JTextField} instance with a context pop-up menu by default.
     *
     * @return the new instance
     */
    static JTextField newTextField() {
        return newTextField(null);
    }

    /**
     * Creates a new {@link JTextField} instance with a context pop-up menu by default.
     *
     * @param text the initial text
     * @return the new instance
     */
    static JTextField newTextField(String text) {
        JTextField textField = text == null ? new JTextField() : new JTextField(text);
        textField.addMouseListener(new TextComponentPopupListener());
        TextComponentActionType.bindAllActions(textField);
        return textField;
    }

    /**
     * Creates a new {@link JPasswordField} instance with a context pop-up menu by default.
     *
     * @return the new instance
     */
    static JPasswordField newPasswordField() {
        JPasswordField passwordField = new JPasswordField();
        passwordField.addMouseListener(new TextComponentPopupListener());
        TextComponentActionType.bindAllActions(passwordField);
        return passwordField;
    }

    /**
     * Creates a new {@link JTextArea} instance with a context pop-up menu by default.
     *
     * @return the new instance
     */
    static JTextArea newTextArea() {
        return newTextArea(null);
    }

    /**
     * Creates a new {@link JTextArea} instance with a context pop-up menu by default.
     *
     * @param text the initial text
     * @return the new instance
     */
    static JTextArea newTextArea(String text) {
        JTextArea textArea = text == null ? new JTextArea() : new JTextArea(text);
        textArea.addMouseListener(new TextComponentPopupListener());
        TextComponentActionType.bindAllActions(textArea);
        return textArea;
    }
}

abstract class TextComponentAction extends TextAction {

    TextComponentAction(String text, KeyStroke accelerator, int mnemonic) {
        super(text);
        if (accelerator != null) {
            putValue(ACCELERATOR_KEY, accelerator);
        }
        putValue(MNEMONIC_KEY, mnemonic);
    }

    public abstract boolean isEnabled(JTextComponent component);
}

enum TextComponentActionType {
    CUT(new TextComponentAction("Cut", getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_MASK), KeyEvent.VK_T) {
        @Override
        public void actionPerformed(ActionEvent e) {
            JTextComponent component = getTextComponent(e);
            if (isEnabled(component)) {
                try {
                    ClipboardUtils.setClipboardContent(component.getSelectedText());
                } catch (Exception ex) {
                    // ignore
                }
                component.replaceSelection("");
            }
        }

        @Override
        public boolean isEnabled(JTextComponent component) {
            return component != null && component.isEnabled() && component.isEditable()
                    && component.getSelectedText() != null;
        }
    }),
    COPY(new TextComponentAction("Copy", getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK), KeyEvent.VK_C) {
        @Override
        public void actionPerformed(ActionEvent e) {
            JTextComponent component = getTextComponent(e);
            if (isEnabled(component)) {
                try {
                    ClipboardUtils.setClipboardContent(component.getSelectedText());
                } catch (Exception ex) {
                    // ignore
                }
            }
        }

        @Override
        public boolean isEnabled(JTextComponent component) {
            return component != null && component.isEnabled() && component.getSelectedText() != null;
        }
    }),
    PASTE(new TextComponentAction("Paste", getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_MASK), KeyEvent.VK_P) {
        @Override
        public void actionPerformed(ActionEvent e) {
            JTextComponent component = getTextComponent(e);
            if (isEnabled(component)) {
                component.replaceSelection(ClipboardUtils.getClipboardContent());
            }
        }

        @Override
        public boolean isEnabled(JTextComponent component) {
            return component != null && component.isEnabled() && component.isEditable()
                    && ClipboardUtils.getClipboardContent() != null;
        }
    }),
    DELETE(new TextComponentAction("Delete", getKeyStroke(KeyEvent.VK_DELETE, 0), KeyEvent.VK_D) {
        @Override
        public void actionPerformed(ActionEvent e) {
            JTextComponent component = getTextComponent(e);
            if (component != null && component.isEnabled() && component.isEditable()) {
                try {
                    Document doc = component.getDocument();
                    Caret caret = component.getCaret();
                    int dot = caret.getDot();
                    int mark = caret.getMark();
                    if (dot != mark) {
                        doc.remove(Math.min(dot, mark), Math.abs(dot - mark));
                    } else if (dot < doc.getLength()) {
                        int delChars = 1;
                        if (dot < doc.getLength() - 1) {
                            String dotChars = doc.getText(dot, 2);
                            char c0 = dotChars.charAt(0);
                            char c1 = dotChars.charAt(1);
                            if (c0 >= '\uD800' && c0 <= '\uDBFF' && c1 >= '\uDC00' && c1 <= '\uDFFF') {
                                delChars = 2;
                            }
                        }
                        doc.remove(dot, delChars);
                    }
                } catch (Exception bl) {
                    // ignore
                }
            }
        }

        @Override
        public boolean isEnabled(JTextComponent component) {
            return component != null && component.isEnabled() && component.isEditable()
                    && component.getSelectedText() != null;
        }
    }),
    CLEAR_ALL(new TextComponentAction("Clear All", null, KeyEvent.VK_L) {
        @Override
        public void actionPerformed(ActionEvent e) {
            JTextComponent component = getTextComponent(e);
            if (isEnabled(component)) {
                component.selectAll();
                component.replaceSelection("");
            }
        }

        @Override
        public boolean isEnabled(JTextComponent component) {
            boolean result;
            if (component instanceof JPasswordField) {
                result = component.isEnabled() && component.isEditable()
                        && ((JPasswordField) component).getPassword() != null
                        && ((JPasswordField) component).getPassword().length > 0;
            } else {
                result = component != null && component.isEnabled() && component.isEditable()
                        && component.getText() != null && !component.getText().isEmpty();
            }
            return result;
        }
    }),
    SELECT_ALL(new TextComponentAction("Select All", getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_MASK), KeyEvent.VK_A) {
        @Override
        public void actionPerformed(ActionEvent e) {
            JTextComponent component = getTextComponent(e);
            if (isEnabled(component)) {
                component.selectAll();
            }
        }

        @Override
        public boolean isEnabled(JTextComponent component) {
            boolean result;
            if (component instanceof JPasswordField) {
                result = component.isEnabled() && ((JPasswordField) component).getPassword() != null
                        && ((JPasswordField) component).getPassword().length > 0;
            } else {
                result = component != null && component.isEnabled() && component.getText() != null
                        && !component.getText().isEmpty();
            }
            return result;
        }
    });

    private final String name;
    private final TextComponentAction action;

    TextComponentActionType(TextComponentAction action) {
        this.name = String.format("passwordmanager.text.%s_action", this.name().toLowerCase());
        this.action = action;
    }

    private String getName() {
        return this.name;
    }

    public TextComponentAction getAction() {
        return this.action;
    }

    private KeyStroke getAccelerator() {
        return (KeyStroke) this.action.getValue(Action.ACCELERATOR_KEY);
    }

    public static void bindAllActions(JTextComponent component) {
        ActionMap actionMap = component.getActionMap();
        InputMap inputMap = component.getInputMap();
        for (TextComponentActionType type : values()) {
            actionMap.put(type.getName(), type.getAction());
            KeyStroke acc = type.getAccelerator();
            if (acc != null) {
                inputMap.put(type.getAccelerator(), type.getName());
            }
        }
    }
}

class TextComponentPopupListener extends MouseAdapter {

    private final JPopupMenu popup;
    private final JMenuItem cutItem;
    private final JMenuItem copyItem;
    private final JMenuItem pasteItem;
    private final JMenuItem deleteItem;
    private final JMenuItem clearAllItem;
    private final JMenuItem selectAllItem;

    TextComponentPopupListener() {
        this.cutItem = new JMenuItem(TextComponentActionType.CUT.getAction());
        this.copyItem = new JMenuItem(TextComponentActionType.COPY.getAction());
        this.pasteItem = new JMenuItem(TextComponentActionType.PASTE.getAction());
        this.deleteItem = new JMenuItem(TextComponentActionType.DELETE.getAction());
        this.clearAllItem = new JMenuItem(TextComponentActionType.CLEAR_ALL.getAction());
        this.selectAllItem = new JMenuItem(TextComponentActionType.SELECT_ALL.getAction());

        this.popup = new JPopupMenu();
        this.popup.add(this.cutItem);
        this.popup.add(this.copyItem);
        this.popup.add(this.pasteItem);
        this.popup.add(this.deleteItem);
        this.popup.addSeparator();
        this.popup.add(this.clearAllItem);
        this.popup.add(this.selectAllItem);
    }

    private void showPopupMenu(MouseEvent e) {
        if (e.isPopupTrigger() && e.getSource() instanceof JTextComponent) {
            JTextComponent textComponent = (JTextComponent) e.getSource();
            if (textComponent.isEnabled() && (textComponent.hasFocus() || textComponent.requestFocusInWindow())) {
                this.cutItem.setEnabled(TextComponentActionType.CUT.getAction().isEnabled(textComponent));
                this.copyItem.setEnabled(TextComponentActionType.COPY.getAction().isEnabled(textComponent));
                this.pasteItem.setEnabled(TextComponentActionType.PASTE.getAction().isEnabled(textComponent));
                this.deleteItem.setEnabled(TextComponentActionType.DELETE.getAction().isEnabled(textComponent));
                this.clearAllItem.setEnabled(TextComponentActionType.CLEAR_ALL.getAction().isEnabled(textComponent));
                this.selectAllItem.setEnabled(TextComponentActionType.SELECT_ALL.getAction().isEnabled(textComponent));
                this.popup.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        showPopupMenu(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        showPopupMenu(e);
    }
}
