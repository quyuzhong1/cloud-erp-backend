package com.erp.server.oms.service.authorize;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.utils.RedisUtil;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.dto.CfgAppClientDTO;
import com.erp.model.dmp.dto.DmpSyncReportScheduleDTO;
import com.erp.model.dmp.dto.PlatformTaskDTO;
import com.erp.model.dmp.entity.CfgAppClientEntity;
import com.erp.model.dmp.enums.AppClientEnum;
import com.erp.model.oms.dto.CancelAuthorizeDTO;
import com.erp.model.oms.dto.ShopAuthorizeDTO;
import com.erp.model.oms.dto.ShopAuthorizeUrlDTO;
import com.erp.model.oms.entity.ShopAuthEntity;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.enums.AuthStatusEnum;
import com.erp.rpc.dmp.feign.DmpReportFeign;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.sdk.oms.amz.spapi.dto.AmazonTokenDTO;
import com.erp.sdk.oms.amz.spapi.enums.AmazonMarketplaceEnum;
import com.erp.sdk.oms.amz.spapi.utils.AmazonAuthClientUtils;
import com.erp.server.oms.service.AuthSaveData;
import com.erp.server.oms.service.IShopAuthorizeService;
import com.erp.server.oms.service.ShopAuthService;
import com.erp.server.oms.service.ShopInfoService;
import com.sdk.oms.shopify.constant.ShopifyConstant;
import com.sdk.oms.shopify.dto.ShopifyShopInfoDTO;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Objects;

/**
 * 亚马逊授权和校验
 *
 * @author Jim
 * @since 2023-11-09
 **/
