package skytales.Auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;


import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.util.StreamUtils;

@Service
public class TranslationService {

    private final ObjectMapper objectMapper;

    @Value("${default.language:en}")
    private String defaultLanguage;

    public TranslationService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Map<String, String> loadTranslations(String translationFile, String lang) throws IOException {

        String language;
        if (lang != null && !lang.isEmpty()) {
            language = lang;
        } else {
            language = defaultLanguage;
        }

        String filePath = "translations/" + translationFile;

        ClassPathResource resource = new ClassPathResource(filePath);
        InputStream inputStream = resource.getInputStream();
        String content = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);

        Map<String, Map<String, String>> translations = objectMapper.readValue(content, Map.class);
        return translations.get(language);
    }
}
