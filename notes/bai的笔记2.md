### jmm 特性

java  memory model ， Java 内存模型，属于一种规范，定义了Java 内存中数据都存储在工作内存，线程通过将主内存中数据 copy 到工作内存，进行修改

三大特性： 原子性、可见性、有序性

volatile （保证可见性、有序性）

> ***volatile 如何保证可见性？***

volatile 修饰的变量，如果线程对该变量进行了修改，就会更新到主内存，其他线程的变量将被置为无效，其他线程重新加载最新的主内存数据

> ***为什么要保证有序性？***

在单线程的情况下，指令重排序并不会影响程序的执行结果。但是在多线程的情况下，如果发生指令重排序，可能会导致意外错误的结果

> ***volatile 如何保证有序性？***

编译器与处理器能够对代码进行优化，代码的执行顺序与编写顺序不一致。volatile 通过在指令中插入**内存屏障**，保证指令顺序执行

> ***什么是 Happen-before ?***

happen-before 是程序执行指令的一个原则，在进行指令重排序的时候，需要遵循 happen-before 的原则，即定义了程序指向的顺序。如果2个操作无法通过 happen-before 推导，则可以任意对他们进行排序

synchronized 保证原子性，也可以保证有序性

> ***volatile 实现机制？***

加入 volatile 关键字后，会多出一个 lock 前缀指令， 相当于一个内存屏障，确保指令重排序时后面的指令不会出现在前面，也不会把前面的指令排到后面。在执行到内存屏障的位置时，会将缓存的修改写入主存，其他cup 中对应的缓存也会置为无效

> ***除了 synchronized 可以保证原子性，还有其他的方式吗？***

java.util.concurrent.atomic  原子类包中的原子类，可以保证原子性

> ***原子类的底层实现原理？***

原子类底层通过 unsafe 类保证原子性，而 unsafe 类的实现通过计算机原语 CAS 

```JAVA
 do {
        var5 = this.getIntVolatile(var1, var2);
 } while(!this.compareAndSwapInt(var1, var2, var5, var5 + var4));
```

汇编指令 cas 是原子的，不允许中断的，因此保证了原子性

> ***为什么 unsafe 使用 CAS ,而不是 synchronized?***

如果使用了 synchronized ，在高并发的情况下，会导致并发效率降低。使用 cas 可以有效的提高并发能力

> ***CAS 有什么缺点？***

1. CAS 比较与交换，通过循环获取，可能导致多次循环，消耗 cup 资源
2. CAS 每次只能修改一个值，不发保证代码块的原子性， synchronized 可以锁定整个块
3. CAS 存在 ABA的问题（ABA 的问题是中间值如果被其他线程获取，又是 BAB 的问题）

> ***如何解决 CAS 可能的 ABA 问题？***

 通过将每次修改增加一个版本号，AtomicStampedReference  支持版本号，每次修改版本号+1。在比较与交换只有版本号与期望值都相等才会修改成功

> ***什么是原子引用？***

原子包下 AtomicReference ，可以将实体包装为原子类型。实现原子性，保证数据安全

> ***ArrayList 线程不安全的异常是什么？***

```java
    List<String> list = new ArrayList<>();
        for(int i=1;i<30;i++){
            new Thread(()->{
                list.add(UUID.randomUUID().toString().substring(0,8));
                System.out.println(list);
            }).start();
        }
```

java.util.**ConcurrentModificationException**  ,多线程下 ArrayList 修改异常

> ***如何解决多线程ArrayList异常？***

1. 使用 Vector
2. Collectors.synchronizedList()
3. CopyOnWriteArrayList()  写时复制，读写分离。在写的时候加锁，写完释放锁。
4. CopyOnWriteArrayList 内部使用 ReentrantLock ,在 put 方法时加锁，结束释放锁，value 存储在 volatile 修饰的 Object[]

> ***HashSet 是否线程安全？如何实现线程安全***

HashSet 线程不安全 ，并发报 java.util.ConcurrentModificationException  

1. Collections.synchronizedSet()
2. CopyOnWriteArraySet() ,底层创建的还是 CopyOnWriteArrayList()

> ***HashSet 底层是什么？***

HashSet 底层使用的是 HashMap,  底层使用了 key , value 存储固定的 precent 常量

> ***HashMap 是否线程安全？ 如何实现线程安全？***

HashMap 线程不安全，多线程报 java.util.ConcurrentModificationException 

实现线程安全 1. 使用 Collections.synchronizedMap()  2.使用 ConcurrentHashMap

### 锁

> ***什么是公平锁，非公平锁？  ReentrantLock 如何实现?***

公平锁：多线程并发先到先得 

非公平锁：抢占式，可能造成饥饿现象，但吞吐量大

ReentrantLock 默认**使用的非公平锁**，通过构造函数 true 实现公平锁

Synchronized 是非公平锁

> ***什么是可重入锁（递归锁）？***

 如果线程已经持有该对象的锁，可以重复获取。目的是防止死锁

ReentrantLock ，Synchronized 均为可重入锁

> ***什么是自旋锁？***

如果一个线程尝试获取锁的时候，其他线程已经获取，通过循环等待尝试获取锁，直到获取成功。可能会造成 busy-waiting

通过 CAS 可实现自旋锁

> ***手写一个自旋锁***

```java
public class CasLock {

    private final AtomicReference<Thread> reference = new AtomicReference<Thread>();

    public  void lock(){
        Thread current = Thread.currentThread();
        while (!reference.compareAndSet(null,current)){
        }
        System.out.println(Thread.currentThread().getName()+"成功获取锁");
    }

    public void unlock(){
        System.out.println(Thread.currentThread().getName()+"释放锁");
        Thread current = Thread.currentThread();
        reference.compareAndSet(current,null);
    }

}
```

测试自旋锁

```java
   public static CasLock casLock = new CasLock();

    public static void main(String[] args) {

        for(int i=0;i<10;i++){
            new Thread(Test::m1,"t"+i).start();
        }
    }

    public static void m1(){
        casLock.lock();
        System.out.println("do something");
        casLock.unlock()
    }
```

> ***什么是独占锁 ？ 什么是共享锁 ？ 什么是读写锁？***

独占锁即写锁， 共享锁即读锁

读写锁：读可以共享，写独占 **ReentrantReadWriteLock**

> ***实现一个使用读写锁实现的缓存？***

```java
    ReentrantReadWriteLock reentrantReadWriteLock =  new ReentrantReadWriteLock();

    Lock readLock = reentrantReadWriteLock.readLock();
    Lock writeLock = reentrantReadWriteLock.writeLock();
```

> ***什么是 CountDownLatch ? CyclicBarrier ?  Semaphore ?***

```JAVA
    CountDownLatch latch = new CountDownLatch(4);

        for(int i=1;i<5;i++){
            new Thread(()->{
                System.out.println(Thread.currentThread().getName()+"离开了");
                latch.countDown();
            },"t"+i).start();
        }

        //主线程进入等待状态，直到 latch 值为 0
        latch.await();
        System.out.println("门锁了");
```

CountDownLatch ，倒计时减法，需要设置初始值。调用 await() 将进入阻塞状态，直到 latch = 0 则自动解除阻塞。（不可重复使用）

> ***CountDownLatch  实现原理是什么 ？***

CountDownLatch 集成自 **AbstractQueuedSynchronizer** 



```java
 CyclicBarrier barrier = new CyclicBarrier(7,()->{
            System.out.println("神龙现身");
        });

        for(int i = 1; i<8 ;i++){
            int finalI = i;
            new Thread(()->{
                System.out.println("收集到第"+ finalI +"号龙珠");
                try {
                    barrier.await();
                }catch (Exception e){

                }
            },"thread"+i).start();
        }
```

CyclicBarrier ，加法，默认初始值为 0 ，并指定到达指定条件执行指定 Runnable 内容。（可以不指定满足条件后的操作）

> ***CyclicBarrier 内部实现原理是什么 ？***

CyclicBarrier 内部使用了 **ReentrantLock** 

```java
Semaphore semaphore = new Semaphore(3,true);

        for(int i=1;i<7;i++){
            new Thread(()->{
                try {
                    semaphore.acquire();
                    System.out.println(Thread.currentThread().getName()+"成功获取到车位");
                    TimeUnit.SECONDS.sleep(new Random().nextInt(5));
                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    System.out.println(Thread.currentThread().getName()+"离开了");
                    semaphore.release();

                }
            },"t"+i).start();
        }
```

Semaphore (发音：三木方),信号量，可以指定是否使用公平锁，通过指定资源数量，可以增加与释放资源，如果资源不足将进行阻塞

> ***Semaphore 内部实现原理 ？***

内部通过 **AbstractQueuedSynchronizer** ，

> ***CountDownLatch  与 Semaphore 都使用到了 AQS ， AQS 是什么 ？***

CyclicBarrier 使用 ReentrantLock ,ReentrantLock 内部也是使用 AQS 

> synchronized 与 lock 的区别 ？

- synchronized 属于关键字 ， lock 是 api 层接口
- synchronized  不能收到释放， lock 上锁与释放必需手动
- synchronzied 不可中断 ， lock 可以中断
- 线程唤醒， synchronized 不能指定具体线程唤醒 ，Lock 可指定多种 condition

### 集合

#### 1.ConcurrentSkipListMap

- 底层采用跳表结构，数据按照关键节点 key 升序存储
- 跳跃表采用的是单向链表+索引结构实现
- 索引可以有多层，查询效率越高，占用空间越大
- 使用跳表结构的有 redis 有序集合， leveldb

### kafka  

#### 1.简介

消息系统负责将数据从一个应用程序传输到另一个应用程序，包括两种模式，**点对点，发布订阅**

*apache kafka 是一个分布式的发布-订阅消息系统。kafka 将消息保存在磁盘上，构建在 zookeeper 之上。*

1. 生产者与消费者：进行消息发送与消息接收

   > kafka 消息接受采用的是 **pull 模式**，由生产者主动拉取消息。如果是 broker push 推送消息，对于不同消费能力的消费者，数量过多，有的可能要崩溃，而如果 broker 必需知道下游消费者的消费能力，导致资源浪费。
   >
   > kafka pull 拉取，如果 broker 没有消息，为了避免轮询，kafka 可以配置让消费者阻塞直到新消息送达。

2. broker：集群中每一个实例称为一个 broker 

3. 主题topic： 每个 topic 都可以分成多个 partition , topic 存储的是一类消息

4. 分区partition :  每个partition 在存储层面是 append log 文件，发布到 partition 的消息都会被追加到 log 文件尾部。

   > kafka 基于文件存储，如果文件足够大，容易到达磁盘上限。分区能够扩大存储容量，每个分区对应一个文件

5. 偏移量 offset :  每个分区对应一个文件，而消息在文件中存储的位置称为 offset 偏移量，（Long 型数字）

6. 副本 replicated : 分区的数据可以备份到其他 broker ，副本提供高可用。分区 partition 与 副本 replicated 分为**一个 leader 和多个 follower** 

#### 2.Server配置

`server.properties` 配置kafka 服务端

| 配置                                                         | 说明                                                         |
| ------------------------------------------------------------ | ------------------------------------------------------------ |
| broker.id=0                                                  | 每一个 broker 在集群中都要有唯一的标识                       |
| port=9092                                                    | 提供给客户端的响应端口                                       |
| advertised.listeners=PLAINTEXT:81.68.206.246:9092            | 对外暴漏的服务                                               |
| num.network.threads=3<br />num.io.threads=8                  | broker 处理消息的最大线程数<br />broker 处理磁盘 IO 的线程数 |
| socket.send.buffer.bytes=102400<br />socket.receive.buffer.bytes=102400<br />socket.requiest.max.bytes=104857600 | socket 发送缓冲区<br />socker接受缓冲区<br />socket 请求的最大数值 |
| log.dirs=<br />num.partitions=1                              | kafka 存放数据的地址<br />每个 topic 默认的分区数            |
| offsets.topic.replication.factor=1<br />transaction.state.log.replication.factor=1<br />transaction.state.log.min.isr=1 |                                                              |
| log.retention.hours=168<br />log.segment.bytes=1073741824<br />log.retention.check.interval.ms=300000 | 日志存储时间，默认 7 * 24<br />日志存储大小<br />文件大小检查的周期时间 |
| zookeeper.connect=<br />zookeeper.connection.timeout.ms=6000 | zookeeper  配置                                              |
| group.initial.rebalance.delay.ms=0                           |                                                              |
| message.max.bytes=1000000                                    | 消息体最大字节                                               |

*注：日志文件大小与时间任意条件满足，触发删除策略*

#### 3.Producer 配置

| 配置                             | 说明                                                         |
| -------------------------------- | ------------------------------------------------------------ |
| bootstrap.servers=localhost:9092 |                                                              |
| compression.type=none            |                                                              |
| #partitioner.class=              |                                                              |
| #request.timeout.ms=             |                                                              |
| #max.block.ms=                   |                                                              |
| #linger.ms=                      |                                                              |
| #max.request.size=               |                                                              |
| #batch.size=                     | 缓冲区大小                                                   |
| #buffer.memory=                  | 控制生产者可用于缓存的存储器的总量                           |
| request.required.acks=1          | 1 : 发送消息，等待 leader 确认<br />0：发送完消息不进行确认<br />-1 ：等待所有副本确认 |

#### 4.Consumer 配置

| 配置                             | 说明                                                         |
| -------------------------------- | ------------------------------------------------------------ |
| bootstrap.servers=localhost:9092 |                                                              |
| group.id=test-consumer-group     |                                                              |
| auto.offset.reset=largest        | 当 zookeeper 没有初始的 offset 时候<br />smallest : 重置为最小值<br />largest : 重置为最大值<br />anythingelse ：抛出异常 |

配置参考:https://www.cnblogs.com/alan319/p/8651434.html

#### 5.生产者&消费者

**生产者**

**消费者**

- 消费者使用相同的 group id  加入群组
- 消息只能被组内的一个消费者消费

#### 6.ISR 机制

ISR 机制被称为“不丢失消息”机制

**副本 replica**

