package com.bullit.networkprobe.service;

import com.bullit.networkprobe.domain.ConnectionResponse;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.reactivestreams.Subscriber;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;

import static org.mockito.Mockito.*;

public class ConnectionSchedulerTests {
    @Test
    public void performingConnectionsShouldResultInMessages() {
        var connectionServiceMock = mock(ConnectionService.class);
        Subscriber<ConnectionResponse> outageSubscriberMock = mock(Subscriber.class);
        Subscriber<ConnectionResponse> connectionSubscriberMock = mock(Subscriber.class);
        var publisherFlux = Flux.just(new ConnectionResponse(true, 666, "none"));

        when(connectionServiceMock.connectToServers()).thenReturn(publisherFlux);

        var connectionScheduler = new ConnectionScheduler(connectionServiceMock, List.of(outageSubscriberMock, connectionSubscriberMock));
        connectionScheduler.performConnections();

        verify(outageSubscriberMock, times(1)).onSubscribe(any());
        verify(connectionSubscriberMock, times(1)).onSubscribe(any());
    }
}
