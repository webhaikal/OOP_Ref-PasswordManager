import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for displaying message dialogs.
 *
 * @author Haikal Izzuddin
 *
 */
final class MessageDialog extends JDialog implements ActionListener {

    private static final Logger LOG = Logger.getLogger(MessageDialog.class.getName());

    private static final int DEFAULT_OPTION = -1;
    static final int YES_NO_OPTION = 0;
    static final int YES_NO_CANCEL_OPTION = 1;
    private static final int OK_CANCEL_OPTION = 2;

    static final int YES_OPTION = 0;
    private static final int OK_OPTION = 0;
    static final int NO_OPTION = 1;
    private static final int CANCEL_OPTION = 2;
    private static final int CLOSED_OPTION = -1;

    private int selectedOption;

    private MessageDialog(final Dialog parent, final Object message, final String title, int optionType) {
        super(parent);
        initializeDialog(parent, message, title, optionType);
    }

    private MessageDialog(final Frame parent, final Object message, final String title, int optionType) {
        super(parent);
        initializeDialog(parent, message, title, optionType);
    }

    private void initializeDialog(final Component parent, final Object message, final String title, int optionType) {
        setModal(true);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(title);
        this.selectedOption = CLOSED_OPTION;

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        JButton defaultButton;
        switch (optionType) {
            case YES_NO_OPTION:
                defaultButton = createButton("Yes", YES_OPTION);
                buttonPanel.add(defaultButton);
                buttonPanel.add(createButton("No", NO_OPTION));
                break;
            case YES_NO_CANCEL_OPTION:
                defaultButton = createButton("Yes", YES_OPTION);
                buttonPanel.add(defaultButton);
                buttonPanel.add(createButton("No", NO_OPTION));
                buttonPanel.add(createButton("Cancel", CANCEL_OPTION));
                break;
            case OK_CANCEL_OPTION:
                defaultButton = createButton("OK", OK_OPTION);
                buttonPanel.add(defaultButton);
                buttonPanel.add(createButton("Cancel", CANCEL_OPTION));
                break;
            default:
                defaultButton = createButton("OK", OK_OPTION);
                buttonPanel.add(defaultButton);
                break;
        }
        getRootPane().setDefaultButton(defaultButton);

        JPanel mainPanel = new JPanel(new BorderLayout(5, 0));

        float widthMultiplier;
        JPanel messagePanel = new JPanel(new BorderLayout());
        if (message instanceof JScrollPane) {
            widthMultiplier = 1.0f;
            messagePanel.add((Component) message, BorderLayout.CENTER);
        } else if (message instanceof Component) {
            widthMultiplier = 1.5f;
            messagePanel.add((Component) message, BorderLayout.NORTH);
        } else {
            widthMultiplier = 1.0f;
            messagePanel.setBorder(new EmptyBorder(10, 0, 10, 10));
            messagePanel.add(new JLabel("<html>" + String.valueOf(message)
                    .replaceAll("\\n", "<br />") + "</html>"), BorderLayout.CENTER);
        }
        mainPanel.add(messagePanel, BorderLayout.CENTER);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        getContentPane().add(mainPanel);
        setResizable(false);
        pack();
        setSize((int) (getWidth() * widthMultiplier), getHeight());
        setLocationRelativeTo(parent);
        setVisible(true);
    }

    private JButton createButton(String name, int option) {
        JButton button = new JButton(name);
        button.setMnemonic(name.charAt(0));
        button.setActionCommand(String.valueOf(option));
        button.addActionListener(this);
        return button;
    }

    private int getSelectedOption() {
        return this.selectedOption;
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        this.selectedOption = Integer.parseInt(event.getActionCommand());
        dispose();
    }

    private static void showMessageDialog(final Component parent, final Object message, final String title) {
        showMessageDialog(parent, message, title, DEFAULT_OPTION);
    }

    private static int showMessageDialog(final Component parent, final Object message, final String title, int optionType) {
        int ret = CLOSED_OPTION;
        MessageDialog dialog = null;
        if (parent instanceof Frame) {
            dialog = new MessageDialog((Frame) parent, message, title, optionType);
        } else if (parent instanceof Dialog) {
            dialog = new MessageDialog((Dialog) parent, message, title, optionType);
        }
        if (dialog != null) {
            ret = dialog.getSelectedOption();
        }
        return ret;
    }

