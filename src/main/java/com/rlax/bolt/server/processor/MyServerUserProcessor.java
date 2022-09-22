package com.rlax.bolt.server.processor;

import cn.hutool.core.lang.Console;
import com.alipay.remoting.BizContext;
import com.alipay.remoting.rpc.protocol.SyncUserProcessor;
import com.rlax.bolt.message.RequestBody;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Rlax
 * @date 2022/08/30
 */
@Slf4j
public class MyServerUserProcessor extends SyncUserProcessor<RequestBody> {

    @Override
    public Object handleRequest(BizContext bizCtx, RequestBody request) throws Exception {
        log.info("服务端收到来自 {} 的请求：{}", bizCtx.getRemoteAddress(), request);
        return "hello client, i am server";
    }

    @Override
    public String interest() {
        return RequestBody.class.getName();
    }

}
