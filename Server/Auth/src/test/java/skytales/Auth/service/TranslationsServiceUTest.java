package skytales.Auth.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TranslationsServiceUTest {

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private TranslationService translationService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void testLoadTranslations_Success() throws IOException {

        Map<String, Map<String, String>> mockTranslations = Map.of(
                "en", Map.of("header", "Login", "emailLabel", "Email:"),
                "fr", Map.of("header", "Se connecter", "emailLabel", "Email:")
        );

        when(objectMapper.readValue(any(String.class), eq(Map.class))).thenReturn(mockTranslations);

        Map<String, String> result = translationService.loadTranslations("loginTranslations.json", "en");

        assertNotNull(result);
        assertEquals("Login", result.get("header"));
        assertEquals("Email:", result.get("emailLabel"));
    }

    @Test
    void testLoadTranslations_FrenchLanguage() throws IOException {

        Map<String, Map<String, String>> mockTranslations = Map.of(
                "en", Map.of("header", "Login", "emailLabel", "Email:"),
                "fr", Map.of("header", "Se connecter", "emailLabel", "Email:")
        );

        when(objectMapper.readValue(any(String.class), eq(Map.class))).thenReturn(mockTranslations);

        Map<String, String> result = translationService.loadTranslations("loginTranslations.json", "fr");

        assertNotNull(result);
        assertEquals("Se connecter", result.get("header"));
        assertEquals("Email:", result.get("emailLabel"));
    }

    @Test
    void testLoadTranslations_LanguageNotFound() throws IOException {

        Map<String, Map<String, String>> mockTranslations = Map.of(
                "en", Map.of("header", "Login", "emailLabel", "Email:"),
                "fr", Map.of("header", "Se connecter", "emailLabel", "Email:")
        );

        when(objectMapper.readValue(any(String.class), eq(Map.class))).thenReturn(mockTranslations);

        Map<String, String> result = translationService.loadTranslations("loginTranslations.json", "es");

        assertNull(result);
    }

    @Test
    void testLanguageSelection() {

        String lang = "fr";
        String defaultLanguage = "en";
        String result = getLanguageOrDefault(lang, defaultLanguage);
        assertEquals("fr", result, "Language should be 'fr' when it's provided.");

        lang = null;
        result = getLanguageOrDefault(lang, defaultLanguage);
        assertEquals(defaultLanguage, result, "Language should default to 'en' when 'lang' is null.");

        lang = "";
        result = getLanguageOrDefault(lang, defaultLanguage);
        assertEquals(defaultLanguage, result, "Language should default to 'en' when 'lang' is empty.");
    }

    private String getLanguageOrDefault(String lang, String defaultLanguage) {
        String language;
        if (lang != null && !lang.isEmpty()) {
            language = lang;
        } else {
            language = defaultLanguage;
        }
        return language;
    }

    @Test
    void testLoadTranslations_MissingFile() throws IOException {
        assertThrows(IOException.class,
                () -> translationService.loadTranslations("Translations.json", "en"));
    }
}
