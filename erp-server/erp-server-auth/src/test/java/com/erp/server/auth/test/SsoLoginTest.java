package com.erp.server.auth.test;

import com.alibaba.fastjson.JSON;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.SignTypeEnum;
import com.common.core.utils.RsaEncryptUtil;
import com.erp.model.sys.dto.SsoLoginRequestDTO;
import com.erp.model.sys.dto.SsoLoginResponseDTO;
import com.erp.model.sys.enums.AppTypeEnum;
import com.erp.server.auth.ErpServerAuthApplication;
import com.erp.server.auth.controller.api.SsoController;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.security.KeyPair;
import java.util.Map;

/**
 * 单点登录测试类
 *
 * @author wuhaotian
 * @since 2025-09-18
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ErpServerAuthApplication.class)
public class SsoLoginTest {

    @Resource
    private SsoController ssoController;

    /**
     * 测试飞书单点登录
     */
    @Test
    public void testFeishuSsoLogin() {
        // 1. 生成测试密钥对
        KeyPair keyPair = SsoLoginTestHelper.generateTestKeyPair();
        String publicKey = RsaEncryptUtil.publicKeyToBase64(keyPair.getPublic());
        String privateKey = RsaEncryptUtil.privateKeyToBase64(keyPair.getPrivate());
        
        // 2. 生成测试数据
        String appId = SsoLoginTestHelper.generateTestAppId();
        String unionId = SsoLoginTestHelper.generateTestUnionId();
        
        // 3. 生成请求参数
        SsoLoginRequestDTO request = SsoLoginTestHelper.generateFeishuSsoRequest(unionId, publicKey);
        
        // 4. 打印测试信息
        SsoLoginTestHelper.printTestInfo(keyPair, request, appId);
        
        // 5. 验证解密
        boolean isValid = SsoLoginTestHelper.verifyDecryption(
            request.getEncryptedPayload(), 
            privateKey, 
            AppTypeEnum.FS.getCode(), 
            unionId
        );
        log.info("解密验证结果: {}", isValid);
        
        // 注意：实际调用需要先配置数据库中的sys_referer_config表
        // 和sys_user_third表中的用户绑定关系
        log.info("如需实际测试，请先配置数据库中的相关数据");
    }

    /**
     * 测试PDA单点登录
     */
    @Test
    public void testPdaSsoLogin() {
        KeyPair keyPair = SsoLoginTestHelper.generateTestKeyPair();
        String publicKey = RsaEncryptUtil.publicKeyToBase64(keyPair.getPublic());
        String appId = SsoLoginTestHelper.generateTestAppId();
        String unionId = SsoLoginTestHelper.generateTestUnionId();
        
        SsoLoginRequestDTO request = SsoLoginTestHelper.generatePdaSsoRequest(unionId, publicKey);
        SsoLoginTestHelper.printTestInfo(keyPair, request, appId);
        
        log.info("PDA单点登录测试参数已生成");
    }

    /**
     * 测试微信小程序单点登录
     */
    @Test
    public void testWechatSsoLogin() {
        KeyPair keyPair = SsoLoginTestHelper.generateTestKeyPair();
        String publicKey = RsaEncryptUtil.publicKeyToBase64(keyPair.getPublic());
        String appId = SsoLoginTestHelper.generateTestAppId();
        String unionId = SsoLoginTestHelper.generateTestUnionId();
        
        SsoLoginRequestDTO request = SsoLoginTestHelper.generateWechatSsoRequest(unionId, publicKey);
        SsoLoginTestHelper.printTestInfo(keyPair, request, appId);
        
        log.info("微信小程序单点登录测试参数已生成");
    }

    /**
     * 测试ERP系统单点登录
     */
    @Test
    public void testErpSsoLogin() {
        KeyPair keyPair = SsoLoginTestHelper.generateTestKeyPair();
        String publicKey = RsaEncryptUtil.publicKeyToBase64(keyPair.getPublic());
        String appId = SsoLoginTestHelper.generateTestAppId();
        String unionId = SsoLoginTestHelper.generateTestUnionId();
        
        SsoLoginRequestDTO request = SsoLoginTestHelper.generateErpSsoRequest(unionId, publicKey);
        SsoLoginTestHelper.printTestInfo(keyPair, request, appId);
        
        log.info("ERP系统单点登录测试参数已生成");
    }

    /**
     * 测试所有应用类型的单点登录
     */
    @Test
    public void testAllAppTypesSsoLogin() {
        String publicKey =RsaEncryptUtil.extractPublicKeyFromPem("-----BEGIN PUBLIC KEY-----\n" +
                "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA5nMJXoFdk0GoK7fnQa9f\n" +
                "ye2NTdrSHfce/GBV10E30FqgiIVFY+R/2HnlBoaZmdF72la2whT3VYtrx9jDkOZX\n" +
                "gakXRf7IAWl1sfJLARMQkpUlwNPwbKN1Ahay95pFeoyxfev5pF69O0VkxeDmw+qm\n" +
                "xSFyC448QWx4ISZMwvzj+j32wbemt+yoBEAojcy4cxO8oIg2D4Pkh2a30JzpgyCr\n" +
                "+MiDgkfM/vL2d81ndxtQW7YZfO3893NGzxT8+TbgwsDVIFIaU5sUZ2rLBch/XUHu\n" +
                "/0RE2KiaF4Kcj0ojlfvKG//W/bX0OGOy5QnF9FXQhAFF71jWNkAkf/CEjD6u1YS+\n" +
                "gQIDAQAB\n" +
                "-----END PUBLIC KEY-----\n");
        String privateKey = RsaEncryptUtil.extractPrivateKeyFromPem("-----BEGIN PRIVATE KEY-----\n" +
                "MIIEwAIBADANBgkqhkiG9w0BAQEFAASCBKowggSmAgEAAoIBAQDmcwlegV2TQagr\n" +
                "t+dBr1/J7Y1N2tId9x78YFXXQTfQWqCIhUVj5H/YeeUGhpmZ0XvaVrbCFPdVi2vH\n" +
                "2MOQ5leBqRdF/sgBaXWx8ksBExCSlSXA0/Bso3UCFrL3mkV6jLF96/mkXr07RWTF\n" +
                "4ObD6qbFIXILjjxBbHghJkzC/OP6PfbBt6a37KgEQCiNzLhzE7ygiDYPg+SHZrfQ\n" +
                "nOmDIKv4yIOCR8z+8vZ3zWd3G1Bbthl87fz3c0bPFPz5NuDCwNUgUhpTmxRnassF\n" +
                "yH9dQe7/RETYqJoXgpyPSiOV+8ob/9b9tfQ4Y7LlCcX0VdCEAUXvWNY2QCR/8ISM\n" +
                "Pq7VhL6BAgMBAAECggEBAJkJmBU8j+n01juCO38xecldilNDl/nyXI95Ykg6tVgW\n" +
                "dzlyV6yI2CXofSGiTWsK5NcQcvncdoxGtGLZHrahYUdoBV6kXioh4Nk1cH3cLUtK\n" +
                "5PFzTdy3iELE1ED+IFnMCSr4hKaVi9FKuit2uYkt0WZkWZo28Oj5oaVSC/QJgVRy\n" +
                "guTXT8GOjxq71Jz+kiKehO9LelAmlaNZFLlRudAEgtjig90aVeOflayqM2/T1on+\n" +
                "HIGZRe3JpjvtsSUNpXC6eZGjwOqhE2kE7xZU7huEy31gm7gVAGfqK1DuZCjtYz2X\n" +
                "iPme344AaLWnfaM9GC5yEItTAY0wsj8IwkbwXtEmogkCgYEA/1Zvc99tJsVgVUU0\n" +
                "gHkicXDdYqjAhoU1T78Kgad5Q+FHnb6o5rBEpSyNosd90LMcC6pSaN8ar1EBoLJs\n" +
                "C+jwg7gsYmmz2LJeqsSyoyXOMi9ZwWyZkjrXekA6NsKGjoRKkLGJdOSz2qbnqJ0o\n" +
                "gu5vZxEEr3NhYrh5RiM9o+w5d/8CgYEA5wwSzCy6RlW2/ul5b9xjGlSpxPIIqM42\n" +
                "9XKxtxpa4ywMKETrYmhjg2tpvgP69npITUfBr3bNkxJW/4L/JM9GB0q6qUbdHaYy\n" +
                "73L0HjX/8t4UXSMv7qVfn9Z4LzoaWobPaUcCny3aglckh8pI9sp7y5ErklflqnPR\n" +
                "ETbKclI1yX8CgYEAlWYzrC7MpOTT6tClWY6HEq0hO/rVO2Bxwwla0mX1VDxaNPg7\n" +
                "x07Xu3WIIfhrq1NBU6oCsg3ohS/2rdn30jgnkkqO3b5PVrl3HqAcjc0vCrod4Ej0\n" +
                "ylTXg1ZVOXk0Vesd3e+jJ9wxR/xki4n5DDMKx9lzTl1KXALoIpu4E1PChG0CgYEA\n" +
                "2vG9/m2EN5KSTw3AXhhicVk5Z8BUJGMGAaImdnUrG4tqGd71j9OMOcVX9xCeiWNn\n" +
                "kJQnHrdeJ7iBaLAiSCeDHPXb3P2iG66bJf2xB8/0M5nvgjMOAQAFldoPGfHdd1u4\n" +
                "wPLhsSYqoskhfOxcKEHI/icbOxrAML5/p+hb/+mXvtECgYEApcz/FjUAoBwFu5PN\n" +
                "KdvNiXOjZIGX7GSe8KQAa55jk41m4+Pmcvrzfqwbn0DxVEtaHO7gCp+QafN+s+3p\n" +
                "M6l8kOtX6e5xXAUnpEQCGUujDZt9lPI9b5rHhBTvemrX3GcxUSe38dWwCRvTjQhM\n" +
                "2eBeCEugExVC46nsWv1XtiAiO8o=\n" +
                "-----END PRIVATE KEY-----\n");
        String appId = "8962cfaa0ba74c5db34988a6cbeae0de";
        String unionId = "on_3bbb75fc9a3d428a8a7add17d81d66f8";
        log.info("=== 测试所有应用类型的单点登录 ===");

        AppTypeEnum appType= AppTypeEnum.FS;
        SsoLoginRequestDTO request = SsoLoginTestHelper.generateSsoLoginRequest(
                appType.getCode(), unionId, "test_key_" + System.currentTimeMillis(), publicKey);

        log.info("应用类型: {} ({})", appType.getDescription(), appType.getCode());
        log.info("UnionId: {}", unionId);
        log.info("加密Payload: {}", request.getEncryptedPayload());

        // 验证解密
        boolean isValid = SsoLoginTestHelper.verifyDecryption(
                request.getEncryptedPayload(), privateKey, appType.getCode(), unionId);
        log.info("解密验证: {}", isValid ? "成功" : "失败");
        log.info("---");
    }

    /**
     * 测试API签名生成
     */
    @Test
    public void testApiSignatureGeneration() {
        // 1. 生成测试数据
        KeyPair keyPair = SsoLoginTestHelper.generateTestKeyPair();
        String publicKey = RsaEncryptUtil.publicKeyToBase64(keyPair.getPublic());
        String appId = SsoLoginTestHelper.generateTestAppId();
        String unionId = SsoLoginTestHelper.generateTestUnionId();
        
        // 2. 生成单点登录请求
        SsoLoginRequestDTO request = SsoLoginTestHelper.generateFeishuSsoRequest(unionId, publicKey);
        String requestBody = JSON.toJSONString(request);
        
        // 3. 测试密钥和签名类型
        String secretKey = "test_secret_key_123456";
        
        // 4. 测试AES签名
        log.info("=== 测试AES签名 ===");
        SsoLoginTestHelper.printApiSignatureTestInfo("POST", "/sso/login", requestBody, secretKey, SignTypeEnum.AES.getCode());
        
        // 5. 测试HMAC签名
        log.info("=== 测试HMAC签名 ===");
        SsoLoginTestHelper.printApiSignatureTestInfo("POST", "/sso/login", requestBody, secretKey, SignTypeEnum.HMAC.getCode());
        
        // 6. 测试带查询参数的签名
        log.info("=== 测试带查询参数的签名 ===");
        String signatureWithQuery = SsoLoginTestHelper.generateApiSignatureWithQuery(
            "GET", "/sso/status", "", secretKey, SignTypeEnum.AES.getCode(), 
            "signSessionId", "test_session_123", "appId", appId);
        log.info("带查询参数的API-Signature: {}", signatureWithQuery);
        
        // 7. 生成完整的请求头
        Map<String, String> headers = SsoLoginTestHelper.generateSsoRequestHeaders(requestBody, appId, secretKey, SignTypeEnum.AES.getCode());
        log.info("=== 完整的请求头 ===");
        headers.forEach((key, value) -> log.info("{}: {}", key, value));
    }

    /**
     * 测试不同HTTP方法的API签名
     */
    @Test
    public void testDifferentHttpMethods() {
        String secretKey = "test_secret_key_123456";
        String requestBody = "{\"test\":\"data\"}";
        
        // 测试GET请求
        SsoLoginTestHelper.printApiSignatureTestInfo("GET", "/api/users", "", secretKey, SignTypeEnum.AES.getCode());
        
        // 测试POST请求
        SsoLoginTestHelper.printApiSignatureTestInfo("POST", "/api/users", requestBody, secretKey, SignTypeEnum.AES.getCode());
        
        // 测试PUT请求
        SsoLoginTestHelper.printApiSignatureTestInfo("PUT", "/api/users/123", requestBody, secretKey, SignTypeEnum.AES.getCode());
        
        // 测试DELETE请求
        SsoLoginTestHelper.printApiSignatureTestInfo("DELETE", "/api/users/123", "", secretKey, SignTypeEnum.AES.getCode());
    }

    /**
     * 测试API签名验证
     */
    @Test
    public void testApiSignatureVerification() {
        String secretKey = "test_secret_key_123456";
        String requestBody = "{\"test\":\"data\"}";
        String httpMethod = "POST";
        String uri = "/api/test";
        
        // 生成签名
        String signatureHeader = SsoLoginTestHelper.generateApiSignature(httpMethod, uri, requestBody, null, secretKey, SignTypeEnum.AES.getCode());
        log.info("生成的签名头: {}", signatureHeader);
        
        // 解析签名
        String[] parsed = SsoLoginTestHelper.parseApiSignature(signatureHeader);
        if (parsed != null && parsed.length == 2) {
            String timestamp = parsed[0];
            String signature = parsed[1];
            
            // 验证签名
            boolean isValid = SsoLoginTestHelper.verifyApiSignature(httpMethod, uri, requestBody, null, secretKey, SignTypeEnum.AES.getCode(), 
                                                                   signature, Long.parseLong(timestamp));
            log.info("签名验证结果: {}", isValid ? "成功" : "失败");
            
            // 测试错误的签名
            boolean isInvalid = SsoLoginTestHelper.verifyApiSignature(httpMethod, uri, requestBody, null, "wrong_key", SignTypeEnum.AES.getCode(), 
                                                                     signature, Long.parseLong(timestamp));
            log.info("错误密钥验证结果: {}", isInvalid ? "成功" : "失败");
        }
    }

    /**
     * 测试SsoController#login接口 - 飞书应用SSO登录
     * 根据时序图实现的完整测试方法
     */
    @Test
    public void testSsoControllerLogin() {
        log.info("=== 开始测试SsoController#login接口 ===");
        
        // 1. 生成测试密钥对 - 使用正确的PKCS#8格式
        String publicKey = RsaEncryptUtil.extractPublicKeyFromPem("-----BEGIN PUBLIC KEY-----\n" +
                "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA5nMJXoFdk0GoK7fnQa9f\n" +
                "ye2NTdrSHfce/GBV10E30FqgiIVFY+R/2HnlBoaZmdF72la2whT3VYtrx9jDkOZX\n" +
                "gakXRf7IAWl1sfJLARMQkpUlwNPwbKN1Ahay95pFeoyxfev5pF69O0VkxeDmw+qm\n" +
                "xSFyC448QWx4ISZMwvzj+j32wbemt+yoBEAojcy4cxO8oIg2D4Pkh2a30JzpgyCr\n" +
                "+MiDgkfM/vL2d81ndxtQW7YZfO3893NGzxT8+TbgwsDVIFIaU5sUZ2rLBch/XUHu\n" +
                "/0RE2KiaF4Kcj0ojlfvKG//W/bX0OGOy5QnF9FXQhAFF71jWNkAkf/CEjD6u1YS+\n" +
                "gQIDAQAB\n" +
                "-----END PUBLIC KEY-----\n");
        // 使用PKCS#8格式的私钥
        String privateKey = RsaEncryptUtil.extractPrivateKeyFromPem("-----BEGIN PRIVATE KEY-----\n" +
                "MIIEwAIBADANBgkqhkiG9w0BAQEFAASCBKowggSmAgEAAoIBAQDmcwlegV2TQagr\n" +
                "t+dBr1/J7Y1N2tId9x78YFXXQTfQWqCIhUVj5H/YeeUGhpmZ0XvaVrbCFPdVi2vH\n" +
                "2MOQ5leBqRdF/sgBaXWx8ksBExCSlSXA0/Bso3UCFrL3mkV6jLF96/mkXr07RWTF\n" +
                "4ObD6qbFIXILjjxBbHghJkzC/OP6PfbBt6a37KgEQCiNzLhzE7ygiDYPg+SHZrfQ\n" +
                "nOmDIKv4yIOCR8z+8vZ3zWd3G1Bbthl87fz3c0bPFPz5NuDCwNUgUhpTmxRnassF\n" +
                "yH9dQe7/RETYqJoXgpyPSiOV+8ob/9b9tfQ4Y7LlCcX0VdCEAUXvWNY2QCR/8ISM\n" +
                "Pq7VhL6BAgMBAAECggEBAJkJmBU8j+n01juCO38xecldilNDl/nyXI95Ykg6tVgW\n" +
                "dzlyV6yI2CXofSGiTWsK5NcQcvncdoxGtGLZHrahYUdoBV6kXioh4Nk1cH3cLUtK\n" +
                "5PFzTdy3iELE1ED+IFnMCSr4hKaVi9FKuit2uYkt0WZkWZo28Oj5oaVSC/QJgVRy\n" +
                "guTXT8GOjxq71Jz+kiKehO9LelAmlaNZFLlRudAEgtjig90aVeOflayqM2/T1on+\n" +
                "HIGZRe3JpjvtsSUNpXC6eZGjwOqhE2kE7xZU7huEy31gm7gVAGfqK1DuZCjtYz2X\n" +
                "iPme344AaLWnfaM9GC5yEItTAY0wsj8IwkbwXtEmogkCgYEA/1Zvc99tJsVgVUU0\n" +
                "gHkicXDdYqjAhoU1T78Kgad5Q+FHnb6o5rBEpSyNosd90LMcC6pSaN8ar1EBoLJs\n" +
                "C+jwg7gsYmmz2LJeqsSyoyXOMi9ZwWyZkjrXekA6NsKGjoRKkLGJdOSz2qbnqJ0o\n" +
                "gu5vZxEEr3NhYrh5RiM9o+w5d/8CgYEA5wwSzCy6RlW2/ul5b9xjGlSpxPIIqM42\n" +
                "9XKxtxpa4ywMKETrYmhjg2tpvgP69npITUfBr3bNkxJW/4L/JM9GB0q6qUbdHaYy\n" +
                "73L0HjX/8t4UXSMv7qVfn9Z4LzoaWobPaUcCny3aglckh8pI9sp7y5ErklflqnPR\n" +
                "ETbKclI1yX8CgYEAlWYzrC7MpOTT6tClWY6HEq0hO/rVO2Bxwwla0mX1VDxaNPg7\n" +
                "x07Xu3WIIfhrq1NBU6oCsg3ohS/2rdn30jgnkkqO3b5PVrl3HqAcjc0vCrod4Ej0\n" +
                "ylTXg1ZVOXk0Vesd3e+jJ9wxR/xki4n5DDMKx9lzTl1KXALoIpu4E1PChG0CgYEA\n" +
                "2vG9/m2EN5KSTw3AXhhicVk5Z8BUJGMGAaImdnUrG4tqGd71j9OMOcVX9xCeiWNn\n" +
                "kJQnHrdeJ7iBaLAiSCeDHPXb3P2iG66bJf2xB8/0M5nvgjMOAQAFldoPGfHdd1u4\n" +
                "wPLhsSYqoskhfOxcKEHI/icbOxrAML5/p+hb/+mXvtECgYEApcz/FjUAoBwFu5PN\n" +
                "KdvNiXOjZIGX7GSe8KQAa55jk41m4+Pmcvrzfqwbn0DxVEtaHO7gCp+QafN+s+3p\n" +
                "M6l8kOtX6e5xXAUnpEQCGUujDZt9lPI9b5rHhBTvemrX3GcxUSe38dWwCRvTjQhM\n" +
                "2eBeCEugExVC46nsWv1XtiAiO8o=\n" +
                "-----END PRIVATE KEY-----\n");
        
        // 2. 生成测试数据
        String appId = "cli_a8467e05407c1013"; // 飞书应用ID
        String unionId = "on_d1c5059823f5d4591ce4f539614c3585";
        String symmetricKey = "7dBX5zCFMhWtbAjhUzN1faENyG2ypP31";
        
        // 3. 构建payload（按照时序图步骤4）
        Map<String, Object> payload = new java.util.HashMap<>();
//        payload.put("appType", AppTypeEnum.FS.getCode()); // 飞书应用类型
        payload.put("unionId", unionId); // 飞书用户唯一标识
        payload.put("symmetricKey", symmetricKey); // 前端生成的对称密钥
        
        String payloadJson = JSON.toJSONString(payload);
        log.info("步骤4 - 构建payload: {}", payloadJson);
        
        // 4. 使用公钥加密payload（按照时序图步骤5）
        String encryptedPayload;
        try {
            encryptedPayload = RsaEncryptUtil.encrypt(payloadJson, publicKey);
            log.info("步骤5 - 使用公钥加密payload成功，加密后长度: {}", encryptedPayload.length());
        } catch (Exception e) {
            log.error("步骤5 - 加密payload失败", e);
            return;
        }
        
        // 5. 构建请求对象（按照时序图步骤6）
        SsoLoginRequestDTO request = new SsoLoginRequestDTO();
        request.setEncryptedPayload(encryptedPayload);
        
        log.info("步骤6 - 发送登录请求");
        log.info("请求参数: {}", JSON.toJSONString(request));
        log.info("请求头 App-Id: {}", appId);
        
        // 6. 验证解密功能（验证payload的正确性）
        boolean isValidPayload = SsoLoginTestHelper.verifyDecryption(
            encryptedPayload, privateKey, AppTypeEnum.FS.getCode(), unionId);
        log.info("Payload解密验证: {}", isValidPayload ? "成功" : "失败");
        
        // 7. 模拟调用SsoController#login接口
        try {
            // 创建模拟的HttpServletRequest
            javax.servlet.http.HttpServletRequest mockRequest = createMockHttpServletRequest(appId);
            
            // 调用接口
            log.info("步骤7 - 调用SsoController#login接口");
            ApiResult<SsoLoginResponseDTO> result = ssoController.login(request, mockRequest);
            
            // 8. 验证响应结果
            log.info("接口调用结果: {}", JSON.toJSONString(result));
            
            if (result.getCode() == 200 && result.getData() != null) {
                SsoLoginResponseDTO response = result.getData();
                log.info("步骤16 - 返回JWT Token");
                log.info("登录成功 - 用户ID: {}, 应用ID: {}, Token: {}", 
                        response.getUserId(), response.getAppId(), 
                        response.getToken() != null ? "已生成" : "未生成");
                log.info("权限路径: {}", java.util.Arrays.toString(response.getPathList()));
                log.info("签名会话ID: {}", response.getSignSessionId());
                
                // 验证JWT Token格式
                if (response.getToken() != null && response.getToken().contains(".")) {
                    log.info("JWT Token格式验证: 通过");
                } else {
                    log.warn("JWT Token格式验证: 失败");
                }
                
            } else {
                log.warn("登录失败 - 错误码: {}, 错误信息: {}", result.getCode(), result.getMsg());
                
                // 根据时序图，可能的错误情况：
                // 10a. 应用不存在
                // 12a. 用户未绑定ERP
                if (result.getMsg() != null) {
                    if (result.getMsg().contains("应用不存在") || result.getMsg().contains("应用")) {
                        log.info("步骤10a - 返回'应用不存在'");
                    } else if (result.getMsg().contains("用户未绑定") || result.getMsg().contains("绑定")) {
                        log.info("步骤12a - 返回'用户未绑定ERP'");
                    }
                }
            }
            
        } catch (Exception e) {
            log.error("调用SsoController#login接口异常", e);
        }
        
        log.info("=== SsoController#login接口测试完成 ===");
    }

    /**
     * 测试SsoController#login接口 - 不同场景测试
     * 测试应用不存在、用户未绑定、正常登录等场景
     */
    @Test
    public void testSsoControllerLoginScenarios() {
        log.info("=== 开始测试SsoController#login接口不同场景 ===");
        
        // 1. 测试应用不存在场景
        testAppNotExistScenario();
        
        // 2. 测试用户未绑定场景
        testUserNotBoundScenario();
        
        // 3. 测试正常登录场景
        testNormalLoginScenario();
        
        log.info("=== 所有场景测试完成 ===");
    }
    
    /**
     * 测试应用不存在场景
     */
    private void testAppNotExistScenario() {
        log.info("--- 测试场景1: 应用不存在 ---");
        
        // 使用相同的密钥对进行测试
        String publicKey = RsaEncryptUtil.extractPublicKeyFromPem("-----BEGIN PUBLIC KEY-----\n" +
                "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA5nMJXoFdk0GoK7fnQa9f\n" +
                "ye2NTdrSHfce/GBV10E30FqgiIVFY+R/2HnlBoaZmdF72la2whT3VYtrx9jDkOZX\n" +
                "gakXRf7IAWl1sfJLARMQkpUlwNPwbKN1Ahay95pFeoyxfev5pF69O0VkxeDmw+qm\n" +
                "xSFyC448QWx4ISZMwvzj+j32wbemt+yoBEAojcy4cxO8oIg2D4Pkh2a30JzpgyCr\n" +
                "+MiDgkfM/vL2d81ndxtQW7YZfO3893NGzxT8+TbgwsDVIFIaU5sUZ2rLBch/XUHu\n" +
                "/0RE2KiaF4Kcj0ojlfvKG//W/bX0OGOy5QnF9FXQhAFF71jWNkAkf/CEjD6u1YS+\n" +
                "gQIDAQAB\n" +
                "-----END PUBLIC KEY-----\n");
        
        // 使用不存在的应用ID
        String appId = "non_exist_app_999";
        String unionId = SsoLoginTestHelper.generateTestUnionId();
        String symmetricKey = "test_symmetric_key_" + System.currentTimeMillis();

        // 构建payload
        Map<String, Object> payload = new java.util.HashMap<>();
        payload.put("appType", AppTypeEnum.FS.getCode());
        payload.put("unionId", unionId);
        payload.put("symmetricKey", symmetricKey);
        
        String payloadJson = JSON.toJSONString(payload);
        String encryptedPayload;
        
        try {
            encryptedPayload = RsaEncryptUtil.encrypt(payloadJson, publicKey);
            
            SsoLoginRequestDTO request = new SsoLoginRequestDTO();
            request.setEncryptedPayload(encryptedPayload);
            
            javax.servlet.http.HttpServletRequest mockRequest = createMockHttpServletRequest(appId);
            ApiResult<SsoLoginResponseDTO> result = ssoController.login(request, mockRequest);
            
            log.info("应用不存在测试结果: {}", JSON.toJSONString(result));
            if (result.getMsg() != null && result.getMsg().contains("应用")) {
                log.info("✓ 步骤10a - 正确返回'应用不存在'");
            }
            
        } catch (Exception e) {
            log.error("应用不存在场景测试异常", e);
        }
    }
    
    /**
     * 测试用户未绑定场景
     */
    private void testUserNotBoundScenario() {
        log.info("--- 测试场景2: 用户未绑定ERP ---");
        
        // 使用相同的密钥对进行测试
        String publicKey = RsaEncryptUtil.extractPublicKeyFromPem("-----BEGIN PUBLIC KEY-----\n" +
                "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA5nMJXoFdk0GoK7fnQa9f\n" +
                "ye2NTdrSHfce/GBV10E30FqgiIVFY+R/2HnlBoaZmdF72la2whT3VYtrx9jDkOZX\n" +
                "gakXRf7IAWl1sfJLARMQkpUlwNPwbKN1Ahay95pFeoyxfev5pF69O0VkxeDmw+qm\n" +
                "xSFyC448QWx4ISZMwvzj+j32wbemt+yoBEAojcy4cxO8oIg2D4Pkh2a30JzpgyCr\n" +
                "+MiDgkfM/vL2d81ndxtQW7YZfO3893NGzxT8+TbgwsDVIFIaU5sUZ2rLBch/XUHu\n" +
                "/0RE2KiaF4Kcj0ojlfvKG//W/bX0OGOy5QnF9FXQhAFF71jWNkAkf/CEjD6u1YS+\n" +
                "gQIDAQAB\n" +
                "-----END PUBLIC KEY-----\n");
        
        // 使用存在的应用ID但不存在的用户
        String appId = "feishu_app_001";
        String unionId = "non_exist_user_" + System.currentTimeMillis(); // 不存在的用户ID
        String symmetricKey = "test_symmetric_key_" + System.currentTimeMillis();
        
        // 构建payload
        Map<String, Object> payload = new java.util.HashMap<>();
        payload.put("appType", AppTypeEnum.FS.getCode());
        payload.put("unionId", unionId);
        payload.put("symmetricKey", symmetricKey);
        
        String payloadJson = JSON.toJSONString(payload);
        String encryptedPayload;
        
        try {
            encryptedPayload = RsaEncryptUtil.encrypt(payloadJson, publicKey);
            
            SsoLoginRequestDTO request = new SsoLoginRequestDTO();
            request.setEncryptedPayload(encryptedPayload);
            
            javax.servlet.http.HttpServletRequest mockRequest = createMockHttpServletRequest(appId);
            ApiResult<SsoLoginResponseDTO> result = ssoController.login(request, mockRequest);
            
            log.info("用户未绑定测试结果: {}", JSON.toJSONString(result));
            if (result.getMsg() != null && result.getMsg().contains("绑定")) {
                log.info("✓ 步骤12a - 正确返回'用户未绑定ERP'");
            }
            
        } catch (Exception e) {
            log.error("用户未绑定场景测试异常", e);
        }
    }

    public static void main(String[] args) {
//        String test="Z1h4UXk4ZDdML1dhRFNUTXNtZGRsWGhzZzZjZU82eUV1UFJPZ0UwN3ZQTVE2SCtiOGsyVms3UkR3Z3djbmVCUWxLSElCU04vUUZkbW5lU3JoWXAvQjBBbEZXSlZmSWFTQkpTM0liWVlaZHYrV3FTUlJqREEyQjlEc2VtM2gzYTUwY20xQ0NTY0hvZFdsUnYwd282WEhBYm8rMlcxdE1zU3E3WStybmwvcUFVSWhMMSs5QzJENmtZWUFoV2VSbWNaM3FEbHp1ck5la3R1RmlNSG85V1Y1eml5L2psMXExRktBbkw3M2ovVENyTnhkeEVlU29WQzQ3cjVTREp5T1Q4ajBieWVxU1lXZlgwSlBnaFUzTjRDZlpmekhXbjJ0K2pORVZKQmxTbmEwc1ViTWd6cU5iM25hN0J3SDJFZlR0S2M4Q1VsL3NPNW5ubXpneEhzRVkxdGJRPT0=";
        String privateKey = RsaEncryptUtil.extractPrivateKeyFromPem("-----BEGIN PRIVATE KEY-----\n" +
                "MIIEwAIBADANBgkqhkiG9w0BAQEFAASCBKowggSmAgEAAoIBAQDmcwlegV2TQagr\n" +
                "t+dBr1/J7Y1N2tId9x78YFXXQTfQWqCIhUVj5H/YeeUGhpmZ0XvaVrbCFPdVi2vH\n" +
                "2MOQ5leBqRdF/sgBaXWx8ksBExCSlSXA0/Bso3UCFrL3mkV6jLF96/mkXr07RWTF\n" +
                "4ObD6qbFIXILjjxBbHghJkzC/OP6PfbBt6a37KgEQCiNzLhzE7ygiDYPg+SHZrfQ\n" +
                "nOmDIKv4yIOCR8z+8vZ3zWd3G1Bbthl87fz3c0bPFPz5NuDCwNUgUhpTmxRnassF\n" +
                "yH9dQe7/RETYqJoXgpyPSiOV+8ob/9b9tfQ4Y7LlCcX0VdCEAUXvWNY2QCR/8ISM\n" +
                "Pq7VhL6BAgMBAAECggEBAJkJmBU8j+n01juCO38xecldilNDl/nyXI95Ykg6tVgW\n" +
                "dzlyV6yI2CXofSGiTWsK5NcQcvncdoxGtGLZHrahYUdoBV6kXioh4Nk1cH3cLUtK\n" +
                "5PFzTdy3iELE1ED+IFnMCSr4hKaVi9FKuit2uYkt0WZkWZo28Oj5oaVSC/QJgVRy\n" +
                "guTXT8GOjxq71Jz+kiKehO9LelAmlaNZFLlRudAEgtjig90aVeOflayqM2/T1on+\n" +
                "HIGZRe3JpjvtsSUNpXC6eZGjwOqhE2kE7xZU7huEy31gm7gVAGfqK1DuZCjtYz2X\n" +
                "iPme344AaLWnfaM9GC5yEItTAY0wsj8IwkbwXtEmogkCgYEA/1Zvc99tJsVgVUU0\n" +
                "gHkicXDdYqjAhoU1T78Kgad5Q+FHnb6o5rBEpSyNosd90LMcC6pSaN8ar1EBoLJs\n" +
                "C+jwg7gsYmmz2LJeqsSyoyXOMi9ZwWyZkjrXekA6NsKGjoRKkLGJdOSz2qbnqJ0o\n" +
                "gu5vZxEEr3NhYrh5RiM9o+w5d/8CgYEA5wwSzCy6RlW2/ul5b9xjGlSpxPIIqM42\n" +
                "9XKxtxpa4ywMKETrYmhjg2tpvgP69npITUfBr3bNkxJW/4L/JM9GB0q6qUbdHaYy\n" +
                "73L0HjX/8t4UXSMv7qVfn9Z4LzoaWobPaUcCny3aglckh8pI9sp7y5ErklflqnPR\n" +
                "ETbKclI1yX8CgYEAlWYzrC7MpOTT6tClWY6HEq0hO/rVO2Bxwwla0mX1VDxaNPg7\n" +
                "x07Xu3WIIfhrq1NBU6oCsg3ohS/2rdn30jgnkkqO3b5PVrl3HqAcjc0vCrod4Ej0\n" +
                "ylTXg1ZVOXk0Vesd3e+jJ9wxR/xki4n5DDMKx9lzTl1KXALoIpu4E1PChG0CgYEA\n" +
                "2vG9/m2EN5KSTw3AXhhicVk5Z8BUJGMGAaImdnUrG4tqGd71j9OMOcVX9xCeiWNn\n" +
                "kJQnHrdeJ7iBaLAiSCeDHPXb3P2iG66bJf2xB8/0M5nvgjMOAQAFldoPGfHdd1u4\n" +
                "wPLhsSYqoskhfOxcKEHI/icbOxrAML5/p+hb/+mXvtECgYEApcz/FjUAoBwFu5PN\n" +
                "KdvNiXOjZIGX7GSe8KQAa55jk41m4+Pmcvrzfqwbn0DxVEtaHO7gCp+QafN+s+3p\n" +
                "M6l8kOtX6e5xXAUnpEQCGUujDZt9lPI9b5rHhBTvemrX3GcxUSe38dWwCRvTjQhM\n" +
                "2eBeCEugExVC46nsWv1XtiAiO8o=\n" +
                "-----END PRIVATE KEY-----\n");
        String publicKey = RsaEncryptUtil.extractPublicKeyFromPem("-----BEGIN PUBLIC KEY-----\n" +
                "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA5nMJXoFdk0GoK7fnQa9f\n" +
                "ye2NTdrSHfce/GBV10E30FqgiIVFY+R/2HnlBoaZmdF72la2whT3VYtrx9jDkOZX\n" +
                "gakXRf7IAWl1sfJLARMQkpUlwNPwbKN1Ahay95pFeoyxfev5pF69O0VkxeDmw+qm\n" +
                "xSFyC448QWx4ISZMwvzj+j32wbemt+yoBEAojcy4cxO8oIg2D4Pkh2a30JzpgyCr\n" +
                "+MiDgkfM/vL2d81ndxtQW7YZfO3893NGzxT8+TbgwsDVIFIaU5sUZ2rLBch/XUHu\n" +
                "/0RE2KiaF4Kcj0ojlfvKG//W/bX0OGOy5QnF9FXQhAFF71jWNkAkf/CEjD6u1YS+\n" +
                "gQIDAQAB\n" +
                "-----END PUBLIC KEY-----\n");
//        String encrypt = RsaEncryptUtil.encrypt("{\"symmetricKey\":\"7dBX5zCFMhWtbAjhUzN1faENyG2ypP31\",\"unionId\":\"on_d1c5059823f5d4591ce4f539614c3585\"}", publicKey);
//        System.out.println(encrypt);

        System.out.println(RsaEncryptUtil.decrypt("4BlHClpFD036xpakECm3SqrmSGsDumlvB0nF60LHovaiwRUsSBNGVIo9UN/+P3QTILK1rxTc32ciudMdP6stQP+F/cMoAaudULtzm1zY/oI4/5+yOgq/hlaFaVsbdptOSWgjBsWVhIB9Q7Bw64IdlJJig/hWZ/zvkCV39tZUXH2Qs/edaDsF+8wqi00oOcYZh8cK9ZPol3fnPN9R8mvNdlYXTX40oaM4uu8Wo60o4KzTb6tPo5NmxUPFk3do5i9i0+pKLTqm1kk5zb5MwBFlTo8fmnvq56xueb4RTi8ZsAmwa0dUY9joDxcwRd4cXbZnGHB7HQvYU2lhmcQXV+DOMA==",privateKey));
    }
    
    /**
     * 测试正常登录场景
     */
    private void testNormalLoginScenario() {
        log.info("--- 测试场景3: 正常登录 ---");
        
        // 使用相同的密钥对进行测试
        String publicKey = RsaEncryptUtil.extractPublicKeyFromPem("-----BEGIN PUBLIC KEY-----\n" +
                "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA5nMJXoFdk0GoK7fnQa9f\n" +
                "ye2NTdrSHfce/GBV10E30FqgiIVFY+R/2HnlBoaZmdF72la2whT3VYtrx9jDkOZX\n" +
                "gakXRf7IAWl1sfJLARMQkpUlwNPwbKN1Ahay95pFeoyxfev5pF69O0VkxeDmw+qm\n" +
                "xSFyC448QWx4ISZMwvzj+j32wbemt+yoBEAojcy4cxO8oIg2D4Pkh2a30JzpgyCr\n" +
                "+MiDgkfM/vL2d81ndxtQW7YZfO3893NGzxT8+TbgwsDVIFIaU5sUZ2rLBch/XUHu\n" +
                "/0RE2KiaF4Kcj0ojlfvKG//W/bX0OGOy5QnF9FXQhAFF71jWNkAkf/CEjD6u1YS+\n" +
                "gQIDAQAB\n" +
                "-----END PUBLIC KEY-----\n");
        
        String appId = "feishu_app_001";
        String unionId = SsoLoginTestHelper.generateTestUnionId();
        String symmetricKey = "test_symmetric_key_" + System.currentTimeMillis();
        
        // 构建payload
        Map<String, Object> payload = new java.util.HashMap<>();
        payload.put("appType", AppTypeEnum.FS.getCode());
        payload.put("unionId", unionId);
        payload.put("symmetricKey", symmetricKey);
        
        String payloadJson = JSON.toJSONString(payload);
        String encryptedPayload;
        
        try {
            encryptedPayload = RsaEncryptUtil.encrypt(payloadJson, publicKey);
            
            SsoLoginRequestDTO request = new SsoLoginRequestDTO();
            request.setEncryptedPayload(encryptedPayload);
            
            javax.servlet.http.HttpServletRequest mockRequest = createMockHttpServletRequest(appId);
            ApiResult<SsoLoginResponseDTO> result = ssoController.login(request, mockRequest);
            
            log.info("正常登录测试结果: {}", JSON.toJSONString(result));
            if (result.getCode() == 200 && result.getData() != null) {
                SsoLoginResponseDTO response = result.getData();
                log.info("✓ 步骤16 - 成功返回JWT Token");
                log.info("  - 用户ID: {}", response.getUserId());
                log.info("  - 应用ID: {}", response.getAppId());
                log.info("  - Token: {}", response.getToken() != null ? "已生成" : "未生成");
                log.info("  - 权限路径: {}", java.util.Arrays.toString(response.getPathList()));
                log.info("  - 签名会话ID: {}", response.getSignSessionId());
            } else {
                log.info("ℹ 正常登录场景可能需要配置数据库数据才能成功");
            }
            
        } catch (Exception e) {
            log.error("正常登录场景测试异常", e);
        }
    }
    
    /**
     * 创建模拟的HttpServletRequest
     */
    private javax.servlet.http.HttpServletRequest createMockHttpServletRequest(String appId) {
        return new javax.servlet.http.HttpServletRequest() {
            @Override
            public String getHeader(String name) {
                if ("App-Id".equals(name)) {
                    return appId;
                }
                return null;
            }
            
            // 其他方法提供默认实现，避免编译错误
            @Override public String getAuthType() { return null; }
            @Override public javax.servlet.http.Cookie[] getCookies() { return new javax.servlet.http.Cookie[0]; }
            @Override public long getDateHeader(String name) { return 0; }
            @Override public int getIntHeader(String name) { return 0; }
            @Override public java.util.Enumeration<String> getHeaders(String name) { return null; }
            @Override public java.util.Enumeration<String> getHeaderNames() { return null; }
            @Override public String getMethod() { return "POST"; }
            @Override public String getPathInfo() { return null; }
            @Override public String getPathTranslated() { return null; }
            @Override public String getContextPath() { return ""; }
            @Override public String getQueryString() { return null; }
            @Override public String getRemoteUser() { return null; }
            @Override public boolean isUserInRole(String role) { return false; }
            @Override public java.security.Principal getUserPrincipal() { return null; }
            @Override public String getRequestedSessionId() { return null; }
            @Override public String getRequestURI() { return "/sso/login"; }
            @Override public StringBuffer getRequestURL() { return new StringBuffer("http://localhost:8080/sso/login"); }
            @Override public String getServletPath() { return "/sso/login"; }
            @Override public javax.servlet.http.HttpSession getSession(boolean create) { return null; }
            @Override public javax.servlet.http.HttpSession getSession() { return null; }
            @Override public String changeSessionId() { return null; }
            @Override public boolean isRequestedSessionIdValid() { return false; }
            @Override public boolean isRequestedSessionIdFromCookie() { return false; }
            @Override public boolean isRequestedSessionIdFromURL() { return false; }
            @Override public boolean isRequestedSessionIdFromUrl() { return false; }
            @Override public boolean authenticate(javax.servlet.http.HttpServletResponse response) { return false; }
            @Override public void login(String username, String password) {}
            @Override public void logout() {}
            @Override public java.util.Collection<javax.servlet.http.Part> getParts() { return null; }
            @Override public javax.servlet.http.Part getPart(String name) { return null; }
            @Override public <T extends javax.servlet.http.HttpUpgradeHandler> T upgrade(Class<T> handlerClass) { return null; }
            @Override public Object getAttribute(String name) { return null; }
            @Override public java.util.Enumeration<String> getAttributeNames() { return null; }
            @Override public String getCharacterEncoding() { return "UTF-8"; }
            @Override public void setCharacterEncoding(String env) {}
            @Override public int getContentLength() { return 0; }
            @Override public long getContentLengthLong() { return 0; }
            @Override public String getContentType() { return "application/json"; }
            @Override public javax.servlet.ServletInputStream getInputStream() { return null; }
            @Override public String getParameter(String name) { return null; }
            @Override public java.util.Enumeration<String> getParameterNames() { return null; }
            @Override public String[] getParameterValues(String name) { return new String[0]; }
            @Override public java.util.Map<String, String[]> getParameterMap() { return new java.util.HashMap<>(); }
            @Override public String getProtocol() { return "HTTP/1.1"; }
            @Override public String getScheme() { return "http"; }
            @Override public String getServerName() { return "localhost"; }
            @Override public int getServerPort() { return 8080; }
            @Override public java.io.BufferedReader getReader() { return null; }
            @Override public String getRemoteAddr() { return "127.0.0.1"; }
            @Override public String getRemoteHost() { return "localhost"; }
            @Override public void setAttribute(String name, Object o) {}
            @Override public void removeAttribute(String name) {}
            @Override public java.util.Locale getLocale() { return java.util.Locale.getDefault(); }
            @Override public java.util.Enumeration<java.util.Locale> getLocales() { return null; }
            @Override public boolean isSecure() { return false; }
            @Override public javax.servlet.RequestDispatcher getRequestDispatcher(String path) { return null; }
            @Override public String getRealPath(String path) { return null; }
            @Override public int getRemotePort() { return 0; }
            @Override public String getLocalName() { return "localhost"; }
            @Override public String getLocalAddr() { return "127.0.0.1"; }
            @Override public int getLocalPort() { return 8080; }
            @Override public javax.servlet.ServletContext getServletContext() { return null; }
            @Override public javax.servlet.AsyncContext startAsync() { return null; }
            @Override public javax.servlet.AsyncContext startAsync(javax.servlet.ServletRequest servletRequest, javax.servlet.ServletResponse servletResponse) { return null; }
            @Override public boolean isAsyncStarted() { return false; }
            @Override public boolean isAsyncSupported() { return false; }
            @Override public javax.servlet.AsyncContext getAsyncContext() { return null; }
            @Override public javax.servlet.DispatcherType getDispatcherType() { return javax.servlet.DispatcherType.REQUEST; }
        };
    }
}
