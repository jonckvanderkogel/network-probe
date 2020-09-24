package com.bullit.networkprobe.reactive;

import com.bullit.networkprobe.domain.PingResponse;

import javax.annotation.PreDestroy;
import java.util.concurrent.SubmissionPublisher;

public class PingResponsePublisher extends SubmissionPublisher<PingResponse> {
    @PreDestroy
    public void preDestroy() {
        this.close();
    }
}
