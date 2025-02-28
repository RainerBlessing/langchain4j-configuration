package com.aimitjava.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.mockito.Mockito;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;

class ConfigurationTest {

    @Nested
    class UnitTests {
        private EnvironmentProvider.EnvironmentReader mockReader;
        private EnvironmentProvider environmentProvider;
        private Configuration configuration;

        @BeforeEach
        void setUp() {
            mockReader = Mockito.mock(EnvironmentProvider.EnvironmentReader.class);
            environmentProvider = new EnvironmentProvider(mockReader);
            EnvironmentProvider.setInstance(environmentProvider);
            Configuration.reset();
        }

        @AfterEach
        void tearDown() {
            EnvironmentProvider.reset();
            Configuration.reset();
        }

        @Test
        void shouldReturnDefaultValuesWhenPropertyNotFound() {
            // Given
            when(mockReader.getEnvironmentVariable(Mockito.anyString())).thenReturn(null);
            configuration = new Configuration("empty.properties", environmentProvider);

            // Then
            assertThat(configuration.getProperty("non.existent.property", "default"), is("default"));
            assertThat(configuration.getIntProperty("non.existent.property", 123), is(123));
            assertThat(configuration.getDoubleProperty("non.existent.property", 1.23), is(1.23));
            assertThat(configuration.getBooleanProperty("non.existent.property", true), is(true));
        }

        @Test
        void shouldHandleInvalidTypeConversions() {
            // Given
            when(mockReader.getEnvironmentVariable("invalid.int")).thenReturn("not-a-number");
            when(mockReader.getEnvironmentVariable("invalid.double")).thenReturn("not-a-double");
            configuration = new Configuration("empty.properties", environmentProvider);

            // Then
            assertThat(configuration.getIntProperty("invalid.int", 999), is(999));
            assertThat(configuration.getDoubleProperty("invalid.double", 9.99), is(9.99));
        }

        @Test
        void shouldPreferEnvironmentVariablesOverPropertiesFile() {
            // Given
            when(mockReader.getEnvironmentVariable("test.property")).thenReturn("env-value");
            configuration = new Configuration("test.properties", environmentProvider);

            // Then
            assertThat(configuration.getProperty("test.property"), is("env-value"));
        }
    }

    @Nested
    class IntegrationTests {
        private TestEnvironmentReader testReader;
        private EnvironmentProvider environmentProvider;
        private Configuration configuration;

        // Custom EnvironmentReader implementation for integration tests
        private static class TestEnvironmentReader implements EnvironmentProvider.EnvironmentReader {
            private final java.util.Map<String, String> env = new java.util.HashMap<>();
            private final EnvironmentProvider.EnvironmentReader systemReader;

            public TestEnvironmentReader() {
                this.systemReader = new EnvironmentProvider.SystemEnvironmentReader();
            }

            @Override
            public String getEnvironmentVariable(String key) {
                return env.containsKey(key) ? env.get(key) : systemReader.getEnvironmentVariable(key);
            }

            public void setEnv(String key, String value) {
                env.put(key, value);
            }

            public void clearEnv() {
                env.clear();
            }
        }

        @BeforeEach
        void setUp() {
            testReader = new TestEnvironmentReader();
            environmentProvider = new EnvironmentProvider(testReader);
            EnvironmentProvider.setInstance(environmentProvider);
            Configuration.reset();
        }

        @AfterEach
        void tearDown() {
            testReader.clearEnv();
            EnvironmentProvider.reset();
            Configuration.reset();
        }

        @Test
        void shouldLoadRealPropertiesFromFile() {
            // Given a real configuration with no environment overrides
            configuration = new Configuration("application.properties", environmentProvider);

            // Then - should load values from the actual properties file
            assertThat(configuration.getProperty("test.string.property"), is("test-value"));
            assertThat(configuration.getIntProperty("test.int.property", 0), is(42));
            assertThat(configuration.getDoubleProperty("test.double.property", 0.0), is(3.14));
            assertThat(configuration.getBooleanProperty("test.boolean.property", false), is(true));
        }

        @Test
        void shouldOverridePropertiesWithEnvironmentValues() {
            // Given - set environment variables to override properties
            testReader.setEnv("test.string.property", "environment-value");
            testReader.setEnv("test.int.property", "99");

            // When
            configuration = new Configuration("application.properties", environmentProvider);

            // Then - environment values should override file properties
            assertThat(configuration.getProperty("test.string.property"), is("environment-value"));
            assertThat(configuration.getIntProperty("test.int.property", 0), is(99));
            // These are still from the file since we didn't override them
            assertThat(configuration.getDoubleProperty("test.double.property", 0.0), is(3.14));
        }

        @Test
        void shouldHandleMultiplePropertyTypesFromEnvironment() {
            // Given - set values of different types in environment
            testReader.setEnv("custom.string", "string-value");
            testReader.setEnv("custom.int", "123");
            testReader.setEnv("custom.double", "2.718");
            testReader.setEnv("custom.boolean", "true");

            // When
            configuration = new Configuration("empty.properties", environmentProvider);

            // Then - all property types should be handled correctly
            assertThat(configuration.getProperty("custom.string"), is("string-value"));
            assertThat(configuration.getIntProperty("custom.int", 0), is(123));
            assertThat(configuration.getDoubleProperty("custom.double", 0.0), is(2.718));
            assertThat(configuration.getBooleanProperty("custom.boolean", false), is(true));
        }

        @Test
        void shouldLoadCustomPropertiesFile() {
            // Create a configuration with a custom properties file
            // We need to temporarily set the default environment provider
            EnvironmentProvider.setInstance(environmentProvider);

            // When
            configuration = Configuration.fromFile("application.properties");

            // Then - should load values from the specified file
            assertThat(configuration.getProperty("test.string.property"), is("test-value"));
            assertThat(configuration.getIntProperty("test.int.property", 0), is(42));
        }
    }
}