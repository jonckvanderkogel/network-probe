package com.bullit.caiwayprobe.reactive;

import ch.qos.logback.classic.spi.ILoggingEvent;
import com.bullit.caiwayprobe.domain.PingResponse;
import com.bullit.caiwayprobe.support.MDCLogger;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.TimeUnit;

import static com.bullit.caiwayprobe.reactive.ReactiveTestSupport.setupAppender;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PingSubscriberTests {

    @Test
    public void whenReceivingPingResponseShouldLog() {
        var listAppender = setupAppender(PingSubscriber.class);
        var pingSubscriber = new PingSubscriber(new MDCLogger());
        var pingResultPublisher = new SubmissionPublisher<PingResponse>();
        pingResultPublisher.subscribe(pingSubscriber);
        pingResultPublisher.submit(new PingResponse(true, 123, "foo"));
        pingResultPublisher.close();

        List<ILoggingEvent> logsList = listAppender.list;
        await().atMost(1, TimeUnit.SECONDS).untilAsserted(
                () -> {
                    assertEquals("reachable: true; duration: 123 ms; server: foo", logsList.get(0).getMessage());
                }
        );
    }
}
