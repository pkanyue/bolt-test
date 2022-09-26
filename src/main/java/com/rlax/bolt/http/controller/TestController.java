package com.rlax.bolt.http.controller;

import cn.hutool.core.date.DateUtil;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

/**
 * 测试 控制器
 *
 * @author Rlax
 * @since 2022-03-02 16:59:09
 */
@Slf4j
@RestController
@RequestMapping("test")
public class TestController {

	@Resource
	private BoltServer boltServer;
	@Resource
	private RpcClient rpcClient;
	@Resource(name = "serverExecutor")
	private Executor taskExecutor;

	@GetMapping("/mono")
	public Mono<ResponseEntity<String>> demoAsync() {
		log.info("demoAsync 1 ...");
		Mono<ResponseEntity<String>> mono = Mono.create(monoSink -> {
			try {
				TimeUnit.SECONDS.sleep(3);
				log.info("create 2 ...");
				monoSink.success(ResponseEntity.ok("hello "+ DateUtil.date()));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		});
		log.info("demoAsync 3 ...");
		return mono;
	}

	@GetMapping("/from")
	public Mono<ResponseEntity<String>> demoAsyncFrom() {
		log.info("demoAsyncFrom 1 ...");
		Mono<ResponseEntity<String>> mono = Mono.fromSupplier(() -> {
			try {
				TimeUnit.SECONDS.sleep(3);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			log.info("supplier ...");
			return ResponseEntity.ok("hello "+ DateUtil.date());
		});
		log.info("demoAsyncFrom 3 ...");
		return mono;
	}

	@GetMapping("/send/{msg}")
	public ResponseEntity<String> send(@PathVariable String msg, String key) {
		RequestBody req = new RequestBody(RandomUtil.randomInt(100), msg);
		// 随机哪个连接
		Connection connection = boltServer.getRpcServer().getConnectionManager().get(key);
		Object response = null;
		try {
			response = boltServer.getRpcServer().invokeSync(connection, req, 1000);
		} catch (RemotingException | InterruptedException e) {
			e.printStackTrace();
		}
		log.info("服务端调用返回：{}", response);
		return ResponseEntity.ok(Objects.requireNonNull(response).toString());
	}

	@GetMapping("/client/sync")
	public ResponseEntity<String> clientSync() throws Exception {
		log.info("clientSync 1 ...");
		String addr = "127.0.0.1:" + 8899;
		RequestBody req = new RequestBody(1, "hello , i am client, i call sync");
		log.info("clientSync 2 ...");
		Object response = rpcClient.invokeSync(addr, req, 30000);
		log.info("客户端调用返回：{}", response);
		log.info("clientSync 3 ...");
		return ResponseEntity.ok(Objects.requireNonNull(response).toString());
	}

	@GetMapping("/client/async")
	public Mono<ResponseEntity<String>> clientCall() throws Exception {
		log.info("clientCall 1 ...");
		String addr = "127.0.0.1:" + 8899;
		RequestBody req = new RequestBody(2, "hello , i am client, i call async");

		Mono<ResponseEntity<String>> mono = Mono.create(rMonoSink -> {
			try {
				rpcClient.invokeWithCallback(addr, req, new InvokeCallback() {
					@Override
					public void onResponse(Object result) {
						log.info("clientCall 2 ...");
						log.info("客户端调用返回：{}", result);
						rMonoSink.success(ResponseEntity.ok(result.toString()));
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
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		});

		log.info("clientCall 3 ...");
		return mono;
	}



}
