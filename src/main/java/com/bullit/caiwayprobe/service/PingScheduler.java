package com.bullit.caiwayprobe.service;

import com.bullit.caiwayprobe.domain.PingResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;

@Slf4j
@Component
public class PingScheduler {
    private final PingService pingService;
    private final SubmissionPublisher<PingResponse> pingResponsePublisher;

    public PingScheduler(@Autowired PingService pingService,
                         @Autowired @Qualifier("pingResponsePublisher") SubmissionPublisher<PingResponse> pingResponsePublisher,
                         @Autowired @Qualifier("outageSubscriber") Flow.Subscriber<PingResponse> outageSubscriber,
                         @Autowired @Qualifier("pingSubscriber") Flow.Subscriber<PingResponse> pingSubscriber) {
        this.pingService = pingService;
        this.pingResponsePublisher = pingResponsePublisher;
        pingResponsePublisher.subscribe(outageSubscriber);
        pingResponsePublisher.subscribe(pingSubscriber);
    }

    @Scheduled(fixedRate = 1000)
    public void performPings() {
        pingService
                .pingDnsServers()
                .thenAccept(r -> {
                    pingResponsePublisher.submit(r);
                });
    }
}
