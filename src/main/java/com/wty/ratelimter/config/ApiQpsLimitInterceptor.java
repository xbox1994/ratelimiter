package com.wty.ratelimter.config;

import com.wty.ratelimter.data.QpsLimitCfg;
import com.wty.ratelimter.utils.ObjectMapperUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class ApiQpsLimitInterceptor extends HandlerInterceptorAdapter {
    private QpsLimitCfg qpsLimitCfg;
    private AtomicInteger queueAll; // 限流等待总数
    private final Map<String, QpsLimitData> qpsLimitDataMap;

    public ApiQpsLimitInterceptor(QpsLimitCfg qpsLimitCfg) {
        this.qpsLimitCfg = qpsLimitCfg;
        this.queueAll = new AtomicInteger(0);
        this.qpsLimitDataMap = new ConcurrentHashMap<>();
        log.info("ApiQpsLimitInterceptor init: {}", ObjectMapperUtils.toJSON(qpsLimitCfg));
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!qpsLimitCfg.getEnable()){
            return true;
        }
        long start = System.currentTimeMillis();
        try{
            return preHandleInternal(request);
        }catch (ServiceException e){
            log.info("ApiQpsLimitInterceptor cost: {}, data: {}", System.currentTimeMillis() - start, ObjectMapperUtils.toJSON(e.get));
        }
    }
}
