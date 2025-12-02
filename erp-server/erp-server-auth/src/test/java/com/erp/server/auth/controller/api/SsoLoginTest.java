package com.erp.server.auth.controller.api;

import com.alibaba.fastjson.JSON;
import com.erp.model.sys.dto.SsoLoginRequestDTO;
import com.erp.model.sys.dto.SsoLoginResponseDTO;
import com.erp.server.auth.server.SsoService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.annotation.Resource;

/**
 * <p>
 * 单点登录测试类
 * </p>
 *
 * @author wuhaotian
 * @since 2025-09-18
 */
@SpringBootTest
@ActiveProfiles("test")
public class SsoLoginTest {

    @Resource
    private SsoService ssoService;

    @Test
    public void testSsoLogin() {
        // 创建测试请求
        SsoLoginRequestDTO request = new SsoLoginRequestDTO();
        request.setEncryptedPayload("test_encrypted_payload");

        // 调用单点登录服务（signSessionId由后端生成）
        SsoLoginResponseDTO response = ssoService.ssoLogin(request, "feishu_app_001", "sign_session_test_123456");

        // 打印结果
        System.out.println("单点登录测试结果：");
        System.out.println(JSON.toJSONString(response, true));
    }

    @Test
    public void testSsoController() {
        // 这里可以添加对SsoController的集成测试
        System.out.println("SsoController测试 - 接口地址：/sso/login");
    }
}
