package skytales.Auth.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import skytales.Auth.service.TranslationService;
import skytales.Auth.web.TranslationController;

import java.io.IOException;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TranslationControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TranslationService translationService;

    @InjectMocks
    private TranslationController translationController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(translationController).build();
    }

    @Test
    void testGetLoginTranslation_Success() throws Exception {

        Map<String, String> mockTranslations = Map.of("username", "Username", "password", "Password");
        when(translationService.loadTranslations("loginTranslations.json", "en")).thenReturn(mockTranslations);

        mockMvc.perform(get("/api/auth/translate/login?lang=en"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("Username"))
                .andExpect(jsonPath("$.password").value("Password"));

        verify(translationService, times(1)).loadTranslations("loginTranslations.json", "en");
    }

    @Test
    void testGetLoginTranslation_FileNotFound() throws Exception {

        when(translationService.loadTranslations("loginTranslations.json", "en"))
                .thenThrow(new IOException("Translation file not found"));

        mockMvc.perform(get("/api/auth/translate/login?lang=en"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Translation file not found"));

        verify(translationService, times(1)).loadTranslations("loginTranslations.json", "en");
    }

    @Test
    void testGetRegisterTranslation_Success() throws Exception {

        Map<String, String> mockTranslations = Map.of("email", "Email", "confirmPassword", "Confirm Password");
        when(translationService.loadTranslations("registerTranslations.json", "en")).thenReturn(mockTranslations);

        mockMvc.perform(get("/api/auth/translate/register?lang=en"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("Email"))
                .andExpect(jsonPath("$.confirmPassword").value("Confirm Password"));

        verify(translationService, times(1)).loadTranslations("registerTranslations.json", "en");
    }

    @Test
    void testGetRegisterTranslation_FileNotFound() throws Exception {

        when(translationService.loadTranslations("registerTranslations.json", "en"))
                .thenThrow(new IOException("Translation file not found"));

        mockMvc.perform(get("/api/auth/translate/register?lang=en"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Translation file not found"));

        verify(translationService, times(1)).loadTranslations("registerTranslations.json", "en");
    }

    @Test
    void testGetLoginTranslation_MissingLang() throws Exception {

        Map<String, String> mockTranslations = Map.of("username", "Username", "password", "Password");
        when(translationService.loadTranslations("loginTranslations.json", null)).thenReturn(mockTranslations);

        mockMvc.perform(get("/api/auth/translate/login"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("Username"))
                .andExpect(jsonPath("$.password").value("Password"));

        verify(translationService, times(1)).loadTranslations("loginTranslations.json", null);
    }
}
