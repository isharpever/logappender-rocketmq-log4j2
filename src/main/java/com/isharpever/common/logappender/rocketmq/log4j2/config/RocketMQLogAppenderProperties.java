package com.isharpever.common.logappender.rocketmq.log4j2.config;

import com.isharpever.common.logappender.rocketmq.log4j2.constant.DefaultProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * rocketmq appender的必要配置
 *
 * @author yinxiaolin
 * @since 2018/5/31
 */
@ConfigurationProperties(prefix = DefaultProperties.PREFIX)
public class RocketMQLogAppenderProperties {

    /**
     * rocketmq appender的name
     */
    private String name;

    /**
     * rocketmq appender的pattern
     */
    private String pattern = "%d %p [%t] %c{10}:%M:%L %m%n";

    /**
     * rocketmq的name server
     */
    private String nameServerAddress;

    /**
     * rocketmq生产者的group
     */
    private String producerGroup;

    /**
     * 应用名 rocketmq发消息的tag
     */
    private String app;

    /**
     * 通过rocketmq发消息的最低日志级别
     */
    private String threshold;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public String getNameServerAddress() {
        return nameServerAddress;
    }

    public void setNameServerAddress(String nameServerAddress) {
        this.nameServerAddress = nameServerAddress;
    }

    public String getProducerGroup() {
        return producerGroup;
    }

    public void setProducerGroup(String producerGroup) {
        this.producerGroup = producerGroup;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getThreshold() {
        return threshold;
    }

    public void setThreshold(String threshold) {
        this.threshold = threshold;
    }

    @Override
    public String toString() {
        return "RocketMQLogAppenderProperties{" +
                "name='" + name + '\'' +
                ", pattern='" + pattern + '\'' +
                ", nameServerAddress='" + nameServerAddress + '\'' +
                ", producerGroup='" + producerGroup + '\'' +
                ", app='" + app + '\'' +
                ", threshold='" + threshold + '\'' +
                '}';
    }
}
