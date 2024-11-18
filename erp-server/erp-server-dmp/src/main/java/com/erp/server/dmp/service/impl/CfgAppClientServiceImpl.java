package com.erp.server.dmp.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.annotation.DataIdempotent;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.utils.RedisUtil;
import com.common.core.entity.BaseEntity;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.dmp.dto.AmazonShopInfoDTO;
import com.erp.model.dmp.dto.CfgAppClientDTO;
import com.erp.model.dmp.entity.CfgAppClientEntity;
import com.erp.model.dmp.enums.AppClientEnum;
import com.erp.model.dmp.enums.SettingEnum;
import com.erp.model.oms.dto.AmazonTokenUpdateDTO;
import com.erp.model.oms.entity.ShopAuthEntity;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.sdk.oms.amz.spapi.dto.AmazonTokenDTO;
import com.erp.sdk.oms.amz.spapi.enums.AmazonMarketplaceEnum;
import com.erp.sdk.oms.amz.spapi.utils.AmazonAuthClientUtils;
import com.erp.server.dmp.mapper.CfgAppClientMapper;
import com.erp.server.dmp.service.CfgAppClientService;
import com.erp.server.dmp.service.CfgSettingService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 第三方应用程序信息表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-08-28
 */
@Slf4j
@Service
public class CfgAppClientServiceImpl extends SuperServiceImpl<CfgAppClientMapper, CfgAppClientEntity> implements CfgAppClientService {

    @Resource
    private RedisUtil redisUtil;
    @Resource
    private CfgSettingService cfgSettingService;
    @Resource
    private ShopInfoFeign shopInfoFeign;



    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public String add(CfgAppClientDTO.AddDTO addDTO) {
        CfgAppClientEntity cfgAppClientEntity = new CfgAppClientEntity();
        BeanMapperUtils.copy(addDTO, cfgAppClientEntity);
        // 数据处理
        handleData(cfgAppClientEntity);
        log.info("开始新增第三方应用程序信息单");
        boolean save = super.save(cfgAppClientEntity);
        if (!save) {
            throw new ServiceException("第三方应用程序信息单保存失败");
        }
        return cfgAppClientEntity.getId();
    }

    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(CfgAppClientDTO.UpdateDTO updateDTO) {
        CfgAppClientEntity old = super.getById(updateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "第三方应用程序信息单"));
        CfgAppClientEntity cfgAppClientEntity = BeanMapperUtils.map(CfgAppClientEntity.class, updateDTO);

