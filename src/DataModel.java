import java.util.ArrayList;
import java.util.List;

/**
 * Data model of the application data.
 *
 * @author Gabor_Bata
 *
 */
public class DataModel {

    private static volatile DataModel INSTANCE;

    private Entries entries = new Entries();
    private String fileName = null;
    private transient byte[] password = null;
    private boolean modified = false;

    private DataModel() {
        // not intended to be instantiated
    }

    /**
     * Gets the DataModel singleton instance.
     *
     * @return instance of the DataModel
     */
    public static DataModel getInstance() {
        if (INSTANCE == null) {
            synchronized (DataModel.class) {
                if (INSTANCE == null) {
                    INSTANCE = new DataModel();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Gets list of entries.
     *
     * @return list of entries
     */
    public final Entries getEntries() {
        return this.entries;
    }

    /**
     * Sets list of entries.
     *
     * @param entries entries
     */
    public final void setEntries(final Entries entries) {
        this.entries = entries;
    }

    /**
     * Gets the file name for the data model.
     *
     * @return file name
     */
    public final String getFileName() {
        return this.fileName;
    }

    /**
     * Sets the file name for the data model.
     *
     * @param fileName file name
     */
    public final void setFileName(final String fileName) {
        this.fileName = fileName;
    }

    /**
     * Gets the modified state of the data model.
     *
     * @return modified state of the data model
     */
    public final boolean isModified() {
        return this.modified;
    }

    /**
     * Sets the modified state of the data model.
     *
     * @param modified modified state
     */
    public final void setModified(final boolean modified) {
        this.modified = modified;
    }

    public byte[] getPassword() {
        return this.password;
    }

    public void setPassword(byte[] password) {
        this.password = password;
    }

    /**
     * Clears all fields of the data model.
     */
    public final void clear() {
        this.entries.getEntry().clear();
        this.fileName = null;
        this.password = null;
        this.modified = false;
    }

    /**
     * Gets the list of entry titles.
     *
     * @return list of entry titles
     */
    public List<String> getTitles() {
        List<String> list = new ArrayList<>(this.entries.getEntry().size());
        for (Entry entry : this.entries.getEntry()) {
            list.add(entry.getTitle());
        }
        return list;
    }

    /**
     * Gets entry index by title.
     *
     * @param title entry title
     * @return entry index
     */
    private int getEntryIndexByTitle(String title) {
        return getTitles().indexOf(title);
    }

    /**
     * Gets entry by title.
     *
     * @param title entry title
     * @return entry (can be null)
     */
    public Entry getEntryByTitle(String title) {
        int entryIndex = getEntryIndexByTitle(title);
        if (entryIndex != -1) {
            return this.entries.getEntry().get(entryIndex);
        }
        return null;
    }
}
