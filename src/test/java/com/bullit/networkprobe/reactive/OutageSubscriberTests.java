package com.bullit.networkprobe.reactive;

import ch.qos.logback.classic.spi.ILoggingEvent;
import com.bullit.networkprobe.domain.ConnectionResponse;
import com.bullit.networkprobe.support.MDCLogger;
import org.junit.jupiter.api.Test;
import reactor.adapter.JdkFlowAdapter;

import java.util.List;
import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.TimeUnit;

import static com.bullit.networkprobe.reactive.ReactiveTestSupport.*;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class OutageSubscriberTests {

    private ConnectionResponse produceConnectionResponse(boolean reachable) {
        return new ConnectionResponse(reachable, 5, "foo");
    }

    @Test
    public void havingNonReachableConnectionResultsShouldResultInAnOutage() {
        var listAppender = setupAppender(OutageSubscriber.class);
        var outageSubscriber = new OutageSubscriber(new MDCLogger(), createFixedDateSupplier(), createDateFormatSupplier());
        var connectionResultPublisher = new SubmissionPublisher<ConnectionResponse>();
        JdkFlowAdapter.flowPublisherToFlux(connectionResultPublisher).subscribe(outageSubscriber);
//        connectionResultPublisher.subscribe(outageSubscriber);
        connectionResultPublisher.submit(produceConnectionResponse(true));
        connectionResultPublisher.submit(produceConnectionResponse(false));
        connectionResultPublisher.submit(produceConnectionResponse(true));
        connectionResultPublisher.close();

        List<ILoggingEvent> logsList = listAppender.list;
        await().atMost(1, TimeUnit.SECONDS).untilAsserted(
                () -> {
                    var mdcMap = logsList.get(0).getMDCPropertyMap();
                    assertEquals("2020-09-23T20:50:44.000+0200", mdcMap.get("from"));
                    assertEquals("2020-09-23T20:53:22.000+0200", mdcMap.get("to"));
                    assertEquals("Outage", logsList.get(0).getMessage());
                }
        );
    }

    @Test
    public void terminatingPublisherDuringOutageShouldStillResultInAnOutage() {
        var listAppender = setupAppender(OutageSubscriber.class);
        var outageSubscriber = new OutageSubscriber(new MDCLogger(), createFixedDateSupplier(), createDateFormatSupplier());
        var connectionResultPublisher = new SubmissionPublisher<ConnectionResponse>();
        JdkFlowAdapter.flowPublisherToFlux(connectionResultPublisher).subscribe(outageSubscriber);
//        connectionResultPublisher.subscribe(outageSubscriber);
        connectionResultPublisher.submit(produceConnectionResponse(true));
        connectionResultPublisher.submit(produceConnectionResponse(false));
        connectionResultPublisher.close();

        List<ILoggingEvent> logsList = listAppender.list;
        await().atMost(1, TimeUnit.SECONDS).untilAsserted(
                () -> {
                    var mdcMap = logsList.get(0).getMDCPropertyMap();
                    assertEquals("2020-09-23T20:50:44.000+0200", mdcMap.get("from"));
                    assertEquals("2020-09-23T20:53:22.000+0200", mdcMap.get("to"));
                    assertEquals("Outage", logsList.get(0).getMessage());
                }
        );
    }
}
