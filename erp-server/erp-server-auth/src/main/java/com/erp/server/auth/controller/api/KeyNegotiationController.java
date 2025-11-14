package com.erp.server.auth.controller.api;

import com.alibaba.fastjson.JSON;
import com.common.core.controller.vo.ApiResult;
import com.common.core.utils.IdUtils;
import com.erp.model.sys.dto.KeyRegistrationRequestDTO;
import com.erp.model.sys.dto.KeyRegistrationResponseDTO;
import com.erp.server.auth.server.SsoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * <p>
 * 会话密钥协商控制器
 * </p>
 *
 * @author wuhaotian
 * @since 2025-09-18
 */
@RestController
@RequestMapping("/key")
@Slf4j
public class KeyNegotiationController {

    @Resource
    private SsoService ssoService;

    /**
     * 会话密钥注册接口
     *
     * @param request 密钥注册请求
     * @param httpRequest HTTP请求
     * @return 密钥注册响应
     */
    @PostMapping("/register")
    public ApiResult<KeyRegistrationResponseDTO> registerKey(@Valid @RequestBody KeyRegistrationRequestDTO request, HttpServletRequest httpRequest) {
        log.info("会话密钥注册请求：{}", JSON.toJSONString(request));

        try {
            // 获取请求头信息
            String appId = httpRequest.getHeader("App-Id");

            if (StringUtils.isBlank(appId)) {
                return ApiResult.error(400, "请求头App-Id不能为空");
            }

            // 后端生成Session-Id用于标识会话密钥存储位置
            String sessionId = generateSessionId();
            log.info("生成Session-Id：{}", sessionId);

            // 调用密钥注册服务，userId将从payload中解密获取
            KeyRegistrationResponseDTO response = ssoService.registerKey(request, appId, sessionId);

            if (response.getSuccess()) {
                log.info("密钥注册成功，App-Id：{}，Session-Id：{}", appId, sessionId);
                return ApiResult.success(response);
            } else {
                log.warn("密钥注册失败：{}", response.getErrorMessage());
                return ApiResult.error(400, response.getErrorMessage());
            }

        } catch (Exception e) {
            log.error("密钥注册异常", e);
            return ApiResult.error(500, "密钥注册失败：" + e.getMessage());
        }
    }

    /**
     * 生成Session-Id
     * 用于标识会话密钥在Redis中的存储位置
     *
     * @return Session-Id
     */
    private String generateSessionId() {
        // 使用UUID生成唯一的会话标识
        return "session_" + IdUtils.fastUUID();
    }
}
