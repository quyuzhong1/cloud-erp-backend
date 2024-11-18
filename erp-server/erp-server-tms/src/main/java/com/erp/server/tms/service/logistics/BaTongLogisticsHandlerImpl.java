package com.erp.server.tms.service.logistics;

import cn.hutool.json.JSONUtil;
import com.common.business.annotation.LogisticsPlatformType;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.utils.FileUtil;
import com.common.core.utils.MathUtil;
import com.common.core.utils.ValidatorUtil;
import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
import com.erp.model.tms.enums.BusinessTypeEnum;
import com.erp.model.tms.enums.PaperSizeEnum;
import com.erp.model.tms.enums.RequestStatusEnums;
import com.erp.model.tms.vo.request.*;
import com.erp.model.tms.vo.response.CancelResponseVO;
import com.erp.model.tms.vo.response.LogisticsOrderResponseVO;
import com.erp.model.tms.vo.response.LogisticsPrintLabelResponse;
import com.erp.model.tms.vo.response.LogisticsServiceResponseVO;
import com.erp.server.tms.convert.LogisticsChannelConverter;
import com.erp.server.tms.convert.LogisticsOrderConverter;
import com.erp.server.tms.handler.AbstractLogisticsHandler;
import com.erp.server.tms.service.LogisticsOperateService;
import com.erp.tms.batong.constants.BaTongConstants;
import com.erp.tms.batong.model.label.base.BaseData;
import com.erp.tms.batong.model.label.base.BaseResult;
import com.erp.tms.batong.model.label.request.AdditionalInfo;
import com.erp.tms.batong.model.label.request.ConfigInfo;
import com.erp.tms.batong.model.label.request.LabelRequest;
import com.erp.tms.batong.model.label.request.ListOrder;
import com.erp.tms.batong.model.label.response.LabelResponse;
import com.erp.tms.batong.model.order.request.*;
import com.erp.tms.batong.model.order.response.OrderResponse;
import com.erp.tms.batong.model.order.response.TrackBase;
import com.erp.tms.batong.service.BaTongService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * @author Lambda
 * @Classname BtLogisticsHandlerImpl
 * @Description 巴通物流商对接
 * @Date 2024-01-09 15:07
 * @Created by yl
 */
@Slf4j
@Component
@LogisticsPlatformType(LogisticsPlatformEnum.BaTong)
public class BaTongLogisticsHandlerImpl extends AbstractLogisticsHandler {

    @Resource
    private BaTongService baTongService;

    @Resource
    private LogisticsOperateService logisticsOperateService;


