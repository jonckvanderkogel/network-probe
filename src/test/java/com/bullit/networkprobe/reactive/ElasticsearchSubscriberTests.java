package com.bullit.networkprobe.reactive;

import ch.qos.logback.classic.spi.ILoggingEvent;
import com.bullit.networkprobe.domain.ConnectionResponse;
import com.bullit.networkprobe.support.MDCLogger;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Cancellable;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.TimeUnit;

import static com.bullit.networkprobe.reactive.ReactiveTestSupport.*;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class ElasticsearchSubscriberTests {

    @Test
    public void whenReceivingMessageShouldSendToElasticsearch() {
        var esClienWrapperMock = mock(ElasticsearchClientWrapper.class);
        var elasticsearchSubscriber = new ElasticsearchSubscriber(
                new MDCLogger(),
                esClienWrapperMock,
                createFixedDateSupplier(),
                createDateFormatSupplier(),
                (connectionResponse, timestamp) -> mock(IndexRequest.class));
        var connectionResultPublisher = new SubmissionPublisher<ConnectionResponse>();
        connectionResultPublisher.subscribe(elasticsearchSubscriber);
        connectionResultPublisher.submit(new ConnectionResponse(true, 123, "foo"));
        connectionResultPublisher.close();

        when(esClienWrapperMock.indexAsync(any(), any(), any())).thenReturn(mock(Cancellable.class));

        await().atMost(1, TimeUnit.SECONDS).untilAsserted(
                () -> verify(esClienWrapperMock, times(1)).indexAsync(any(), any(), any())
        );
    }

    @Test
    public void whenRestClientHasExceptionWeShouldGetTheLineInTheMissedLogs() {
        var listAppender = setupAppender(ElasticsearchSubscriber.class);

        var elasticsearchSubscriber = new ElasticsearchSubscriber(
                new MDCLogger(),
                (i, o, l) -> {
                    l.onFailure(new IOException());
                    return null;
                },
                createFixedDateSupplier(),
                createDateFormatSupplier(),
                (c, t) -> null);
        var connectionResultPublisher = new SubmissionPublisher<ConnectionResponse>();
        connectionResultPublisher.subscribe(elasticsearchSubscriber);
        connectionResultPublisher.submit(new ConnectionResponse(true, 123, "foo"));
        connectionResultPublisher.close();

        List<ILoggingEvent> logsList = listAppender.list;

        await().atMost(1, TimeUnit.SECONDS).untilAsserted(
                () -> {
                    var mdcMap = logsList.get(0).getMDCPropertyMap();
                    assertEquals("true", mdcMap.get("reachable"));
                    assertEquals("123", mdcMap.get("responseTime"));
                    assertEquals("foo", mdcMap.get("server"));
                    assertEquals("missed", logsList.get(0).getMessage());
                }
        );
    }
}
