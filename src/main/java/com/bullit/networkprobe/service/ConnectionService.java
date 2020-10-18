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

/*
TODO: add a shutdown hook that properly shuts down the Flux that is created here when the JVM shuts down
 */
@Slf4j
@Service
public class ConnectionService {
    private final String serverOne;
    private final String serverTwo;
    private final Integer timeOutMillis;

    private final Supplier<ConnectionResponse> defaultConnectionResponse = () -> new ConnectionResponse(false, 666, "none");

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

    /*
     * In order to get the timing of how long the request takes, we start a new timer and then zip the result of that
     * with the Mono that we get from the connect call.
     */
    private Mono<ConnectionResponse> timedConnection(String server, Integer timeOut) {
        Supplier<Timer> timer = Timer::new;

        return Mono
                .fromSupplier(timer)
                .zipWith(connect(server, timeOut))
                .map(tuple -> new ConnectionResponse(tuple.getT2().status().code() == 200, tuple.getT1().getTimeExpired(), server))
                .onErrorResume(e -> Mono.just(defaultConnectionResponse.get()));
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

    /*
     * We want to get the quickest response from the stream. That's why we do the "mergeWith", this combines the
     * two Mono's and creates a Flux from it where the Flux emits the first value from whichever Mono responds first.
     * We don't want to be logging both calls but just the one. That's why we filter out any non-reachable calls
     * and only take 1 element from this Flux. If neither Mono responds within 1 second we timeout and return the
     * ConnectionResponse from the onErrorResume. If both Mono's complete without returning a reachable result
     * the switchIfEmpty logic is invoked and we get the ConnectionResponse from that one.
     */
    public Flux<ConnectionResponse> connectToServers () {
        Flux<ConnectionResponse> mergedResponse = timedConnection(serverOne, timeOutMillis)
                .mergeWith(timedConnection(serverTwo, timeOutMillis))
                .filter(ConnectionResponse::isReachable)
                .takeUntil(ConnectionResponse::isReachable)
                .timeout(Duration.ofSeconds(1))
                .onErrorResume(e -> Mono.just(defaultConnectionResponse.get()))
                .switchIfEmpty(Mono.just(defaultConnectionResponse.get()));

        return Flux.interval(Duration.ofSeconds(1))
                .flatMap(ignored -> mergedResponse);
    }
}
