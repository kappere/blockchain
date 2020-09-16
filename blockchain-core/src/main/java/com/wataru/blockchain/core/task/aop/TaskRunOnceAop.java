package com.wataru.blockchain.core.task.aop;

import com.wataru.blockchain.core.task.annotation.TaskSync;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author zhouziqiang 
 */
@Slf4j
@Component
@Aspect
public class TaskRunOnceAop {
    private static final Map<String, Lock> LOCK_MAP = new ConcurrentHashMap<>();
    /**
     * task任务锁
     * @param pjp
     */
    @Around("@annotation(taskSync)")
    public Object around(ProceedingJoinPoint pjp, TaskSync taskSync) throws Throwable {
        Signature signature = pjp.getSignature();
        Lock lock = LOCK_MAP.computeIfAbsent("TaskRunOnceAop#".concat(signature.getDeclaringTypeName()).concat("#").concat(signature.getName()), key -> new ReentrantLock());
        if (lock.tryLock()) {
            try {
                return pjp.proceed();
            } finally {
                lock.unlock();
            }
        }
        return null;
    }
}
