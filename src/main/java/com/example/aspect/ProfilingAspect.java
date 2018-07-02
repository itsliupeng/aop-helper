package com.example.aspect;

import com.example.annotation.Profiling;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by liupeng on 28/04/2017.
 */

@Aspect
public class ProfilingAspect {
    private static final Logger logger = LoggerFactory.getLogger(ProfilingAspect.class);
    private static final Switch aSwitch = SwitchManager.register(new AbstractSwitch(ProfilingAspect.class.getName()).on());
    private static final ThreadLocal<String> requestIdContext = new ThreadLocal<>();

    public Switch getSwitch() {
        return aSwitch;
    }

    /**
     * 只对 public 方法有效，包括 scala object 方法
     * attention： 日志级别是 INFO
     * @param joinPoint
     * @param annotation
     * @return
     * @throws Throwable
     */
    @Around("execution(* com.example..*.*(..)) && @annotation(annotation)")
    public Object profileMethod(final ProceedingJoinPoint joinPoint, final Profiling annotation) throws Throwable {
        if (aSwitch.isOn() && annotation.value() && (!annotation.notScalaObject() || isNotScalaObject(joinPoint))) {
            long start = System.currentTimeMillis();
            addRequestId(joinPoint, annotation);
            Object result = null;
            try {
                result = joinPoint.proceed();
                return result;
            } finally {
                long elapsed = System.currentTimeMillis() - start;
                log(joinPoint, annotation, result, elapsed);
            }
        } else {
            return joinPoint.proceed();
        }
    }

    private static void addRequestId(ProceedingJoinPoint joinPoint, Profiling annotation) {
        if (annotation.setRequestId()) {
            Object[] argObjects = joinPoint.getArgs();
            String requestId = null;
            for (int i = 0; i < argObjects.length; i++) {
                // get requestId from argObjects
            }
            requestIdContext.set(requestId);
        }
    }

    private static void log(ProceedingJoinPoint joinPoint, Profiling annotation, Object result, long elapsed) {
        StringBuilder sb = null;
        if (annotation.args().length != 0) {
            sb = new StringBuilder(joinPoint.toShortString()).append(", args: ");
            Object[] argObjects = joinPoint.getArgs();
            for (int i : annotation.args()) {
                if (0 <= i && i < argObjects.length) {
                    sb.append(i + "->" + argObjects[i] + ",");
                }
            }
            if (sb.charAt(sb.length() - 1) == ',') {
                sb.setLength(sb.length() - 1);
            }
        }

        if (contains(annotation.result(), "all")) {
            if (sb == null) {
                sb = new StringBuilder(joinPoint.toLongString());
            }
            sb.append(", result: ").append(result);
        }
        if (contains(annotation.result(), "size")) {
            if (sb == null) {
                sb = new StringBuilder(joinPoint.toLongString());
            }
            sb.append(", result_size: ").append(call(result, "size"));
        }

        if (sb != null) {
            sb.append(", time: ").append(elapsed);
            String requestId = requestIdContext.get();
            if (requestId != null) {
                sb.append(", r=").append(requestId);
            }
            logger.info(sb.toString());
        }
    }

    private static boolean isNotScalaObject(JoinPoint joinPoint) {
        return !joinPoint.getSignature().getDeclaringTypeName().endsWith("$");
    }

    private static <T> boolean contains(T[] arr, T ele) {
        for (int i = 0; i < arr.length; i++) {
            if (ele.equals(arr[i])) {
                return true;
            }
        }
        return false;
    }

    /**
     *
     * @param o
     * @param methodName
     * @return must have toString method
     */
    private static Object call(Object o, String methodName) {
        try {
            Method method = o.getClass().getMethod(methodName);
            return method.invoke(o);
        } catch (NoSuchMethodException e) {
            logger.error(o.getClass().getName() + "does not have method " + methodName, e);
        } catch (IllegalAccessException | InvocationTargetException e) {
            logger.error(o.getClass().getName() + "#" + methodName +  " invoke error", e);
        }
        return "";
    }

    private static <T> T cast(Object o, T classObject) {
        T result = classObject;
        try {
            // to-do: this cast not throw exception
            result = (T)o;
        } catch (ClassCastException e) {
            logger.error("", e);
        }
        return result;
    }

    public static void main(String[] args) {
        List<Integer> result = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            result.add(i);
        }

        boolean isEmpty = cast(call(result, "isEmpty"), Boolean.FALSE);
        int size = cast(call(result, "size"), new Integer(0));

        System.out.println("Done");
    }

    public static String getRequestId() {
        return requestIdContext.get();
    }

}
