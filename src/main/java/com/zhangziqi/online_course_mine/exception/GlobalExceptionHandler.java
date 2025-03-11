package com.zhangziqi.online_course_mine.exception;

import com.zhangziqi.online_course_mine.model.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, String>> handleBusinessException(BusinessException e) {
        log.error("业务异常: {}", e.getMessage());
        Map<String, String> response = new HashMap<>();
        response.put("error", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 处理认证异常
     */
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(AuthenticationException.class)
    public Result<Void> handleAuthenticationException(AuthenticationException e) {
        log.error("认证异常: {}", e.getMessage());
        String message = e instanceof BadCredentialsException ? "用户名或密码错误" : "认证失败";
        return Result.fail(401, message);
    }

    /**
     * 处理授权异常
     */
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(AccessDeniedException.class)
    public Result<Void> handleAccessDeniedException(AccessDeniedException e) {
        log.error("权限不足: {}", e.getMessage());
        return Result.fail(403, "权限不足");
    }

    /**
     * 处理参数校验异常
     */
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public Result<Void> handleValidationException(Exception e) {
        List<FieldError> fieldErrors;
        if (e instanceof MethodArgumentNotValidException) {
            fieldErrors = ((MethodArgumentNotValidException) e).getBindingResult().getFieldErrors();
        } else {
            fieldErrors = ((BindException) e).getBindingResult().getFieldErrors();
        }

        String message = fieldErrors.stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.error("参数校验异常: {}", message);
        return Result.fail(400, message);
    }

    /**
     * 处理其他异常
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.fail(500, "系统异常，请联系管理员");
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleResourceNotFoundException(ResourceNotFoundException e) {
        log.error("资源未找到: {}", e.getMessage());
        Map<String, String> response = new HashMap<>();
        response.put("error", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleBadCredentialsException(BadCredentialsException e) {
        log.error("认证失败: {}", e.getMessage());
        Map<String, String> response = new HashMap<>();
        response.put("error", "用户名或密码错误");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }
} 