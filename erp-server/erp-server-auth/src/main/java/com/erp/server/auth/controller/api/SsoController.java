package com.erp.server.auth.controller.api;

import com.alibaba.fastjson.JSON;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.utils.IdUtils;
import com.erp.model.sys.dto.FindThirdUserDTO;
import com.erp.model.sys.dto.SsoLoginRequestDTO;
import com.erp.model.sys.dto.SsoLoginResponseDTO;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.auth.server.SsoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 单点登录控制器
 * </p>
 *
 * @author wuhaotian
 * @since 2025-09-18
 */
@RestController
@RequestMapping("/sso")
@Slf4j
public class SsoController {

    @Resource
    private SsoService ssoService;
    
    @Resource
    private SysUserFeign sysUserFeign;

    /**
     * 单点登录接口
     *
     * @param request 单点登录请求
     * @param httpRequest HTTP请求
     * @return 单点登录响应
     */
    @PostMapping("/login")
    public ApiResult<SsoLoginResponseDTO> login(@Valid @RequestBody SsoLoginRequestDTO request, HttpServletRequest httpRequest) {
        log.info("单点登录请求：{}", JSON.toJSONString(request));
        
        try {
            // 获取请求头信息
            String appId = httpRequest.getHeader("App-Id");
            
            if (StringUtils.isBlank(appId)) {
                return ApiResult.error(ApiError.ERROR_400.code, "请求头App-Id不能为空");
            }
            
            // 后端生成Sign-Session-Id用于标识会话密钥存储位置
            String signSessionId = generateSignSessionId();
            log.info("生成Sign-Session-Id：{}", signSessionId);
            
            // 调用单点登录服务
            SsoLoginResponseDTO response = ssoService.ssoLogin(request, appId, signSessionId);
            
            log.info("单点登录成功，用户ID：{}，应用ID：{}，Sign-Session-Id：{}", response.getUserId(), response.getAppId(), signSessionId);
            return ApiResult.success(response);
            
        } catch (Exception e) {
            // 异常会被全局异常处理器处理
            throw e;
        }
    }

    /**
     * 单点登录状态检查接口
     *
     * @param signSessionId 签名会话ID
     * @param appId 应用ID
     * @return 登录状态
     */
    @GetMapping("/status")
    public ApiResult<Boolean> checkStatus(@RequestParam String signSessionId, @RequestParam String appId) {
        try {
            // 这里可以添加检查登录状态的逻辑
            // 比如检查Redis中的会话是否有效
            log.info("检查单点登录状态，signSessionId：{}，appId：{}", signSessionId, appId);
            
            // 暂时返回true，实际实现中应该检查Redis中的会话状态
            return ApiResult.success(true);
            
        } catch (Exception e) {
            log.error("检查单点登录状态异常", e);
            return ApiResult.error(500, "检查登录状态失败：" + e.getMessage());
        }
    }

    /**
     * 单点登出接口
     *
     * @param signSessionId 签名会话ID
     * @param appId 应用ID
     * @return 登出结果
     */
    @PostMapping("/logout")
    public ApiResult<String> logout(@RequestParam String signSessionId, @RequestParam String appId) {
        try {
            log.info("单点登出，signSessionId：{}，appId：{}", signSessionId, appId);
            
            // 这里可以添加登出逻辑
            // 比如清除Redis中的会话信息
            
            return ApiResult.success("登出成功");
            
        } catch (Exception e) {
            log.error("单点登出异常", e);
            return ApiResult.error(500, "登出失败：" + e.getMessage());
        }
    }

    /**
     * 获取飞书用户UnionId接口
     * 通过App-Id从sys_referer_config表获取配置信息，然后调用FsService获取用户unionId
     *
     * @param request 查找第三方用户DTO
     * @param httpRequest HTTP请求
     * @return 用户UnionId
     */
    @PostMapping("/getFsUserUnionId")
    public ApiResult<String> getFsUserUnionId(@Valid @RequestBody FindThirdUserDTO request, HttpServletRequest httpRequest) {
        log.info("获取飞书用户UnionId请求：{}", JSON.toJSONString(request));
        
        try {
            String appId = httpRequest.getHeader("App-Id");
            if (StringUtils.isBlank(appId)) {
                return ApiResult.error(ApiError.ERROR_400.code, "请求头App-Id不能为空");
            }
            
            // 使用Feign调用SysUserInfoService的方法
            ApiResult<String> result = sysUserFeign.getFsUserUnionIdByAppId(appId, request);
            
            if (result == null || !result.isSuccess()) {
                log.warn("获取飞书用户UnionId失败，appId: {}, 错误信息: {}", appId, result != null ? result.getMsg() : "调用失败");
                return ApiResult.error(ApiError.ERROR_500.code, result != null ? result.getMsg() : "获取飞书用户UnionId失败");
            }
            String unionId = result.getData();
            log.info("成功获取飞书用户UnionId：{}，appId：{}", unionId, appId);
            return ApiResult.success(unionId);
        } catch (Exception e) {
            log.error("获取飞书用户UnionId异常", e);
            return ApiResult.error(ApiError.ERROR_500.code, "获取飞书用户UnionId失败：" + e.getMessage());
        }
    }

    /**
     * 生成Sign-Session-Id
     * 用于标识会话密钥在Redis中的存储位置
     *
     * @return Sign-Session-Id
     */
    private String generateSignSessionId() {
        // 使用UUID生成唯一的会话标识
        return "sign_session_" + IdUtils.fastUUID();
    }
    


}
