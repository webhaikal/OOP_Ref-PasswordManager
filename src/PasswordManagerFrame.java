import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

import static javax.swing.JOptionPane.*;

/**
 * The main frame for PasswordManager.
 *
 * @author Haikal Izzuddin
 *
 */
final class PasswordManagerFrame extends JFrame {
    private static volatile PasswordManagerFrame INSTANCE;

    private final JPopupMenu popup;
    private final SearchPanel searchPanel;
    private final JList entryTitleList;
    private final DefaultListModel entryTitleListModel;
    private final DataModel model = DataModel.getInstance();
    private final StatusPanel statusPanel;
    private volatile boolean processing = false;

    private PasswordManagerFrame(String fileName) {

        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.add(MenuActionType.NEW_FILE.getAction());
        toolBar.add(MenuActionType.OPEN_FILE.getAction());
        toolBar.add(MenuActionType.SAVE_FILE.getAction());
        toolBar.addSeparator();
        toolBar.add(MenuActionType.ADD_ENTRY.getAction());
        toolBar.add(MenuActionType.EDIT_ENTRY.getAction());
        toolBar.add(MenuActionType.DUPLICATE_ENTRY.getAction());
        toolBar.add(MenuActionType.DELETE_ENTRY.getAction());
        toolBar.addSeparator();
        toolBar.add(MenuActionType.COPY_URL.getAction());
        toolBar.add(MenuActionType.COPY_USER.getAction());
        toolBar.add(MenuActionType.COPY_PASSWORD.getAction());
        toolBar.add(MenuActionType.CLEAR_CLIPBOARD.getAction());
        toolBar.addSeparator();
        toolBar.add(MenuActionType.ABOUT.getAction());
        toolBar.add(MenuActionType.EXIT.getAction());

        this.searchPanel = new SearchPanel(enabled -> {
            if (enabled) {
                refreshEntryTitleList(null);
            }
        });

        JPanel topContainerPanel = new JPanel(new BorderLayout());
        topContainerPanel.add(toolBar, BorderLayout.NORTH);
        topContainerPanel.add(this.searchPanel, BorderLayout.SOUTH);

        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        fileMenu.add(MenuActionType.NEW_FILE.getAction());
        fileMenu.add(MenuActionType.OPEN_FILE.getAction());
        fileMenu.add(MenuActionType.SAVE_FILE.getAction());
        fileMenu.add(MenuActionType.SAVE_AS_FILE.getAction());
        fileMenu.addSeparator();
        fileMenu.add(MenuActionType.EXPORT_XML.getAction());
        fileMenu.add(MenuActionType.IMPORT_XML.getAction());
        fileMenu.addSeparator();
        fileMenu.add(MenuActionType.CHANGE_PASSWORD.getAction());
        fileMenu.addSeparator();
        fileMenu.add(MenuActionType.EXIT.getAction());
        menuBar.add(fileMenu);

        JMenu editMenu = new JMenu("Edit");
        editMenu.setMnemonic(KeyEvent.VK_E);
        editMenu.add(MenuActionType.ADD_ENTRY.getAction());
        editMenu.add(MenuActionType.EDIT_ENTRY.getAction());
        editMenu.add(MenuActionType.DUPLICATE_ENTRY.getAction());
        editMenu.add(MenuActionType.DELETE_ENTRY.getAction());
        editMenu.addSeparator();
        editMenu.add(MenuActionType.COPY_URL.getAction());
        editMenu.add(MenuActionType.COPY_USER.getAction());
        editMenu.add(MenuActionType.COPY_PASSWORD.getAction());
        editMenu.addSeparator();
        editMenu.add(MenuActionType.FIND_ENTRY.getAction());
        menuBar.add(editMenu);

        JMenu toolsMenu = new JMenu("Tools");
        toolsMenu.setMnemonic(KeyEvent.VK_T);
        toolsMenu.add(MenuActionType.GENERATE_PASSWORD.getAction());
        toolsMenu.add(MenuActionType.CLEAR_CLIPBOARD.getAction());
        menuBar.add(toolsMenu);

        JMenu helpMenu = new JMenu("Help");
        helpMenu.setMnemonic(KeyEvent.VK_H);
        helpMenu.add(MenuActionType.ABOUT.getAction());
        menuBar.add(helpMenu);

        this.popup = new JPopupMenu();
        this.popup.add(MenuActionType.ADD_ENTRY.getAction());
        this.popup.add(MenuActionType.EDIT_ENTRY.getAction());
        this.popup.add(MenuActionType.DUPLICATE_ENTRY.getAction());
        this.popup.add(MenuActionType.DELETE_ENTRY.getAction());
        this.popup.addSeparator();
        this.popup.add(MenuActionType.COPY_URL.getAction());
        this.popup.add(MenuActionType.COPY_USER.getAction());
        this.popup.add(MenuActionType.COPY_PASSWORD.getAction());
        this.popup.addSeparator();
        this.popup.add(MenuActionType.FIND_ENTRY.getAction());

        this.entryTitleListModel = new DefaultListModel();
        this.entryTitleList = new JList(this.entryTitleListModel);
        this.entryTitleList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.entryTitleList.addMouseListener(new ListListener());

        JScrollPane scrollPane = new JScrollPane(this.entryTitleList);
        MenuActionType.bindAllActions(this.entryTitleList);

        this.statusPanel = new StatusPanel();

        refreshAll();

        getContentPane().add(topContainerPanel, BorderLayout.NORTH);
        getContentPane().add(scrollPane, BorderLayout.CENTER);
        getContentPane().add(this.statusPanel, BorderLayout.SOUTH);

        setJMenuBar(menuBar);
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setSize(420, 400);
        setMinimumSize(new Dimension(420, 200));
        addWindowListener(new CloseListener());
        setLocationRelativeTo(null);
        setVisible(true);
        FileHelper.doOpenFile(fileName, this);

        // set focus to the list for easier keyboard navigation
        this.entryTitleList.requestFocusInWindow();
    }

