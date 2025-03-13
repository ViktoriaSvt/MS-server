package skytales.questions.model;

import jakarta.persistence.*;
import lombok.*;
import skytales.auth.model.User;

import java.util.UUID;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String text;

    private String answer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    private UUID author;

    private UUID admin;

}
