package com.bullit.caiwayprobe.reactive;

import com.bullit.caiwayprobe.service.PingResponse;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.Flow;
import java.util.stream.Stream;

@Slf4j
public class OutageSubscriber implements Flow.Subscriber<PingResponse> {
    private Flow.Subscription subscription;
    private final OutageMarker outageMarker = new OutageMarker();
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss.SSS");

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        this.subscription = subscription;
        subscription.request(1);
    }

    @Override
    public void onNext(PingResponse item) {
        outageMarker.handleMessage(item).ifPresent(outage -> {
            MDC.put("caiway-pinger", "outages");
            MDC.put("from", df.format(outage.getFrom()));
            MDC.put("to", df.format(outage.getTo()));
            log.info("Outage");
        });

        subscription.request(1);
    }

    @Override
    public void onError(Throwable throwable) {
        MDC.put("caiway-pinger", "errors");
        log.error(throwable.getMessage());
        Stream.of(throwable.getStackTrace()).forEach(l -> log.error(l.toString()));
    }

    @Override
    public void onComplete() {
        // Send one last message manually to ensure an ongoing outage will be logged on exit
        onNext(new PingResponse(true, 666, "shutdown signal"));
        MDC.put("caiway-pinger", "system");
        log.info("OutageSubscriber stopping");
    }

    @Getter
    private static class Outage {
        private final Date from;
        private final Date to;

        public Outage(Date from, Date to) {
            this.from = from;
            this.to = to;
        }
    }

    @Setter
    @Getter
    private static class OutageMarker {
        private boolean outageGoingOn = false;
        private Date startTime;

        public Optional<Outage> handleMessage(PingResponse item) {
            return switch (item.getReachableState()) {
                case REACHABLE -> reachable();
                case NOT_REACHABLE -> notReachable();
            };
        }

        public Optional<Outage> reachable() {
            if (outageGoingOn) {
                outageGoingOn = false;
                return Optional.of(new Outage(startTime, new Date()));
            } else {
                return Optional.empty();
            }
        }

        public Optional<Outage> notReachable() {
            if (!outageGoingOn) {
                outageGoingOn = true;
                startTime = new Date();
            }
            return Optional.empty();
        }
    }
}
