package com.bullit.caiwayprobe.configuration;

import com.bullit.caiwayprobe.reactive.OutageSubscriber;
import com.bullit.caiwayprobe.reactive.PingResponsePublisher;
import com.bullit.caiwayprobe.service.PingResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;

@Configuration
public class ReactiveStreamsConfiguration {

    @Bean(name="pingResponsePublisher")
    public SubmissionPublisher<PingResponse> getPingResponsePublisher() {
        return new PingResponsePublisher();
    }

    @Bean(name="outageSubscriber")
    public Flow.Subscriber<PingResponse> getOutageSubscriber() {
        return new OutageSubscriber();
    }
}
