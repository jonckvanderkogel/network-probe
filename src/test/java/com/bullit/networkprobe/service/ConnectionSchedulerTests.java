package com.bullit.networkprobe.service;

import com.bullit.networkprobe.domain.ConnectionResponse;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscriber;
import reactor.core.publisher.Flux;

import java.util.List;

import static org.mockito.Mockito.*;

public class ConnectionSchedulerTests {
    @Test
    public void performingConnectionsShouldResultInMessages() {
        Subscriber<ConnectionResponse> outageSubscriberMock = mock(Subscriber.class);
        Subscriber<ConnectionResponse> connectionSubscriberMock = mock(Subscriber.class);
        var publisherFlux = Flux.just(new ConnectionResponse(true, 666, "none"));

        var connectionScheduler = new ConnectionScheduler(publisherFlux, List.of(outageSubscriberMock, connectionSubscriberMock));
        connectionScheduler.performConnections();

        verify(outageSubscriberMock, times(1)).onSubscribe(any());
        verify(connectionSubscriberMock, times(1)).onSubscribe(any());
    }
}
