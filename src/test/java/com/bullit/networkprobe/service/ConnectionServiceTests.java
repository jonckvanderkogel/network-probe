package com.bullit.networkprobe.service;

import com.bullit.networkprobe.support.MDCLogger;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.jodah.concurrentunit.Waiter;
import org.junit.jupiter.api.Test;

import java.util.concurrent.*;
import java.util.function.BiFunction;

import static com.bullit.networkprobe.reactive.ReactiveTestSupport.createTestExecutor;

public class ConnectionServiceTests {

    private BiFunction<String, String, ConnectionService> connectionServiceSupplier = (server1, server2) -> new ConnectionService(createTestExecutor(), server1, server2, 443, 900, new MDCLogger());

    @Test
    public void testPerformConnection() throws TimeoutException, InterruptedException {
        var connectionService = connectionServiceSupplier.apply("www.google.com", "www.nu.nl");
        var waiter = new Waiter();
        connectionService
                .connectToServers()
                .whenComplete((connectionResponse, e) -> {
                    waiter.assertTrue(connectionResponse.isReachable());
                    waiter.assertNull(e);
                    waiter.resume();
                });

        waiter.await(2, TimeUnit.SECONDS);
    }
}
