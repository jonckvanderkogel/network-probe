package com.bullit.networkprobe.service;

import com.bullit.networkprobe.domain.ConnectionResponse;
import com.bullit.networkprobe.support.MDCLogger;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.util.function.BiFunction;
import java.util.function.Predicate;

public class ConnectionServiceTests {

    private BiFunction<String, String, ConnectionService> connectionServiceSupplier = (server1, server2) -> new ConnectionService(server1, server2, 900, new MDCLogger());

    @Test
    public void testPerformConnection() {
        var connectionService = new ConnectionService("https://www.google.com", "https://www.nu.nl", 900, new MDCLogger());
        Predicate<ConnectionResponse> responsePredicate = ConnectionResponse::isReachable;

        StepVerifier
                .create(connectionService.connectToServers())
                .expectNextMatches(responsePredicate)
                .thenCancel()
                .verify();
    }
}