    static PasswordManagerFrame getInstance() {
        return getInstance(null);
    }

    static PasswordManagerFrame getInstance(String fileName) {
        if (INSTANCE == null) {
            synchronized (PasswordManagerFrame.class) {
                if (INSTANCE == null) {
                    INSTANCE = new PasswordManagerFrame(fileName);
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Gets the entry title list.
     *
     * @return entry title list
     */
    JList getEntryTitleList() {
        return this.entryTitleList;
    }

    /**
     * Gets the data model of this frame.
     *
     * @return data model
     */
    DataModel getModel() {
        return this.model;
    }

    /**
     * Clears data model.
     */
    void clearModel() {
        this.model.clear();
        this.entryTitleListModel.clear();
    }

    /**
     * Refresh frame title based on data model.
     */
    void refreshFrameTitle() {
        setTitle((getModel().isModified() ? "*" : "")
                + (getModel().getFileName() == null ? "Untitled" : getModel().getFileName()) + " - "
                + "PasswordManager");
    }

    /**
     * Refresh the entry titles based on data model.
     *
     * @param selectTitle title to select, or {@code null} if nothing to select
     */
    void refreshEntryTitleList(String selectTitle) {
        this.entryTitleListModel.clear();
        List<String> titles = this.model.getTitles();
        titles.sort(String.CASE_INSENSITIVE_ORDER);

        String searchCriteria = this.searchPanel.getSearchCriteria();
        for (String title : titles) {
            if (searchCriteria.isEmpty() || title.toLowerCase().contains(searchCriteria.toLowerCase())) {
                this.entryTitleListModel.addElement(title);
            }
        }

        if (selectTitle != null) {
            this.entryTitleList.setSelectedValue(selectTitle, true);
        }

        if (searchCriteria.isEmpty()) {
            this.statusPanel.setText("Entries count: " + titles.size());
        } else {
            this.statusPanel.setText("Entries found: " + this.entryTitleListModel.size() + " / " + titles.size());
        }
    }

    /**
     * Refresh frame title and entry list.
     */
    void refreshAll() {
        refreshFrameTitle();
        refreshEntryTitleList(null);
    }

    /**
     * Exits the application.
     */
    void exitFrame() {
        if (Configuration.getInstance().is("clear.clipboard.on.exit.enabled", false)) {
            EntryHelper.copyEntryField(this, null);
        }

        if (this.processing) {
            return;
        }
        if (this.model.isModified()) {
            int option = MessageDialog.showQuestionMessage(this,
                    "The current file has been modified.\nDo you want to save the changes before closing?", YES_NO_CANCEL_OPTION);
            if (option == YES_OPTION) {
                FileHelper.saveFile(this, false, result -> {
                    if (result) {
                        System.exit(0);
                    }
                });
                return;
            } else if (option != NO_OPTION) {
                return;
            }
        }
        System.exit(0);
    }

    JPopupMenu getPopup() {
        return this.popup;
    }

    /**
     * Sets the processing state of this frame.
     *
     * @param processing processing state
     */
    void setProcessing(boolean processing) {
        this.processing = processing;
        for (MenuActionType actionType : MenuActionType.values()) {
            actionType.getAction().setEnabled(!processing);
        }
        this.searchPanel.setEnabled(!processing);
        this.entryTitleList.setEnabled(!processing);
        this.statusPanel.setProcessing(processing);
    }

    /**
     * Gets the processing state of this frame.
     *
     * @return processing state
     */
    boolean isProcessing() {
        return this.processing;
    }

    /**
     * Get search panel.
     *
     * @return the search panel
     */
    SearchPanel getSearchPanel() {
        return searchPanel;
    }
}

class CloseListener extends WindowAdapter {

    /**
     * Calls the {@code exitFrame} method of main frame.
     *
     * @see java.awt.event.WindowAdapter#windowClosing(java.awt.event.WindowEvent)
     */
    @Override
    public void windowClosing(WindowEvent event) {
        if (event.getSource() instanceof PasswordManagerFrame) {
            ((PasswordManagerFrame) event.getSource()).exitFrame();
        }
    }
}