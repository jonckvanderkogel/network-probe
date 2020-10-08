package com.bullit.networkprobe.service;

import com.bullit.networkprobe.support.MDCLogger;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.jodah.concurrentunit.Waiter;
import org.junit.jupiter.api.Test;

import java.util.concurrent.*;
import java.util.function.BiFunction;

public class ConnectionServiceTests {
    public Executor getConnectionExecutor() {
        ThreadFactory threadFactory = new ThreadFactoryBuilder()
                .setNameFormat("connectionExecutor-%d")
                .setDaemon(false)
                .build();

        return Executors.newFixedThreadPool(2, threadFactory);
    }

    private BiFunction<String, String, ConnectionService> connectionServiceSupplier = (server1, server2) -> new ConnectionService(getConnectionExecutor(), server1, server2, 443, new MDCLogger());

    @Test
    public void testPerformConnection() throws TimeoutException, InterruptedException {
        var connectionService = connectionServiceSupplier.apply("intranet.ing.net", "intranet.ing.net");
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
