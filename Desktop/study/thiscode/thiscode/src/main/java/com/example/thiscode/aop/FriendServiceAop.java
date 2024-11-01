package com.example.thiscode.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class FriendServiceAop {

    private static final Logger logger = LoggerFactory.getLogger(FriendServiceAop.class);

    @AfterThrowing(pointcut = "execution(* com.example.thiscode.service.FriendService.*(..))", throwing = "ex")
    public void handleServiceExceptions(JoinPoint joinPoint, Exception ex) throws Exception {
        if (ex instanceof IllegalArgumentException || ex instanceof IllegalStateException) {
            // 예외 메시지를 변경하지 않고 원래의 메시지를 유지합니다.
            logger.info("AOP에서 처리한 예외: {}", ex.getMessage());
            logger.info("메소드 파라미터: {}", Arrays.toString(joinPoint.getArgs()));

            // 예외를 덮어쓰지 않고 그대로 다시 던지기
            throw ex;
        } else {
            String errorMessage = "서버 에러가 발생했습니다.";
            logger.error(errorMessage + " : " , ex.getMessage(), ex);

            throw new RuntimeException(errorMessage, ex);
        }
    }
}

