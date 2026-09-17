package com.example.lipsticks.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 统一错误码枚举。
 * <p>
 * 每个错误码包含：
 * <ul>
 *   <li>code —— 机器可读的唯一标识（前端可据此精确区分错误类型）</li>
 *   <li>defaultMessage —— 默认中文错误提示</li>
 *   <li>httpStatus —— 对应的 HTTP 状态码</li>
 * </ul>
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {

    // ==================== 认证相关 AUTH_1xxx ====================
    USERNAME_EXISTS("AUTH_1001", "用户名已存在", HttpStatus.CONFLICT),
    BAD_CREDENTIALS("AUTH_1002", "用户名或密码错误", HttpStatus.UNAUTHORIZED),
    ACCOUNT_DISABLED("AUTH_1003", "账号已被禁用", HttpStatus.FORBIDDEN),
    OLD_PASSWORD_WRONG("AUTH_1004", "原密码不正确", HttpStatus.BAD_REQUEST),

    // ==================== 资源相关 RES_2xxx ====================
    PRODUCT_NOT_FOUND("RES_2001", "商品不存在或已下架", HttpStatus.NOT_FOUND),
    USER_NOT_FOUND("RES_2002", "用户不存在", HttpStatus.NOT_FOUND),

    // ==================== 参数校验 VAL_3xxx ====================
    VALIDATION_FAILED("VAL_3001", "参数校验失败", HttpStatus.BAD_REQUEST),
    INVALID_ROLE("VAL_3002", "角色只能是 USER 或 ADMIN", HttpStatus.BAD_REQUEST),

    // ==================== AI 服务异常 AI_4xxx ====================
    AI_TIMEOUT("AI_4001", "AI 推荐服务响应超时，请稍后重试", HttpStatus.SERVICE_UNAVAILABLE),
    AI_RATE_LIMITED("AI_4002", "AI 推荐服务繁忙，请稍后重试", HttpStatus.SERVICE_UNAVAILABLE),
    AI_ERROR("AI_4003", "AI 推荐服务暂时不可用", HttpStatus.SERVICE_UNAVAILABLE),
    AI_INVALID_RESPONSE("AI_4004", "AI 推荐服务返回异常，请重试", HttpStatus.BAD_GATEWAY),

    // ==================== 图片处理 IMG_5xxx ====================
    IMAGE_READ_FAILED("IMG_5001", "无法读取图片文件，请上传有效的 JPG/PNG 图片", HttpStatus.BAD_REQUEST),
    IMAGE_PROCESS_FAILED("IMG_5002", "图片处理失败，请重试", HttpStatus.INTERNAL_SERVER_ERROR),

    // ==================== 限流 RATE_6xxx ====================
    RATE_LIMITED("RATE_6001", "请求过于频繁，请稍后再试", HttpStatus.TOO_MANY_REQUESTS),

    // ==================== 订单/支付相关 ORD_7xxx ====================
    ORDER_NOT_FOUND("ORD_7001", "订单不存在", HttpStatus.NOT_FOUND),
    INSUFFICIENT_STOCK("ORD_7002", "商品库存不足", HttpStatus.BAD_REQUEST),
    PAYMENT_NOT_FOUND("ORD_7003", "支付记录不存在", HttpStatus.NOT_FOUND),
    PAYMENT_ALREADY_PROCESSED("ORD_7004", "该支付已处理", HttpStatus.CONFLICT),
    PAYMENT_METHOD_NOT_SUPPORTED("ORD_7005", "不支持的支付方式", HttpStatus.BAD_REQUEST),

    // ==================== 通用 SYS_9xxx ====================
    INTERNAL_ERROR("SYS_9001", "系统内部错误", HttpStatus.INTERNAL_SERVER_ERROR),
    REQUEST_BODY_ERROR("SYS_9002", "请求体格式错误，请检查 JSON 格式", HttpStatus.BAD_REQUEST),
    DATA_CONFLICT("SYS_9003", "数据操作冲突，请重试", HttpStatus.CONFLICT);

    /** 机器可读的错误码，前端精确判断用 */
    private final String code;
    /** 默认中文用户提示 */
    private final String defaultMessage;
    /** 对应的 HTTP 状态码 */
    private final HttpStatus httpStatus;
}
