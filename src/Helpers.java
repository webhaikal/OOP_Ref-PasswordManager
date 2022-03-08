import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

final class DocumentHelper {

    class DocumentProcessException extends Exception {
        DocumentProcessException(String message) {
            super("Cannot process document due to the following exception:\n" + message);
        }
    }

    /**
     * File name to read/write.
     */
    private final String fileName;

    /**
     * Key for encryption.
     */
    private final byte[] key;

    /**
     * Converter between document objects and streams representing XMLs
     */
    private static final XmlConverter<Entries> CONVERTER = new XmlConverter<>(Entries.class);

    /**
     * Creates a DocumentHelper instance.
     *
     * @param fileName file name
     * @param key key for encryption
     */
    private DocumentHelper(final String fileName, final byte[] key) {
        this.fileName = fileName;
        this.key = key;
    }

    /**
     * Creates a document helper with no encryption.
     *
     * @param fileName file name
     * @return a new DocumentHelper object
     */
    static DocumentHelper newInstance(final String fileName) {
        return new DocumentHelper(fileName, null);
    }

    /**
     * Creates a document helper with encryption.
     *
     * @param fileName file name
     * @param key key for encryption
     * @return a new DocumentHelper object
     */
    static DocumentHelper newInstance(final String fileName, final byte[] key) {
        return new DocumentHelper(fileName, key);
    }

    /**
     * Reads and XML file to an {@link Entries} object.
     *
     * @return the document
     * @throws FileNotFoundException if file is not exists
     * @throws IOException when I/O error occurred
     * @throws DocumentProcessException when file format or password is incorrect
     */
    Entries readDocument() throws IOException, DocumentProcessException {
        InputStream inputStream = null;
        Entries entries;
        try {
            if (this.key == null) {
                inputStream = new FileInputStream(this.fileName);
            } else {
                inputStream = new GZIPInputStream(new CryptInputStream(new BufferedInputStream(new FileInputStream(this.fileName)), this.key));
            }
            entries = CONVERTER.read(inputStream);
        } catch (Exception e) {
            throw new DocumentProcessException(StringUtils.stripString(e.getMessage()));
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return entries;
    }

    /**
     * Writes a document into an XML file.
     *
     * @param document the document
     * @throws DocumentProcessException when document format is incorrect
     * @throws IOException when I/O error occurred
     */
    public void writeDocument(final Entries document) throws DocumentProcessException, IOException {
        OutputStream outputStream = null;
        try {
            if (this.key == null) {
                outputStream = new FileOutputStream(this.fileName);
            } else {
                outputStream = new GZIPOutputStream(new CryptOutputStream(new BufferedOutputStream(new FileOutputStream(this.fileName)), this.key));
            }
            CONVERTER.write(document, outputStream);
        } catch (Exception e) {
            throw new DocumentProcessException(StringUtils.stripString(e.getMessage()));
        } finally {
            if (outputStream != null) {
                outputStream.close();
            }
        }
    }
}

final class EntryHelper {

    private EntryHelper() {
        // not intended to be instantiated
    }

    /**
     * Deletes an entry.
     *
     * @param parent parent component
     */
    static void deleteEntry(PasswordManagerFrame parent) {
        if (parent.getEntryTitleList().getSelectedIndex() == -1) {
            MessageDialog.showWarningMessage(parent, "Please select an entry.");
            return;
        }
        int option = MessageDialog.showQuestionMessage(parent, "Do you really want to delete this entry?",
                MessageDialog.YES_NO_OPTION);
        if (option == MessageDialog.YES_OPTION) {
            String title = (String) parent.getEntryTitleList().getSelectedValue();
            parent.getModel().getEntries().getEntry().remove(parent.getModel().getEntryByTitle(title));
            parent.getModel().setModified(true);
            parent.refreshFrameTitle();
            parent.refreshEntryTitleList(null);
        }
    }

    /**
     * Duplicates an entry.
     *
     * @param parent parent component
     */
    static void duplicateEntry(PasswordManagerFrame parent) {
        if (parent.getEntryTitleList().getSelectedIndex() == -1) {
            MessageDialog.showWarningMessage(parent, "Please select an entry.");
            return;
        }
        String title = (String) parent.getEntryTitleList().getSelectedValue();
        Entry oldEntry = parent.getModel().getEntryByTitle(title);
        EntryDialog ed = new EntryDialog(parent, "Duplicate Entry", oldEntry, true);
        if (ed.getFormData() != null) {
            parent.getModel().getEntries().getEntry().add(ed.getFormData());
            parent.getModel().setModified(true);
            parent.refreshFrameTitle();
            parent.refreshEntryTitleList(ed.getFormData().getTitle());
        }
    }

