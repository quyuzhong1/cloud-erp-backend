package com.erp.server.tms.service.logistics;

import cn.hutool.json.JSONUtil;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.common.business.annotation.LogisticsPlatformType;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
import com.erp.model.tms.enums.BusinessTypeEnum;
import com.erp.model.tms.enums.LogisticsPlatformResultEnum;
import com.erp.model.tms.enums.RequestStatusEnums;
import com.erp.model.tms.vo.request.*;
import com.erp.model.tms.vo.response.CancelResponseVO;
import com.erp.model.tms.vo.response.InterceptResponseVO;
import com.erp.model.tms.vo.response.LogisticsOrderResponseVO;
import com.erp.model.tms.vo.response.LogisticsPrintLabelResponse;
import com.erp.server.tms.convert.LogisticsChannelConverter;
import com.erp.server.tms.convert.LogisticsOrderConverter;
import com.erp.server.tms.handler.AbstractLogisticsHandler;
import com.erp.server.tms.service.LogisticsOrderOperateLogService;
import com.sdk.tms.ubi.model.catalog.response.ServiceCataLog;
import com.sdk.tms.ubi.model.label.LabelRequest;
import com.sdk.tms.ubi.model.label.LabelResponse;
import com.sdk.tms.ubi.model.order.request.HoldRequest;
import com.sdk.tms.ubi.model.order.request.OrderItem;
import com.sdk.tms.ubi.model.order.request.UbiOrder;
import com.sdk.tms.ubi.model.order.response.OrderResponse;
import com.sdk.tms.ubi.model.order.response.TrackBase;
import com.sdk.tms.ubi.service.UbiShipperService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author zdy
 * @ClassName UbiLogisticsHandlerImpl
 * @description: TODO
 * @date 2023年11月09日
 * @version: 1.0
 */
@Slf4j
@Component
@LogisticsPlatformType(LogisticsPlatformEnum.UBI)
public class UbiLogisticsHandlerImpl extends AbstractLogisticsHandler {