Kafka topic 可以设置多个副本，数量应当小于 broker 数量 , partition 分区有 1 个 leader  和 0到多个 follower 。

当生产者向分区写数据时，根据 ack 机制（默认 1），只会向 leader 中写数据，然后 leader 将数据同步到其他 副本中。follower 不对外提供服务，

只有 leader 挂了才重选 leader

**同步**

kafka 不是完全同步，也不是完全异步，是一种特殊的 ISR (In Sync Replica)

- leader 会维持一个与其保持同步的 副本集，该集合就是 ISR ,由 leader 动态维护
- 要保证 kafka message 不丢失，要保证 ISR 至少一个存活，并且 commit 成功

#### 7.ack 机制

ack 机制指生产者的消息发送确认机制。ack 的值由三个：

- 1 ： producer 只要收到一个分区副本成功写入的通知就认为消息推送成功（**默认值**，可能会丢失消息）
- 0： producer 发送一次就不再发送，不管是否成功
- -1：producer 只有收到分区内所有副本都写入成功的通知才认为推送成功

#### 总结

1. 一旦消费者数量超过分区数量，新消费者将不再接收任何消息
2. kafka 副本数量最好不超过 broker 数量
3. 

#### 面试题

***1.kafka 可以脱离 zk单独使用吗***

​	kafka 不能脱离 zookeeper 单独使用 ，因为 kafka 使用 zookeeper 管理与协调 kafka 节点服务器

**2.kafka 与其他消息队列的区别**

> ***使用 kafka 集群需要注意什么 ?***

集群的数量不是越多越好，最好不要超过 7 个 。节点越多，需要复制消耗的时间越长，降低了吞吐率

集群数量最好是单数 ，因为超过一半的故障集群就不可用，设置单点容错效率更高

> ***kafka 数据的保存策略？*** 

kafka 有 2 种数据保存策略： 按照过期时间、按照存储消息的大小。 

如果任意一个条件满足，kafka 会清空数据

> ***kafka 为什么快？***

*顺序写入 与 MMFile*

磁盘的写入都先寻址、写入。顺序写入提高了磁盘写入的速度。kafka 每个 partition 是一个文件，收到消息 kafka 会把数据插入文件尾部（保存所有的数据）。对于消费者，每个消费者都有 一个 offset 标识读取数据的下表。一般情况下 offset 存储在 zookeeper 。

*Memory Mapper Files (MMF)*

内存映射文件，工作原理是直接利用操作系统的 Page 来实现文件到物理内存的直接映射。映射完成后对物理内存的操作会被同步到硬盘上。

*读取数据*

*基于 sendfile 实现 Zero Copy*

sendfile 减少了传统读取数据的 copy 次数

*批量压缩*

多数情况下，系统的瓶颈不是 CPU 或者 磁盘，而是网络IO 。kafka 使用批量压缩，多个消息一起压缩，支持多种压缩协议（Gzip ,Snappy）

***2.Kafka  消息丢失与重复消费***  ?

要确定 kafka 的消息是否丢失或者重复 ，从两个方面入手分析：消息发送和消息消费

1.消息发送

​	kafka 消息发送有2种方式： 同步 和 异步，默认是同步方式， 可通过 producer.type 属性进行配置。 kafka 通过 require.required.acks 属性确认消息的生产:

​	0 - 表示不进行消息接收是否成功的确认

​	1 - 表示当 leader 接收成功时确认

​	-1 -表示 Leader 和 Follower 都接收成功时确认

 根据上述情况，有 6 种生产场景，会丢失的情况如下：

（1） acks = 0 , 不和 kafka 集群进行消息接收确认，则网络异常，缓存区满等情况下，数据可能丢失

（2）acks = 1 ,同步模式下，只有 Leader 确认接受成功后但挂掉，副本没有同步，数据可能丢失

2.消息消费

当消费者从集群中把消息取出来，并提交了新的消息 offset 值后，还没来得及消费就挂掉，那么下次消费时之前没有消费成功的消息就丢失

**针对消息丢失：同步模式下，确认机制设置为 -1 ，异步模式下，为防止缓冲区满，可以设置文件配置，当缓冲区满时让生产者一直处于阻塞状态**

**针对消息重复：将消息的唯一标识保存到外部介质中，每次消费时判断是否处理过即可**

> ***zookeeper 是什么？***

zookeeper 是一个分布式的应用程序协调服务

> ***zookeeper 如何保证主从节点的状态同步？***

zookeeper 的核心是原子广播，这个机制保证了各个 server 之间的同步。实现这个机制的协议叫做 ZAB 协议 ， 有2种模式，恢复模式（选主节点）与广播模式（消息同步）。当服务启动或者leader奔溃后， ZAB 就进入恢复模式，当 leader 被

选举出来，并且多数 server 完成了 和 leader 的状态同步后，恢复模式就结束了。 状态同步保证了 leader 和 server 具有相同的系统状态。

> ***zookeeper 的通知机制是什么？***

客户端会对某个 znode 建立一个 watcher 事件，当该 znode 发生变化时，客户端会收到 zookeeper 的通知，然后客户端可以根据 znode 变化来做出业务上的改变。





### zookeeper

#### 1.简介

zookeeper 为大型分布式计算提供分布式配置服务，同步服务，命名注册等，通过冗余服务提供高可用

zookeeper 提供的名称空间类似于标准文件系统， key - value 的形式存储，通过 `/` 分割路径元素 ，名称空间每个节点都是一个路径标识

#### 2.配置

| 配置                                                         | 说明 |
| ------------------------------------------------------------ | ---- |
| tickTime=2000                                                |      |
| initLimit=10                                                 |      |
| syncLimit=5                                                  |      |
| dataDir=/usr/local/zookeeper/data                            |      |
| clientPort=2181                                              |      |
| server.1=127.0.0.1:2887:3887<br />server.2=127.0.0.1:2888:3888<br />server.3=127.0.0.1:2889:3889 |      |

> 2181 ：对 client 提供的服务端口
>
> 2888：集群内机器通信使用的端口
>
> 3888：选举 leader 端口

#### 3.znode 结构

zookeeper 中存储的数据是由 znode 组成的，节点也称为 znode , 并以 key -value 形式存储数据。

```shell
ls /
ls /zookeeper
```

zookeeper  默认的根节点是  /zookeeper ，可以**创建根节点以及子节点**

```
create  -e  /zookeeper/test 1
```

可以查看节点的属性 

```shell
get  /zookeeper/test

1
cZxid = 0x6c
ctime = Mon Jul 12 15:25:31 CST 2021
mZxid = 0x6c
mtime = Mon Jul 12 15:25:31 CST 2021
pZxid = 0x6c
cversion = 0
dataVersion = 0
aclVersion = 0
ephemeralOwner = 0x10015b218ef002e
dataLength = 1
numChildren = 0
```

*第一行值 1 为该节点的  value 值*

| cZxid          | 创建节点时的事物id                                           |
| -------------- | ------------------------------------------------------------ |
| ctime          | 创建节点的时间                                               |
| mZxid          | 最后修改节点时的事物id                                       |
| mtime          | 最后修改节点时的时间                                         |
| pZxid          | 最后修改子节点的事物id                                       |
| cversion       | 子节点版本号，每次修改版本号加 1                             |
| dataVersion    | 数据版本号，每次修改版本号加 1                               |
| aclVersion     | 权限版本号                                                   |
| ephemeralOwner | 创建该临时节点的会话 sessionId  (如果是持久节点，那么属性值为 0) |
| dataLength     | 该节点的数据长度                                             |
| numChildren    | 该节点拥有子节点的数量                                       |

#### 4.基础命令

| 命令                           | 说明                                                         |
| ------------------------------ | ------------------------------------------------------------ |
| ls  /zookeeper/test            | 查询路径下的目录列表                                         |
| ls2  /zookeeper/test           | 比 ls 更详细                                                 |
| get  /zookeeper/test           | 获取节点的数据与信息                                         |
| get /zookeeper/test **watch**  | watch 端口对节点进行监听，其他端口修改该节点会收到通知       |
| set /zookeeper/test 2          | test 节点的 value 被修改为 2                                 |
| stat  /zookeeper/test          | 查看节点状态                                                 |
| stat  /zookeeper/test watch    | 查看节点状态并监控                                           |
| create [-s] [-e] path data acl | -s ，-e 是可选参数，-s 代表顺序节点， -e 代表临时节点<br />path 创建节点的路径<br />data 节点存储的数据<br />acl 访问权限，默认 world |
| delete /zookeeper/test         | 删除test 节点                                                |

**节点特性**

- 同一级节点 key 是唯一的
- 创建节点必须带上全路径
- session 关闭，临时节点清除
- 自动创建顺序节点
- watch 监听，单次触发后，事件失效

#### 5.数据同步流程

zookeeper 中，主要依赖 ZAB 协议实现分布式数据一致性，ZAB 协议分为2部分：

- 消息广播
- 奔溃恢复

**ZAB 恢复策略**

选举 zxid 最大的节点作为新的 leader , 新 leader 将事物日志中尚未提交的消息进行处理

#### 6.leader 选举

leader 选举规则

- 服务器 id ( myid ) : 编号越大的在选举算法中权重越大
- 事物 id (zxid ): 值越大说明数据越新，权重越大

#### 7.zk分布式锁

zk 实现的排他锁、共享锁两类分布式锁

**排他锁 exclusive Lock**

排他锁，也称为独占锁，zookeeper 实现方式是利用 zookeeper 的同级节点唯一特性，在创建临时节点时，只有一个客户端可以创建成功，未创建成功的

可以在该锁节点注册 watch 监听事件

**共享锁 shared Lock**

zookeeper 实现共享锁的方式是客户端调用 create 方法创建定义锁方式的零食节点，通过 getChildren 查询子节点列表

*实际开发中，curator  工具包已经封装好了分布式锁*

参考 ：https://www.jianshu.com/p/a974eec257e6

### dubbo

#### 1.简介

官网：https://dubbo.apache.org/zh/docs/

Dubbo 是一款高性能，轻量级的开源 Java  RPC 框架，提供几大核心功能

- 面向接口的远程方法调用
- 智能容错与负载均衡
- 服务自动注册和发现

dubbo 主要包含角色：

- provider 服务提供方，暴漏服务并注册可用服务列表到注册中心 
- consumer 服务调用方，从注册中心拉取服务列表进行调用（因此当注册中心崩溃，依然短时间可用，因为服务列表是定期从注册中心获取）
- registry 服务注册与发现
- moniter 服务运行监控中心

#### 2.rpc

RPC (remote procedure call) ，一种远程过程调用，不需要了解底层网络技术的协议

**可以使用 http 调用 ，为何还要 rpc ?**

- 负载均衡 使用 dubbo 可以实现负载均衡调用
- 服务调用链路生成 ，了解服务之间的调用链路
- 服务监控

### rokcetmq

### rabbitmq

Rabbitmq 是一种消息队列，常见的消息队列有 ActiveMQ ,RabbitMQ ,kafka ，ZeroMQ , RocketMQ , 甚至 Redis 也支持 MQ

#### 1.简介

消息是应用之间传送的数据，可能是简单为文本字符串，也可能是嵌入的对象。

消息队列是一种应用之间的通信方式，消息发送后立即返回，由消息队列确保消息的可靠传递。消息队列可以当作应用之间异步协作的机制。

**RabbitMQ 是由 erlang 语言开发的 AMQP 实现。**

*主要作用：削峰填谷，应用解耦*

#### 2.一些名词

- ConnectionFactory 连接管理器
- Channel 信道
- Exchange 交换器
- Queue 队列
- RoutingKey 路邮键
- BindingKey 绑定键

#### 3.消息持久化



#### 4.消息丢失与重复消费

https://blog.csdn.net/tanga842428/article/details/79550805

https://blog.csdn.net/lucky_ly/article/details/89590646

#### 5.角色分类

| 角色          | 描述                       |
| ------------- | -------------------------- |
| none          | 不能访问 management plugin |
| management    | 普通管理者                 |
| policymaker   | 策略制定者                 |
| monitoring    | 监控者                     |
| administrator | 超级管理员                 |







### zeromq

### elasticsearch

中文官方文档 https://www.elastic.co/guide/cn/elasticsearch/guide/current/preface.html

#### 1.配置文件

| 配置                                                 | 说明 |
| ---------------------------------------------------- | ---- |
| cluster.name: my-application                         |      |
| node.name: node-1                                    |      |
| node.attr.rack: r1                                   |      |
| path.data: /path/to/data                             |      |
| path.logs: /path/to/logs                             |      |
| bootstrap.memory_lock: true                          |      |
| network.host: 192.168.0.1                            |      |
| http.port: 9200                                      |      |
| discovery.zen.ping.unicast.hosts: ["host1", "host2"] |      |
| discovery.zen.minimum_master_nodes:                  |      |
| gateway.recover_after_nodes: 3                       |      |
| action.destructive_requires_name: true               |      |



Elasticsearch 是一个**实时的分布式搜索分析引擎**，能够以前所未有的速度检索数据，常被用作全文检索、结构化搜索、分析

建立在全文搜索引擎 Apache Lucene 基础之上，但隐藏 Lucene 的复杂性，使用 **Java** 编写，提供Restful API

#### 2.简单操作

**计算集群的文档**

```json
GET _count 
{
  "query": {
    "match_all": {}
  }
}
```

创建一个员工

```json
PUT /people/emp/1
{
  "first_name":"zhang",
  "last_name":"san",
  "age":25,
  "about":"I love to go rock climbing",
  "interests":["music","sports"]
}
```

people 索引名称，emp 类型名称 ，1 员工id

**检索文档**

```
GET /people/emp/1  //查询 id=1 的员工

HEAD /people/emp/1 //判断 id=1 的员工是否存在

DELETE /people/emp/1 //删除 id=1 的员工

GET /people/_search  //查询索引下所有文档
```

*注意：6.x 版本 index 只能创建一个 type ，7.x 版本 type 完全移除*

**使用表达式查询**

```json
GET /people/_search
{
  "query": {
    "match": {
      "first_name": "zhang"
    }
  }
}
```

**使用过滤器 Filter** 

