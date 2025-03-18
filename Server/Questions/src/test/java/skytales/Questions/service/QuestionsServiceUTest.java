package skytales.Questions.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import skytales.Questions.model.Question;
import skytales.Questions.model.Status;
import skytales.Questions.repository.QuestionRepository;
import skytales.Questions.web.dto.AnswerRequest;
import skytales.Questions.web.dto.PostQuestionRequest;


import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class QuestionsServiceUTest {

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private QuestionService questionServiceTest;

    @InjectMocks
    private QuestionService questionService;

    private AnswerRequest answerRequest;
    private PostQuestionRequest request;
    private Question question;
    private Question question2;
    private UUID userId;

    @BeforeEach
    public void setUp() {
        userId = UUID.randomUUID();
        question = new Question();
        question2 = new Question();
        request = new PostQuestionRequest("How was your day?");
    }

    @Test
    void shouldReturnUsersAnsweredQuestions() {

        question.setAuthor(userId);
        question.setAnswer("answer");

        when(questionRepository.findByAuthorAndAnswerIsNotNull(userId)).thenReturn(List.of(question));

        List<Question> result = questionService.getByUserId(userId);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(question.getAuthor(), result.getFirst().getAuthor());
        Assertions.assertEquals(question.getAnswer(), result.getFirst().getAnswer());


        verify(questionRepository, times(1)).findByAuthorAndAnswerIsNotNull(userId);
    }

    @Test
    void shouldReturnEmptyListWhenUserHasNoAnsweredQuestions() {

        when(questionRepository.findByAuthorAndAnswerIsNotNull(userId)).thenReturn(List.of());

        List<Question> result = questionService.getByUserId(userId);

        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());

        verify(questionRepository, times(1)).findByAuthorAndAnswerIsNotNull(userId);
    }

    @Test
    void successfullyCreatesQuestion() {

        questionService.createQuestion(request, userId);
        verify(questionRepository, times(1)).save(any(Question.class));

        ArgumentCaptor<Question> questionCaptor = ArgumentCaptor.forClass(Question.class);
        verify(questionRepository).save(questionCaptor.capture());

        Question savedQuestion = new Question();
        savedQuestion.setText(request.text());
        savedQuestion.setAuthor(userId);
        savedQuestion.setStatus(Status.PENDING);

    }

    @Test
    void retreatsAllQuestions() {
        question.setText("Text1");
        question2.setText("Text2");

        List<Question> mockQuestions = List.of(question, question2);
        when(questionRepository.findAll()).thenReturn(mockQuestions);

        List<Question> result = questionService.getAll();

        Assertions.assertNotNull(result);
        Assertions.assertEquals(2, result.size());
        Assertions.assertEquals(question.getText(), result.get(0).getText());

        verify(questionRepository, times(1)).findAll();
    }

    @Test
    void updatesQuestionWhenAnswered() {

        UUID questionId = UUID.randomUUID();
        answerRequest = new AnswerRequest("Cool.");

        question.setId(questionId);
        question.setStatus(Status.PENDING);

        when(questionRepository.findById(questionId)).thenReturn(Optional.of(question));

        questionService.sendAnswer(questionId, answerRequest, userId);

        ArgumentCaptor<Question> questionCaptor = ArgumentCaptor.forClass(Question.class);
        verify(questionRepository, times(1)).save(questionCaptor.capture());

        Question updatedQuestion = questionCaptor.getValue();
        assertEquals(answerRequest.text(), updatedQuestion.getAnswer());
        assertEquals(Status.ANSWERED, updatedQuestion.getStatus());
        assertEquals(userId, updatedQuestion.getAdmin());

    }



    @Test
    void shouldThrowExceptionForInvalidQuestionId() {
        UUID invalidQuestionId = UUID.randomUUID();
        AnswerRequest answerRequest = new AnswerRequest("This is an answer.");

        when(questionRepository.findById(invalidQuestionId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            questionService.sendAnswer(invalidQuestionId, answerRequest, userId);
        });

        assertEquals("Not a valid ID", exception.getMessage());
        verify(questionRepository, never()).save(any(Question.class));
    }

}
