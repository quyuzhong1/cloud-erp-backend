package com.erp.server.tms.service.logistics;

import com.common.business.annotation.PlatformType;
import com.common.business.enums.PlatformDictEnum;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.erp.model.tms.entity.LogisticsAuthEntity;
import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
import com.erp.model.tms.vo.request.ChanelQueryVO;
import com.erp.model.tms.vo.request.LogisticsCancelOrderVO;
import com.erp.model.tms.vo.request.LogisticsGetLabelVO;
import com.erp.model.tms.vo.request.LogisticsOrderVO;
import com.erp.model.tms.vo.request.LogisticsQueryBaseVO;
import com.erp.model.tms.vo.response.LogisticsOrderResponseVO;
import com.erp.server.tms.convert.LogisticsChannelConverter;
import com.erp.server.tms.convert.LogisticsOrderConverter;
import com.erp.server.tms.handler.AbstractLogisticsHandler;
import com.erp.server.tms.service.LogisticsAuthService;
import com.sdk.tms.yanwen.dto.request.YanWenCancelOrderRequest;
import com.sdk.tms.yanwen.dto.request.YanWenCreateWayBillRequest;
import com.sdk.tms.yanwen.dto.request.YanWenGetLabelRequest;
import com.sdk.tms.yanwen.dto.request.YanWenQueryOrderRequest;
import com.sdk.tms.yanwen.dto.response.*;
import com.sdk.tms.yanwen.server.YanWenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;


/**
 * 燕文物流接口处理器
 */
@Slf4j
@Component
@PlatformType(PlatformDictEnum.YAN_WEN)
public class YanWenLogisticsHandlerImpl extends AbstractLogisticsHandler {

    @Resource
    private LogisticsAuthService logisticsAuthService;

    @Resource
    private YanWenService yanWenService;

    @Override
    public LogisticsAuthEntity getLogisticsAuthConfig(String authId) {
        //自定义渠道配置信息 支持 物流：渠道 = 1：n
        return logisticsAuthService.getById(authId);
    }

    @Override
    public ApiResult<List<LogisticsSaleChannelEntity>> getChannel(ChanelQueryVO chanelQueryVO) {
        YanWenResponse<List<YanWenChannel>> yanWenResponse =  yanWenService.getAllChannel();
        if(!yanWenResponse.getSuccess()){
            return ApiResult.error(ApiError.ERROR_500.code,yanWenResponse.getMessage());
        }
        List<LogisticsSaleChannelEntity> response = LogisticsChannelConverter.INSTANCE.channelConvertByYanWenList(yanWenResponse.getData());
        return success(response);
    }

    @Override
    public ApiResult<LogisticsOrderResponseVO> createOrder(LogisticsOrderVO logisticsOrderVO) {
        YanWenCreateWayBillRequest request = LogisticsOrderConverter.INSTANCE.orderRequestByYanWen(logisticsOrderVO);
        YanWenResponse<YanWenCreateWayBill> yanWenResponse = yanWenService.createWayBill(request);
        if(!yanWenResponse.getSuccess()){
            return ApiResult.error(ApiError.ERROR_500.code,yanWenResponse.getMessage());
        }
        return success(new LogisticsOrderResponseVO(yanWenResponse.getData().getWaybillNumber(),yanWenResponse.getData().getWaybillNumber(),
                logisticsOrderVO.getDeliveryNo(),null,null));
    }

    @Override
    public ApiResult<String> getLabelUrl(LogisticsGetLabelVO labelVO) {
        YanWenGetLabelRequest request = YanWenGetLabelRequest.builder()
                .waybillNumber(labelVO.getTransportNo().get(0))
                .printRemark(labelVO.getPrintRemark())
                .build();
        YanWenResponse<YanWenGetLabel> labelResponse = yanWenService.getLabel(request);
        if(!labelResponse.getSuccess()){
            return ApiResult.error(ApiError.ERROR_500.code,labelResponse.getMessage());
        }
        return success(labelResponse.getData().getBase64String());
    }

    @Override
    public ApiResult<String> cancelOrder(LogisticsCancelOrderVO cancelOrderVO) {
        YanWenCancelOrderRequest request = YanWenCancelOrderRequest.builder()
                .waybillNumber(cancelOrderVO.getTransportNo().get(0))
                .note(cancelOrderVO.getReason())
                .build();
        YanWenResponse<String> yanWenResponse =  yanWenService.cancelOrder(request);
        if(!yanWenResponse.getSuccess()){
            return ApiResult.error(ApiError.ERROR_500.code,yanWenResponse.getMessage());
        }
        return success();
    }

    @Override
    public ApiResult<List<LogisticsOrderResponseVO>> queryOrder(List<LogisticsQueryBaseVO> logisticsQueryVOList){
        YanWenQueryOrderRequest request = YanWenQueryOrderRequest.builder()
                .listNumber(logisticsQueryVOList.stream().map(LogisticsQueryBaseVO :: getDeliveryNo).collect(Collectors.toList()))
                .build();
        YanWenResponse<List<YanWenQueryOrder>> yanWenResponse = yanWenService.queryOrder(request);
        if(!yanWenResponse.getSuccess()){
            return ApiResult.error(ApiError.ERROR_500.code,yanWenResponse.getMessage());
        }
        List<LogisticsOrderResponseVO> list = LogisticsOrderConverter.INSTANCE.orderQueryByYanWen(yanWenResponse.getData());
        return success(list);
    }
}
