package com.example.lipsticks.common.exception;

import com.example.lipsticks.common.api.ApiResponse;
import com.example.lipsticks.common.api.ValidationError;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 全局异常处理切面。
 * <p>
 * 统一拦截所有 Controller 抛出的异常，按类型分发，
 * 返回带有正确 HTTP 状态码和 errorCode 的 {@link ApiResponse}。
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // ==================== 业务异常 ====================

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        ErrorCode errorCode = e.getErrorCode();

        log.warn("业务异常: code={}, httpStatus={}, message={}, detail={}",
                errorCode.getCode(), errorCode.getHttpStatus().value(),
                e.getMessage(), e.getDetail());

        String userMessage = e.getDetail() != null ? e.getDetail() : e.getMessage();
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ApiResponse.fail(userMessage, errorCode.getCode()));
    }

    // ==================== JSR-303 参数校验失败 ====================

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<List<ValidationError>>> handleValidation(
            MethodArgumentNotValidException e) {

        List<ValidationError> errors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fe -> new ValidationError(fe.getField(), fe.getDefaultMessage()))
                .toList();

        String joined = errors.stream()
                .map(v -> v.getField() + ": " + v.getMessage())
                .collect(Collectors.joining("; "));

        log.warn("参数校验失败: {}", joined);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail("参数校验失败: " + joined,
                        ErrorCode.VALIDATION_FAILED.getCode(), errors));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(ConstraintViolationException e) {
        log.warn("约束校验失败: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail("参数校验失败: " + e.getMessage(),
                        ErrorCode.VALIDATION_FAILED.getCode()));
    }

    // ==================== 缺少必需参数 ====================

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingParam(MissingServletRequestParameterException e) {
        log.warn("缺少必需参数: {}", e.getParameterName());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail("缺少必需参数: " + e.getParameterName(),
                        ErrorCode.VALIDATION_FAILED.getCode()));
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingPart(MissingServletRequestPartException e) {
        log.warn("缺少文件上传: {}", e.getRequestPartName());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail("缺少文件上传: " + e.getRequestPartName(),
                        ErrorCode.VALIDATION_FAILED.getCode()));
    }

    // ==================== 请求体格式错误 ====================

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleMessageNotReadable(HttpMessageNotReadableException e) {
        log.warn("请求体格式错误: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail("请求体格式错误，请检查 JSON 格式",
                        ErrorCode.REQUEST_BODY_ERROR.getCode()));
    }

    // ==================== Spring Security 权限异常 ====================

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException e) {
        log.warn("权限不足: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.fail("权限不足", null));
    }

    // ==================== 数据库约束冲突 ====================

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrity(DataIntegrityViolationException e) {
        log.error("数据库约束冲突", e);

        Throwable root = e.getRootCause();
        String message;
        if (root != null && root.getMessage() != null
                && root.getMessage().contains("Duplicate entry")) {
            message = "数据已存在，请勿重复提交";
        } else {
            message = "数据操作冲突，请重试";
        }

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.fail(message, ErrorCode.DATA_CONFLICT.getCode()));
    }

    // ==================== 未知异常兜底 ====================

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnknown(Exception e) {
        log.error("未捕获的系统异常", e);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.fail("系统内部错误", ErrorCode.INTERNAL_ERROR.getCode()));
    }
}
