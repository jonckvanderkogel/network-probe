package com.bullit.networkprobe.support;

import org.slf4j.MDC;

/**
Since Reactive Streams does not play nice with Thread Local implementations such as MDC we have to make sure the MDC
 context is cleared before logging.
 */
public class MDCLogger {
    public void logWithMDCClearing(Runnable runnable) {
        MDC.clear();
        runnable.run();
    }
}
