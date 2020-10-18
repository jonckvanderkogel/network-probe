package com.bullit.networkprobe.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@Getter
@RequiredArgsConstructor
public class ConnectionResponse {
    private final boolean reachable;
    private final long responseTime;
    private final String server;

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
