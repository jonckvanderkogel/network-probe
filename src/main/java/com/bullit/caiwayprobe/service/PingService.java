package com.bullit.caiwayprobe.service;

import com.bullit.caiwayprobe.domain.PingResponse;
import com.bullit.caiwayprobe.support.MDCLogger;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Stream;

@Slf4j
@Service
public class PingService {
    private final String serverOne;
    private final String serverTwo;
    private final Executor pingExecutor;
    private final MDCLogger mdcLogger;

    public PingService(@Autowired @Qualifier("pingExecutor") Executor pingExecutor,
                       @Autowired @Qualifier("dnsServerOne") String dnsServerOne,
                       @Autowired @Qualifier("dnsServerTwo") String dnsServerTwo,
                       @Autowired @Qualifier("mdcLogger") MDCLogger mdcLogger) {
        log.info(String.format("Starting up PingService with servers: %s and %s", dnsServerOne, dnsServerTwo));
        this.pingExecutor = pingExecutor;
        this.serverOne = dnsServerOne;
        this.serverTwo = dnsServerTwo;
        this.mdcLogger = mdcLogger;
    }

    public CompletableFuture<PingResponse> pingDnsServers() {
        var pingFuture1 = CompletableFuture.supplyAsync(() -> performPing(serverOne), pingExecutor);
        var pingFuture2 = CompletableFuture.supplyAsync(() -> performPing(serverTwo), pingExecutor);

        return CompletableFuture
                .allOf(pingFuture1, pingFuture2)
                .thenApply(ignoredVoid -> pingFuture1
                        .join()
                        .orElseGet(() -> pingFuture2
                                .join()
                                .orElseGet(() -> new PingResponse(false, 666, "none"))
                        )
                );
    }

    private Optional<PingResponse> performPing(String ipAddress) {
        try {
            InetAddress address = InetAddress.getByName(ipAddress);
            Date now = new Date();
            boolean isReachable = address.isReachable(900);
            long timeToRespond = new Date().getTime() - now.getTime();

            return isReachable ? Optional.of(new PingResponse(true, timeToRespond, ipAddress)) : Optional.empty();
        } catch (IOException e) {
            mdcLogger.logWithMDCClearing(() -> {
                MDC.put("caiway-pinger", "errors");
                log.error(e.getMessage());
                Stream.of(e.getStackTrace()).forEach(l -> log.error(l.toString()));
            });

            return Optional.empty();
        }
    }
}
