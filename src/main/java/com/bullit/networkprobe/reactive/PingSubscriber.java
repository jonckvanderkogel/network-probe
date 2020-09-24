package com.bullit.networkprobe.reactive;

import com.bullit.networkprobe.domain.PingResponse;
import com.bullit.networkprobe.support.MDCLogger;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import static com.bullit.networkprobe.support.MDCLogger.MDC_KEY;
import static com.bullit.networkprobe.support.MDCLogger.MDC_VALUE_PINGS;

@Slf4j
public class PingSubscriber extends BaseSubscriber<PingResponse> {
    public PingSubscriber(MDCLogger mdcLogger) {
        super(mdcLogger,"PingSubscriber");
    }

    @Override
    public void onNext(PingResponse item) {
        mdcLogger.logWithMDCClearing(() -> {
            MDC.put(MDC_KEY, MDC_VALUE_PINGS);
            MDC.put("reachable", String.valueOf(item.isReachable()));
            MDC.put("responseTime", String.valueOf(item.getResponseTime()));
            MDC.put("dnsServer", item.getDnsServerAddress());
            log.info(
                    String.format("reachable: %s; duration: %s ms; server: %s",
                            item.isReachable(),
                            item.getResponseTime(),
                            item.getDnsServerAddress()
                    )
            );
        });

        subscription.request(1);
    }
}
