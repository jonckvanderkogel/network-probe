package com.bullit.networkprobe.service;

import com.bullit.networkprobe.domain.ConnectionResponse;
import com.bullit.networkprobe.support.MDCLogger;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.jodah.concurrentunit.Waiter;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.util.concurrent.*;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import static com.bullit.networkprobe.reactive.ReactiveTestSupport.createTestExecutor;

public class ConnectionServiceTests {

    private BiFunction<String, String, ConnectionService> connectionServiceSupplier = (server1, server2) -> new ConnectionService(server1, server2, 900, new MDCLogger());

    @Test
    public void testPerformConnection() throws TimeoutException, InterruptedException {
        //var connectionService = connectionServiceSupplier.apply("https://www.google.com", "https://www.nu.nl");
//        var waiter = new Waiter();
//        connectionService
//                .connectToServers()
//                .whenComplete((connectionResponse, e) -> {
//                    waiter.assertTrue(connectionResponse.isReachable());
//                    waiter.assertNull(e);
//                    waiter.resume();
//                });
//
//        waiter.await(2, TimeUnit.SECONDS);
        var connectionService = new ConnectionService("https://www.google.com", "https://www.nu.nl", 900, new MDCLogger());
        Predicate<ConnectionResponse> responsePredicate = ConnectionResponse::isReachable;

        StepVerifier
                .create(connectionService.connectToServers())
                .expectNextMatches(responsePredicate)
                .thenCancel()
                .verify();
    }
}
