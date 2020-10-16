package com.bullit.networkprobe.configuration;

import com.bullit.networkprobe.reactive.*;
import com.bullit.networkprobe.domain.ConnectionResponse;
import com.bullit.networkprobe.support.MDCLogger;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;
import java.util.function.Supplier;

import static com.bullit.networkprobe.configuration.ElasticsearchConfiguration.INDEX;

@Configuration
public class ReactiveStreamsConfiguration {

    @Bean
    public SubmissionPublisher<ConnectionResponse> getConnectionResponsePublisher() {
        return new ConnectionResponsePublisher();
    }

    @Bean
    public Flow.Subscriber<ConnectionResponse> getOutageSubscriber() {
        return new OutageSubscriber(getMdcLogger(), () -> new Date(), getDateFormatSupplier());
    }

    @Bean
    public Flow.Subscriber<ConnectionResponse> getConnectionSubscriber() {
        return new ConnectionSubscriber(getMdcLogger());
    }

    @Bean
    public Flow.Subscriber<ConnectionResponse> getElasticsearchSubscriber(
            @Autowired RestHighLevelClient restHighLevelClient,
            @Autowired @Qualifier("elasticsearchExecutor") Executor executor
            ) {
        return new ElasticsearchSubscriber(
                getMdcLogger(),
                new ElasticsearchClientWrapperImpl(restHighLevelClient),
                () -> new Date(),
                getDateFormatSupplier(),
                this::createIndexRequest,
                executor
        );
    }

    private IndexRequest createIndexRequest(ConnectionResponse item, String timestamp) {
        IndexRequest indexRequest = new IndexRequest(INDEX);
        indexRequest.source(
                Map.of(
                        "timestamp", timestamp,
                        "responseTime", item.getResponseTime(),
                        "reachable", item.isReachable()
                )
        );
        return indexRequest;
    }

    @Bean
    public Supplier<SimpleDateFormat> getDateFormatSupplier() {
        return () -> new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    }

    @Bean
    public MDCLogger getMdcLogger() {
        return new MDCLogger();
    }
}
