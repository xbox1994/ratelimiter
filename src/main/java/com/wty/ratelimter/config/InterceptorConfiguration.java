package com.wty.ratelimter.config;

import com.wty.ratelimter.data.QpsLimitCfg;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@Slf4j
public class InterceptorConfiguration implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new ApiQpsLimitInterceptor(new QpsLimitCfg()));
        log.info("InterceptorConfiguration init");
    }
}
