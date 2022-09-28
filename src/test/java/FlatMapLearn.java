import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple3;

import java.time.Duration;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 在Stream中我们可以通过flatMap将多维数据打开降维，扁平化处理数据为一维数据。
 * Reactor当然也有这种需求，我们可以使用flatMap和concatMap进行数据的降维处理
 * <p>
 * flatMap、concatMap用法比对
 * <p>
 * 根据示意图可以清楚的看出这三个方法的异同：
 * <p>
 * flatMap和flatMapSequential的订阅是同时进行的，而concatMap的是有先后顺序的
 * concatMap和flatMapSequential的值是跟源中值顺序相同，其中flatMapSequential是经过后排序，二者输出相同
 * flatMap中的值是交错的，根据事件触发
 */
@Slf4j
public class FlatMapLearn {

    @Test
    public void flatMap() throws InterruptedException {
        Flux.just("abcd", "ffzs")
                .flatMap(i -> Flux.fromArray(i.split("")).delayElements(Duration.ofMillis(10)))
                .subscribe(i -> System.out.print("->" + i));
        Thread.sleep(100);
    }

    @Test
    public void flatMapSequential() throws InterruptedException {
        Flux.just("abcd", "ffzs")
                .flatMapSequential(i -> Flux.fromArray(i.split("")).delayElements(Duration.ofMillis(10)))
                .subscribe(i -> System.out.print("->" + i));
        Thread.sleep(100);
    }

    @Test
    public void flatMapIterable() {
        Flux.just("abcd", "ffzs")
                .flatMapIterable(i -> Arrays.asList(i.split("")))
                .subscribe(i -> System.out.print("->" + i));
    }

    @Test
    public void concatMap() throws InterruptedException {
        Flux.just("abcd", "ffzs")
                .concatMap(i -> Flux.fromArray(i.split("")).delayElements(Duration.ofMillis(10)))
                .subscribe(i -> System.out.print("->" + i));
        Thread.sleep(110);
    }

    @Test
    public void concatMapIterable() {
        Flux.just("abcd", "ffzs")
                .concatMapIterable(i -> Arrays.asList(i.split("")))
                .subscribe(i -> System.out.print("->" + i));
    }

    /**
     * 处理业务的时候一定有这样的需求：将多个源数据压缩成一个，Reactor提供了zip和zipWith方法可以做到这一点。
     * <p>
     * zip和zipwith有些不同：
     * <p>
     * zip可以一次合并多个源
     * <p>
     * zipWith一次只能合并两个
     * zipWith支持prefectch
     * <p>
     * zip、zipWith方法测试
     * <p>
     * 准备数据
     */
    private Flux<String> name() {
        return Flux.just("ffzs", "dz", "sleepycat");
    }

    private Flux<Integer> age() {
        return Flux.just(12, 22, 32);
    }

    private Flux<Integer> salary() {
        return Flux.just(10000, 20000, 30000);
    }

    @Data
    @AllArgsConstructor
    static class Employee {
        String name;
        Integer age;
        Integer salary;
    }

    @Data
    @AllArgsConstructor
    static class User {
        String name;
        Integer age;
    }

    /**
     * zip方法使用
     * 多个源压缩到一起，等待所有源发出一个元素之后，将这些元素进行组合
     * <p>
     * 如果是输入publisher会将多个源自动转化为tuple类型
     */
    @Test
    public void zipTest() {
        Flux<Tuple3<String, Integer, Integer>> flux = Flux.zip(name(), age(), salary());
        flux.subscribe(i -> log.info(i.toString()));
    }

    /**
     * 将数据转化为类
     * tuple 通过getT1这类方法获取数据
     */
    @Test
    public void zipTest1() {
        Flux<Tuple3<String, Integer, Integer>> flux = Flux.zip(name(), age(), salary());
        Flux<Employee> employee = flux.map(tuple -> new Employee(tuple.getT1(), tuple.getT2(), tuple.getT3()));
        employee.subscribe(i -> log.info(i.toString()));
    }

    /**
     * 再zip中可以直接给出合并器
     */
    @Test
    public void zipCombineTest() {
        Flux<Employee> flux = Flux.zip(objects -> {
            return new Employee((String) objects[0], (Integer) objects[1], (Integer) objects[2]);
        }, name(), age(), salary());
        flux.subscribe(i -> log.info(i.toString()));
    }

    /**
     * zipWith用法
     * <p>
     * 跟with方法差不多，只能处理两个源：
     */
    @Test
    public void zipWithTest() {
//        Flux<User> flux = name().zipWith(age(), (name, age) -> new User(name, age));
        Flux<User> flux = name().zipWith(age(), User::new);
        flux.subscribe(i -> log.info(i.toString()));
    }

    /**
     * 有的时候流数据有需要转化为其他类型数据，同Stream相同，Reactor也有将数据进行收集的方法：
     * <p>
     * collect () : 将数据根据给出的collector进行收集
     * collectList() : 收集收集为list形式
     * collectSortedList(): 数据收集为list并排序，需要给出排序规则
     * collectMap(): 数据收集为Map形式，是key，value形式，因此如果有重复key会覆盖
     * collectMultimap(): 数据收集为Map形式，是key，collection形式，如果有重复key值写在list中
     * collect()方法
     * <p>
     * 代码示例：
     * 需要注意的是收集后的为Mono
     */
    @Test
    public void collectTest() {
        Mono<Set<String>> flux = Flux.just("ffzs", "vincent", "vincent", "tony", "sleepycate")
                .collect(Collectors.toSet())
                .log();

        flux.subscribe();
    }

    /**
     * collectList() 方法
     * 代码示例：
     * 直接收集为Mono<List<>>
     */
    @Test
    public void collectListTest() {
        Flux.just("ffzs", "vincent", "tony", "sleepycate")
                .collectList()
                .log()
                .subscribe();
    }

    /**
     * collectSortedList()方法
     */
    @Test
    public void collectSortedListTest() {
        Flux.just("ffzs", "vincent", "tony", "sleepycate")
                .collectSortedList(Comparator.comparing(String::length))
                .log()
                .subscribe();
    }

    /**
     * collectMap()方法
     * 代码示例：
     * 根据字符串的长度作为key
     * <p>
     * 由于 ffzs和tony长度都是4,因此ffzs被tony覆盖掉了
     */
    @Test
    public void collectMapTest() {
        Flux.just("ffzs", "vincent", "tony", "sleepycate")
                .collectMap(String::length)
                .log()
                .subscribe();
    }

    /**
     * collectMultimap()方法
     * <p>
     * 所有数据都收集到collection中
     */
    @Test
    public void collectMultimapTest() {
        Flux.just("ffzs", "vincent", "tony", "sleepycate")
                .collectMultimap(String::length)
                .log()
                .subscribe();
    }

}
