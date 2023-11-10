package com.erp.server.tms.service.logistics;

import cn.hutool.json.JSONUtil;
import com.common.business.annotation.LogisticsPlatformType;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
import com.erp.model.tms.enums.BusinessTypeEnums;
import com.erp.model.tms.enums.RequestStatusEnums;
import com.erp.model.tms.vo.request.*;
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
import io.seata.common.util.CollectionUtils;
import jodd.util.CollectionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
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
            OrderResponse order = ubiShipperService.createOrder(ubiOrder);
            logisticsOrderOperateLogService.addOperateLog(logisticsOrderVO.getLogisticsAuthEntity().getId(),
                    logisticsOrderVO.getDeliveryNo(), BusinessTypeEnums.CREATE_ORDER.getCode(), LogisticsPlatformEnum.UBI.getCode(),
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
            logisticsOrderOperateLogService.addOperateLog(logisticsOrderVO.getLogisticsAuthEntity().getId(),
                    logisticsOrderVO.getDeliveryNo(), BusinessTypeEnums.CREATE_ORDER.getCode(), LogisticsPlatformEnum.UBI.getCode(),
                    RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsOrderVO), JSONUtil.toJsonStr(e.getMessage()));
            return failure(e.getMessage());
        }
    }


    /**
     * 取消订单
     *
     * @param logisticsQueryVO
     * @return
     */
//    @Override
//    public ApiResult<String> cancelOrder(LogisticsCancelOrderVO logisticsQueryVO) {
//        //只支持单个订单取消
//        try {
//            OrderResponse orderResponse = ubiShipperService.deleteShipperOrder(logisticsQueryVO.getLogisticsAuthEntity().getAccount(),
//                    logisticsQueryVO.getLogisticsAuthEntity().getPassword(), logisticsQueryVO.getTransportNo().get(0));
//
//            logisticsOrderOperateLogService.addOperateLog(logisticsQueryVO.getLogisticsAuthEntity().getId(),
//                    logisticsQueryVO.getTransportNo().get(0), BusinessTypeEnums.CANCEL_ORDER.getCode(), LogisticsPlatformEnum.UBI.getCode(),
//                    RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(logisticsQueryVO), JSONUtil.toJsonStr(orderResponse));
//            return success(orderResponse.getStatus());
//        } catch (Exception e) {
//            logisticsOrderOperateLogService.addOperateLog(logisticsQueryVO.getLogisticsAuthEntity().getId(),
//                    logisticsQueryVO.getTransportNo().get(0), BusinessTypeEnums.CANCEL_ORDER.getCode(), LogisticsPlatformEnum.UBI.getCode(),
//                    RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsQueryVO), JSONUtil.toJsonStr(e.getMessage()));
//            return failure(e.getMessage());
//        }
//    }


    /**
     * 更新订单
     *
     * @param logisticsOrderVO
     * @return
     */
    @Override
    public ApiResult<String> updateOrder(LogisticsOrderVO logisticsOrderVO) {
        return ApiResult.error(-1, "功能未开放");
    }

    /**
     * 拦截订单
     *
     * @param logisticsQueryVO
     * @return
     */
//    @Override
//    public ApiResult<List<InterceptResponseVO>> interceptOrder(LogisticsInterceptOrderVO logisticsQueryVO) {
//        HoldRequest holdRequest = HoldRequest.builder()
//                .orderIds(logisticsQueryVO.getDeliveryNo())
//                .holdType(1)
//                .build();
//        try {
//            List<OrderResponse> orderResponses = ubiShipperService.interceptOrder(logisticsQueryVO.getLogisticsAuthEntity().getAccount(),
//                    logisticsQueryVO.getLogisticsAuthEntity().getPassword(), holdRequest);
//
//            logisticsOrderOperateLogService.addOperateLog(logisticsQueryVO.getLogisticsAuthEntity().getId(),
//                    logisticsQueryVO.getDeliveryNo().get(0), BusinessTypeEnums.INTERCEPT_ORDER.getCode(), LogisticsPlatformEnum.UBI.getCode(),
//                    RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(logisticsQueryVO), JSONUtil.toJsonStr(orderResponses));
//            List<InterceptResponseVO> responseVOS = new ArrayList<>();
//            if (CollectionUtils.isNotEmpty(orderResponses)) {
//                orderResponses.stream().forEach(orderResponse -> {
//                    responseVOS.add(InterceptResponseVO.builder()
////                            .errors(orderResponse.getErrors())
////                            .status(orderResponse.getStatus())
//                            .deliveryNo(orderResponse.getOrderId())
//                            .build());
//                });
//            }
//            return success(responseVOS);
//        } catch (Exception e) {
//            logisticsOrderOperateLogService.addOperateLog(logisticsQueryVO.getLogisticsAuthEntity().getId(),
//                    logisticsQueryVO.getDeliveryNo().get(0), BusinessTypeEnums.INTERCEPT_ORDER.getCode(), LogisticsPlatformEnum.UBI.getCode(),
//                    RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsQueryVO), JSONUtil.toJsonStr(e.getMessage()));
//            return failure(e.getMessage());
//        }
//    }

    /**
     * 查询订单(批量)
     *
     * @param logisticsQueryVOList
     * @return
     */
