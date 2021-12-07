package com.bullit.networkprobe.controller;

import com.bullit.networkprobe.domain.ConnectionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import static org.springframework.http.MediaType.APPLICATION_NDJSON_VALUE;

@RestController
@RequiredArgsConstructor
@RequestMapping("/response")
public class ConnectionResponseController {
    private final Flux<ConnectionResponse> connectionResponseFlux;

    @GetMapping(produces = APPLICATION_NDJSON_VALUE)
    public Flux<ConnectionResponse> connectionResponses() {
        return connectionResponseFlux;
    }
}
