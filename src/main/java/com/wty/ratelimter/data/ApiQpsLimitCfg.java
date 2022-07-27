package com.wty.ratelimter.data;

import lombok.Data;

@Data
public class ApiQpsLimitCfg {
    private Integer queueAll;
    private Integer queue;
    private Integer qps;
    private Integer checkCnt;
    private Long checkWaitMs;
    private Long cleanTimeoutMs;
}