```json
GET /people/_search
{
  "query": {
    "bool": {
      "must": [
        {
          "match": {
            "first_name": "zhang"
          }
        }
      ],
      "filter": {
        "range": {
          "age": {"gte": 10, "lte": 30}
        }
      }
    }
  }
}
```

**查询喜欢rock的所有员工**

```json
GET /people/_search
{
  "query": {
    "match": {
      "about": "go rock"
    }
  }
}
```

Elasticsearch 默认按照相关性排序，每个文档的匹配程度。每个匹配的文档都会有评分，评分越高匹配程度越高

**短语匹配**

```json
GET /people/_search
{
  "query": {
    "match_phrase": {
      "about": "go rock"
    }
  }
}
```

 *注：短语匹配 go rock 不匹配 go 或者 rock ，匹配的是 go rock 整体*

**高亮展示匹配字段**

```json
GET /people/_search
{
  "query": {
    "match": {
      "about": "go rock"
    }
  },
  "highlight": {
    "fields": {
      "about":{
        "pre_tags":"++"
        , "post_tags": "--"
      }
    }
  }
}
```

#### 3.集群内部原理

集群是由一个或者多个拥有相同 cluster.name 配置的节点组成

**集群健康检测**

```http
GET _cluster/health

{
  "cluster_name" : "es-test",
  "status" : "yellow",
  "timed_out" : false,
  "number_of_nodes" : 1,
  "number_of_data_nodes" : 1,
  "active_primary_shards" : 5566,
  "active_shards" : 5566,
  "relocating_shards" : 0,
  "initializing_shards" : 0,
  "unassigned_shards" : 539,
  "delayed_unassigned_shards" : 0,
  "number_of_pending_tasks" : 0,
  "number_of_in_flight_fetch" : 0,
  "task_max_waiting_in_queue_millis" : 0,
  "active_shards_percent_as_number" : 91.17117117117117
}
```

**status** 指示当前集群的状态

- green 主分片与副本分片都正常
- yellow  所有主分片正常，有副本分片未启动
- red 有主分片没能启动

**索引**

> 索引是指向一个或者多个物理分片的逻辑命名空间

**创建索引**

```
PUT /blog
{
  "settings": {
    "number_of_shards": 3, 
    "number_of_replicas": 1
  }
}
```

删除索引

```
DELETE /blog
```

查询一个文档

```
GET /people/emp/1
```

****查询文档指定的字段****

```
GET /people/emp/1?_source=age,about
```

****分页查询****

```
GET people/_search?size=3&from=0
```

****查询多个文档****

```
GET /_mget
{
  "docs":[
    {
    "_index":"people",
    "_type":"emp",
    "_id":1
   }
  ]
}
```

**更新一个文档**

```
PUT /people/emp/1
{
  "age":26
}
```

*注：每更新一个文档，_version 的值都会增加*

**更新文档的部分字段**

```
POST /people/emp/1/_update
{
  "doc": {
    "age":26
  }
}
```

**创建文档**，未指定 id 将使用系统的默认 id

```
POST /people/emp/
{
  "age":26
}
```

**处理冲突**

Elasticsearch 在并发情况下通过**乐观锁**保证数据的一致性，通过 **_version** 是否一致确定修改是否成功

**路由查询文档**

当索引创建文档的时候，文档会被存储到一个分片中。如何决定创建在哪个分片？根据如下公式

```
shard = hash(routing) % number_of_primary_shards
```

routing 是可变的值，默认是文档的 id , 将文档 id 取哈希值，再对分片数取余操作得到存储文档的分片。

**为什么分片数一旦指定即不可修改，如果修改分片数，则所有的路由都将失效**

**查询索引结构**

```
GET people/_mapping
```

Elasticsearch 的数据类型大概分为 2 种 ，精确值 与 全文

- 精确值  要么匹配，要么不匹配
- 全文 查询相关性

#### 4.倒排索引



#### 5.kibana 

后台启动方式

```shell
nohup ./bin/kibana  &
```





#### 6.Canal & Maxwell

> 需求背景：项目业务数据都存储在 mysql 数据库， 在 web 需要检索，搜索框模糊检索等工作通过 es 做查询，为了保证数据的有效性，
>
> 需要将 mysql 数据与 es 数据保持实时的同步（最开始是用定时任务做同步，缺点是不是实时数据）

Canal 是阿里的一个中间件组件，Maxwell 是国外开源组件。作用都是 Mysql -> 中间件 -> kafka ->ES/Hbase

|                 | Canal    | Maxwell                             |
| --------------- | -------- | ----------------------------------- |
| 语言            | java     | java                                |
| HA              | 支持     | 定制                                |
| 数据落地        | 定制     | 落地到Kafka                         |
| 分区            | 支持     | 支持                                |
| Bootstrap(引导) | 不支持   | 支持                                |
| 数据格式        | 格式自由 | Json（格式固定） spark json ---> DF |
| 随机读          | 支持     | 支持                                |

**数据必要库配置**

查询数据库当前配置

```sql
use mysql;

show variables like '%binlog%';

show variables like %log_bin%;
```

| 数据库配置    | 说明 |
| ------------- | ---- |
| log_bin       | on   |
| binlog_format | row  |

##### Canal 

> 使用 Canal 中间件，将 mysql 数据的增量同步到 es ，整体的结构是：
>
> mysql 数据变化 -> binlog -> canal-server(伪装 slave) ->canal-adapter -> es

使用 实现 mysql 数据实时同步到 es, **Canal 基于数据库增量日志解析，提供增量订阅和消费**

**原理**: canal 把自己伪装称 mysql slave ， 模拟 mysql slave 的交互协议向 mysql master 发送 dump 协议，mater 收到 dump 请求，开始推送 binary log 

给 canal ,然后 canal 解析 binary log ，发送到存储的目的地(如： **mysql , kafka , elasticsearch , rdb**)

**canal  安装与使用**

下载官网地址：https://github.com/alibaba/canal/releases

搭建项目参考：https://blog.csdn.net/jcmj123456/article/details/109705562

- adapter  客户端，将数据写入目的地
- admin ，可以不用，管理端
- deployer ,canal 服务端

注：版本一定是 **v1.1.5-alpha-2** 

**问题与解决方案**

1.adapter 可以打印 binlog 日志，无法写入 es

> 版本问题导致，使用的是 v1.1.15 ,将版本更换为 v1.1.15-alpha-2

2.adapter 无法启动，无任何错误日志或者提示日志

> adapter 启动脚本默认的 xms = 2048 ,xmx = 3072 ,启动内存过大

**全量同步**

```
curl http://127.0.0.1:8081/etl/es6/product.yml -X POST
```

es6 即 conf 包下的 yml 包名， product 名字与数据库表名对应

**指定同步的表**

> 通过修改 canal/conf/example/instance.properties 配置过滤规则，指定需要监控的表
>
> canal.instance.filter.regex = xxx

| 作用域         | regex                            |
| -------------- | -------------------------------- |
| 全库全表       | .\*\\..*                         |
| 指定数据库全表 | test\..*                         |
| 单表           | test.user (多张表用逗号分割)     |
| 多规则使用     | test\..*,test2.user1,test3.user2 |





##### maxwell

**mysql > maxwell > kafka > logstash > es > kibana** 







分页与深分页的解决方案

### redis

#### 简介

redis 是使用 C 语言编写，遵循 BSD协议，支持网络，可基于内存，分布式，可选持久性的 key-value 存储数据库， value 支持的类型包括

- string
- hash
- list
- set
- sorted set

#### 1.配置

redis 配置文件 redis.config (window 下 redis.window.conf), 启动客户端，通过 **config** 命令查看或者配置修改

**查询指定配置**, 如日志等级

```shell
config get loglevel
```

*注：查询所有配置使用  \**

**配置参数**

```powershell
config set timeout 0
```

**参数说明**

| 配置项                                       | 说明                                                         |
| -------------------------------------------- | ------------------------------------------------------------ |
| daemonize no                                 | 默认情况redis 不在后台运行                                   |
| pidfile /var/run/redis.pid                   | 后台运行时 pid 文件存放位置                                  |
| bind 127.0.0.1                               | 绑定的主机地址                                               |
| port 6379                                    | 监听端口                                                     |
| protected-mode yes                           | 保护模式，默认开启，远程需要访问，需要关闭。或者设置密码     |
| timeout 0                                    | 客户端限制多久关闭连接，0表示关闭改功能                      |
| tcp-keepalive 0                              | 指定 tcp 连接是否为长连接，“侦探”信号由 server 端维护，默认0为关闭 |
| loglevel notice                              | 日志记录级别，支持 debug 、verbose、notice(默认)、warning    |
| logfile ""                                   | 日志记录方式，默认为 stdout 标准输出                         |
| databases 16                                 | 设置数据库数量（0-15），默认使用库为 0                       |
| save 900 1<br/>save 300 10<br/>save 60 10000 | 指定多长时间内，有多少次更新操作，将数据同步到数据文件       |
| rdbcompression yes                           | 指定存储到本地数据库是否压缩数据，默认 yes , 采用 LZF 压缩   |
| rdbchecksum yes                              | 是否对 rdb 文件使用 CRC64校验，默认 yes                      |
| dbfilename dump.rdb                          | 指定本地数据库文件名 ，默认 dump.rdb                         |
| dir ./                                       | 指定本地数据库存放路径                                       |
| requirepass foobared                         | 设置连接密码，通过 auth <password> 提供，默认关闭            |
| maxclients 10000                             | 同一时间最大的连接数，默认 10000, 0表示不限制                |
| maxmemory <bytes>                            | 指定 redis 最大内存限制，在启动时会把数据加载到内存，达到最大内存后，先尝试清除已到期或即将到期的key<br />处理后扔达到最大内存，则无法再进行写入操作，读取正常（新的vm 机制 key 存在内存，value 会存放在 swap 区） |
| appendonly no                                | 指定是否每次更新后进行日志记录，默认情况下通过异步把数据写入磁盘，如果不开启，断电会导致一段时间内的数据<br />丢失。因为 redis 同步数据文件按照 save 条件。默认为关闭 |
| appendfilename   "appendonly.aof"            | 指定更新日志文件名，默认为 appendonly.aof                    |
| appendfsync everysec                         | 指定更新日志条件<br />no : 表示等操作系统进行数据缓存同步到磁盘（快）<br />always : 表示每次更新操作后手动调用 fsync() 将数据写入到磁盘 （慢，安全）<br />everysec : 表示每秒同步一次 (折中，默认值) |
| activerehashing yes                          | 是否激活重置哈希，默认开启                                   |
| include /path/to/local.conf                  | 指定包含其他配置文件                                         |

#### 2.数据类型

**String**

string 是redis 最基本的数据类型 ，最大值能存储 512 M

 get / set 命令获取与存储 string 类型 key-value 数据

**Hash**

hash 类型存储的是 key-value 集合，特别适合对象存储

存储zhangsan 、lisi

```shell
 hmset people name zhangsan age 25
 
 hmset people name lisi age 22  // 覆盖 zhagnsan 25
 
 hmset people name wangwu age 22 address beijing  //报错
```

查询 name

```shell
hget people name 
```

*结果是 lisi ,因为如果 key 存在则会进行更新，如果 存储的field 不存在，则报错*

> del  key  可以删除key
>
> keys *  查询所有的 key

**List**

链表左侧插入与删除

```shell
lpush school zhangsan 
lpop school
```

链表右侧插入与删除

```
rpush school lisi
rpop school
```

链表查看

```shell
lrange school 0 10
```

**Set**

set 集合是 string 类型的无序无重复集合

添加元素

```
sadd school zhangsan lisi
```

查询元素

```
smembers school
```

**zset**

zset 与 set 相同，都是存储 string 无重复元素，但 zset 每个元素都会关联一个 double 类型的分数，redis 通过分数对集合排序（分数可以重复）

添加元素

```
zadd school 1 zhangsan
```

查看元素

```
zrange school 0 10
```

#### 3.命令

| 命令               | 描述                                 |
| ------------------ | ------------------------------------ |
| del  key           | 删除 key                             |
| dump key           | 返回被序列化后的值                   |
| exists key         | 检查 key 是否存在                    |
| expire key seconds | 为指定 key 设置过期时间              |
| move key db        | 将当前库的 key 移动到指定的db        |
| persist key        | 移除 key 的过期时间，将 key 永久保存 |
| pttl key           | 返回 key 的剩余时间（毫秒）          |
| ttl key            | 返回剩余时间（秒）                   |
| randomkey          | 随机返回一个key                      |
| type key           | 返回 key 的类型                      |
| select  0          | 选择第0号数据库                      |

#### 4.发布订阅

发布订阅是一种消息通信模式，发布者发送消息，订阅者接收消息。客户端可以订阅任意数量的频道 **channel**  , 当有消息通过 **publish**

发送给频道 channel 时，消息就会被发送给订阅者

**订阅频道**

```
subscribe school

unsubscribe school //退订
```

**发布信息到频道**

```
publish school “msg”
```

> 发生一点意外：使用 subscribe 命令订阅后闪退 ，经分析发现是之前测试 timeout 将该值设置为 3 ,即 3秒即断开连接，设置为0关闭即可

#### 5.事物

redis 事物一次可以执行多条命令，保证

- 批量操作在 exec 之前是被放在队列缓存
- 收到 exec 开始执行事物，事物中命令执行失败，其余的命令依然继续执行，也不会回滚
- 事物执行过程中，其他客户端提交的命令请求不会插入到事物命令执行序列中

| 命令    | 解释               |
| ------- | ------------------ |
| multi   | 标记事物开始       |
| exec    | 执行事物块内的命令 |
| discard | 取消事物           |

#### 6.脚本

redis 通过 Lua 解释器执行脚本，执行脚本的常用命令是 **eval**

**批量删除 key**

//todo

#### 7.服务器命令

redis 服务器命令用于管理服务器

**获取服务器配置**

```
info
info [参数]  //获取指定配置信息
```

其他命令

