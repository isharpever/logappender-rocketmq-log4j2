package com.isharpever.common.logappender.rocketmq.log4j2.appender;

import com.isharpever.common.logappender.rocketmq.log4j2.constant.DefaultProperties;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.ErrorHandler;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Node;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderFactory;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.Required;
import org.apache.logging.log4j.core.layout.SerializedLayout;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.MQProducer;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.logappender.common.ProducerInstance;

/**
 * 定义rocketmq appender行为
 *
 * @author yinxiaolin
 * @since 2018/5/31
 */
@Plugin(name = "CustomRocketMQ",
        category = Node.CATEGORY,
        elementType = Appender.ELEMENT_TYPE,
        printObject = true)
public class CustomRocketmqLog4j2Appender extends AbstractAppender {

    public static final String HOST_KEY = "ip";

    /**
     * RocketMQ nameserver address
     */
    private String nameServerAddress;

    /**
     * Log producer group
     */
    private String producerGroup;

    /**
     * Log producer send instance
     */
    private MQProducer producer;

    /**
     * Appended message tag define
     */
    private String tag;

    /**
     * Whitch topic to send log messages
     */
    private String topic;

    protected CustomRocketmqLog4j2Appender(String name, Filter filter,
            Layout<? extends Serializable> layout,
            boolean ignoreExceptions, String nameServerAddress, String producerGroup,
            String topic, String tag) {
        super(name, filter, layout, ignoreExceptions);
        this.topic = topic;
        this.tag = tag;
        this.nameServerAddress = nameServerAddress;
        this.producerGroup = producerGroup;
        try {
            DefaultMQProducer defaultMQProducer = new DefaultMQProducer();
            defaultMQProducer.setNamesrvAddr(this.nameServerAddress);
            defaultMQProducer.setProducerGroup(this.producerGroup);
            defaultMQProducer.setInstanceName(this.producerGroup);
            defaultMQProducer.start();
            this.producer = defaultMQProducer;
        } catch (Exception e) {
            ErrorHandler handler = this.getHandler();
            if (handler != null) {
                handler.error("Starting CustomRocketmqLog4j2Appender [" + this.getName()
                        + "] nameServerAddress:" + nameServerAddress + " group:" + producerGroup
                        + " " + e.getMessage());
            }
        }
    }

    /**
     * Info,error,warn,callback method implementation
     */
    @Override
    public void append(LogEvent event) {
        if (null == producer) {
            return;
        }
        byte[] data = this.getLayout().toByteArray(event);
        try {
            Message msg = new Message(topic, tag, data);
            msg.getProperties()
                    .put(ProducerInstance.APPENDER_TYPE, ProducerInstance.LOG4J2_APPENDER);

            // 附加额外属性
            this.attachMsgProperties(msg);

            //Send message and do not wait for the ack from the message broker.
            producer.sendOneway(msg);
        } catch (Exception e) {
            ErrorHandler handler = this.getHandler();
            if (handler != null) {
                String msg = new String(data);
                handler.error(
                        "Could not send message in CustomRocketmqLog4j2Appender [" + this.getName()
                                + "].Message is : " + msg, e);
            }

        }
    }

    /**
     * When system exit,this method will be called to close resources
     */
    @Override
    public void stop() {
        this.setStopping();
        try {
            ProducerInstance.removeAndClose(this.nameServerAddress, this.producerGroup);
        } catch (Exception e) {
            ErrorHandler handler = this.getHandler();
            if (handler != null) {
                handler.error("Closeing CustomRocketmqLog4j2Appender [" + this.getName()
                        + "] nameServerAddress:" + nameServerAddress + " group:" + producerGroup
                        + " " + e.getMessage());
            }
        }

        super.stop();
        this.setStopped();
    }

    /**
     * 附加额外属性
     * @param msg
     */
    private void attachMsgProperties(Message msg) {
        // 本机ip
        String host = "(unknown host)";
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            if (localHost != null) {
                host = localHost.getHostName();
                host += "(" + localHost.getHostAddress() + ")";
            }
        } catch (UnknownHostException e) {
        }
        msg.getProperties().put(HOST_KEY, host);
    }

    /**
     * Log4j2 builder creator
     * @return CustomRocketmqLog4j2Appender.Builder
     */
    @PluginBuilderFactory
    public static CustomRocketmqLog4j2Appender.Builder newBuilder() {
        return new CustomRocketmqLog4j2Appender.Builder();
    }

    /**
     * Log4j2 xml builder define
     */
    public static class Builder implements
            org.apache.logging.log4j.core.util.Builder<CustomRocketmqLog4j2Appender> {

        @PluginBuilderAttribute
        @Required(message = "A name for the CustomRocketmqLog4j2Appender must be specified")
        private String name;

        @PluginElement("Layout")
        private Layout<? extends Serializable> layout;

        @PluginElement("Filter")
        private Filter filter;

        @PluginBuilderAttribute
        private boolean ignoreExceptions;

        @PluginBuilderAttribute
        @Required(message = "A app name must be specified")
        private String app;

        @PluginBuilderAttribute
        @Required(message = "A nameserver adress must be specified")
        private String nameServerAddress;

        @PluginBuilderAttribute
        private String producerGroup;

        private Builder() {
            this.layout = SerializedLayout.createLayout();
            this.ignoreExceptions = true;
        }

        public CustomRocketmqLog4j2Appender.Builder setName(String name) {
            this.name = name;
            if (StringUtils.isBlank(this.name)) {
                this.name = DefaultProperties.APPENDER_NAME;
            }
            return this;
        }

        public CustomRocketmqLog4j2Appender.Builder setLayout(
                Layout<? extends Serializable> layout) {
            this.layout = layout;
            return this;
        }

        public CustomRocketmqLog4j2Appender.Builder setFilter(Filter filter) {
            this.filter = filter;
            return this;
        }

        public CustomRocketmqLog4j2Appender.Builder setIgnoreExceptions(boolean ignoreExceptions) {
            this.ignoreExceptions = ignoreExceptions;
            return this;
        }

        public CustomRocketmqLog4j2Appender.Builder setApp(String app) {
            this.app = app;
            return this;
        }

        public CustomRocketmqLog4j2Appender.Builder setNameServerAddress(String nameServerAddress) {
            this.nameServerAddress = nameServerAddress;
            return this;
        }

        public CustomRocketmqLog4j2Appender.Builder setProducerGroup(String producerGroup) {
            this.producerGroup = producerGroup;
            if (StringUtils.isBlank(this.producerGroup)) {
                this.producerGroup = String.format("p-%s-log", this.app);
            }
            return this;
        }

        @Override
        public CustomRocketmqLog4j2Appender build() {
            return new CustomRocketmqLog4j2Appender(name, filter, layout, ignoreExceptions,
                    nameServerAddress, producerGroup, DefaultProperties.APPDENDER_TOPIC, app);
        }
    }
}
