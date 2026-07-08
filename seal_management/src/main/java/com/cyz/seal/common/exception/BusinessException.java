package com.cyz.seal.common.exception;

import com.cyz.seal.common.result.ResultCode;
import lombok.Getter;

/**
 * 业务异常：抛出后由全局异常处理器转为统一响应（HTTP 200，body 中 code 非 200）。
 */
@Getter
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(String message) {
        super(message);
        this.code = ResultCode.BUSINESS_ERROR.getCode();
    }

    public BusinessException(ResultCode rc) {
        super(rc.getMessage());
        this.code = rc.getCode();
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
}
