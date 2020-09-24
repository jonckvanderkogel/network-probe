package com.bullit.caiwayprobe.reactive;

import com.bullit.caiwayprobe.domain.PingResponse;

import javax.annotation.PreDestroy;
import java.util.concurrent.SubmissionPublisher;

public class PingResponsePublisher extends SubmissionPublisher<PingResponse> {
    @PreDestroy
    public void preDestroy() {
        this.close();
    }
}