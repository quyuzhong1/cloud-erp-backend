package com.erp.server.tms.service.logistics;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.business.annotation.LogisticsPlatformType;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.utils.FileUtil;
import com.common.core.utils.ValidatorUtil;
import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
import com.erp.model.tms.enums.BusinessTypeEnum;
import com.erp.model.tms.enums.RequestStatusEnums;
import com.erp.model.tms.vo.request.*;
import com.erp.model.tms.vo.response.LogisticsOrderResponseVO;
import com.erp.model.tms.vo.response.LogisticsPrintLabelResponse;
import com.erp.model.tms.vo.response.LogisticsServiceResponseVO;
import com.erp.server.tms.convert.LogisticsChannelConverter;
import com.erp.server.tms.convert.LogisticsOrderConverter;
import com.erp.server.tms.handler.AbstractLogisticsHandler;
import com.erp.server.tms.service.LogisticsOperateService;
import com.sdk.tms.tongyou.dto.request.TongYouCreateOrderRequest;
import com.sdk.tms.tongyou.dto.request.TongYouGetOrderRequest;
import com.sdk.tms.tongyou.dto.request.TongYouPrintLabelRequest;
import com.sdk.tms.tongyou.dto.request.TongYouUpdateWeightRequest;
import com.sdk.tms.tongyou.dto.response.*;
import com.sdk.tms.tongyou.server.TongYouService;
import com.sdk.tms.weishi.dto.request.WeiShiUpdateWeightRequest;
import com.sdk.tms.weishi.dto.response.WeiShiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;


/**
 * 纬狮物流接口处理器
 */
@Slf4j
@Component
@LogisticsPlatformType(LogisticsPlatformEnum.TONG_YOU)
public class TongYouLogisticsHandlerImpl extends AbstractLogisticsHandler {
    @Resource
    private TongYouService tongYouService;
    @Resource
    private LogisticsOperateService logisticsOperateService;

    @Override
    public ApiResult<List<LogisticsSaleChannelEntity>> getChannel(ChanelQueryVO chanelQueryVO) {
        try {
            TongYouResponse<List<TongYouChannel>> tongYouResponse = tongYouService.getAllChannel(chanelQueryVO.getAuthMap());
            if (!tongYouResponse.getSuccess()) {
                logisticsOperateService.pullOperateLog(chanelQueryVO.getOrderId(),
                        chanelQueryVO.getTransportMode(), BusinessTypeEnum.GET_CHANEL_LIST.getCode(), LogisticsPlatformEnum.TONG_YOU.getCode(),
                        RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(chanelQueryVO), JSONUtil.toJsonStr(tongYouResponse));
                return ApiResult.error(ApiError.CALL_THIRD_LOGISTICS_PLATFORM_ERROR.code, getPlatForm().getName() + ":" + tongYouResponse.getMsg());
            }
            List<LogisticsSaleChannelEntity> response = LogisticsChannelConverter.INSTANCE.channelConvertByTongYou(tongYouResponse.getData());
            logisticsOperateService.pullOperateLog(chanelQueryVO.getOrderId(),
                    chanelQueryVO.getTransportMode(), BusinessTypeEnum.GET_CHANEL_LIST.getCode(), LogisticsPlatformEnum.TONG_YOU.getCode(),
                    RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(chanelQueryVO), JSONUtil.toJsonStr(tongYouResponse));
            return success(response);
        } catch (Exception e) {
            log.error("通邮渠道接口异常：{}", e.getMessage());
            logisticsOperateService.pullOperateLog(chanelQueryVO.getOrderId(),
                    chanelQueryVO.getTransportMode(), BusinessTypeEnum.GET_CHANEL_LIST.getCode(), LogisticsPlatformEnum.TONG_YOU.getCode(),
                    RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(chanelQueryVO), JSONUtil.toJsonStr(e));
            return failure(getPlatForm().getName() + ":" + e.getMessage());
        }

    }

