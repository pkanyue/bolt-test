package com.rlax.bolt.client.processor;

import com.alipay.remoting.Connection;
import com.alipay.remoting.ConnectionEventProcessor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Rlax
 * @date 2022/08/30
 */
@Data
@Slf4j
public class MyClientConnectEventProcessor implements ConnectionEventProcessor {

    private Connection     connection;
    private String         remoteAddr;

    @Override
    public void onEvent(String remoteAddress, Connection connection) {
        this.remoteAddr = remoteAddress;
        this.connection = connection;
        log.info("客户端已与服务端 {} 建立连接", remoteAddress);
    }


}
