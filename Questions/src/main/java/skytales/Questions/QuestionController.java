package skytales.questions;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import skytales.auth.dto.SessionResponse;
import skytales.common.security.SessionService;
import skytales.questions.dto.AnswerRequest;
import skytales.questions.dto.PostQuestionRequest;
import skytales.questions.model.Question;
import skytales.questions.service.QuestionService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/faq")
public class QuestionController {

    private final QuestionService questionService;
    private final SessionService sessionService;

    public QuestionController(QuestionService questionService, SessionService sessionService) {
        this.questionService = questionService;
        this.sessionService = sessionService;
    }

    @GetMapping("/{userId}/questions")
    public ResponseEntity<List<Question>> getUserQuestions(@PathVariable UUID userId) {
        List<Question> questions = questionService.fetchQuestions(userId);
        return ResponseEntity.ok(questions);
    }

    @PostMapping("/upload")
    public ResponseEntity<?> postQuestion(@RequestBody @Valid PostQuestionRequest postQuestionRequest,BindingResult bindingResult, HttpServletRequest request) {

        if (bindingResult.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        SessionResponse sessionResponse = sessionService.getSessionData(request);
        UUID userId = UUID.fromString(sessionResponse.id());

        questionService.createQuestion(postQuestionRequest, userId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/all")
    public ResponseEntity<List<Question>> getAllQuestions() {
        List<Question> questions = questionService.getAll();

        if(questions.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return ResponseEntity.ok(questions);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> answerQuestion(@PathVariable String id, @RequestBody @Valid AnswerRequest answerRequest, BindingResult bindingResult, HttpServletRequest request) {

        if(bindingResult.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        SessionResponse sessionResponse = sessionService.getSessionData(request);
        UUID adminId = UUID.fromString(sessionResponse.id());
        UUID questionId= UUID.fromString(id);


        questionService.sendAnswer(questionId ,answerRequest , adminId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}