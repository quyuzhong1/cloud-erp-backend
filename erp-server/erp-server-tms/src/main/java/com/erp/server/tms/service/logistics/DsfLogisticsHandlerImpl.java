package com.erp.server.tms.service.logistics;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.api.utils.StringUtils;
import com.common.business.annotation.LogisticsPlatformType;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.enums.CurrencyEnum;
import com.common.core.exception.ServiceException;
import com.common.core.utils.FileUtil;
import com.common.core.utils.MathUtil;
import com.common.core.utils.ValidatorUtil;
import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
import com.erp.model.tms.enums.BusinessTypeEnum;
import com.erp.model.tms.enums.RequestStatusEnums;
import com.erp.model.tms.vo.request.*;
import com.erp.model.tms.vo.response.*;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.server.tms.convert.LogisticsChannelConverter;
import com.erp.server.tms.convert.LogisticsOrderConverter;
import com.erp.server.tms.handler.AbstractLogisticsHandler;
import com.erp.server.tms.service.LogisticsOperateService;
import com.sdk.tms.disifang.model.base.ResponseMsg;
import com.sdk.tms.disifang.model.chanel.response.ChanelInfo;
import com.sdk.tms.disifang.model.label.request.LabelRequest;
import com.sdk.tms.disifang.model.order.request.*;
import com.sdk.tms.disifang.model.order.response.OrderResponse;
import com.sdk.tms.disifang.model.order.response.QueryOrderResponse;
import com.sdk.tms.disifang.model.product.request.ChanelRequest;
import com.sdk.tms.disifang.service.DsfShipperService;
import io.seata.common.util.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author zdy
 * @ClassName DsfLogisticsHandlerImpl
 * @date 2023年11月02日
 * @version: 1.0
 */
@Slf4j
@Component
@LogisticsPlatformType(LogisticsPlatformEnum.DSF)
public class DsfLogisticsHandlerImpl extends AbstractLogisticsHandler {
    public static final String YYYY_MM_DD = "yyyy-MM-dd";
    @Resource
    private DsfShipperService dsfShipperService;
    @Resource
    private LogisticsOperateService logisticsOperateService;
    @Resource
    private DmpTaskFeign dmpTaskFeign;

