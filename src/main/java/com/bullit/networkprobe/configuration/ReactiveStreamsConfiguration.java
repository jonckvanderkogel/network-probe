package com.bullit.networkprobe.configuration;

import com.bullit.networkprobe.domain.ConnectionResponse;
import com.bullit.networkprobe.reactive.ConnectionSubscriber;
import com.bullit.networkprobe.reactive.ElasticsearchClientWrapperImpl;
import com.bullit.networkprobe.reactive.ElasticsearchSubscriber;
import com.bullit.networkprobe.reactive.OutageSubscriber;
import com.bullit.networkprobe.support.MDCLogger;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.reactivestreams.Subscriber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.function.Supplier;

import static com.bullit.networkprobe.configuration.ElasticsearchConfiguration.INDEX;

@Configuration
public class ReactiveStreamsConfiguration {

    @Bean
    public Subscriber<ConnectionResponse> getOutageSubscriber() {
        return new OutageSubscriber(getMdcLogger(), LocalDateTime::now, getDateFormatSupplier());
    }

    @Bean
    public Subscriber<ConnectionResponse> getConnectionSubscriber() {
        return new ConnectionSubscriber(getMdcLogger());
    }

    @Bean
    public Subscriber<ConnectionResponse> getElasticsearchSubscriber(@Autowired RestHighLevelClient restHighLevelClient) {
        return new ElasticsearchSubscriber(
                getMdcLogger(),
                new ElasticsearchClientWrapperImpl(restHighLevelClient),
                LocalDateTime::now,
                getDateFormatSupplier(),
                this::createIndexRequest
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
