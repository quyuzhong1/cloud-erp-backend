package com.erp.server.tms.service.logistics;

import cn.hutool.json.JSONUtil;
import com.common.business.annotation.LogisticsPlatformType;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.business.threadlocal.TransferLogisticsContext;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
import com.erp.model.tms.entity.LogisticsTrackEntity;
import com.erp.model.tms.enums.BusinessTypeEnum;
import com.erp.model.tms.enums.RequestStatusEnums;
import com.erp.model.tms.vo.request.*;
import com.erp.model.tms.vo.response.*;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.server.tms.convert.BaoHongConverter;
import com.erp.server.tms.handler.AbstractLogisticsHandler;
import com.erp.server.tms.service.LogisticsOperateService;
import com.sdk.tms.baohong.api.order.SmRow;
import com.sdk.tms.baohong.dto.response.BaoHongResponse;
import com.sdk.tms.baohong.service.BaoHongService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@LogisticsPlatformType(LogisticsPlatformEnum.BAO_HONG)
public class BaoHongLogisticsHandlerImp extends AbstractLogisticsHandler {
    @Resource
    private DmpTaskFeign dmpTaskFeign;

    @Resource
    private BaoHongService baoHongService;

    @Resource
    private LogisticsOperateService logisticsOperateService;

    @Override
    public Map<String, String> getLogisticsAuthConfig(String authId) {
        return super.getLogisticsAuthConfig(authId);
    }

    @Override
    public List<Map<String, String>> getLogisticsAuthConfigByPlatform(String platform) {
        return super.getLogisticsAuthConfigByPlatform(platform);
    }

    @Override
    public ApiResult<LogisticsOrderResponseVO> createOrder(LogisticsOrderVO logisticsOrderVO) {
        return super.createOrder(logisticsOrderVO);
    }

    @Override
    public ApiResult<List<ConfirmResponseVO>> confirmOrder(List<LogisticsQueryBaseVO> logisticsQueryVO) {
        return super.confirmOrder(logisticsQueryVO);
    }

    @Override
    public ApiResult<List<CancelResponseVO>> cancelOrder(List<LogisticsCancelOrderVO> logisticsQueryVO) {
        return super.cancelOrder(logisticsQueryVO);
    }

    @Override
    public ApiResult<List<InterceptResponseVO>> interceptOrder(List<LogisticsInterceptOrderVO> logisticsQueryVO) {
        return super.interceptOrder(logisticsQueryVO);
    }

    @Override
    public ApiResult<List<UpdateResponseVO>> updateOrder(List<LogisticsOrderVO> logisticsOrderVOS) {
        return super.updateOrder(logisticsOrderVOS);
    }

    @Override
    public ApiResult<List<LogisticsOrderResponseVO>> queryOrderList(List<LogisticsQueryBaseVO> logisticsQueryVOList) {
        return super.queryOrderList(logisticsQueryVOList);
    }

