package com.bullit.caiwayprobe.reactive;

import com.bullit.caiwayprobe.support.MDCLogger;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.util.concurrent.Flow;
import java.util.stream.Stream;

@Slf4j
public abstract class BaseSubscriber<T> implements Flow.Subscriber<T> {
    protected Flow.Subscription subscription;
    private final String subscriberName;
    protected final MDCLogger mdcLogger;

    public BaseSubscriber(MDCLogger mdcLogger, String subscriberName) {
        this.mdcLogger = mdcLogger;
        this.subscriberName = subscriberName;
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        this.subscription = subscription;
        subscription.request(1);
    }

    @Override
    public void onError(Throwable throwable) {
        mdcLogger.logWithMDCClearing(() -> {
            MDC.put("caiway-pinger", "errors");
            log.error(String.format("%s threw an error", subscriberName));
            log.error(throwable.getMessage());
            Stream.of(throwable.getStackTrace()).forEach(l -> log.error(l.toString()));
        });
    }

    @Override
    public void onComplete() {
        mdcLogger.logWithMDCClearing(() -> {
            MDC.put("caiway-pinger", "system");
            log.info(String.format("%s stopping", subscriberName));
        });
    }
}
