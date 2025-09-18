package com.erp.model.sys.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 单点登录响应DTO
 * </p>
 *
 * @author wuhaotian
 * @since 2025-09-18
 */
@Data
public class SsoLoginResponseDTO implements Serializable {

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
     * JWT Token
     */
    private String token;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 应用ID
     */
    private String appId;

    /**
     * 权限路径列表
     */
    private String[] pathList;

    /**
     * 签名会话ID（后端生成，用于标识会话密钥存储位置）
     */
    private String signSessionId;

    /**
     * 创建成功响应
     */
    public static SsoLoginResponseDTO success(String token, String userId, String appId, String[] pathList, String signSessionId) {
        SsoLoginResponseDTO response = new SsoLoginResponseDTO();
        response.setSuccess(true);
        response.setToken(token);
        response.setUserId(userId);
        response.setAppId(appId);
        response.setPathList(pathList);
        response.setSignSessionId(signSessionId);
        return response;
    }

    /**
     * 创建失败响应
     */
    public static SsoLoginResponseDTO error(String errorCode, String errorMessage) {
        SsoLoginResponseDTO response = new SsoLoginResponseDTO();
        response.setSuccess(false);
        response.setErrorCode(errorCode);
        response.setErrorMessage(errorMessage);
        return response;
    }
}
