package skytales.Library.util.elasticsearch.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import org.springframework.stereotype.Service;
import skytales.Library.model.Book;


import java.io.IOException;

@Service
public class ElasticSearchService {

   private final ElasticsearchClient elasticsearchClient;

    public ElasticSearchService(ElasticsearchClient elasticsearchClient) {
        this.elasticsearchClient = elasticsearchClient;
    }

    public void addBookToElasticsearch(Book book) {

        IndexRequest<Book> request = IndexRequest.of(i -> i
                .index("book-library")
                .id(book.getId().toString())
                .document(book)
        );

        try {
            IndexResponse response = elasticsearchClient.index(request);
            System.out.println("Indexed with version " + response.version());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
