package com.bullit.networkprobe.service;

import com.bullit.networkprobe.domain.ConnectionResponse;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;

import static org.mockito.Mockito.*;

public class ConnectionSchedulerTests {
    @Test
    public void performingConnectionsShouldResultInMessages() {
        var connectionServiceMock = mock(ConnectionService.class);
        SubmissionPublisher<ConnectionResponse> publisherMock = mock(SubmissionPublisher.class);
        Flow.Subscriber<ConnectionResponse> outageSubscriberMock = mock(Flow.Subscriber.class);
        Flow.Subscriber<ConnectionResponse> connectionSubscriberMock = mock(Flow.Subscriber.class);

        var connectionResponse = new ConnectionResponse(true, 123, "foo");
        var answer = new CompletableFuture<ConnectionResponse>();
        answer.complete(connectionResponse);
        when(connectionServiceMock.connectToServers()).thenReturn(answer);

        var connectionScheduler = new ConnectionScheduler(connectionServiceMock, publisherMock, List.of(outageSubscriberMock, connectionSubscriberMock));
        connectionScheduler.performConnections();

        verify(publisherMock).submit(ArgumentMatchers.eq(connectionResponse));
        verify(publisherMock, times(1)).submit(connectionResponse);
    }
}
