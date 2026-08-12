package com.khedmataktak.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.io.ClassPathResource;

/**
 * Loads {@code .env} from classpath (resources) or {@code src/main/resources/.env} in dev.
 * System environment variables take precedence.
 */
public class DotenvEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final String PROPERTY_SOURCE_NAME = "dotenvProperties";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Map<String, Object> properties = new HashMap<>();
        loadFromClasspath(properties);
        loadFromDevPath(properties);
        if (!properties.isEmpty()) {
            environment.getPropertySources().addFirst(new MapPropertySource(PROPERTY_SOURCE_NAME, properties));
        }
    }

    private void loadFromClasspath(Map<String, Object> properties) {
        try {
            ClassPathResource resource = new ClassPathResource(".env");
            if (resource.exists()) {
                parseEnvFile(resource.getInputStream(), properties);
            }
        } catch (IOException ignored) {
            // optional file
        }
    }

    private void loadFromDevPath(Map<String, Object> properties) {
        Path devPath = Path.of("src/main/resources/.env");
        if (!Files.exists(devPath)) {
            return;
        }
        try (InputStream inputStream = Files.newInputStream(devPath)) {
            parseEnvFile(inputStream, properties);
        } catch (IOException ignored) {
            // optional file
        }
    }

    private void parseEnvFile(InputStream inputStream, Map<String, Object> properties) throws IOException {
        String content = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        for (String line : content.split("\\R")) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            int eq = line.indexOf('=');
            if (eq <= 0) {
                continue;
            }
            String key = line.substring(0, eq).trim();
            String value = unquote(line.substring(eq + 1).trim());
            if (System.getenv(key) == null) {
                properties.put(key, value);
            }
        }
    }

    private String unquote(String value) {
        if (value.length() >= 2) {
            char first = value.charAt(0);
            char last = value.charAt(value.length() - 1);
            if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
                return value.substring(1, value.length() - 1);
            }
        }
        return value;
    }
}