| 命令                       | 解释                                                     |
| -------------------------- | -------------------------------------------------------- |
| bgrewriteaof               | 异步执行一个 aof 文件重写操作                            |
| bgsave<br />save           | 在后台异步保存当前数据库的数据到磁盘<br />同步数据到磁盘 |
| client list                | 获取连接到服务器的客户端列表                             |
| cluster clots              | 获取集群节点的映射数组                                   |
| command<br />command count | redis 命令详情<br />命令数量                             |
| time                       | 获取当前服务器时间                                       |
| dbsize                     | 返回当前数据库的 key 数量                                |
| flushall                   | 删除所有数据库的 key                                     |
| lastsave                   | 返回最后一次 redis 成功将数据保存到磁盘的时间            |
| monitor                    | 实时打印服务器收到的命令                                 |
| role                       | 返回主从实例所属的角色                                   |
| sync                       | 用于复制功能(replication)的内部命令                      |

#### 8.stream

redis stream 是 redis 5.0 版本新增加的数据结构，redis 本身的发布订阅无法持久化消息，如果网络断开消息就会丢失。 redis stream 提供

消息持久化和主备复制功能，可以让客户端访问任何时刻的数据，并且记录每个客户端的访问位置，保证消息不丢失。

- consumer group	消费组，可以包含多个消费者  
- last_delivered_id	游标，消费者读取消息都会从游标往前移动
- pending_ids    消费者的状态变量，记录当前已被客户端读取的消息，但是还没有 ack 确认

| 命令   | 操作 | 说明           |
| ------ | ---- | -------------- |
| xadd   |      | 添加消息到尾端 |
| xtrim  |      | 对流进行修剪   |
| xdel   |      | 删除消息       |
| xrange |      | 获取消息列表   |
| ...    |      |                |

#### 9.备份与恢复

redis 使用 **save** 命令用于创建当前数据库的备份，如果需要恢复数据，**将备份文件 dump.rdb 移动到 redis 安装目录**

```
config get dir  // 获取redis 安装目录
```

备份也可以使用 **bgsave** 后台进行备份

#### 10.性能测试

在redis bin 目录下执行命令 **redis-benchmark** 检测性能， 例如同时执行 10000 个请求来检测性能

```shell
redis-benchmark -n 10000  -q
```

| 选项 | 描述              |
| ---- | ----------------- |
| -h   | 指定服务器主机名  |
| -p   | 指定服务器端口    |
| -s   | 指定服务器 socket |
| -c   | 指定并发连接数    |
| -n   | 指定请求数        |
| -q   | 强制退出          |
| ...  | ...               |

#### 11.数据过期淘汰策略

redis 作为缓存数据库，底层数据结构主要是由 **dict** 和 **expires** 两个字典构成。dict 存储健值数据，expires 则保存健值的过期时间。

**内存策略**

redis 通过配置 **maxmemory** 来配置最大容量的阀值，当达到最大容量，就会触发淘汰策略 ，默认使用 **volatile-lru**

- **noeviction**   新写入操作报错
- **allkeys-lru**  移除最近最少使用的 key
- **allkeys-random**  随机移除 key
- **volatile-lru**  在设置了过期健，移除最近最少使用的key
- **volatile-random**   在设置了过期健的空间中，随机移除某个 key
- **volatile-ttl**   在设置了过期健的空间中，有更早过期时间的 key 优先移除

**确定移除的 key**

当存储数量量足够大，如何高效确定被移除的 key ？ redis 引入配置 **maxmemory-samples**, 称为过期检测样本，默认值 5 ，

当使用内存超过最大允许内存，会触发 **freeMemoryIfNeeded**函数清理内存，清理策略不是针对所有的 key ,而是内存中样本

个健作为样本池进行抽样清理

#### 12.热键与大健

**如何保证 redis 存储的都是 hot key**

> 限定 redis 占用最大内存， redis 会根据淘汰策略，留下热数据到内存。预估需要热数据的内存，将淘汰策略修改为
>
> **volatile-lru** 或者 **allkeys-lru** , 修改 maxmemory 配置

**如何发现 hot key**

1. 凭借行业经验，进行预估热 key ，缺点是不全面
2. 在客户端收集 ，缺点是对代码进行侵入
3. redis 自带命令统计

**如何解决热点key**

1. 利用二级缓存
2. 备份热key

**big key**

由于 redis 使用单线程运行， 操作 big  value 影响很大，解决办法

> 拆分大对象，分成多个 key -value 存储

不建议存储big key ，会造成很多问题

#### 13.缓存穿透、缓存雪崩

**缓存穿透**

一般的系统都是通过 key 查询 value , 如果不存在则去查询 db . 如果 **key 对应的 value 一定不存在**，并且该 key 并发请求量很大，对后端系统造成很大的压力，

称为缓存穿透

**解决办法**

1. 对查询为空的情况也进行缓存，并设置超时时间
2. 对一定不存在的 key 进行过滤，采用 bitmap 存储一定存在的key （也叫布隆过滤器）

**缓存雪崩**

当缓存服务器重启或者集体失效，所有的请求都会请求 db ，对数据库造成很大的压力

**解决办法**

1. 通过加锁或者队列控制访问数量
2. 不同的 key 设置不同的过期时间，避免同时失效
3. 设置二级拷贝缓存，当一级缓存失效，可以临时使用二级缓存

#### 14.持久化

##### **rdb 持久化**

rdb 持久化通过配置文件指定持久化的条件，如

```shell
save 900 1
save 300 10
save 60 10000
```

含义是 每 900秒有一个变化 **或**者 每300秒有10个变化 **或** 每60秒有10000个变化，系统就会 fork 命令，调用 bgsave 命令后台生成快照

**快照文件特点**

不是可读的文件，打开会乱码不可读。缺点是**当前执行快照保存与上一次执行快照保存之间的数据可能会丢失。**

快照生成的文件名是 **dump.rdb**

> 经测试发现，redis 在 window 下默认加载的配置文件是 redis.windows-service.conf , 而不是redis.windows.conf

| 配置                                         | 说明                               |
| -------------------------------------------- | ---------------------------------- |
| save 900 1<br/>save 300 10<br/>save 60 10000 | rdb 执行条件                       |
| stop-writes-on-bgsave-error yes              | /                                  |
| rdbcompression yes                           | 存储到磁盘的快照，是否进行压缩存储 |
| rdbchecksum yes                              | 是否进行 crc64 校验                |
| dbfilename dump.rdb                          | 快照文件名                         |
| dir ./                                       | 快照文件路径                       |

##### **aof 持久化**

aof  持久化是将操作日志写到磁盘，通过调用 fsync 将数据写入磁盘，通过 配置 **appendfsync** 选项控制

- appendfsync no

  > redis 不会主动调用 fsync 将 aof 日志写到磁盘，依赖系统调用 （linux 是每30秒一次 fsync）

- appendfsync everysec

  > 每秒进行一次 fsync

- appendfsync always

  > 每一次写操作都会进行 fsync

| 配置                                                         | 说明                                                         |
| ------------------------------------------------------------ | ------------------------------------------------------------ |
| appendonly  no                                               | 是否开启 aof  ， 默认关闭                                    |
| appendfilename "appendonly.aof"                              | aof 文件名称                                                 |
| appendfsync always<br/>appendfsync everysec<br/>appendfsync no | always 每次修改<br />everysec 每秒钟修改<br />no 不修改      |
| no-appendfsync-on-rewrite no                                 | 重启 aof 文件过程中，是否禁止 fsync                          |
| auto-aof-rewrite-percentage 100<br/>auto-aof-rewrite-min-size 64mb | 指定重写 aof 文件的条件<br />指定触发重写操作aof文件的大小，默认 64M |
| aof-load-truncated yes                                       | 当 aof 文件损坏，redis 启动是否加载 aof                      |

*注：系统默认使用 rdb 持久化， aof 默认是关闭的*

**数据恢复**

在进行 rdb 持久化后，在 redis 配置的 data 路径下生产 dumb.rdb ，如果需要恢复数据，只需要将该文件放置在 data 指定的路径下即可

aof 记录文件 appendonly.aof 也是同理

#### 15.集群

**主从复制**

- 一个主节点，多个从节点，从节点 通过 sync 同步数据
- 主节点通过 bgsave 生产快照，以及缓冲区数据同步到从节点
- 初始化同步完成后，主节点写命令都会同步到从节点执行

优缺点

- 主从复制可以进行读写分离，降低主节点的压力，但所有的写都必须是主节点完成。
- 不具备自动容错与恢复功能，容量上限难以扩容

**哨兵模式**

- redus 2.8 提供哨兵，自动监控系统并实现故障恢复，主节点故障，将从节点切换为主节点
- 哨兵模式是改进版主从模式，确定是难以在线扩容，集群容量达到上限变得复杂

**Redis-Cluster 集群**

- 哨兵模式实现高可用、读写分离，但容量存储有上限，redis 3.0 加入 cluster 模式，分布式存储
- 分布式存储无中心结构，每个节点都有一个 slot 值，通过哈希槽对范围值（0~16383）取余，得到存储的节点
- 为保证高可用，集群引入主从模式，每个节点对应多个从节点，主节点宕机，启动从节点

**总结**

主从复制： 读写分离，**不满足高可用，容量有上限**

哨兵模式：读写分离，高可用，**不满足容量上限**

集群：高可用，容量可扩容，读写分离

#### 16.分布式锁

在并发场景中，为了保证多台服务器在执行某一段代码时只有一台服务器执行，采用分布式锁可以实现这种场景，一般有如下几种

- 使用 mysql 唯一索引
- 使用 zookeeper ，利用临时**有序**节点
- 使用 redis ，基于 setnx

redis 是单进程单线程模式，采用队列模式将并发变为串行访问，可以解决分布式一致性问题，redis 分布式锁实现

**上锁**

```java
    /**
     * 尝试获取分布式锁
     * @param jedis
     * @param key
     * @param value
     * @param expireTime
     * @return
     */
    public static boolean tryGetDistributedLock(Jedis jedis,String key,String value,int expireTime){
        SetParams setParams = new SetParams();
        setParams.nx();
        setParams.ex(expireTime);
        String result = jedis.set(key,value,setParams);
        if(LOCK_SUCCESS.equals(result)){
            return true;
        }else {
            return false;
        }
    }
```

**释放锁**

```java
/**
     * 释放分布式锁
     * @param jedis
     * @param key
     * @param value
     * @return
     */
    public static boolean releaseDistributedLock(Jedis jedis,String key,String value){
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        Object result = jedis.eval(script, Collections.singletonList(key),Collections.singletonList(value));
        if(RELEASE_SUCCESS.equals(result)){
            return true;
        }else{
            return false;
        }
    }
```

**redis 分布式锁出现死锁？**

> 当客户端成功上锁后出现服务奔溃，而只有当前客户端可以释放锁，所以要加上超时时间，防止死锁

**超时时间的设置**

> 一般根据业务经验设置超时时间，也可以通过 Redission 提供的思路，采用 watchDog 机制实现锁的续期，当加锁成功后，同时开启守护线程，持续的
>
> 给锁延长过期时间，只要持有锁的客户端没有宕机，可以一直持续拥有锁，直到业务执行完毕

**redis 分布式锁如何实现为可重入锁?**

> 当前redis 分布式锁只能上一次锁，如何实现为可重入锁？即当前客户端上锁后，可以继续上锁。
>
> 在 Redission 实现的思路是使用 Redis 哈希表存储可重入次数，当加锁成功后，使用 hset 命令，value 重入次数为 1。
>
> 当同一个客户端再次加锁成功，则使用 hincrby 自增加 1 ，释放锁可判断可重入次数是否大于 0 ，大于则减一，否则删除键值，释放锁

#### 17.优化

redis 所有的数据都在内存中，内存的资源是非常宝贵的，常用的优化方向

- 配置优化
- 



### mongodb

#### 1.mongodb 是什么

> mongodb 是基于分布式文件存储的数据库，由 **c++** 编写，目的是针对当前 web 应用产生的大量数据，不适用传统的关系型数据库存储
>
> mongodb 是 NoSql 的一种，采用 mongodb 可以方便对数据进行挖掘

关系型数据库与 NoSql 的区别在与 关系型数据库遵循 ACID ,数据存储在表中，高度结构化， NoSql 遵循 CAP 定理，存储数据灵活

#### 2.CAP  布鲁尔定理

cap 定义，被称作 布鲁尔定理，指出对于分布式系统，不可能同事满足以下三点

> - **一致性 consistency**  所有的节点具有相同的数据
> - **可用性 availability**  每个请求成功或失败都有响应
> - **分区容错性 partition tolerance**  系统信息的丢失不影响系统的运作

因为不能同事满足三点，因此将 Nosql 数据库分为 CA、CP、AP

- CA  满足一致性，扩展较弱 
- CP  满足一致性，分区容错性，但性能不强
- AP  满足可用性与分区容错，用于对一致性要求不强的场景

#### 3.NoSql 分类

| 存储类型       | 代表数据库 | 特性                                         |
| -------------- | ---------- | -------------------------------------------- |
| 列存储         | Hbase      | 方便存储结构化与半结构化数据，方便做数据压缩 |
| 文档存储       | Mongodb    | 一般存储 json 格式                           |
| key-value 存储 | Redis      | 通过 key 可以快速查询到value                 |

#### 4.操作mongo

| 关系数据库  | mongo       | 说明               |
| ----------- | ----------- | ------------------ |
| database    | database    | 数据库             |
| table       | collection  | 数据库表/集合      |
| row         | document    | 数据行/文档        |
| column      | field       | 数据字段/域        |
| index       | index       | 索引               |
| join        |             | 表连接/不支持      |
| primary-key | primary-key | 主键/自动_id为主键 |

**show dbs** 显示所有数据库

**db** 查询当前数据库

**use bai** 切换到 bai 数据库，如果 bai 不存在则创建

> 当数据库中无数据时，show dbs 不显示该数据库

向集合 abc 插入数据 (集合不存在即创建)

```sql
db.abc.insert({"name":"zhangsan"})
```

删除当前数据库

```sql
db.dropDatabase()
```

查询当前数据库的集合

```sql
show tables
show collections
```

创建新的集合 dog

```sql
db.createCollection("dog")
```

创建带有参数选项的集合 dog

