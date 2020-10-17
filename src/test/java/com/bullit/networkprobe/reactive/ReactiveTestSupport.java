package com.bullit.networkprobe.reactive;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class ReactiveTestSupport {

    public static <T> ListAppender<ILoggingEvent> setupAppender(Class<T> loggerTest) {
        Logger testLogger = (Logger) LoggerFactory.getLogger(loggerTest);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        testLogger.addAppender(listAppender);
        return listAppender;
    }

    public static Supplier<Date> createFixedDateSupplier() {
        Date date1 = new GregorianCalendar(2020, Calendar.SEPTEMBER, 23, 20, 50, 44).getTime();
        Date date2 = new GregorianCalendar(2020, Calendar.SEPTEMBER, 23, 20, 53, 22).getTime();
        final List<Date> dates = List.of(date1, date2);
        final AtomicInteger atomicInteger = new AtomicInteger();
        return () -> dates.get(atomicInteger.getAndAdd(1));
    }

    public static Supplier<SimpleDateFormat> createDateFormatSupplier() {
        return () -> new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    }
}