    @Override
    public ApiResult<LogisticsOrderResponseVO> createOrder(LogisticsOrderVO logisticsOrder) {
        LogisticsOrderResponseVO responseVO = new LogisticsOrderResponseVO();
        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setReferenceNo(logisticsOrder.getDeliveryNo());
        orderRequest.setShippingMethod(logisticsOrder.getLogisticsChannelEntity().getCode());
        ParceInfoVO parceInfoVO = logisticsOrder.getParceInfoVO();
        Boolean success = true;
        //总重量 单位g
        Integer totalWeight = parceInfoVO.getTotalWeight();
        if (Objects.nonNull(totalWeight)) {
            BigDecimal orderWeight = MathUtil.divide(new BigDecimal(totalWeight), new BigDecimal("1000"), 3);
            orderRequest.setOrderWeight(orderWeight.toString());
        }
        orderRequest.setOrderPieces("1");
        orderRequest.setCargoType("W");
        //发货人信息
        Shipper shipper = LogisticsOrderConverter.INSTANCE.orderShippingByBaTong(logisticsOrder);
        orderRequest.setShipper(shipper);

        //收货人信息
        Consignee consignee = LogisticsOrderConverter.INSTANCE.orderConsigneeByBaTong(logisticsOrder);
        orderRequest.setConsignee(consignee);
        List<LogisticsProductVO> logisticsProductList = logisticsOrder.getLogisticsProductVOList();
        //报关信息
        List<Invoice> invoiceList = LogisticsOrderConverter.INSTANCE.orderInvoiceByBaTong(logisticsProductList);
        orderRequest.setInvoiceList(invoiceList);

        //商品信息
        CargoVolume cargoVolume = LogisticsOrderConverter.INSTANCE.orderCargoVolumeByBaTong(parceInfoVO);
        List<CargoVolume> cargoVolumeList = Arrays.asList(cargoVolume);
        orderRequest.setCargoVolumeList(cargoVolumeList);
        try {
            BaseResult<Void> result = baTongService.createOrder(logisticsOrder.getAuthMap(), orderRequest);

            Integer createOrderSuccess = result.getSuccess();
            //表示成功
            if (BaTongConstants.SUCCESS.equals(createOrderSuccess)) {
                OrderResponse orderResponse = JSONUtil.toBean(JSONUtil.toJsonStr(result.getData()), OrderResponse.class);
                responseVO = LogisticsOrderResponseVO.builder()
                        .transportNo(orderResponse.getShippingMethodNo())
                        .trackNo(orderResponse.getChannelHawbcode())
                        .deliveryNo(logisticsOrder.getDeliveryNo())
                        .build();

                logisticsOperateService.pushOperateLog(logisticsOrder.getSourceId(),
                        logisticsOrder.getDeliveryNo(), BusinessTypeEnum.CREATE_ORDER.getCode(), LogisticsPlatformEnum.BaTong.getCode(),
                        RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(logisticsOrder), JSONUtil.toJsonStr(""), false);
            }else{
                success = false;
                responseVO.failure(LogisticsPlatformEnum.BaTong.getName(), logisticsOrder.getDeliveryNo(), result.getCnMessage());
                logisticsOperateService.pushOperateLog(logisticsOrder.getSourceId(),
                        logisticsOrder.getDeliveryNo(), BusinessTypeEnum.CREATE_ORDER.getCode(), LogisticsPlatformEnum.BaTong.getCode(),
                        RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsOrder),result.getCnMessage(), false);
            }

        } catch (Exception e) {
            log.error("巴通创建订单异常：{}", e.getMessage());
            logisticsOperateService.pushOperateLog(logisticsOrder.getSourceId(),
                    logisticsOrder.getDeliveryNo(), BusinessTypeEnum.CREATE_ORDER.getCode(), LogisticsPlatformEnum.BaTong.getCode(),
                    RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsOrder), JSONUtil.toJsonStr(e), true);
            success = false;
        }

        return success ? success(responseVO) : failure(responseVO);

    }

    /**
     * 获取物流原始渠道信息
     *
     * @param chanelQueryVO
     * @return
     */
    @Override
    public ApiResult<List<LogisticsSaleChannelEntity>> getChannel(ChanelQueryVO chanelQueryVO) {
        try {
            List<BaseData> baseList = baTongService.listShippingMethod(chanelQueryVO.getAuthMap());
            List<LogisticsSaleChannelEntity> list = new ArrayList<>(baseList.size());
            if (CollectionUtils.isEmpty(baseList)) {
                return success(list);
            }
            list = LogisticsChannelConverter.INSTANCE.channelConvertByBaTong(baseList);
            logisticsOperateService.pullOperateLog(chanelQueryVO.getOrderId(),
                    chanelQueryVO.getTransportMode(), BusinessTypeEnum.GET_CHANEL_LIST.getCode(), LogisticsPlatformEnum.BaTong.getCode(),
                    RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(chanelQueryVO), JSONUtil.toJsonStr(baseList));
            return success(list);
        } catch (Exception e) {
            log.error("获取物流原始渠道信息异常 {}", e.getMessage());
        }

        return success(Collections.emptyList());
    }


    /**
     * 取消订单
     *
     * @param logisticsCancelOrderList
     * @return
     */
    @Override
    public ApiResult<List<CancelResponseVO>> cancelOrder(List<LogisticsCancelOrderVO> logisticsCancelOrderList) {
        return ApiResult.error(-1, "功能未开放");
    }



    public ApiResult<List<CancelResponseVO>> deleteOrder(List<LogisticsCancelOrderVO> logisticsCancelOrderList) {
        List<CancelResponseVO> result = new ArrayList<>();
        Boolean isSuccess = true;

        LogisticsCancelOrderVO logisticsCancelOrderVO = logisticsCancelOrderList.stream().filter(e -> Objects.nonNull(e.getAuthMap())).findFirst().orElse(null);
        Map<String, String> authMap = logisticsCancelOrderVO.getAuthMap();
        if (Objects.isNull(authMap)) {
            return failure("缺少授权信息");
        }
        CancelResponseVO responseVO = new CancelResponseVO();

        for (LogisticsCancelOrderVO item : logisticsCancelOrderList) {
            try {
                responseVO.setDeliveryNo(item.getDeliveryNo());
                BaseResult<String> cancelResult = baTongService.deleteOrder(authMap, item.getDeliveryNo());
                if (!BaTongConstants.SUCCESS.equals(cancelResult.getSuccess())) {
                    isSuccess = false;
                    responseVO.failure(getPlatForm().getName(),item.getDeliveryNo(),cancelResult.getCnMessage());
                    logisticsOperateService.pushOperateLog(item.getOrderId(),
                            item.getDeliveryNo(), BusinessTypeEnum.CANCEL_ORDER.getCode(), LogisticsPlatformEnum.BaTong.getCode(),
                            RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(item), JSONUtil.toJsonStr(cancelResult), false);
                }else{
                    //成功
                    responseVO.success();
                    logisticsOperateService.pushOperateLog(item.getOrderId(),
                            item.getDeliveryNo(), BusinessTypeEnum.CANCEL_ORDER.getCode(), LogisticsPlatformEnum.BaTong.getCode(),
                            RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(responseVO), JSONUtil.toJsonStr(cancelResult), false);
                }
            } catch (Exception e) {
                isSuccess = false;
                responseVO.failure(getPlatForm().getName(),item.getDeliveryNo(),e.getMessage());
                logisticsOperateService.pushOperateLog(item.getOrderId(),
                        item.getDeliveryNo(), BusinessTypeEnum.CANCEL_ORDER.getCode(), LogisticsPlatformEnum.BaTong.getCode(),
                        RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(item), JSONUtil.toJsonStr(e), true);
            }

            result.add(responseVO);
        }
        return isSuccess ? success(result) : failure(result);

    }


    /**
     * 更新重量
     *
     * @return
     */
    @Override
    public ApiResult<String> updateWeight(LogisticsUpdateWeightVO logisticsUpdateWeightVO) {
        try {
            BaTongUpdateWeightReq request = BaTongUpdateWeightReq.builder()
                    .referenceNo(logisticsUpdateWeightVO.getDeliveryNo())
                    .orderWeight(logisticsUpdateWeightVO.getWeight().divide(new BigDecimal(1000),4, RoundingMode.HALF_UP).toString())
                    .build();
            ValidatorUtil.validateEntity(request);
            BaseResult<String> response = baTongService.updateWeight(logisticsUpdateWeightVO.getAuthMap(), request);

            if (!BaTongConstants.SUCCESS.equals(response.getSuccess())) {
                logisticsOperateService.pushOperateLog(logisticsUpdateWeightVO.getOrderId(),
                        logisticsUpdateWeightVO.getDeliveryNo(), BusinessTypeEnum.UPDATE_WEIGHT.getCode(), LogisticsPlatformEnum.BaTong.getCode(),
                        RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsUpdateWeightVO), JSONUtil.toJsonStr(response),false);
                return ApiResult.error(ApiError.CALL_THIRD_LOGISTICS_PLATFORM_ERROR.code,response.getCnMessage());
            }
            logisticsOperateService.pushOperateLog(logisticsUpdateWeightVO.getOrderId(),
                    logisticsUpdateWeightVO.getDeliveryNo(), BusinessTypeEnum.UPDATE_WEIGHT.getCode(), LogisticsPlatformEnum.BaTong.getCode(),
                    RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(logisticsUpdateWeightVO), JSONUtil.toJsonStr(response),false);
            return success();
        }catch (Exception e){
            logisticsOperateService.pushOperateLog(logisticsUpdateWeightVO.getOrderId(),
                    logisticsUpdateWeightVO.getDeliveryNo(), BusinessTypeEnum.UPDATE_WEIGHT.getCode(), LogisticsPlatformEnum.BaTong.getCode(),
                    RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsUpdateWeightVO), JSONUtil.toJsonStr(e),true);
            return failure(getPlatForm().getName() + ":" + e.getMessage());
        }
    }


    /**
     * 获取标签
     *
     * @param
     * @return
     * @description
     * @author Lambda
     * @create 2024-01-16 14:13
     */
    @Override
    public ApiResult<List<LogisticsPrintLabelResponse>> getLabelList(List<LogisticsGetLabelVO> logisticsQueryList) {
        LogisticsGetLabelVO logisticsGetLabelVO = logisticsQueryList.stream().filter(e -> Objects.nonNull(e.getAuthMap())).findFirst().orElse(null);
        assert logisticsGetLabelVO != null;

        List<LogisticsPrintLabelResponse> responseList = new ArrayList<>(logisticsQueryList.size());
        Boolean isSuccess = true;

        for (LogisticsGetLabelVO item : logisticsQueryList) {
            try {
                Map<String, String> authMap = item.getAuthMap();
                String deliveryNo = item.getDeliveryNo();
                //是否打印配货单
                Boolean isPrintPacking = "Y".equalsIgnoreCase(item.getIsPdn());
                //是否打印报关单
                Boolean isPrintDeclare = "Y".equalsIgnoreCase(logisticsGetLabelVO.getIsPcd());

                ConfigInfo configInfo = new ConfigInfo();
                //pdf
                configInfo.setLabelFileType("2");
                String labelType = item.getLabelType();
                String a4Code = PaperSizeEnum.A4.getCode();
                String labelPaperType = "1";
                if (a4Code.equals(labelType)) {
                    labelPaperType = "2";
                }
                configInfo.setLabelPaperType(labelPaperType);
                //标签内容类型
                String labelContentType = getLabelContentType(isPrintPacking, isPrintDeclare);
                configInfo.setLabelContentType(labelContentType);
                AdditionalInfo additionalInfo = new AdditionalInfo();
                //是否打印配货信息
                additionalInfo.setLabelPrintInvoiceInfo(item.getIsPdn());
                additionalInfo.setLabelPrintBuyerid("N");
                additionalInfo.setLabelPrintDatetime("Y");
                additionalInfo.setCustomsDeclarationPrintActualWeight("N");
                configInfo.setAdditionalInfo(additionalInfo);
                ListOrder listOrder = ListOrder.builder().referenceNo(deliveryNo).build();
                LabelRequest labelRequest = LabelRequest.builder().
                        configInfo(configInfo).
                        orderList(Arrays.asList(listOrder)).build();
                BaseResult<String> result = baTongService.getLabel(authMap, labelRequest);
                //表示失败
                if (!BaTongConstants.SUCCESS.equals(result.getSuccess())) {
                    LogisticsPrintLabelResponse response = new LogisticsPrintLabelResponse();
                    response.failure(getPlatForm().getName(), logisticsGetLabelVO.getDeliveryNo(), result.getCnMessage());
                    responseList.add(response);
                    logisticsOperateService.pullOperateLog(logisticsGetLabelVO.getOrderId(),
                            logisticsGetLabelVO.getDeliveryNo(), BusinessTypeEnum.GET_LABEL.getCode(), LogisticsPlatformEnum.BaTong.getCode(),
                            RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsGetLabelVO), JSONUtil.toJsonStr(result));
                    isSuccess = false;
                    continue;
                }
                LogisticsPrintLabelResponse response = new LogisticsPrintLabelResponse();
                response.setDeliveryNoList(Collections.singletonList(deliveryNo));
                response.setTrackNoList(Collections.singletonList(item.getTrackNo()));
                response.setTransportNoList(Collections.singletonList(item.getTransportNo()));
                List<LabelResponse> labelResponseList = JSONUtil.toList(JSONUtil.toJsonStr(result.getData()), LabelResponse.class);
                if (CollectionUtils.isEmpty(labelResponseList)) {
                    LogisticsPrintLabelResponse response1 = new LogisticsPrintLabelResponse();
                    response1.failure(getPlatForm().getName(), logisticsGetLabelVO.getDeliveryNo(), "没有返回文件url");
                    responseList.add(response);
                    isSuccess = false;
                    continue;
                }
                LabelResponse labelResponse= labelResponseList.get(0);
                String labelUrl = labelResponse.getLabelUrl();
                String base64 = FileUtil.convertPdfUrlToBase64(labelUrl);
                labelResponse.setBase64(base64);
                response.setBase64(base64);
                logisticsOperateService.pullOperateLog(logisticsGetLabelVO.getOrderId(),
                        logisticsGetLabelVO.getDeliveryNo(), BusinessTypeEnum.GET_LABEL.getCode(), LogisticsPlatformEnum.BaTong.getCode(),
                        RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(logisticsGetLabelVO), JSONUtil.toJsonStr(labelResponse));

                responseList.add(response);
            } catch (Exception e) {
                logisticsOperateService.pullOperateLog(logisticsGetLabelVO.getOrderId(),
                        logisticsGetLabelVO.getDeliveryNo(), BusinessTypeEnum.GET_LABEL_LIST.getCode(), LogisticsPlatformEnum.BaTong.getCode(),
                        RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(item), JSONUtil.toJsonStr(e));
                return failure(getPlatForm().getName() + ":" + e.getMessage());
            }

        }

        return isSuccess ? success(responseList) : failure(responseList);
    }


    /**
     * 获取订单列表
     *
     * @return
     * @description
     * @author Lambda
     * @create 2024-01-16 18:09
     */
    @Override
    public ApiResult<List<LogisticsOrderResponseVO>> queryOrderList(List<LogisticsQueryBaseVO> logisticsQueryVOList) {
        List<LogisticsOrderResponseVO> resultList = new ArrayList<>(logisticsQueryVOList.size());
        for (LogisticsQueryBaseVO item : logisticsQueryVOList) {
            Map<String, String> authMap = item.getAuthMap();
            ListOrder listOrder = ListOrder.builder().
                    referenceNo(item.getDeliveryNo()).
                    build();
            try {
                TrackBase trackBase = baTongService.getTrack(authMap, listOrder);
                LogisticsOrderResponseVO responseVO = LogisticsOrderResponseVO.builder().
                        transportNo(trackBase.getShippingMethodNo()).
                        trackNo(trackBase.getShippingMethodNo()).build();
                resultList.add(responseVO);
            } catch (Exception e) {
                logisticsOperateService.pullOperateLog(item.getOrderId(),
                        item.getTransportNo(), BusinessTypeEnum.QUERY_ORDER.getCode(), LogisticsPlatformEnum.BaTong.getCode(),
                        RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsQueryVOList), JSONUtil.toJsonStr(e));
                return failure(getPlatForm().getName() + ":" + e.getMessage());
            }

        }
        return success(resultList);

    }

    /**
     * 标签内容类型代码
     * 1：标签
     * 2：报关单
     * 3：配货单
     * 4：标签+报关单
     * 5：标签+配货单
     * 6：标签+报关单+配货单
     *
     * @param isPrintPacking 是否打印配货单
     * @param isPrintDeclare 是否打印报关单
     * @return
     */
    private String getLabelContentType(Boolean isPrintPacking, Boolean isPrintDeclare) {
        if (isPrintPacking && isPrintDeclare) {
            return "6";
        }
        if (isPrintPacking) {
            return "3";
        }
        if (isPrintDeclare) {
            return "2";
        }
        return "1";
    }


    /**
     * 授权判断
     *
     * @return
     */
    @Override
    public ApiResult<Object>authorization(Map<String, String> authMap) {
        try {
            baTongService.listShippingMethod(authMap);
            return success();
        } catch (Exception e) {
            return failure(getPlatForm().getName() + ":" + e.getMessage());

        }
    }

    @Override
    public LogisticsPlatformEnum getPlatForm() {
        return LogisticsPlatformEnum.BaTong;
    }

    @Override
    public ApiResult<List<LogisticsServiceResponseVO>> listLogisticsService(Map<String, String> authMap) {
        return ApiResult.error(-1, "功能未开放");

    }


}
