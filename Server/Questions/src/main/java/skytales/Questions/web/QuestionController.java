package skytales.Questions.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import skytales.Questions.web.dto.AnswerRequest;
import skytales.Questions.web.dto.PostQuestionRequest;
import skytales.Questions.model.Question;
import skytales.Questions.service.QuestionService;


import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/questions")
public class QuestionController {

    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @GetMapping("/all")
    public ResponseEntity<List<Question>> getAllQuestions() {

        List<Question> questions = questionService.getAll();

        if (questions.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return ResponseEntity.ok(questions);
    }

    @GetMapping("/{userId}/history")
    public ResponseEntity<List<Question>> getUserQuestions(@PathVariable UUID userId) {

        List<Question> questions = questionService.getByUserId(userId);
        return ResponseEntity.ok(questions);
    }

    @PostMapping("/upload")
    public ResponseEntity<?> postQuestion(@RequestBody @Valid PostQuestionRequest postQuestionRequest, BindingResult bindingResult, HttpServletRequest request) {

        if (bindingResult.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        UUID userId = UUID.fromString(request.getAttribute("userId").toString());
        questionService.createQuestion(postQuestionRequest, userId);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> answerQuestion(@PathVariable String id, @RequestBody @Valid AnswerRequest answerRequest, BindingResult bindingResult, HttpServletRequest request) {

        if (bindingResult.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        UUID adminId = (UUID) request.getAttribute("userId");
        UUID questionId = UUID.fromString(id);
        questionService.sendAnswer(questionId, answerRequest, adminId);

        return ResponseEntity.status(HttpStatus.OK).build();
    }
}