package pl.wiktor.koprowski.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.*;

@Service
public class TranslationService {

    private final MessageSource messageSource;

    @Autowired
    public TranslationService(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public Map<String, String> getTranslations(String language) {

        Locale locale = new Locale(language);
        LocaleContextHolder.setLocale(locale);

        Map<String, String> translations = new HashMap<>();

        try {
            String propertiesFile = "i18n/messages_" + language + ".properties";
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propertiesFile);

            if (inputStream == null) {
                translations.put("error", "Plik tłumaczeń dla języka " + language + " nie został znaleziony.");
                return translations;
            }

            Properties properties = new Properties();
            properties.load(inputStream);

            Enumeration<?> propertyNames = properties.propertyNames();
            while (propertyNames.hasMoreElements()) {
                String key = (String) propertyNames.nextElement();
                String value = properties.getProperty(key);
                translations.put(key, value);
            }

        } catch (Exception e) {
            translations.put("error", "Wystąpił błąd podczas ładowania tłumaczeń dla języka " + language + ".");
        }

        return translations;
    }
}
