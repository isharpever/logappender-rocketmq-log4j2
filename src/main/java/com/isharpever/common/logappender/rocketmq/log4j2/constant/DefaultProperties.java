package com.isharpever.common.logappender.rocketmq.log4j2.constant;

import com.isharpever.tool.enums.CustomLogLevel;

/**
 * 默认配置
 *
 * @author yinxiaolin
 * @since 2018/5/31
 */
public class DefaultProperties {

    /**
     * 配置项前缀
     */
    public static final String PREFIX = "log4j2.rocketmqappender";

    /**
     * 默认的rocketmq appender的name
     */
    public static final String APPENDER_NAME = "rocketmqAppender";

    /**
     * 默认的rocketmq appender的pattern
     */
    public static final String APPENDER_PATTERN = "%d %p [%t] %c{10}:%M:%L %m%n";

    /**
     * 默认的rocketmq appender的topic
     */
    public static final String APPDENDER_TOPIC = "ylzx_log";

    /**
     * 默认的通过rocketmq发消息的最低日志级别
     */
    public static final String APPENDER_THRESHOLD = CustomLogLevel.DING.getLevelName();
}
