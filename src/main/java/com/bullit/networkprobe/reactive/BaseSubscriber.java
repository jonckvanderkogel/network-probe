package com.bullit.networkprobe.reactive;

import com.bullit.networkprobe.support.MDCLogger;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.util.concurrent.Flow;
import java.util.stream.Stream;

import static com.bullit.networkprobe.support.MDCLogger.*;

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
            MDC.put(MDC_KEY, MDC_VALUE_ERRORS);
            log.error(String.format("%s threw an error", subscriberName));
            log.error(throwable.getMessage());
            Stream.of(throwable.getStackTrace()).forEach(l -> log.error(l.toString()));
        });
    }

    @Override
    public void onComplete() {
        mdcLogger.logWithMDCClearing(() -> {
            MDC.put(MDC_KEY, MDC_VALUE_SYSTEM);
            log.info(String.format("%s stopping", subscriberName));
        });
    }
}
