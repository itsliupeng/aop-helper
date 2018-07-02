package com.example.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by liupeng on 28/04/2017.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Profiling {
    /**
     * 控制 Profiling 逻辑是否有效的开关，默认打开
     * @return
     */
    boolean value() default true;

    /**
     * 是否不对 Scala object 的方法有效
     * @return
     */
    boolean notScalaObject() default false;

    /**
     * 通过 index 指定打印那些参数
     * @return
     */
    int[] args() default {};

    /**
     * 是否打印 return 结果
     * @return
     */
    String[] result() default "";

    /**
     * eg: @Profiling(perfCounter={"qps:method-qps", "time:method-time", "qps.isEmpty:method-isEmpty", "histogram.size:method-size"})
     * @return
     */
    String[] perfCounter() default "";

    /**
     * 是否在 log 中记录 setRequestId
     * @return
     */
    boolean setRequestId() default false;
}
