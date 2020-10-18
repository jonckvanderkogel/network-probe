package com.bullit.networkprobe.service;

import com.bullit.networkprobe.domain.ConnectionResponse;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscriber;
import reactor.core.publisher.Flux;

import javax.annotation.PostConstruct;
import java.util.List;

@Slf4j
public class ConnectionScheduler {
    private final ConnectionService testConnectionService;
    private final List<Subscriber<ConnectionResponse>> subscribers;

    public ConnectionScheduler(ConnectionService connectionService,
                               List<Subscriber<ConnectionResponse>> subscribers) {
        this.testConnectionService = connectionService;
        this.subscribers = subscribers;
    }

    @PostConstruct
    public void performConnections() {
        Flux<ConnectionResponse> connectionResponseFlux = testConnectionService.connectToServers();

        subscribers
                .stream()
                .forEach(sub -> connectionResponseFlux.subscribe(sub));
    }
}
