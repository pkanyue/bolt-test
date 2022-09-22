package com.rlax.bolt.server.processor;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Console;
import com.alipay.remoting.AbstractRemotingProcessor;
import com.alipay.remoting.RemotingCommand;
import com.alipay.remoting.RemotingContext;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Rlax
 * @date 2022/08/30
 */
@Slf4j
public class MyHeartBeatProcessor extends AbstractRemotingProcessor<RemotingCommand> {
    @Override
    public void doProcess(RemotingContext ctx, RemotingCommand msg) throws Exception {
        log.info("心跳消息: {}, {}, {}", DateUtil.now(), ctx, msg);
    }
}
