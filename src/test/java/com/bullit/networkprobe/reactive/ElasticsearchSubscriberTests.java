package com.bullit.networkprobe.reactive;

import ch.qos.logback.classic.spi.ILoggingEvent;
import com.bullit.networkprobe.domain.ConnectionResponse;
import com.bullit.networkprobe.support.MDCLogger;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.TimeUnit;

import static com.bullit.networkprobe.configuration.ElasticsearchConfiguration.INDEX;
import static com.bullit.networkprobe.reactive.ReactiveTestSupport.*;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class ElasticsearchSubscriberTests {

    @Test
    public void whenReceivingMessageShouldSendToElasticsearch() throws IOException {
        var esClienWrapperMock = mock(ElasticsearchClientWrapper.class);
        var elasticsearchSubscriber = new ElasticsearchSubscriber(
                new MDCLogger(),
                esClienWrapperMock,
                createFixedDateSupplier(),
                createDateFormatSupplier(),
                (connectionResponse, timestamp) -> mock(IndexRequest.class),
                createTestExecutor());
        var connectionResultPublisher = new SubmissionPublisher<ConnectionResponse>();
        connectionResultPublisher.subscribe(elasticsearchSubscriber);
        connectionResultPublisher.submit(new ConnectionResponse(true, 123, "foo"));
        connectionResultPublisher.close();

        when(esClienWrapperMock.index(any(), any())).thenReturn(mock(IndexResponse.class));

        IndexRequest expectedIndexRequest = new IndexRequest(INDEX);
        expectedIndexRequest.source(
                Map.of(
                        "timestamp", "2020-09-23T20:50:44.000+0200",
                        "responseTime", 123,
                        "reachable", true
                )
        );

        await().atMost(1, TimeUnit.SECONDS).untilAsserted(
                () -> verify(esClienWrapperMock, times(1)).index(any(), any())
        );
    }

    @Test
    public void whenRestClientThrowsExceptionWeShouldGetTheLineInTheMissedLogs() {
        var listAppender = setupAppender(ElasticsearchSubscriber.class);
        // for some reason the below code was not working, to be figured out!!
        // In the meantime just putting in an implementatino of ElasticsearchClientWrapper that throws exceptions
        // var esClienWrapperMock = mock(ElasticsearchClientWrapper.class);
        // when(esClienWrapperMock.index(any(), any())).thenThrow(IOException.class);
        // Also had to replace the IndexRequest creation function to return nulls as even mocking IndexRequest did not work

        ElasticsearchClientWrapper wrapper = (i, r) -> {throw new IOException();};
        var elasticsearchSubscriber = new ElasticsearchSubscriber(
                new MDCLogger(),
                (r, o) -> {throw new IOException();},
                createFixedDateSupplier(),
                createDateFormatSupplier(),
                (c, t) -> null,
                createTestExecutor());
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
