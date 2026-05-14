package com.company.reservationserver.support.aop;

import com.company.reservationserver.support.annotation.DistributedLock;
import com.company.reservationserver.support.utils.CustomSpELParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
@Order(1)
@RequiredArgsConstructor
@Slf4j
public class DistributedLockAspect {

    private static final String REDISSON_LOCK_PREFIX = "lock:accommodation:";
    private final RedissonClient redissonClient;

    @Around("@annotation(com.company.reservationserver.support.annotation.DistributedLock)")
    public Object lock(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        DistributedLock distributedLock = method.getAnnotation(DistributedLock.class);

        // SpEL을 이용해 동적으로 락 키 생성
        Object dynamicKey = CustomSpELParser.getDynamicValue(
                signature.getParameterNames(), joinPoint.getArgs(), distributedLock.key());
        String lockKey = REDISSON_LOCK_PREFIX + String.valueOf(dynamicKey);

        RLock lock = redissonClient.getLock(lockKey);

        try {
            boolean available = lock.tryLock(distributedLock.waitTime(), distributedLock.leaseTime(), distributedLock.timeUnit());
            if (!available) {
                log.warn("[Lock Timeout] 락 획득 실패. key: {}", lockKey);
                throw new IllegalStateException("현재 예약 요청이 많아 처리가 지연되고 있습니다. 잠시 후 다시 시도해주세요.");
            }
            log.info("락 획득 성공: {}", lockKey);

            return joinPoint.proceed();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("예약 처리 중 인터럽트가 발생했습니다.");
        } finally {
            if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.info("락 반환 완료: {}", lockKey);
            }
        }
    }
}
