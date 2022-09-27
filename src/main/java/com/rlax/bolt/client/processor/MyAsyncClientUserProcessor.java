package com.rlax.bolt.client.processor;

import cn.hutool.core.util.StrUtil;
import com.alipay.remoting.AsyncContext;
import com.alipay.remoting.BizContext;
import com.alipay.remoting.rpc.protocol.AsyncUserProcessor;
import com.rlax.bolt.message.RequestBody;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

/**
 * Async 异步用户处理器
 *
 * @author Rlax
 * @date 2022/08/30
 */
@Slf4j
@AllArgsConstructor
public class MyAsyncClientUserProcessor extends AsyncUserProcessor<RequestBody> {

    private final Executor taskExecutor;

    @Override
    public void handleRequest(BizContext bizCtx, AsyncContext asyncCtx, RequestBody request) {
        log.info("客户端收到来自 {} 的请求：{}", bizCtx.getRemoteAddress(), request);
        taskExecutor.execute(() -> {
            log.info("客户端 {} 处理请求：{}", bizCtx.getRemoteAddress(), request);
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            asyncCtx.sendResponse(StrUtil.format("客户端收到来自 {}, 睡眠3秒后响应：{}", bizCtx.getRemoteAddress(), "hello server, i am client"));
        });
    }

    @Override
    public String interest() {
        return RequestBody.class.getName();
    }

}
