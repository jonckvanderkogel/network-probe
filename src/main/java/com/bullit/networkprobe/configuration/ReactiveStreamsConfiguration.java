package com.bullit.networkprobe.configuration;

import com.bullit.networkprobe.reactive.OutageSubscriber;
import com.bullit.networkprobe.reactive.PingResponsePublisher;
import com.bullit.networkprobe.reactive.PingSubscriber;
import com.bullit.networkprobe.domain.PingResponse;
import com.bullit.networkprobe.support.MDCLogger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Date;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;

@Configuration
public class ReactiveStreamsConfiguration {

    @Bean
    public SubmissionPublisher<PingResponse> getPingResponsePublisher() {
        return new PingResponsePublisher();
    }

    @Bean
    public Flow.Subscriber<PingResponse> getOutageSubscriber() {
        return new OutageSubscriber(getMdcLogger(), () -> new Date());
    }

    @Bean
    public Flow.Subscriber<PingResponse> getPingSubscriber() {
        return new PingSubscriber(getMdcLogger());
    }

    @Bean
    public MDCLogger getMdcLogger() {
        return new MDCLogger();
    }
}
