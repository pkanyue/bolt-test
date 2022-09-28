package com.rlax.bolt.client.processor;

import com.alipay.remoting.BizContext;
import com.alipay.remoting.rpc.protocol.SyncUserProcessor;
import com.rlax.bolt.message.RequestBody;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * @author Rlax
 * @date 2022/08/30
 */
@Slf4j
public class MyClientUserProcessor extends SyncUserProcessor<RequestBody> {

    @Override
    public Object handleRequest(BizContext bizCtx, RequestBody request) throws Exception {
        log.info("客户端收到来自 {} 的请求：{}", bizCtx.getRemoteAddress(), request);
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "hello server, i am client";
    }

    @Override
    public String interest() {
        return RequestBody.class.getName();
    }

}
