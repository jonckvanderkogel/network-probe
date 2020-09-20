package com.bullit.caiwayprobe.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Stream;

@Slf4j
@Service
public class PingService {
    private static final String DNS_1 = "62.45.70.117";
    private static final String DNS_2 = "62.45.71.117";
    private final Executor pingExecutor;

    public PingService(@Autowired @Qualifier("pingExecutor") Executor pingExecutor) {
        this.pingExecutor = pingExecutor;
    }

    public CompletableFuture<PingResponse> pingDnsServers() {
        var pingFuture1 = CompletableFuture.supplyAsync(() -> performPing(DNS_1), pingExecutor);
        var pingFuture2 = CompletableFuture.supplyAsync(() -> performPing(DNS_2), pingExecutor);

        return CompletableFuture
                .allOf(pingFuture1, pingFuture2)
                .thenApply(ignoredVoid -> pingFuture1
                        .join()
                        .orElseGet(() -> pingFuture2
                                .join()
                                .orElse(new PingResponse(false, 400))
                        )
                );
    }

    private Optional<PingResponse> performPing(String ipAddress) {
        try {
            InetAddress address = InetAddress.getByName(ipAddress);
            Date now = new Date();
            boolean isReachable = address.isReachable(400);
            long timeToRespond = new Date().getTime() - now.getTime();

            return Optional.of(new PingResponse(isReachable, timeToRespond));
        } catch (IOException e) {
            log.error(e.getMessage());
            Stream.of(e.getStackTrace()).forEach(l -> log.error(l.toString()));
            return Optional.empty();
        }
    }
}
