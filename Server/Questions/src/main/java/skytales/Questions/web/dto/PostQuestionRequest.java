package skytales.Questions.web.dto;

import jakarta.validation.constraints.Size;

public record PostQuestionRequest(
        @Size(min = 5)
        String text
) {
}