    @Resource
    UbiShipperService ubiShipperService;
    @Resource
    private LogisticsOrderOperateLogService logisticsOrderOperateLogService;
    /**
     * 创建订单
     *
     * @param logisticsOrderVO
     * @return
     */
    @Override
    public ApiResult<LogisticsOrderResponseVO> createOrder(LogisticsOrderVO logisticsOrderVO) {
        //字段转换
        UbiOrder ubiOrder = LogisticsOrderConverter.INSTANCE.orderRequestByUBI(logisticsOrderVO);
        ubiOrder.setDescription(logisticsOrderVO.getLogisticsProductVOList().get(0).getDeclareEnglishName());
        ubiOrder.setNativeDescription(logisticsOrderVO.getLogisticsProductVOList().get(0).getDeclareChineseName());
        ubiOrder.setWeight(Double.valueOf(logisticsOrderVO.getLogisticsProductVOList().get(0).getWeight()));
        //TODO 货值(>=0.01)，与sum(itemCount * unitValue)的误差不能超过0.1
//        BigDecimal price = logisticsOrderVO.getLogisticsProductVOList().get(0).getPrice();
        BigDecimal price = logisticsOrderVO.getParceInfoVO().getTotalPrice();
        Integer quantity = logisticsOrderVO.getLogisticsProductVOList().get(0).getQuantity();
        ubiOrder.setInvoiceValue(logisticsOrderVO.getParceInfoVO().getTotalPrice().doubleValue());
        ubiOrder.setInvoiceCurrency(logisticsOrderVO.getParceInfoVO().getCurrency());
        //订单信息
        List<OrderItem> orderItems = LogisticsOrderConverter.INSTANCE.orderItemsRequestByUBI(logisticsOrderVO.getLogisticsProductVOList());
        ubiOrder.setOrderItems(orderItems);
        try {
            OrderResponse order = ubiShipperService.createOrder(logisticsOrderVO.getAuthMap(),ubiOrder);
            logisticsOrderOperateLogService.addOperateLog(logisticsOrderVO.getAuthMap().get("id"),
                    logisticsOrderVO.getDeliveryNo(), BusinessTypeEnum.CREATE_ORDER.getCode(), LogisticsPlatformEnum.UBI.getCode(),
                    RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(logisticsOrderVO), JSONUtil.toJsonStr(order));
            //一票多件包裹信息 会有多层嵌套 暂不考虑
            LogisticsOrderResponseVO responseVO = LogisticsOrderResponseVO.builder()
                    .deliveryNo(order.getReferenceNo())
                    .transportNo(order.getOrderId())
                    .trackNo(order.getTrackingNo())
                    .build();
            return success(responseVO);
        } catch (Exception e) {
            log.error(e.getMessage());
            logisticsOrderOperateLogService.addOperateLog(logisticsOrderVO.getAuthMap().get("id"),
                    logisticsOrderVO.getDeliveryNo(), BusinessTypeEnum.CREATE_ORDER.getCode(), LogisticsPlatformEnum.UBI.getCode(),
                    RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsOrderVO), JSONUtil.toJsonStr(e.getMessage()));
            return failure(e.getMessage());
        }
    }


    /**
     * 取消订单
     *
     * @param logisticsCancelOrderVOS
     * @return
     */
    @Override
    public ApiResult<List<CancelResponseVO>> cancelOrder(List<LogisticsCancelOrderVO> logisticsCancelOrderVOS) {
        LogisticsCancelOrderVO logisticsCancelOrderVO = logisticsCancelOrderVOS.stream().filter(e -> Objects.nonNull(e.getAuthMap())).findFirst().orElse(null);
        List<CancelResponseVO> responseVOS = new ArrayList<>();
        boolean isSuccess = true;
        for (LogisticsCancelOrderVO logisticsQueryVO : logisticsCancelOrderVOS) {
            //只支持单个订单取消
            CancelResponseVO responseVO = new CancelResponseVO();
            try {
                OrderResponse orderResponse = ubiShipperService.deleteShipperOrder(logisticsCancelOrderVO.getAuthMap(), logisticsQueryVO.getDeliveryNo());
                logisticsOrderOperateLogService.addOperateLog(logisticsQueryVO.getAuthMap().get("id"),
                        logisticsQueryVO.getDeliveryNo(), BusinessTypeEnum.CANCEL_ORDER.getCode(), LogisticsPlatformEnum.UBI.getCode(),
                        RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(logisticsQueryVO), JSONUtil.toJsonStr(orderResponse));
                if ("Success".equalsIgnoreCase(orderResponse.getStatus())){
                    responseVO.success();
                    responseVO.setDeliveryNo(orderResponse.getReferenceNo());
                    responseVO.setTransportNo(orderResponse.getOrderId());
                    responseVO.setTrackNo(orderResponse.getTrackingNo());
                }else {
                    isSuccess = false;
                    logisticsOrderOperateLogService.addOperateLog(logisticsQueryVO.getAuthMap().get("id"),
                            logisticsQueryVO.getDeliveryNo(), BusinessTypeEnum.CANCEL_ORDER.getCode(), LogisticsPlatformEnum.UBI.getCode(),
                            RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsQueryVO), JSONUtil.toJsonStr(orderResponse));
                    responseVO.failure(LogisticsPlatformEnum.UBI.getName(),"-1", orderResponse.getErrors());
                    responseVO.setDeliveryNo(orderResponse.getReferenceNo());
                }
                responseVOS.add(responseVO);
            } catch (Exception e) {
                logisticsOrderOperateLogService.addOperateLog(logisticsQueryVO.getAuthMap().get("id"),
                        logisticsQueryVO.getDeliveryNo(), BusinessTypeEnum.CANCEL_ORDER.getCode(), LogisticsPlatformEnum.UBI.getCode(),
                        RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsQueryVO), JSONUtil.toJsonStr(e.getMessage()));
                isSuccess = false;
            }
        }
        return isSuccess?success(responseVOS):failure(responseVOS);
    }


    /**
     * 拦截订单
     *
     * @param logisticsInterceptOrderVOS
     * @return
     */
    @Override
    public ApiResult<List<InterceptResponseVO>> interceptOrder(List<LogisticsInterceptOrderVO> logisticsInterceptOrderVOS) {
        LogisticsInterceptOrderVO logisticsInterceptOrderVO = logisticsInterceptOrderVOS.stream().filter(e -> Objects.nonNull(e.getAuthMap())).findFirst().orElse(null);
        HoldRequest holdRequest = HoldRequest.builder()
                .orderIds(logisticsInterceptOrderVOS.stream().map(LogisticsInterceptOrderVO::getDeliveryNo).collect(Collectors.toList()))
                .holdType(1)
                .build();
        try {
            List<OrderResponse> orderResponses = ubiShipperService.interceptOrder(logisticsInterceptOrderVO.getAuthMap(), holdRequest);
            logisticsOrderOperateLogService.addOperateLog(logisticsInterceptOrderVO.getAuthMap().get("id"),
                    logisticsInterceptOrderVO.getDeliveryNo(), BusinessTypeEnum.INTERCEPT_ORDER.getCode(), LogisticsPlatformEnum.UBI.getCode(),
                    RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(logisticsInterceptOrderVOS), JSONUtil.toJsonStr(orderResponses));
            List<InterceptResponseVO> responseVOS = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(orderResponses)) {
                orderResponses.stream().forEach(orderResponse -> {
                    if ("Success".equalsIgnoreCase(orderResponse.getStatus())) {
                        InterceptResponseVO responseVO = InterceptResponseVO.builder()
                                .deliveryNo(orderResponse.getOrderId())
                                .build();
                        responseVO.setCode(LogisticsPlatformResultEnum.SUCESS.getCode());
                        responseVOS.add(responseVO);
                    } else {
                        InterceptResponseVO responseVO = InterceptResponseVO.builder()
                                .deliveryNo(orderResponse.getOrderId())
                                .build();
                        responseVO.setCode(LogisticsPlatformResultEnum.FAILURE.getCode());
                        responseVO.setMessage(LogisticsPlatformResultEnum.FAILURE.getDesc());
                        responseVOS.add(responseVO);
                    }
                });
            }
            return success(responseVOS);
        } catch (Exception e) {
            logisticsOrderOperateLogService.addOperateLog(logisticsInterceptOrderVO.getAuthMap().get("id"),
                    logisticsInterceptOrderVO.getDeliveryNo(), BusinessTypeEnum.INTERCEPT_ORDER.getCode(), LogisticsPlatformEnum.UBI.getCode(),
                    RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsInterceptOrderVOS), JSONUtil.toJsonStr(e.getMessage()));
            return failure(e.getMessage());
        }
    }

    /**
     * 查询订单(批量)
     *
     * @param logisticsQueryVOList
     * @return
     */
    @Override
    public ApiResult<List<LogisticsOrderResponseVO>> queryOrderList(List<LogisticsQueryBaseVO> logisticsQueryVOList) {
        LogisticsQueryBaseVO logisticsQueryBaseVO = logisticsQueryVOList.stream().filter(e -> Objects.nonNull(e.getAuthMap())).findFirst().orElse(null);
        try {
            List<TrackBase> trackNumber = ubiShipperService.getTrackNumber(logisticsQueryBaseVO.getAuthMap(), logisticsQueryVOList.stream().map(LogisticsQueryBaseVO::getDeliveryNo).collect(Collectors.toList()));

            logisticsOrderOperateLogService.addOperateLog(logisticsQueryBaseVO.getAuthMap().get("id"),
                    logisticsQueryBaseVO.getDeliveryNo(), BusinessTypeEnum.QUERY_ORDER.getCode(), LogisticsPlatformEnum.UBI.getCode(),
                    RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(logisticsQueryVOList), JSONUtil.toJsonStr(trackNumber));
            //转换
            List<LogisticsOrderResponseVO> responseVOS = LogisticsOrderConverter.INSTANCE.ordersQueryByUBI(trackNumber);
            return success(responseVOS);
        } catch (Exception e) {
            logisticsOrderOperateLogService.addOperateLog(logisticsQueryBaseVO.getAuthMap().get("id"),
                    logisticsQueryBaseVO.getTransportNo(), BusinessTypeEnum.QUERY_ORDER.getCode(), LogisticsPlatformEnum.UBI.getCode(),
                    RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsQueryVOList), JSONUtil.toJsonStr(e.getMessage()));
            return failure(e.getMessage());
        }
    }

    /**
     * 获取标签批量
     *
     * @param logisticsQueryVO
     * @return
     */
    @Override
    public ApiResult<List<LogisticsPrintLabelResponse>> getLabelList(List<LogisticsGetLabelVO> logisticsQueryVO) throws IOException {
        LabelRequest labelRequest = LabelRequest.builder()
                .orderIds(logisticsQueryVO.stream().map(LogisticsQueryBaseVO::getDeliveryNo).collect(Collectors.toList()))
                //TODO 根据传参决定打印单大小
                .labelType("0")
                .packinglist(false)
                .merged(true)
                .labelFormat("JPG")
                .dpi("300")
                .build();
        LogisticsGetLabelVO logisticsGetLabelVO = logisticsQueryVO.stream().filter(e -> Objects.nonNull(e.getAuthMap())).findFirst().orElse(null);
        assert logisticsGetLabelVO != null;
        try {
            List<LabelResponse> labelSpecs = ubiShipperService.getLabels(logisticsGetLabelVO.getAuthMap(), labelRequest);

            logisticsOrderOperateLogService.addOperateLog(logisticsGetLabelVO.getAuthMap().get("id"),
                    logisticsGetLabelVO.getDeliveryNo(), BusinessTypeEnum.GET_LABEL_LIST.getCode(), LogisticsPlatformEnum.UBI.getCode(),
                    RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(logisticsQueryVO), JSONUtil.toJsonStr(labelSpecs));
            List<LogisticsPrintLabelResponse> responses = new ArrayList<>();
            labelSpecs.forEach(labelResponse -> {
                responses.add(LogisticsPrintLabelResponse.builder()
                        .deliveryNoList(Collections.singletonList(labelResponse.getOrderId()))
                        .base64(labelResponse.getLabelContent())
                        .trackNoList(Collections.singletonList(labelResponse.getTrackingNo()))
                        .build());
            });
            return success(responses);
        } catch (Exception e) {
            logisticsOrderOperateLogService.addOperateLog(logisticsGetLabelVO.getAuthMap().get("id"),
                    logisticsGetLabelVO.getDeliveryNo(), BusinessTypeEnum.GET_LABEL_LIST.getCode(), LogisticsPlatformEnum.UBI.getCode(),
                    RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsQueryVO), JSONUtil.toJsonStr(e.getMessage()));
            return failure(e.getMessage());
        }
    }

    /**
     * 渠道查询
     *
     * @param chanelQueryVO
     * @return
     */
    @Override
    public ApiResult<List<LogisticsSaleChannelEntity>> getChannel(ChanelQueryVO chanelQueryVO) {
        List<ServiceCataLog> serviceCataLogList;
        try {
            serviceCataLogList = ubiShipperService.getServiceCatalog(chanelQueryVO.getAuthMap());
            List<LogisticsSaleChannelEntity> list = LogisticsChannelConverter.INSTANCE.channelConvertByUBI(serviceCataLogList);
            logisticsOrderOperateLogService.addOperateLog(chanelQueryVO.getAuthMap().get("id"),
                    chanelQueryVO.getTransportMode(), BusinessTypeEnum.GET_CHANEL_LIST.getCode(), LogisticsPlatformEnum.UBI.getCode(),
                    RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(chanelQueryVO), JSONUtil.toJsonStr(serviceCataLogList));
            return success(list);
        } catch (Exception e) {
            logisticsOrderOperateLogService.addOperateLog(chanelQueryVO.getAuthMap().get("id"),
                    chanelQueryVO.getTransportMode(), BusinessTypeEnum.GET_CHANEL_LIST.getCode(), LogisticsPlatformEnum.UBI.getCode(),
                    RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(chanelQueryVO), JSONUtil.toJsonStr(e.getMessage()));
            return failure(e.getMessage());
        }

    }

    @Override
    public LogisticsPlatformEnum getPlatForm() {
        return LogisticsPlatformEnum.UBI;
    }
}
