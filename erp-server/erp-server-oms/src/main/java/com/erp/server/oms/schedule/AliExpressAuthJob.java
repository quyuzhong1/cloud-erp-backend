package com.erp.server.oms.schedule;

import com.erp.model.dmp.dto.CfgAppClientDTO;
import com.erp.model.dmp.entity.CfgAppClientEntity;
import com.erp.model.dmp.enums.AppClientEnum;
import com.erp.model.oms.entity.ShopAuthEntity;
import com.erp.model.oms.enums.AuthStatusEnum;
import com.erp.model.oms.enums.AuthTypeEnum;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.server.oms.service.ShopAuthService;
import com.sdk.oms.shopee.service.ShopeeAuthService;
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

    /**
     * 刷新速卖通的token
     */
    //@XxlJob("refreshTokenAliExpress")
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
        String clientId = cfgAppClient.getId();
        List<ShopAuthEntity> shopList = shopAuthService.listByClientId(clientId);
        if (CollectionUtils.isEmpty(shopList)) {
            return;
        }



    }
}