@Slf4j
@Component
@AuthSaveData(method = PlatformDictEnum.AMAZON)
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
        ShopInfoEntity shopInfo = shopInfoService.getById(dto.getShopId());
        if (Objects.isNull(shopInfo)) {
            throw new ServiceException("店铺不存在");
        }
        AmazonMarketplaceEnum marketplaceEnum = AmazonMarketplaceEnum.getByCountryCode(shopInfo.getDictCountryCode());
        if (null == marketplaceEnum){
            throw new ServiceException("该店铺国家在亚马逊市场未开放");
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
        String state = Base64.getEncoder().encodeToString(randomBytes);
        // 账号要求
        String resultState = "GSA_" + state.substring(4);

        // 缓存state
        String key = StrUtil.format(RedisCacheConstants.AUTH_AMAZON_STATE, resultState);
        Object obj = redisUtil.get(key);
        if (null != obj) {
            throw new ServiceException("该店铺真正申请授权中");
        }
        redisUtil.set(key, shopInfo.getId(), RedisCacheConstants.THIRD_PARTY_AUTH_EXPIRATION);
        return String.format(cfgAppClient.getUrl(), marketplaceEnum.getSellerCentralUrl(), resultState);
    }

    /**
     * 授权校验
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public Boolean shopAuthorize(ShopAuthorizeDTO dto) {
        if (StringUtils.isBlank(dto.getState())){
            throw new ServiceException("信息state不存在");
        }
        // 校验是否是本系统发起
        String key = StrUtil.format(RedisCacheConstants.AUTH_AMAZON_STATE, dto.getState());
        Object shopIdObj = redisUtil.get(key);
        if (null == shopIdObj){
            throw new ServiceException("信息已失效, 请重新发起授权");
        }
        String shopId = shopIdObj.toString();
        if (StringUtils.isBlank(shopId)) {
            throw new ServiceException(ApiError.ERROR_WALMART_SHOP_ID_NOT_NULL);
        }
        ShopInfoEntity shopInfo = shopInfoService.getById(shopId);
        if (null == shopInfo) {
            throw new ServiceException(ApiError.ERROR_92058);
        }
        // 校验国家唯一
        boolean existSameCountry = shopInfoService.checkExist(shopInfo.getDictCountryCode(), shopInfo.getDictPlatform(), AuthStatusEnum.ALREADY.getCode());
        if (existSameCountry) {
            throw new ServiceException(ApiError.ERROR_COUNTRY_COUNT_SHOP_EXIST, shopInfo.getCountryName());
        }
        if (StringUtils.isBlank(dto.getSpapi_oauth_code())){
            throw new ServiceException("信息Spapi_oauth_code不存在");
        }
        if (StringUtils.isBlank(dto.getSelling_partner_id())){
            throw new ServiceException("信息Selling_partner_id不存在");
        }

        shopInfo.setPlatformShopCode(dto.getSelling_partner_id());
        shopInfo.setAuthStatus(AuthStatusEnum.ALREADY.getCode());
        shopInfo.setAuthTime(LocalDateTime.now());
        shopInfo.setIsGenTask(Boolean.TRUE);
        boolean result = shopInfoService.updateById(shopInfo);
        if (!result) {
            throw new ServiceException("店铺授权保存失败");
        }

        AppClientEnum appClient = AppClientEnum.AMAZON_ACCESS_TOKEN;
        CfgAppClientDTO.FindDTO findDTO = new CfgAppClientDTO.FindDTO();
        findDTO.setBusinessType(appClient.getBusinessType());
        findDTO.setDictPlatform(appClient.getPlatform());
        findDTO.setPlatformType(appClient.getPlatformType());
        CfgAppClientEntity cfgAppClient = dmpTaskFeign.getCfgAppClient(findDTO);
        if (Objects.isNull(cfgAppClient)) {
            throw new ServiceException("亚马逊应用授权配置不存在");
        }
        // 发起授权请求
        AmazonTokenDTO tokenDTO = AmazonAuthClientUtils.getShopAuthorizeInfo(
                cfgAppClient.getUrl(),
                cfgAppClient.getClientId(),
                cfgAppClient.getClientSecret(),
                cfgAppClient.getRedirectUrl(),
                dto.getSpapi_oauth_code());

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
        if (!shopAuthService.saveOrUpdate(shopAuth)){
            log.error("亚马逊授权信息更新失败:shopId={}, tokenDTO={}", shopId, JSONUtil.toJsonStr(tokenDTO));
            throw new ServiceException("亚马逊授权信息更新失败");
        }
        // 添加token信息到缓存并按失效时间消失
        ShopifyShopInfoDTO shopInfoDTO = initShopInfoDTO(shopInfo, tokenDTO.getAccessToken());
        // platform-token:平台名称:店铺ID
        String tokenKey = StrUtil.format(RedisCacheConstants.REDIS_PLATFORM_TOKEN, PlatformDictEnum.AMAZON.getCode(), shopId);
        redisUtil.set(tokenKey, shopInfoDTO, tokenDTO.getExpiresIn());

        // 授权后添加任务
        dmpTaskFeign.createPlatformTask(new PlatformTaskDTO.AddDTO(shopInfo.getId(), shopInfo.getName(), shopInfo.getDictPlatform()));

        // 添加报告计划
        DmpSyncReportScheduleDTO dmpDTO = new DmpSyncReportScheduleDTO();
        BeanUtils.copyProperties(shopInfo, dmpDTO);
        dmpDTO.setShopId(shopInfo.getId());
        dmpReportFeign.addReportSchedule(dmpDTO);

        redisUtil.del(key);
        return Boolean.TRUE;
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
            dmpTaskFeign.removePlatformTask(new PlatformTaskDTO.AddDTO(shopInfo.getId(), shopInfo.getName(), shopInfo.getDictPlatform()));
            shopInfo.setIsGenTask(Boolean.FALSE);
            shopInfoService.updateShopInfoById(shopInfo);
        }
        // 取消报告计划任务
        DmpSyncReportScheduleDTO dmpDTO = new DmpSyncReportScheduleDTO();
        BeanUtils.copyProperties(shopInfo, dmpDTO);
        dmpDTO.setShopId(dto.getShopId());
        dmpReportFeign.cancelReportSchedule(dmpDTO);
        return result;

    }

    /**
     * 亚马逊 Entity 转换DTO
     */
    private ShopifyShopInfoDTO initShopInfoDTO(ShopInfoEntity shopInfo, String accessToken) {
        return new ShopifyShopInfoDTO()
                // 店铺ID
                .setId(shopInfo.getId())
                // 访问token
                .setAccessToken(accessToken)
                // 店铺名称
                .setName(shopInfo.getName())
                // 区域id
                .setDictAreaCode(shopInfo.getDictAreaCode())
                // 国家id
                .setDictCountryCode(shopInfo.getDictCountryCode())
                // 负责人id
                .setChargeId(shopInfo.getChargeId())
                // 店铺全域名: 无用
                .setShopDomain(shopInfo.getDomain());
    }
}
