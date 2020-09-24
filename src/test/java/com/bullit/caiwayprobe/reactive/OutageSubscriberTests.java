package com.bullit.caiwayprobe.reactive;

import ch.qos.logback.classic.spi.ILoggingEvent;
import com.bullit.caiwayprobe.domain.PingResponse;
import com.bullit.caiwayprobe.support.MDCLogger;
import org.junit.jupiter.api.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static com.bullit.caiwayprobe.reactive.ReactiveTestSupport.setupAppender;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class OutageSubscriberTests {

    private Supplier<Date> createFixedDateSupplier() {
        Date date1 = new GregorianCalendar(2020, Calendar.SEPTEMBER, 23, 20, 50, 44).getTime();
        Date date2 = new GregorianCalendar(2020, Calendar.SEPTEMBER, 23, 20, 53, 22).getTime();
        final List<Date> dates = List.of(date1, date2);
        final AtomicInteger atomicInteger = new AtomicInteger();
        return () -> dates.get(atomicInteger.getAndAdd(1));
    }

    private PingResponse producePingResponse(boolean reachable) {
        return new PingResponse(reachable, 5, "foo");
    }

    @Test
    public void havingNonReachablePingResultsShouldResultInAnOutage() {
        var listAppender = setupAppender(OutageSubscriber.class);;
        var outageSubscriber = new OutageSubscriber(new MDCLogger(), createFixedDateSupplier());
        var pingResultPublisher = new SubmissionPublisher<PingResponse>();
        pingResultPublisher.subscribe(outageSubscriber);
        pingResultPublisher.submit(producePingResponse(true));
        pingResultPublisher.submit(producePingResponse(false));
        pingResultPublisher.submit(producePingResponse(true));
        pingResultPublisher.close();

        List<ILoggingEvent> logsList = listAppender.list;
        await().atMost(1, TimeUnit.SECONDS).untilAsserted(
                () -> {
                    var mdcMap = logsList.get(0).getMDCPropertyMap();
                    assertEquals("2020-09-23 20:50:44.000", mdcMap.get("from"));
                    assertEquals("2020-09-23 20:53:22.000", mdcMap.get("to"));
                    assertEquals("Outage", logsList.get(0).getMessage());
                }
        );
    }

    @Test
    public void terminatingPublisherDuringOutageShouldStillResultInAnOutage() {
        var listAppender = setupAppender(OutageSubscriber.class);
        var outageSubscriber = new OutageSubscriber(new MDCLogger(), createFixedDateSupplier());
        var pingResultPublisher = new SubmissionPublisher<PingResponse>();
        pingResultPublisher.subscribe(outageSubscriber);
        pingResultPublisher.submit(producePingResponse(true));
        pingResultPublisher.submit(producePingResponse(false));
        pingResultPublisher.close();

        List<ILoggingEvent> logsList = listAppender.list;
        await().atMost(1, TimeUnit.SECONDS).untilAsserted(
                () -> {
                    var mdcMap = logsList.get(0).getMDCPropertyMap();
                    assertEquals("2020-09-23 20:50:44.000", mdcMap.get("from"));
                    assertEquals("2020-09-23 20:53:22.000", mdcMap.get("to"));
                    assertEquals("Outage", logsList.get(0).getMessage());
                }
        );
    }
}
