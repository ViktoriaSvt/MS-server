package skytales.Questions;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mockito;

import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.validation.BindingResult;

import skytales.Questions.util.security.SecurityConfig;
import skytales.Questions.model.Question;
import skytales.Questions.service.QuestionService;
import skytales.Questions.service.TranslationService;
import skytales.Questions.web.QuestionController;
import skytales.Questions.web.dto.AnswerRequest;
import skytales.Questions.web.dto.PostQuestionRequest;


import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WithMockUser(username = "testuser", roles = {"USER"})
@WebMvcTest(QuestionController.class)
@Import(SecurityConfig.class)
public class QuestionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private QuestionService questionService;

    @MockitoBean
    private TranslationService translationService;

    @MockitoBean
    private BindingResult bindingResult;

    @InjectMocks
    private QuestionController questionController;

    private String token;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        Question question1 = new Question();
        Question question2 = new Question();
        token = "eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiVVNFUiIsImNhcnRJZCI6IjU1MjYxNzEzLTY0ZDMtNDhhMy04ZWFiLWViNWU3YWU4MjU1NiIsInVzZXJJZCI6ImRlZTAwZGQ5LWQzMWUtNGUxNi1iZWFlLTc4OWNlMzM4OTNmMiIsImVtYWlsIjoidGVzdGVtYWlsQGFidi5iZyIsInVzZXJuYW1lIjoidXNlcm5hbWUiLCJzdWIiOiJ0ZXN0ZW1haWxAYWJ2LmJnIiwiaWF0IjoxNzQzMzQ3NTQzLCJleHAiOjE3NDMzNTAxMzV9.f84j-KD9aQFetidEy_ShHd3qSyDzlMUQQn_NnejGA1g";

        question1.setText("Sample Question 1");
        question1.setAnswer("Sample Answer 1");

        question2.setText("Sample Question 2");
        question2.setAnswer("Sample Answer 2");

        List<Question> mockQuestions = Arrays.asList(question1, question2);

        when(bindingResult.hasErrors()).thenReturn(false);
        when(questionService.getByUserId(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))).thenReturn(mockQuestions);
        when(questionService.getAll()).thenReturn(mockQuestions);
    }

    @Test
    void testGetAllQuestions() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/questions/all")
                        .header("Authorization", "Bearer " + token))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].text").value("Sample Question 1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].text").value("Sample Question 2"));
    }

    @Test
    void testGetAllQuestionsEmpty() throws Exception {
        when(questionService.getAll()).thenReturn(Arrays.asList());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/questions/all")
                        .header("Authorization", "Bearer " + token))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    void testAnswerQuestion() throws Exception {
        UUID questionId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        AnswerRequest answerRequest = new AnswerRequest("Sample Answer");

        Mockito.doNothing().when(questionService).sendAnswer(Mockito.eq(questionId), Mockito.any(AnswerRequest.class), Mockito.eq(adminId));

        String answerRequestJson = objectMapper.writeValueAsString(answerRequest);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/questions/{id}", questionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(answerRequestJson)
                        .header("Authorization", "Bearer " + token)
                        .requestAttr("userId", adminId))
                .andExpect(status().isOk());
    }

    @Test
    void testAnswerQuestionWithBindingErrors() throws Exception {
        when(bindingResult.hasErrors()).thenReturn(true);
        UUID questionId = UUID.randomUUID();

        mockMvc.perform(MockMvcRequestBuilders.put("/api/questions/{id}", questionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"answer\": \"Sample Answer\"}")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }
    

    @Test
    void testGetUserQuestions() throws Exception {
        UUID userId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

        mockMvc.perform(MockMvcRequestBuilders.get("/api/questions/{userId}/history", userId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].text", Matchers.is("Sample Question 1")))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].text", Matchers.is("Sample Question 2")));

        verify(questionService).getByUserId(Mockito.eq(userId));
    }
}