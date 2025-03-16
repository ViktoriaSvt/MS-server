package skytales.Library.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import skytales.Library.util.state_engine.UpdateProducer;
import skytales.Library.util.state_engine.model.UpdateType;
import skytales.Library.web.dto.BookData;
import skytales.Library.util.elasticsearch.service.ElasticSearchService;
import skytales.Library.model.Book;
import skytales.Library.repository.BookRepository;


import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

@Slf4j
@Service
public class BookService {

    private final BookRepository bookRepository;
    private final ElasticSearchService elasticSearchService;
    private final UpdateProducer updateProducer;
    private final Cloudinary cloudinary;

    @Autowired
    public BookService(BookRepository bookRepository, ElasticSearchService elasticSearchService, UpdateProducer updateProducer, Cloudinary cloudinary) {
        this.bookRepository = bookRepository;
        this.elasticSearchService = elasticSearchService;
        this.updateProducer = updateProducer;
        this.cloudinary = cloudinary;
    }

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    public Book getBookById(UUID bookId) {
        return bookRepository.findById(bookId).orElse(null);
    }

    public List<Book> getNewestBooks(int year) {
        return bookRepository.findBooksByYearAfter(year)
                .orElseThrow(() -> new NoSuchElementException("No books found after the year " + year));
    }



    @Transactional
    public Book createBook( BookData data,MultipartFile bannerImage,MultipartFile coverImage) throws IOException {

        BigDecimal price = new BigDecimal(data.price());
        String coverImageUrl = uploadCoverImage(coverImage);
        String bannerImageUrl = uploadCoverImage(bannerImage);

        Book book = Book.builder()
                .title(data.title())
                .author(data.author())
                .coverImageUrl(coverImageUrl)
                .bannerImageUrl(bannerImageUrl)
                .price(price)
                .description(data.description())
                .year(Integer.parseInt(data.year()))
                .genre(data.genre())
                .quantity(Integer.parseInt(data.quantity()))
                .build();

        bookRepository.save(book);
        elasticSearchService.addBookToElasticsearch(book);
        updateProducer.sendBookUpdate(UpdateType.NEW_BOOK, book);

        log.info("Book created and indexed with title - " + book.getTitle());
        return book;
    }

    private String uploadCoverImage(MultipartFile coverImage) throws IOException {
        Map<String, Object> uploadResult = cloudinary.uploader().upload(coverImage.getBytes(),
                ObjectUtils.asMap("resource_type", "auto"));
        return (String) uploadResult.get("secure_url");
    }



}
