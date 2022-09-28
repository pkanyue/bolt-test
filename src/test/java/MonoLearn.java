import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

/**
 * Reactor中的Publisher
 * Reactor中有两种Publisher：Flux和Mono，其中Flux用来表示0N个元素的异步序列，Mono用来表示01个元素的异步序列，相对于Flux而言Mono更简单一些。
 *
 * @author Rlax
 * @date 2022/09/27
 */
@Slf4j
public class MonoLearn {

    /**
     * 创建Mono
     * reactor中的mono可以通过一些方法创建，常用方法如下：
     * <p>
     * just()：可以指定序列中包含的全部元素。
     * empty()：创建一个不包含任何元素。
     * error(Throwable error)：创建一个只包含错误消息的序列。
     * fromCallable()、fromCompletionStage()、fromFuture()、fromRunnable()和 fromSupplier()：分别从 Callable、CompletionStage、CompletableFuture、Runnable 和 Supplier 中创建 Mono。
     * delay(Duration duration)：创建一个 Mono 序列，在指定的延迟时间之后，产生数字 0 作为唯一值。
     * ignoreElements(Publisher source)：创建一个 Mono 序列，忽略作为源的 Publisher 中的所有元素，只产生结束消息。
     * justOrEmpty(Optional<? extends T> data)和 justOrEmpty(T data)：从一个 Optional 对象或可能为 null 的对象中创建 Mono。只有 Optional 对象中包含值或对象不为 null 时，Mono 序列才产生对应的元素。
     */
    @Test
    public void mono() {
        // 通过just直接赋值
        Mono.just("my name is ffzs").subscribe(log::info);
        // empty 创建空mono
        Mono.empty().subscribe();
        // 延迟生成0
        Mono.delay(Duration.ofMillis(2)).map(String::valueOf).subscribe(log::info);
        // 通过Callable
        Mono.fromCallable(() -> "callback function").subscribe(log::info);
        // future
        Mono.fromFuture(CompletableFuture.completedFuture("from future")).subscribe(log::info);
        // 通过runnable
        Mono<Void> runnableMono = Mono.fromRunnable(() -> log.warn(Thread.currentThread().getName()));
        runnableMono.subscribe();
        // 通过使用 Supplier
        Mono.fromSupplier(() -> new Date().toString()).subscribe(log::info);
        // flux中
        Mono.from(Flux.just("from", "flux")).subscribe(log::info);  // 只返回flux第一个
    }

    /**
     * 使用StepVerifier测试
     * 通过expectNext执行类似断言的功能
     */
    @Test
    public void StepVerifier() {
        Mono<String> mono = Mono.just("ffzs").log();
        StepVerifier.create(mono).expectNext("ffzs").verifyComplete();
        StepVerifier.create(mono).expectNext("ff").verifyComplete();
    }

    /**
     * 创建Flux
     * 上一篇介绍了Mono，mono表示0~1的序列，flux用来表示0~N个元素序列，mono是flux的简化版，flux可以用来表示流
     * <p>
     * 因为是表示连续序列Flux和Mono的创建方法，有些不同，下面是flux的一些创建方法：
     * <p>
     * just()：可以指定序列中包含的全部元素。
     * range(): 可以用来创建连续数值
     * empty()：创建一个不包含任何元素。
     * error(Throwable error)：创建一个只包含错误消息的序列。
     * fromIterable(): 通过迭代器创建如list，set
     * fromStream(): 通过流创建
     * fromArray(T[]): 通过列表创建 如 String[], Integer[]
     * merge(): 通过将两个flux合并得到新的flux
     * interval(): 每隔一段时间生成一个数字，从1开始递增
     *
     * @throws InterruptedException
     */
    @Test
    public void flux() throws InterruptedException {
        Flux<Integer> intFlux = Flux.just(1, 2, 3, 4, 5);
        Flux<Integer> rangeFlux = Flux.range(6, 4);  // 以6开始，取4个值：6,7,8,9
        Flux.fromArray(new Integer[]{1, 3, 4, 5, 6, 12}).subscribe(System.out::println);  // 通过fromArray构建
        Flux<String> strFluxFromStream = Flux.fromStream(Stream.of("just", "test", "reactor", "Flux", "and", "Mono"));
        Flux<String> strFluxFromList = Flux.fromIterable(Arrays.asList("just", "test", "reactor", "Flux", "and", "Mono"));
        // 通过merge合并
        Flux<String> strMerge = Flux.merge(strFluxFromStream, strFluxFromList);
        Flux<Integer> intFluxMerged = Flux.merge(intFlux, rangeFlux);
        strMerge.subscribe(log::info);
        intFluxMerged.subscribe(i -> log.info("{}", i));
        // 通过interval创建流数据
        Flux.interval(Duration.ofMillis(100)).map(String::valueOf)
                .subscribe(log::info);
        Thread.sleep(2000);
    }

