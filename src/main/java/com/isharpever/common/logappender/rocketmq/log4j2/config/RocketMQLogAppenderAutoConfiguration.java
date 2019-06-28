package com.isharpever.common.logappender.rocketmq.log4j2.config;

import com.isharpever.common.logappender.rocketmq.log4j2.appender.CustomRocketmqLog4j2Appender;
import com.isharpever.common.logappender.rocketmq.log4j2.constant.DefaultProperties;
import com.isharpever.tool.enums.CustomLogLevel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * 动态配置rocketmq appender
 *
 * @author yinxiaolin
 * @since 2018/5/31
 */
@Configuration
@ComponentScan(basePackages = "com.isharpever.common.logappender")
@EnableConfigurationProperties(RocketMQLogAppenderProperties.class)
public class RocketMQLogAppenderAutoConfiguration {

    public static final Logger logger = LoggerFactory
            .getLogger(RocketMQLogAppenderAutoConfiguration.class);

    @Resource
    private RocketMQLogAppenderProperties properties;

    @PostConstruct
    public void config() {
        logger.info("--- RocketMQLogAppenderAutoConfiguration start");

        // 配置校验
        if (!this.validateConfigProperties()) {
            logger.info("--- RocketMQLogAppenderAutoConfiguration end");
            return;
        }

        final LoggerContext ctx = (LoggerContext) LogManager
                .getContext(this.getClass().getClassLoader(), false);
        final org.apache.logging.log4j.core.config.Configuration configuration =
                ctx.getConfiguration();

        LoggerConfig oldRootLogger = configuration.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
        List<AppenderRef> oldAappenderRefs = oldRootLogger.getAppenderRefs();
        Map<String, Appender> oldAppenders = oldRootLogger.getAppenders();
        configuration.removeLogger(LogManager.ROOT_LOGGER_NAME);

        PatternLayout layout = this.createPatternLayout(configuration);
        CustomRocketmqLog4j2Appender rocketMqAppender = CustomRocketmqLog4j2Appender.newBuilder()
                .setName(properties.getName())
                .setLayout(layout)
                .setNameServerAddress(properties.getNameServerAddress())
                .setProducerGroup(properties.getProducerGroup())
                .setApp(properties.getApp())
                .build();
        rocketMqAppender.start();

        // 什么级别的日志通过rocketmq发消息
        Level threshold = CustomLogLevel.DING.toLevel();
        try {
            threshold = Level.valueOf(StringUtils.upperCase(properties.getThreshold()));
        } catch (Exception e) {
            logger.error("指定发送rocketmq的日志级别不能被识别,默认使用DING {}.{}={}",
                    DefaultProperties.PREFIX, "threshold", properties.getThreshold(), e);
        }

        AppenderRef rocketMqAppenderRef = AppenderRef
                .createAppenderRef(properties.getName(), threshold, null);
        List<AppenderRef> newAppenderRefs = new ArrayList<>();
        newAppenderRefs.addAll(oldAappenderRefs);
        newAppenderRefs.add(rocketMqAppenderRef);
        AppenderRef[] newAppenderRefArr = new AppenderRef[newAppenderRefs.size()];
        newAppenderRefs.toArray(newAppenderRefArr);

        LoggerConfig newRootLogger = LoggerConfig
                .createLogger(true, Level.INFO, LogManager.ROOT_LOGGER_NAME,
                        null, newAppenderRefArr,
                        null, configuration, null);

        oldAappenderRefs.stream().forEach(ref -> newRootLogger
                .addAppender(oldAppenders.get(ref.getRef()), ref.getLevel(), ref.getFilter()));
        newRootLogger.addAppender(rocketMqAppender, threshold, null);

        configuration.addAppender(rocketMqAppender);
        configuration.addLogger(LogManager.ROOT_LOGGER_NAME, newRootLogger);

        ctx.updateLoggers(configuration);

        logger.info("--- RocketMQLogAppenderAutoConfiguration end");
    }

    /**
     * 创建layout
     * @param configuration
     * @return
     */
    private PatternLayout createPatternLayout(
            org.apache.logging.log4j.core.config.Configuration configuration) {
        if (StringUtils.isBlank(properties.getPattern())) {
            return null;
        }

        return PatternLayout
                    .createLayout(properties.getPattern(), null,
                            configuration, null, null, true,
                            false, null, null);
    }

    /**
     * 配置校验
     */
    private boolean validateConfigProperties() {
        // 配置校验
        // 必须,且没有默认值
        if (StringUtils.isBlank(properties.getNameServerAddress())) {
            logger.warn("--- {}.{}未配置,创建rocketmq appender失败",
                    DefaultProperties.PREFIX, "nameServerAddress");
            return false;
        }
        if (StringUtils.isBlank(properties.getApp())) {
            logger.warn("--- {}.{}未配置,创建rocketmq appender失败",
                    DefaultProperties.PREFIX, "app");
            return false;
        }

        // 必须,但有默认值
        if (StringUtils.isBlank(properties.getName())) {
            properties.setName(DefaultProperties.APPENDER_NAME);
            logger.info("--- {}.{}未配置,使用默认值{}", DefaultProperties.PREFIX, "name",
                    DefaultProperties.APPENDER_NAME);
        }
        if (StringUtils.isBlank(properties.getPattern())) {
            properties.setPattern(DefaultProperties.APPENDER_PATTERN);
            logger.info("--- {}.{}未配置,使用默认值{}", DefaultProperties.PREFIX, "pattern",
                    DefaultProperties.APPENDER_PATTERN);
        }
        if (StringUtils.isBlank(properties.getProducerGroup())) {
            String produceGroupName = String.format("p-%s-log", properties.getApp());
            properties.setProducerGroup(produceGroupName);
            logger.info("--- {}.{}未配置,使用默认值{}", DefaultProperties.PREFIX, "producerGroup",
                    produceGroupName);
        }
        if (StringUtils.isBlank(properties.getThreshold())) {
            properties.setThreshold(DefaultProperties.APPENDER_THRESHOLD);
            logger.info("--- {}.{}未配置,使用默认值{}", DefaultProperties.PREFIX, "threshold",
                    DefaultProperties.APPENDER_THRESHOLD);
        }
        return true;
    }
}
