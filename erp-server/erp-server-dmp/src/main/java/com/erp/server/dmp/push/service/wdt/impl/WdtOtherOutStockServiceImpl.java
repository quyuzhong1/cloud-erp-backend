package com.erp.server.dmp.push.service.wdt.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.message.enums.ApiModuleTypeEnum;
import com.erp.model.dmp.entity.PlatformEntity;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.server.dmp.push.service.CommonService;
import com.erp.server.dmp.push.service.kingdee.KingdeeCommonService;
import com.erp.server.dmp.push.service.wdt.WdtOtherOutStockService;
import com.sdk.wangdian.sdk.WdtErpException;
import com.sdk.wangdian.sdk.api.wms.stockout.StockoutAPI;
import com.sdk.wangdian.sdk.api.wms.stockout.dto.CreateOtherStockoutRequest;
import com.sdk.wangdian.sdk.api.wms.stockout.dto.CreateOtherStockoutResponse;
import com.sdk.wangdian.server.WangDianClientService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 旺店通其他出库单消费
 * @date 2024-05-24
 * @author tanmujin
 */
@Component
public class WdtOtherOutStockServiceImpl implements WdtOtherOutStockService {
    @Resource
    private KingdeeCommonService kingdeeCommonService;
    @Resource
    private WangDianClientService wangDianClientService;
    @Resource
    private CommonService commonService;

    @Override
    public void executeConsumer(CreateOtherStockoutRequest stockoutRequest) {
        PlatformEntity platformEntity = kingdeeCommonService.getPlatformEntity(PlatformEnum.WANGDIAN.getDesc());
        if (ObjectUtils.isEmpty(platformEntity)) {
            return;
        }

        StockoutAPI stockoutAPI = wangDianClientService.get(StockoutAPI.class);
        Map<String, Object> requestMap = JSON.parseObject(JSON.toJSONString(stockoutRequest), new TypeReference<Map<String, Object>>() {});
        Map<String, Object> request = commonService.makeApiFieldMap(requestMap, platformEntity.getId(), ApiModuleTypeEnum.WDT_OTHER_OUT_STOCK.getCode());
        CreateOtherStockoutResponse response = null;
        try {
            response = stockoutAPI.createOtherOutOrder(request);
        } catch (WdtErpException e) {
            e.printStackTrace();
            throw new ServiceException(ApiError.ERROR_3000.code, String.format("推送旺店通其他出库单失败: %s", e.getMessage()));
        }
        if(response.getStatus() != 0){
            throw new ServiceException(ApiError.ERROR_3000.code, String.format("推送旺店通其他出库单失败: %s", response.getMessage()));
        }
    }
}
