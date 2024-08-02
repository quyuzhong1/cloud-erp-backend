package com.erp.server.oms.schedule;

import cn.hutool.core.exceptions.ExceptionUtil;
import com.erp.model.oms.dto.RefreshShopTokenDTO;
import com.erp.model.oms.entity.ShopAuthEntity;
import com.erp.server.oms.service.ShopAuthService;
import com.erp.server.oms.service.impl.AuthSaveHandler;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;

@Component
@Slf4j
public class RefreshTokenJob {

    @Resource
    private ShopAuthService shopAuthService;

    /**
     * 刷新店铺token
     */
    @XxlJob("refreshShopToken")
    public ReturnT<String> refreshShopToken() {
        XxlJobHelper.log("[刷新店铺token] 任务开始--------------------------------------->");
        List<ShopAuthEntity> shopAuthEntities = shopAuthService.listTokenExpiresShop();
        if (CollectionUtils.isEmpty(shopAuthEntities)){
            XxlJobHelper.log("[刷新店铺token] 任务结束: 无需要刷新token的店铺--------------------------------------->");
            return ReturnT.SUCCESS;
        }
        for (ShopAuthEntity shopAuthEntity : shopAuthEntities) {
            RefreshShopTokenDTO dto = new RefreshShopTokenDTO();
            dto.setShopId(shopAuthEntity.getShopId());
            dto.setPlatformCode(shopAuthEntity.getDictPlatform());
            try {
                AuthSaveHandler.refreshShopToken(dto);
                XxlJobHelper.log("[刷新店铺token] 刷新成功: shopId={}, PlatformCode={}", shopAuthEntity.getShopId(), shopAuthEntity.getDictPlatform());
            } catch (Exception e) {
                XxlJobHelper.log("[刷新店铺token] 刷新失败: shopId={}, PlatformCode={}， error={}",
                        shopAuthEntity.getShopId(),
                        shopAuthEntity.getDictPlatform(),
                        ExceptionUtil.stacktraceToString(e)
                );
                // TODO 发送预警
            }
        }
        XxlJobHelper.log("[刷新店铺token] 任务结束--------------------------------------->");
        return ReturnT.SUCCESS;
    }

}
