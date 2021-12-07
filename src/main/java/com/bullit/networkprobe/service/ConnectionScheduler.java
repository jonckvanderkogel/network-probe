package com.bullit.networkprobe.service;

import com.bullit.networkprobe.domain.ConnectionResponse;
import lombok.RequiredArgsConstructor;
import org.reactivestreams.Subscriber;
import reactor.core.publisher.Flux;

import javax.annotation.PostConstruct;
import java.util.List;

@RequiredArgsConstructor
public class ConnectionScheduler {
    private final Flux<ConnectionResponse> connectionResponseFlux;
    private final List<Subscriber<ConnectionResponse>> subscribers;

    @PostConstruct
    public void performConnections() {
        subscribers
                .forEach(connectionResponseFlux::subscribe);
    }
}
