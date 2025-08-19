package skytales.Library.util.elasticsearch.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.DeleteRequest;
import co.elastic.clients.elasticsearch.core.DeleteResponse;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import skytales.Library.model.Book;


import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import skytales.Library.util.exceptions.BookSearchException;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ElasticSearchService {

    private final ElasticsearchClient elasticsearchClient;

    public ElasticSearchService(ElasticsearchClient elasticsearchClient) {
        this.elasticsearchClient = elasticsearchClient;
    }


    public void addBookToElasticsearch(Book book) {
        IndexRequest<Book> request = IndexRequest.of(i -> i
                .index("library_collection")
                .id(book.getId().toString())
                .document(book)
        );

        try {
            IndexResponse response = elasticsearchClient.index(request);
            log.info("Indexed with version " + response.version());
        } catch (IOException e) {
            throw new BookSearchException("Failed to index book: " + book.getTitle(), e);
        }
    }

    public void deleteBookFromElasticsearch(String bookId) {
        DeleteRequest request = DeleteRequest.of(d -> d
                .index("library_collection")
                .id(bookId)
        );

        try {
            DeleteResponse response = elasticsearchClient.delete(request);
            log.info("Deleted book with ID: " + bookId + " Response: " + response.result());
        } catch (IOException e) {
            throw new BookSearchException("Failed to delete book from Elasticsearch: " + bookId, e);
        }
    }

    public List<Book> searchBooks(String query) {
        try {
            SearchRequest searchRequest = SearchRequest.of(s -> s
                    .index("library_collection")
                    .query(q -> q
                            .multiMatch(m -> m
                                    .query(query)
                                    .fields("title^3", "author^2", "description")
                                    .fuzziness("AUTO")
                            )
                    )
            );

            SearchResponse<Book> searchResponse = elasticsearchClient.search(searchRequest, Book.class);

            List<Book> books = searchResponse.hits().hits().stream()
                    .map(Hit::source)
                    .collect(Collectors.toList());

            if (books.isEmpty()) {
                throw new BookSearchException("No books found for query: " + query);
            }

            return books;

        } catch (IOException e) {
            throw new BookSearchException("Failed to search books for query: " + query, e);
        }
    }


}
