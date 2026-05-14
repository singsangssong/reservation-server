package com.company.reservationserver.support.aop;

import com.company.reservationserver.support.annotation.Idempotent;
import com.company.reservationserver.support.utils.CustomSpELParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
@Order(2)
@RequiredArgsConstructor
@Slf4j
public class IdempotencyAspect {

    private static final String IDEMPOTENCY_PREFIX = "idempotency:";
    private final RedissonClient redissonClient;

    @Around("@annotation(com.company.reservationserver.support.annotation.Idempotent)")
    public Object checkIdempotency(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Idempotent idempotent = method.getAnnotation(Idempotent.class);

        Object dynamicKey = CustomSpELParser.getDynamicValue(
                signature.getParameterNames(), joinPoint.getArgs(), idempotent.key());
        String redisKey = IDEMPOTENCY_PREFIX + String.valueOf(dynamicKey);

        RBucket<String> bucket = redissonClient.getBucket(redisKey);

        // SETNX 로직 (없으면 PROCESSING 상태로 저장하고 true 반환)
        boolean isFirstRequest = bucket.trySet("PROCESSING", idempotent.ttlMinutes(), TimeUnit.MINUTES);

        if (!isFirstRequest) {
            log.warn("[Idempotency] 중복 결제 요청 감지: {}", redisKey);
            throw new IllegalStateException("이미 처리 중이거나 완료된 결제 요청입니다.");
        }

        try {
            // 비즈니스 로직 실행
            return joinPoint.proceed();
        } catch (Exception e) {
            // 로직 실패 시 (잔액 부족 등), 멱등성 키를 지워주어 사용자가 다시 시도할 수 있게 함
            bucket.delete();
            throw e;
        }
    }
}
