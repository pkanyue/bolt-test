package com.rlax.bolt.config;

import com.alipay.remoting.ConnectionEventType;
import com.alipay.remoting.config.Configs;
import com.alipay.remoting.rpc.RpcClient;
import com.rlax.bolt.client.processor.MyAsyncClientUserProcessor;
import com.rlax.bolt.client.processor.MyClientConnectEventProcessor;
import com.rlax.bolt.client.processor.MyClientDisConnectEventProcessor;
import com.rlax.bolt.client.processor.MyClientExceptionEventProcessor;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
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

    @Resource(name = "clientExecutor")
    private final Executor clientExecutor;

    @Bean
    public RpcClient rpcClient() {
        // 客户端断线重连
        System.setProperty(Configs.CONN_RECONNECT_SWITCH, "true");

        MyClientConnectEventProcessor clientConnectProcessor = new MyClientConnectEventProcessor();
        MyClientDisConnectEventProcessor clientDisConnectProcessor = new MyClientDisConnectEventProcessor();
        // 同步
//        MyClientUserProcessor clientUserProcessor = new MyClientUserProcessor();
        // 异步
        MyAsyncClientUserProcessor clientUserProcessor = new MyAsyncClientUserProcessor(clientExecutor);

        // 1. create a rpc client
        RpcClient client = new RpcClient();
        // 2. add processor for connect and close event if you need
        client.addConnectionEventProcessor(ConnectionEventType.CONNECT, clientConnectProcessor);
        client.addConnectionEventProcessor(ConnectionEventType.CLOSE, clientDisConnectProcessor);
        client.addConnectionEventProcessor(ConnectionEventType.EXCEPTION, new MyClientExceptionEventProcessor());
        client.registerUserProcessor(clientUserProcessor);
        // 3. do init
        client.startup();
        return client;
    }

}