    private List<Parcel> getParcel(LogisticsOrderVO logisticsOrderVO) {
        if (CollectionUtils.isEmpty(logisticsOrderVO.getLogisticsProductVOList())) {
            return Collections.emptyList();
        }
        Parcel parcel = new Parcel();
        ParceInfoVO parceInfoVO = logisticsOrderVO.getParceInfoVO();
        parcel.setLength(BigDecimal.valueOf(parceInfoVO.getLength()));
        parcel.setWidth(BigDecimal.valueOf(parceInfoVO.getWidth()));
        parcel.setHeight(BigDecimal.valueOf(parceInfoVO.getHeight()));
        parcel.setWeight(parceInfoVO.getTotalWeight());
        parcel.setParcel_value(parceInfoVO.getTotalPrice());
        parcel.setCurrency(parceInfoVO.getCurrency());
        String includeBattery = "N";
        LogisticsProductVO logisticsProductVO1 = logisticsOrderVO.getLogisticsProductVOList().stream().filter(e -> Objects.nonNull(e.getIsElectric()) && e.getIsElectric()).findFirst().orElse(null);
        if (Objects.nonNull(logisticsProductVO1) && logisticsProductVO1.getIsElectric()){
            includeBattery = "Y";
        }
        parcel.setInclude_battery(includeBattery);
        List<DeclareProductInfo> declareProductInfos = new ArrayList<>(logisticsOrderVO.getLogisticsProductVOList().size());
        logisticsOrderVO.getLogisticsProductVOList().forEach(logisticsProductVO -> {
            //海关申报信息
            DeclareProductInfo productInfo = LogisticsOrderConverter.INSTANCE.dsfProductMapping(logisticsProductVO);
            if (StringUtils.isEmpty(productInfo.getCountry_export())) {
                productInfo.setCountry_export(logisticsOrderVO.getSenderInfo().getCountry());
            }
            if (StringUtils.isEmpty(productInfo.getCountry_import())) {
                productInfo.setCountry_import(logisticsOrderVO.getReceiverInfoVO().getCountry());
            }
            if (StringUtils.isEmpty(productInfo.getCurrency_export())) {
                productInfo.setCurrency_export(logisticsOrderVO.getParceInfoVO().getCurrency());
            }
            if (StringUtils.isEmpty(productInfo.getCurrency_import())) {
                productInfo.setCurrency_import(logisticsOrderVO.getParceInfoVO().getCurrency());
            }
            //目的国申报币种处理
            BigDecimal destDeclarePrice = logisticsProductVO.getDestDeclarePrice();
            //当不是美元时，进行转换
            if (Objects.nonNull(destDeclarePrice) && !CurrencyEnum.USD.getCurrencyCode().equals(logisticsProductVO.getDestCurrency())){
                String currency = CurrencyEnum.CNY.getCurrencyCode();
                //默认出口申报币种为人民币
                if (!StringUtils.isBlank(logisticsProductVO.getDestCurrency())){
                    currency = logisticsProductVO.getDestCurrency();
                }
                BigDecimal exchangeRate1 = dmpTaskFeign.getRate(LocalDate.now().format(DateTimeFormatter.ofPattern(YYYY_MM_DD)), currency);
                if (Objects.isNull(exchangeRate1)){
                    throw new ServiceException(ApiError.ERROR_EXCHANGE_RATE_NOT_EXIST, LocalDate.now(), currency);
                }
                //先转换成人民币
                BigDecimal cnyDestDeclarePrice = MathUtil.multiply(destDeclarePrice, exchangeRate1).setScale(4, RoundingMode.HALF_UP);
                //再统一转换成美元
                BigDecimal exchangeRate2 = dmpTaskFeign.getRate(LocalDate.now().format(DateTimeFormatter.ofPattern(YYYY_MM_DD)), CurrencyEnum.USD.getCurrencyCode());
                if (Objects.isNull(exchangeRate2)){
                    throw new ServiceException(ApiError.ERROR_EXCHANGE_RATE_NOT_EXIST, LocalDate.now(), CurrencyEnum.USD.getCurrencyCode());
                }
                BigDecimal usdDestDeclarePrice = MathUtil.divide(cnyDestDeclarePrice, exchangeRate2).setScale(4, RoundingMode.HALF_UP);
                productInfo.setDeclare_unit_price_import(usdDestDeclarePrice);
                productInfo.setCurrency_import(CurrencyEnum.USD.getCurrencyCode());
            }
            //出口国币种处理 人民币转美元（目的国申报价默认美金）
            BigDecimal declarePrice = logisticsProductVO.getDeclarePrice();
            if (Objects.nonNull(declarePrice)){
                String currency = CurrencyEnum.CNY.getCurrencyCode();
                //默认出口申报币种为人民币
                if (!StringUtils.isBlank(logisticsProductVO.getDeclareCurrency())){
                    currency = logisticsProductVO.getDeclareCurrency();
                }
                BigDecimal exchangeRate1 = dmpTaskFeign.getRate(LocalDate.now().format(DateTimeFormatter.ofPattern(YYYY_MM_DD)), currency);
                if (Objects.isNull(exchangeRate1)){
                    throw new ServiceException(ApiError.ERROR_EXCHANGE_RATE_NOT_EXIST, LocalDate.now(), currency);
                }
                //先转换成人民币
                BigDecimal cnyDeclarePrice = MathUtil.multiply(declarePrice, exchangeRate1).setScale(4, RoundingMode.HALF_UP);
                //再统一转换成美元
                BigDecimal exchangeRate2 = dmpTaskFeign.getRate(LocalDate.now().format(DateTimeFormatter.ofPattern(YYYY_MM_DD)), CurrencyEnum.USD.getCurrencyCode());
                if (Objects.isNull(exchangeRate2)){
                    throw new ServiceException(ApiError.ERROR_EXCHANGE_RATE_NOT_EXIST, LocalDate.now(), CurrencyEnum.USD.getCurrencyCode());
                }
                BigDecimal usdDeclarePrice = MathUtil.divide(cnyDeclarePrice, exchangeRate2).setScale(4, RoundingMode.HALF_UP);
                productInfo.setDeclare_unit_price_export(usdDeclarePrice);
                productInfo.setCurrency_export(CurrencyEnum.USD.getCurrencyCode());
            }
            declareProductInfos.add(productInfo);
        });
        parcel.setDeclare_product_info(declareProductInfos);
        return Collections.singletonList(parcel);
    }

