package skytales.Library.es;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import skytales.Library.model.Book;
import skytales.Library.util.elasticsearch.service.ElasticSearchService;


import java.io.IOException;
import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ElasticSearchServiceUTest {

    @Mock
    private ElasticsearchClient elasticsearchClient;

    @InjectMocks
    private ElasticSearchService elasticSearchService;

    private Book book;

    @BeforeEach
    void setUp() {
        book = Book.builder()
                .id(UUID.randomUUID())
                .title("Test Book")
                .author("Test Author")
                .price(BigDecimal.valueOf(19.99))
                .description("Test Description")
                .build();
    }

    @Test
    void addBookToElasticsearch_ShouldIndexBookSuccessfully() throws IOException {
        IndexResponse mockResponse = Mockito.mock(IndexResponse.class);
        when(mockResponse.version()).thenReturn(1L);
        when(elasticsearchClient.index(any(IndexRequest.class))).thenReturn(mockResponse);

        elasticSearchService.addBookToElasticsearch(book);

        verify(elasticsearchClient, times(1)).index(any(IndexRequest.class));
    }

    @Test
    void addBookToElasticsearch_ShouldHandleIOException() throws IOException {
        doThrow(IOException.class).when(elasticsearchClient).index(any(IndexRequest.class));

        elasticSearchService.addBookToElasticsearch(book);

        verify(elasticsearchClient, times(1)).index(any(IndexRequest.class));
    }
}
