package com.bullit.caiwayprobe.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PingScheduler {
    private PingService pingService;

    public PingScheduler(@Autowired PingService pingService) {
        this.pingService = pingService;
    }

    @Scheduled(fixedRate = 1000)
    public void performPings() {
        pingService
                .pingDnsServers()
                .thenAccept(r -> log.info(
                        String.format("reachable: %s; duration: %s ms; server: %s",
                                r.isReachable(),
                                r.getResponseTime(),
                                r.getDnsServerAddress()
                        )
                ));
    }
}