```sql
db.createCollection("dog",{ capped:true, autoIndexId:true, size:6142800, max:10000})
```

| 字段        | 类型 | 说明                                                         |
| ----------- | ---- | ------------------------------------------------------------ |
| capped      | 布尔 | true 表示创建固定大小的集合，达到最大时会自动覆盖最早的文档，且必须指定 size |
| autoIndexId | 布尔 | 3.2 不再支持，默认为false , 如果为 true ，则自动在 _id 创建索引 |
| size        | 数值 | 集合的大小                                                   |
| max         | 数值 | 指定固定集合包含文档的最大数量（**超过改数值则删除最早文档**） |

删除 dog 集合

```
db.dog.drop()
```

**插入文档**

存储在集合中文档的数据是 BSON 格式，类似于 JSON 的二进制存储格式，Binary JSON 的简称

```
db.dog.insert({"age":"10"})
db.dog.save({"age":"10","name":"zhang"})
```

- save() 如果 _id 主键存在则更新数据，不存在就插入数据，新版本**已废弃**，增加 insertOne() / replaceOne() 代替
- insert() 如果插入的 _id 已存在，则抛出 **DuplicateKeyException** 异常

3.2 版本后增加 insertOne() 和 insertMany()

```sql
 db.dog.insertMany([{"name":"lisi"},{"name":"zhangsan"}])
```

批量查询文档

```java
for(var i=1;i<100;i++){
	db.abc.insert({name:"jack",age:i})
}
```

**查询文档**

```sql
db.dog.find()
db.dog.find().pretty()  格式化显示
db.dog.findOne() 查询一条
```

查询可以带有条件（year 小于等于 100）

```sql
db.abc.find({"year":{$lte:100}}).pretty()
```

| 操作     | 操作                                     |
| -------- | ---------------------------------------- |
| 等于     | db.abc.find( {"year" : 100 } )           |
| 小于     | db.abc.find( {"year" : { $lt : 100} } )  |
| 大于     | db.abc.find( {"year" : { $gt : 100} } )  |
| 大于等于 | db.abc.find( {"year" : { $gte : 100} } ) |
| 小于等于 | db.abc.find( {"year" : { $lte : 100} } ) |
| 不等于   | db.abc.find( {"year" : { $ne: 100} } )   |

**使用联合比较**  *where  year > 100 and year < 200*

```sql
db.abc.find({"year":{$lt:200,$gt:100}})
```

或者写为

```sql
 db.abc.find({"year":{$lt:200},"year":{$gt:100}})
```

**and**  *where age = 25 and year = 100*

```sql
 db.abc.find({"age":"25","year":{$ne:100}})
```

**or** *where age = 20 or year = 100*

```sql
 db.abc.find({ $or:[{"age":"20"},{"year":100}] })
```

**and 与 or 联合使用**  *where  age = 25 and ( year = 100 or year = 150 )*

```sql
 db.abc.find({"age":"25",$or:[{"year":100},{"year":150}] })
```

**更新文档**

```sql
 db.abc.update( {"age":"21"}, {$set:{"age":"25"}} )
```

如果存在多条匹配的记录，需要设置 **multi : true**

```sql
db.abc.update( {"age":"25"},{$set:{"age":26}},{multi:true} )
```

**删除文档**

```sql
 db.abc.remove({"age":26})
```

justOne 是否只移除一条，默认false 即全部

```sql
db.abc.remove({"age":"25"},{"justOne":true})
```

**$type 根据类型查询**

```sql
 db.abc.find({"year":{$type:1}})
```

| 类型   | 数字 |
| ------ | ---- |
| Double | 1    |
| String | 2    |
| Object | 3    |
| ...    |      |

**limit 与 skip**

limit() 指定查询的记录数 ， skip() 跳过的记录数，均接受一个数值参数

```sql
 db.abc.find().skip(1).limit(1)
```

**sort 排序**

可以对查询结果排序，1 为升序 ， -1 为降序 

```sql
db.abc.find().sort({"year":-1})
```

当 year 相同的情况下，按 age 升序

```sql
 db.abc.find().sort({"year":-1,"age":1})
```

*注意：如果一个字段存在不同的类型的数据，则在同类型内部排序*

#### 5.索引

mongo 创建索引可以提高检索效率，对指定列创建索引，可以选择升序 1 降序 -1

```sql
 db.abc.createIndex({"age":1})
```

也可以创建多列索引，类似关系数据库的组合索引

```sql
db.abc.createIndex({"age":1,"year":-1})
```

createIndex 可以接受参数，指定索引属性

| 参数       | 类型   | 描述                                 |
| ---------- | ------ | ------------------------------------ |
| background | 布尔   | 是否在后台创建索引，默认 false       |
| unique     | 布尔   | true 即创建唯一索引，默认false       |
| name       | string | 索引的名称，未指定则系统生成一个名称 |
| ...        | ...    | ...                                  |

创建所有，指定索引参数

```sql
db.abc.createIndex( {"age":1,"year":-1} , {background:false,unique:false,name:"myIndex"} )
```

**删除索引**

删除集合下所有的索引

```
db.abc.dropIndexes()
```

指定 name 删除索引

```sql
db.abc.dropIndex("myIndex")
```

*注：mongo 为 id 设置索引，不可被删除*

**查询集合索引**

```sql
db.abc.getIndexes()
```

**覆盖索引**

mongodb 如果查询条件在一个索引中，则会走索引查询，使用覆盖索引即创建联合索引

#### 6.聚合

聚合类比关系数据库 count 操作， mongo 通过 aggregate（） 实现聚合查询

*select  age, count(id)  from  abc group by age*

```sql
db.abc.aggregate([ {$group:{_id:"$age",year_cnt:{$sum:1}}}])
```

聚合表达式

| 表达式 | 描述     | 案例                                                         |
| ------ | -------- | ------------------------------------------------------------ |
| $sum   | 计算总和 | db.abc.aggregate([{$group:{"_id":"$year",total:{"$sum":"$year"}}}]) |
| $avg   | 求平均值 | db.abc.aggregate([{$group:{"_id":"avg",avg_year:{"$avg":"$year"}}}]) |
| $min   | 最小值   | db.abc.aggregate([{$group:{"_id":"min",min_year:{"$min":"$year"}}}]) |
| $max   | 最大值   | db.abc.aggregate([{$group:{"_id":"max",max_year:{"$max":"$year"}}}]) |
| ...    |          |                                                              |

**管道**

管道可以修改文档结构

| 操作符   | 案例                                              | 说明             |
| -------- | ------------------------------------------------- | ---------------- |
| $project | db.abc.aggregate({$project:{year:1,age:1,_id:0}}) | 可以修改文档结构 |
| $mtch    | db.abc.aggregate({$project:{year:1,age:1,_id:0}}) | 过滤数据         |
| $limit   | db.abc.aggregate({$limit:3})                      | 前n条数据        |
| $skip    | db.abc.aggregate({$skip:3})                       | 跳过n条数据      |
| ...      | ...                                               | ..               |

#### 7.副本集 & 分片

**副本集**

mongodb 副本集是将数据同步在多个服务器，提供数据的可用性，安全性，故障恢复

常见的搭配方式： 一主一从、一主多从

> 主节点记录在其上的所有操作oplog ,从节点定期轮询主节点同步操作，保证主从节点数据一致

**分片**

分片是另一种集群方式，当存储的数据急剧增多时，一台机器不能满足读写通途，可以通过多台机器上分割数据，使得数据库能够存储更多的数据

分片组成： **shard 分片、路由、config server 配置**服务组成

#### 8.监控

mongodb 提供 **mongostat** 和 **mongotop** 监控数据

#### 9.文档关系

Mongodb 关系表示多个文档之间逻辑上的关系，通过 **嵌入**与**引用** 关联

关系可以是

- 1:1
- 1：N
- N：1
- N：N

嵌入关系的结构会导致数据不断变大，影响读写性能

**引用关系**

通过文档 id 建立关系，需要经过两次查询。引用分为两种

1. 手动引用
2. DBRefs

**使用 DBRefs**

创建部门信息

```sql
 db.dept.insert({"dept_name":"研发部"})
 
 db.dept.find() //查询dept id
```

创建员工

```sql
 db.emp.insert({"name":"张三","dept":{"$ref":"dept","$db":"bai","$id":"60cff42564df3a39fa533aa1"}})
```

到此员工-部门信息已经创建完成并进行了 DBRefs 关联，通过员工查询部门信息

```sql
var user = db.emp.findOne()

var dbRef = user.dept

db[dbRef.$ref].findOne()
```

**查询分析**

explain() ，分析查询是否使用索引

```sql
 db.abc.find({"age":"25","year":100}).explain()
```

hint() ,指定查询走的索引

#### 10.事物

mongo 4.0 之前是不支持事物的，4.0 更新的重大地方就是支持事物

#### 11.ObjectId

mongo 存储文档必需有 _id 字段，可以是任意类型。默认使用 ObjectId ，是12字节的 BSON 数据

- 前4字节表示时间戳
- 3字节是机器标识码
- 2个字节由进程 id 组成
- 最后3个字节是随机数

可以通过 ObjectId 获取时间戳

```sql
ObjectId("60cc6a106ac68a7106def45b").getTimestamp()
```

### jvm

#### 1.内存布局







**虚拟机**

目前使用的虚拟机是 HotSpot 虚拟机，除此之外，还知道哪些虚拟机？

> sun Classic , JRockit ， J9 ，Liquid VM 等许多其他虚拟机



**jdk 版本与默认的收集器**

| 版本   | 默认收集器                       |
| ------ | -------------------------------- |
| jdk1.7 | Parallel Scavenge + Parallel Old |
| jdk1.8 | Parallel Scavenge + Parallel Old |
| jdk1.9 | G1                               |

命令 `java -XX:+PrintCommandLineFlags  -version`   查看jvm 参数

命令 `java -XX:PrintGCDetails -version`  查看GC 详情

**对象**

在 hotSpot 虚拟机中，对象在内存的布局可以分为三个部分： **对象头(MarkWord)、实例数据、对齐填充(Padding)**

对象头 MarkWord 存储的信息包括

| 存储内容                 | 状态    |
| ------------------------ | ------- |
| 对象哈希码、对象分代年龄 |         |
| 指向锁记录的指针         |         |
| 指向重量级锁的指针       |         |
| 空，不需要记录的信息     | gc 标记 |
| 偏向线程id ，偏向时间戳  |         |





1.如何查看jvm gc 回收情况

通过命令 jstat -gcutil  pid  ms

   S0         S1       E         O        M      CCS      YGC   YGCT  FGC  FGCT    GCT
  0.00  93.77  73.14  16.78  94.26  91.84     15    0.104     3    0.164    0.268
  0.00  93.77  73.14  16.78  94.26  91.84     15    0.104     3    0.164    0.268

解析：s0 、s1 代表新生代 Survivor space 内存使用百分比 。 

E 代表新生代 Eden 区使用百分比

O 老年队空间使用百分比

M 元空间使用百分比

YGC 从程序启动到当前，发生的 Yang GC 次数 （YGCT 为所用时间）

FGC 从程序启动到当前，发送 Full GC 次数 (FGCT 为所用时间)

GCT 垃圾回收总耗时

2.项目中进行过哪些jvm调优

3.如何查看 jvm 哪些对象占用内存



3.new Object() 内存分析

**Object obj = new Object() ,内存分布情况，占用了多少字节** 

对象分为三部分：对象头（MarkWord、ClassPointer）、实例数据、Pading 对齐填充

占用内存  MarkWord(8字节) ，ClassPointer(4字节) ,实例数据为空，为满足 8的倍数， Padding 占用 4 字节。 共16字节

obj 在栈区种分配了一个对象的引用，创建的实例对象在堆中，obj 引用指向了创建的对象



类加载器

虚拟机栈、本地方法栈、程序计数器、**方法区、堆**

垃圾回收算法：引用技术、可达性分析、**标记清除、标记整理、复制（年轻代）**

> 可达性分析 哪些对象可以作为 GC root ？

- 虚拟机栈中引用的对象
- 方法区中类静态属性引用的对象
- 方法区中常量引用的对象
- 本地方法栈中 native 方法引用的对象

> Jvm 系统调优 ? 参数类型？

参数类型：标配类型 、 x 参数 、xx 参数

```
#查看Java 进程号
jps -l   

#查看是否开启 GC 参数
jinfo -flag PrintGCDetails 进程号  

#开启 GC 参数
PrintGCDetails 布尔参数，通过启动配置开启  -XX:+PrintGCDetails

#查看元空间大小
jinfo -flag MetaspaceSize 进程号

#启动配置 元空间参数
-XX:MetaspaceSize=128m

#查询所有配置
jinfo -flags 进程号

#打印 Jvm 初始化参数
java -XX:+PrintFlagsInitial

#idea jvm配置
-XX:+PrintGCDetails -XX:MetaspaceSize=128m -Xms256m -Xmx2048m

#查询堆栈信息
# jstack -l 进程号
```

-xms  等价于 -XX:InitialHeapSize

-xmx 等价于 -XX:MaxHeapSize

最大堆内存默认为物理内存的 1/4 ，默认GC 回收器 ParallelGC

> 项目中常用的 jvm 配置参数有哪些？

-Xms 初始化堆内存 

-Xmx 最大堆内存

-Xss  栈空间的最大内存（影响多线程数量）

-Xmn 新生代大小

-MetaspaceSize 设置元空间

**强软弱虚四大引用**

强引用：不能够 GC 回收

软引用：内存不足回收

弱引用：只要有 GC 都会被回收（Thread ->ThreadLocalMap->Node 后用到弱引用）

虚引用：监控对象的回收清空（如对象被回收会受到通知）

> ***如何查看服务器默认的垃圾回收器件？***

java  -XX:+PrintCommandLineFlags -version ， 可以查看默认的垃圾回收器（jdk8 默认的垃圾回收器 ParallelGC）

> ***jdk 有哪些GC ？***

SerialGC ,ParallelGC , CMS , G1  ,ParallelNewGC ,ParallelOldGC

可以在服务启动的时候指定 GC ，默认使用 **UseParallelGC**

