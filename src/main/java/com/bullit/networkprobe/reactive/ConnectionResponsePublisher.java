package com.bullit.networkprobe.reactive;

import com.bullit.networkprobe.domain.ConnectionResponse;

import javax.annotation.PreDestroy;
import java.util.concurrent.SubmissionPublisher;

public class ConnectionResponsePublisher extends SubmissionPublisher<ConnectionResponse> {
    @PreDestroy
    public void preDestroy() {
        this.close();
    }
}
