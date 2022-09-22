package com.rlax.bolt;

import com.rlax.corebin.launch.listener.CorebinApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 服务启动类
 * @author Rlax
 *
 */
@SpringBootApplication
public class BoltTestApplication {

    public static final String APPLICATION_NAME = "bolt-test";

    public static void main(String[] args) {
        CorebinApplication.run(APPLICATION_NAME, BoltTestApplication.class, args);
    }

}
