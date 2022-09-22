package com.rlax.bolt.client.processor;

import com.alipay.remoting.Connection;
import com.alipay.remoting.ConnectionEventProcessor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Rlax
 * @date 2022/08/30
 */
@Slf4j
public class MyClientDisConnectEventProcessor implements ConnectionEventProcessor {
    @Override
    public void onEvent(String remoteAddress, Connection connection) {
        log.info("客户端与服务端 {} 断开连接: {}", remoteAddress, connection);
    }
}
