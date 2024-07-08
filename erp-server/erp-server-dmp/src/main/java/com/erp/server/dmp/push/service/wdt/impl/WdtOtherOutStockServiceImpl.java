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
import com.sdk.wangdian.sdk.Pager;
import com.sdk.wangdian.sdk.WdtErpException;
import com.sdk.wangdian.sdk.api.wms.external.out.CreateStockExternalOutRequest;
import com.sdk.wangdian.sdk.api.wms.external.out.CreateStockExternalOutResponse;
import com.sdk.wangdian.sdk.api.wms.external.out.StockExternalOutAPI;
import com.sdk.wangdian.sdk.api.wms.stockout.StockoutAPI;
import com.sdk.wangdian.sdk.api.wms.stockout.dto.*;
import com.sdk.wangdian.server.WangDianClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.net.ConnectException;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 旺店通其他出库单消费
 * @date 2024-05-24
 * @author tanmujin
 */
@Component
@Slf4j
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
            throw new ServiceException(ApiError.ERROR_3000.code, String.format("推送旺店通其他出库单异常: %s", e.getMessage()));
        }
        if(response.getStatus() != 0){
            log.error("旺店通其他出库单推送失败，request：{}， response：{}", stockoutRequest, response);
            throw new ServiceException(ApiError.ERROR_3000.code, String.format("推送旺店通其他出库单失败: %s, %s, %s", stockoutRequest.getOuterNo(), response.getStatus(), response.getMessage()));
        }
        if(null != response.getData() && null != response.getData().getStatus() && 0 != response.getData().getStatus()){
            log.error("旺店通其他出库单审核失败，request：{}，response：{}", stockoutRequest, response);
            throw new ServiceException(ApiError.ERROR_3000.code, String.format("推送旺店通其他出库单审核失败: %s, %s", response.getData().getStatus(), response.getData().getMessage()));
        }
    }

    @Override
    public void executeSelfConsumer(CreateOtherStockoutRequest stockOutRequest) {
        PlatformEntity platformEntity = kingdeeCommonService.getPlatformEntity(PlatformEnum.WANGDIAN.getDesc());
        if (ObjectUtils.isEmpty(platformEntity)) {
            return;
        }
        StockExternalOutAPI stockExternalOutApi = wangDianClientService.get(StockExternalOutAPI.class);
        CreateStockExternalOutRequest extRequest = new CreateStockExternalOutRequest();
        extRequest.setIsCheck(true);
        CreateStockExternalOutRequest.Order order = new CreateStockExternalOutRequest.Order();
        order.setOrderNo(stockOutRequest.getOuterNo());
        order.setWarehouseNo(stockOutRequest.getWarehouseNo());
        order.setRemark(stockOutRequest.getRemark());
        order.setSrcOrderType("0");
        order.setReason(stockOutRequest.getReason());
        extRequest.setOrder(order);
        List<CreateStockExternalOutRequest.OrderDetail> orderDetails = stockOutRequest.getGoodsList().stream()
                .map(v -> {
                    CreateStockExternalOutRequest.OrderDetail orderDetail = new CreateStockExternalOutRequest.OrderDetail();
                    orderDetail.setSpecNo(v.getSpecNo());
                    orderDetail.setNum(v.getNum());
                    orderDetail.setRemark(v.getRemark());
                    return orderDetail;
                })
                .collect(Collectors.toList());
        extRequest.setOrderDetails(orderDetails);
        Map<String, Object> requestMap = JSON.parseObject(JSON.toJSONString(extRequest), new TypeReference<Map<String, Object>>() {});
        Map<String, Object> request = commonService.makeApiFieldMap(requestMap, platformEntity.getId(), ApiModuleTypeEnum.WDT_EXT_OUT_STOCK.getCode());
        CreateStockExternalOutResponse response;
        try {
            response = stockExternalOutApi.createOrder(request);
        } catch (WdtErpException e) {
            log.error("推送旺店通其他出库单失败:{}", e.getMessage(), e);
            throw new ServiceException(ApiError.ERROR_3000.code, String.format("推送旺店通其他出库单失败: %s", e.getMessage()));
        }
        if(response.getStatus() != 0){
            log.error("推送旺店通其他出库单失败:{}", response.getMessage());
            throw new ServiceException(ApiError.ERROR_3000.code, String.format("推送旺店通其他出库单失败: %s", response.getMessage()));
        }
    }

    @Override
    public StockoutOtherQueryResponse queryWithDetail(CreateOtherStockoutRequest createOutstock) {
        StockoutOtherQueryRequest request = new StockoutOtherQueryRequest();
        StockoutAPI stockoutAPI = wangDianClientService.get(StockoutAPI.class);
        StockoutOtherQueryResponse salesStockoutResponse;
        request.setStockoutNo(createOutstock.getOuterNo());
        Pager pager = new Pager(10, 0, true);
        try {
            salesStockoutResponse = stockoutAPI.searchOther(request, pager);
        } catch (WdtErpException | ConnectException e) {
            throw new RuntimeException(e);
        }
        return salesStockoutResponse;
    }
}
