package com.bullit.networkprobe.support;

import org.slf4j.MDC;

/**
Since Reactive Streams processes are not guaranteed to run on any specific thread we have to make sure the MDC
 context is cleared before logging.
 */
public class MDCLogger {
    public static final String MDC_KEY = "network-probe";
    public static final String MDC_VALUE_SYSTEM = "system";
    public static final String MDC_VALUE_ERRORS = "errors";
    public static final String MDC_VALUE_PINGS = "pings";
    public static final String MDC_VALUE_OUTAGES = "outages";

    public void logWithMDCClearing(Runnable runnable) {
        MDC.clear();
        runnable.run();
    }
}
