package com.erp.server.auth.server.impl;

import com.alibaba.fastjson.JSON;
import org.redisson.api.RedissonClient;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.RsaEncryptUtil;
import com.erp.model.sys.dto.*;
import com.erp.model.sys.entity.SysRefererConfigEntity;
import com.erp.rpc.sys.feign.SysRefereConfigFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.model.sys.enums.AppTypeEnum;
import com.erp.model.sys.utils.JwtUtils;
import com.erp.server.auth.config.AuthJwtProperties;
import com.erp.server.auth.server.SsoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 单点登录服务实现类
 * </p>
 *
 * @author wuhaotian
 * @since 2025-09-18
 */
@Slf4j
@Service
public class SsoServiceImpl implements SsoService {

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private AuthJwtProperties authJwtProperties;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private SysRefereConfigFeign sysRefereConfigFeign;

    @Override
    public SsoLoginResponseDTO ssoLogin(SsoLoginRequestDTO request, String appId, String sessionId) {
        try {
            // 1. 先解密payload获取appType
            String decryptedPayload = "";
            try {
                // 这里需要先获取配置来解密，所以先查询所有配置
                List<SysRefererConfigEntity> configList = getRefererConfigList(appId);
                if (configList == null || configList.isEmpty()) {
                    throw new ServiceException(ApiError.SSO_APP_NOT_FOUND);
                }
                
                // 尝试用每个配置解密，找到正确的配置
                SysRefererConfigEntity config = null;
                for (SysRefererConfigEntity cfg : configList) {
                    try {
                        // 处理PEM格式的私钥，提取Base64编码的私钥部分
                        String privateKey = cfg.getPrivateKey();
                        if (privateKey != null && privateKey.contains("-----BEGIN")) {
                            privateKey = RsaEncryptUtil.extractPrivateKeyFromPem(privateKey);
                        }
                        decryptedPayload = RsaEncryptUtil.decrypt(request.getEncryptedPayload(), privateKey);
                        SsoPayloadDTO payload = JSON.parseObject(decryptedPayload, SsoPayloadDTO.class);
                        if (payload != null && StringUtils.isNotBlank(payload.getAppType())) {
                            config = cfg;
                            break;
                        }
                    } catch (Exception e) {
                        // 继续尝试下一个配置
                        continue;
                    }
                }
                
                if (config == null) {
                    throw new ServiceException(ApiError.SSO_DECRYPT_FAILED);
                }
                
                // 2. 检查是否开启单点登录
                if (Boolean.TRUE.equals(config.getSsoDisabled())) {
                    throw new ServiceException(ApiError.SSO_DISABLED);
                }
                
                // 3. 解析payload
                SsoPayloadDTO payload = JSON.parseObject(decryptedPayload, SsoPayloadDTO.class);
                if (payload == null) {
                    throw new ServiceException(ApiError.SSO_PARSE_PAYLOAD_FAILED);
                }
                
                // 4. 验证payload内容
                if (!validatePayload(payload)) {
                    throw new ServiceException(ApiError.SSO_INVALID_PAYLOAD);
                }
                
                // 5. 根据应用类型查询ERP用户
                String userId = getUserByThirdParty(payload.getUnionId(), payload.getAppType());
                if (StringUtils.isBlank(userId)) {
                    throw new ServiceException(ApiError.SSO_USER_NOT_BOUND);
                }
                
                // 6. 生成Redis键值对存储对称密钥
                String redisKey = String.format("sign:session:%s:%s:%s", appId, userId, sessionId);
                redissonClient.getBucket(redisKey).set(payload.getSymmetricKey(), 24, TimeUnit.HOURS);
                log.info("存储对称密钥到Redis，key：{}", redisKey);
                
                // 7. 获取权限路径列表
                String[] pathList = getPathList(appId);
                
                // 8. 生成JWT Token（带权限路径列表）
                SysUserDTO userDTO = new SysUserDTO();
                userDTO.setUid(userId);
                userDTO.setUserName(userId); // 这里可以根据需要设置用户名
                userDTO.setToken(userId);
                
                String jwtToken = JwtUtils.generateToken(userDTO, authJwtProperties.getSecret(), authJwtProperties.getExpire(), pathList);
                
                // 9. 返回成功响应，包含signSessionId
                return SsoLoginResponseDTO.success(jwtToken, userId, appId, pathList, sessionId);
                
            } catch (ServiceException e) {
                throw e;
            } catch (Exception e) {
                log.error("解密payload失败", e);
                throw new ServiceException(ApiError.SSO_DECRYPT_FAILED);
            }

        } catch (ServiceException e) {
            // ServiceException会被全局异常处理器处理，这里重新抛出
            throw e;
        } catch (Exception e) {
            log.error("单点登录处理异常", e);
            throw new ServiceException(ApiError.SSO_SYSTEM_ERROR, e.getMessage());
        }
    }

    /**
     * 根据App-Id查询所有配置
     */
    private List<SysRefererConfigEntity> getRefererConfigList(String appId) {
        try {
            ApiResult<List<SysRefererConfigEntity>> result = sysRefereConfigFeign.getByAppId(appId);
            
            if (result != null && result.isSuccess() && result.getData() != null) {
                return result.getData();
            }
            log.warn("未找到应用配置，appId：{}", appId);
            return null;
        } catch (Exception e) {
            log.error("查询应用配置失败，appId：{}", appId, e);
            return null;
        }
    }

