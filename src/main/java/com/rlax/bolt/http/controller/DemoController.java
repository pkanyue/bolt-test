package com.rlax.bolt.http.controller;

import cn.hutool.core.util.RandomUtil;
import com.alipay.remoting.Connection;
import com.alipay.remoting.InvokeCallback;
import com.alipay.remoting.exception.RemotingException;
import com.alipay.remoting.rpc.RpcClient;
import com.rlax.bolt.message.RequestBody;
import com.rlax.bolt.server.BoltServer;
import com.rlax.corebin.core.result.R;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.concurrent.Executor;

/**
 * 中台调用站级 Demo
 *
 * @author Rlax
 * @since 2022-9-26
 */
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/demo")
public class DemoController {

	private final BoltServer boltServer;
	private final RpcClient rpcClient;
	private final Executor taskExecutor;

	@GetMapping("/sync")
	public R<String> clientSync() throws Exception {
		log.info("clientSync 1 ...");
		String addr = "127.0.0.1:" + 8899;
		RequestBody req = new RequestBody(1, "hello , i am client, i call sync");
		log.info("clientSync 2 ...");
		Object response = rpcClient.invokeSync(addr, req, 30000);
		log.info("客户端调用返回：{}", response);
		log.info("clientSync 3 ...");
		return R.success(Objects.requireNonNull(response).toString());
	}

	/**
	 * 模拟中台调用站级客户端，客户端线程睡眠3秒
	 * @param key
	 * @return
	 * @throws Exception
	 */
	@GetMapping("/async")
	public Mono<R<String>> callClientByAsync(String key) throws Exception {
		log.info("callClientByAsync 1 ...");
		RequestBody req = new RequestBody(RandomUtil.randomInt(100), "server call client ...");
		// 随机哪个连接
		Connection connection = boltServer.getRpcServer().getConnectionManager().get(key);

		Mono<R<String>> mono = Mono.create(rMonoSink -> {
			try {
				boltServer.getRpcServer().invokeWithCallback(connection, req, new InvokeCallback() {
					@Override
					public void onResponse(Object result) {
						log.info("callClientByAsync 2 ...");
						log.info("客户端调用返回：{}", result);
						rMonoSink.success(R.success(result.toString()));
					}

					@Override
					public void onException(Throwable e) {

					}

					@Override
					public Executor getExecutor() {
						return taskExecutor;
					}
				}, 30000);
			} catch (RemotingException e) {
				e.printStackTrace();
			}
		});

		log.info("callClientByAsync 3 ...");
		return mono;
	}



}
