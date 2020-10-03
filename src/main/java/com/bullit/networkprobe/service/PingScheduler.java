package com.bullit.networkprobe.service;

import com.bullit.networkprobe.domain.PingResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;

@Slf4j
@Component
public class PingScheduler {
    private final PingService pingService;
    private final SubmissionPublisher<PingResponse> pingResponsePublisher;

    public PingScheduler(@Autowired PingService pingService,
                         @Autowired SubmissionPublisher<PingResponse> pingResponsePublisher,
                         @Autowired List<Flow.Subscriber<PingResponse>> subscribers) {
        this.pingService = pingService;
        this.pingResponsePublisher = pingResponsePublisher;
        subscribers.stream().forEach(pingResponsePublisher::subscribe);
    }

    @Scheduled(fixedRate = 1000)
    public void performPings() {
        pingService
                .pingDnsServers()
                .thenAccept(pingResponsePublisher::submit);
    }
}
