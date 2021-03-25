package com.bullit.networkprobe.reactive;

import com.bullit.networkprobe.domain.ConnectionResponse;
import com.bullit.networkprobe.support.MDCLogger;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.function.Supplier;

import static com.bullit.networkprobe.support.MDCLogger.MDC_KEY;
import static com.bullit.networkprobe.support.MDCLogger.MDC_VALUE_OUTAGES;

@Slf4j
public class OutageSubscriber extends BaseSubscriber<ConnectionResponse> {
    private final OutageMarker outageMarker = new OutageMarker();
    private final Supplier<ZonedDateTime> dateSupplier;
    private final DateTimeFormatter dateTimeFormatter;


    public OutageSubscriber(MDCLogger mdcLogger, Supplier<ZonedDateTime> dateSupplier, DateTimeFormatter dateTimeFormatter) {
        super(mdcLogger,"OutageSubscriber");
        this.dateSupplier = dateSupplier;
        this.dateTimeFormatter = dateTimeFormatter;
    }

    @Override
    public void performOnNext(ConnectionResponse item) {
        outageMarker.handleMessage(item).ifPresent(outage -> mdcLogger.logWithMDCClearing(() -> {
            MDC.put(MDC_KEY, MDC_VALUE_OUTAGES);
            MDC.put("from", dateTimeFormatter.format(outage.getFrom()));
            MDC.put("to", dateTimeFormatter.format(outage.getTo()));
            log.info("Outage");
        }));
    }

    @Override
    public void onComplete() {
        // Send one last message manually to ensure an ongoing outage will be logged on exit
        onNext(new ConnectionResponse(true, 666, "shutdown signal"));
        super.onComplete();
    }

    @Getter
    private static class Outage {
        private final ZonedDateTime from;
        private final ZonedDateTime to;

        public Outage(ZonedDateTime from, ZonedDateTime to) {
            this.from = from;
            this.to = to;
        }
    }

    private class OutageMarker {
        private boolean outageGoingOn = false;
        private ZonedDateTime startTime;

        public Optional<Outage> handleMessage(ConnectionResponse item) {
            return switch (item.getReachableState()) {
                case REACHABLE -> reachable();
                case NOT_REACHABLE -> notReachable();
            };
        }

        private Optional<Outage> reachable() {
            if (outageGoingOn) {
                outageGoingOn = false;
                return Optional.of(new Outage(startTime, dateSupplier.get()));
            } else {
                return Optional.empty();
            }
        }

        private Optional<Outage> notReachable() {
            if (!outageGoingOn) {
                outageGoingOn = true;
                startTime = dateSupplier.get();
            }
            return Optional.empty();
        }
    }
}
