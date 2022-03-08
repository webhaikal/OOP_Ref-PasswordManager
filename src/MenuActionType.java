import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.*;

import static javax.swing.KeyStroke.getKeyStroke;

/**
 * Enumeration which holds menu actions and related data.
 *
 * @author Gabor_Bata
 *
 */
public enum MenuActionType {
    NEW_FILE(new AbstractMenuAction("New", getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK)) {
        @Override
        public void actionPerformed(ActionEvent ev) {
            FileHelper.createNew(PasswordManagerFrame.getInstance());
        }
    }),
    OPEN_FILE(new AbstractMenuAction("Open File...", getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK)) {
        @Override
        public void actionPerformed(ActionEvent ev) {
            FileHelper.openFile(PasswordManagerFrame.getInstance());
        }
    }),
    SAVE_FILE(new AbstractMenuAction("Save", getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK)) {
        @Override
        public void actionPerformed(ActionEvent ev) {
            FileHelper.saveFile(PasswordManagerFrame.getInstance(), false);
        }
    }),
    SAVE_AS_FILE(new AbstractMenuAction("Save As...", null) {
        @Override
        public void actionPerformed(ActionEvent ev) {
            FileHelper.saveFile(PasswordManagerFrame.getInstance(), true);
        }
    }),
    EXPORT_XML(new AbstractMenuAction("Export to XML...", null) {
        @Override
        public void actionPerformed(ActionEvent ev) {
            FileHelper.exportFile(PasswordManagerFrame.getInstance());
        }
    }),
    IMPORT_XML(new AbstractMenuAction("Import from XML...", null) {
        @Override
        public void actionPerformed(ActionEvent ev) {
            FileHelper.importFile(PasswordManagerFrame.getInstance());
        }
    }),
    CHANGE_PASSWORD(new AbstractMenuAction("Change Password...", null) {
        @Override
        public void actionPerformed(ActionEvent ev) {
            PasswordManagerFrame parent = PasswordManagerFrame.getInstance();
            byte[] password = MessageDialog.showPasswordDialog(parent, true);
            if (password == null) {
                MessageDialog.showInformationMessage(parent, "Password has not been modified.");
            } else {
                parent.getModel().setPassword(password);
                parent.getModel().setModified(true);
                parent.refreshFrameTitle();
                MessageDialog.showInformationMessage(parent,
                        "Password has been successfully modified.\n\nSave the file now in order to\nget the new password applied.");
            }
        }
    }),
    GENERATE_PASSWORD(new AbstractMenuAction("Generate Password...", getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK)) {
        @Override
        public void actionPerformed(ActionEvent ev) {
            new GeneratePasswordDialog(PasswordManagerFrame.getInstance());
        }
    }),
    EXIT(new AbstractMenuAction("Exit", getKeyStroke(KeyEvent.VK_F4, InputEvent.ALT_DOWN_MASK)) {
        @Override
        public void actionPerformed(ActionEvent ev) {
            PasswordManagerFrame.getInstance().exitFrame();
        }
    }),
    ABOUT(new AbstractMenuAction("About", getKeyStroke(KeyEvent.VK_F1, 0)) {
        @Override
        public void actionPerformed(ActionEvent ev) {
            String sb = "<b>PasswordManager</b>\n" +
                    "Copyright &copy; 2019 Haikal Izzuddin\n" +
                    "\n" +
                    "Java version: " + System.getProperties().getProperty("java.version") + "\n" +
                    System.getProperties().getProperty("java.vendor");
            MessageDialog.showInformationMessage(PasswordManagerFrame.getInstance(), sb);
        }
    }),
    ADD_ENTRY(new AbstractMenuAction("Add Entry...", getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK)) {
        @Override
        public void actionPerformed(ActionEvent ev) {
            EntryHelper.addEntry(PasswordManagerFrame.getInstance());
        }
    }),
    EDIT_ENTRY(new AbstractMenuAction("Edit Entry...",  getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK)) {
        @Override
        public void actionPerformed(ActionEvent ev) {
            EntryHelper.editEntry(PasswordManagerFrame.getInstance());
        }
    }),
    DUPLICATE_ENTRY(new AbstractMenuAction("Duplicate Entry...", getKeyStroke(KeyEvent.VK_K, InputEvent.CTRL_DOWN_MASK)) {
        @Override
        public void actionPerformed(ActionEvent ev) {
            EntryHelper.duplicateEntry(PasswordManagerFrame.getInstance());
        }
    }),
    DELETE_ENTRY(new AbstractMenuAction("Delete Entry...", getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK)) {
        @Override
        public void actionPerformed(ActionEvent ev) {
            EntryHelper.deleteEntry(PasswordManagerFrame.getInstance());
        }
    }),
    COPY_URL(new AbstractMenuAction("Copy URL", getKeyStroke(KeyEvent.VK_U, InputEvent.CTRL_DOWN_MASK)) {
        @Override
        public void actionPerformed(ActionEvent ev) {
            PasswordManagerFrame parent = PasswordManagerFrame.getInstance();
            Entry entry = EntryHelper.getSelectedEntry(parent);
            if (entry != null) {
                EntryHelper.copyEntryField(parent, entry.getUrl());
            }
        }
    }),
    COPY_USER(new AbstractMenuAction("Copy User Name", getKeyStroke(KeyEvent.VK_B, InputEvent.CTRL_DOWN_MASK)) {
        @Override
        public void actionPerformed(ActionEvent ev) {
            PasswordManagerFrame parent = PasswordManagerFrame.getInstance();
            Entry entry = EntryHelper.getSelectedEntry(parent);
            if (entry != null) {
                EntryHelper.copyEntryField(parent, entry.getUser());
            }
        }
    }),
    COPY_PASSWORD(new AbstractMenuAction("Copy Password", getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK)) {
        @Override
        public void actionPerformed(ActionEvent ev) {
            PasswordManagerFrame parent = PasswordManagerFrame.getInstance();
            Entry entry = EntryHelper.getSelectedEntry(parent);
            if (entry != null) {
                EntryHelper.copyEntryField(parent, entry.getPassword());
            }
        }
    }),
    CLEAR_CLIPBOARD(new AbstractMenuAction("Clear Clipboard", getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_DOWN_MASK)) {
        @Override
        public void actionPerformed(ActionEvent ev) {
            EntryHelper.copyEntryField(PasswordManagerFrame.getInstance(), null);
        }
    }),
    FIND_ENTRY(new AbstractMenuAction("Find Entry", getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK)) {
        @Override
        public void actionPerformed(ActionEvent ev) {
            PasswordManagerFrame.getInstance().getSearchPanel().setVisible(true);
        }
    });

    private final String name;
    private final AbstractMenuAction action;

    MenuActionType(AbstractMenuAction action) {
        this.name = String.format("passwordmanager.menu.%s_action", this.name().toLowerCase());
        this.action = action;
    }

    private String getName() {
        return this.name;
    }

    public AbstractMenuAction getAction() {
        return this.action;
    }

    private KeyStroke getAccelerator() {
        return (KeyStroke) this.action.getValue(Action.ACCELERATOR_KEY);
    }

    public static void bindAllActions(JComponent component) {
        ActionMap actionMap = component.getActionMap();
        InputMap inputMap = component.getInputMap();
        for (MenuActionType type : values()) {
            actionMap.put(type.getName(), type.getAction());
            KeyStroke acc = type.getAccelerator();
            if (acc != null) {
                inputMap.put(type.getAccelerator(), type.getName());
            }
        }
    }
}

abstract class AbstractMenuAction extends AbstractAction {

    /**
     * Creates a new menu action.
     *
     * @param text title of the action that appears on UI
     * @param accelerator accelerator key
     */
    AbstractMenuAction(String text, KeyStroke accelerator) {
        super(text);
        putValue(SHORT_DESCRIPTION, text);
        if (accelerator != null) {
            putValue(ACCELERATOR_KEY, accelerator);
        }
    }
}
