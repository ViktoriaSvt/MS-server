package skytales.Library.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.web.multipart.MultipartFile;
import skytales.Library.util.exceptions.*;
import skytales.Library.util.state_engine.UpdateProducer;
import skytales.Library.util.state_engine.model.UpdateType;
import skytales.Library.web.dto.BookData;
import skytales.Library.util.elasticsearch.service.ElasticSearchService;
import skytales.Library.model.Book;
import skytales.Library.repository.BookRepository;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

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
        return bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException("No book found with id " + bookId));
    }

    public List<Book> getNewestBooks(int year) {
        return bookRepository.findBooksByYearAfter(year)
                .orElseThrow(() -> new BookNotFoundException("No books found after the year " + year));
    }

    @Transactional
    public void deleteBooks(List<String> bookIds) {
        if (bookIds == null || bookIds.isEmpty()) {
            throw new InvalidBookDataException("Book ID list cannot be empty.");
        }

        bookIds.forEach(bookId -> {
            try {
                UUID uuid = UUID.fromString(bookId);
                Book book = bookRepository.findById(uuid)
                        .orElseThrow(() -> new BookNotFoundException("No book found with id " + bookId));

                updateProducer.sendBookUpdate(UpdateType.REMOVE_BOOK, book);
                bookRepository.delete(book);
                elasticSearchService.deleteBookFromElasticsearch(bookId);
            } catch (Exception e) {
                throw new BookDeletionException("Failed to delete book with ID: " + bookId, e);
            }
        });
    }

    @Transactional
    public Book createBook(BookData data, MultipartFile bannerImage, MultipartFile coverImage) throws IOException {

        BigDecimal price = new BigDecimal(data.price());

        String coverImageUrl = uploadCoverImage(coverImage);
        String bannerImageUrl = uploadCoverImage(bannerImage);

        Book book = buildBook(data, coverImageUrl, bannerImageUrl, price);

        bookRepository.save(book);
        elasticSearchService.addBookToElasticsearch(book);
        updateProducer.sendBookUpdate(UpdateType.NEW_BOOK, book);

        log.info("Book created and indexed with title - " + book.getTitle());
        return book;
    }

    private static Book buildBook(BookData data, String coverImageUrl, String bannerImageUrl, BigDecimal price) {
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
        return book;
    }

    private String uploadCoverImage(MultipartFile coverImage) {
        String fileName = coverImage.getOriginalFilename();
        String fileExtension = fileName.substring(fileName.lastIndexOf('.') + 1);

        if (!fileExtension.matches("jpg|jpeg|png|webp")) {
            throw new InvalidFileFormatException("Unsupported file format: " + fileExtension);
        }

        try {
            Map<String, Object> uploadResult = cloudinary.uploader().upload(
                    coverImage.getBytes(),
                    ObjectUtils.asMap(
                            "resource_type", "auto",
                            "quality", "auto:good",
                            "format", fileExtension,
                            "width", 800,
                            "crop", "limit"
                    )
            );
            return (String) uploadResult.get("secure_url");
        } catch (IOException e) {
            throw new FileUploadException("Failed to upload image: " + fileName, e);
        }
    }



}