    @Override
    public ApiResult<LogisticsOrderResponseVO> createOrder(LogisticsOrderVO logisticsOrderVO) {
        TongYouCreateOrderRequest request = LogisticsOrderConverter.INSTANCE.orderRequestByTongYou(logisticsOrderVO);
        ValidatorUtil.validateEntity(request);
        String iossVatId = request.getIossVatId();
        try {
            request.getDeclareInfos().forEach(v->v.setUrl(""));
            TongYouCreateOrder tongYouCreateOrder = tongYouService.createOrder(request, logisticsOrderVO.getAuthMap());
            if (!tongYouCreateOrder.getSuccess()) {
                logisticsOperateService.pushOperateLog(logisticsOrderVO.getSourceId(),
                        logisticsOrderVO.getDeliveryNo(), BusinessTypeEnum.CREATE_ORDER.getCode(), LogisticsPlatformEnum.TONG_YOU.getCode(),
                        RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsOrderVO), JSONUtil.toJsonStr(tongYouCreateOrder), false);
                return ApiResult.error(ApiError.CALL_THIRD_LOGISTICS_PLATFORM_ERROR.code, getPlatForm().getName() + ":" + tongYouCreateOrder.getMsg());
            }
            logisticsOperateService.pushOperateLog(logisticsOrderVO.getSourceId(),
                    logisticsOrderVO.getDeliveryNo(), BusinessTypeEnum.CREATE_ORDER.getCode(), LogisticsPlatformEnum.TONG_YOU.getCode(),
                    RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(logisticsOrderVO), JSONUtil.toJsonStr(tongYouCreateOrder), false);
            return success(LogisticsOrderResponseVO.builder()
                    .transportNo(tongYouCreateOrder.getLogisticsNo())
                    .trackNo(tongYouCreateOrder.getLogisticsNo())
                    .deliveryNo(logisticsOrderVO.getDeliveryNo())
                    .iossTaxNo(iossVatId)
                    .build());
        } catch (Exception e) {
            logisticsOperateService.pushOperateLog(logisticsOrderVO.getSourceId(),
                    logisticsOrderVO.getDeliveryNo(), BusinessTypeEnum.CREATE_ORDER.getCode(), LogisticsPlatformEnum.TONG_YOU.getCode(),
                    RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsOrderVO), JSONUtil.toJsonStr(e), true);
            return ApiResult.error(ApiError.CALL_THIRD_LOGISTICS_PLATFORM_ERROR.code, getPlatForm().getName() + ":" + e.getMessage());
        }
    }


    @Override
    public ApiResult<List<LogisticsPrintLabelResponse>> getLabelList(List<LogisticsGetLabelVO> labelVO) throws IOException {
        List<LogisticsPrintLabelResponse> responseList = new ArrayList<>();
        boolean isSuccess = true;
        //通邮接口有说支持多个单号，但实际测试发现，传多个单号，如果其中某个失败，结果会返回成功单号的pdf信息，但不会表示哪个单号失败，导致我们这边无法判断到底是否全部成功打印，所以单个请求
        for (LogisticsGetLabelVO logisticsGetLabelVO : labelVO) {
            TongYouPrintLabelRequest request = TongYouPrintLabelRequest.builder()
                    .orderNo(logisticsGetLabelVO.getDeliveryNo())
                    .trackNo(StringUtils.isBlank(logisticsGetLabelVO.getTrackNo())? logisticsGetLabelVO.getTransportNo() : logisticsGetLabelVO.getTrackNo())
                    .logisticsId(logisticsGetLabelVO.getLogisticsSaleChannelEntity().getCode())
                    .isPaoc(logisticsGetLabelVO.getIsPdn())
                    .isPcd(logisticsGetLabelVO.getIsPcd())
                    .build();
            try {
                ValidatorUtil.validateEntity(request);
                TongYouPrintLabel tongYouResponse = tongYouService.printLabel(request, logisticsGetLabelVO.getAuthMap());
                //调用接口失败，不立刻返回，继续剩下的调用
                if (!tongYouResponse.getSuccess()) {
                    LogisticsPrintLabelResponse response = new LogisticsPrintLabelResponse();
                    response.failure(getPlatForm().getName(), logisticsGetLabelVO.getDeliveryNo(), tongYouResponse.getMsg());
                    responseList.add(response);
                    logisticsOperateService.pullOperateLog(logisticsGetLabelVO.getOrderId(),
                            logisticsGetLabelVO.getDeliveryNo(), BusinessTypeEnum.GET_LABEL.getCode(), LogisticsPlatformEnum.TONG_YOU.getCode(),
                            RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsGetLabelVO), JSONUtil.toJsonStr(tongYouResponse));
                    isSuccess = false;
                    continue;
                }

                LogisticsPrintLabelResponse response = new LogisticsPrintLabelResponse();
                response.setDeliveryNoList(Collections.singletonList(logisticsGetLabelVO.getDeliveryNo()));
                response.setTransportNoList(Collections.singletonList(logisticsGetLabelVO.getTransportNo()));
                response.setTrackNoList(Collections.singletonList(logisticsGetLabelVO.getTrackNo()));
                //返回格式是base64
                if (tongYouResponse.getType().equals(0)) {
                    response.setBase64("data:application/pdf;base64,"+tongYouResponse.getBase64());
                } else {
                    //返回是url
                    response.setBase64(FileUtil.convertPdfUrlToBase64(tongYouResponse.getUrl()));
                }
                logisticsOperateService.pullOperateLog(logisticsGetLabelVO.getOrderId(),
                        logisticsGetLabelVO.getDeliveryNo(), BusinessTypeEnum.GET_LABEL.getCode(), LogisticsPlatformEnum.TONG_YOU.getCode(),
                        RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(logisticsGetLabelVO), JSONUtil.toJsonStr(tongYouResponse));
                response.success();
                responseList.add(response);
            } catch (Exception e) {
                LogisticsPrintLabelResponse response = new LogisticsPrintLabelResponse();
                response.failure(getPlatForm().getName(), logisticsGetLabelVO.getDeliveryNo(), e.getMessage());
                responseList.add(response);
                logisticsOperateService.pullOperateLog(logisticsGetLabelVO.getOrderId(),
                        logisticsGetLabelVO.getDeliveryNo(), BusinessTypeEnum.GET_LABEL.getCode(), LogisticsPlatformEnum.TONG_YOU.getCode(),
                        RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsGetLabelVO), JSONUtil.toJsonStr(e));
                isSuccess = false;
            }

        }
        return isSuccess ? success(responseList) : failure(responseList);
    }

    /**
     * 查询订单(批量)
     *
     * @param logisticsQueryVOList
     * @return
     */
    @Override
    public ApiResult<List<LogisticsOrderResponseVO>> queryOrderList(List<LogisticsQueryBaseVO> logisticsQueryVOList) {
        List<LogisticsOrderResponseVO> responseList = new ArrayList<>();
        boolean isSuccess = true;
        for (LogisticsQueryBaseVO logisticsQueryBaseVO : logisticsQueryVOList) {
            TongYouGetOrderRequest request = TongYouGetOrderRequest.builder()
                    .orderNo(logisticsQueryBaseVO.getDeliveryNo())
                    .build();
            LogisticsOrderResponseVO response = new LogisticsOrderResponseVO();
            try {
                ValidatorUtil.validateEntity(request);
                TongYouOrderInfo tongYouOrderInfo = tongYouService.getOrderInfo(request, logisticsQueryBaseVO.getAuthMap());
                if (!tongYouOrderInfo.getSuccess()) {
                    response.failure(getPlatForm().getName(), logisticsQueryBaseVO.getDeliveryNo(), tongYouOrderInfo.getMsg());
                    responseList.add(response);
                    logisticsOperateService.pullOperateLog(logisticsQueryBaseVO.getOrderId(),
                            logisticsQueryBaseVO.getDeliveryNo(), BusinessTypeEnum.QUERY_ORDER.getCode(), LogisticsPlatformEnum.TONG_YOU.getCode(),
                            RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsQueryBaseVO), JSONUtil.toJsonStr(tongYouOrderInfo));

                    isSuccess = false;
                    continue;
                }
                response = LogisticsOrderConverter.INSTANCE.orderQueryByTongYou(tongYouOrderInfo);
                responseList.add(response);
                logisticsOperateService.pullOperateLog(logisticsQueryBaseVO.getOrderId(),
                        logisticsQueryBaseVO.getDeliveryNo(), BusinessTypeEnum.QUERY_ORDER.getCode(), LogisticsPlatformEnum.TONG_YOU.getCode(),
                        RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(logisticsQueryBaseVO), JSONUtil.toJsonStr(tongYouOrderInfo));
            } catch (Exception e) {
                response.failure(getPlatForm().getName(), logisticsQueryBaseVO.getDeliveryNo(), e.getMessage());
                responseList.add(response);
                logisticsOperateService.pullOperateLog(logisticsQueryBaseVO.getOrderId(),
                        logisticsQueryBaseVO.getDeliveryNo(), BusinessTypeEnum.QUERY_ORDER.getCode(), LogisticsPlatformEnum.TONG_YOU.getCode(),
                        RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsQueryBaseVO), JSONUtil.toJsonStr(e));
                isSuccess = false;
            }

        }
        return isSuccess ? success(responseList) : failure(responseList);
    }

    /**
     * 更新重量
     *
     * @return
     */
    @Override
    public ApiResult<String> updateWeight(LogisticsUpdateWeightVO logisticsUpdateWeightVO) {
        try {
            TongYouUpdateWeightRequest request = TongYouUpdateWeightRequest.builder()
                    .orderNo(logisticsUpdateWeightVO.getDeliveryNo())
                    .trackNo(logisticsUpdateWeightVO.getTransportNo())
                    .weight(logisticsUpdateWeightVO.getWeight().divide(new BigDecimal(1000),4, RoundingMode.HALF_UP))
                    .build();
            ValidatorUtil.validateEntity(request);
            TongYouResponse<String> response = tongYouService.updateWeight(request, logisticsUpdateWeightVO.getAuthMap());

            if (!response.getSuccess()) {
                logisticsOperateService.pushOperateLog(logisticsUpdateWeightVO.getOrderId(),
                        logisticsUpdateWeightVO.getDeliveryNo(), BusinessTypeEnum.UPDATE_WEIGHT.getCode(), LogisticsPlatformEnum.TONG_YOU.getCode(),
                        RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsUpdateWeightVO), JSONUtil.toJsonStr(response),false);
                return ApiResult.error(ApiError.CALL_THIRD_LOGISTICS_PLATFORM_ERROR.code,response.getMsg());
            }
            logisticsOperateService.pushOperateLog(logisticsUpdateWeightVO.getOrderId(),
                    logisticsUpdateWeightVO.getDeliveryNo(), BusinessTypeEnum.UPDATE_WEIGHT.getCode(), LogisticsPlatformEnum.TONG_YOU.getCode(),
                    RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(logisticsUpdateWeightVO), JSONUtil.toJsonStr(response),false);
            return success();
        }catch (Exception e){
            logisticsOperateService.pushOperateLog(logisticsUpdateWeightVO.getOrderId(),
                    logisticsUpdateWeightVO.getDeliveryNo(), BusinessTypeEnum.UPDATE_WEIGHT.getCode(), LogisticsPlatformEnum.TONG_YOU.getCode(),
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
            TongYouResponse<List<TongYouChannel>> tongYouResponse = tongYouService.getAllChannel(authMap);
            if (!tongYouResponse.getSuccess()) {
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
        return LogisticsPlatformEnum.TONG_YOU;
    }

    @Override
    public ApiResult<List<LogisticsServiceResponseVO>> listLogisticsService(Map<String, String> authMap) {
        return ApiResult.error(-1, "功能未开放");

    }
}
