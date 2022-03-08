import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SpringLayout;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

/**
 * A dialog with the entry data.
 *
 * @author Haikal Izzuddin
 *
 */
class EntryDialog extends JDialog implements ActionListener {

    private static final char NULL_ECHO = '\0';

    private final JTextField titleField;
    private final JTextField userField;
    private final JPasswordField passwordField;
    private final JPasswordField repeatField;
    private final JTextField urlField;
    private final JTextArea notesField;

    private final JToggleButton showButton;

    private final char ORIGINAL_ECHO;

    private Entry formData;

    private final boolean newEntry;

    private String originalTitle;

    /**
     * Creates a new EntryDialog instance.
     *
     * @param parent parent component
     * @param title dialog title
     * @param entry the entry
     * @param newEntry new entry marker
     */
    EntryDialog(final PasswordManagerFrame parent, final String title, final Entry entry, final boolean newEntry) {
        super(parent, title, true);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.newEntry = newEntry;

        this.formData = null;

        JPanel fieldPanel = new JPanel();

        fieldPanel.add(new JLabel("Title:"));
        this.titleField = TextComponentFactory.newTextField();
        fieldPanel.add(this.titleField);

        fieldPanel.add(new JLabel("URL:"));
        this.urlField = TextComponentFactory.newTextField();
        fieldPanel.add(this.urlField);

        fieldPanel.add(new JLabel("User name:"));
        this.userField = TextComponentFactory.newTextField();
        fieldPanel.add(this.userField);

        fieldPanel.add(new JLabel("Password:"));
        this.passwordField = TextComponentFactory.newPasswordField();
        this.ORIGINAL_ECHO = this.passwordField.getEchoChar();
        fieldPanel.add(this.passwordField);

        fieldPanel.add(new JLabel("Repeat:"));
        this.repeatField = TextComponentFactory.newPasswordField();
        fieldPanel.add(this.repeatField);

        fieldPanel.add(new JLabel(""));
        JPanel passwordButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        this.showButton = new JToggleButton("Show");
        this.showButton.setActionCommand("show_button");
        this.showButton.setMnemonic(KeyEvent.VK_S);
        this.showButton.addActionListener(this);
        passwordButtonPanel.add(this.showButton);
        JButton generateButton = new JButton("Generate");
        generateButton.setActionCommand("generate_button");
        generateButton.setMnemonic(KeyEvent.VK_G);
        generateButton.addActionListener(this);
        passwordButtonPanel.add(generateButton);
        JButton copyButton = new JButton("Copy");
        copyButton.setActionCommand("copy_button");
        copyButton.setMnemonic(KeyEvent.VK_C);
        copyButton.addActionListener(this);
        passwordButtonPanel.add(copyButton);
        fieldPanel.add(passwordButtonPanel);

        fieldPanel.setLayout(new SpringLayout());
        SpringUtilities.makeCompactGrid(fieldPanel,
                6, 2, //rows, columns
                5, 5, //initX, initY
                5, 5);    //xPad, yPad

        JPanel notesPanel = new JPanel(new BorderLayout(5, 5));
        notesPanel.setBorder(new EmptyBorder(0, 5, 0, 5));
        notesPanel.add(new JLabel("Notes:"), BorderLayout.NORTH);

        this.notesField = TextComponentFactory.newTextArea();
        this.notesField.setFont(TextComponentFactory.newTextField().getFont());
        this.notesField.setLineWrap(true);
        this.notesField.setWrapStyleWord(true);
        notesPanel.add(new JScrollPane(this.notesField), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton okButton = new JButton("OK");
        okButton.setActionCommand("ok_button");
        okButton.setMnemonic(KeyEvent.VK_O);
        okButton.addActionListener(this);
        buttonPanel.add(okButton);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setActionCommand("cancel_button");
        cancelButton.setMnemonic(KeyEvent.VK_C);
        cancelButton.addActionListener(this);
        buttonPanel.add(cancelButton);

        getContentPane().add(fieldPanel, BorderLayout.NORTH);
        getContentPane().add(notesPanel, BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        fillDialogData(entry);
        setSize(420, 400);
        setMinimumSize(new Dimension(370, 300));
        setLocationRelativeTo(parent);
        setVisible(true);
    }

    /**
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        if ("show_button".equals(command)) {
            this.passwordField.setEchoChar(this.showButton.isSelected() ? NULL_ECHO : this.ORIGINAL_ECHO);
            this.repeatField.setEchoChar(this.showButton.isSelected() ? NULL_ECHO : this.ORIGINAL_ECHO);
        } else if ("ok_button".equals(command)) {
            if (this.titleField.getText().trim().isEmpty()) {
                MessageDialog.showWarningMessage(this, "Please fill the title field.");
                return;
            } else if (!checkEntryTitle()) {
                MessageDialog.showWarningMessage(this, "Title is already exists,\nplease enter a different title.");
                return;
            } else if (!Arrays.equals(this.passwordField.getPassword(), this.repeatField.getPassword())) {
                MessageDialog.showWarningMessage(this, "Password and repeated password are not identical.");
                return;
            }
            setFormData(fetchDialogData());
            dispose();
        } else if ("cancel_button".equals(command)) {
            dispose();
        } else if ("generate_button".equals(command)) {
            GeneratePasswordDialog gpd = new GeneratePasswordDialog(this);
            String generatedPassword = gpd.getGeneratedPassword();
            if (generatedPassword != null && !generatedPassword.isEmpty()) {
                this.passwordField.setText(generatedPassword);
                this.repeatField.setText(generatedPassword);
            }
        } else if ("copy_button".equals(command)) {
            EntryHelper.copyEntryField(PasswordManagerFrame.getInstance(), String.valueOf(this.passwordField.getPassword()));
        }
    }

    /**
     * Fills the form with the data of given entry.
     *
     * @param entry an entry
     */
    private void fillDialogData(Entry entry) {
        if (entry == null) {
            return;
        }
        this.originalTitle = entry.getTitle() == null ? "" : entry.getTitle();
        this.titleField.setText(this.originalTitle + (this.newEntry ? " (copy)" : ""));
        this.userField.setText(entry.getUser() == null ? "" : entry.getUser());
        this.passwordField.setText(entry.getPassword() == null ? "" : entry.getPassword());
        this.repeatField.setText(entry.getPassword() == null ? "" : entry.getPassword());
        this.urlField.setText(entry.getUrl() == null ? "" : entry.getUrl());
        this.notesField.setText(entry.getNotes() == null ? "" : entry.getNotes());
        this.notesField.setCaretPosition(0);
    }

    /**
     * Retrieves the form data.
     *
     * @return an entry
     */
    private Entry fetchDialogData() {
        Entry entry = new Entry();

        String title = StringUtils.stripNonValidXMLCharacters(this.titleField.getText());
        String user = StringUtils.stripNonValidXMLCharacters(this.userField.getText());
        String password = StringUtils.stripNonValidXMLCharacters(String.valueOf(this.passwordField.getPassword()));
        String url = StringUtils.stripNonValidXMLCharacters(this.urlField.getText());
        String notes = StringUtils.stripNonValidXMLCharacters(this.notesField.getText());

        entry.setTitle(title == null || title.isEmpty() ? null : title);
        entry.setUser(user == null || user.isEmpty() ? null : user);
        entry.setPassword(password == null || password.isEmpty() ? null : password);
        entry.setUrl(url == null || url.isEmpty() ? null : url);
        entry.setNotes(notes == null || notes.isEmpty() ? null : notes);

        return entry;
    }

    /**
     * Sets the form data.
     *
     * @param formData form data
     */
    private void setFormData(Entry formData) {
        this.formData = formData;
    }

    /**
     * Gets the form data (entry) of this dialog.
     *
     * @return nonempty form data if the 'OK1 button is pressed, otherwise an empty data
     */
    Entry getFormData() {
        return this.formData;
    }

    /**
     * Checks the entry title.
     *
     * @return if the entry title is already exists in the data model than returns {@code false},
     * otherwise {@code true}
     */
    private boolean checkEntryTitle() {
        boolean titleIsOk = true;
        PasswordManagerFrame parent = PasswordManagerFrame.getInstance();
        String currentTitleText = StringUtils.stripNonValidXMLCharacters(this.titleField.getText());
        if (currentTitleText == null) {
            currentTitleText = "";
        }
        if (this.newEntry || !currentTitleText.equalsIgnoreCase(this.originalTitle)) {
            for (Entry entry : parent.getModel().getEntries().getEntry()) {
                if (currentTitleText.equalsIgnoreCase(entry.getTitle())) {
                    titleIsOk = false;
                    break;
                }
            }
        }
        return titleIsOk;
    }
}
