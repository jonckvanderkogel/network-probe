package com.bullit.caiwayprobe.service;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;

@Slf4j
@Component
public class PingScheduler {
    private final PingService pingService;
    private final SubmissionPublisher<PingResponse> pingResponsePublisher;

    public PingScheduler(@Autowired PingService pingService,
                         @Autowired @Qualifier("pingResponsePublisher") SubmissionPublisher<PingResponse> pingResponsePublisher,
                         @Autowired @Qualifier("outageSubscriber")Flow.Subscriber<PingResponse> outageSubscriber) {
        this.pingService = pingService;
        this.pingResponsePublisher = pingResponsePublisher;
        pingResponsePublisher.subscribe(outageSubscriber);
    }

    @Scheduled(fixedRate = 1000)
    public void performPings() {
        pingService
                .pingDnsServers()
                .thenAccept(r -> {
                    pingResponsePublisher.submit(r);
                    MDC.put("caiway-pinger", "pings");
                    MDC.put("reachable", String.valueOf(r.isReachable()));
                    MDC.put("responseTime", String.valueOf(r.getResponseTime()));
                    MDC.put("dnsServer", r.getDnsServerAddress());
                    log.info(
                        String.format("reachable: %s; duration: %s ms; server: %s",
                                r.isReachable(),
                                r.getResponseTime(),
                                r.getDnsServerAddress()
                        )
                    );
                });
    }
}