    /**
     * Edits the entry.
     *
     * @param parent parent component
     */
    static void editEntry(PasswordManagerFrame parent) {
        if (parent.getEntryTitleList().getSelectedIndex() == -1) {
            MessageDialog.showWarningMessage(parent, "Please select an entry.");
            return;
        }
        String title = (String) parent.getEntryTitleList().getSelectedValue();
        Entry oldEntry = parent.getModel().getEntryByTitle(title);
        EntryDialog ed = new EntryDialog(parent, "Edit Entry", oldEntry, false);
        if (ed.getFormData() != null) {
            parent.getModel().getEntries().getEntry().remove(oldEntry);
            parent.getModel().getEntries().getEntry().add(ed.getFormData());
            parent.getModel().setModified(true);
            parent.refreshFrameTitle();
            parent.refreshEntryTitleList(ed.getFormData().getTitle());
        }
    }

    /**
     * Adds an entry.
     *
     * @param parent parent component
     */
    static void addEntry(PasswordManagerFrame parent) {
        EntryDialog ed = new EntryDialog(parent, "Add New Entry", null, true);
        if (ed.getFormData() != null) {
            parent.getModel().getEntries().getEntry().add(ed.getFormData());
            parent.getModel().setModified(true);
            parent.refreshFrameTitle();
            parent.refreshEntryTitleList(ed.getFormData().getTitle());
        }
    }

    /**
     * Gets the selected entry.
     *
     * @param parent the parent frame
     * @return the entry or null
     */
    static Entry getSelectedEntry(PasswordManagerFrame parent) {
        if (parent.getEntryTitleList().getSelectedIndex() == -1) {
            MessageDialog.showWarningMessage(parent, "Please select an entry.");
            return null;
        }
        return parent.getModel().getEntryByTitle((String) parent.getEntryTitleList().getSelectedValue());
    }

    /**
     * Copy entry field value to clipboard.
     *
     * @param parent the parent frame
     * @param content the content to copy
     */
    static void copyEntryField(PasswordManagerFrame parent, String content) {
        try {
            ClipboardUtils.setClipboardContent(content);
        } catch (Exception e) {
            MessageDialog.showErrorMessage(parent, e.getMessage());
        }
    }
}

final class FileHelper {

    private FileHelper() {
        // not intended to be instantiated
    }

    /**
     * Creates a new entries document.
     *
     * @param parent parent component
     */
    static void createNew(final PasswordManagerFrame parent) {
        if (parent.getModel().isModified()) {
            int option = MessageDialog.showQuestionMessage(
                    parent,
                    "The current file has been modified.\n"
                            + "Do you want to save the changes before closing?",
                    MessageDialog.YES_NO_CANCEL_OPTION);
            if (option == MessageDialog.YES_OPTION) {
                saveFile(parent, false, result -> {
                    if (result) {
                        parent.clearModel();
                        parent.getSearchPanel().setVisible(false);
                        parent.refreshAll();
                    }
                });
                return;
            } else if (option != MessageDialog.NO_OPTION) {
                return;
            }
        }
        parent.clearModel();
        parent.getSearchPanel().setVisible(false);
        parent.refreshAll();
    }

