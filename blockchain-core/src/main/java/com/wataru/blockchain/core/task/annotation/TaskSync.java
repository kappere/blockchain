package com.wataru.blockchain.core.task.annotation;

import java.lang.annotation.*;

/**
 * 每次运行一次任务
 * @author zhouziqiang 
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TaskSync {
}
