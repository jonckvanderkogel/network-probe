package com.bullit.networkprobe.service;

import com.bullit.networkprobe.domain.ConnectionResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;

@Slf4j
@Component
public class ConnectionScheduler {
    private final ConnectionService testConnectionService;
    private final SubmissionPublisher<ConnectionResponse> connectionResponsePublisher;

    public ConnectionScheduler(@Autowired ConnectionService connectionService,
                               @Autowired SubmissionPublisher<ConnectionResponse> connectionResponsePublisher,
                               @Autowired List<Flow.Subscriber<ConnectionResponse>> subscribers) {
        this.testConnectionService = connectionService;
        this.connectionResponsePublisher = connectionResponsePublisher;
        subscribers.stream().forEach(connectionResponsePublisher::subscribe);
    }

    @Scheduled(fixedRate = 1000)
    public void performConnections() {
        testConnectionService
                .connectToServers()
                .thenAccept(connectionResponsePublisher::submit);
    }
}
