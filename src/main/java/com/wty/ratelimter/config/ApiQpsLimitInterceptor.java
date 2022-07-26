package com.wty.ratelimter.config;

import com.wty.ratelimter.data.ApiQpsLimitCfg;
import com.wty.ratelimter.data.QpsLimitCfg;
import com.wty.ratelimter.data.QpsLimitData;
import com.wty.ratelimter.exception.ServiceException;
import com.wty.ratelimter.utils.ObjectMapperUtils;
import com.wty.ratelimter.utils.PathMatcherUtils;
import com.wty.ratelimter.utils.ThreadUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class ApiQpsLimitInterceptor extends HandlerInterceptorAdapter {
    private final QpsLimitCfg qpsLimitCfg; // 全局配置
    private final AtomicInteger queueAll; // 限流等待总数
    private final Map<String, QpsLimitData> qpsLimitDataMap; // uri to uri对应的单个配置

    public ApiQpsLimitInterceptor(QpsLimitCfg qpsLimitCfg) {
        this.qpsLimitCfg = qpsLimitCfg;
        this.queueAll = new AtomicInteger(0);
        this.qpsLimitDataMap = new ConcurrentHashMap<>();
        log.info("ApiQpsLimitInterceptor init: {}", ObjectMapperUtils.toJSON(qpsLimitCfg));
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!qpsLimitCfg.getEnable()) {
            return true;
        }
        long start = System.currentTimeMillis();
        try {
            return preHandleInternal(request);
        } catch (ServiceException e) {
            log.info("ApiQpsLimitInterceptor cost: {}, data: {}", System.currentTimeMillis() - start, ObjectMapperUtils.toJSON(e.getData()));
            throw e;
        }
    }

    private boolean preHandleInternal(HttpServletRequest request) {
        String uri = request.getServletPath();
        log.info("preHandleInternal 1: {}", uri);
        if (checkUri(uri)) { // 是否需要拦截
            log.info("preHandleInternal 2: {}", uri);
            QpsLimitData qpsLimitData = getQpsLimitData(uri);
            if (qpsLimitData.getRateLimiter().tryAcquire()) { // 是否被限流，单url的限制器，还没有全局的限制器
                // 没有被限流，直接过
                log.info("preHandleInternal 3: {}", uri);
                return true;
            }
            // 被限流，需要进入等待队列，增加自身队列数以及全局队列数
            qpsLimitData.getQueueCount().incrementAndGet();
            queueAll.incrementAndGet();
            log.info("preHandleInternal 4: {}, {}, {}", uri, qpsLimitData.getQueueCount().get(), queueAll.get());

            ApiQpsLimitCfg apiQpsLimitCfg = qpsLimitCfg.findApiQpsLimitCfg(uri);
            int cnt = 0; // 当前重试次数
            long waitMs = apiQpsLimitCfg.getCheckWaitMs();
            // 当前重试次数小于配置的最大重试次数 且 全局队列以及api队列等待值小于最大值
            while (cnt < apiQpsLimitCfg.getCheckCnt() && !checkQueue(qpsLimitData, apiQpsLimitCfg)) {
                log.info("preHandleInternal 5: {}, {}, {}", uri, cnt, apiQpsLimitCfg.getCheckCnt());
                cnt++;
                if (waitMs > 0) {
                    ThreadUtils.sleep(waitMs);
                }
                if (qpsLimitData.getRateLimiter().tryAcquire()) {
                    qpsLimitData.getQueueCount().decrementAndGet();
                    queueAll.decrementAndGet();
                    log.info("preHandleInternal 6: {}, {}, {}", uri, cnt, apiQpsLimitCfg.getCheckCnt());
                    return true;
                }
            }
            // 超过重试最大次数，减少排队次数，报错
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("uri", uri);
            data.put("allQueue", queueAll.decrementAndGet());
            data.put("apiQueue", qpsLimitData.getQueueCount().decrementAndGet());
            data.put("apiQpsLimitCfg", apiQpsLimitCfg);
            throw ServiceException.of(HttpStatus.SERVICE_UNAVAILABLE.value(), HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase(), data);
        }
        log.info("preHandleInternal 7: {}", uri);
        return true;
    }

    private boolean checkQueue(QpsLimitData qpsLimitData, ApiQpsLimitCfg apiQpsLimitCfg) {
        int allQueueSizeLimit = apiQpsLimitCfg.getQueueAll();
        int allQueueSizeCurrent = queueAll.get();
        if (allQueueSizeCurrent > allQueueSizeLimit) {
            // 全局限流等待数超过阈值
            return true;
        }
        int apiQueueSizeLimit = apiQpsLimitCfg.getQueue();
        int apiQueueSizeCurrent = qpsLimitData.getQueueCount().get();
        // 本api限流等待数超过api阈值
        return apiQueueSizeCurrent > apiQueueSizeLimit;
    }

    private QpsLimitData getQpsLimitData(String uri) {
        QpsLimitData qpsLimitData = qpsLimitDataMap.get(uri);
        if (qpsLimitData == null) {
            qpsLimitData = new QpsLimitData(uri, qpsLimitCfg);
            qpsLimitDataMap.put(uri, qpsLimitData);
        }
        return qpsLimitData;
    }

    private boolean checkUri(String uri) {
        if (PathMatcherUtils.pathMatch(uri, qpsLimitCfg.getIncludeUrls())) {
            return !PathMatcherUtils.pathMatch(uri, qpsLimitCfg.getExcludeUrls());
        }
        return false;
    }
}
