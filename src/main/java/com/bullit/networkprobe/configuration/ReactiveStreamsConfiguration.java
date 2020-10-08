package com.bullit.networkprobe.configuration;

import com.bullit.networkprobe.reactive.OutageSubscriber;
import com.bullit.networkprobe.reactive.ConnectionResponsePublisher;
import com.bullit.networkprobe.reactive.ConnectionSubscriber;
import com.bullit.networkprobe.domain.ConnectionResponse;
import com.bullit.networkprobe.support.MDCLogger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Date;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;

@Configuration
public class ReactiveStreamsConfiguration {

    @Bean
    public SubmissionPublisher<ConnectionResponse> getConnectionResponsePublisher() {
        return new ConnectionResponsePublisher();
    }

    @Bean
    public Flow.Subscriber<ConnectionResponse> getOutageSubscriber() {
        return new OutageSubscriber(getMdcLogger(), () -> new Date());
    }

    @Bean
    public Flow.Subscriber<ConnectionResponse> getConnectionSubscriber() {
        return new ConnectionSubscriber(getMdcLogger());
    }

    @Bean
    public MDCLogger getMdcLogger() {
        return new MDCLogger();
    }
}
