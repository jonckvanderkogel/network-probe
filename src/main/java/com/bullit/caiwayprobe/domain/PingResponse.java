package com.bullit.caiwayprobe.domain;

import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Data
@Getter
@RequiredArgsConstructor
public class PingResponse {
    private final boolean reachable;
    private final long responseTime;
    private final String dnsServerAddress;

    public ReachableState getReachableState() {
        return ReachableState.getState(reachable);
    }

    public enum ReachableState {
        REACHABLE, NOT_REACHABLE;

        public static ReachableState getState(boolean isReachable) {
            return isReachable ? REACHABLE : NOT_REACHABLE;
        }
    }
}
