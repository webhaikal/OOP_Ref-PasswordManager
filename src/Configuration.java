import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Configuration {

    private static final Logger LOG = Logger.getLogger(Configuration.class.getName());
    private static volatile Configuration INSTANCE;
    private final Properties properties = new Properties();

    private Configuration() {
        try {
            File filePath = new File("passwordmanager.properties");
            if (filePath.exists() && filePath.isFile()) {
                InputStream is = new FileInputStream(filePath);
                properties.load(is);
                is.close();
            }
        } catch (Exception e) {
            LOG.log(Level.WARNING, "An error occurred during loading configuration.", e);
        }
    }

    private <T> T getValue(String key, T defaultValue, Class<T> type) {
        T value = defaultValue;
        String prop = properties.getProperty(key);
        if (prop != null) {
            try {
                value = type.getConstructor(String.class).newInstance(prop);
            } catch (Exception e) {
                LOG.log(Level.WARNING, String.format("Could not parse value as [%s] for key [%s]", type.getName(), key));
            }
        }
        return value;
    }

    public Boolean is(String key, Boolean defaultValue) {
        return getValue(key, defaultValue, Boolean.class);
    }

    public Integer getInteger(String key, Integer defaultValue) {
        return getValue(key, defaultValue, Integer.class);
    }

    public String get(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public static Configuration getInstance() {
        if (INSTANCE == null) {
            synchronized (Configuration.class) {
                if (INSTANCE == null) {
                    INSTANCE = new Configuration();
                }
            }
        }
        return INSTANCE;
    }
}