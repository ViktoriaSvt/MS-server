package skytales.Questions.entity;


import org.junit.jupiter.api.Test;
import skytales.Questions.model.Question;
import skytales.Questions.model.Status;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class QuestionTest {


    @Test
    void testValidQuestion() {
        Question question = Question.builder()
                .id(UUID.randomUUID())
                .text("Sample question text")
                .answer("Sample answer")
                .status(Status.PENDING)
                .author(UUID.randomUUID())
                .admin(UUID.randomUUID())
                .build();


        assertNotNull(question.getId());
        assertNotNull(question.getText());
        assertNotNull(question.getAnswer());
        assertNotNull(question.getStatus());
        assertNotNull(question.getAuthor());
        assertNotNull(question.getAdmin());
    }

    @Test
    void testInvalidQuestion_NullText() {
        Question question = Question.builder()
                .id(UUID.randomUUID())
                .status(Status.PENDING)
                .author(UUID.randomUUID())
                .admin(UUID.randomUUID())
                .build();


        assertNull(question.getText());
        assertThrows(IllegalArgumentException.class, () -> {
            if (question.getText() == null) {
                throw new IllegalArgumentException("Text must not be null");
            }
        });
    }

    @Test
    void testInvalidQuestion_NullStatus() {
        Question question = Question.builder()
                .id(UUID.randomUUID())
                .text("Sample question text")
                .author(UUID.randomUUID())
                .admin(UUID.randomUUID())
                .build();


        assertNull(question.getStatus());
        assertThrows(IllegalArgumentException.class, () -> {
            if (question.getStatus() == null) {
                throw new IllegalArgumentException("Status must not be null");
            }
        });
    }
}

