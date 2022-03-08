import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Random;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SpringLayout;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;

/**
 * Dialog for generating random passwords.
 *
 * @author Haikal Izzuddin
 *
 */
final class GeneratePasswordDialog extends JDialog implements ActionListener {

    /**
     * Characters for custom symbols generation.
     */
    private static final String SYMBOLS = "!\"#$%&'()*+,-./:;<=>?@[\\]^_{|}~";

    /**
     * Options for password generation.
     */
    private static final String[][] PASSWORD_OPTIONS = {
        {"Upper case letters (A-Z)", "ABCDEFGHIJKLMNOPQRSTUVWXYZ"},
        {"Lower case letters (a-z)", "abcdefghijklmnopqrstuvwxyz"},
        {"Numbers (0-9)", "0123456789"}
    };

    private JCheckBox[] checkBoxes;
    private JCheckBox customSymbolsCheck;

    private JTextField customSymbolsField;
    private JTextField passwordField;

    private JSpinner lengthSpinner;

    private String generatedPassword;

    private final Random random = CryptUtils.newRandomNumberGenerator();

    /**
     * Constructor of GeneratePasswordDialog.
     *
     * @param parent JFrame parent component
     */
    GeneratePasswordDialog(JFrame parent) {
        super(parent);
        initDialog(parent, false);
    }

    /**
     * Constructor of GeneratePasswordDialog.
     *
     * @param parent JDialog parent component
     */
    GeneratePasswordDialog(JDialog parent) {
        super(parent);
        initDialog(parent, true);
    }

    /**
     * Initializes the GeneratePasswordDialog instance.
     *
     * @param parent parent component
     * @param showAcceptButton if true then the dialog shows an "Accept" and "Cancel" button,
     * otherwise only a "Close" button
     *
     */
    private void initDialog(final Component parent, final boolean showAcceptButton) {
        setModal(true);
        setTitle("Generate Password");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.generatedPassword = null;

        JPanel lengthPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        JLabel lengthLabel = new JLabel("Password length:");
        lengthPanel.add(lengthLabel);

        int passwordGenerationLength = Configuration.getInstance().getInteger("default.password.generation.length", 14);
        if (passwordGenerationLength > 64) {
            passwordGenerationLength = 64;
        }
        if (passwordGenerationLength < 1) {
            passwordGenerationLength = 1;
        }

        this.lengthSpinner = new JSpinner(new SpinnerNumberModel(passwordGenerationLength, 1, 64, 1));
        lengthPanel.add(this.lengthSpinner);

        JPanel charactersPanel = new JPanel();
        charactersPanel.setBorder(new TitledBorder("Settings"));
        charactersPanel.add(lengthPanel);
        this.checkBoxes = new JCheckBox[PASSWORD_OPTIONS.length];
        for (int i = 0; i < PASSWORD_OPTIONS.length; i++) {
            this.checkBoxes[i] = new JCheckBox(PASSWORD_OPTIONS[i][0], true);
            charactersPanel.add(this.checkBoxes[i]);
        }
        this.customSymbolsCheck = new JCheckBox("Custom symbols");
        this.customSymbolsCheck.setActionCommand("custom_symbols_check");
        this.customSymbolsCheck.addActionListener(this);
        charactersPanel.add(this.customSymbolsCheck);
        this.customSymbolsField = TextComponentFactory.newTextField(SYMBOLS);
        this.customSymbolsField.setEditable(false);
        charactersPanel.add(this.customSymbolsField);

        charactersPanel.setLayout(new SpringLayout());
        SpringUtilities.makeCompactGrid(charactersPanel, 6, 1, 5, 5, 5, 5);

        JPanel passwordPanel = new JPanel(new BorderLayout());
        passwordPanel.setBorder(new TitledBorder("Generated password"));

        this.passwordField = TextComponentFactory.newTextField();
        passwordPanel.add(this.passwordField, BorderLayout.NORTH);
        JButton generateButton = new JButton("Generate");
        generateButton.setActionCommand("generate_button");
        generateButton.addActionListener(this);
        generateButton.setMnemonic(KeyEvent.VK_G);
        JPanel generateButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        generateButtonPanel.add(generateButton);
        passwordPanel.add(generateButtonPanel, BorderLayout.SOUTH);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton cancelButton;
        if (showAcceptButton) {
            JButton acceptButton = new JButton("Accept");
            acceptButton.setActionCommand("accept_button");
            acceptButton.setMnemonic(KeyEvent.VK_A);
            acceptButton.addActionListener(this);
            buttonPanel.add(acceptButton);

            cancelButton = new JButton("Cancel");
        } else {
            cancelButton = new JButton("Close");
        }

        cancelButton.setActionCommand("cancel_button");
        cancelButton.setMnemonic(KeyEvent.VK_C);
        cancelButton.addActionListener(this);
        buttonPanel.add(cancelButton);

        getContentPane().add(charactersPanel, BorderLayout.NORTH);
        getContentPane().add(passwordPanel, BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        setResizable(false);
        pack();
        setSize((int) (getWidth() * 1.5), getHeight());
        setLocationRelativeTo(parent);
        setVisible(true);
    }

    /**
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        if ("custom_symbols_check".equals(command)) {
            this.customSymbolsField.setEditable(((JCheckBox) e.getSource()).isSelected());
        } else if ("generate_button".equals(command)) {
            StringBuilder characterSetBuilder = new StringBuilder();
            for (int i = 0; i < PASSWORD_OPTIONS.length; i++) {
                if (this.checkBoxes[i].isSelected()) {
                    characterSetBuilder.append(PASSWORD_OPTIONS[i][1]);
                }
            }
            String characterSet = characterSetBuilder.toString();

            if (this.customSymbolsCheck.isSelected()) {
                characterSet += this.customSymbolsField.getText();
            }

            if (characterSet.isEmpty()) {
                MessageDialog.showWarningMessage(this, "Cannot generate password.\nPlease select a character set.");
                return;
            }

            StringBuilder generated = new StringBuilder();
            int passwordLength = Integer.parseInt(String.valueOf(this.lengthSpinner.getValue()));
            for (int i = 0; i < passwordLength; i++) {
                generated.append(characterSet.charAt(this.random.nextInt(characterSet.length())));
            }
            this.passwordField.setText(generated.toString());
        } else if ("accept_button".equals(command)) {
            this.generatedPassword = this.passwordField.getText();
            if (this.generatedPassword.isEmpty()) {
                MessageDialog.showWarningMessage(this, "Please generate a password.");
                return;
            }
            dispose();
        } else if ("cancel_button".equals(command)) {
            dispose();
        }
    }

    /**
     * Gets the generated password.
     *
     * @return if the password is not generated than the return value is {@code null}, otherwise the
     * generated password
     */
    String getGeneratedPassword() {
        return this.generatedPassword;
    }
}
