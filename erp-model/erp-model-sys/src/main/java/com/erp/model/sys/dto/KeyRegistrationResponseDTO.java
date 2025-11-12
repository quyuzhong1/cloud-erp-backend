package com.erp.model.sys.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * <p>
 * 会话密钥协商响应DTO
 * </p>
 *
 * @author wuhaotian
 * @since 2025-09-18
 */
@Data
@NoArgsConstructor
public class KeyRegistrationResponseDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 错误码
     */
    private String errorCode;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 会话ID（后端生成，用于标识会话密钥存储位置）
     */
    private String sessionId;

    /**
     * 创建成功响应
     */
    public static KeyRegistrationResponseDTO success(String sessionId) {
        KeyRegistrationResponseDTO response = new KeyRegistrationResponseDTO();
        response.setSuccess(true);
        response.setSessionId(sessionId);
        return response;
    }

    /**
     * 创建失败响应
     */
    public static KeyRegistrationResponseDTO error(String errorCode, String errorMessage) {
        KeyRegistrationResponseDTO response = new KeyRegistrationResponseDTO();
        response.setSuccess(false);
        response.setErrorCode(errorCode);
        response.setErrorMessage(errorMessage);
        return response;
    }
}
