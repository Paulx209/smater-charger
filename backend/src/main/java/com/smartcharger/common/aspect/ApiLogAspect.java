package com.smartcharger.common.aspect;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.nio.charset.StandardCharsets;

/**
 * 接口日志切面
 */
@Slf4j
@Aspect
@Component
public class ApiLogAspect {

    /**
     * 定义切点：拦截所有Controller
     */
    @Pointcut("execution(* com.smartcharger.controller..*.*(..))")
    public void apiLog() {
    }

    /**
     * 环绕通知：记录接口调用信息
     */
    @Around("apiLog()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        // 获取请求信息
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return joinPoint.proceed();
        }

        HttpServletRequest request = attributes.getRequest();

        // 记录请求信息
        String method = request.getMethod();
        String url = request.getRequestURI();
        String queryString = request.getQueryString();

        log.info("========== 接口请求 ==========");
        log.info("URL: {} {}", method, queryString != null ? url + "?" + queryString : url);

        // 记录请求体（仅POST/PUT/PATCH）
        if ("POST".equals(method) || "PUT".equals(method) || "PATCH".equals(method)) {
            String body = getRequestBody(request);
            if (body != null && !body.isEmpty()) {
                log.info("请求体: {}", body);
            }
        }

        // 执行方法
        Object result;
        try {
            result = joinPoint.proceed();
            long endTime = System.currentTimeMillis();
            log.info("执行成功, 耗时: {}ms", (endTime - startTime));
        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            log.error("执行失败, 耗时: {}ms", (endTime - startTime));
            throw e;
        }

        return result;
    }

    /**
     * 获取请求体内容
     */
    private String getRequestBody(HttpServletRequest request) {
        if (request instanceof ContentCachingRequestWrapper) {
            ContentCachingRequestWrapper wrapper = (ContentCachingRequestWrapper) request;
            byte[] buf = wrapper.getContentAsByteArray();
            if (buf.length > 0) {
                return new String(buf, 0, buf.length, StandardCharsets.UTF_8);
            }
        }
        return null;
    }
}
