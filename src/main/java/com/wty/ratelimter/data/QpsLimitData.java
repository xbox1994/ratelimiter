package com.wty.ratelimter.data;

import com.google.common.util.concurrent.RateLimiter;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

@Data
public class QpsLimitData {
    private String uri;
    private AtomicInteger queueCount;
    private QpsLimitCfg qpsLimitCfg;
    private RateLimiter rateLimiter;

    public ApiQpsLimitCfg findApiQpsLimitCfg(String uri) {
        return null;
    }
}
