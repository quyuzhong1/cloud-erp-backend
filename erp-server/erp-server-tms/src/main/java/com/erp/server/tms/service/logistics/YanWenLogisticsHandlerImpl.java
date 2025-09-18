package com.erp.server.tms.service.logistics;

import cn.hutool.json.JSONUtil;
import com.common.business.annotation.LogisticsPlatformType;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.enums.CurrencyEnum;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.common.core.utils.ValidatorUtil;
import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
import com.erp.model.tms.enums.BusinessTypeEnum;
import com.erp.model.tms.enums.RequestStatusEnums;
import com.erp.model.tms.vo.request.ChanelQueryVO;
import com.erp.model.tms.vo.request.LogisticsCancelOrderVO;
import com.erp.model.tms.vo.request.LogisticsGetLabelVO;
import com.erp.model.tms.vo.request.LogisticsOrderVO;
import com.erp.model.tms.vo.request.LogisticsQueryBaseVO;
import com.erp.model.tms.vo.response.CancelResponseVO;
import com.erp.model.tms.vo.response.LogisticsOrderResponseVO;
import com.erp.model.tms.vo.response.LogisticsPrintLabelResponse;
import com.erp.model.tms.vo.response.LogisticsServiceResponseVO;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.server.tms.convert.LogisticsOperationOrderConverter;
import com.erp.server.tms.convert.LogisticsChannelConverter;
import com.erp.server.tms.convert.LogisticsOrderConverter;
import com.erp.server.tms.handler.AbstractLogisticsHandler;
import com.erp.server.tms.service.LogisticsOperateService;
import com.sdk.tms.yanwen.dto.request.YanWenCancelOrderRequest;
import com.sdk.tms.yanwen.dto.request.YanWenCreateWayBillRequest;
import com.sdk.tms.yanwen.dto.request.YanWenGetLabelRequest;
import com.sdk.tms.yanwen.dto.request.YanWenQueryOrderRequest;
import com.sdk.tms.yanwen.dto.response.*;
import com.sdk.tms.yanwen.server.YanWenService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;


/**
 * 燕文物流接口处理器
 */
@Slf4j
@Component
@LogisticsPlatformType(LogisticsPlatformEnum.YAN_WEN)
public class YanWenLogisticsHandlerImpl extends AbstractLogisticsHandler {
    @Resource
    private LogisticsOperateService logisticsOperateService;
    @Resource
    private YanWenService yanWenService;
    @Resource
    private DmpTaskFeign dmpTaskFeign;
    public static final String YYYY_MM_DD = "yyyy-MM-dd";

    @Override
    public ApiResult<List<LogisticsSaleChannelEntity>> getChannel(ChanelQueryVO chanelQueryVO) {
        try {
            YanWenResponse<List<YanWenChannel>> yanWenResponse =  yanWenService.getAllChannel(chanelQueryVO.getAuthMap());
            if(!yanWenResponse.getSuccess()){
                logisticsOperateService.pullOperateLog(chanelQueryVO.getOrderId(),
                        chanelQueryVO.getTransportMode(), BusinessTypeEnum.GET_CHANEL_LIST.getCode(), LogisticsPlatformEnum.YAN_WEN.getCode(),
                        RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(chanelQueryVO), JSONUtil.toJsonStr(yanWenResponse));
                return ApiResult.error(ApiError.CALL_THIRD_LOGISTICS_PLATFORM_ERROR.code,yanWenResponse.getMessage());
            }
            List<LogisticsSaleChannelEntity> response = LogisticsChannelConverter.INSTANCE.channelConvertByYanWenList(yanWenResponse.getData());
            logisticsOperateService.pullOperateLog(chanelQueryVO.getOrderId(),
                    chanelQueryVO.getTransportMode(), BusinessTypeEnum.GET_CHANEL_LIST.getCode(), LogisticsPlatformEnum.YAN_WEN.getCode(),
                    RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(chanelQueryVO), JSONUtil.toJsonStr(yanWenResponse));
            return success(response);
        }catch (Exception e){
            log.error("燕文渠道接口异常：{}",e.getMessage());
            logisticsOperateService.pullOperateLog(chanelQueryVO.getOrderId(),
                    chanelQueryVO.getTransportMode(), BusinessTypeEnum.GET_CHANEL_LIST.getCode(), LogisticsPlatformEnum.YAN_WEN.getCode(),
                    RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(chanelQueryVO), JSONUtil.toJsonStr(e));
            return failure(getPlatForm().getName() + ":" + e.getMessage());
        }

    }

