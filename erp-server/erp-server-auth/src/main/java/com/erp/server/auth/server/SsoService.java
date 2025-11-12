package com.erp.server.auth.server;

import com.erp.model.sys.dto.KeyRegistrationRequestDTO;
import com.erp.model.sys.dto.KeyRegistrationResponseDTO;
import com.erp.model.sys.dto.SsoLoginRequestDTO;
import com.erp.model.sys.dto.SsoLoginResponseDTO;

/**
 * <p>
 * 单点登录服务接口
 * </p>
 *
 * @author wuhaotian
 * @since 2025-09-18
 */
public interface SsoService {

    /**
     * 单点登录
     *
     * @param request   单点登录请求
     * @param appId     应用ID
     * @param sessionId 会话ID
     * @return 单点登录响应
     */
    SsoLoginResponseDTO ssoLogin(SsoLoginRequestDTO request, String appId, String sessionId);

    /**
     * 会话密钥注册
     *
     * @param request 密钥注册请求
     * @param appId 应用ID
     * @param sessionId 会话ID
     * @return 密钥注册响应
     */
    KeyRegistrationResponseDTO registerKey(KeyRegistrationRequestDTO request, String appId, String sessionId);
}
