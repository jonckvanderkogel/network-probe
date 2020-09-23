package com.bullit.caiwayprobe.configuration;

import com.bullit.caiwayprobe.reactive.OutageSubscriber;
import com.bullit.caiwayprobe.reactive.PingResponsePublisher;
import com.bullit.caiwayprobe.reactive.PingSubscriber;
import com.bullit.caiwayprobe.domain.PingResponse;
import com.bullit.caiwayprobe.support.MDCLogger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Date;
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
        return new OutageSubscriber(getMdcLogger(), () -> new Date());
    }

    @Bean(name="pingSubscriber")
    public Flow.Subscriber<PingResponse> getPingSubscriber() {
        return new PingSubscriber(getMdcLogger());
    }

    @Bean(name="mdcLogger")
    public MDCLogger getMdcLogger() {
        return new MDCLogger();
    }
}
