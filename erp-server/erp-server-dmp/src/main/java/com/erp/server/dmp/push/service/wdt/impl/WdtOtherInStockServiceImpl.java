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
import com.sdk.wangdian.sdk.Pager;
import com.sdk.wangdian.sdk.WdtErpException;
import com.sdk.wangdian.sdk.api.wms.external.in.CreateStockExternalInRequest;
import com.sdk.wangdian.sdk.api.wms.external.in.CreateStockExternalInResponse;
import com.sdk.wangdian.sdk.api.wms.external.in.StockExternalInAPI;
import com.sdk.wangdian.sdk.api.wms.stockin.StockinAPI;
import com.sdk.wangdian.sdk.api.wms.stockin.dto.CreateOtherStockinRequest;
import com.sdk.wangdian.sdk.api.wms.stockin.dto.CreateOtherStockinResponse;
import com.sdk.wangdian.sdk.api.wms.stockin.dto.OtherStockinRequest;
import com.sdk.wangdian.sdk.api.wms.stockin.dto.OtherStockinResponse;
import com.sdk.wangdian.server.WangDianClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 旺店通其他入库单消费
 * @date 2024-05-24
 * @author tanmujin
 */
@Slf4j
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
            throw new ServiceException(ApiError.ERROR_3000.code, String.format("推送旺店通其他入库单异常: %s", e.getMessage()));
        }
        if(response.getStatus() != 0){
            log.error("旺店通其他出库单推送失败，request：{}， response：{}", stockinRequest, response);
            throw new ServiceException(ApiError.ERROR_3000.code, String.format("推送旺店通其他入库单失败: %s, %s, %s", stockinRequest.getOuterNo(), response.getStatus(), response.getMessage()));
        }
        if(null != response.getData() && null != response.getData().getStatus() && 0 != response.getData().getStatus()){
            log.error("旺店通其他出库单审核失败，request：{}，response：{}", stockinRequest, response);
            throw new ServiceException(ApiError.ERROR_3000.code, String.format("推送旺店通其他入库单审核失败: %s, %s, %s", stockinRequest.getOuterNo(), response.getStatus(), response.getMessage()));
        }
    }

    @Override
    public void executeSelfConsumer(CreateOtherStockinRequest request) {
        PlatformEntity platformEntity = kingdeeCommonService.getPlatformEntity(PlatformEnum.WANGDIAN.getDesc());
        if (ObjectUtils.isEmpty(platformEntity)) {
            return;
        }
        StockExternalInAPI stockExternalInAPI = wangDianClientService.get(StockExternalInAPI.class);
        CreateStockExternalInRequest externalInRequest = new CreateStockExternalInRequest();
        externalInRequest.setIsCheck(true);
        CreateStockExternalInRequest.Order order = new CreateStockExternalInRequest.Order();
        order.setOrderNo(request.getOuterNo());
        order.setWarehouseNo(request.getWarehouseNo());
        order.setRemark(request.getRemark());
        order.setSrcOrderType("0");
        order.setReason(request.getReason());
        externalInRequest.setOrder(order);
        List<CreateStockExternalInRequest.OrderDetail> orderDetails = request.getGoodsList().stream()
                .map(v -> {
                    CreateStockExternalInRequest.OrderDetail orderDetail = new CreateStockExternalInRequest.OrderDetail();
                    orderDetail.setSpecNo(v.getSpecNo());
                    orderDetail.setNum(v.getNum());
                    orderDetail.setRemark(v.getRemark());
                    return orderDetail;
                })
                .collect(Collectors.toList());
        externalInRequest.setOrderDetails(orderDetails);
        Map<String, Object> requestMap = JSON.parseObject(JSON.toJSONString(externalInRequest), new TypeReference<Map<String, Object>>() {});
        Map<String, Object> requestBody = commonService.makeApiFieldMap(requestMap, platformEntity.getId(), ApiModuleTypeEnum.WDT_EXT_IN_STOCK.getCode());
        CreateStockExternalInResponse response = null;
        try {
            response = stockExternalInAPI.createOrder(requestBody);
        } catch (WdtErpException e) {
            log.error("推送旺店通其他出库单失败:{}", e.getMessage(), e);
            throw new ServiceException(ApiError.ERROR_3000.code, String.format("推送旺店通其他入库单失败: %s", e.getMessage()));
        }
        if(response.getStatus() != 0){
            log.error("推送旺店通其他出库单失败:{}", response.getMessage());
            throw new ServiceException(ApiError.ERROR_3000.code, String.format("推送旺店通其他入库单失败: %s", response.getMessage()));
        }
    }

    @Override
    public OtherStockinResponse.DataInfoDto queryWithDetail(CreateOtherStockinRequest createRequest) {
        OtherStockinRequest request = new OtherStockinRequest();
        StockinAPI stockinAPI = wangDianClientService.get(StockinAPI.class);
        OtherStockinResponse.DataInfoDto salesStockoutResponse;
        request.setStockinNo(createRequest.getOuterNo());
        Pager pager = new Pager(10, 0, true);
        salesStockoutResponse = stockinAPI.queryWithDetail(request, pager);
        return salesStockoutResponse;
    }
}
