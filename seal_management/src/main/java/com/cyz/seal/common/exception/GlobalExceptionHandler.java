package com.cyz.seal.common.exception;

import com.cyz.seal.common.result.Result;
import com.cyz.seal.common.result.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理：把异常统一转成 {@link Result}（HTTP 200，body 中 code 表示成败）。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 业务异常：返回业务码 + 提示，warn 级别（属预期内）。 */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusiness(BusinessException e) {
        log.warn("业务异常: {}", e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    /** 参数校验失败：取第一条校验错误信息。 */
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public Result<Void> handleValidation(Exception e) {
        String msg = "参数校验失败";
        if (e instanceof MethodArgumentNotValidException ex) {
            msg = ex.getBindingResult().getFieldErrors().stream()
                    .findFirst().map(fe -> fe.getDefaultMessage()).orElse(msg);
        }
        return Result.fail(ResultCode.PARAM_ERROR.getCode(), msg);
    }

    /** 权限不足（@PreAuthorize 校验失败）：HTTP 403。方法级安全抛出的 AccessDeniedException 在此处统一处理。 */
    @org.springframework.web.bind.annotation.ResponseStatus(org.springframework.http.HttpStatus.FORBIDDEN)
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public Result<Void> handleAccessDenied(org.springframework.security.access.AccessDeniedException e) {
        return Result.fail(ResultCode.FORBIDDEN);
    }

    /** 兜底：未预期的系统异常，error 级别 + 通用提示（不向前端泄露堆栈）。 */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleOther(Exception e) {
        log.error("系统异常", e);
        return Result.fail(ResultCode.SYSTEM_ERROR);
    }
}
