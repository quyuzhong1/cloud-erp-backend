package com.erp.server.oms.schedule;

import com.erp.model.dmp.dto.CfgAppClientDTO;
import com.erp.model.dmp.entity.CfgAppClientEntity;
import com.erp.model.dmp.enums.AppClientEnum;
import com.erp.model.oms.entity.ShopAuthEntity;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.enums.AuthStatusEnum;
import com.erp.model.oms.enums.AuthTypeEnum;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.tms.feign.LogisticsBillFeign;
import com.erp.server.oms.service.ShopAuthService;
import com.erp.server.oms.service.ShopInfoService;
import com.sdk.oms.shopee.dto.base.ShopeeTokenAuth;
import com.sdk.oms.shopee.dto.base.request.AuthRequest;
import com.sdk.oms.shopee.service.ShopeeAuthService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * @author zdy
 * @ClassName ShopeeAuthJob
 * @description: TODO
 * @date 2023年10月23日
 * @version: 1.0
 */
@Component
@Slf4j
@EnableScheduling
public class ShopeeAuthJob {

    @Resource
    private ShopAuthService shopAuthService;
    @Resource
    private ShopInfoService shopInfoService;
    @Resource
    private ShopeeAuthService shopeeAuthService;
    @Resource
    private DmpTaskFeign dmpTaskFeign;
    @Resource
    private LogisticsBillFeign logisticsBillFeign;

    /**
     * 更新shopee授权时间
     */
//     @Scheduled(cron = "*/5 * * * * ?")
    // @Scheduled(cron = "0 0 */3 * * ?")
    @XxlJob("updateShopeeAuth")
    public void updateShopeeAuth() {
        //先获取授权店铺 然后根据授权店铺进行
        CfgAppClientDTO.FindDTO findDTO = new CfgAppClientDTO.FindDTO();
        AppClientEnum appClientEnum = AppClientEnum.SHOPEE_ACCESS_TOKEN;
        findDTO.setBusinessType(appClientEnum.getBusinessType());
        findDTO.setDictPlatform(appClientEnum.getPlatform());
        findDTO.setPlatformType(appClientEnum.getPlatformType());
        CfgAppClientEntity cfgAppClient = dmpTaskFeign.getCfgAppClient(findDTO);
        if (Objects.isNull(cfgAppClient)) {
            return;
        }
        List<ShopAuthEntity> shopeeShopList = shopAuthService.getShopeeShopList(AuthTypeEnum.SHOP.getCode(), AuthStatusEnum.ALREADY.getCode());
        if (CollectionUtils.isNotEmpty(shopeeShopList)) {
            for (ShopAuthEntity shopAuthEntity : shopeeShopList) {
                //判断店铺是否授权
                if (Objects.isNull(shopAuthEntity.getShopId())) {
                    continue;
                }
                ShopInfoEntity shopInfo = shopInfoService.getById(shopAuthEntity.getShopId());
                if (Objects.isNull(shopInfo) || !AuthStatusEnum.ALREADY.getCode().equalsIgnoreCase(shopInfo.getAuthStatus())) {
                    continue;
                }
                AuthRequest authRequest = AuthRequest.builder()
                        .host(cfgAppClient.getUrl())
                        .refreshToken(shopAuthEntity.getRefreshToken())
                        .partnerId(Long.parseLong(cfgAppClient.getClientId()))
                        .tmpPartnerKey(cfgAppClient.getClientSecret())
                        .shopId(Long.parseLong(shopAuthEntity.getShopeeId()))
                        .build();
                ShopeeTokenAuth shopeeResponse = shopeeAuthService.refreshShopToken(authRequest);
                if (Objects.isNull(shopeeResponse) || StringUtils.isNotEmpty(shopeeResponse.getError())) {
                    shopInfo.setAuthStatus(AuthStatusEnum.NOT.getCode());
                    shopInfoService.saveOrUpdate(shopInfo);
                    log.error("授权异常：{}", shopeeResponse);
                    continue;
                }
                shopInfoService.saveOrUpdateShopee(shopeeResponse, AuthTypeEnum.SHOP.getCode(), shopAuthEntity.getShopeeId(), shopInfo, cfgAppClient.getId());
            }
        }
        List<ShopAuthEntity> shopeeShopList1 = shopAuthService.getShopeeShopList(AuthTypeEnum.MERCHANT.getCode(), AuthStatusEnum.ALREADY.getCode());
        if (CollectionUtils.isNotEmpty(shopeeShopList1)) {
            for (ShopAuthEntity  shopAuthEntity:shopeeShopList1) {
                //判断店铺是否授权
                if (Objects.isNull(shopAuthEntity.getShopId())) {
                    continue;
                }
                ShopInfoEntity shopInfo = shopInfoService.getById(shopAuthEntity.getShopId());
                if (Objects.isNull(shopInfo) || !AuthStatusEnum.ALREADY.getCode().equalsIgnoreCase(shopInfo.getAuthStatus())) {
                    continue;
                }
                AuthRequest authRequest = AuthRequest.builder()
                        .host(cfgAppClient.getUrl())
                        .refreshToken(shopAuthEntity.getRefreshToken())
                        .partnerId(Long.parseLong(cfgAppClient.getClientId()))
                        .tmpPartnerKey(cfgAppClient.getClientSecret())
                        .merchantId(Long.parseLong(shopAuthEntity.getShopeeId()))
                        .build();
                ShopeeTokenAuth shopeeResponse = shopeeAuthService.refreshMerchantToken(authRequest);
                if (Objects.isNull(shopeeResponse) || StringUtils.isNotEmpty(shopeeResponse.getError())) {
                    shopInfo.setAuthStatus(AuthStatusEnum.NOT.getCode());
                    shopInfoService.saveOrUpdate(shopInfo);
                    log.error("授权异常：{}", shopeeResponse);
                    continue;
                }
                shopInfoService.saveOrUpdateShopee(shopeeResponse, AuthTypeEnum.MERCHANT.getCode(), shopAuthEntity.getShopeeId(), shopInfo, cfgAppClient.getId());
            }
        }
    }

    /**
     * 获取虾皮包裹编号
     */
    @XxlJob("shopeeGetLogisticsTrackNo")
    public void getLogisticsPackageNumber() {
        //TODO 获取虾皮无物流单号订单
        //TODO 同步获取单号
    }
}