> ***上述 GC 分别用来那个区？***

老年区： CMS （并发标记清除）, ParallelOld ，ParallelGC ，SerialOld

新生区：SerialGC , ParallelNew 

**G1 可以同时在新生代与老年代(jdk1.9 开始默认的收集器)**

**ParallelNew + SerialOld(jdk1.6默认的收集器)**

**ParallelNew + ParallelOld(jdk1.8 默认)**

**CMS (并发标记清除)，最大限度较低 gc 停顿**



### **算法**

#### 1.求输入字符串的最长回文子串长度

> 例如 moon , level 等为回文串，长度为 4，5
>
> 例如 moonn 最长回文子串 oo/ nn ,长度为 2

```java
 public static int getMaxLength(String str){
        int maxLength = 0;
        byte[] strArray = str.toLowerCase().getBytes(StandardCharsets.UTF_8);
        for(int i = 0; i<strArray.length; i++){
            for(int j = i;j<strArray.length;j++){
                //判断是否回文串 ，是获取长度，不是continue
                if(isPalindromicString(strArray,i,j)){
                    int length = j -i +1;
                    maxLength = Math.max(maxLength, length);
                }
            }
        }
        return maxLength;
    }

    private static boolean isPalindromicString(byte[] strArray,int left,int right){
        for(;left<right&&strArray[left]==strArray[right];left++,right--);
        return left >= right;
    }
```



zhaoyingkeji 

#### **2.[1、2、3、4] 四个数字可以组成多少个不同的四位数？**

> 若不能重复： 4\*3\*2*1 = 24种
>
> 若数字可以重复：4\*4\*4\*4 = 256 种

#### **3.输入年、月、日，判断是该年的第几天？**

```java
 public static int getDayNum(){
        int dayNum = 0;
        Scanner scanner = new Scanner(System.in);
        System.out.println("请输入年：");
        int year = scanner.nextInt();
        System.out.println("请输入月：");
        int month = scanner.nextInt();
        System.out.println("请输入日：");
        int day = scanner.nextInt();
        //todo year month day 合法性校验
        switch (month){
            case 12:{
                dayNum+=31;
            }
            case 11:{
                dayNum+=30;
            }
            case 10:{
                dayNum+=31;
            }
            case 9:{
                dayNum+=30;
            }
            case 8:{
                dayNum+=31;
            }
            case 7:{
                dayNum+=31;
            }
            case 6:{
                dayNum+=30;
            }
            case 5:{
                dayNum+=31;
            }
            case 4:{
                dayNum+=30;
            }
            case 3:{
                dayNum+=31;
            }
            case 2:{
                if( ( year%4==0  && year%100!=0) || year%400==0 ){
                    dayNum+=29;
                }else{
                    dayNum+=28;
                }

            }
            case 1:{
                dayNum+=31;
            }
            default:{

            }
        }
        return dayNum;
    }
```

#### **4.判断 s1 是否包含 s2 ?**

```java
public static int checkSubStr(String s1,String s2){
        return s1.indexOf(s2);
}
```

```java
 public static boolean checkSubStr2(String s1,String s2){
        return s1.contains(s2);
}
```

**子类继承父类，构造方法，代码块，静态代码块执行顺序**

> 父类静态代码块 > 子类静态代码块 > 父类代码块 > 父类构造方法 > 子类代码块> 子类构造方法

**Integer.valueOf() 采用什么模式？** **Reader Writer 采用什么设计模式？**

> Integer 采用享元模式 ，IO 流采用装饰者模式

#### **5.求2个数的最大公约数与最小公倍数**

```java
    /**
     * 辗转相除求最大公约数
     * @param n
     * @param m
     * @return
     */
    public static int getMaxCommonNum(int x,int y){
        int n = y;
        while( x%y != 0 ){
            n = x%y;
            x = y;
            y = n;
        }
        return n;
    }
```

x * y = 最大公约数 * 最小公倍数

#### **6.手写快速排序**

```java
public static void quickSort(int[] data,int left,int right){
        if(left<right){
            int i=left,j =right,x =data[i];
            while (i<j){
                while (j >i && data[j] >=x){
                    j--;
                }
                if(i<j){
                    data[i++] = data[j];
                }
                while (i<j && data[i]<x){
                    i++;
                }
                if(i<j){
                    data[j--] = data[i];
                }
            }
            data[i] = x;
            quickSort(data,left,i-1);
            quickSort(data,i+1,right);
        }
    }
```

#### 7.求字符串数组最长公共子串

{"javabean","javascript","javalanguage","hellojava"}

### mysql 

#### 1.命令操作

| 命令                                                         | 说明               |
| ------------------------------------------------------------ | ------------------ |
| show databases                                               | 查询所有数据库     |
| select version()                                             | 查询当前数据库版本 |
| create database test                                         | 创建 test 数据库   |
| create table `t_user`(<br />`id` bigint(20),`name` varchar(20)<br />); | 创建表 t_user      |
|                                                              |                    |
|                                                              |                    |
|                                                              |                    |

**创建数据库**

```sql

```

**创建表**

```

```



#### 2.日志

window 下打开与关闭mysql 服务

```
net stop mysql
net start mysql
```

**binlog**

binlog 三种保存模式 

- row
- statement
- mixed

binlog、redolog 、undolog 区别,作用

#### 3.删除操作

- drop
- truncate
- delete

> drop 删除表结构，数据
>
> truncate table 删除表全部数据，计数器会复位, 等价于不带条件的where 
>
> delete 删除表数据



#### 4.mysql 8.x

**启动问题**

启动报错：/tmp/mysql.sock(111) ,可能问题是 /etc/my.cnf 

```shell
[client]
port = 3306
socket=/var/lib/mysql/mysql.sock

[mysqld]
#bind-address = 0.0.0.0
#port=3306
basedir=/usr/local/mysql
datadir=/usr/local/mysql/data
#socket=/var/lib/mysql/mysql.sock
socket=/var/lib/mysql/mysql.sock
# Disabling symbolic-links is recommended to prevent assorted security risks
symbolic-links=0
# Settings user and group are ignored when systemd is used.
# If you need to run mysqld under a different user or group,
# customize your systemd unit file for mariadb according to the
# instructions in http://fedoraproject.org/wiki/Systemd
#skip-grant-tables
character-set-server=utf8
[mysqld_safe]
log-error=/var/log/mariadb/mariadb.log
pid-file=/var/run/mariadb/mariadb.pid

#
# include all files from the config directory
#
!includedir /etc/my.cnf.d
```

**忘记密码**

在 my.cnf 增加配置 `skip-grant-tables` ，重启 mysql 服务

```sql
mysql -u root 
```

**重置密码**

```sql
alter user user() identified by "123456";
```

**授权远程登陆**

```sql
update user set host='%' where user='root';
```

参考：https://www.cnblogs.com/brady-wang/p/11561300.html

**修改密码加密方式**

```sql
ALTER USER 'root'@'%' IDENTIFIED WITH mysql_native_password BY '123456';
```

### mysql binlog处理

#### Canal

#### MaxWell

#### mysql_stream

### sql 查询

有如下表结构

| uid  | create_time | msg  |
| :--: | :---------: | :--: |
|      |             |      |

#### 1.查询每个用户的消息数量

```sql
SELECT
	uid,( count( uid ) ) 
FROM
	t_msg 
GROUP BY
	uid
```

#### 2.查询每个用户的最新消息

```sql
SELECT
	a.* 
FROM
	t_msg a,
	( SELECT uid, max( create_time ) AS create_time FROM t_msg GROUP BY uid ) b 
WHERE
	a.uid = b.uid 
	AND a.create_time = b.create_time
```

(该题目实现组内排序)，以下为令一种实现

```sql
SELECT
	uid,create_time,msg,new_rank 
FROM
	(
SELECT uid,create_time,msg,
IF(@tmp = uid, @rank := @rank + 1, @rank := 1 ) AS new_rank,
	@tmp := uid AS tmp 
FROM
	t_msg 
ORDER BY uid,create_time DESC) t 
WHERE
	new_rank <=1
```

| uid  | username | grade 分数 | dept 部门 |
| ---- | -------- | ---------- | --------- |
|      |          |            |           |

#### 3.查询每个部门分数的前3名

```sql
select 
id,grade,dept,new_rank
from(
SELECT
	id,
	grade,
	dept,
  if(@tmp = dept, @rank := @rank + 1, @rank := 1 ) AS new_rank,
	@tmp := dept as tmp 
FROM
	student
order by dept,grade desc

) b 
where new_rank <=3
```

| id   | msg  | create_time |
| ---- | ---- | ----------- |
|      |      |             |

#### 4.查询近七日的消息

```sql
SELECT
	* 
FROM
	t_msg 
WHERE
	to_days( CURRENT_DATE ) - to_days( create_time ) < 7
```

#### 5.查询日期x之前的消息数量

