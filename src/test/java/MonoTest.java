import cn.hutool.core.date.DateUtil;
import cn.hutool.core.thread.ThreadUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * @author Rlax
 * @date 2022/09/13
 */
@Slf4j
public class MonoTest {

    @Test
    void just() throws InterruptedException {
        log.info("main start...");

        Mono.just("hello world 2 "+ DateUtil.date())
                .subscribe(s -> {
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    log.info("subscribe : {}", s);
                });

        log.info("main end...");
        TimeUnit.SECONDS.sleep(6);
    }

    @Test
    void create() throws InterruptedException {
        log.info("main start...");

        Mono.create(monoSink -> monoSink.success("hello world 2 "+ DateUtil.date()))
                .subscribe(s -> {
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    log.info("subscribe : {}", s);
                });

        log.info("main end...");
        TimeUnit.SECONDS.sleep(3);
    }

    @Test
    void fromSupplier() throws InterruptedException {
        log.info("main start...");

        Mono.fromSupplier(() -> {
            log.info("supplier ...");
            return "hello world 2 "+ DateUtil.date();
        }).subscribe(s -> {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            log.info("subscribe : {}", s);
        });

        log.info("main end...");
        TimeUnit.SECONDS.sleep(3);
    }

    @Test
    void createPool() throws InterruptedException {
        log.info("main start...");

        Mono.create(monoSink -> monoSink.success("hello world 2 "+ DateUtil.date()))
                .subscribe(s -> {
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    log.info("subscribe : {}", s);
                });

        log.info("main end...");
        TimeUnit.SECONDS.sleep(3);
    }
}
