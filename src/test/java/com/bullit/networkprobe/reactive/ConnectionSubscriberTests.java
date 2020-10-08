package com.bullit.networkprobe.reactive;

import ch.qos.logback.classic.spi.ILoggingEvent;
import com.bullit.networkprobe.domain.ConnectionResponse;
import com.bullit.networkprobe.support.MDCLogger;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.TimeUnit;

import static com.bullit.networkprobe.reactive.ReactiveTestSupport.setupAppender;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConnectionSubscriberTests {

    @Test
    public void whenReceivingConnectionResponseShouldLog() {
        var listAppender = setupAppender(ConnectionSubscriber.class);
        var connectionSubscriber = new ConnectionSubscriber(new MDCLogger());
        var connectionResultPublisher = new SubmissionPublisher<ConnectionResponse>();
        connectionResultPublisher.subscribe(connectionSubscriber);
        connectionResultPublisher.submit(new ConnectionResponse(true, 123, "foo"));
        connectionResultPublisher.close();

        List<ILoggingEvent> logsList = listAppender.list;
        await().atMost(1, TimeUnit.SECONDS).untilAsserted(
                () -> assertEquals("reachable: true; duration: 123 ms; server: foo", logsList.get(0).getMessage())
        );
    }
}
