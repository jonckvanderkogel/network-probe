package com.bullit.caiwayprobe.service;

import com.bullit.caiwayprobe.domain.PingResponse;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;

import static org.mockito.Mockito.*;

public class PingSchedulerTests {
    @Test
    public void performingPingShouldResultInMessages() {
        var pingServiceMock = mock(PingService.class);
        SubmissionPublisher<PingResponse> publisherMock = mock(SubmissionPublisher.class);
        Flow.Subscriber<PingResponse> outageSubscriberMock = mock(Flow.Subscriber.class);
        Flow.Subscriber<PingResponse> pingSubscriberMock = mock(Flow.Subscriber.class);

        var pingResponse = new PingResponse(true, 123, "foo");
        var answer = new CompletableFuture<PingResponse>();
        answer.complete(pingResponse);
        when(pingServiceMock.pingDnsServers()).thenReturn(answer);

        var pingScheduler = new PingScheduler(pingServiceMock, publisherMock, outageSubscriberMock, pingSubscriberMock);
        pingScheduler.performPings();

        verify(publisherMock).submit(ArgumentMatchers.eq(pingResponse));
        verify(publisherMock, times(1)).submit(pingResponse);
    }
}
