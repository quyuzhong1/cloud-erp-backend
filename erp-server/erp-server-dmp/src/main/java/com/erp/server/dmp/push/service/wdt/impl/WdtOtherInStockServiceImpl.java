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
import com.erp.server.dmp.push.service.wdt.WdtOtherInStockService;
import com.sdk.wangdian.sdk.WdtErpException;
import com.sdk.wangdian.sdk.api.wms.stockin.StockinAPI;
import com.sdk.wangdian.sdk.api.wms.stockin.dto.CreateOtherStockinRequest;
import com.sdk.wangdian.sdk.api.wms.stockin.dto.CreateOtherStockinResponse;
import com.sdk.wangdian.server.WangDianClientService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 旺店通其他入库单消费
 * @date 2024-05-24
 * @author tanmujin
 */
@Service
public class WdtOtherInStockServiceImpl implements WdtOtherInStockService {
    @Resource
    private KingdeeCommonService kingdeeCommonService;
    @Resource
    private WangDianClientService wangDianClientService;
    @Resource
    private CommonService commonService;

    @Override
    public void executeConsumer(CreateOtherStockinRequest stockinRequest) {
        PlatformEntity platformEntity = kingdeeCommonService.getPlatformEntity(PlatformEnum.WANGDIAN.getDesc());
        if (ObjectUtils.isEmpty(platformEntity)) {
            return;
        }

        StockinAPI stockinAPI = wangDianClientService.get(StockinAPI.class);
        Map<String, Object> requestMap = JSON.parseObject(JSON.toJSONString(stockinRequest), new TypeReference<Map<String, Object>>() {});
        Map<String, Object> request = commonService.makeApiFieldMap(requestMap, platformEntity.getId(), ApiModuleTypeEnum.WDT_OTHER_IN_STOCK.getCode());
        CreateOtherStockinResponse response = null;
        try {
            response = stockinAPI.createOtherOrder(request);
        } catch (WdtErpException e) {
            e.printStackTrace();
            throw new ServiceException(ApiError.ERROR_3000.code, String.format("推送旺店通其他入库单失败: %s", e.getMessage()));
        }
        if(response.getStatus() != 0){
            throw new ServiceException(ApiError.ERROR_3000.code, String.format("推送旺店通其他入库单失败: %s", response.getMessage()));
        }
    }
}
