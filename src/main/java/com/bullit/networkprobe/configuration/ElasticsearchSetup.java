package com.bullit.networkprobe.configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;

import static com.bullit.networkprobe.configuration.ElasticsearchConfiguration.INDEX;

@Slf4j
@Component
@RequiredArgsConstructor
public class ElasticsearchSetup {
    private final RestHighLevelClient client;

    @PostConstruct
    public void setupElasticsearch() throws IOException {
        log.info("Setting up Elasticsearch");
        if (!indexExists()) {
            createIndex();
        }
    }

    private boolean indexExists() throws IOException {
        boolean exists = client
                .indices()
                .exists(new GetIndexRequest(INDEX), RequestOptions.DEFAULT);

        log.info(String.format("Index exists: %s", exists));

        return exists;
    }

    private void createIndex() throws IOException {
        log.info("Creating index");

        CreateIndexRequest createIndexRequest = new CreateIndexRequest(INDEX);
        createIndexRequest.mapping("""
                            {
                                "properties": {
                                    "timestamp": {
                                        "type": "date",
                                        "format": "yyyy-MM-dd'T'HH:mm:ss.SSSZ"
                                    },
                                    "responseTime": {
                                        "type": "integer"
                                    },
                                    "reachable": {
                                        "type": "keyword"
                                    }
                                }
                            }
                            """, XContentType.JSON);

        CreateIndexResponse response = client
                .indices()
                .create(createIndexRequest, RequestOptions.DEFAULT);

        log.info(String.format("Index created: %s", response.isAcknowledged()));
    }
}