//    @Override
//    public ApiResult<List<LogisticsOrderResponseVO>> queryOrderList(LogisticsQueryBaseVO logisticsQueryVOList) {
//        try {
//            List<TrackBase> trackNumber = ubiShipperService.getTrackNumber(logisticsQueryVOList.getLogisticsAuthEntity().getAccount(),
//                    logisticsQueryVOList.getLogisticsAuthEntity().getPassword(), logisticsQueryVOList.getDeliveryNo());
//
//            logisticsOrderOperateLogService.addOperateLog(logisticsQueryVOList.getLogisticsAuthEntity().getId(),
//                    logisticsQueryVOList.getTransportNo().get(0), BusinessTypeEnums.QUERY_ORDER.getCode(), LogisticsPlatformEnum.UBI.getCode(),
//                    RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(logisticsQueryVOList), JSONUtil.toJsonStr(trackNumber));
//            //转换
//            List<LogisticsOrderResponseVO> responseVOS = LogisticsOrderConverter.INSTANCE.ordersQueryByUBI(trackNumber);
//            return success(responseVOS);
//        } catch (Exception e) {
//            logisticsOrderOperateLogService.addOperateLog(logisticsQueryVOList.getLogisticsAuthEntity().getId(),
//                    logisticsQueryVOList.getTransportNo().get(0), BusinessTypeEnums.QUERY_ORDER.getCode(), LogisticsPlatformEnum.UBI.getCode(),
//                    RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsQueryVOList), JSONUtil.toJsonStr(e.getMessage()));
//            return failure(e.getMessage());
//        }
//    }

    /**
     * 获取标签批量
     *
     * @param logisticsQueryVO
     * @return
     */
//    @Override
//    public ApiResult<List<LogisticsPrintLabelResponse>> getLabelList(LogisticsGetLabelVO logisticsQueryVO) {
//        LabelRequest labelRequest = LabelRequest.builder()
//                .orderIds(logisticsQueryVO.getDeliveryNo())
//                //TODO 根据传参决定打印单大小
//                .labelType("0")
//                .packinglist(false)
//                .merged(true)
//                .labelFormat("JPG")
//                .dpi("300")
//                .build();
//        try {
//            List<LabelResponse> labelSpecs = ubiShipperService.getLabels(logisticsQueryVO.getLogisticsAuthEntity().getAccount(),
//                    logisticsQueryVO.getLogisticsAuthEntity().getPassword(), labelRequest);
//
//            logisticsOrderOperateLogService.addOperateLog(logisticsQueryVO.getLogisticsAuthEntity().getId(),
//                    logisticsQueryVO.getTransportNo().get(0), BusinessTypeEnums.GET_LABEL_LIST.getCode(), LogisticsPlatformEnum.UBI.getCode(),
//                    RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(logisticsQueryVO), JSONUtil.toJsonStr(labelSpecs));
//            List<LogisticsPrintLabelResponse> responses = new ArrayList<>();
//            labelSpecs.forEach(labelResponse -> {
//                responses.add(LogisticsPrintLabelResponse.builder()
//                        .transportNoList(Collections.singletonList(labelResponse.getOrderId()))
//                        .base64(labelResponse.getLabelContent())
//                        .trackNoList(Collections.singletonList(labelResponse.getTrackingNo()))
//                        .build());
//            });
//            return success(responses);
//        } catch (Exception e) {
//            logisticsOrderOperateLogService.addOperateLog(logisticsQueryVO.getLogisticsAuthEntity().getId(),
//                    logisticsQueryVO.getTransportNo().get(0), BusinessTypeEnums.GET_LABEL_LIST.getCode(), LogisticsPlatformEnum.UBI.getCode(),
//                    RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsQueryVO), JSONUtil.toJsonStr(e.getMessage()));
//            return failure(e.getMessage());
//        }
//    }

    /**
     * 轨迹查询
     *
     * @param logisticsQueryVO
     * @return
     */
    public ApiResult getTrack(LogisticsQueryBaseVO logisticsQueryVO) {
        return ApiResult.error(-1, "功能未开放");
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
            serviceCataLogList = ubiShipperService.getServiceCatalog(chanelQueryVO.getLogisticsAuthEntity().getAccount(), chanelQueryVO.getLogisticsAuthEntity().getPassword());
            List<LogisticsSaleChannelEntity> list = LogisticsChannelConverter.INSTANCE.channelConvertByUBIList(serviceCataLogList);
            logisticsOrderOperateLogService.addOperateLog(chanelQueryVO.getLogisticsAuthEntity().getId(),
                    chanelQueryVO.getTransportMode(), BusinessTypeEnums.GET_CHANEL_LIST.getCode(), LogisticsPlatformEnum.UBI.getCode(),
                    RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(chanelQueryVO), JSONUtil.toJsonStr(serviceCataLogList));
            return success(list);
        } catch (Exception e) {
            logisticsOrderOperateLogService.addOperateLog(chanelQueryVO.getLogisticsAuthEntity().getId(),
                    chanelQueryVO.getTransportMode(), BusinessTypeEnums.GET_CHANEL_LIST.getCode(), LogisticsPlatformEnum.UBI.getCode(),
                    RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(chanelQueryVO), JSONUtil.toJsonStr(e.getMessage()));
            return failure(e.getMessage());
        }

    }
}
