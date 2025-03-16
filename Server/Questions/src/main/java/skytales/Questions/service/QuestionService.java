package skytales.Questions.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import skytales.Questions.web.dto.AnswerRequest;
import skytales.Questions.web.dto.PostQuestionRequest;
import skytales.Questions.model.Question;
import skytales.Questions.model.Status;
import skytales.Questions.repository.QuestionRepository;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class QuestionService {

    private final QuestionRepository questionRepository;

    public QuestionService(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    public List<Question> getAll() {
        return questionRepository.findAll();
    }

    public List<Question> getByUserId(UUID userId) {
        return questionRepository.findByAuthorAndAnswerIsNotNull(userId);
    }

    public void createQuestion(PostQuestionRequest request, UUID userId) {

        Question question = Question.builder()
                .text(request.text())
                .author(userId)
                .status(Status.PENDING)
                .build();

        log.info("Question with id was created : " + question.getId());
        questionRepository.save(question);
    }

    public void sendAnswer(UUID questionId, AnswerRequest answerRequest, UUID adminId) {

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Not a valid ID"));

        question.setAnswer(answerRequest.text());
        question.setStatus(Status.ANSWERED);
        question.setAdmin(adminId);

        log.info("Question with id - " + question.getId() + " has been answered by admin with id - " + adminId);
        questionRepository.save(question);
    }
}
