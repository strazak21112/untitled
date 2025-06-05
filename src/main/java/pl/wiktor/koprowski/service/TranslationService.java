package pl.wiktor.koprowski.service;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

@Service
public class TranslationService {

    private final MessageSource messageSource;

    public TranslationService(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public String getTranslation(String label, String language) {
        if (language == null || language.isBlank()) {
            language = "pl";
        }
        var locale = switch (language) {
            case "de" -> new java.util.Locale("de");
            case "en" -> java.util.Locale.ENGLISH;
            default -> new java.util.Locale("pl");
        };
        return messageSource.getMessage(label, null, label, locale);
    }

    public Map<String, String> getTranslations(String language) {
        if (language == null || language.isBlank()) {
            language = "pl";
        }
        Map<String, String> translations = new HashMap<>();
        try {
            String filePath = switch (language) {
                case "de" -> "i18n/messages_de.properties";
                case "en" -> "i18n/messages_en.properties";
                default -> "i18n/messages_pl.properties";
            };
            InputStreamReader reader = new InputStreamReader(
                    Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream(filePath)),
                    StandardCharsets.UTF_8
            );
            Properties props = new Properties();
            props.load(reader);
            for (String key : props.stringPropertyNames()) {
                translations.put(key, props.getProperty(key));
            }
        } catch (Exception e) {
            translations.put("error", "Błąd ładowania tłumaczeń dla języka " + language);
        }
        return translations;
    }
}
