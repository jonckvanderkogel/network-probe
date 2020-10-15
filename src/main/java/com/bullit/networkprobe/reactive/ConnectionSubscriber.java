package com.bullit.networkprobe.reactive;

import com.bullit.networkprobe.domain.ConnectionResponse;
import com.bullit.networkprobe.support.MDCLogger;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import static com.bullit.networkprobe.support.MDCLogger.*;

@Slf4j
public class ConnectionSubscriber extends BaseSubscriber<ConnectionResponse> {
    public ConnectionSubscriber(MDCLogger mdcLogger) {
        super(mdcLogger,"ConnectionSubscriber");
    }

    @Override
    public void performOnNext(ConnectionResponse item) {
        mdcLogger.logWithMDCClearing(() -> {
            MDC.put(MDC_KEY, MDC_VALUE_CONNECTIONS);
            MDC.put("reachable", String.valueOf(item.isReachable()));
            MDC.put("responseTime", String.valueOf(item.getResponseTime()));
            MDC.put("server", item.getServer());
            log.info(
                    String.format("reachable: %s; duration: %s ms; server: %s",
                            item.isReachable(),
                            item.getResponseTime(),
                            item.getServer()
                    )
            );
        });
    }
}
