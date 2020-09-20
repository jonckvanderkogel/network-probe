package com.bullit.caiwayprobe.service;

import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Data
@Getter
@RequiredArgsConstructor
public class PingResponse {
    private final boolean reachable;
    private final long responseTime;
}
