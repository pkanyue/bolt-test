package com.rlax.bolt.client;

import cn.hutool.core.lang.Console;
import com.alipay.remoting.ConnectionEventType;
import com.alipay.remoting.exception.RemotingException;
import com.alipay.remoting.rpc.RpcClient;
import com.rlax.bolt.client.processor.MyClientConnectEventProcessor;
import com.rlax.bolt.client.processor.MyClientDisConnectEventProcessor;
import com.rlax.bolt.client.processor.MyClientUserProcessor;
import com.rlax.bolt.message.RequestBody;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * @author Rlax
 * @date 2022/09/08
 */
@Slf4j
public class ClientMain {

    public RpcClient init() {
        MyClientConnectEventProcessor clientConnectProcessor = new MyClientConnectEventProcessor();
        MyClientDisConnectEventProcessor clientDisConnectProcessor = new MyClientDisConnectEventProcessor();
        MyClientUserProcessor clientUserProcessor = new MyClientUserProcessor();

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

    public static void main(String[] args) throws IOException, RemotingException, InterruptedException {
        String addr = "127.0.0.1:" + 8899;
        RpcClient client = new ClientMain().init();
        RequestBody req = new RequestBody(2, "hello , i am client");
        Object response = client.invokeSync(addr, req, 1000);
        log.info("客户端调用返回：{}", response);

        Thread.sleep(9000000000000L);
    }

}
