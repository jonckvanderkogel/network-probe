package com.bullit.networkprobe.configuration;

import com.bullit.networkprobe.reactive.ElasticsearchSubscriber;
import com.bullit.networkprobe.reactive.OutageSubscriber;
import com.bullit.networkprobe.reactive.ConnectionResponsePublisher;
import com.bullit.networkprobe.reactive.ConnectionSubscriber;
import com.bullit.networkprobe.domain.ConnectionResponse;
import com.bullit.networkprobe.support.MDCLogger;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;
import java.util.function.Supplier;

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
    public Flow.Subscriber<ConnectionResponse> getElasticsearchSubscriber(@Autowired RestHighLevelClient restHighLevelClient) {
        return new ElasticsearchSubscriber(getMdcLogger(), restHighLevelClient, getDateFormatSupplier());
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
