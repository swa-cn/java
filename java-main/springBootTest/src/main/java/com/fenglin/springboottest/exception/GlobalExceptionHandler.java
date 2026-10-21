package com.fenglin.springboottest.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 把 AuthException 统一转成 { error, message } 响应体，状态码随异常。
 * 前端 api/auth.js 的 httpPost 在 !res.ok 时读取 data.error / data.message。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<Map<String, Object>> handleAuth(AuthException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", ex.getError());
        body.put("message", ex.getMessage());
        return ResponseEntity.status(ex.getHttpStatus()).body(body);
    }
}
