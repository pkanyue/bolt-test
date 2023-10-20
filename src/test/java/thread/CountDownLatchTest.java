package thread;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Rlax
 * @date 2022/09/29
 */
@Slf4j
public class CountDownLatchTest {

    @Test
    void test() throws InterruptedException {
        final int totalThread = 10;
        CountDownLatch countDownLatch = new CountDownLatch(totalThread);
        ExecutorService executorService = Executors.newCachedThreadPool();
        for (int i = 0; i < totalThread; i++) {
            executorService.execute(() -> {
                log.info("run ..");
                countDownLatch.countDown();
            });
        }
        countDownLatch.await();
        log.info("end ..");
        executorService.shutdown();

    }

}
