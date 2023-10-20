package com.rlax.bolt.http.controller;

import cn.hutool.core.util.RandomUtil;
import com.alipay.remoting.Connection;
import com.alipay.remoting.InvokeCallback;
import com.alipay.remoting.exception.RemotingException;
import com.alipay.remoting.rpc.RpcClient;
import com.rlax.bolt.message.RequestBody;
import com.rlax.bolt.server.BoltServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
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
@RequestMapping("/demo")
public class DemoController {

	@Resource
	private BoltServer boltServer;
	@Resource
	private RpcClient rpcClient;
	@Resource(name = "serverExecutor")
	private Executor taskExecutor;

	/**
	 * client 发送请求，连接到 server 端
	 * @return
	 * @throws Exception
	 */
	@GetMapping("/connect")
	public ResponseEntity<String> clientConnect(@RequestHeader String traceId) throws Exception {
		log.info("clientConnect 1 ...");
		String addr = "127.0.0.1:" + 8899;
		RequestBody req = new RequestBody(1, "hello , i am client, i call sync", traceId);
		log.info("clientConnect 2 ...");
		Object response = rpcClient.invokeSync(addr, req, 30000);
		log.info("客户端调用返回：{}", response);
		log.info("clientConnect 3 ...");
		return ResponseEntity.ok(Objects.requireNonNull(response).toString());
	}

	/**
	 * 模拟中台调用站级客户端，客户端线程睡眠3秒
	 * @param key 客户端 address，如：127.0.0.1:52049
	 * @return 站级客户端响应结果
	 */
	@GetMapping("/async")
	public Mono<ResponseEntity<String>> callClientByAsync(String key, @RequestHeader String traceId) {
		log.info("callClientByAsync 1 ...");
		RequestBody req = new RequestBody(RandomUtil.randomInt(100), "server call client ...", traceId);
		// 随机哪个连接
		Connection connection = boltServer.getRpcServer().getConnectionManager().get(key);

		Mono<ResponseEntity<String>> mono = Mono.create(rMonoSink -> {
			try {
				boltServer.getRpcServer().invokeWithCallback(connection, req, new InvokeCallback() {
					@Override
					public void onResponse(Object result) {
						log.info("callClientByAsync 2 ...");
						log.info("客户端调用返回：{}, 响应中台 HTTP Response", result);
						rMonoSink.success(ResponseEntity.ok(result.toString()));
					}

					@Override
					public void onException(Throwable e) {
						rMonoSink.error(e);
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

	/**
	 * 模拟中台调用站级客户端，客户端线程睡眠3秒
	 * @param key 客户端 address，如：127.0.0.1:52049
	 * @return 站级客户端响应结果
	 */
	@GetMapping("/timeout")
	public Mono<ResponseEntity<String>> timeoutTest(String key, @RequestHeader String traceId) {
		log.info("callClientByAsync 1 ...");
		RequestBody req = new RequestBody(RandomUtil.randomInt(100), "server call client ...", traceId);
		// 随机哪个连接
		Connection connection = boltServer.getRpcServer().getConnectionManager().get(key);

		Mono<ResponseEntity<String>> mono = Mono.create(rMonoSink -> {
			try {
				boltServer.getRpcServer().invokeWithCallback(connection, req, new InvokeCallback() {
					@Override
					public void onResponse(Object result) {
						log.info("callClientByAsync 2 ...");
						log.info("客户端调用返回：{}, 响应中台 HTTP Response", result);
						rMonoSink.success(ResponseEntity.ok(result.toString()));
					}

					@Override
					public void onException(Throwable e) {
						rMonoSink.success(ResponseEntity.ok("请求异常：" + e.getMessage() + ", 请求体" + req.toString()));
//						rMonoSink.error(e);
					}

					@Override
					public Executor getExecutor() {
						return taskExecutor;
					}
				}, 1000);
			} catch (RemotingException e) {
				e.printStackTrace();
			}
		});

		log.info("callClientByAsync 3 ...");
		return mono;
	}

}
