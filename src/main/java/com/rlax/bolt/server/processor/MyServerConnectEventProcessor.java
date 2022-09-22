package com.rlax.bolt.server.processor;

import com.alipay.remoting.Connection;
import com.alipay.remoting.ConnectionEventProcessor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Rlax
 * @date 2022/08/30
 */
@Data
@Slf4j
public class MyServerConnectEventProcessor implements ConnectionEventProcessor {

    private AtomicInteger count = new AtomicInteger(0);

    private Connection     connection;
    private String         remoteAddr;

    @Override
    public void onEvent(String remoteAddress, Connection connection) {
        this.remoteAddr = remoteAddress;
        this.connection = connection;
        int countValue = count.incrementAndGet();
        log.info("服务器已接受来自客户端 {} 的连接，连接计数: {}, poolkey: {}", remoteAddress, countValue, connection.getPoolKeys());
    }


}
