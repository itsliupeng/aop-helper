## 基于 aspectjweaver AOP 实现的 Annotation Profiling 和 一些 HDFS 和 Spark helper 方法

实现了 Annotation Profiling，可以对 java 和 scala method 的耗时和输入输出进行 profile，打印日志等，同时提供了一些 HDFS 和 spark 的 helper 方法，方便对代码架构进行提炼，更有条理

### 准备

将 lib 文件夹中 aspectjweaver-1.8.9.jar 放在 $RESIN_HOME/lib, 并在 `resin.conf` 中增加  ```<jvm-arg>-javaagent:$RESIN_HOME/lib/aspectjweaver-1.8.9.jar</jvm-arg>`,
tomcat 配置也是如此，将 aspectjweaver-1.8.9.jar 放入 lib 包中，在启动命名中引入 aspectjweaver-1.8.9.jar ``

```
项目 pom.xml dependencies 中加入

        <dependency>
            <groupId>com.example</groupId>
            <artifactId>aop-helper</artifactId>
            <version>最新版本号</version>
        </dependency>

```

### AOP

#### @Profiling

对一个方法打印输入参数，输出结果，以及耗时

- Java 使用方式如下：

    ``` java
    @Profiling(args = {0, 1}, result = {"all", "size"})
    
    ``` 
    - args: 需要打印的参数的 index
    
    - result: "all" 打印返回值所有内容；"size" 打印返回值的 size （返回类型必须有 `size()` 方法）





- Scala 使用方式
    ```
     @Profiling(args = Array(0, 1), result = Array("all", "size"))
   ```


### Helper (暂只支持 Scala)

#### Monitor
- ```registerToMonitor(name: String)```
    
    对 guava Cache, ThreadPoolExecutor 统计运行状况打日志, 30s 打一次日志

#### HDFS
- ```def read[B](pathStr: String)(f: Stream[String] => B): Try[B]```

    lazy 方式读取 HDFS 文件


#### Spark
- ```implicit class OverwritePath[T <: String](pathStr: String)```
    
    如果 `pathStr` 存在，则删除
    
- ```implicit class SaveAsTextFileAndProduction[T <: RDD[_]](rdd: T)```

- ```implicit class SaveNotEmptyAsTextFile[T <: RDD[_]](rdd: T)```

- ```implicit class SaveNotEmptyAsTextFileAndProduction[T <: RDD[_]](rdd: T)```

- ```implicit class RemovePath[T <: String](pathStr: String)```
