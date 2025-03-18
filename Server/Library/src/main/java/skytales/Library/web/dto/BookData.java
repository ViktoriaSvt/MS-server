package skytales.Library.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record BookData(
        @NotNull(message = "Title is mandatory")
        @NotEmpty(message = "Title cannot be empty")
        @JsonProperty("title") String title,

        @NotNull(message = "Genre is mandatory")
        @NotEmpty(message = "Genre cannot be empty")
        @JsonProperty("genre") String genre,

        @NotNull(message = "Author is mandatory")
        @NotEmpty(message = "Author cannot be empty") String author,

        @NotNull(message = "Year is mandatory") String year,

        @NotNull(message = "Price is mandatory") String price,

        @NotNull(message = "Quantity is mandatory") String quantity,

        @Size(max = 10000, message = "Description cannot exceed 10000 characters") String description
) {
}
