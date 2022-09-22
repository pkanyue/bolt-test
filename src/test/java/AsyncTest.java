import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * @author Rlax
 * @date 2022/09/14
 */
@Slf4j
public class AsyncTest {

    @Test
    void test() throws InterruptedException {
        log.info("start ...");
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        CompletableFuture<String> c = CompletableFuture.supplyAsync(new Supplier<String>() {
            @Override
            public String get() {
                //做处理
                try {
                    log.info("sleep start ...");
                    TimeUnit.SECONDS.sleep(1);
                    log.info("sleep end ...");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return "hello";
            }
        }, executorService);

        c.thenAccept(log::info);
        log.info("end ...");

        TimeUnit.SECONDS.sleep(6);
    }

}
