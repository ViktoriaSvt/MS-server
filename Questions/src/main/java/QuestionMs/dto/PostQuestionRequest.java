package skytales.Questions.dto;

import jakarta.validation.constraints.Size;

public record PostQuestionRequest(
        @Size(min = 5)
        String text
) {
}
