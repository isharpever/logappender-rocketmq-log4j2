package com.isharpever.common.logappender.rocketmq.log4j2;

import java.net.InetAddress;
import java.net.UnknownHostException;
import org.apache.logging.log4j.core.util.NetUtils;
import org.junit.Test;

public class OutOfSpringTest {

    @Test
    public void testLocalHost() {
        // 本机ip
        String host = NetUtils.getLocalHostname();
        System.out.println(host);

        host = "(unknown host)";
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            if (localHost != null) {
                host = localHost.getHostName();
                host += "(" + localHost.getHostAddress() + ")";
            }
        } catch (UnknownHostException e) {
        }
        System.out.println(host);
    }
}
