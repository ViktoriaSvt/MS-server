package skytales.Library.util.elasticsearch.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.DeleteRequest;
import co.elastic.clients.elasticsearch.core.DeleteResponse;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import skytales.Library.model.Book;


import java.io.IOException;

@Slf4j
@Service
public class ElasticSearchService {

   private final ElasticsearchClient elasticsearchClient;

    public ElasticSearchService(ElasticsearchClient elasticsearchClient) {
        this.elasticsearchClient = elasticsearchClient;
    }


    public void addBookToElasticsearch(Book book) {

        IndexRequest<Book> request = IndexRequest.of(i -> i
                .index("general-search")
                .id(book.getId().toString())
                .document(book)
        );

        try {
            IndexResponse response = elasticsearchClient.index(request);
            log.info("Indexed with version " + response.version());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void deleteBookFromElasticsearch(String bookId) {
        DeleteRequest request = DeleteRequest.of(d -> d
                .index("general-search")
                .id(bookId)
        );

        try {
            DeleteResponse response = elasticsearchClient.delete(request);
            log.info("Deleted book with ID: " + bookId + " Response: " + response.result());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
