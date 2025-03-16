package skytales.Questions.web.dto;

import jakarta.validation.constraints.NotNull;

public record AnswerRequest(
        @NotNull
        String text
) {
}