    /**
     * subscribe方法使用
     * subscribe方法最多可以传入四个参数：
     *
     * @throws InterruptedException
     */
    @Test
    public void subscribe() throws InterruptedException {
        Flux
                .interval(Duration.ofMillis(100))
                .map(i -> {
                    if (i == 3) throw new RuntimeException("fake a mistake");
                    else return String.valueOf(i);
                })
                .subscribe(info -> log.info("info： {}", info), // 参数1, 接受内容
                        err -> log.error("error: {}", err.getMessage()),  // 参数2， 对err处理的lambda函数
                        () -> log.info("Done"),   // 参数3, 完成subscribe之后执行的lambda函数u
                        sub -> sub.request(10));  // 参数4, Subscription操作, 设定从源头获取元素的个数

        Thread.sleep(2000);
    }

    /**
     * 在Publisher使用subscribe()方法的时候，Subscriber触发回触发一系列的on方法，如onSubscribe()；
     * 为了更好的监控以及观测异步序列的传递情况，设置了一系列的doOn方法，
     * 在触发on方法的时候作behavior的副作用发生用于监控行为的运行情况
     * <p>
     * doOnSubscribe(): 用以监控onSubscribe()方法的执行
     * <p>
     * doOnRequest：对request行为监控产生副作用
     * <p>
     * doOnNext：onNext副作用
     */
    @Test
    public void doOnWithMono() {
        Mono.just("ffzs")
                .map(String::toUpperCase)
                .doOnSubscribe(subscription -> log.info("test do on subscribe"))
                .doOnRequest(longNumber -> log.info("test do on request"))
                .doOnNext(next -> log.info("test do on next1, value is {}", next))
                .map(String::toLowerCase)
                .doOnNext(next -> log.info("test do on next2, value is {}", next))
                .doOnSuccess(success -> log.info("test do on success: {}", success))
                .subscribe();
    }

    /**
     * doOnError：出现error时的副作用，用于监控报错，可以通过错误类型进行筛选
     * <p>
     * doOnComplete：完成时触犯
     * <p>
     * doOnCancel：取消时触发
     * <p>
     * doOnTerminate：终止时触发，无论是成功还是出现异常
     */
    @Test
    public void doOnWithFlux() {
        Flux.range(1, 10)
                .map(i -> {
                    if (i == 3) throw new RuntimeException("fake a mistake");
                    else return String.valueOf(i);
                })
                .doOnError(error -> log.error("test do on error, error msg is: {}", error.getMessage()))
                .doOnEach(info -> log.info("do on Each: {}", info.get()))
                .doOnComplete(() -> log.info("test do on complete"))  // 因为error没有完成不触发
                .doOnTerminate(() -> log.info("test do on terminate"))  // 无论完成与否，只要终止就触发
                .subscribe();
    }

