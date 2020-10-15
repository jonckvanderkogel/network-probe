package com.bullit.networkprobe.reactive;

import com.bullit.networkprobe.domain.ConnectionResponse;
import com.bullit.networkprobe.support.MDCLogger;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.function.Supplier;

import static com.bullit.networkprobe.support.MDCLogger.*;

@Slf4j
public class OutageSubscriber extends BaseSubscriber<ConnectionResponse> {
    private final OutageMarker outageMarker = new OutageMarker();
    // SimpleDateFormat is not thread safe so make sure to get a new instance every time you use it
    private final Supplier<SimpleDateFormat> dfSupplier;
    private final Supplier<Date> dateSupplier;

    public OutageSubscriber(MDCLogger mdcLogger, Supplier<Date> dateSupplier, Supplier<SimpleDateFormat> dateFormatSupplier) {
        super(mdcLogger,"OutageSubscriber");
        this.dateSupplier = dateSupplier;
        this.dfSupplier = dateFormatSupplier;
    }

    @Override
    public void performOnNext(ConnectionResponse item) {
        outageMarker.handleMessage(item).ifPresent(outage -> mdcLogger.logWithMDCClearing(() -> {
            MDC.put(MDC_KEY, MDC_VALUE_OUTAGES);
            MDC.put("from", dfSupplier.get().format(outage.getFrom()));
            MDC.put("to", dfSupplier.get().format(outage.getTo()));
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
        private final Date from;
        private final Date to;

        public Outage(Date from, Date to) {
            this.from = from;
            this.to = to;
        }
    }

    @Getter
    private class OutageMarker {
        private boolean outageGoingOn = false;
        private Date startTime;

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
