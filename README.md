# Java Configuration Manager

Eine flexible Java-Bibliothek zum Verwalten von Konfigurationen aus verschiedenen Quellen mit definierter Präzedenz.

## Features

- Lesen von Konfigurationswerten aus Properties-Dateien
- Lesen von Umgebungsvariablen mit höherer Priorität
- Typisierte Getter-Methoden für verschiedene Datentypen (String, Integer, Double, Boolean)
- Unterstützung für Default-Werte
- Singleton-Pattern für globale Konfiguration
- Spezielle Unterstützung für LangChain4j-Konfigurationen
- Testfreundliches Design durch Dependency Injection

## Installation

### Maven

```xml
<dependency>
    <groupId>com.aimitjava</groupId>
    <artifactId>config</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Gradle

```groovy
implementation 'com.aimitjava:config:1.0.0'
```

## Grundlegende Verwendung

### Standard-Konfiguration

Die Bibliothek lädt standardmäßig Werte aus `application.properties` im Classpath:

```java
// Zugriff auf die Singleton-Instanz
Configuration config = Configuration.getInstance();

// Abrufen von Konfigurationswerten
String dbUrl = config.getProperty("database.url");
int port = config.getIntProperty("server.port", 8080);
boolean debug = config.getBooleanProperty("app.debug", false);
```

### Benutzerdefinierte Properties-Datei

```java
// Laden einer benutzerdefinierten Properties-Datei
Configuration config = Configuration.fromFile("custom.properties");
```

### Umgebungsvariablen

Umgebungsvariablen haben Vorrang vor Werten aus der Properties-Datei:

```java
// Wird zuerst die Umgebungsvariable DATABASE_URL prüfen
// und nur wenn diese nicht existiert auf die Properties-Datei zurückgreifen
String dbUrl = config.getProperty("DATABASE_URL");
```

## LangChain4j-Unterstützung

Die Bibliothek bietet spezielle Unterstützung für LangChain4j-Konfigurationen:

```java
LangchainConfiguration langchainConfig = new LangchainConfiguration("langchain.properties");

// OpenAI-spezifische Konfigurationen
String apiKey = langchainConfig.getOpenAiApiKey(); // Prüft OPENAI_API_KEY oder openai.api.key
String modelName = langchainConfig.getOpenAiModelName();
double temperature = langchainConfig.getOpenAiTemperature();
```

## Fortgeschrittene Nutzung

### Prüfen, ob eine Property existiert

```java
boolean hasDebugConfig = config.hasProperty("app.debug");
```

### Verwendung in Testumgebungen

Die Bibliothek ist testfreundlich durch Dependency Injection:

```java
// Mock EnvironmentProvider für Tests erstellen
EnvironmentProvider.EnvironmentReader mockReader = key -> {
    if ("DATABASE_URL".equals(key)) {
        return "jdbc:h2:mem:testdb";
    }
    return null;
};

EnvironmentProvider mockProvider = new EnvironmentProvider(mockReader);
EnvironmentProvider.setInstance(mockProvider);

// Konfiguration mit Mock-Provider testen
Configuration config = Configuration.getInstance();
assertEquals("jdbc:h2:mem:testdb", config.getProperty("DATABASE_URL"));

// Nach dem Test zurücksetzen
EnvironmentProvider.reset();
Configuration.reset();
```

## Architektur

Die Bibliothek basiert auf folgenden Kernklassen:

- `Configuration`: Hauptklasse für die Konfigurationsverwaltung
- `EnvironmentProvider`: Verwaltet den Zugriff auf Umgebungsvariablen
- `EnvironmentReader`: Interface zur Abstraktion des Lesens von Umgebungsvariablen
- `LangchainConfiguration`: Spezialisierte Konfiguration für LangChain4j

## Beitragen

Beiträge sind herzlich willkommen! Bitte erstelle einen Fork des Repositories und reiche einen Pull Request ein.

## Lizenz

Diese Bibliothek steht unter der [MIT-Lizenz](LICENSE).