*(当前 2021-5-21 00:00:00）*

```sql
SELECT
	* 
FROM
	t_msg 
WHERE
	create_time < '2021-5-21 00:00:00'
```

*注意：显示的判断日期需要加单引号*

### 设计模式

#### 六大设计原则

- 单一职责原则	
  - 类或接口只负责一件事，降低复杂性
- 理氏替换原则
  - 父类出现的地方，可以使用子类替换
- 依赖倒置原则
  - 高层模块不依赖底层模块，抽象不依赖细节
- 接口隔离原则
  - 接口要高内聚，定制服务，尽量小
- 第米尔特法则
  - 也叫最少知道原则
- 开放封闭原则
  - 对扩展开放，对修改关闭



#### 1.单例模式







### 多线程

#### 1.实现三个线程顺序输出 a , b ,c，循环 5次 

```java
    public static Lock lock = new ReentrantLock();
    public static volatile int state = 0;  //通过 state 确定打印哪个线程

    public static class PrintA extends Thread{
        @Override
        public void run() {
            for(int i=0;i<5;){
                try{
                    lock.lock();
                    while(state%3==0){  //使用 while 而不是 if ,防止虚假换新
                        System.out.println("a");
                        state++;
                        i++;
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    lock.unlock();
                }
            }
        }
    }

    public static class PrintB extends Thread{
        @Override
        public void run() {
            for(int i=0;i<5;){
                try{
                    lock.lock();
                    while(state%3==1){
                        System.out.println("b");
                        state++;
                        i++;
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    lock.unlock();
                }

            }
        }
    }

    public static class PrintC extends Thread{
        @Override
        public void run() {
            for(int i=0;i<5;){
                try{
                    lock.lock();
                    while(state%3==2){
                        System.out.println("c");
                        state++;
                        i++;
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    lock.unlock();
                }

            }
        }
    }

    public static void main(String[] args) {
        new PrintA().start();
        new PrintB().start();
        new PrintC().start();
    }
```

### 遇到的问题

**1.数据库数据使用 es 作为搜索引擎查询时的数据同步问题**

> 项目中数据存储在 mysql 中 ， web 页面有搜索框需要检索数据 ，如果数据量不断增加，那么使用 es 作为搜索引擎进行检索，如何保证数据是同步的？
>
> 解决方案：使用 alibaba 开源中间件 canal 进行数据同步，通过 简体 mysql Binlog ，实时同步数据。
>
> 问题1 遇到问题包括数据监听正常但是无法写入 es ，经过排查，是 canal 版本问题，通过修改版本更新。
>
> 问题2 canal 无法启动，无异常日志，经过研究，数据内存不足导致，canal 在 64位系统下堆内存初始化 2g ,通过修改启动脚本解决











### 事物

事物，必须满足ACID (原子性、一致性、持久性、隔离性) ，保证事件的完整性。

**事物在并发中的问题**

- 脏读  `（事物A 读取到事物B 未提交的数据，事物B回滚，A读取到脏数据）`

- 不可重复读（`事物A多次读取数据，事物B在事物A 读取的过程中更新了数据，导致事物A多次读取的数据不一致）`

- 幻读 `（事物A更新数据，事物B在事物A期间新增了数据，事物A结束后发现仍有数据未更新）`

#### 数据库事务

mysql 提供的事物隔离级别

- 读未提交  read-uncommited（最低的隔离级别，脏读、不可重复、幻读都会产生）
- 读已提交 read-committed  (只能读取已提交事物的数据，解决脏读，仍会产生不可重复与幻读)
- 可重复读 repeatable-read（innodb 默认级别，事物从开始到结束读取的数据都是一致的，消除了脏读、不可重复读，仍存在幻读）
- 串行化  serializable（每次读取都要获取表级别锁，串行化读取，读写互相阻塞）

**mysql 查询当前事物隔离级别**

```sql
select @@global.transaction_isolation;
```

默认的隔离级别为 Repeatable-Read

**mysql 修改事物隔离级别**

```sql
set @@global.transaction_isolation = 'read-uncommitted';
```

**mysql 事物操作**

- begin   开启事物
- rollback  回滚
- commit  提交事物

**隔离级别： 读未提交测试**

A 开始事物修改数据，B 事物立刻可以获取A未提交的数据（脏读）

**隔离级别：读已提交**

事物A不停读取数据，事物B对数据进行修改，并提交。事物A多次读取数据不一致

**隔离级别：可重复读**

事物A 不停读取数据，事物B 对数据进行修改，并提交。事物A 读取的数据始终一致，不会受事物B影响

**幻读测试**

事物A 需要将已有2条数据 money 置 0 操作， 事物 B 在新增一条数据 money 为 1000，并提交。事物 A 操作完发现有3条数据的money 都为 0.

事物A 在可重读隔离级别，读取的数据都是一致的，但是更新的操作却彷佛出现幻觉。**幻读是针对增加或者删除**

幻读会根据事物A 与 B 提交的顺序产生不同的结果，如果 A 先提交，则会出现还为修改的数据。B 先提交，则全部数据修改

#### mysql 快照读与当前读

- 快照读  读取的是记录数据的可见版本，不会因为其他事物插入数据而变化，遵循多版本并发控制规则
- 当前读 （update 、insert、delete）读取的记录是**数据最新的版本**，并且返回的记录都会加上锁（记录锁+gap 间隙锁） ，保证其他事物不能修改当前的记录

**快照读的实现方式：** mvcc

**当前读的实现方式：** next-key 锁（行记录锁+Gap间隙锁）

使用当前读的情况

1. select  ...  for update 

   ```sql
   select * from t_user for update 
   ```

2. select ... lock in share mode

   ```
   select * from t_user lock in share mode
   ```

总结：

1.间隙锁只有 Repeatable Read 、Serialable 隔离级别才会有

2.对主键、唯一索引，如果是当前读，只会执行行记录锁（行记录锁锁定的是索引）

3.如果没有索引的列查询使用当前读，使用的是全表gap 锁（全表锁定）

4.快照读 每次 select 都生成一个快照

5.快照读的实现方式 ： undolog 和 多版本并发控制 MVCC

#### **mysql undolog 与 mvcc**

https://blog.csdn.net/filling_l/article/details/112854716

mysql 在 RR 事物隔离级别，事物内使用 update 的时候，会指向当前读，并将返回数据加锁，之后执行 update 默认是排他锁，读取数据都被锁定

#### mysql 解决幻读问题

mysql 在 Repeatbale Read 隔离级别下，通过**当前读**与**快照读**解决幻读的问题

在当前读使用记录锁+间隙锁，解决幻读的问题

在快照读，如果mysql 不更新记录，由于读取的是旧版本数据，从对其他事物不可见，从而解决幻读

#### spring 事物

spring 方法通过 `@Transactional` 开启事物  ，包含事物的定义

- value 
- transactionManager
- propagation
- isolation
- timeout
- readOnly
- rollbackFor
- rollbackForClassName
- noRollbackFor
- noRollbackForClassName

#### spring 事物隔离级别

isolation 指定事物的隔离级别，默认使用 default ，即使用数据库的事物隔离级别

- default
- read_uncommitted
- read_committed
- repeatable_read
- serialable

#### spring 事物传播特性

spring 类 `Propagation` 定义了事物的7大传播特性

1. required （spring 默认的传播特性）

   `如果当前没有事物，则新建一个事物，如果已存在一个事物，加入这个事物`

2. supports

   `支持当前事物，如果没有当前事物，就以非事物方法执行`

3. mandatory

   `使用当前事物，如果没有当前事物，则抛出异常`

4. requires_new

   `新建事物，如果当前存在事物，就把当前事物挂起`

5. not_support

   `以非事物方式执行，如果当前存在事物，就把当前事物挂起`

6. never

   `以非事物方式执行，如果当前事物存在，则抛出异常`

7. nested

   `如果当前存在事物，则在嵌套事物内执行。如果不存在事物，则执行与 required 类似`

需求1.  method A 事物执行成功， method B 事物不管是否成功，不影响A的操作，如果用事物实现？

> method A 使用事物传播特性 required , method B使用事物 required_new ，开启新的事物。
>
> method A 可以捕获 method B 抛出的异常，不处理，就不会报错。method B 成功与否都不会影响 事物 A ，且事物 B 失败会回滚

需求2. method A 与 method B 要么同时成功，要么同时失败 

> 将事物method A 与 method B的事物传播特性都设置为 Required 即可满足，或者A设置事物也会满足

分析1. method A 与 method B 都没有事物，则执行过程中产生异常，不能进行回滚，产生异常数据

分析2. method A 无事物，method B 有事物，**A调用 B** ，若 A或B 发生异常，**都不能回滚**

分析3. method A 有事物， method B 无事物，A 或 B 发生异常，**都会进行回滚**

分析4. 同一个server 类中，非事物方法调用事物方法，不会开启事物

分析5.@Transaction 注解作用在 private 方法将**失效**

分析6. 使用 try-catch 就会脱离事物控制，不抛出异常就不会回滚

分析7. 使用 required-new ，外层事物不会失败不影响内层事物，内部事物失败外层捕获也可以不处理

#### spring 事物的本质

spring 事物的本质是数据库对事物的支持，spring 无法提供事物功能。纯 jdbc 操作数据库，用到事物步骤

1. 获取连接  Connection con = DriverManager.getConnection()
2. 开启事物
3. 执行 CRUD
4. 提交事物 commit /回滚事物 rollback
5. 关闭连接 con.close()

使用 spring 管理事物后，不需要关注 2 、4  ，是由 spring 自动完成。 那么 spring 是如何完成在 CRUD 之前开启与之后提交事物呢？

通过注解为例：

1. 开启事物，通过在类或方法标注 @Transaction
2. spring 在启动的时候会解析生产相关的 bean ,查看拥有相关注解的类 、方法，并生成代理，根据@Transaction 参数进行配置注入，通过代理类处理事物
3. 真正的数据层的事物提交与回滚是通过 binlog 或者 redo log 完成

#### spring 事物机制

spring 通过提供统一的机制处理不同数据访问事物， 提供 `PlatformTransactionManager` 接口，不同事物访问使用不同的接口实现

| jdbc                         | jpa                   | hibernate                   | jdo                   | 分布式事物            |
| ---------------------------- | --------------------- | --------------------------- | --------------------- | --------------------- |
| DatasourceTransactionManager | JpaTransactionManager | HibernateTransactionManager | JdoTransactionManager | JtaTransactionManager |

代理实现

- jdk 代理接口
- cglib 代理子类

事物参考：https://blog.csdn.net/xiaoxiaole0313/article/details/111713954

### spring 

#### 1.过滤器

过滤器的核心方法： init()  、doFilter()、destroy()

过滤器依赖 servlet 容器，在容器启动时执行 init , 容器停止执行 destroy() 。 doFilter 方法根据匹配规则进行执行

**多个过滤器执行顺序**

> 如果存在多个过滤器，可以指定 doFilter 执行顺序，通过 setOrder() ,值越小优先级越高
>
> 1.  执行 init() 初始化
> 2.  高优先级 doFilter()
> 3. 低 doFilter()
> 4. method()
> 5. 执行销毁 destroy()

#### 2.拦截器

拦截器核心方法：preHandle() 、postHandle()、afterCompletion()

只有 preJandle 返回布尔值 true 才执行后面的方法

**多个拦截器执行顺序**

> 如果存在多个拦截器，可以通过 order 指定加载顺序，值越小优先级越高
>
> 1. 高优先级 preHandle()
> 2. 低 preHandle()
> 3. **metod() 执行**
> 4. 低 postHandle()
> 5. 高 postHandle()
> 6. 低 afterCompletion()
> 7. 高 afterCompletion()

**过滤器与拦截器执行顺序**

> 1. 启动容器执行 **过滤器 init()**
> 2. 执行**过滤器 doFilter()**
> 3. 拦截器 preHandler()
> 4. **method()执行**
> 5. 拦截器 postHandle()
> 6. 拦截器 afterCompletion()
> 7. 关闭容器执行**过滤器 destroy()**
>

#### 3.Ioc & Aop

**Ioc , Inversion of Control** ,意为控制反转 ，Spring 核心容器的主要组件 BeanFactory , 使用 控制反转降低代码之间的耦合度

控制反转，就是创建对象的控制权被转移给 spring 框架。通常实例化一个对象，使用 new 创建，而控制反转将new 的过程交给 spring 容器

依赖注入 DI ，由 Ioc 容器动态地将某个对象所需要的外部资源注入到组件中。依赖注入的四种方式：

- 基于注解注入
- set 注入
- 构造器注入
- 静态工厂注入

> @Autowire 默认按照类型注入
>
> @Resource 默认按照名称注入，如果没有 name 匹配，会按照 类型进行注入
>
> @Qualifier
>
> 区别： 例如 UserService 接口以及实现类 UserServiceImpl ，使用 @Autowire 注入 userService ，会根据类型注入，userServiceImpl 也会被找到，但是
>
> 如果由 userServiceImpl2 也继承 UserService ,则不能使用 @Autowire 注入。
>
> 必须使用 @Autowire + @Qualifier 直接使用根据名称注入

**Aop , Aspect-Oriented Programming** ，意为面向切面编程

实际开发中, 商品查询，业务处理等都需要记录日志、异常等操作，每个接口都进行记录会导致大量代码重复，冗余。 AOP 把所有共用的代码剥离出来，单独

进行集中管理，在具体运行时，由容器进行动态织入这些公共代码。涉及的名字：

- 切面 Aspect
- 通知 Advice
- 连接点 JoinPoint
- 切入点  Pointcut
- 目标对象 Target
- 代理对象 Proxy
- 织入 Weaving

Apo 底层实现是基于**代理技术**，即 jdk 代理 与 cglib 代理

#### 4.cglib 代理与 jdk 代理

Spring Aop 根据类是否使用接口，采用不同的代理

- 如果实现类接口，使用 JDK 动态代理
- 如果没有实现接口，使用 CGLIB 动态代理

*注： IOC 解决了类之间的耦合，动态代理解决了方法之间的耦合*

**JDK 代理**

通过反射与拦截器，实现代理。只能对实现接口的类进行代理

**CDLIB 代理**

被代理类不受限制，底层使用 ASM 框架生产被代理类的子类进行代理

**cglib 代理与 jdk动态代理的区别**

1. jdk 动态代理：利用拦截器+反射机制生成一个代理接口的匿名类，调用具体方法前调用 InvokeHandler 来处理
2. cglib 动态代理： 利用 ASM 框架，对代理对象类生成的 class 文件加载进来，通过修改字节码生产子类处理

**什么时候用 cglib ,什么时候用 jdk 动态代理**

1. jdk 动态代理只能对实现了接口的类成成代理
2. cglib 针对类实现代理，通过生成子类，覆盖所有方法（final 方法无法代理）

***注：spring会自动的切换 jdk 代理与 cglib 代理，根据被代理类是否实现接口选择默认的代理方式***

https://www.cnblogs.com/sandaman2019/p/12636727.html

### spring mvc

#### 1.核心组件

- DispatcherServlet 前端控制器：处理请求，响应结果
- HandlerMapping : 根据 url 查找处理器
- Handler 处理器：控制层逻辑
- HandlerAdapter 处理器适配器 
- ViewResolver 视图解析器 

#### 2.请求步骤

第一步:用户发起请求到前端控制器（DispatcherServlet）

第二步：前端控制器请求处理器映射器（HandlerMappering）去查找处理器（Handle）：通过xml配置或者注解进行查找

第三步：找到以后处理器映射器（HandlerMappering）像前端控制器返回执行链（HandlerExecutionChain）

第四步：前端控制器（DispatcherServlet）调用处理器适配器（HandlerAdapter）去执行处理器（Handler）

第五步：处理器适配器去执行Handler

第六步：Handler执行完给处理器适配器返回ModelAndView

第七步：处理器适配器向前端控制器返回ModelAndView

第八步：前端控制器请求视图解析器（ViewResolver）去进行视图解析

第九步：视图解析器像前端控制器返回View

第十步：前端控制器对视图进行渲染

第十一步：前端控制器向用户响应结果

### spring cloud

#### 1.Netflix 与 Alibaba 对比

|            | 官方                | NetFlix      | Spring Cloud Alibaba |
| ---------- | ------------------- | ------------ | -------------------- |
| 注册中心   | --                  | Eureka       | Nacos                |
| 分布式配置 | Spring Cloud Config | --           | Nacos                |
| 服务熔断   | --                  | Hystrix      | Sentinel             |
| 服务调用   | --                  | Feign        | Dubbo RPC            |
| 服务路由   | --                  | Zuul         | GateWay              |
| 负载均衡   | --                  | Ribbon       | Dubbo LB             |
| 分布式消息 | --                  | SRC RabbitMQ | SRC RocketMQ         |
| 分布式事物 | --                  | --           | Seata                |





### mybatis 

#### 1.一级缓存与二级缓存

一级缓存是 SqlSession 级别，默认开启

二级缓存是 mapper 级别的缓存，多个 SqlSession 可以公用二级缓存，二级缓存是多个 SqlSession 共享的

#### **2.面试题**



### linux

#### 1.用户命令

| 命令                                       | 解释                          |
| ------------------------------------------ | ----------------------------- |
| useradd  es                                | 添加用户 es                   |
| passwd es                                  | 给用户 es 设置密码            |
| userdel es                                 | 删除用户 es                   |
| su  es                                     | 切换用户到 es                 |
| chown -R es  /usr/local/elasticsearc-6.6.2 | 将文件夹所有文件授权给用户 es |
|                                            |                               |
|                                            |                               |

#### 2.防火墙

| 命令                                | 说明               |
| ----------------------------------- | ------------------ |
| firewall-cmd  --state               | 查看当前防火墙状态 |
| systemctl stop firewalld.service    | 停止防火墙         |
| systemctl disable firewalld.service | 禁止防火墙开机启动 |
| systemctl start firewalld.service   | 启动防火墙         |
| systemctl enable firewalld.service  | 允许防火墙开机启动 |
| firewall-cmd --reload               | 重启防火墙         |

*注: 基于 Linux Centos 7.X*

#### 3.解压

| 命令                  | 说明          |
| --------------------- | ------------- |
| tar  -zxvf   a.tar.gz | 解压 tar.gz   |
| tar -xf   a.tar       | 解压 tar 文件 |
|                       |               |
|                       |               |
|                       |               |
|                       |               |
|                       |               |

#### 4.进程管理

| 命令          | 说明               |
| ------------- | ------------------ |
| lsof  -i:3306 | 查询端口进程       |
| kill -9  pid  | 结束进程           |
| netstat  -an  | 查询所有网络和端口 |



### docker

#### 简介



#### 操作

**查询镜像**

```dockerfile
docker images
```

**查询容器**

```dockerfile
docker ps 

docker pas -a  //查询所有容器
```

**进入容器**

```
docker exec -it  容器id/名称 /bin/bash
```

### 密码学

**加密**

数据加密的过程就是对原来的**明文**的文件按照某种算法，使其变为不可读的 **密文** ，达到保护数据的目的

**解密**

将加密后的密文**还原为原来的数据**的过程

**对称加密**

对称加密算法又称为 共享密钥加密算法 , 在对称加密算法中，发送与接受双非都使用**同一个密钥对数据进行加密与解密**

> 例如：DES , 3DES ， AES

**非对称加密**

非对称加密又称为公开密钥加密算法，需要两个密钥，一个公开密钥即**公钥**，一个私有密钥即**私钥**。加密与解密必需使用相对的密钥

> 例如: RSA , ECC

**数字签名**

数字签名是对非对称加密算法的一种应用，发送者通过私钥加密，接收方通过公钥解密确定发送者的身份

**摘要算法**

摘要算法是不可逆的算法，通过对原有信息提取部分信息，生成一串摘要，用于校验数据的完整性

1. md5  ,无论多长的数据，都会生成 128 bit 的串
2. sha1 ，比 md5安全，都是不可逆的
3. hmac ，将输入信息和密钥生成一个摘要输出，必需持有密钥，否则无法验证

***注： md5 不能不可逆，不保证唯一***

### 服务端口备注

| 服务          | 默认端口 | 说明                                                         |
| ------------- | -------- | ------------------------------------------------------------ |
| Tomcat        | 8080     |                                                              |
| RabbitMQ      | 5672     | 15672 ：管理界面ui 端口                                      |
| Mysql         | 3306     |                                                              |
| Oracle        | 1521     |                                                              |
| Postgrsql     | 5432     |                                                              |
| Zookeeper     | 2181     | 2181：对客户端提供的端口<br />2888：集群内部通信的端口<br />3888：集群 leader 选举端口 |
| Redis         | 6379     |                                                              |
| Elasticsearch | 9200     | 9200：http 端口<br />9300：集群之间交换数据                  |
| Kibana        | 5601     |                                                              |
| Mongodb       | 27017    |                                                              |
| ActiveMQ      | 61616    | 8161：监控端口                                               |
| Nginx         | 80       |                                                              |
| SSH           | 22       |                                                              |
| FTP           | 21       |                                                              |

### 错题分析

> 解题数量： 155

#### 1.HttpServlet 响应 web 请求

- web 客户端向 servlet 发送 http 请求， servlet 解析请求
- servlet 容器创建 HttpRequest 对象封装 http 请求， 创建 HttpResponse 对象
- servlet 容器调用 HttpServlet 的 service 方法，方法会根据请求方式，决定调用 doGet 还是 doPost
- servlet 容器把调用结果返回给客户端

#### 2.Map使用开放地址法

- ThreadLocalMap 采用开放地址法解决哈希冲突
- HashMap 采用拉链法，HashSet  采用的是 HashMap

#### 3.枚举代码输出

```Java
public enum AccountType {
    SAVING, FIXED, CURRENT;
    private AccountType()
    {
        System.out.println("It is a account type");
    }
}

class EnumOne
{
    public static void main(String[]args)
    {
        System.out.println(AccountType.FIXED);
    }
}

It is a account type
It is a account type
It is a account type
FIXED
```

#### 4.关于 public class

- 一个类文件中类名可以不使用 public 声明
- 声明为 public 的类，类名必需与文件名相同

#### 5.Object 方法

1. clone()
2. equal()
3. finalize()
4. getClass()
5. hashCode()
6. notify()
7. notifyAll()
8. registerNativers()
9. toString()
10. wait()

#### 6.super 获取类

```java
public class SuperTest extends Date {

    public void test(){
        System.out.println(super.getClass().getName());
    }
    public static void main(String[] args) {
        new SuperTest().test();
    }
}
答案: 包名.SuperTest
```

因为 Date 类与当前类没有重写 getClass() , 所以调用 Object 类的方法，而该方法返回当前运行的类的名称（包名+类名，没有后缀 .class）

#### 7.命名规则

组成：数字、字母、下划线、$符号

规则：

- 只能以字母、下划线、$符开头
- 不能是关键字
- true,false null 不是关键字，不能命名， NULL 不是

#### 8.基础类型比较

```java
        Integer i = 42;
        Long l = 42L;
        Double d = 42.0;

        //System.out.println(i == l);  报错
        //System.out.println(i == d);  报错
        //System.out.println(d == l);  报错
        //System.out.println(i.equals(d));  false
        //System.out.println(i.equals(l)); false
        //System.out.println(d.equals(l)); false
       // System.out.println(l.equals(42L)); true
```

解析：前三条因为不同类型引用 == 比较编译报错，后三条比较返回 false 因为 equal 方法，先比较类型，如果类型不同直接返回 false 

```java
    public boolean equals(Object obj) {
        if (obj instanceof Long) {
            return value == ((Long)obj).longValue();
        }
        return false;
    }
```

总结：

- 基本类型与封装类型 "==" 比较，封装类型先自动拆箱，再进行比较
- Integer "==" 比较，（-128~127） 范围内是值比较，范围外是地址比较
- 2个基本类型的封装类型 equals 比较，首先比较类型，再比较值

#### 9.执行顺序

1. 父类静态块
2. 子类静态块
3. 父类构造块
4. 父类构造函数
5. 子类构造块
6. 子类构造函数

> - 构造块和构造方法是绑定一起执行的，先执行构造块
> - 静态变量和静态块的执行顺序是根据前后顺序加载的（构造方法比静态方法先执行是可能的）
> - 类成员变量初始化在构造块之前执行

#### 10.类修饰符

- 普通类：**public ,default, final , abstract**
- 内部类：**public , private ,default,protected,static,abstract**

#### 11.URL 

``` 

 URL url = new URL("http://www.baidu123.com");

 System.out.println(url);

 #结果 http://www.baidu123.com
```

当 URL 内的网址不存在时，返回参数内容。**如果不加 http:// 则直接报错**

#### 12.多态方法

```java
public class Base {

    private String name = "base";

    public Base() {
        printName();
    }

    public void printName(){
        System.out.println(name);
    }
}

public class Sub extends Base{

    public String name = "sub";

    public void printName(){
        System.out.println(name);
    }

    public static void main(String[] args) {
        Base base = new Sub();
    }
}

```

结果： null 

分析：因为在 new 子类的时候先要构造父类，父类调用 printName() 方法，而子类对方法进行了重写，则实际调用的是子类的方法，此时子类未初始化，则 null

**如果父类方法未被重写，则输出 base**

#### 13.类描述

- 抽象类相比与普通类，是不能有实例。可以没有抽象方法
- 抽象类可以继承普通类
- 抽象类可以有构造方法
- final 不能修饰接口

#### 14.String.split

```java 
   String str = "";

   System.out.println(str.split(",").length);
```

输出： 1

分析：split 会根据指定分隔符分割，如果没有找打分隔符，则把整个字符串放入字符数组第一个位置，所以为 1

#### 15.DBMS 子系统

- 原子性：事物管理子系统
- 一致性：完整性子系统
- 持久性：恢复管理子系统
- 隔离性：并发控制子系统

#### 16.构造赋值

```java
public class InitVariable {
    int x,y,z,w;

    public InitVariable(int a,int b){
        x = a;
        y = b;
    }

    public InitVariable(int a,int b,int c,int d){
       // x = a, y =d;   错误表达，逗号只能用于变量初始化，不能用于赋值
       // InitVariable(a,b); 错误表达
        //  new InitVariable(a,b), 错误
        this(a,b); 
        z = c;
        w = d;
    }
}
```

#### 17.ArrayList 扩容

> ArrayList list = new ArrayList(20);  中的list扩充几次
>
> ArrayList 默认容量为 10 ，扩容为原来的 1.5倍 ，最大容量 Integer.MAX_VALUE - 8 .
>
> 如果初始化传入容量，默认为传入的大小，则不进行扩容

#### 18.Java自带三大注解

| 注解              | 描述                                                 |
| ----------------- | ---------------------------------------------------- |
| @Override         | 标明重写                                             |
| @Deprecated       | 标明过期                                             |
| @Suppresswarnings | @Suppresswarnings(“deprecation”)  屏蔽过期的类的警告 |

#### 19.Thread 运行 run

```java
public class RunnableTest {

    public static void main(String[] args) {

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                System.out.println("foo");
            }
        };

        Thread t = new Thread(runnable);
        t.run();
        System.out.println("bar");

    }
}
```

线程调用 run 方法，实际调用的是 Runnable 本身的方法，也并非启动线程（start 方法）

```java
    public void run() {
        if (target != null) {
            target.run();
        }
    }
```

> 答错题因为考虑到多态的问题，因为 Thread 继承 Runnable , 可能调用的是自己的 run 方法
>

#### 20.Replace/ReplaceAll

```java
        String str1 = "com.jd.".replaceAll(".","/")+"my.class";
        System.out.println(str1);

        String str2 = "com.jd.".replaceAll("\\.","/")+"my.class";
        System.out.println(str2);

        String str3 = "com.jd.".replace(".","/")+"my.class";
        System.out.println(str3);
```

结果：

```java
///////my.class
com/jd/my.class
com/jd/my.class
```

解析： replaceAll 第一个参数为正则匹配，'.' 匹配的是全部，即将全部的字符替换。如果只想替换 ‘. ’ ,需要第二种转义，或者使用 replace 

#### 21.HashMap 与 HashTable

- HashTable 的 key 与 value 都不能为 null
- HashMap key 与 value 均可以为 null
- HashMap 没有 contails() 方法

#### 22.声明数组

```java 
        char a[];

        String []b;

       // Object d[50]; 报错

       // String e[50]; 报错

        Object f[];
```

在 Java 中，声明一个数组不能指定大小， 只有在实例化的时候才可以指定

#### 21.局部变量初始化

> 类成员变量，类静态变量使用不需要初始化，系统会自动初始化
>
> 方法内局部变量使用前必需进行初始化，否则编译不通过

```java
public class AboutPrint {
    public int x;
    public static void main(String[] args) {
        //System.out.println(x); 报错，静态方法不能引用非静态成员
        //System.out.println(new AboutPrint().x);  输出 ： 0
    }
}
```

数组声明

```java
	int[] arr1 = new int[10];

    Integer[] arr2 = new Integer[10];

    System.out.println(arr1[0]);
        
    System.out.println(arr2[0]);
```

> int 数组创建后会默认初始值 0 ，Integer 数组默认初始值 null

#### 22.构造函数

> 构造函数智只能被显示调用，**不能被继承**

#### 23.位运算取反

```
        int i= 5;
        int j= 10;
        System.out.println(i+~j);
```

结果： -6

分析：~ 为位运算符，将所有二进制位取反后为负数，负数表示的是补码（-11），转换为元码后进行计算

#### 24.try-catch

```java
public class TryTest {
    public static void main(String[] args) {
        System.out.println(test());
    }

    public static int test(){
        int i = 1;
        try{
            System.out.println(i);
            return ++i;
        }catch (Exception e){
            System.out.println(i);
            return ++i;
        }finally {
            ++i;
            System.out.println(i);
        }
    }
}
```

结果：1 3 2

分析：return 的值会存入零时空间，如果finally 有return ，则刷新该值

```java
public class TryTest {
    public static void main(String[] args) {
        System.out.println(test());
    }

    public static int test(){
        int i = 1;
        try{
            System.out.println(i);
            int j = 10/0;
            return ++i;
        }catch (Exception e){
            System.out.println(i);
            return ++i;
        }finally {
            ++i;
            System.out.println(i);
        }
    }
}
```

结果： 1 1 3 2

#### 25.引用可变与对象可变

- String ,StringBuffer ，StringBuilder 三个类都是 final 修饰，即**引用不可变**
- String 对象不可变因为 内部使用 final 修饰了字符数组
- StringBuffer ，StringBuilder 内部数组都可变，即**对象可变**

#### 26.i++ 自增

```java
int i = 0;  
i++;
System.out.println(i);  //值为1    打印的是表达式的结果

int i = 0;  
++i;
System.out.println(i);  //值为1     打印的是表达式的结果 

int i = 0;  
i = i++;
System.out.println(i);  //值为0      打印的是中间变量(JVM中间缓存变量机制)

int i = 0;  
i = ++i;
System.out.println(i);  //值为1    打印的是表达式的结果
```

#### 27.wait 与 notifyAll

- wait() ,notify(), notifyAll() 调用必需在同步块内（synchronized 内）
- notify ,notifyall 并不会释放锁，只有同步块执行结束才释放锁
- wait 会释放锁，等待被唤醒

#### 28.String 创建了几个对象

- String str = new  String("abc") 创建了几个对象

  > 首先检查常理池是否存在 “abc” ，如果不存在，则创建改常量，并在堆上创建字符串对象，对象
  >
  > 引用指向字符串常量池 “abc” , 所以 1个或者 2个

- String str = “abc ” 创建了几个对象

  > 0 个或者一个，在常量池

- String str = "abc" + "def" 创建了几个对象

  > 0个或者 一个 ，Java 编译器会优化为 “abcdef” ，判断该对象是否存在

- String str = "abc" + new String("def") 创建了几个对象

  > 4个对象 与 一个 StringBuffer 对象

#### 29.赋值

``` java
        //Double d = 10;  int 不能直接赋值给double

        //float f = 10.0; float 赋值需要使用 f ,默认值类型为 double

        //char ch = 10;  正确赋值

        //String str = 'a'; 赋值错误，单引号为字符，需要使用双引号

        //double d = 5.3e12; 正确赋值

        //int id= 10.0;  double 类型不能赋值给 int 
```

#### 30.lang 包下不能被继承的类

```java
public final class Byte
public final class Character
public static final class Character.UnicodeBlock
public final class Class<T>
public final class Compile
public final class Double
public final class Float
public final class Integer
public final class Long
public final class Math
public final class ProcessBuilder
public final class RuntimePermission
public final class Short
public final class StackTraceElement
public final class StrictMath
public final class String
public final class StringBuffer
public final class StringBuilder
public final class System
public final class Void
```

#### 32.String.length()

```java
        String str1 = " ";
        System.out.println(str1.length());

        String str2 = "你好！";
        System.out.println(str2.length());
        System.out.println(str2.getBytes("GBK").length);

        String str3 = "-";
        System.out.println(str3.length());
```

> 分析：
>
> - 空格占用字符串长度为 1 ，中文也是 1
> - 中文占用字节长度为 2
> - length() 方法是查看字符串长度，而不是字节长度

#### 33.类之间的关系

- is  a
- uses a
- has a

