    /**
     * Shows a warning message.
     *
     * @param parent parent component
     * @param message dialog message
     */
    static void showWarningMessage(final Component parent, final String message) {
        showMessageDialog(parent, message, "Warning");
    }

    /**
     * Shows an error message.
     *
     * @param parent parent component
     * @param message dialog message
     */
    static void showErrorMessage(final Component parent, final String message) {
        showMessageDialog(parent, message, "Error");
    }

    /**
     * Shows an information message.
     *
     * @param parent parent component
     * @param message dialog message
     */
    static void showInformationMessage(final Component parent, final String message) {
        showMessageDialog(parent, message, "Information");
    }

    /**
     * Shows a question dialog.
     *
     * @param parent parent component
     * @param message dialog message
     * @param optionType question type
     * @return selected option
     */
    static int showQuestionMessage(final Component parent, final String message, final int optionType) {
        return showMessageDialog(parent, message, "Confirmation", optionType);
    }

    /**
     * Shows a password dialog.
     *
     * @param parent parent component
     * @param confirm password confirmation
     * @return the password
     */
    static byte[] showPasswordDialog(final Component parent, final boolean confirm) {
        JPanel panel = new JPanel();
        panel.add(new JLabel("Password:"));
        final JPasswordField password = TextComponentFactory.newPasswordField();
        panel.add(password);
        JPasswordField repeat = null;
        if (confirm) {
            repeat = TextComponentFactory.newPasswordField();
            panel.add(new JLabel("Repeat:"));
            panel.add(repeat);
        }
        panel.setLayout(new SpringLayout());
        SpringUtilities.makeCompactGrid(panel, confirm ? 2 : 1, 2, 5, 5, 5, 5);
        boolean notCorrect = true;

        while (notCorrect) {
            int option = showMessageDialog(parent, panel, "Enter Password", OK_CANCEL_OPTION);
            if (option == OK_OPTION) {
                if (password.getPassword().length == 0) {
                    showWarningMessage(parent, "Please enter a password.");
                } else if (confirm && !Arrays.equals(password.getPassword(), repeat.getPassword())) {
                    showWarningMessage(parent, "Password and repeated password are not identical.");
                } else {
                    notCorrect = false;
                }
            } else {
                return null;
            }
        }

        byte[] passwordHash = null;
        try {
            passwordHash = CryptUtils.getPKCS5Sha256Hash(password.getPassword());
        } catch (Exception e) {
            showErrorMessage(parent,
                    "Cannot generate password hash:\n" + StringUtils.stripString(e.getMessage()) + "\n\nOpening and saving files are not possible!");
        }
        return passwordHash;
    }

    /**
     * Get resource as string
     */
    private static String getResourceAsString(String name) {
        StringBuilder builder = new StringBuilder();
        BufferedReader bufferedReader = null;
        try {
            InputStream is = MessageDialog.class.getClassLoader().getResourceAsStream("resources/" + name);
            if (is != null) {
                bufferedReader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            }
            String line;
            if (bufferedReader != null) {
                while ((line = bufferedReader.readLine()) != null) {
                    builder.append(line).append('\n');
                }
            }
        } catch (Exception e) {
            LOG.log(Level.WARNING, String.format("An error occurred during reading resource [%s]", name), e);
        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (IOException e) {
                LOG.log(Level.WARNING, String.format("An error occurred during closing reader for resource [%s]", name), e);
            }
        }
        return builder.toString();
    }

    /**
     * Shows a text file from the class path.
     *
     * @param parent parent component
     * @param title window title
     * @param textFile text file name
     */
    public static void showTextFile(final Component parent, final String title, final String textFile) {
        JTextArea area = TextComponentFactory.newTextArea(getResourceAsString(textFile));
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(area);
        scrollPane.setPreferredSize(new Dimension(600, 400));
        showMessageDialog(parent, scrollPane, title, DEFAULT_OPTION);
    }
}
