package com.aimitjava.config;

public class EnvironmentProvider {
    private static EnvironmentProvider instance;
    private final EnvironmentReader environmentReader;

    // Konstruktor mit Dependency Injection
    EnvironmentProvider(EnvironmentReader reader) {
        this.environmentReader = reader;
    }

    public EnvironmentProvider() {
        this(new SystemEnvironmentReader());
    }

    // Statische Fabrikmethode mit Standardimplementierung
    public static EnvironmentProvider getInstance() {
        if (instance == null) {
            instance = new EnvironmentProvider(new SystemEnvironmentReader());
        }
        return instance;
    }

    // Interface für Testbarkeit
    public interface EnvironmentReader {
        String getEnvironmentVariable(String key);
    }

    // Produktive Implementierung
    public static class SystemEnvironmentReader implements EnvironmentReader {
        @Override
        public String getEnvironmentVariable(String key) {
            return System.getenv(key);
        }
    }

    public String getEnv(String key) {
        return environmentReader.getEnvironmentVariable(key);
    }

    // Für Testzwecke
    public static void setInstance(EnvironmentProvider provider) {
        instance = provider;
    }

    public static void reset() {
        instance = null;
    }
}