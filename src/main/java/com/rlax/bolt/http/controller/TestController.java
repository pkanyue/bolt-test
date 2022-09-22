package com.rlax.bolt.http.controller;

import cn.hutool.core.date.DateUtil;
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
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 用户 控制器
 *
 * @author Rlax
 * @since 2022-03-02 16:59:09
 */
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping
public class TestController {

	private final BoltServer boltServer;
	private final RpcClient rpcClient;

	@GetMapping("/demo/mono")
	public Mono<R<String>> demoAsync() {
		log.info("demoAsync 1 ...");
		Mono<R<String>> mono = Mono.create(monoSink -> {
			try {
				TimeUnit.SECONDS.sleep(3);
				log.info("create 2 ...");
				monoSink.success(R.success("hello "+ DateUtil.date()));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		});
		log.info("demoAsync 3 ...");
		return mono;
	}

	@GetMapping("/demo/from")
	public Mono<R<String>> demoAsyncFrom() {
		log.info("demoAsyncFrom 1 ...");
		Mono<R<String>> mono = Mono.fromSupplier(() -> {
			try {
				TimeUnit.SECONDS.sleep(3);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			log.info("supplier ...");
			return R.success("hello "+ DateUtil.date());
		});
		log.info("demoAsyncFrom 3 ...");
		return mono;
	}

	@GetMapping("/get/{id}")
	public R<Long> get(@PathVariable Long id) {
		return R.success(id);
	}

	@GetMapping("/send/{msg}")
	public R<String> send(@PathVariable String msg, String key) {
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
		return R.success(Objects.requireNonNull(response).toString());
	}

	@GetMapping("/client/send")
	public R<String> clientSync() throws Exception {
		String addr = "127.0.0.1:" + 8899;
		RequestBody req = new RequestBody(1, "hello , i am client, i call sync");
		Object response = rpcClient.invokeSync(addr, req, 1000);
		log.info("客户端调用返回：{}", response);
		return R.success(Objects.requireNonNull(response).toString());
	}

	@GetMapping("/client/call")
	public Mono<R<String>> clientCall() throws Exception {
		log.info("clientCall 1 ...");
		String addr = "127.0.0.1:" + 8899;
		RequestBody req = new RequestBody(2, "hello , i am client, i call async");

		Mono<R<String>> mono = Mono.create(rMonoSink -> {
			try {
				rpcClient.invokeWithCallback(addr, req, new InvokeCallback() {
					Executor executor = Executors.newCachedThreadPool();

					@Override
					public void onResponse(Object result) {
						log.info("clientCall 2 ...");
						log.info("客户端调用返回：{}", result);
						rMonoSink.success(R.success(result.toString()));
					}

					@Override
					public void onException(Throwable e) {

					}

					@Override
					public Executor getExecutor() {
						return executor;
					}

				}, 1000);
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
