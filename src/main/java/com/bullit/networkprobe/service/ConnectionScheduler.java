package com.bullit.networkprobe.service;

import com.bullit.networkprobe.domain.ConnectionResponse;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscriber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

@Slf4j
@Component
public class ConnectionScheduler {
    private final ConnectionService testConnectionService;
    private final List<Subscriber<ConnectionResponse>> subscribers;

    public ConnectionScheduler(@Autowired ConnectionService connectionService,
                               @Autowired List<Subscriber<ConnectionResponse>> subscribers) {
        this.testConnectionService = connectionService;
        this.subscribers = subscribers;
    }

    @PostConstruct
    public void performConnections() {
        subscribers.stream().forEach(sub -> testConnectionService.connectToServers().subscribe(sub));
    }
}