    /**
     * 集成监控 log()方法
     * reactor提供了一个很便利的监控方法：log()
     * <p>
     * 在编写publisher的时候加上log，在subscriber调用的时候会将触发的每一个behavior以日志的形式打印出来：
     * <p>
     * 看个小栗子：
     */
    @Test
    public void logTest() {
        Flux.range(1, 5)
                .map(i -> {
                    if (i == 3) throw new RuntimeException("fake a mistake");
                    else return String.valueOf(i);
                })
                .onErrorContinue((e, val) -> log.error("error type is: {}, msg is : {}", e.getClass(), e.getMessage()))
                .log()
                .subscribe();
    }

    /**
     * 数据在map等方法处理的过程中有可能会出现一些异常情况，如果出现异常需要进行处理reactor提供了几种处理error的方法：
     * <p>
     * onErrorReturn：出现错误直接返回默认值
     * onErrorResume：出现错误使用备用方案
     * onErrorContinue：出现错误跳过错误，使用原数据继续执行
     * onErrorMap：替换错误内容
     * onErrorReturn使用
     */
    @Test
    public void onErrorReturn() {
        Flux.interval(Duration.ofMillis(100))
                .map(i -> {
                    if (i == 2) throw new RuntimeException("fake a mistake");
                    return String.valueOf(100 / (i - 5));
                })
                .doOnError(e -> log.error("error 类型：{}， error 消息： {}", e.getClass(), e.getMessage()))
                // 遇到error直接返回指定value， 错误类型判断可选
                .onErrorReturn("test on error return")
                .subscribe(log::info);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * onErrorResume使用
     */
    @Test
    public void onErrorResume() {
        Flux.interval(Duration.ofMillis(100))
                .map(i -> {
                    if (i == 2) throw new RuntimeException("fake a mistake");  // 设置两个错误，一个runtime错误，一个zero错误
                    return String.valueOf(100 / (i - 5));
                })
                .doOnError(e -> log.error("error 类型：{}， error 消息： {}", e.getClass(), e.getMessage()))
                //一旦遇到error可以用来返回备选方案， 错误类型判断可选
                .onErrorResume(e -> Flux.range(1, 3).map(String::valueOf))
                .subscribe(log::info);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * onErrorContinue使用
     */
    @Test
    public void onErrorContinue() {
        Flux.interval(Duration.ofMillis(100))
                .map(i -> {
                    if (i == 2) throw new RuntimeException("fake a mistake");
                    return String.valueOf(100 / (i - 5));
                })
                // 遇到error之后跳过，可以通过不同错误类型做不同处理
                .onErrorContinue((err, val) -> log
                        .error("处理第{}个元素时遇到错误，错误类型为：{}， 错误信息为： {}", val, err.getClass(), err.getMessage()))
                .subscribe(log::info);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * onErrorMap使用
     */
    @Test
    public void onErrorMap() {
        Flux.interval(Duration.ofMillis(100))
                .map(i -> {
                    if (i == 2) throw new RuntimeException("fake a mistake");
                    return String.valueOf(100 / (i - 5));
                })
                // 当发生错误时更换错误内容
                .onErrorMap(e -> new RuntimeException("change error type"))
                .subscribe(log::info);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    /**
     * (背压)
     * 有这样的情况，上游传递到下游的数据需要进行处理，然而上游推送的速度又很快，下游由于资源等原因来不及处理；
     * 如果这时还是通过不限制上游速度的方式推送数据，就会出问题，
     * 因此Reactive Streams有两一种处理方式，就是通过request的机制向上游传递信号，并指定接收数量；
     * 通过这种方法将push模型转化为push-pull hybrid，这就是backpressure的用法。
     * <p>
     * 通过编写Subscriber实现backpressure
     * 下面介绍backpressure比较原始的写法，通过构建Subscriber控制request的大小：
     */
    @Test
    public void rawBackPressure() {
        Flux<String> flux = Flux.range(1, 10)
                .map(String::valueOf)
                .log();

        flux.subscribe(new Subscriber<String>() {
            private int count = 0;
            private Subscription subscription;
            private final int requestCount = 2;

            @Override
            public void onSubscribe(Subscription s) {
                this.subscription = s;
                s.request(requestCount);  // 启动
            }

            @SneakyThrows
            @Override
            public void onNext(String s) {
                count++;
                if (count == requestCount) {  // 通过count控制每次request两个元素
                    Thread.sleep(1000);
                    subscription.request(requestCount);
                    count = 0;
                }
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onComplete() {

            }
        });
    }

    /**
     * 通过编写BaseSubscriber实现backpressure
     * 可以使用功能更多的BaseSubscriber类处理背压逻辑，因为BaseSubscriber的一些方法可以简化处理的逻辑
     */
    @Test
    public void baseBackPressure() {
        Flux<Integer> flux = Flux.range(1, 10).log();

        flux.subscribe(new BaseSubscriber<Integer>() {
            private int count = 0;
            private final int requestCount = 2;

            @Override
            protected void hookOnSubscribe(Subscription subscription) {
                request(requestCount);
            }

            @SneakyThrows
            @Override
            protected void hookOnNext(Integer value) {
                count++;
                if (count == requestCount) {  // 通过count控制每次request两个元素
                    Thread.sleep(1000);
                    request(requestCount);
                    count = 0;
                }
            }
        });
    }

    /**
     * 通过使用limitRate()方法实现
     * 通过flux的limitrate方式实现调整request数量
     */
    @Test
    public void backPressureLimitRate() {
        Flux.range(1, 10)
                .log()
                .limitRate(2)
                .subscribe();
    }

    /**
     * 无论是使用webflux还是reactor都有访问文件获取数据的需求，这里简单介绍reactor读写文件的方法：
     * <p>
     * 项目目录下准备一个文件命名info.txt，随便写点什么东西：
     * <p>
     * Mono读文件
     * mono可以通过callable进行文件的读取
     * 因为mono元素只能是0或1个，这里通过Files的readAllLines方法获取list
     */
    @Test
    public void monoRead() {
        Mono.fromCallable(() -> Files.readAllLines(Paths.get("/Users/gemiman/IdeaProjects/study/webflux/reactor-demos/info.txt")))
                .log()
                .subscribe();
    }

    /**
     * Flux读文件
     * 两种方法：
     * <p>
     * 通过Files的readAllLines方法获取list，在由Flux的fromIterable接收
     * 通过Files的lines方法活的stream；再由Flux的fromStream接收
     * <p>
     * 两种方法运行结果都一样，不过第一种方法是全部读取完成之后再处理，
     * 第二种方法是流式处理，文件大的话第一种方法会内存消耗过大，推荐使用第二种方法：
     */
    @Test
    public void FluxRead() throws IOException {
        log.info("--------------------from iterable--------------------------");
        Flux.fromIterable(Files.readAllLines(Paths.get("/Users/gemiman/IdeaProjects/study/webflux/reactor-demos/info.txt")))
                .log()
                .subscribe();

        log.info("--------------------from stream--------------------------");
        Flux.fromStream(Files.lines(Paths.get("/Users/gemiman/IdeaProjects/study/webflux/reactor-demos/info.txt")))
                .log()
                .subscribe();
    }

    /**
     * 通过Subscriber写入文件
     * 文件有读就有写，写的处理要在subscribe时进行
     * <p>
     * 构建BaseSubscriber对象，通过BufferedWriter，每次onNext的时候将value写入文件
     * 在onComplete的时候flush并关闭
     */
    @Test
    public void baseWrite() throws IOException {
        if (Files.notExists(Paths.get("info.txt"))) {
            Files.createFile(Paths.get("info.txt"));
        }

        Flux<String> flux = Flux.fromStream(Files.lines(Paths.get("info.txt")))
                .map(String::toUpperCase)
                .log();

        flux.subscribe(new BaseSubscriber<String>() {
            final BufferedWriter bw = Files.newBufferedWriter(Paths.get("newInfo.txt"));

            @SneakyThrows
            @Override
            protected void hookOnNext(String value) {
                bw.write(value + "\n");
            }

            @SneakyThrows
            @Override
            protected void hookOnComplete() {
                bw.flush();
                bw.write("**** do flush **** \n");
                bw.close();
            }
        });
    }

    /**
     * 如果文件比较大的话，在关闭的时候flush显然不是很合理
     * 通过计数，写入一定行数之后清理buffer进行flush操作
     */
    @Test
    public void flushWrite() throws IOException {
        Flux<String> flux = Flux.fromStream(Files.lines(Paths.get("info.txt")))
                .map(String::toUpperCase)
                .log();

        flux.subscribe(new BaseSubscriber<String>() {
            final BufferedWriter bw = Files.newBufferedWriter(Paths.get("newInfo.txt"));
            private int count = 0;

            @SneakyThrows
            @Override
            protected void hookOnNext(String value) {
                count++;
                bw.write(value + "\n");
                if (count % 2 == 0) {       // 设定行数进行清理缓存写入文件
                    bw.write("**** do flush **** \n");
                    bw.flush();
                }
            }

            @SneakyThrows
            @Override
            protected void hookOnComplete() {
                bw.close();
            }
        });
    }

    /**
     * 之前文章也介绍过了Mono和Flux的静态创建方法，这里有两种方法能够更加随意的生成Mono或是Flux：
     * <p>
     * create(): 该方法可以用来创建flux和mono，
     * 通过出入一个callback函数用来对sink进行操作，
     * 添加sink的元素，create可以获取回调中发生的多线程事件，
     * 比如桥接一些异步多值得api
     * <p>
     * generate(): 该方法只能用来生成Flux，
     * 相对create而言，generate在创建过程可以持久化一个state变量记录一些状态，
     * 类似闭包结构，通过一个回调函数进行对sink的操作，还可以对最后的state进行处理。
     * 当然generate可以像create一样使用
     * <p>
     * generate()方法使用
     * 使用generate()创建Flux代码
     */
    @Test
    public void generateFlux() {
        Flux<Object> flux = Flux.
                generate(
                        () -> 0,
                        (i, sink) -> {
                            sink.next(i * i);
                            if (i == 5) sink.complete();
                            return ++i;
                        },
                        state -> log.warn("the final state is:{}", state)
                ).
                log();

        flux.subscribe();
    }


    /**
     * create() 方法使用
     * <p>
     * 使用create()创建Mono代码
     */
    @Test
    public void createMono() {
        Mono<Object> mono = Mono
                .create(sink -> {
                    List<Integer> list = new ArrayList<>();
                    for (int i = 0; i < 5; i++) {
                        list.add(i);
                    }
                    sink.success(list);
                })
                .log();

        StepVerifier.create(mono)
                .expectSubscription()
                .expectNext(Arrays.asList(0, 1, 2, 3, 4))
                .verifyComplete();
    }

    /**
     * 使用create()创建Flux代码
     */
    @Test
    public void createFlux() {
        Flux<Object> flux = Flux
                .create(sink -> {
                    for (int i = 0; i < 5; i++) {
                        sink.next(i * i);
                    }
                    sink.error(new RuntimeException("fake a mistake"));
                    sink.complete();
                })
                .log();

        StepVerifier.create(flux)
                .expectSubscription()
                .expectNext(0, 1, 4, 9, 16)
                .expectError(RuntimeException.class)
                .verify();
    }

    /**
     * Stream中的并行处理非常简单，只要加上parallel()，就可以将stream并行化：
     */
    @Test
    public void streamParallel() {
        Stream.of(1, 2, 3, 4, 5, 6, 7, 8).parallel().map(String::valueOf).forEach(log::info);
    }

    /**
     * Reactor的并行化跟stream一样简单，不同于stream对并行的不可控，Reator还提供了可以对并行运行的调度器Schedulers
     * <p>
     * Schedulers简介
     * 在Reactor中，并行执行以及执行的位置由所Scheduler确定 。
     * <p>
     * Schedulers 类有如下几种对上下文操作的静态方法：
     * <p>
     * immediate()：无执行上下文，提交的Runnable将直接在原线程上执行，可以理解没有调度
     * single()：可重用单线程，使用一个线程处理所有请求
     * elastic()： 没有边界的弹性线程池
     * boundedElastic()：有边界弹性线程池，设置线程限制，默认为cpu核心数*10。达到上限后最多可以提交10万个任务。是阻塞线程的方法
     * parallel(): 固定线程数量的并行线程池，线程数量和cpu内核一样多
     * Reactor 提供了两种通过Scheduler切换上下文执行的方法：publishOn和subscribeOn。
     * <p>
     * publishOn在执行顺序中的位置很重要
     * subscribeOn的位置不重要
     * <p>
     * publishOn方法
     * publishOn可以用来切换执行链中上下文执行模式
     * <p>
     * 从结果可以看出，每次运行publishOn之后就切换了线程
     */
    @Test
    public void publishOnTest() {
        Flux.range(1, 2)
                .map(i -> {
                    log.info("Map 1, the value map to: {}", i * i);
                    return i * i;
                })
                .publishOn(Schedulers.single())
                .map(i -> {
                    log.info("Map 2, the value map to: {}", -i);
                    return -i;
                })
                .publishOn(Schedulers.newParallel("parallel", 4))
                .map(i -> {
                    log.info("Map 3, the value map to: {}", i + 2);
                    return (i + 2) + "";
                })
                .subscribe();
    }

    /**
     * subscribeOn方法
     * <p>
     * subscribeOn使用之后会全方位覆盖，因此如果出现多个subscribeOn()，回执行后触发的
     * <p>
     * 由结果可见，subscribe是反向处理，因此先触发parallel，后触发single，因此都是使用的single
     */
    @Test
    public void subscribeOnTest() throws InterruptedException {
        Flux.range(1, 2)
                .map(i -> {
                    log.info("Map 1, the value map to: {}", i * i);
                    return i * i;
                })
                .subscribeOn(Schedulers.single())
                .map(i -> {
                    log.info("Map 2, the value map to: {}", -i);
                    return -i;
                })
                .subscribeOn(Schedulers.newParallel("parallel", 4))
                .map(i -> {
                    log.info("Map 3, the value map to  {}", i + 2);
                    return (i + 2) + "";
                })
                .subscribe();

        Thread.sleep(100);
    }

    /**
     * subscribeOn和publishOn混合使用
     * 看个例子代码：
     * <p>
     * 优先触发subscribeOn，使用parallel覆盖之后所有过程
     * 当执行完map1后
     * 触发了publishOn，因此该publishOn之后的所有都更换了Schedulers
     * 因此之后的map2,map3都是publishOn的single
     */
    @Test
    public void pSubscribeOnTest() throws InterruptedException {
        Flux.range(1, 2)
                .map(i -> {
                    log.info("Map 1, the value map to: {}", i * i);
                    return i * i;
                })
                .publishOn(Schedulers.single())
                .map(i -> {
                    log.info("Map 2, the value map to: {}", -i);
                    return -i;
                })
                .subscribeOn(Schedulers.newParallel("parallel", 4))
                .map(i -> {
                    log.info("Map 3, the value map to  {}", i + 2);
                    return (i + 2) + "";
                })
                .subscribe();

        Thread.sleep(100);
    }

    /**
     * 由于业务需求有的时候需要将多个数据源进行合并，Reactor提供了concat方法和merge方法：
     * <p>
     * 这两种合并方法的不同：
     * <p>
     * concat是合并的flux，按照顺序分别运行，flux1运行完成以后再运行flux2
     * merge是同时运行，根据时间先后运行
     * <p>
     * 下面对concat和merge相关的方法进行测试，先准备测试数据：
     */
    private Flux<Integer> flux1() {
        return Flux.range(1, 4);
    }

    private Flux<Integer> flux2() {
        return Flux.range(5, 8);
    }


    private Flux<String> hotFlux1() {
        return flux1().map(i -> "[1]" + i).delayElements(Duration.ofMillis(10));
    }

    private Flux<String> hotFlux2() {
        return flux2().map(i -> "[2]" + i).delayElements(Duration.ofMillis(4));
    }

    /**
     * concat相关方法
     * concat代码演示
     * <p>
     * 从结果可以看出先运行完flux1之后再运行flux2
     */
    @Test
    public void concatTest() throws InterruptedException {

        Flux.concat(hotFlux1(), hotFlux2())
                .subscribe(i -> System.out.print("->" + i));

        Thread.sleep(200);
    }

    /**
     * concatWith方法
     * 用法和concat基本相同，写法略有不同：
     */
    @Test
    public void concatWithTest() {
        flux1().concatWith(flux2())
                .log()
                .subscribe();
    }

    /**
     * merge相关方法
     * merge代码演示
     * <p>
     * 运行结果
     * 很明显顺序和concat的区别，是按照时间先后执行
     */
    @Test
    public void mergeTest() throws InterruptedException {

        Flux.merge(hotFlux1(), hotFlux2())
                .subscribe(i -> System.out.print("->" + i));

        Thread.sleep(200);
    }

    /**
     * mergeWith用法
     * 用法和merge相同，写法不同而已
     */
    @Test
    public void mergeWithTest() throws InterruptedException {

        hotFlux1().mergeWith(hotFlux2())
                .subscribe(i -> System.out.print("->" + i));

        Thread.sleep(200);
    }

    /**
     * mergeSequential用法
     * 跟concat有些相似，得到的结果类似
     * 跟concat不同在于，订阅的源是hot型，接收数据后根据订阅顺序重新排序
     * <p>
     * 结果和concat的一样都是
     */
    @Test
    public void mergeSequentialTest() throws InterruptedException {
        Flux.mergeSequential(hotFlux1(), hotFlux2())
                .subscribe(i -> System.out.print("->" + i));

        Thread.sleep(200);
    }

    /**
     * mergeOrdered用法
     * 合并接收之后再排序
     * <p>
     * ->[2]5->[2]6->[2]7->[2]8->[2]9->[2]10->[2]11->[2]12->[1]1->[1]2->[1]3->[1]4
     */
    @Test
    public void mergeOrderedTest() throws InterruptedException {

        Flux.mergeOrdered(Comparator.reverseOrder(), hotFlux1(), hotFlux2())
                .subscribe(i -> System.out.print("->" + i));

        Thread.sleep(200);
    }

    /**
     * mergeComparing用法
     * 合并接收之后再排序
     * <p>
     * ->[2]5->[2]6->[2]7->[2]8->[2]9->[2]10->[2]11->[2]12->[1]1->[1]2->[1]3->[1]4
     */
    @Test
    public void mergeComparingTest() throws InterruptedException {

        Flux.mergeComparing(Comparator.reverseOrder(), hotFlux1(), hotFlux2())
                .subscribe(i -> System.out.print("->" + i));

        Thread.sleep(200);
    }

    /**
     * combineLatest用法
     * <p>
     * 跟concat和merge不同该方法是将多个源的最后得到元素通过函数进行融合的到新的值
     * <p>
     * 运行结果
     * 结果都是flux1和flux2中元素进行融合之后的元素
     */
    @Test
    public void combineLatestTest() throws InterruptedException {

        Flux.combineLatest(hotFlux1(), hotFlux2(), (v1, v2) -> v1 + ":" + v2)
                .subscribe(i -> System.out.print("->" + i));

        Thread.sleep(200);
    }

}
