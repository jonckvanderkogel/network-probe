package com.bullit.networkprobe.service;

import com.bullit.networkprobe.domain.ConnectionResponse;
import com.bullit.networkprobe.support.MDCLogger;
import io.netty.channel.ChannelOption;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.client.HttpClientResponse;

import java.time.Duration;
import java.util.Date;
import java.util.function.Supplier;

import static com.bullit.networkprobe.support.MDCLogger.MDC_KEY;
import static com.bullit.networkprobe.support.MDCLogger.MDC_VALUE_SYSTEM;

@Slf4j
@Service
public class ConnectionService {
    private final String serverOne;
    private final String serverTwo;
    private final Integer timeOutMillis;

    public ConnectionService(@Autowired @Qualifier("connectionServerOne") String serverOne,
                             @Autowired @Qualifier("connectionServerTwo") String serverTwo,
                             @Autowired @Qualifier("timeOutMillis") Integer timeOutMillis,
                             @Autowired MDCLogger mdcLogger) {
        this.serverOne = serverOne;
        this.serverTwo = serverTwo;
        this.timeOutMillis = timeOutMillis;

        mdcLogger.logWithMDCClearing(() -> {
            MDC.put(MDC_KEY, MDC_VALUE_SYSTEM);
            log.info(String.format("Starting up ConnectionService with servers: %s and %s", serverOne, serverTwo));
        });
    }

    private static class Timer {
        private final Date start = new Date();

        public long getTimeExpired() {
            return new Date().getTime() - start.getTime();
        }
    }

    private Mono<ConnectionResponse> timedConnection(String server, Integer timeOut) {
        Supplier<Timer> timer = () -> new Timer();

        return Mono
                .fromSupplier(timer)
                .zipWith(connect(server, timeOut))
                .map(tuple -> new ConnectionResponse(tuple.getT2().status().code() == 200, tuple.getT1().getTimeExpired(), server))
                .onErrorResume(e -> Mono.just(new ConnectionResponse(false, 666, "none")));
    }

    private Mono<HttpClientResponse> connect(String server, Integer timeOut) {
        return HttpClient.create()
                .tcpConfiguration(tcpClient ->  tcpClient
                        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, timeOut)
                )
                .get()
                .uri(server)
                .response();
    }

    public Flux<ConnectionResponse> connectToServers () {
        Mono<ConnectionResponse> response1 = timedConnection(serverOne, timeOutMillis);
        Mono<ConnectionResponse> response2 = timedConnection(serverTwo, timeOutMillis);

        Flux<ConnectionResponse> mergedResponse = response1
                .mergeWith(response2)
                .filter(ConnectionResponse::isReachable)
                .takeUntil(ConnectionResponse::isReachable)
                .timeout(Duration.ofSeconds(1))
                .switchIfEmpty(Mono.just(new ConnectionResponse(false, 666, "none")));

        return Flux.interval(Duration.ofSeconds(1))
                .flatMap(ignored -> mergedResponse);
    }
}
