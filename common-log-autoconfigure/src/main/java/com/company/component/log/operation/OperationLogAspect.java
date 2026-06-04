package com.company.component.log.operation;

import com.company.component.log.properties.LogProperties;
import com.company.component.log.spi.OperationLogRecorder;
import com.company.component.log.support.MdcKeys;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * {@link OperationLog} 切面，委托 SPI 记录（不阻塞主链路由实现方保证）。
 */
@Aspect
public class OperationLogAspect {

    private static final Logger log = LoggerFactory.getLogger(OperationLogAspect.class);

    private final OperationLogRecorder recorder;
    private final LogProperties.Operation operationProperties;

    public OperationLogAspect(OperationLogRecorder recorder, LogProperties properties) {
        this.recorder = recorder;
        this.operationProperties = properties.getOperation();
    }

    @Around("@annotation(operationLog)")
    public Object around(ProceedingJoinPoint joinPoint, OperationLog operationLog) throws Throwable {
        if (!operationProperties.isEnabled()) {
            return joinPoint.proceed();
        }
        long start = System.currentTimeMillis();
        OperationLogEntry entry = new OperationLogEntry();
        entry.setModule(operationLog.module());
        entry.setAction(operationLog.action());
        entry.setTraceId(MDC.get(MdcKeys.TID));
        entry.setOperatorId(MDC.get(MdcKeys.USER_ID));
        entry.setOperatorName(MDC.get(MdcKeys.USERNAME));
        entry.setRequestUri(currentRequestUri());
        try {
            Object result = joinPoint.proceed();
            entry.setSuccess(true);
            return result;
        } catch (Throwable ex) {
            entry.setSuccess(false);
            entry.setErrorMessage(ex.getMessage());
            throw ex;
        } finally {
            entry.setDurationMs(System.currentTimeMillis() - start);
            try {
                recorder.record(entry);
            } catch (Exception ex) {
                log.error("OperationLogRecorder failed module={} action={}", operationLog.module(),
                        operationLog.action(), ex);
            }
        }
    }

    private static String currentRequestUri() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            HttpServletRequest request = attributes.getRequest();
            return request.getRequestURI();
        }
        return null;
    }
}
