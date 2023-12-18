package com.erp.server.oms.schedule;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.utils.RedisUtil;
import com.erp.model.dmp.dto.CfgAppClientDTO;
import com.erp.model.dmp.entity.CfgAppClientEntity;
import com.erp.model.dmp.enums.AppClientEnum;
import com.erp.model.oms.entity.ShopAuthEntity;
import com.erp.oms.aliexpress.dto.AliExpressShopInfoDTO;
import com.erp.oms.aliexpress.dto.request.RefreshTokenRequest;
import com.erp.oms.aliexpress.service.AliExpressAuthService;
import com.erp.oms.aliexpress.util.ApiException;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.server.oms.service.ShopAuthService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * @author Lambda
 * @Classname AliExpressAuthJob
 * @Description 速卖通刷新token
 * @Date 2023-12-12 10:31
 * @Created by yl
 */
@Component
@Slf4j
public class AliExpressAuthJob {

    @Resource
    private DmpTaskFeign dmpTaskFeign;

    @Resource
    private ShopAuthService shopAuthService;

    @Resource
    private AliExpressAuthService aliExpressAuthService;

    @Resource
    private RedisUtil redisUtil;



    /**
     * 刷新速卖通的token
     */
    @XxlJob("refreshTokenAliExpress")
    public void refreshTokenAliExpress() {
        //先获取授权店铺 然后根据授权店铺进行
        CfgAppClientDTO.FindDTO findDTO = new CfgAppClientDTO.FindDTO();
        AppClientEnum appClientEnum = AppClientEnum.ALI_EXPRESS_TOKEN;
        findDTO.setBusinessType(appClientEnum.getBusinessType());
        findDTO.setDictPlatform(appClientEnum.getPlatform());
        findDTO.setPlatformType(appClientEnum.getPlatformType());
        CfgAppClientEntity cfgAppClient = dmpTaskFeign.getCfgAppClient(findDTO);
        if (Objects.isNull(cfgAppClient)) {
            return;
        }
        String appClientId= cfgAppClient.getId();
        String clientId = cfgAppClient.getClientId();
        String baseUrl= cfgAppClient.getUrl();
        String clientSecret=cfgAppClient.getClientSecret();

        List<ShopAuthEntity> shopList = shopAuthService.listByClientId(appClientId);
        if (CollectionUtils.isEmpty(shopList)) {
            return;
        }
        for (ShopAuthEntity item : shopList) {
            RefreshTokenRequest request=RefreshTokenRequest.builder().
                    baseUrl(baseUrl).
                    refreshToken(item.getRefreshToken()).
                    clientId(clientId).
                    clientSecret(clientSecret).
                    build();
            try {
                JSONObject jsonObject= aliExpressAuthService.RefreshToken(request);
                String code=jsonObject.getOrDefault("code","").toString();
                //表示成功
                if("0".equals(code)){
                    String accessToken=jsonObject.getOrDefault("access_token","").toString();
                    String refreshToken=jsonObject.getOrDefault("refresh_token","").toString();
                    Integer expiresIn=jsonObject.getInteger("expires_in");
                    shopAuthService.refreshToken(item.getId(),accessToken, refreshToken, expiresIn);
                    AliExpressShopInfoDTO shopInfoDTO=new AliExpressShopInfoDTO();
                    shopInfoDTO.setId(item.getShopId());
                    shopInfoDTO.setClientId(clientId);
                    shopInfoDTO.setClientSecret(clientSecret);
                    shopInfoDTO.setBaseUrl(baseUrl);
                    shopInfoDTO.setName("");
                    shopInfoDTO.setToken(accessToken);
                    String tokenKey = StrUtil.format(RedisCacheConstants.REDIS_PLATFORM_TOKEN, PlatformDictEnum.ALI_EXPRESS.getCode(), item.getShopId());
                    redisUtil.set(tokenKey, shopInfoDTO,expiresIn);
                }
            } catch (ApiException e) {
                log.error("刷新店铺id 为>>>{} token失败 {}",item.getShopId(),e.getMessage());
            }
        }


    }
}
