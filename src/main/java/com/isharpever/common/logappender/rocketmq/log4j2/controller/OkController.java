package com.isharpever.common.logappender.rocketmq.log4j2.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OkController {

    public static final Logger logger = LoggerFactory.getLogger(OkController.class);

    @RequestMapping("/ok")
    public String ok(String start) {
        return "ok";
    }

}
