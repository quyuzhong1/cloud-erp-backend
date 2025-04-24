package com.erp.server.tms.service.logistics;

import cn.hutool.json.JSONUtil;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.common.business.annotation.LogisticsPlatformType;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.common.core.utils.ValidatorUtil;
import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
import com.erp.model.tms.enums.BusinessTypeEnum;
import com.erp.model.tms.enums.LogisticsPlatformResultEnum;
import com.erp.model.tms.enums.RequestStatusEnums;
import com.erp.model.tms.vo.request.*;
import com.erp.model.tms.vo.response.*;
import com.erp.server.tms.convert.LogisticsOrderConverter;
import com.erp.server.tms.handler.AbstractLogisticsHandler;
import com.erp.server.tms.service.LogisticsOperateService;
import com.sdk.tms.ubi.model.catalog.response.Origin;
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
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author zdy
 * @ClassName UbiLogisticsHandlerImpl
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
    private LogisticsOperateService logisticsOperateService;

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
        ubiOrder.setInvoiceValue(logisticsOrderVO.getParceInfoVO().getTotalPrice().doubleValue());
        ubiOrder.setInvoiceCurrency(logisticsOrderVO.getParceInfoVO().getCurrency());
        ubiOrder.setSku(logisticsOrderVO.getLogisticsProductVOList().get(0).getSkuId());
        //订单信息
        List<OrderItem> orderItems = LogisticsOrderConverter.INSTANCE.orderItemsRequestByUBI(logisticsOrderVO.getLogisticsProductVOList());
        ubiOrder.setOrderItems(orderItems);
        ValidatorUtil.validateEntity(ubiOrder);
        try {
            List<OrderResponse> order = ubiShipperService.createOrder(logisticsOrderVO.getAuthMap(), ubiOrder);
            logisticsOperateService.pushOperateLog(logisticsOrderVO.getSourceId(),
                    logisticsOrderVO.getDeliveryNo(), BusinessTypeEnum.CREATE_ORDER.getCode(), LogisticsPlatformEnum.UBI.getCode(),
                    RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(logisticsOrderVO), JSONUtil.toJsonStr(order), false);
            //一票多件包裹信息 会有多层嵌套 暂不考虑
            LogisticsOrderResponseVO responseVO = LogisticsOrderResponseVO.builder()
                    .deliveryNo(order.get(0).getReferenceNo())
                    .transportNo(order.get(0).getOrderId())
                    .trackNo(order.get(0).getTrackingNo())
                    .build();
            return success(responseVO);
        } catch (Exception e) {
            log.error(e.getMessage());
            logisticsOperateService.pushOperateLog(logisticsOrderVO.getSourceId(),
                    logisticsOrderVO.getDeliveryNo(), BusinessTypeEnum.CREATE_ORDER.getCode(), LogisticsPlatformEnum.UBI.getCode(),
                    RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsOrderVO), JSONUtil.toJsonStr(e), true);
            return ApiResult.error(ApiError.CALL_THIRD_LOGISTICS_PLATFORM_ERROR.code, getPlatForm().getName() + ":" + e.getMessage());
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
                logisticsOperateService.pushOperateLog(logisticsQueryVO.getOrderId(),
                        logisticsQueryVO.getDeliveryNo(), BusinessTypeEnum.CANCEL_ORDER.getCode(), LogisticsPlatformEnum.UBI.getCode(),
                        RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(logisticsQueryVO), JSONUtil.toJsonStr(orderResponse), false);
                if ("Success".equalsIgnoreCase(orderResponse.getStatus())) {
                    responseVO.success();
                    responseVO.setDeliveryNo(orderResponse.getReferenceNo());
                    responseVO.setTransportNo(orderResponse.getOrderId());
                    responseVO.setTrackNo(orderResponse.getTrackingNo());
                } else {
                    isSuccess = false;
                    logisticsOperateService.pushOperateLog(logisticsQueryVO.getOrderId(),
                            logisticsQueryVO.getDeliveryNo(), BusinessTypeEnum.CANCEL_ORDER.getCode(), LogisticsPlatformEnum.UBI.getCode(),
                            RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsQueryVO), JSONUtil.toJsonStr(orderResponse), false);
                    responseVO.failure(getPlatForm().getName(), logisticsQueryVO.getDeliveryNo(), orderResponse.getErrors());
                    responseVO.setDeliveryNo(orderResponse.getReferenceNo());
                }
                responseVOS.add(responseVO);
            } catch (Exception e) {
                logisticsOperateService.pushOperateLog(logisticsQueryVO.getOrderId(),
                        logisticsQueryVO.getDeliveryNo(), BusinessTypeEnum.CANCEL_ORDER.getCode(), LogisticsPlatformEnum.UBI.getCode(),
                        RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsQueryVO), JSONUtil.toJsonStr(e), true);
                isSuccess = false;
            }
        }
        return isSuccess ? success(responseVOS) : failure(responseVOS);
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
        ValidatorUtil.validateEntity(holdRequest);
        try {
            if(Objects.isNull(logisticsInterceptOrderVO)){
                throw new ServiceException("拦截订单信息不能为空");
            }
            List<OrderResponse> orderResponses = ubiShipperService.interceptOrder(logisticsInterceptOrderVO.getAuthMap(), holdRequest);
            logisticsOperateService.pushOperateLog(logisticsInterceptOrderVO.getOrderId(),
                    logisticsInterceptOrderVO.getDeliveryNo(), BusinessTypeEnum.INTERCEPT_ORDER.getCode(), LogisticsPlatformEnum.UBI.getCode(),
                    RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(logisticsInterceptOrderVOS), JSONUtil.toJsonStr(orderResponses), false);
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
                        responseVO.failure(getPlatForm().getName(), orderResponse.getOrderId(), orderResponse.getErrors());
                        responseVOS.add(responseVO);
                    }
                });
            }
            return success(responseVOS);
        } catch (Exception e) {
            logisticsOperateService.pushOperateLog(logisticsInterceptOrderVO.getOrderId(),
                    logisticsInterceptOrderVO.getDeliveryNo(), BusinessTypeEnum.INTERCEPT_ORDER.getCode(), LogisticsPlatformEnum.UBI.getCode(),
                    RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsInterceptOrderVOS), JSONUtil.toJsonStr(e), true);
            return failure(getPlatForm().getName() + ":" + e.getMessage());
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
            if(logisticsQueryBaseVO == null){
                throw new ServiceException("参数异常");
            }
            List<TrackBase> trackNumber = ubiShipperService.getTrackNumber(logisticsQueryBaseVO.getAuthMap(), logisticsQueryVOList.stream().map(LogisticsQueryBaseVO::getDeliveryNo).collect(Collectors.toList()));

            logisticsOperateService.pullOperateLog(logisticsQueryBaseVO.getOrderId(),
                    logisticsQueryBaseVO.getDeliveryNo(), BusinessTypeEnum.QUERY_ORDER.getCode(), LogisticsPlatformEnum.UBI.getCode(),
                    RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(logisticsQueryVOList), JSONUtil.toJsonStr(trackNumber));
            //转换
            List<LogisticsOrderResponseVO> responseVOS = LogisticsOrderConverter.INSTANCE.ordersQueryByUBI(trackNumber);
            return success(responseVOS);
        } catch (Exception e) {
            logisticsOperateService.pullOperateLog(logisticsQueryBaseVO.getOrderId(),
                    logisticsQueryBaseVO.getTransportNo(), BusinessTypeEnum.QUERY_ORDER.getCode(), LogisticsPlatformEnum.UBI.getCode(),
                    RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsQueryVOList), JSONUtil.toJsonStr(e));
            return failure(getPlatForm().getName() + ":" + e.getMessage());
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
        LogisticsGetLabelVO logisticsGetLabelVO = logisticsQueryVO.stream().filter(e -> Objects.nonNull(e.getAuthMap())).findFirst().orElse(null);
        assert logisticsGetLabelVO != null;
        boolean isPrintPacking = false;
        if (StringUtils.isNotEmpty(logisticsGetLabelVO.getIsPdn()) && "Y".equalsIgnoreCase(logisticsGetLabelVO.getIsPdn())){
            isPrintPacking = true;
        }
        LabelRequest labelRequest = LabelRequest.builder()
                .orderIds(logisticsQueryVO.stream().map(LogisticsQueryBaseVO::getDeliveryNo).collect(Collectors.toList()))
                //TODO 根据传参决定打印单大小
                .labelType("1")
                .packinglist(isPrintPacking)
                .merged(true)
                .labelFormat("PDF")
                .dpi("203")
                .build();
        ValidatorUtil.validateEntity(labelRequest);
        try {
            List<LabelResponse> labelSpecs = ubiShipperService.getLabels(logisticsGetLabelVO.getAuthMap(), labelRequest);

            logisticsOperateService.pullOperateLog(logisticsGetLabelVO.getOrderId(),
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
            logisticsOperateService.pullOperateLog(logisticsGetLabelVO.getOrderId(),
                    logisticsGetLabelVO.getDeliveryNo(), BusinessTypeEnum.GET_LABEL_LIST.getCode(), LogisticsPlatformEnum.UBI.getCode(),
                    RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsQueryVO), JSONUtil.toJsonStr(e));
            return failure(getPlatForm().getName() + ":" + e.getMessage());
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
            List<LogisticsSaleChannelEntity> list = new ArrayList<>();
            //数据拆分
            if (CollectionUtils.isEmpty(serviceCataLogList)) return success(list);
            serviceCataLogList.forEach(serviceCataLog -> {
                List<String> serviceOptions = serviceCataLog.getServiceOptions();
                if (CollectionUtils.isNotEmpty(serviceOptions)) {
                    handleData(serviceCataLog, serviceOptions, list);
                }
            });

            logisticsOperateService.pullOperateLog(chanelQueryVO.getOrderId(),
                    chanelQueryVO.getTransportMode(), BusinessTypeEnum.GET_CHANEL_LIST.getCode(), LogisticsPlatformEnum.UBI.getCode(),
                    RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(chanelQueryVO), JSONUtil.toJsonStr(serviceCataLogList));
            return success(list);
        } catch (Exception e) {
            logisticsOperateService.pullOperateLog(chanelQueryVO.getOrderId(),
                    chanelQueryVO.getTransportMode(), BusinessTypeEnum.GET_CHANEL_LIST.getCode(), LogisticsPlatformEnum.UBI.getCode(),
                    RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(chanelQueryVO), JSONUtil.toJsonStr(e));
            return failure(getPlatForm().getName() + ":" + e.getMessage());
        }

    }

    private static void handleData(ServiceCataLog serviceCataLog, List<String> serviceOptions, List<LogisticsSaleChannelEntity> list) {
        serviceOptions.forEach(serviceOption -> {
            //产品确认只拉取该类型渠道数据
            if (serviceOption.contains("E-Parcel")) {
                //快递类型
                List<Origin> destinations = serviceCataLog.getDestinations();
                if (CollectionUtils.isNotEmpty(destinations)) {
                    destinations.forEach(destination -> {
                        List<Origin> origins = serviceCataLog.getOrigins();
                        if (CollectionUtils.isNotEmpty(origins)) {
                            origins.forEach(origin -> {
                                //发货国
                                LogisticsSaleChannelEntity entity = new LogisticsSaleChannelEntity()
                                        .setCode(serviceCataLog.getServiceCode())
                                        .setCnName(serviceCataLog.getServiceName())
                                        .setEnName(serviceCataLog.getNativeName())
                                        .setSupplierCode(serviceCataLog.getServiceProviderCode())
                                        .setSupplierName(serviceCataLog.getServiceProvider())
                                        .setLogisticsPlatform(LogisticsPlatformEnum.UBI.getCode())
                                        .setChannelStatus(MathUtil.ZERO)
                                        .setDestinationCountry(destination.getCountry())
                                        .setOriginCountry(origin.getCountry())
                                        .setShipmentMethod(serviceOption);
                                list.add(entity);
                            });
                        }
                    });
                }
            }

        });
    }

    /**
     * 授权判断
     * @param authMap
     * @return
     */
    @Override
    public ApiResult<Object>authorization(Map<String, String> authMap){
        try {
            ubiShipperService.getServiceCatalog(authMap);
            return success("授权成功");
        } catch (Exception e) {
            return failure(getPlatForm().getName() + ":" + e.getMessage());
        }
    }
    @Override
    public LogisticsPlatformEnum getPlatForm() {
        return LogisticsPlatformEnum.UBI;
    }

    @Override
    public ApiResult<List<LogisticsServiceResponseVO>> listLogisticsService(Map<String, String> authMap) {
        return ApiResult.error(-1, "功能未开放");

    }
}
