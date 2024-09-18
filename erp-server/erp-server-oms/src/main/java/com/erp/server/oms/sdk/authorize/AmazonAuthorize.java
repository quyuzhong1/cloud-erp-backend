package com.erp.server.oms.sdk.authorize;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.utils.RedisUtil;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.dto.CfgAppClientDTO;
import com.erp.model.dmp.dto.PlatformTaskDTO;
import com.erp.model.dmp.entity.CfgAppClientEntity;
import com.erp.model.dmp.enums.AppClientEnum;
import com.erp.model.dmp.enums.SettingEnum;
import com.erp.model.oms.dto.*;
import com.erp.model.oms.entity.ShopAuthEntity;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.enums.AuthStatusEnum;
import com.erp.rpc.dmp.feign.DmpReportFeign;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.model.dmp.dto.AmazonShopInfoDTO;
import com.erp.sdk.oms.amz.spapi.dto.AmazonTokenDTO;
import com.erp.sdk.oms.amz.spapi.enums.AmazonMarketplaceEnum;
import com.erp.sdk.oms.amz.spapi.utils.AmazonAuthClientUtils;
import com.common.business.annotation.PlatformAnnotate;
import com.erp.server.oms.service.IShopAuthorizeService;
import com.erp.server.oms.service.ShopAuthService;
import com.erp.server.oms.service.ShopInfoService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 亚马逊授权和校验
 *
 * @author Jim
 * @since 2023-11-09
 **/
@Slf4j
@Component
@PlatformAnnotate(method = PlatformDictEnum.AMAZON)
public class AmazonAuthorize implements IShopAuthorizeService<T> {
    @Resource
    private ShopInfoService shopInfoService;
    @Resource
    private DmpReportFeign dmpReportFeign;
    @Resource
    private DmpTaskFeign dmpTaskFeign;
    @Resource
    private RedisUtil redisUtil;
    @Resource
    private ShopAuthService shopAuthService;

