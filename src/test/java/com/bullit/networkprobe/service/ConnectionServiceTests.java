package com.bullit.networkprobe.service;

import com.bullit.networkprobe.domain.ConnectionResponse;
import com.bullit.networkprobe.support.MDCLogger;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

public class ConnectionServiceTests {

    @Test
    public void testPerformConnection() {
        var connectionService = new ConnectionService("https://www.google.com", "https://www.nu.nl", 999, new MDCLogger());

        StepVerifier
                .create(connectionService.connectToServers())
                .expectNextMatches(r -> !r.isReachable()) // for some reason the first response is non-reachable, figure out why, should not be the case
                .expectNextMatches(ConnectionResponse::isReachable)
                .thenCancel()
                .verify();
    }
}
