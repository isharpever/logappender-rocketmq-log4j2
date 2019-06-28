package com.isharpever.common.logappender.rocketmq.log4j2.controller;

import com.isharpever.tool.enums.CustomLogLevel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LogController {

    public static final Logger logger = LogManager.getLogger(LogController.class);

    @RequestMapping("/errorLog")
    public String errorLog(String msg) {
        logger.error("--- {}", msg);
        return msg;
    }

    @RequestMapping("/dingLog")
    public String dingLog(String msg) {
        logger.log(CustomLogLevel.DING.toLevel(), "--- {}", msg);
        return msg;
    }

    @RequestMapping("/warnLog")
    public String warnLog(String msg) {
        logger.warn("--- {}", msg);
        return msg;
    }
}
