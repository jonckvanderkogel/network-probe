package com.bullit.networkprobe.service;

import com.bullit.networkprobe.domain.ConnectionResponse;
import com.bullit.networkprobe.support.MDCLogger;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Stream;

import static com.bullit.networkprobe.support.MDCLogger.*;

@Slf4j
@Service
public class ConnectionService {
    private final String serverOne;
    private final String serverTwo;
    private final Integer port;
    private final Executor connectionExecutor;
    private final MDCLogger mdcLogger;

    public ConnectionService(@Autowired @Qualifier("connectionExecutor") Executor connectionExecutor,
                             @Autowired @Qualifier("connectionServerOne") String serverOne,
                             @Autowired @Qualifier("connectionServerTwo") String serverTwo,
                             @Autowired @Qualifier("port") Integer port,
                             @Autowired MDCLogger mdcLogger) {
        log.info(String.format("Starting up ConnectionService with servers: %s and %s", serverOne, serverTwo));
        this.connectionExecutor = connectionExecutor;
        this.serverOne = serverOne;
        this.serverTwo = serverTwo;
        this.port = port;
        this.mdcLogger = mdcLogger;
    }

    public CompletableFuture<ConnectionResponse> connectToServers() {
        var connectionFuture1 = CompletableFuture.supplyAsync(() -> performConnection(serverOne, port), connectionExecutor);
        var connectionFuture2 = CompletableFuture.supplyAsync(() -> performConnection(serverTwo, port), connectionExecutor);

        return CompletableFuture
                .allOf(connectionFuture1, connectionFuture2)
                .thenApply(ignoredVoid -> connectionFuture1
                        .join()
                        .orElseGet(() -> connectionFuture2
                                .join()
                                .orElseGet(() -> new ConnectionResponse(false, 666, "none"))
                        )
                );
    }

    private boolean isReachable(String ipAddress, Integer port, int timeOutMillis) {
        try (Socket soc = new Socket()) {
            soc.connect(new InetSocketAddress(ipAddress, port), timeOutMillis);
        } catch (IOException e) {
            mdcLogger.logWithMDCClearing(() -> {
                MDC.put(MDC_KEY, MDC_VALUE_ERRORS);
                log.error(e.getMessage());
                Stream.of(e.getStackTrace()).forEach(l -> log.debug(l.toString()));
            });

            return false;
        }
        return true;
    }

    private Optional<ConnectionResponse> performConnection(String ipAddress, Integer port) {
        Date now = new Date();
        boolean isReachable = isReachable(ipAddress, port,900);
        long timeToRespond = new Date().getTime() - now.getTime();

        return isReachable ? Optional.of(new ConnectionResponse(true, timeToRespond, ipAddress)) : Optional.empty();
    }
}