    /**
     * Shows a file chooser dialog and exports the file.
     *
     * @param parent parent component
     */
    static void exportFile(final PasswordManagerFrame parent) {
        MessageDialog.showWarningMessage(parent,
                "Please note that all data will be stored unencrypted.\nMake sure you keep the exported file in a secure location.");
        File file = showFileChooser(parent, "Export", "xml", "XML Files (*.xml)");
        if (file == null) {
            return;
        }
        final String fileName = checkExtension(file.getPath(), "xml");
        if (checkFileOverwrite(fileName, parent)) {
            return;
        }
        Worker worker = new Worker(parent) {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    DocumentHelper.newInstance(fileName).writeDocument(parent.getModel().getEntries());
                } catch (Throwable e) {
                    throw new Exception("An error occurred during the export operation:\n" + e.getMessage());
                }
                return null;
            }
        };
        worker.execute();
    }

    /**
     * Shows a file chooser dialog and exports the file.
     *
     * @param parent parent component
     */
    static void importFile(final PasswordManagerFrame parent) {
        File file = showFileChooser(parent, "Import", "xml", "XML Files (*.xml)");
        if (file == null) {
            return;
        }
        final String fileName = file.getPath();
        if (parent.getModel().isModified()) {
            int option = MessageDialog.showQuestionMessage(
                    parent,
                    "The current file has been modified.\n"
                            + "Do you want to save the changes before closing?",
                    MessageDialog.YES_NO_CANCEL_OPTION);
            if (option == MessageDialog.YES_OPTION) {
                saveFile(parent, false, result -> {
                    if (result) {
                        doImportFile(fileName, parent);
                    }
                });
                return;
            } else if (option != MessageDialog.NO_OPTION) {
                return;
            }
        }
        doImportFile(fileName, parent);
    }

    /**
     * Imports the given file.
     *
     * @param fileName file name
     * @param parent parent component
     */
    private static void doImportFile(final String fileName, final PasswordManagerFrame parent) {
        Worker worker = new Worker(parent) {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    parent.getModel().setEntries(DocumentHelper.newInstance(fileName).readDocument());
                    parent.getModel().setModified(true);
                    parent.getModel().setFileName(null);
                    parent.getModel().setPassword(null);
                    parent.getSearchPanel().setVisible(false);
                } catch (Throwable e) {
                    throw new Exception("An error occurred during the import operation:\n" + e.getMessage());
                }
                return null;
            }
        };
        worker.execute();
    }

    /**
     * Shows a file chooser dialog and saves a file.
     *
     * @param parent parent component
     * @param saveAs normal 'Save' dialog or 'Save as'
     */
    static void saveFile(final PasswordManagerFrame parent, final boolean saveAs) {
        saveFile(parent, saveAs, result -> {
            //default empty call
        });
    }

    /**
     * Shows a file chooser dialog and saves a file.
     *
     * @param parent parent component
     * @param saveAs normal 'Save' dialog or 'Save as'
     * @param callback callback function with the result; the result is {@code true} if the file
     * successfully saved; otherwise {@code false}
     */
    static void saveFile(final PasswordManagerFrame parent, final boolean saveAs, final Callback callback) {
        final String fileName;
        if (saveAs || parent.getModel().getFileName() == null) {
            File file = showFileChooser(parent, "Save", "crypt", "Encrypted Data Files (*.crypt)");
            if (file == null) {
                callback.call(false);
                return;
            }
            fileName = checkExtension(file.getPath(), "crypt");
            if (checkFileOverwrite(fileName, parent)) {
                callback.call(false);
                return;
            }
        } else {
            fileName = parent.getModel().getFileName();
        }

        final byte[] password;
        if (parent.getModel().getPassword() == null) {
            password = MessageDialog.showPasswordDialog(parent, true);
            if (password == null) {
                callback.call(false);
                return;
            }
        } else {
            password = parent.getModel().getPassword();
        }
        Worker worker = new Worker(parent) {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    DocumentHelper.newInstance(fileName, password).writeDocument(parent.getModel().getEntries());
                    parent.getModel().setFileName(fileName);
                    parent.getModel().setPassword(password);
                    parent.getModel().setModified(false);
                } catch (Throwable e) {
                    throw new Exception("An error occurred during the save operation:\n" + e.getMessage());
                }
                return null;
            }

            @Override
            protected void done() {
                stopProcessing();
                boolean result = true;
                try {
                    get();
                } catch (Exception e) {
                    result = false;
                    showErrorMessage(e);
                }
                callback.call(result);
            }
        };
        worker.execute();
    }

    /**
     * Shows a file chooser dialog and opens a file.
     *
     * @param parent parent component
     */
    static void openFile(final PasswordManagerFrame parent) {
        final File file = showFileChooser(parent, "Open", "crypt", "Encrypted Data Files (*.crypt)");
        if (file == null) {
            return;
        }
        if (parent.getModel().isModified()) {
            int option = MessageDialog.showQuestionMessage(
                    parent,
                    "The current file has been modified.\n"
                            + "Do you want to save the changes before closing?",
                    MessageDialog.YES_NO_CANCEL_OPTION);
            if (option == MessageDialog.YES_OPTION) {
                saveFile(parent, false, result -> {
                    if (result) {
                        doOpenFile(file.getPath(), parent);
                    }
                });
                return;
            } else if (option != MessageDialog.NO_OPTION) {
                return;
            }
        }
        doOpenFile(file.getPath(), parent);
    }

    /**
     * Loads a file and fills the data model.
     *
     * @param fileName file name
     * @param parent parent component
     */
    static void doOpenFile(final String fileName, final PasswordManagerFrame parent) {
        parent.clearModel();
        if (fileName == null) {
            return;
        }
        final byte[] password = MessageDialog.showPasswordDialog(parent, false);
        if (password == null) {
            return;
        }
        Worker worker = new Worker(parent) {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    parent.getModel().setEntries(DocumentHelper.newInstance(fileName, password).readDocument());
                    parent.getModel().setFileName(fileName);
                    parent.getModel().setPassword(password);
                    parent.getSearchPanel().setVisible(false);
                } catch (FileNotFoundException e) {
                    throw e;
                } catch (IOException e) {
                    throw new Exception("An error occurred during the open operation.\nPlease check your password.");
                } catch (Throwable e) {
                    throw new Exception("An error occurred during the open operation:\n" + e.getMessage());
                }
                return null;
            }

            @Override
            protected void done() {
                stopProcessing();
                try {
                    get();
                } catch (Exception e) {
                    if (e.getCause() != null && e.getCause() instanceof FileNotFoundException) {
                        handleFileNotFound(parent, fileName, password);
                    } else {
                        showErrorMessage(e);
                    }
                }
            }
        };
        worker.execute();
    }

    /**
     * Handles file not found exception.
     *
     * @param parent parent frame
     * @param fileName file name
     * @param password password to create a new file
     */
    private static void handleFileNotFound(final PasswordManagerFrame parent, final String fileName, final byte[] password) {
        int option = MessageDialog.showQuestionMessage(parent, "File not found:\n" + StringUtils.stripString(fileName)
                + "\n\nDo you want to create the file?", MessageDialog.YES_NO_OPTION);
        if (option == MessageDialog.YES_OPTION) {
            Worker fileNotFoundWorker = new Worker(parent) {
                @Override
                protected Void doInBackground() throws Exception {
                    try {
                        DocumentHelper.newInstance(fileName, password).writeDocument(parent.getModel().getEntries());
                        parent.getModel().setFileName(fileName);
                        parent.getModel().setPassword(password);
                    } catch (Exception ex) {
                        throw new Exception("An error occurred during the open operation:\n" + ex.getMessage());
                    }
                    return null;
                }

            };
            fileNotFoundWorker.execute();
        }
    }

    /**
     * Shows a file chooser dialog.
     *
     * @param parent parent component
     * @param taskName name of the task
     * @param extension accepted file extension
     * @param description file extension description
     * @return a file object
     */
    private static File showFileChooser(final PasswordManagerFrame parent, final String taskName,
                                        final String extension, final String description) {
        File ret = null;
        JFileChooser fc = new JFileChooser("./");
        fc.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith("." + extension);
            }

            @Override
            public String getDescription() {
                return description;
            }
        });
        int returnVal = fc.showDialog(parent, taskName);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            ret = fc.getSelectedFile();
        }
        return ret;
    }

    /**
     * Checks if overwrite is accepted.
     *
     * @param fileName file name
     * @param parent parent component
     * @return {@code true} if overwrite is accepted; otherwise {@code false}
     */
    private static boolean checkFileOverwrite(String fileName, PasswordManagerFrame parent) {
        boolean overwriteAccepted = true;
        File file = new File(fileName);
        if (file.exists()) {
            int option = MessageDialog.showQuestionMessage(parent, "File is already exists:\n" + StringUtils.stripString(fileName)
                    + "\n\nDo you want to overwrite?", MessageDialog.YES_NO_OPTION);
            if (option != MessageDialog.YES_OPTION) {
                overwriteAccepted = false;
            }
        }
        return !overwriteAccepted;
    }

    /**
     * Checks if the file name has the given extension
     *
     * @param fileName file name
     * @param extension extension
     * @return file name ending with the given extension
     */
    private static String checkExtension(final String fileName, final String extension) {
        String separator = fileName.endsWith(".") ? "" : ".";
        if (!fileName.toLowerCase().endsWith(separator + extension)) {
            return fileName + separator + extension;
        }
        return fileName;
    }
}