    /**
     * 获取授权地址
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public String getShopAuthorizeUrl(ShopAuthorizeUrlDTO dto) {
        // 获取需要授权的店铺列表
        List<ShopInfoEntity> shopInfoEntityList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(dto.getShopIdList())){
            shopInfoEntityList = dto.getShopInfoEntityList();
        }

        if (CollectionUtils.isEmpty(shopInfoEntityList) && StringUtils.isNotBlank(dto.getShopId())){
            ShopInfoEntity shopInfo = shopInfoService.getById(dto.getShopId());
            if (!Objects.isNull(shopInfo)) {
                shopInfoEntityList.add(shopInfo);
            }
        }

        if (CollectionUtils.isEmpty(shopInfoEntityList)){
            throw new ServiceException("店铺不存在");
        }

        // 获取市场枚举
        AmazonMarketplaceEnum marketplaceEnum = null;
        for (ShopInfoEntity shopInfo : shopInfoEntityList) {
            AmazonMarketplaceEnum currentMarketplaceEnum = AmazonMarketplaceEnum.getByCountryCode(shopInfo.getDictCountryCode());
            if (null == marketplaceEnum){
                marketplaceEnum = currentMarketplaceEnum;
            } else {
                // 批量授权的店铺对应市场是否一致
                if (!marketplaceEnum.getEndpointsEnum().equals(currentMarketplaceEnum.getEndpointsEnum())){
                    throw new ServiceException("批量授权的地区不一致");
                }
            }
        }

        if (null == marketplaceEnum) {
            throw new ServiceException("该店铺国家在亚马逊市场未开放");
        }
        // 请求卖家授权地址
        String sellerCentralUrl = "";
        if (1 == shopInfoEntityList.size()){
            sellerCentralUrl = marketplaceEnum.getSellerCentralUrl();
        } else {
            // 批量授权指定
            sellerCentralUrl = marketplaceEnum.getEndpointsEnum().getBatchSellerCentralUrl();
            if (StringUtils.isBlank(sellerCentralUrl)){
                throw new ServiceException("该国家地区类型暂不支持批量授权");
            }
        }

        // 获取客户端配置
        AppClientEnum appClient = AppClientEnum.AMAZON_AUTHORIZE;
        CfgAppClientDTO.FindDTO findDTO = new CfgAppClientDTO.FindDTO();
        findDTO.setBusinessType(appClient.getBusinessType());
        findDTO.setDictPlatform(appClient.getPlatform());
        findDTO.setPlatformType(appClient.getPlatformType());
        CfgAppClientEntity cfgAppClient = dmpTaskFeign.getCfgAppClient(findDTO);
        if (Objects.isNull(cfgAppClient)) {
            throw new ServiceException("亚马逊授权链接配置不存在");
        }
        // 生成随机数据
        SecureRandom secureRandom = new SecureRandom();
        // 生成 256 字节的随机数据
        byte[] randomBytes = new byte[256];
        secureRandom.nextBytes(randomBytes);
        // 进行 Base64 编码
        String state = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
        // 账号要求
        String resultState = "GSA_" + state.substring(4);

        // 缓存state
        String key = StrUtil.format(RedisCacheConstants.AUTH_AMAZON_STATE, resultState);
        Object obj = redisUtil.get(key);
        if (null != obj) {
            throw new ServiceException("该店铺真正申请授权中");
        }
        // 批量缓存店铺
        List<String> shopIds = shopInfoEntityList.stream().map(ShopInfoEntity::getId).collect(Collectors.toList());
        redisUtil.set(key, shopIds, RedisCacheConstants.THIRD_PARTY_AUTH_EXPIRATION);
        return String.format(cfgAppClient.getUrl(), sellerCentralUrl , resultState);
    }

    public static void main(String[] args) {
        // 生成随机数据
        SecureRandom secureRandom = new SecureRandom();
        // 生成 256 字节的随机数据
        byte[] randomBytes = new byte[256];
        secureRandom.nextBytes(randomBytes);
        // 进行 Base64 编码
        String state = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
        // 账号要求
        String resultState = "GSA_" + state.substring(4);
        System.out.println(resultState);
    }

    /**
     * 授权校验
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public AuthorizeResultDTO shopAuthorize(ShopAuthorizeDTO dto, HttpServletResponse response) {
        AuthorizeResultDTO resultDTO = new AuthorizeResultDTO();

        if (StringUtils.isBlank(dto.getState())) {
            throw new ServiceException("信息state不存在");
        }
        // 校验是否是本系统发起
        String key = StrUtil.format(RedisCacheConstants.AUTH_AMAZON_STATE, dto.getState());
        Object shopIdObj = redisUtil.get(key);
        if (null == shopIdObj) {
            throw new ServiceException("信息已失效, 请重新发起授权");
        }
        if (!(shopIdObj instanceof List)){
            throw new ServiceException("系统缓存结构异常");
        }
        List<String> shopIds = (List<String>) shopIdObj;
        if (CollectionUtils.isEmpty(shopIds)) {
            throw new ServiceException(ApiError.ERROR_WALMART_SHOP_ID_NOT_NULL);
        }

        resultDTO.setIsAuthorize(Boolean.TRUE);
        resultDTO.setShopIdList(shopIds);

        if (StringUtils.isBlank(dto.getSpapi_oauth_code())) {
            throw new ServiceException("信息Spapi_oauth_code不存在");
        }
        if (StringUtils.isBlank(dto.getSelling_partner_id())) {
            throw new ServiceException("信息Selling_partner_id不存在");
        }
        // 查询客户端配置
        AppClientEnum appClient = AppClientEnum.AMAZON_ACCESS_TOKEN;
        CfgAppClientDTO.FindDTO findDTO = new CfgAppClientDTO.FindDTO();
        findDTO.setBusinessType(appClient.getBusinessType());
        findDTO.setDictPlatform(appClient.getPlatform());
        findDTO.setPlatformType(appClient.getPlatformType());
        CfgAppClientEntity cfgAppClient = dmpTaskFeign.getCfgAppClient(findDTO);
        if (Objects.isNull(cfgAppClient)) {
            throw new ServiceException("亚马逊应用授权配置不存在");
        }
        // 查询亚马逊SP-API配置
        // 查询授权信息
        Map<SettingEnum, String> configMap = dmpTaskFeign.getCfgSettingList("amazon_sp_api_config");

        // 发起授权请求
        AmazonTokenDTO tokenDTO = AmazonAuthClientUtils.getShopAuthorizeInfo(
                cfgAppClient.getUrl(),
                cfgAppClient.getClientId(),
                cfgAppClient.getClientSecret(),
                cfgAppClient.getRedirectUrl(),
                dto.getSpapi_oauth_code());

        for (String shopId : shopIds) {
            ShopInfoEntity shopInfo = shopInfoService.getById(shopId);
            if (null == shopInfo) {
                throw new ServiceException(ApiError.ERROR_92058);
            }
            // 店铺已授权
            if(AuthStatusEnum.ALREADY.getCode().equalsIgnoreCase(shopInfo.getAuthStatus())){
                throw new ServiceException(ApiError.ERROR_SHOP_ALREADY_AUTH);
            }

            shopInfo.setPlatformShopCode(dto.getSelling_partner_id());
            shopInfo.setAuthStatus(AuthStatusEnum.ALREADY.getCode());
            shopInfo.setAuthTime(LocalDateTime.now());
            shopInfo.setIsGenTask(Boolean.TRUE);
            boolean result = shopInfoService.updateById(shopInfo);
            if (!result) {
                throw new ServiceException("店铺授权保存失败");
            }

            // 添加token信息到缓存并按失效时间消失
            AmazonShopInfoDTO redisShopInfoDTO = initShopInfoDTO(shopInfo, configMap, cfgAppClient);

            //根据店铺id 获取到授权信息
            ShopAuthEntity shopAuth = shopAuthService.getByShopId(shopId);
            if (Objects.isNull(shopAuth)) {
                shopAuth = new ShopAuthEntity();
            }
            shopAuth.setShopId(shopId);
            shopAuth.setAccessToken(tokenDTO.getAccessToken());
            shopAuth.setToken(tokenDTO.getAccessToken());
            shopAuth.setRefreshToken(tokenDTO.getRefreshToken());
            shopAuth.setExpiresIn(tokenDTO.getExpiresIn());
            shopAuth.setAppClientId(cfgAppClient.getId());
            shopInfo.setAuthStatus(AuthStatusEnum.ALREADY.getCode());
            shopInfo.setAuthTime(LocalDateTime.now());
            if (!shopAuthService.saveOrUpdate(shopAuth)) {
                log.error("亚马逊授权信息更新失败:shopId={}, tokenDTO={}", shopId, JSONUtil.toJsonStr(tokenDTO));
                throw new ServiceException("亚马逊授权信息更新失败");
            }

            // platform-token:平台名称:店铺ID
            String tokenKey = StrUtil.format(RedisCacheConstants.REDIS_PLATFORM_TOKEN, PlatformDictEnum.AMAZON.getCode(), shopId);
            redisShopInfoDTO.setAccessToken(tokenDTO.getAccessToken());
            redisShopInfoDTO.setRefreshToken(tokenDTO.getRefreshToken());
            redisUtil.set(tokenKey, redisShopInfoDTO, tokenDTO.getExpiresIn());

            // 授权后添加任务和添加报告计划
            dmpTaskFeign.allAddOrUpdateTaskAndSchedule(new PlatformTaskDTO.DisabledDTO(shopInfo.getId(),
                    shopInfo.getName(),
                    shopInfo.getDictPlatform(),
                    false,
                    shopInfo.getDictCountryCode(),
                    shopInfo.getPlatformShopCode()));
        }
        redisUtil.del(key);
        return resultDTO;
    }

    /**
     * 取消授权
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public Boolean cancelAuthorize(CancelAuthorizeDTO dto) {
        ShopInfoEntity shopInfo = shopInfoService.getById(dto.getShopId());
        if (Objects.isNull(shopInfo)) {
            throw new ServiceException("店铺不存在");
        }
        //授权状态
        if (!AuthStatusEnum.ALREADY.getCode().equals(shopInfo.getAuthStatus())) {
            throw new ServiceException("该店铺未授权,无需取消授权");
        }
        shopInfo.setAuthStatus(AuthStatusEnum.CANCEL.getCode());
        Boolean result = shopInfoService.updateById(shopInfo);
        if (result) {
            // 删除授权
//            dmpTaskFeign.removePlatformTask(new PlatformTaskDTO.AddDTO(shopInfo.getId(), shopInfo.getName(), shopInfo.getDictPlatform()));
            // 禁用启用任务和取消报告计划任务
            dmpTaskFeign.allAddOrUpdateTaskAndSchedule(new PlatformTaskDTO.DisabledDTO(shopInfo.getId(),
                    shopInfo.getName(),
                    shopInfo.getDictPlatform(),
                    true,
                    shopInfo.getDictCountryCode(),
                    shopInfo.getPlatformShopCode()));
            shopInfo.setIsGenTask(Boolean.FALSE);
            shopInfoService.updateShopInfoById(shopInfo);
        } else {
            throw new ServiceException("取消授权失败");
        }
        return result;

    }

    /**
     * 亚马逊 Entity 转换DTO
     */
    private AmazonShopInfoDTO initShopInfoDTO(ShopInfoEntity shopInfo, Map<SettingEnum, String> config, CfgAppClientEntity cfgAppClient) {
        // 亚马逊SP-API用户
        String spApiUser = config.getOrDefault(SettingEnum.AMAZON_SP_API_USER, "");
        String accessKeyId = config.getOrDefault(SettingEnum.AMAZON_SP_API_ACCESS_KEY_ID, "");
        String secretKey = config.getOrDefault(SettingEnum.AMAZON_SP_API_SECRET_KEY, "");
        String roleArn = config.getOrDefault(SettingEnum.AMAZON_SP_API_ROLE_ARN, "");

        if (StringUtils.isBlank(spApiUser) ||
                StringUtils.isBlank(accessKeyId) ||
                StringUtils.isBlank(secretKey) ||
                StringUtils.isBlank(roleArn)
        ) {
            throw new ServiceException("亚马逊SP-API配置缺失");
        }

        return new AmazonShopInfoDTO()
                // 店铺ID
                .setId(shopInfo.getId())
                // 访问token
                .setAccessToken("")
                // 刷新token
                .setRefreshToken("")
                // 店铺名称
                .setName(shopInfo.getName())
                // 区域id
                .setDictAreaCode(shopInfo.getDictAreaCode())
                // 国家id
                .setDictCountryCode(shopInfo.getDictCountryCode())
                // 负责人id
                .setChargeId(shopInfo.getChargeId())
                // 亚马逊客户端ID
                .setClientId(cfgAppClient.getClientId())
                // 亚马逊客户端密钥
                .setClientSecret(cfgAppClient.getClientSecret())
                // 亚马逊Sp-API 访问keyID
                .setAccessKeyId(accessKeyId)
                // 亚马逊Sp-API 密钥
                .setSecretKey(secretKey)
                // 亚马逊Sp-API 角色
                .setRoleStr(roleArn)
                // 亚马逊Sp-API 用户
                .setUserStr(spApiUser)
                // 授权地址
                .setAuthUrl(cfgAppClient.getUrl())
                ;
    }

    @Override
    public Boolean refreshToken(RefreshShopTokenDTO dto) {
        return Boolean.TRUE;
    }
}
