package com.wty.ratelimter.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AsyncTaskController {
    @GetMapping("qps1")
    public void qps1() {
        System.out.println("qps1 ok");
    }
    @GetMapping("qps2")
    public void qps2() {
        System.out.println("qps2 ok");
    }
}
