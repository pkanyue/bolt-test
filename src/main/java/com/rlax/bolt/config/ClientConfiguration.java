package com.rlax.bolt.config;

import com.alipay.remoting.ConnectionEventType;
import com.alipay.remoting.config.Configs;
import com.alipay.remoting.rpc.RpcClient;
import com.rlax.bolt.client.processor.MyAsyncClientUserProcessor;
import com.rlax.bolt.client.processor.MyClientConnectEventProcessor;
import com.rlax.bolt.client.processor.MyClientDisConnectEventProcessor;
import com.rlax.bolt.client.processor.MyClientUserProcessor;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executor;

/**
 * Client
 *
 * @author Rlax
 * @date 2022/09/08
 */
@Slf4j
@Configuration
@AllArgsConstructor
public class ClientConfiguration {

    private final Executor taskExecutor;

    @Bean
    public RpcClient rpcClient() {
        // 客户端断线重连
        System.setProperty(Configs.CONN_RECONNECT_SWITCH, "true");

        MyClientConnectEventProcessor clientConnectProcessor = new MyClientConnectEventProcessor();
        MyClientDisConnectEventProcessor clientDisConnectProcessor = new MyClientDisConnectEventProcessor();
//        MyClientUserProcessor clientUserProcessor = new MyClientUserProcessor();
        // 异步
        MyAsyncClientUserProcessor clientUserProcessor = new MyAsyncClientUserProcessor(taskExecutor);

        // 1. create a rpc client
        RpcClient client = new RpcClient();
        // 2. add processor for connect and close event if you need
        client.addConnectionEventProcessor(ConnectionEventType.CONNECT, clientConnectProcessor);
        client.addConnectionEventProcessor(ConnectionEventType.CLOSE, clientDisConnectProcessor);
        client.registerUserProcessor(clientUserProcessor);
        // 3. do init
        client.startup();
        return client;
    }

}
