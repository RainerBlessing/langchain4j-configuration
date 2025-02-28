package com.aimitjava.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Configuration management class that handles loading properties from
 * environment variables and properties files with a defined precedence.
 */
public class Configuration {
    private static final String DEFAULT_PROPERTIES_FILE = "application.properties";
    protected static Configuration instance;
    protected final Properties properties;
    protected final EnvironmentProvider environmentProvider;
    private final String propertiesFile;

    /**
     * Creates a Configuration instance with default properties file.
     */
    protected Configuration() {
        this(DEFAULT_PROPERTIES_FILE, EnvironmentProvider.getInstance());
    }

    /**
     * Creates a Configuration instance with a custom properties file.
     *
     * @param propertiesFile The properties file to load
     */
    public Configuration(String propertiesFile) {
        this(propertiesFile, EnvironmentProvider.getInstance());
    }

    /**
     * Creates a Configuration instance with custom properties file and environment provider.
     * Primarily used for testing.
     *
     * @param propertiesFile The properties file to load
     * @param environmentProvider The environment provider to use
     */
    Configuration(String propertiesFile, EnvironmentProvider environmentProvider) {
        this.propertiesFile = propertiesFile;
        this.environmentProvider = environmentProvider;
        this.properties = new Properties();
        loadProperties();
    }

    /**
     * Gets the singleton instance of Configuration.
     *
     * @return The singleton instance
     */
    public static Configuration getInstance() {
        if (instance == null) {
            instance = new Configuration();
        }
        return instance;
    }

    /**
     * Creates a new instance with a specific properties file.
     *
     * @param propertiesFile The properties file to use
     * @return A new Configuration instance
     */
    public static Configuration fromFile(String propertiesFile) {
        return new Configuration(propertiesFile);
    }

    /**
     * Resets the singleton instance (for testing).
     */
    static void reset() {
        instance = null;
    }

    private void loadProperties() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(propertiesFile)) {
            if (input != null) {
                properties.load(input);
            }
        } catch (IOException e) {
            System.err.println("Warning: Could not load " + propertiesFile);
        }
    }

    /**
     * Gets a property value with environment variable precedence.
     *
     * @param key The property key
     * @return The property value or null if not found
     */
    public String getProperty(String key) {
        // Environment variables take precedence over properties file
        String envValue = environmentProvider.getEnv(key);
        if (envValue != null && !envValue.trim().isEmpty()) {
            return envValue;
        }

        return properties.getProperty(key);
    }

    /**
     * Gets a property value with a default fallback.
     *
     * @param key The property key
     * @param defaultValue The default value if property is not found
     * @return The property value or defaultValue if not found
     */
    public String getProperty(String key, String defaultValue) {
        String value = getProperty(key);
        return value != null ? value : defaultValue;
    }

    /**
     * Gets an integer property value.
     *
     * @param key The property key
     * @param defaultValue The default value if property is not found or not a valid integer
     * @return The property value as an integer or defaultValue
     */
    public int getIntProperty(String key, int defaultValue) {
        String value = getProperty(key);
        if (value == null) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Gets a double property value.
     *
     * @param key The property key
     * @param defaultValue The default value if property is not found or not a valid double
     * @return The property value as a double or defaultValue
     */
    public double getDoubleProperty(String key, double defaultValue) {
        String value = getProperty(key);
        if (value == null) {
            return defaultValue;
        }

        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Gets a boolean property value.
     *
     * @param key The property key
     * @param defaultValue The default value if property is not found
     * @return The property value as a boolean or defaultValue
     */
    public boolean getBooleanProperty(String key, boolean defaultValue) {
        String value = getProperty(key);
        if (value == null) {
            return defaultValue;
        }

        return Boolean.parseBoolean(value);
    }

    /**
     * Checks if a property exists.
     *
     * @param key The property key
     * @return true if the property exists in environment or properties file
     */
    public boolean hasProperty(String key) {
        String envValue = environmentProvider.getEnv(key);
        if (envValue != null && !envValue.trim().isEmpty()) {
            return true;
        }

        return properties.containsKey(key);
    }
}