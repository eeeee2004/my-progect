package com.hotel.common.aspect;

import com.hotel.entity.OperationLog;
import com.hotel.mapper.OperationLogMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private final OperationLogMapper operationLogMapper;

    @Around("@annotation(annotation)")
    public Object around(ProceedingJoinPoint joinPoint, com.hotel.common.annotation.OperationLog annotation) throws Throwable {
        OperationLog logEntity = new OperationLog();
        logEntity.setModule(annotation.module());
        logEntity.setAction(annotation.action());
        logEntity.setDescription(annotation.description());
        logEntity.setCreateTime(LocalDateTime.now());

        populateOperator(logEntity);

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] paramNames = signature.getParameterNames();
        Object[] paramValues = joinPoint.getArgs();
        if (paramNames != null && paramValues != null) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < Math.min(paramNames.length, paramValues.length); i++) {
                if (paramValues[i] != null && !"password".equals(paramNames[i])) {
                    sb.append(paramNames[i]).append("=");
                    String val = paramValues[i].toString();
                    if (val.length() > 200) val = val.substring(0, 200) + "...";
                    sb.append(val).append("; ");
                }
            }
            logEntity.setRequestParams(sb.toString());
        }

        extractTarget(joinPoint, logEntity);

        try {
            Object result = joinPoint.proceed();
            logEntity.setResult("SUCCESS");
            saveLog(logEntity);
            return result;
        } catch (Throwable e) {
            logEntity.setResult("FAIL");
            saveLog(logEntity);
            throw e;
        }
    }

    private void populateOperator(OperationLog logEntity) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof Long) {
                logEntity.setOperatorId((Long) auth.getPrincipal());
            }
            if (auth != null && auth.getAuthorities() != null) {
                auth.getAuthorities().stream().findFirst()
                        .ifPresent(a -> logEntity.setOperatorType(a.getAuthority()));
            }
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                logEntity.setIpAddress(request.getRemoteAddr());
            }
        } catch (Exception ignored) {
        }
    }

    private void extractTarget(ProceedingJoinPoint joinPoint, OperationLog logEntity) {
        Object[] args = joinPoint.getArgs();
        if (args != null) {
            for (Object arg : args) {
                if (arg instanceof Long) {
                    logEntity.setTargetId((Long) arg);
                    break;
                }
            }
        }
    }

    private void saveLog(OperationLog logEntity) {
        try {
            operationLogMapper.insert(logEntity);
        } catch (Exception e) {
            log.warn("审计日志记录失败: {}", e.getMessage());
        }
    }
}
