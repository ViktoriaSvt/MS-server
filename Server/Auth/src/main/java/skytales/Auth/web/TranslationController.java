package skytales.Auth.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import skytales.Auth.service.TranslationService;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("api/auth/translate")
public class TranslationController {

    private final TranslationService translationService;

    @Autowired
    public TranslationController(TranslationService translationService) {
        this.translationService = translationService;
    }

    @GetMapping("/login")
    public ResponseEntity<Map<String, String>> getLoginTranslation(@RequestParam(value = "lang", required = false) String lang) {
        try {
            Map<String, String> translations = translationService.loadTranslations("loginTranslations.json", lang);
            return ResponseEntity.ok(translations);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Translation file not found"));
        }
    }

    @GetMapping("/register")
    public ResponseEntity<Map<String, String>> getRegisterTranslation(@RequestParam(value = "lang", required = false) String lang) {
        try {
            Map<String, String> translations = translationService.loadTranslations("registerTranslations.json", lang);
            return ResponseEntity.ok(translations);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Translation file not found"));
        }
    }
}
