package com.fenglin.springboottest.exception;

/**
 * 认证业务异常。携带「错误字段(error)」和 HTTP 状态码，
 * 由 GlobalExceptionHandler 统一转成 { error, message } 响应体。
 */
public class AuthException extends RuntimeException {

    /** 错误字段：username / email / password / code / notfound / wrongpw */
    private final String error;
    private final int httpStatus;

    public AuthException(String error, String message) {
        this(error, message, 400);
    }

    public AuthException(String error, String message, int httpStatus) {
        super(message);
        this.error = error;
        this.httpStatus = httpStatus;
    }

    public String getError() { return error; }
    public int getHttpStatus() { return httpStatus; }
}
