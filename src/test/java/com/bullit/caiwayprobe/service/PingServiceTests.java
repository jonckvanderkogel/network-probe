package com.bullit.caiwayprobe.service;

import com.bullit.caiwayprobe.support.MDCLogger;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.jodah.concurrentunit.Waiter;
import org.junit.jupiter.api.Test;

import java.util.concurrent.*;
import java.util.function.BiFunction;

public class PingServiceTests {
    public Executor getPingExecutor() {
        ThreadFactory threadFactory = new ThreadFactoryBuilder()
                .setNameFormat("pingExecutor-%d")
                .setDaemon(false)
                .build();
        ExecutorService executorService = Executors.newFixedThreadPool(2, threadFactory);

        return executorService;
    }

    private BiFunction<String, String, PingService> pingServiceSupplier = (dnsServer1, dnsServer2) -> new PingService(getPingExecutor(), dnsServer1, dnsServer2, new MDCLogger());

    @Test
    public void testPerformPing() throws TimeoutException, InterruptedException {
        var pingService = pingServiceSupplier.apply("127.0.0.1", "127.0.0.1");
        var waiter = new Waiter();
        pingService
                .pingDnsServers()
                .whenComplete((pingResponse, e) -> {
                    waiter.assertTrue(pingResponse.isReachable());
                    waiter.assertNull(e);
                    waiter.resume();
                });

        waiter.await(2, TimeUnit.SECONDS);
    }
}
