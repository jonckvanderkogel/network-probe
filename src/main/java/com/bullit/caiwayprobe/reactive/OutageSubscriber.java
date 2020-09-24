package com.bullit.caiwayprobe.reactive;

import com.bullit.caiwayprobe.domain.PingResponse;
import com.bullit.caiwayprobe.support.MDCLogger;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.function.Supplier;

@Slf4j
public class OutageSubscriber extends BaseSubscriber<PingResponse> {
    private final OutageMarker outageMarker = new OutageMarker();
    // SimpleDateFormat is not thread safe so make sure to get a new instance every time you use it
    private final Supplier<SimpleDateFormat> dfSupplier = () -> new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss.SSS");
    private final Supplier<Date> dateSupplier;

    public OutageSubscriber(MDCLogger mdcLogger, Supplier<Date> dateSupplier) {
        super(mdcLogger,"OutageSubscriber");
        this.dateSupplier = dateSupplier;
    }

    @Override
    public void onNext(PingResponse item) {
        outageMarker.handleMessage(item).ifPresent(outage -> mdcLogger.logWithMDCClearing(() -> {
            MDC.put("caiway-pinger", "outages");
            MDC.put("from", dfSupplier.get().format(outage.getFrom()));
            MDC.put("to", dfSupplier.get().format(outage.getTo()));
            log.info("Outage");
        }));

        subscription.request(1);
    }

    @Override
    public void onComplete() {
        // Send one last message manually to ensure an ongoing outage will be logged on exit
        onNext(new PingResponse(true, 666, "shutdown signal"));
        super.onComplete();
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
    private class OutageMarker {
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
                return Optional.of(new Outage(startTime, dateSupplier.get()));
            } else {
                return Optional.empty();
            }
        }

        public Optional<Outage> notReachable() {
            if (!outageGoingOn) {
                outageGoingOn = true;
                startTime = dateSupplier.get();
            }
            return Optional.empty();
        }
    }
}