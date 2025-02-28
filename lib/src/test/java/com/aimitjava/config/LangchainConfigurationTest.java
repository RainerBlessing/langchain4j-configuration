package com.aimitjava.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class LangchainConfigurationTest {

    @Mock
    private EnvironmentProvider mockEnvironmentProvider;

    private LangchainConfiguration langchainConfig;
    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
        Configuration.reset();
        EnvironmentProvider.reset();
    }

    @Test
    void testGetInstance() {
        // Test singleton creation
        LangchainConfiguration instance1 = (LangchainConfiguration) LangchainConfiguration.getInstance();
        LangchainConfiguration instance2 = (LangchainConfiguration) LangchainConfiguration.getInstance();

        assertThat("getInstance should not return null", instance1, is(notNullValue()));
        assertThat("getInstance should return the same instance", instance1, is(sameInstance(instance2)));
    }

    @Test
    void testConstructorWithCustomPropertiesFile() {
        String propertiesFile = "test-langchain.properties";
        langchainConfig = new LangchainConfiguration(propertiesFile);

        assertThat("Configuration should use the specified properties file",
                getPropertiesFileField(langchainConfig), is(equalTo(propertiesFile)));
    }

    @Test
    void testGetOpenAiApiKeyFromEnvironment() {
        // Setup
        mockEnvironmentProviderAndCreate("OPENAI_API_KEY", "env-api-key-value");

        // Test
        String apiKey = langchainConfig.getOpenAiApiKey();

        // Verify
        assertThat("API key should be retrieved from environment",
                apiKey, is(equalTo("env-api-key-value")));
        verify(mockEnvironmentProvider).getEnv("OPENAI_API_KEY");
    }

    @Test
    void testGetOpenAiApiKeyFromProperties() {
        // Setup
        Properties properties = new Properties();
        properties.setProperty("openai.api.key", "property-api-key-value");

        mockEnvironmentProviderAndCreate(null, null);
        setPropertiesField(langchainConfig, properties);

        // Test
        String apiKey = langchainConfig.getOpenAiApiKey();

        // Verify
        assertThat("API key should be retrieved from properties when env is not available",
                apiKey, is(equalTo("property-api-key-value")));
        verify(mockEnvironmentProvider).getEnv("OPENAI_API_KEY");
    }

    @Test
    void testGetOpenAiApiKeyEnvironmentPrecedence() {
        // Setup
        Properties properties = new Properties();
        properties.setProperty("openai.api.key", "property-api-key-value");

        mockEnvironmentProviderAndCreate("OPENAI_API_KEY", "env-api-key-value");
        setPropertiesField(langchainConfig, properties);

        // Test
        String apiKey = langchainConfig.getOpenAiApiKey();

        // Verify - Environment variable should take precedence
        assertThat("Environment variable should take precedence over properties file",
                apiKey, is(equalTo("env-api-key-value")));
    }

    @Test
    void testGetOpenAiModelName() {
        // Setup
        Properties properties = new Properties();
        properties.setProperty("openai.model.name", "gpt-4");

        mockEnvironmentProviderAndCreate(null, null);
        setPropertiesField(langchainConfig, properties);

        // Test
        String modelName = langchainConfig.getOpenAiModelName();

        // Verify
        assertThat("Model name should be retrieved from properties",
                modelName, is(equalTo("gpt-4")));
    }

    @Test
    void testGetOpenAiTemperatureFromProperties() {
        // Setup
        Properties properties = new Properties();
        properties.setProperty("openai.temperature", "0.5");

        mockEnvironmentProviderAndCreate(null, null);
        setPropertiesField(langchainConfig, properties);

        // Test
        double temperature = langchainConfig.getOpenAiTemperature();

        // Verify
        assertThat("Temperature should be parsed from properties",
                temperature, is(closeTo(0.5, 0.001)));
    }

    @Test
    void testGetOpenAiTemperatureDefaultValue() {
        // Setup
        mockEnvironmentProviderAndCreate(null, null);

        // Test
        double temperature = langchainConfig.getOpenAiTemperature();

        // Verify - Should return default value 0.7
        assertThat("Default temperature should be 0.7",
                temperature, is(closeTo(0.7, 0.001)));
    }

    @Test
    void testGetOpenAiTemperatureInvalidValue() {
        // Setup
        Properties properties = new Properties();
        properties.setProperty("openai.temperature", "invalid");

        mockEnvironmentProviderAndCreate(null, null);
        setPropertiesField(langchainConfig, properties);

        // Test & Verify - Should throw NumberFormatException
        assertThrows(NumberFormatException.class,
                () -> langchainConfig.getOpenAiTemperature(),
                "Invalid temperature value should throw NumberFormatException");
    }

    // Helper methods
    private void mockEnvironmentProviderAndCreate(String key, String value) {
        mockEnvironmentProvider = mock(EnvironmentProvider.class);
        when(mockEnvironmentProvider.getEnv(key != null ? key : anyString())).thenReturn(value);

        // Den angepassten Konstruktor mit dem mockEnvironmentProvider verwenden
        langchainConfig = new LangchainConfiguration("test.properties", mockEnvironmentProvider);

        // Properties direkt setzen, falls nötig
        if (langchainConfig.properties == null) {
            setPropertiesField(langchainConfig, new Properties());
        }
    }

    // Reflection helpers to access private fields
    private String getPropertiesFileField(LangchainConfiguration config) {
        try {
            java.lang.reflect.Field field = Configuration.class.getDeclaredField("propertiesFile");
            field.setAccessible(true);
            return (String) field.get(config);
        } catch (Exception e) {
            throw new RuntimeException("Error accessing propertiesFile field", e);
        }
    }

    private void setPropertiesField(LangchainConfiguration config, Properties properties) {
        try {
            java.lang.reflect.Field field = Configuration.class.getDeclaredField("properties");
            field.setAccessible(true);
            field.set(config, properties);
        } catch (Exception e) {
            throw new RuntimeException("Error setting properties field", e);
        }
    }
}