package com.cyz.common.exception;

import com.cyz.common.result.R;
import com.cyz.entity.SysSystemLog;
import com.cyz.service.SystemLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final SystemLogService systemLogService;

    @ExceptionHandler(BizException.class)
    @ResponseStatus(HttpStatus.OK)
    public R<Void> handleBizException(BizException e) {
        log.warn("Business exception: {}", e.getMessage());
        return R.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<Void> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("Validation exception: {}", message);
        return R.fail(400, message);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<Void> handleMethodValidationException(HandlerMethodValidationException e) {
        String message = e.getAllValidationResults().stream()
                .flatMap(r -> r.getResolvableErrors().stream())
                .map(Object::toString)
                .collect(Collectors.joining("; "));
        log.warn("Method validation exception: {}", message);
        return R.fail(400, message);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public R<Void> handleException(Exception e) {
        log.error("Unexpected exception", e);
        recordSystemLog(e);
        return R.fail(500, "系统内部错误");
    }

    private void recordSystemLog(Exception e) {
        try {
            SysSystemLog systemLog = new SysSystemLog();
            systemLog.setLogLevel("ERROR");
            systemLog.setLogModule("GlobalExceptionHandler");
            systemLog.setLogContent(e.getMessage());
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String stack = sw.toString();
            systemLog.setExceptionStack(stack.length() > 4000 ? stack.substring(0, 4000) : stack);
            systemLog.setOccurTime(LocalDateTime.now());
            try {
                systemLog.setServerIp(InetAddress.getLocalHost().getHostAddress());
            } catch (Exception ignored) {
            }
            systemLogService.save(systemLog);
        } catch (Exception ex) {
            log.warn("记录系统日志失败", ex);
        }
    }
}