    @Override
    public ApiResult<LogisticsOrderResponseVO> createOrder(LogisticsOrderVO logisticsOrderVO) {
        LogisticsOrderResponseVO responseVO = new LogisticsOrderResponseVO();
        OrderRequest orderRequest = LogisticsOrderConverter.INSTANCE.orderRequestToDsf(logisticsOrderVO);
        //包裹信息封装
        orderRequest.setParcelList(getParcel(logisticsOrderVO));
        ValidatorUtil.validateEntity(orderRequest);
        boolean success = true;
        try {
            ResponseMsg responseMsg = dsfShipperService.createOrder(logisticsOrderVO.getAuthMap(), orderRequest);
            if (StringUtils.isBlank(responseMsg.getResult()) || !Objects.equals("1", responseMsg.getResult())) {
                responseVO.failure(LogisticsPlatformEnum.DSF.getName(), logisticsOrderVO.getDeliveryNo(), JSON.toJSONString(responseMsg.getErrors()));
                logisticsOperateService.pushOperateLog(logisticsOrderVO.getSourceId(),
                        logisticsOrderVO.getDeliveryNo(), BusinessTypeEnum.CREATE_ORDER.getCode(), LogisticsPlatformEnum.DSF.getCode(),
                        RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsOrderVO), JSONUtil.toJsonStr(responseMsg), false);
                success = false;
            } else {
                OrderResponse orderResponse = JSON.parseObject(JSON.toJSONString(responseMsg.getData()), OrderResponse.class);
                responseVO = LogisticsOrderResponseVO.builder()
                        .transportNo(orderResponse.getRef_no())
                        .trackNo(orderResponse.getTracking_no())
                        .transportNo(orderResponse.getDs_consignment_no())
                        .logisticsChannelNo(orderResponse.getLogistics_channel_no())
                        .odaResultSign(orderResponse.getOda_result_sign())
                        .build();
                logisticsOperateService.pushOperateLog(logisticsOrderVO.getSourceId(),
                        logisticsOrderVO.getDeliveryNo(), BusinessTypeEnum.CREATE_ORDER.getCode(), LogisticsPlatformEnum.DSF.getCode(),
                        RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(logisticsOrderVO), JSONUtil.toJsonStr(responseMsg), false);
            }
        } catch (Exception e) {
            log.error("递四方创建订单异常：{}", e.getMessage());
            logisticsOperateService.pushOperateLog(logisticsOrderVO.getSourceId(),
                    logisticsOrderVO.getDeliveryNo(), BusinessTypeEnum.CREATE_ORDER.getCode(), LogisticsPlatformEnum.DSF.getCode(),
                    RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsOrderVO), JSONUtil.toJsonStr(e), true);
            success = false;
        }
        return success ? success(responseVO) : failure(responseVO);
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
                OrderCancelRequest orderCancelRequest = OrderCancelRequest.builder()
                        .request_no(logisticsQueryVO.getDeliveryNo())
                        .cancel_reason(StringUtils.isBlank(logisticsQueryVO.getReason()) ? "订单取消" : logisticsQueryVO.getReason())
                        .build();
                ValidatorUtil.validateEntity(orderCancelRequest);
                ResponseMsg orderResponse = dsfShipperService.cancelOrder(logisticsCancelOrderVO.getAuthMap(), orderCancelRequest);
                logisticsOperateService.pushOperateLog(logisticsQueryVO.getOrderId(),
                        logisticsQueryVO.getDeliveryNo(), BusinessTypeEnum.CANCEL_ORDER.getCode(), LogisticsPlatformEnum.DSF.getCode(),
                        RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(logisticsQueryVO), JSONUtil.toJsonStr(orderResponse), false);
                responseVO.setDeliveryNo(logisticsQueryVO.getDeliveryNo());
                responseVO.setTransportNo(logisticsQueryVO.getTransportNo());
                responseVO.setTrackNo(logisticsQueryVO.getTrackNo());
                if ("1".equalsIgnoreCase(orderResponse.getResult())) {
                    responseVO.success();
                } else {
                    isSuccess = false;
                    logisticsOperateService.pushOperateLog(logisticsQueryVO.getOrderId(),
                            logisticsQueryVO.getDeliveryNo(), BusinessTypeEnum.CANCEL_ORDER.getCode(), LogisticsPlatformEnum.DSF.getCode(),
                            RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsQueryVO), JSONUtil.toJsonStr(orderResponse), false);
                    responseVO.failure(LogisticsPlatformEnum.DSF.getName(), logisticsQueryVO.getDeliveryNo(), orderResponse.getMsg());
                }
                responseVOS.add(responseVO);
            } catch (Exception e) {
                logisticsOperateService.pushOperateLog(logisticsQueryVO.getOrderId(),
                        logisticsQueryVO.getDeliveryNo(), BusinessTypeEnum.CANCEL_ORDER.getCode(), LogisticsPlatformEnum.DSF.getCode(),
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
    public ApiResult<List<InterceptResponseVO>> interceptOrder(List<LogisticsInterceptOrderVO> logisticsInterceptOrderVOS) {
        List<InterceptResponseVO> responseVOS = new ArrayList<>();
        boolean isSuccess = true;
        for (LogisticsInterceptOrderVO logisticsQueryVO : logisticsInterceptOrderVOS) {
            //只支持单个订单取消
            InterceptResponseVO responseVO = new InterceptResponseVO();
            try {
                OrderInterceptRequest orderInterceptRequest = OrderInterceptRequest.builder()
                        .request_no(logisticsQueryVO.getDeliveryNo())
                        .is_hold("Y")
                        .holdReason(StringUtils.isBlank(logisticsQueryVO.getInterceptReason()) ? "订单拦截" : logisticsQueryVO.getInterceptReason())
                        .build();
                ValidatorUtil.validateEntity(orderInterceptRequest);
                ResponseMsg orderResponse = dsfShipperService.interceptOrder(logisticsQueryVO.getAuthMap(), orderInterceptRequest);
                responseVO.setDeliveryNo(logisticsQueryVO.getDeliveryNo());
                responseVO.setTransportNo(logisticsQueryVO.getTransportNo());
                responseVO.setTrackNo(logisticsQueryVO.getTrackNo());
                if ("1".equalsIgnoreCase(orderResponse.getResult())) {
                    responseVO.success();
                    logisticsOperateService.pushOperateLog(logisticsQueryVO.getOrderId(),
                            logisticsQueryVO.getDeliveryNo(), BusinessTypeEnum.INTERCEPT_ORDER.getCode(), LogisticsPlatformEnum.DSF.getCode(),
                            RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(logisticsQueryVO), JSONUtil.toJsonStr(orderResponse), false);
                } else {
                    isSuccess = false;
                    logisticsOperateService.pushOperateLog(logisticsQueryVO.getOrderId(),
                            logisticsQueryVO.getDeliveryNo(), BusinessTypeEnum.INTERCEPT_ORDER.getCode(), LogisticsPlatformEnum.DSF.getCode(),
                            RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsQueryVO), JSONUtil.toJsonStr(orderResponse), false);
                    responseVO.failure(LogisticsPlatformEnum.DSF.getName(), logisticsQueryVO.getDeliveryNo(), orderResponse.getMsg());
                }
                responseVOS.add(responseVO);
            } catch (Exception e) {
                logisticsOperateService.pushOperateLog(logisticsQueryVO.getOrderId(),
                        logisticsQueryVO.getDeliveryNo(), BusinessTypeEnum.INTERCEPT_ORDER.getCode(), LogisticsPlatformEnum.DSF.getCode(),
                        RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsQueryVO), JSONUtil.toJsonStr(e), true);
                isSuccess = false;
            }
        }
        return isSuccess ? success(responseVOS) : failure(responseVOS);
    }

    /**
     * 查询订单(批量)
     *
     * @param logisticsQueryVOList
     * @return
     */
    @Override
    public ApiResult<List<LogisticsOrderResponseVO>> queryOrderList(List<LogisticsQueryBaseVO> logisticsQueryVOList) {
        List<LogisticsOrderResponseVO> list = new ArrayList<>();
        boolean success = true;
        for (LogisticsQueryBaseVO logisticsQueryBaseVO : logisticsQueryVOList) {
            OrderQueryRequest orderQueryRequest = OrderQueryRequest.builder()
                    .request_no(logisticsQueryBaseVO.getDeliveryNo())
                    .build();
            try {
                ValidatorUtil.validateEntity(orderQueryRequest);
                ResponseMsg responseMsg = dsfShipperService.queryOrder(logisticsQueryBaseVO.getAuthMap(), orderQueryRequest);
                //失败
                if (StringUtils.isBlank(responseMsg.getResult()) || !Objects.equals("1", responseMsg.getResult())) {
                    logisticsOperateService.pullOperateLog(logisticsQueryBaseVO.getOrderId(),
                            logisticsQueryBaseVO.getTransportNo(), BusinessTypeEnum.QUERY_ORDER.getCode(), LogisticsPlatformEnum.DSF.getCode(),
                            RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsQueryBaseVO), JSONUtil.toJsonStr(responseMsg));
                    LogisticsOrderResponseVO responseVO = new LogisticsOrderResponseVO();
                    responseVO.failure(LogisticsPlatformEnum.DSF.getName(), logisticsQueryBaseVO.getDeliveryNo(), responseMsg.getMsg());
                    list.add(responseVO);
                    success = false;
                } else {
                    List<QueryOrderResponse> responses = JSON.parseArray(responseMsg.getData().toString(), QueryOrderResponse.class);
                    if (CollectionUtils.isNotEmpty(responses)) {
                        responses.forEach(queryOrderResponse -> {
                            LogisticsOrderResponseVO orderResponseVO = LogisticsOrderResponseVO.builder()
                                    .deliveryNo(queryOrderResponse.getConsignmentInfo().getRef_no())
                                    .trackNo(queryOrderResponse.getConsignmentInfo().getTrackingNo())
                                    .transportNo(queryOrderResponse.getConsignmentInfo().getDs_consignment_no())
                                    .logisticsChannelNo(queryOrderResponse.getConsignmentInfo().getLogistics_channel_no())
                                    .odaResultSign(queryOrderResponse.getConsignmentInfo().getOda_result_sign())
                                    .build();
                            logisticsOperateService.pullOperateLog(logisticsQueryBaseVO.getOrderId(),
                                    logisticsQueryBaseVO.getTransportNo(), BusinessTypeEnum.QUERY_ORDER.getCode(), LogisticsPlatformEnum.DSF.getCode(),
                                    RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(logisticsQueryBaseVO), JSONUtil.toJsonStr(responseMsg));
                            list.add(orderResponseVO);
                        });
                    }
                }
            } catch (Exception e) {
                logisticsOperateService.pullOperateLog(logisticsQueryBaseVO.getOrderId(),
                        logisticsQueryBaseVO.getTransportNo(), BusinessTypeEnum.QUERY_ORDER.getCode(), LogisticsPlatformEnum.DSF.getCode(),
                        RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsQueryBaseVO), JSONUtil.toJsonStr(e));
                LogisticsOrderResponseVO responseVO = new LogisticsOrderResponseVO();
                responseVO.failure(LogisticsPlatformEnum.DSF.getName(), logisticsQueryBaseVO.getDeliveryNo(), e.getMessage());
                list.add(responseVO);
            }
        }
        return success ? success(list) : failure(list);
    }

    /**
     * 获取标签
     * request_no 请求单号（支持4PX单号、客户单号和面单号
     *
     * @param logisticsQueryVO
     * @return
     */
    @Override
    public ApiResult<List<LogisticsPrintLabelResponse>> getLabelList(List<LogisticsGetLabelVO> logisticsQueryVO) throws IOException {
        List<LogisticsPrintLabelResponse> responses = new ArrayList<>();
        LogisticsGetLabelVO logisticsGetLabelVO = logisticsQueryVO.stream().filter(e -> Objects.nonNull(e.getAuthMap())).findFirst().orElse(null);
        assert logisticsGetLabelVO != null;
        LabelRequest labelRequest = LabelRequest.builder()
                .labelSize("label_100x150")
                .isPrintTime("Y")
                .isPrintDeclarationList(logisticsGetLabelVO.getIsPcd())
                .isPrintPickInfo(logisticsGetLabelVO.getIsPdn())
                .createPackageLabel(logisticsGetLabelVO.getIsPdn())
                .isPrintPickBarcode(logisticsGetLabelVO.getIsPdn())
                .requestNo(logisticsQueryVO.stream().map(LogisticsGetLabelVO::getDeliveryNo).collect(Collectors.toList()))
                .logisticsProductCode(logisticsGetLabelVO.getLogisticsSaleChannelEntity().getCode())
                .build();
        ValidatorUtil.validateEntity(labelRequest);
        LogisticsPrintLabelResponse response = new LogisticsPrintLabelResponse();
        try {
            ResponseMsg responseMsg = dsfShipperService.getLabelList(logisticsGetLabelVO.getAuthMap(), labelRequest);
            //失败
            if (StringUtils.isBlank(responseMsg.getResult()) || !Objects.equals("1", responseMsg.getResult())) {
                logisticsOperateService.pullOperateLog(logisticsGetLabelVO.getOrderId(),
                        logisticsGetLabelVO.getDeliveryNo(), BusinessTypeEnum.GET_LABEL_LIST.getCode(), LogisticsPlatformEnum.DSF.getCode(),
                        RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsQueryVO), JSONUtil.toJsonStr(responseMsg));
                response.failure(LogisticsPlatformEnum.DSF.getName(), String.join(",", labelRequest.getRequestNo()), responseMsg.getMsg());
                responses.add(response);
                return failure(responses);
            } else {
                logisticsOperateService.pullOperateLog(logisticsGetLabelVO.getOrderId(),
                        logisticsGetLabelVO.getDeliveryNo(), BusinessTypeEnum.GET_LABEL_LIST.getCode(), LogisticsPlatformEnum.DSF.getCode(),
                        RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(logisticsQueryVO), JSONUtil.toJsonStr(responseMsg));
                response = LogisticsPrintLabelResponse.builder()
                        .deliveryNoList(logisticsQueryVO.stream().map(LogisticsGetLabelVO::getDeliveryNo).collect(Collectors.toList()))
                        .base64(FileUtil.convertPdfUrlToBase64((String) responseMsg.getData())).build();
                response.success();
                responses.add(response);
                return success(responses);
            }
        } catch (Exception e) {
            log.error("递四方获取getLabelList接口异常：{}", e.getMessage());
            logisticsOperateService.pullOperateLog(logisticsGetLabelVO.getOrderId(),
                    logisticsGetLabelVO.getDeliveryNo(), BusinessTypeEnum.GET_LABEL_LIST.getCode(), LogisticsPlatformEnum.DSF.getCode(),
                    RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsQueryVO), JSONUtil.toJsonStr(e));
            response.failure(LogisticsPlatformEnum.DSF.getName(), String.join(",", labelRequest.getRequestNo()), e.getMessage());
            responses.add(response);
            return failure(responses);
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
        ChanelRequest chanelRequest = ChanelRequest.builder()
                .transport_mode("1")
                .build();
        ValidatorUtil.validateEntity(chanelRequest);
        try {
            ResponseMsg responseMsg = dsfShipperService.getChanelList(chanelQueryVO.getAuthMap(), chanelRequest);
            //失败
            if (StringUtils.isBlank(responseMsg.getResult()) || !Objects.equals("1", responseMsg.getResult())) {
                logisticsOperateService.pullOperateLog(chanelQueryVO.getOrderId(),
                        chanelQueryVO.getTransportMode(), BusinessTypeEnum.GET_CHANEL_LIST.getCode(), LogisticsPlatformEnum.DSF.getCode(),
                        RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(chanelQueryVO), JSONUtil.toJsonStr(responseMsg));
                return failure(getPlatForm().getName() + ":" + responseMsg.getMsg());
            } else {
                List<ChanelInfo> chanelInfos = JSON.parseArray(responseMsg.getData().toString(), ChanelInfo.class);
                logisticsOperateService.pullOperateLog(chanelQueryVO.getOrderId(),
                        chanelQueryVO.getTransportMode(), BusinessTypeEnum.GET_CHANEL_LIST.getCode(), LogisticsPlatformEnum.DSF.getCode(),
                        RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(chanelQueryVO), JSONUtil.toJsonStr(responseMsg));
                return success(LogisticsChannelConverter.INSTANCE.channelConvertByDSF(chanelInfos));
            }
        } catch (Exception e) {
            log.error("递四方渠道接口调用异常:{}", e.getMessage());
            logisticsOperateService.pullOperateLog(chanelQueryVO.getOrderId(),
                    chanelQueryVO.getTransportMode(), BusinessTypeEnum.GET_CHANEL_LIST.getCode(), LogisticsPlatformEnum.DSF.getCode(),
                    RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(chanelQueryVO), JSONUtil.toJsonStr(e));
            return failure(e.getMessage());
        }

    }
    /**
     * 更新重量
     *
     * @return
     */
    @Override
    public ApiResult<String> updateWeight(LogisticsUpdateWeightVO logisticsUpdateWeightVO) {
        try {
            DsfUpdateWeightReq request = DsfUpdateWeightReq.builder()
                    .requestNo(logisticsUpdateWeightVO.getDeliveryNo())
                    .weight(logisticsUpdateWeightVO.getWeight().toString())
                    .build();
            ValidatorUtil.validateEntity(request);
            ResponseMsg response = dsfShipperService.updateWeight(logisticsUpdateWeightVO.getAuthMap(), request);
            //失败
            if (StringUtils.isBlank(response.getResult()) || !Objects.equals("1", response.getResult())) {
                logisticsOperateService.pushOperateLog(logisticsUpdateWeightVO.getOrderId(),
                        logisticsUpdateWeightVO.getDeliveryNo(), BusinessTypeEnum.UPDATE_WEIGHT.getCode(), LogisticsPlatformEnum.DSF.getCode(),
                        RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsUpdateWeightVO), JSONUtil.toJsonStr(response),false);
                return ApiResult.error(ApiError.CALL_THIRD_LOGISTICS_PLATFORM_ERROR.code,response.getMsg());
            }
            logisticsOperateService.pushOperateLog(logisticsUpdateWeightVO.getOrderId(),
                    logisticsUpdateWeightVO.getDeliveryNo(), BusinessTypeEnum.UPDATE_WEIGHT.getCode(), LogisticsPlatformEnum.DSF.getCode(),
                    RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(logisticsUpdateWeightVO), JSONUtil.toJsonStr(response),false);
            return success();
        }catch (Exception e){
            logisticsOperateService.pushOperateLog(logisticsUpdateWeightVO.getOrderId(),
                    logisticsUpdateWeightVO.getDeliveryNo(), BusinessTypeEnum.UPDATE_WEIGHT.getCode(), LogisticsPlatformEnum.DSF.getCode(),
                    RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsUpdateWeightVO), JSONUtil.toJsonStr(e),true);
            return failure(getPlatForm().getName() + ":" + e.getMessage());
        }
    }
    /**
     * 授权判断
     *
     * @param authMap
     * @return
     */
    @Override
    public ApiResult<Object>authorization(Map<String, String> authMap) {
        try {
            ChanelRequest chanelRequest = ChanelRequest.builder()
                    .transport_mode("1")
                    .build();
            ResponseMsg responseMsg = dsfShipperService.getChanelList(authMap, chanelRequest);
            if (StringUtils.isBlank(responseMsg.getResult()) || !Objects.equals("1", responseMsg.getResult())) {
                //授权失败
                return failure("授权失败");
            } else {
                return success("授权成功");
            }
        } catch (Exception e) {
            return failure(getPlatForm().getName() + ":" + e.getMessage());
        }
    }

    @Override
    public LogisticsPlatformEnum getPlatForm() {
        return LogisticsPlatformEnum.DSF;
    }

    @Override
    public ApiResult<List<LogisticsServiceResponseVO>> listLogisticsService(Map<String, String> authMap) {
        return ApiResult.error(-1, "功能未开放");

    }
}