    /**
     * 验证payload内容
     */
    private boolean validatePayload(SsoPayloadDTO payload) {
        if (payload == null) {
            return false;
        }
        
        // 验证应用类型
        if (StringUtils.isBlank(payload.getAppType())) {
            log.warn("payload中的应用类型为空");
            return false;
        }
        
        // 验证unionId
        if (StringUtils.isBlank(payload.getUnionId())) {
            log.warn("payload中的unionId为空");
            return false;
        }
        
        // 验证对称密钥
        if (StringUtils.isBlank(payload.getSymmetricKey())) {
            log.warn("payload中的对称密钥为空");
            return false;
        }
        
        return true;
    }

    /**
     * 根据第三方用户ID查询ERP用户
     */
    private String getUserByThirdParty(String unionId, String appType) {
        try {
            // 将应用类型转换为第三方平台类型
            String thirdPartyType = convertAppTypeToThirdPartyType(appType);
            if (StringUtils.isBlank(thirdPartyType)) {
                log.warn("不支持的应用类型：{}", appType);
                return null;
            }
            
            // 使用SysUserFeign查询用户ID
            FindUserByThirdDTO thirdDTO = new FindUserByThirdDTO();
            thirdDTO.setThirdPartyType(thirdPartyType);
            thirdDTO.setThirdPartyUnionId(unionId);
            
            String userId = sysUserFeign.getUidByUnionId(thirdDTO);
            
            if (StringUtils.isNotBlank(userId)) {
                log.info("找到绑定的用户ID：{}，unionId：{}，appType：{}", userId, unionId, appType);
                return userId;
            }
            
            log.warn("未找到绑定的用户，unionId：{}，appType：{}", unionId, appType);
            return null;
        } catch (Exception e) {
            log.error("查询第三方用户绑定关系失败，unionId：{}，appType：{}", unionId, appType, e);
            return null;
        }
    }

    /**
     * 将应用类型转换为第三方平台类型
     */
    private String convertAppTypeToThirdPartyType(String appType) {
        AppTypeEnum appTypeEnum = AppTypeEnum.getByCode(appType);
        if (appTypeEnum == null) {
            return null;
        }
        
        switch (appTypeEnum) {
            case FS:
                return "FS"; // 飞书
            case PDA:
                return "PDA"; // PDA
            case WECHAT_MINIPROGRAM:
                return "WECHAT"; // 微信小程序
            case ERP:
                return "ERP"; // ERP系统
            default:
                return null;
        }
    }

    /**
     * 获取权限路径列表
     */
    private String[] getPathList(String appId) {
        // 默认权限路径，可以根据实际需求从数据库查询
        return new String[]{"/open/api/**"};
    }

    @Override
    public KeyRegistrationResponseDTO registerKey(KeyRegistrationRequestDTO request, String appId, String sessionId) {
        try {
            // 1. 根据App-Id查询所有配置
            List<SysRefererConfigEntity> configList = getRefererConfigList(appId);
            if (configList == null || configList.isEmpty()) {
                throw new ServiceException(ApiError.SSO_APP_NOT_FOUND);
            }

            // 2. 尝试用每个配置解密，找到正确的配置
            SysRefererConfigEntity config = null;
            String decryptedPayload = null;
            for (SysRefererConfigEntity cfg : configList) {
                try {
                    // 处理PEM格式的私钥，提取Base64编码的私钥部分
                    String privateKey = cfg.getPrivateKey();
                    if (privateKey != null && privateKey.contains("-----BEGIN")) {
                        privateKey = RsaEncryptUtil.extractPrivateKeyFromPem(privateKey);
                    }
                    decryptedPayload = RsaEncryptUtil.decrypt(request.getEncryptedPayload(), privateKey);
                    KeyRegistrationPayloadDTO payload = JSON.parseObject(decryptedPayload, KeyRegistrationPayloadDTO.class);
                    if (payload != null && StringUtils.isNotBlank(payload.getSymmetricKey())) {
                        config = cfg;
                        break;
                    }
                } catch (Exception e) {
                    // 继续尝试下一个配置
                    continue;
                }
            }

            if (config == null) {
                throw new ServiceException(ApiError.SSO_DECRYPT_FAILED);
            }

            // 3. 解析payload
            KeyRegistrationPayloadDTO payload = JSON.parseObject(decryptedPayload, KeyRegistrationPayloadDTO.class);
            if (payload == null || StringUtils.isBlank(payload.getSymmetricKey())) {
                throw new ServiceException(ApiError.SSO_PARSE_PAYLOAD_FAILED);
            }

            // 4. 从payload中获取userId，如果为空则使用"none"
            String userId = payload.getUserId();
            if (StringUtils.isBlank(userId)) {
                userId = "none";
            }

            // 5. 生成Redis键值对存储对称密钥
            // 格式：sign:session:{appId}:{userId}:{sessionId}
            String redisKey = String.format("sign:session:%s:%s:%s", appId, userId, sessionId);
            redissonClient.getBucket(redisKey).set(payload.getSymmetricKey(), 5, TimeUnit.MINUTES);
            log.info("存储对称密钥到Redis，key：{}，userId：{}，过期时间：5分钟", redisKey, userId);

            // 6. 返回成功响应
            return KeyRegistrationResponseDTO.success(sessionId);

        } catch (ServiceException e) {
            // ServiceException会被全局异常处理器处理，这里重新抛出
            throw e;
        } catch (Exception e) {
            log.error("密钥注册处理异常", e);
            throw new ServiceException(ApiError.SSO_SYSTEM_ERROR, e.getMessage());
        }
    }
}