    @Override
    public ApiResult<LogisticsOrderResponseVO> createOrder(LogisticsOrderVO logisticsOrderVO) {
        logisticsOrderVO.getLogisticsProductVOList().forEach(logisticsProductVO -> {
            //出口国币种处理 人民币转美元（目的国申报价默认美金）
            BigDecimal declarePrice = logisticsProductVO.getDeclarePrice();
            if (Objects.nonNull(declarePrice)){
                String currency = CurrencyEnum.CNY.getCurrencyCode();
                //默认出口申报币种为人民币
                if (!com.alibaba.nacos.api.utils.StringUtils.isBlank(logisticsProductVO.getDeclareCurrency())){
                    currency = logisticsProductVO.getDeclareCurrency();
                }
                BigDecimal exchangeRate1 = dmpTaskFeign.getRate(LocalDate.now().format(DateTimeFormatter.ofPattern(YYYY_MM_DD)), currency);
                if (Objects.isNull(exchangeRate1)){
                    throw new ServiceException(ApiError.ERROR_EXCHANGE_RATE_NOT_EXIST, LocalDate.now(), currency);
                }
                //先转换成人民币
                BigDecimal cnyDeclarePrice = MathUtil.multiplyWithTwo(declarePrice, exchangeRate1).setScale(4, RoundingMode.HALF_UP);
                //再统一转换成美元
                BigDecimal exchangeRate2 = dmpTaskFeign.getRate(LocalDate.now().format(DateTimeFormatter.ofPattern(YYYY_MM_DD)), CurrencyEnum.USD.getCurrencyCode());
                if (Objects.isNull(exchangeRate2)){
                    throw new ServiceException(ApiError.ERROR_EXCHANGE_RATE_NOT_EXIST, LocalDate.now(), CurrencyEnum.USD.getCurrencyCode());
                }
                BigDecimal usdDeclarePrice = MathUtil.divide(cnyDeclarePrice, exchangeRate2).setScale(4, RoundingMode.HALF_UP);
                logisticsProductVO.setDeclarePrice(usdDeclarePrice);
                logisticsProductVO.setDeclareCurrency(CurrencyEnum.USD.getCurrencyCode());
            }else {
                throw new ServiceException("燕文物流下单【{}】出口国申报单价不能为空",logisticsProductVO.getSkuNo());
            }
        });
        YanWenCreateWayBillRequest request = LogisticsOrderConverter.INSTANCE.orderRequestByYanWen(logisticsOrderVO);
        //燕文接口[{发件人税号}---senderInfo] 国家为挪威的时候推送[VOEC]税号,其他国家不用推送.
        request.getSenderInfo().setTaxNumber(getTaxNumberByCountry(logisticsOrderVO.getCountry(), logisticsOrderVO.getVoecTaxNo()));
        ValidatorUtil.validateEntity(request);
        String ioss = request.getParcelInfo().getIoss();
        try {
            YanWenResponse<YanWenCreateWayBill> yanWenResponse = yanWenService.createWayBill(request,logisticsOrderVO.getAuthMap());
            if(!yanWenResponse.getSuccess()){
                logisticsOperateService.pushOperateLog(logisticsOrderVO.getSourceId(),
                        logisticsOrderVO.getDeliveryNo(), BusinessTypeEnum.CREATE_ORDER.getCode(), LogisticsPlatformEnum.YAN_WEN.getCode(),
                        RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsOrderVO), JSONUtil.toJsonStr(yanWenResponse), false);
                return ApiResult.error(ApiError.CALL_THIRD_LOGISTICS_PLATFORM_ERROR.code,yanWenResponse.getMessage());
            }
            logisticsOperateService.pushOperateLog(logisticsOrderVO.getSourceId(),
                    logisticsOrderVO.getDeliveryNo(), BusinessTypeEnum.CREATE_ORDER.getCode(), LogisticsPlatformEnum.YAN_WEN.getCode(),
                    RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(logisticsOrderVO), JSONUtil.toJsonStr(yanWenResponse), false);
            return success(LogisticsOrderResponseVO.builder()
                    .transportNo(yanWenResponse.getData().getWaybillNumber())
                    .deliveryNo(yanWenResponse.getData().getOrderNumber())
                    .trackNo(yanWenResponse.getData().getWaybillNumber())
                    .iossTaxNo(ioss)
                    .build());
        }catch (Exception e){
            logisticsOperateService.pushOperateLog(logisticsOrderVO.getSourceId(),
                    logisticsOrderVO.getDeliveryNo(), BusinessTypeEnum.CREATE_ORDER.getCode(), LogisticsPlatformEnum.YAN_WEN.getCode(),
                    RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsOrderVO), JSONUtil.toJsonStr(e), true);
            return failure(getPlatForm().getName() + ":" + e.getMessage());
        }

    }
    /**
     * 根据国家进行判断是否传递voec
     * @param country
     * @param voecTaxNo
     * @return
     */
    private String getTaxNumberByCountry(String country, String voecTaxNo) {
        if (StringUtils.isBlank(voecTaxNo) || StringUtils.isBlank(country)){
            return null;
        }
        //国家是挪威的时候推送，其他的时候不推送
        if ("NO".equals(country)){
            return voecTaxNo;
        }else {
            return null;
        }
    }

    @Override
    public ApiResult<List<LogisticsPrintLabelResponse>> getLabelList(List<LogisticsGetLabelVO> labelVO) {
        List<LogisticsPrintLabelResponse> result = new ArrayList<>();
        boolean isSuccess = true;
        for(LogisticsGetLabelVO logisticsGetLabelVO : labelVO){
            Integer printRemark = 0;
            if(StringUtils.isNotEmpty(logisticsGetLabelVO.getIsPdn()) && "Y".equals(logisticsGetLabelVO.getIsPdn())){
                printRemark = 1;
            }
            YanWenGetLabelRequest request = YanWenGetLabelRequest.builder()
                    .waybillNumber(logisticsGetLabelVO.getTransportNo())
                    .printRemark(printRemark)
                    .build();
            LogisticsPrintLabelResponse response = new LogisticsPrintLabelResponse();
            try {
                ValidatorUtil.validateEntity(request);
                YanWenResponse<YanWenGetLabel> labelResponse = yanWenService.getLabel(request,logisticsGetLabelVO.getAuthMap());
                if(!labelResponse.getSuccess()){
                    logisticsOperateService.pullOperateLog(logisticsGetLabelVO.getOrderId(),
                            logisticsGetLabelVO.getDeliveryNo(), BusinessTypeEnum.GET_LABEL.getCode(), LogisticsPlatformEnum.YAN_WEN.getCode(),
                            RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsGetLabelVO), JSONUtil.toJsonStr(labelResponse));
                    response.failure(getPlatForm().getName(),logisticsGetLabelVO.getDeliveryNo(),labelResponse.getMessage());
                    isSuccess = false;
                }else {
                    String prefix = "data:application/pdf;base64,";
                    response.setBase64(prefix + labelResponse.getData().getBase64String());
                    response.setTransportNoList(Collections.singletonList(labelResponse.getData().getWaybillNumber()));
                    response.setDeliveryNoList(Collections.singletonList(logisticsGetLabelVO.getDeliveryNo()));
                    logisticsOperateService.pullOperateLog(logisticsGetLabelVO.getOrderId(),
                            logisticsGetLabelVO.getDeliveryNo(), BusinessTypeEnum.GET_LABEL.getCode(), LogisticsPlatformEnum.YAN_WEN.getCode(),
                            RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(logisticsGetLabelVO), JSONUtil.toJsonStr(labelResponse));
                }
            }catch (Exception e){
                logisticsOperateService.pullOperateLog(logisticsGetLabelVO.getOrderId(),
                        logisticsGetLabelVO.getDeliveryNo(), BusinessTypeEnum.GET_LABEL.getCode(), LogisticsPlatformEnum.YAN_WEN.getCode(),
                        RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsGetLabelVO), JSONUtil.toJsonStr(e));
                response.failure(getPlatForm().getName(),logisticsGetLabelVO.getDeliveryNo(),e.getMessage());
                isSuccess = false;
            }
            result.add(response);
        }
        return isSuccess?success(result):failure(result);
    }

    @Override
    public ApiResult<List<CancelResponseVO>> cancelOrder(List<LogisticsCancelOrderVO> cancelOrderVOList) {
        List<CancelResponseVO> result = new ArrayList<>();
        boolean isSuccess = true;
        for(LogisticsCancelOrderVO cancelOrderVO : cancelOrderVOList){
            YanWenCancelOrderRequest request = YanWenCancelOrderRequest.builder()
                    .note(cancelOrderVO.getReason())
                    .waybillNumber(cancelOrderVO.getTransportNo())
                    .build();
            CancelResponseVO cancelResponseVO = LogisticsOperationOrderConverter.INSTANCE.cancelOrderCovert(cancelOrderVO);
            try {
                ValidatorUtil.validateEntity(request);
                YanWenResponse<String> yanWenResponse = yanWenService.cancelOrder(request,cancelOrderVO.getAuthMap());
                if(!yanWenResponse.getSuccess()){
                    isSuccess = false;
                    cancelResponseVO.failure(getPlatForm().getName(),cancelOrderVO.getDeliveryNo(),yanWenResponse.getMessage());
                    logisticsOperateService.pushOperateLog(cancelOrderVO.getOrderId(),
                            cancelOrderVO.getDeliveryNo(), BusinessTypeEnum.CANCEL_ORDER.getCode(), LogisticsPlatformEnum.YAN_WEN.getCode(),
                            RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(cancelOrderVO), JSONUtil.toJsonStr(yanWenResponse), false);
                }else{
                    cancelResponseVO.success();
                    logisticsOperateService.pushOperateLog(cancelOrderVO.getOrderId(),
                            cancelOrderVO.getDeliveryNo(), BusinessTypeEnum.CANCEL_ORDER.getCode(), LogisticsPlatformEnum.YAN_WEN.getCode(),
                            RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(cancelOrderVO), JSONUtil.toJsonStr(yanWenResponse), false);
                }
            }catch (Exception e){
                isSuccess = false;
                cancelResponseVO.failure(getPlatForm().getName(),cancelOrderVO.getDeliveryNo(),e.getMessage());
                logisticsOperateService.pushOperateLog(cancelOrderVO.getOrderId(),
                        cancelOrderVO.getDeliveryNo(), BusinessTypeEnum.CANCEL_ORDER.getCode(), LogisticsPlatformEnum.YAN_WEN.getCode(),
                        RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(cancelOrderVO), JSONUtil.toJsonStr(e), true);
            }

            result.add(cancelResponseVO);
        }
        return isSuccess?success(result):failure(result);
    }

    @Override
    public ApiResult<List<LogisticsOrderResponseVO>> queryOrderList(List<LogisticsQueryBaseVO> logisticsQueryVOList){
        List<String> deliveryList = logisticsQueryVOList.stream().map(LogisticsQueryBaseVO::getDeliveryNo).collect(Collectors.toList());
        YanWenQueryOrderRequest request = YanWenQueryOrderRequest.builder()
                .listNumber(deliveryList)
                .build();
        ValidatorUtil.validateEntity(request);
        try {
            YanWenResponse<List<YanWenQueryOrder>> yanWenResponse = yanWenService.queryOrder(request,logisticsQueryVOList.get(0).getAuthMap());
            if(!yanWenResponse.getSuccess()){
                logisticsOperateService.pullOperateLog(logisticsQueryVOList.get(0).getOrderId(),
                        null, BusinessTypeEnum.QUERY_ORDER.getCode(), LogisticsPlatformEnum.YAN_WEN.getCode(),
                        RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsQueryVOList), JSONUtil.toJsonStr(yanWenResponse));
                return ApiResult.error(ApiError.CALL_THIRD_LOGISTICS_PLATFORM_ERROR.code,yanWenResponse.getMessage());
            }
            List<LogisticsOrderResponseVO> list = LogisticsOrderConverter.INSTANCE.orderQueryByYanWen(yanWenResponse.getData());
            logisticsOperateService.pullOperateLog(logisticsQueryVOList.get(0).getOrderId(),
                    null, BusinessTypeEnum.QUERY_ORDER.getCode(), LogisticsPlatformEnum.YAN_WEN.getCode(),
                    RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(logisticsQueryVOList), JSONUtil.toJsonStr(yanWenResponse));
            return success(list);
        }catch (Exception e){
            logisticsOperateService.pullOperateLog(logisticsQueryVOList.get(0).getOrderId(),
                    null, BusinessTypeEnum.QUERY_ORDER.getCode(), LogisticsPlatformEnum.YAN_WEN.getCode(),
                    RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsQueryVOList), JSONUtil.toJsonStr(e));
            return failure(getPlatForm().getName() + ":" + e.getMessage());
        }

    }
    /**
     * 授权判断
     * @param authMap
     * @return
     */
    @Override
    public ApiResult<Object>authorization(Map<String, String> authMap){
        try {
            YanWenResponse<List<YanWenChannel>> yanWenResponse = yanWenService.getAllChannel(authMap);
            if (!yanWenResponse.getSuccess()) {
                //授权失败
                return failure("授权失败");
            }else {
                return success("授权成功");
            }
        } catch (Exception e) {
            return failure(getPlatForm().getName() + ":" + e.getMessage());
        }
    }
    @Override
    public LogisticsPlatformEnum getPlatForm() {
        return LogisticsPlatformEnum.YAN_WEN;
    }

    @Override
    public ApiResult<List<LogisticsServiceResponseVO>> listLogisticsService(Map<String, String> authMap) {
        return ApiResult.error(-1, "功能未开放");

    }
}