    @Override
    public ApiResult<List<LogisticsPrintLabelResponse>> getLabelList(List<LogisticsGetLabelVO> logisticsQueryVOList) throws IOException {
        try {
            TransferLogisticsContext.setAuthMap(logisticsQueryVOList.get(0).getAuthMap());

            List<LogisticsPrintLabelResponse> resultList = new ArrayList<>();
            for(LogisticsGetLabelVO logisticsGetLabelVO : logisticsQueryVOList){
                BaoHongResponse<String> response = baoHongService.printLabel(logisticsGetLabelVO.getDeliveryNo());
                if(isFailure(response)){
                    logisticsOperateService.pullOperateLog(logisticsGetLabelVO.getAuthMap().get("id"),
                            logisticsGetLabelVO.getDeliveryNo(), BusinessTypeEnum.GET_LABEL.getCode(), LogisticsPlatformEnum.BAO_HONG.getCode(),
                            RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsGetLabelVO), JSONUtil.toJsonStr(response));
                    return ApiResult.error(ApiError.CALL_THIRD_LOGISTICS_PLATFORM_ERROR.code,response.getMessage());
                }
                LogisticsPrintLabelResponse logisticsPrintLabelResponse = new LogisticsPrintLabelResponse();
                logisticsPrintLabelResponse.setBase64(response.getData());
                logisticsPrintLabelResponse.setDeliveryNoList(Collections.singletonList(logisticsGetLabelVO.getDeliveryNo()));
                logisticsPrintLabelResponse.setTransportNoList(Collections.singletonList(logisticsGetLabelVO.getTransportNo()));
                logisticsPrintLabelResponse.setTrackNoList(Collections.singletonList(logisticsGetLabelVO.getTrackNo()));
                resultList.add(logisticsPrintLabelResponse);
                logisticsOperateService.pullOperateLog(logisticsGetLabelVO.getAuthMap().get("id"),
                        logisticsGetLabelVO.getDeliveryNo(), BusinessTypeEnum.GET_LABEL.getCode(), LogisticsPlatformEnum.BAO_HONG.getCode(),
                        RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(logisticsGetLabelVO), JSONUtil.toJsonStr(response));
            }
            return success(resultList);
        }finally {
            TransferLogisticsContext.remove();
        }
    }

    @Override
    public ApiResult<List<LogisticsTrackEntity>> getTrack(LogisticsTrackVO logisticsTrackVO) {
        return super.getTrack(logisticsTrackVO);
    }

    @Override
    public ApiResult<List<LogisticsSaleChannelEntity>> getChannel(ChanelQueryVO chanelQueryVO) {
        try {
            TransferLogisticsContext.setAuthMap(chanelQueryVO.getAuthMap());

            BaoHongResponse<List<SmRow>> response = baoHongService.getShippingMethodList();
            if (isFailure(response)) {
                logisticsOperateService.pullOperateLog(chanelQueryVO.getAuthMap().get("id"),
                        chanelQueryVO.getTransportMode(), BusinessTypeEnum.GET_CHANEL_LIST.getCode(), LogisticsPlatformEnum.BAO_HONG.getCode(),
                        RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(chanelQueryVO), JSONUtil.toJsonStr(response));
                return failure("授权失败:" + response.getMessage());
            } else {
                logisticsOperateService.pullOperateLog(chanelQueryVO.getAuthMap().get("id"),
                        chanelQueryVO.getTransportMode(), BusinessTypeEnum.GET_CHANEL_LIST.getCode(), LogisticsPlatformEnum.BAO_HONG.getCode(),
                        RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(chanelQueryVO), JSONUtil.toJsonStr(response));
                List<LogisticsSaleChannelEntity> channelEntityList = BaoHongConverter.INSTANCE.channelConvert(response.getData());
                return success(channelEntityList);
            }
        } catch (Exception e) {
            return failure(getPlatForm().getName() + ":" + e.getMessage());
        } finally {
            TransferLogisticsContext.remove();
        }
    }

    @Override
    public ApiResult authorization(Map<String, String> authMap) {
        try {
            TransferLogisticsContext.setAuthMap(authMap);

            BaoHongResponse<List<SmRow>> response = baoHongService.getShippingMethodList();
            if (isFailure(response)) {
                return failure("授权失败:" + response.getMessage());
            } else {
                return success("授权成功");
            }
        } catch (Exception e) {
            return failure(getPlatForm().getName() + ":" + e.getMessage());
        } finally {
            TransferLogisticsContext.remove();
        }
    }

    @Override
    public ApiResult<List<RegisterResponseVO>> registerLogisticsNumber(RegisterTrackVO registerTrackVO) {
        return super.registerLogisticsNumber(registerTrackVO);
    }

    @Override
    public LogisticsPlatformEnum getPlatForm() {
        return LogisticsPlatformEnum.BAO_HONG;
    }

    @Override
    public ApiResult<List<LogisticsServiceResponseVO>> listLogisticsService(Map<String, String> authMap) {
        return ApiResult.error(-1, "功能未开放");

    }

    private boolean isFailure(BaoHongResponse<?> response){
        return response.getAsk().equals("0");
    }
}
