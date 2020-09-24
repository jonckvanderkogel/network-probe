package com.bullit.networkprobe.reactive;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.slf4j.LoggerFactory;

public class ReactiveTestSupport {
    public static <T> ListAppender<ILoggingEvent> setupAppender(Class<T> loggerTest) {
        Logger testLogger = (Logger) LoggerFactory.getLogger(loggerTest);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        testLogger.addAppender(listAppender);
        return listAppender;
    }
}
