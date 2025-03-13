package skytales.questions.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import skytales.questions.model.Question;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface QuestionRepository extends JpaRepository<Question, UUID> {
    List<Question> findByAuthorAndAnswerIsNotNull(UUID author);

    Optional<Question> findById(UUID questionId);
}