        // 数据处理
        handleData(cfgAppClientEntity);
        log.info("编辑 开始修改第三方应用程序信息单数据，id：【{}】", old.getId());
        boolean save = super.updateById(cfgAppClientEntity);
        if (!save) {
            throw new ServiceException("第三方应用程序信息单保存失败");
        }
        return Boolean.TRUE;
    }

    private void handleData(CfgAppClientEntity cfgAppClientEntity) {
    }


    /**
     * 获取根据信息 获取到配置信息
     *
     * @param dto
     * @return com.erp.model.dmp.entity.CfgAppClientEntity
     * @author yl
     * @date 2023-08-29 10:43
     */
    @Override
    public CfgAppClientEntity getCfgAppClient(CfgAppClientDTO.FindDTO dto) {
        return this.lambdaQuery().eq(CfgAppClientEntity::getBusinessType, dto.getBusinessType()).
                eq(CfgAppClientEntity::getDictPlatform, dto.getDictPlatform()).
                eq(CfgAppClientEntity::getPlatformType, dto.getPlatformType()).
                last("LIMIT 1").one();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AmazonShopInfoDTO cacheAndFindShopAuth(String shopId) {
        if (StringUtils.isBlank(shopId)){
            throw new ServiceException("获取店铺授权异常:数据异常：店铺ID为空");
        }
        // platform-token:平台名称:店铺ID
        String tokenKey = StrUtil.format(RedisCacheConstants.REDIS_PLATFORM_TOKEN, PlatformDictEnum.AMAZON.getCode(), shopId);
        // 缓存获取
        Object tokenObj = redisUtil.get(tokenKey);
        if (null != tokenObj) {
            if (tokenObj instanceof AmazonShopInfoDTO) {
                return (AmazonShopInfoDTO) tokenObj;
            }
            throw new ServiceException("亚马逊授权信息转换异常");
        }
        ShopInfoEntity shopInfo = shopInfoFeign.getShopInfoById(shopId);
        if (null == shopInfo) {
            throw new ServiceException(ApiError.ERROR_92058);
        }
        if (shopInfo.getDisabled()){
            throw new ServiceException(ApiError.ERROR_MARKETPLACE_UNAUTHORIZED, shopInfo.getId());
        }
        // 相同账号的关联店铺
        List<ShopInfoEntity> relatedshopInfoList = shopInfoFeign.getRelatedShopById(shopInfo);

        AppClientEnum appClient = AppClientEnum.AMAZON_ACCESS_TOKEN;
        CfgAppClientDTO.FindDTO findDTO = new CfgAppClientDTO.FindDTO();
        findDTO.setBusinessType(appClient.getBusinessType());
        findDTO.setDictPlatform(appClient.getPlatform());
        findDTO.setPlatformType(appClient.getPlatformType());
        CfgAppClientEntity cfgAppClient = getCfgAppClient(findDTO);

        // 查询亚马逊SP-API配置
        // 查询授权信息
        Map<SettingEnum, String> configMap = cfgSettingService.getMap(SettingEnum.AMAZON_SP_API_CONFIG);
        // 添加token信息到缓存并按失效时间消失
        AmazonShopInfoDTO redisShopInfoDTO = initShopInfoDTO(shopInfo, configMap, cfgAppClient, relatedshopInfoList);

        AmazonTokenDTO tokenDTO = this.requestAmzAndAuth(shopInfo, cfgAppClient);
        // token添加到redis
        redisShopInfoDTO.setRefreshToken(tokenDTO.getRefreshToken());
        redisShopInfoDTO.setAccessToken(tokenDTO.getAccessToken());
        // 缓存到redis
        redisUtil.set(tokenKey, redisShopInfoDTO, tokenDTO.getExpiresIn());

        return redisShopInfoDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @DataIdempotent(keyIdName = "shopInfo.platformShopCode", waitTime = 90)
    public AmazonTokenDTO requestAmzAndAuth(ShopInfoEntity shopInfo, CfgAppClientEntity cfgAppClient) {
        // 查询已授权信息
        //根据店铺id 获取到授权信息
        ShopAuthEntity shopAuth = shopInfoFeign.getShopAuthByShopId(shopInfo.getId());
        if (Objects.isNull(shopAuth)) {
            throw new ServiceException("未找到已授权信息");
        }
        LocalDateTime currentExpiresTime = shopAuth.getUpdateTime().plusSeconds(shopAuth.getExpiresIn());
        // 重新计算当前过期时间
        long until = LocalDateTime.now().until(currentExpiresTime, ChronoUnit.SECONDS);
        if (until > 0){
            return new AmazonTokenDTO(shopAuth.getAccessToken(), shopAuth.getRefreshToken(), shopAuth.getType(), (int) until);
        }
        // 刷新token请求
        AmazonTokenDTO tokenDTO = AmazonAuthClientUtils.refreshAuthorizeInfo(
                cfgAppClient.getUrl(),
                cfgAppClient.getClientId(),
                cfgAppClient.getClientSecret(),
                shopAuth.getRefreshToken());

        AmazonTokenUpdateDTO updateDTO = new AmazonTokenUpdateDTO(shopInfo, shopAuth, tokenDTO.getAccessToken(), tokenDTO.getRefreshToken());
        // 更新
        shopInfoFeign.checkAndSaveAllAmazonToken(updateDTO);
        return tokenDTO;
    }


    /**
     * 亚马逊 Entity 转换DTO
     */
    private AmazonShopInfoDTO initShopInfoDTO(ShopInfoEntity shopInfo, Map<SettingEnum, String> config, CfgAppClientEntity cfgAppClient, List<ShopInfoEntity> relatedshopInfoList) {
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
       //当前亚马逊账号所有已授权的站点map(包含自身)
       // Map<marketplaceId, shopId>
        Map<String, AmazonShopInfoDTO.ShopNameDTO> marketplaceShopIdMap = new HashMap<>();
        // 当前店铺站点
        marketplaceShopIdMap.put(
                AmazonMarketplaceEnum.getByCountryCode(shopInfo.getDictCountryCode()).getMarketplaceId(),
                new AmazonShopInfoDTO.ShopNameDTO(shopInfo.getId(), shopInfo.getName()));
        // 其他站点
        if (!CollectionUtils.isEmpty(relatedshopInfoList)){
            Map<String, AmazonShopInfoDTO.ShopNameDTO> relatedMap = relatedshopInfoList
                    .stream()
                    .collect(Collectors.toMap(e -> AmazonMarketplaceEnum.getByCountryCode(e.getDictCountryCode()).getMarketplaceId(),
                            e -> new AmazonShopInfoDTO.ShopNameDTO(e.getId(), e.getName())));
            marketplaceShopIdMap.putAll(relatedMap);
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
                // 亚马逊账号ID
                .setPlatformShopCode(shopInfo.getPlatformShopCode())
                // 亚马逊账号授权的所有站点
                .setMarketplaceShopIdMap(marketplaceShopIdMap)
                ;
    }


}
