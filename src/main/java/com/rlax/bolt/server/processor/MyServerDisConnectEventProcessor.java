package com.rlax.bolt.server.processor;

import cn.hutool.core.lang.Console;
import com.alipay.remoting.Connection;
import com.alipay.remoting.ConnectionEventProcessor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Rlax
 * @date 2022/08/30
 */
@Slf4j
public class MyServerDisConnectEventProcessor implements ConnectionEventProcessor {
    @Override
    public void onEvent(String remoteAddress, Connection connection) {
        log.info("服务端与客户端 {} 断开连接: {}", remoteAddress, connection);
    }
}
