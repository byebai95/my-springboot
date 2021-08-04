package com.bai.app.config;

import com.bai.app.annotation.OperatorAction;
import com.bai.app.dao.ActionLogDao;
import com.bai.app.model.entity.ActionLog;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * @Author:baizhuang
 * @Date:2021/8/4 14:46
 */
@AllArgsConstructor
@Component
@Slf4j
@Aspect
public class OperationActionConfig {

    private final ActionLogDao actionLogDao;

    @Pointcut(value = "@annotation(com.bai.app.annotation.OperatorAction)")
    public void pointCut(){
    }

    @Before("pointCut() && @annotation(operatorAction)")
    public void before(OperatorAction operatorAction){
        ActionLog actionLog = new ActionLog();
        actionLog.setMessage(operatorAction.value());
        Integer rowNumber = actionLogDao.save(actionLog);
        log.info("actionLogRow:"+rowNumber);
    }

    @Around("pointCut()")
    public void around(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        Arrays.stream(args).forEach(System.out::println);
        joinPoint.proceed();
    }

}
