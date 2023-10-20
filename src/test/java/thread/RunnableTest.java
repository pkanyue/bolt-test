package thread;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

/**
 * @author Rlax
 * @date 2022/09/29
 */
@Slf4j
public class RunnableTest {

    @Test
    void test() {
        new Runnable() {
            @Override
            public void run() {
                log.info("run");
            }
        }.run();
    }

}
