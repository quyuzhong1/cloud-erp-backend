package com.erp.server.oms.schedule;

import com.erp.model.dmp.dto.CfgAppClientDTO;
import com.erp.model.dmp.entity.CfgAppClientEntity;
import com.erp.model.dmp.enums.AppClientEnum;
import com.erp.model.oms.entity.ShopAuthEntity;
import com.erp.model.oms.enums.AuthTypeEnum;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.server.oms.service.ShopAuthService;
import com.erp.server.oms.service.ShopInfoService;
import com.sdk.oms.shopee.dto.base.ShopeeTokenAuth;
import com.sdk.oms.shopee.service.ShopeeAuthService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
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

    /**
     * 更新shopee授权时间
     */
//     @Scheduled(cron = "*/5 * * * * ?")
    // @Scheduled(cron = "0 0 */3 * * ?")
    @XxlJob("updateShopeeAuth")
    public void updateShopeeAuth() {
        CfgAppClientDTO.FindDTO findDTO = new CfgAppClientDTO.FindDTO();
        AppClientEnum appClientEnum = AppClientEnum.SHOPEE_ACCESS_TOKEN;
        findDTO.setBusinessType(appClientEnum.getBusinessType());
        findDTO.setDictPlatform(appClientEnum.getPlatform());
        findDTO.setPlatformType(appClientEnum.getPlatformType());
        CfgAppClientEntity cfgAppClient = dmpTaskFeign.getCfgAppClient(findDTO);
        if (Objects.isNull(cfgAppClient)) {
            return;
        }
        List<ShopAuthEntity> shopeeShopList = shopAuthService.getShopeeShopList(AuthTypeEnum.SHOP.getCode());
        if (CollectionUtils.isNotEmpty(shopeeShopList)) {
            shopeeShopList.forEach(shopAuthEntity -> {
                ShopeeTokenAuth shopeeResponse = shopeeAuthService.refreshShopToken(cfgAppClient.getUrl(), shopAuthEntity.getRefreshToken(),
                        Long.parseLong(cfgAppClient.getClientId()), cfgAppClient.getClientSecret(), Long.parseLong(shopAuthEntity.getShopeeId()));
                shopInfoService.saveOrUpdateShopee(shopeeResponse, AuthTypeEnum.SHOP.getCode(), shopAuthEntity.getShopeeId(), null, cfgAppClient.getId());
            });
        }
        List<ShopAuthEntity> shopeeShopList1 = shopAuthService.getShopeeShopList(AuthTypeEnum.MERCHANT.getCode());
        if (CollectionUtils.isNotEmpty(shopeeShopList1)){
            shopeeShopList1.forEach(shopAuthEntity -> {
                ShopeeTokenAuth shopeeResponse = shopeeAuthService.refreshMerchantToken(cfgAppClient.getUrl(), shopAuthEntity.getRefreshToken(),
                        Long.parseLong(cfgAppClient.getClientId()), cfgAppClient.getClientSecret(), Long.parseLong(shopAuthEntity.getShopeeId()));
                shopInfoService.saveOrUpdateShopee(shopeeResponse, AuthTypeEnum.MERCHANT.getCode(), shopAuthEntity.getShopeeId(), null, cfgAppClient.getId());
            });
        }
    }
}
