package com.rlax.bolt.config;

import com.alipay.remoting.CommonCommandCode;
import com.alipay.remoting.ConnectionEventType;
import com.alipay.remoting.config.BoltServerOption;
import com.alipay.remoting.config.Configs;
import com.alipay.remoting.rpc.protocol.RpcProtocol;
import com.rlax.bolt.server.BoltServer;
import com.rlax.bolt.server.processor.MyHeartBeatProcessor;
import com.rlax.bolt.server.processor.MyServerConnectEventProcessor;
import com.rlax.bolt.server.processor.MyServerDisConnectEventProcessor;
import com.rlax.bolt.server.processor.MyServerUserProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Server\
 *
 * @author Rlax
 * @date 2022/09/08
 */
@Slf4j
@Configuration
public class ServerConfiguration {

    @Bean
    public BoltServer boltServer() {
        // IDLE 事件间隔
        System.setProperty(Configs.TCP_IDLE, "10000");
        System.setProperty(Configs.TCP_IDLE_SWITCH, Boolean.toString(true));
        // 超过心跳次数将断开连接
        System.setProperty(Configs.TCP_IDLE_MAXTIMES, "100");
        // 超过该时间没有接受消息将断开连接
        System.setProperty(Configs.TCP_SERVER_IDLE, "90000");

        MyServerConnectEventProcessor serverConnectProcessor = new MyServerConnectEventProcessor();
        MyServerUserProcessor serverUserProcessor = new MyServerUserProcessor();
        MyServerDisConnectEventProcessor serverDisConnectProcessor = new MyServerDisConnectEventProcessor();
        MyHeartBeatProcessor heartBeatProcessor = new MyHeartBeatProcessor();

        int port = 8899;
        BoltServer server = new BoltServer(port);
        // 2. add processor for connect and close event if you need
        server.addConnectionEventProcessor(ConnectionEventType.CONNECT, serverConnectProcessor);
        server.addConnectionEventProcessor(ConnectionEventType.CLOSE, serverDisConnectProcessor);
        // 3. register user processor for client request
        server.registerUserProcessor(serverUserProcessor);
        server.getRpcServer().option(BoltServerOption.SERVER_MANAGE_CONNECTION_SWITCH, true);

        // 4. server start
        if (server.startup()) {
            log.info("TCP 服务端已启动：{}", port);
        } else {
            log.error("服务端启动失败");
        }

        server.getRpcServer().registerProcessor(RpcProtocol.PROTOCOL_CODE,
                CommonCommandCode.HEARTBEAT, heartBeatProcessor);
        return server;
    }

}
