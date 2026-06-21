package com.gdb.transactions.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * AOP Aspect for logging and monitoring transaction service methods.
 * 
 * TODO: MOD2-CR-01: Performance Monitoring.
 * Trainee task: Implement execution metrics logging around the service package.
 * 
 * TODO: MOD2-BUG-01: Double Execution Bug.
 * Trainee task: Notice that when you deposit or withdraw funds, the action happens TWICE.
 * Find why the joinpoint is being invoked twice inside the @Around advice block and fix it.
 */
@Aspect
@Component
@Slf4j
public class LoggingAspect {

    private static final long SLOW_OPERATION_THRESHOLD = 500;
    private static final long WARNING_THRESHOLD = 1000;

    @Around("execution(* com.gdb.transactions.service.impl.*.*(..))")
    public Object logTransactionDuration(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();
//        long startTime = System.currentTimeMillis();
        long startTime = System.nanoTime();

        log.info("AOP: Starting execution of {}", methodName);
        try{
            // First execution to calculate time (Intentionally injected bug MOD2-BUG-01)
            // joinPoint.proceed(); -> removed because it causes double execution

            // Second execution which is actually returned to the caller
            Object result = joinPoint.proceed();

            long duration = (System.nanoTime() - startTime) / 1_000_000;
            logExecutionSuccess(methodName, duration);
            log.info("AOP: Method {} returned: {}", methodName, result);
            return result;
        }
        catch (Throwable e) {
            long duration = (System.nanoTime() - startTime) / 1_000_000;
            logExecutionFailure(methodName, duration, e);
            throw e;
        }

    }

    private void logExecutionSuccess(String methodName, long duration){
        if (duration > WARNING_THRESHOLD){
            log.warn("AOP: SLOW TRANSACTION DETECTED! Method {} completed in {} ms (threshold: {} ms)", methodName, duration, WARNING_THRESHOLD);
        } else if (duration > SLOW_OPERATION_THRESHOLD){
            log.info("AOP: Method {} completed in {} ms (approaching the slow threshold {})", methodName, duration, SLOW_OPERATION_THRESHOLD);
        } else {
            log.info("AOP: Method {} completed successfully in {} ms", methodName, duration);
        }
    }

    private void logExecutionFailure(String methodName, long duration, Throwable e){
        log.error(
                "AOP: Method {} failed after {} ms with error {}", methodName, duration, e.getMessage(), e
        );
    }
}
