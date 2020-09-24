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
