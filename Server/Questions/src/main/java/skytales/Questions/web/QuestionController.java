package skytales.Questions.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import skytales.Questions.service.TranslationService;
import skytales.Questions.web.dto.AnswerRequest;
import skytales.Questions.web.dto.PostQuestionRequest;
import skytales.Questions.model.Question;
import skytales.Questions.service.QuestionService;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("api/questions")
public class QuestionController {

    private final QuestionService questionService;
    private final TranslationService translationService;

    public QuestionController(QuestionService questionService, TranslationService translationService) {
        this.questionService = questionService;
        this.translationService = translationService;
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

        String attribute = request.getAttribute("userId").toString();
        UUID adminId = UUID.fromString( attribute);
        UUID questionId = UUID.fromString(id);
        questionService.sendAnswer(questionId, answerRequest, adminId);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/translate/faq")
    public ResponseEntity<Map<String, Object>> getRegisterTranslation(@RequestParam(value = "lang", required = false) String lang) {
        try {
            Map<String, Object> translations = translationService.loadTranslations("faqTranslations.json", lang);
            return ResponseEntity.ok(translations);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Translation file not found"));
        }
    }
}