package skytales.Questions.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import skytales.Questions.util.exceptions.TranslationNotFoundException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
public class TranslationService {

    private final ObjectMapper objectMapper;

    @Value("${default.language:en}")
    private String defaultLanguage;

    public TranslationService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Map<String, Object> loadTranslations(String translationFile, String lang) {
        String language = lang != null ? lang : defaultLanguage;
        String filePath = "translations/" + translationFile;

        try {
            ClassPathResource resource = new ClassPathResource(filePath);
            InputStream inputStream = resource.getInputStream();
            String content = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);

            Map<String, Map<String, Object>> translations = objectMapper.readValue(content, new TypeReference<>() {
            });
            return translations.getOrDefault(language, translations.get(defaultLanguage));

        } catch (Exception e) {
            throw new TranslationNotFoundException("Translation file not found: " + translationFile);
        }
    }

}
