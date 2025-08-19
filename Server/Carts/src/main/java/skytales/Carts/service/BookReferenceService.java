package skytales.Carts.service;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import skytales.Carts.model.BookItemReference;
import skytales.Carts.repository.BookItemReferenceRepository;
import skytales.Carts.util.exceptions.BookItemNotFoundException;
import skytales.Carts.util.state_engine.dto.BookMessage;

@Slf4j
@Service
public class BookReferenceService {

    private final BookItemReferenceRepository bookItemReferenceRepository;

    public BookReferenceService(BookItemReferenceRepository bookItemReferenceRepository) {
        this.bookItemReferenceRepository = bookItemReferenceRepository;
    }

    public void addBookToState(BookMessage bookRequest) {

        BookItemReference book = buildItem(bookRequest);

        bookItemReferenceRepository.save(book);
        log.info("Book added to state in cart MS");
    }
    public void removeBookFromState(BookMessage BookRequest) {

        BookItemReference book = bookItemReferenceRepository
                .findById(BookRequest.id())
                .orElseThrow(() -> new BookItemNotFoundException("Book was not found"));

        bookItemReferenceRepository.delete(book);
    }

    private static BookItemReference buildItem(BookMessage bookRequest) {
        BookItemReference book = BookItemReference.builder()
                .author(bookRequest.author())
                .title(bookRequest.title())
                .year(bookRequest.year())
                .price(bookRequest.price())
                .genre(bookRequest.genre())
                .quantity(bookRequest.quantity())
                .coverImageUrl(bookRequest.coverImageUrl())
                .id(bookRequest.id())
                .build();
        return book;
    }


}
