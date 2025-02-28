package com.aimitjava.config;

public class LangchainConfiguration extends Configuration{
    public LangchainConfiguration() {
        super();
    }

    public static Configuration getInstance() {
        if (instance == null) {
            instance = new LangchainConfiguration();
        }
        return instance;
    }

    public LangchainConfiguration(String propertiesFile) {
        super(propertiesFile);
    }

    public LangchainConfiguration(String propertiesFile, EnvironmentProvider environmentProvider) {
        super(propertiesFile, environmentProvider);
    }

    public String getOpenAiApiKey() {
        String key = environmentProvider.getEnv("OPENAI_API_KEY");
        if (key == null || key.trim().isEmpty()) {
            key = properties.getProperty("openai.api.key");
        }
        return key;
    }

    public String getOpenAiModelName() {
        return getProperty("openai.model.name");
    }

    public double getOpenAiTemperature() {
        String temp = getProperty("openai.temperature");
        return temp != null ? Double.parseDouble(temp) : 0.7;
    }
}
