package com.erp.server.tms.service.logistics;

import com.common.business.annotation.LogisticsPlatformType;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.utils.FileUtil;
import com.erp.model.tms.entity.LogisticsAuthEntity;
import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
import com.erp.model.tms.vo.request.ChanelQueryVO;
import com.erp.model.tms.vo.request.LogisticsGetLabelVO;
import com.erp.model.tms.vo.request.LogisticsOrderVO;
import com.erp.model.tms.vo.request.LogisticsQueryBaseVO;
import com.erp.model.tms.vo.response.LogisticsOrderResponseVO;
import com.erp.model.tms.vo.response.LogisticsPrintLabelResponse;
import com.erp.server.tms.convert.LogisticsChannelConverter;
import com.erp.server.tms.convert.LogisticsOrderConverter;
import com.erp.server.tms.handler.AbstractLogisticsHandler;
import com.erp.server.tms.service.LogisticsAuthService;
import com.sdk.tms.tongyou.dto.request.TongYouCreateOrderRequest;
import com.sdk.tms.tongyou.dto.request.TongYouGetOrderRequest;
import com.sdk.tms.tongyou.dto.request.TongYouPrintLabelRequest;
import com.sdk.tms.tongyou.dto.response.*;
import com.sdk.tms.tongyou.server.TongYouService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * 纬狮物流接口处理器
 */
@Slf4j
@Component
@LogisticsPlatformType(LogisticsPlatformEnum.TONG_YOU)
public class TongYouLogisticsHandlerImpl extends AbstractLogisticsHandler {

    @Resource
    private LogisticsAuthService logisticsAuthService;

    @Resource
    private TongYouService tongYouService;

    @Override
    public LogisticsAuthEntity getLogisticsAuthConfig(String authId) {
        //自定义渠道配置信息 支持 物流：渠道 = 1：n
        return logisticsAuthService.getById(authId);
    }

    @Override
    public ApiResult<List<LogisticsSaleChannelEntity>> getChannel(ChanelQueryVO chanelQueryVO) {
        TongYouResponse<List<TongYouChannel>> tongYouResponse =  tongYouService.getAllChannel();
        if(!tongYouResponse.getSuccess()){
            return ApiResult.error(ApiError.CALL_THIRD_LOGISTICS_PLATFORM_ERROR.code,tongYouResponse.getMsg());
        }
        List<LogisticsSaleChannelEntity> response = LogisticsChannelConverter.INSTANCE.channelConvertByTongYou(tongYouResponse.getData());
        return success(response);
    }

    @Override
    public ApiResult<LogisticsOrderResponseVO> createOrder(LogisticsOrderVO logisticsOrderVO) {
        TongYouCreateOrderRequest request = LogisticsOrderConverter.INSTANCE.orderRequestByTongYou(logisticsOrderVO);
        TongYouCreateOrder tongYouCreateOrder = tongYouService.createOrder(request);
        if(!tongYouCreateOrder.getSuccess()){
            return ApiResult.error(ApiError.CALL_THIRD_LOGISTICS_PLATFORM_ERROR.code,tongYouCreateOrder.getMsg());
        }
        return success(LogisticsOrderResponseVO.builder()
                .transportNo(tongYouCreateOrder.getLogisticsNo())
                .deliveryNo(logisticsOrderVO.getDeliveryNo())
                .build());
    }


    @Override
    public ApiResult<List<LogisticsPrintLabelResponse>> getLabelList(List<LogisticsGetLabelVO> labelVO) throws IOException {
        List<LogisticsPrintLabelResponse> responseList = new ArrayList<>();
        boolean isSuccess = true;
        //通邮接口有说支持多个单号，但实际测试发现，传多个单号，如果其中某个失败，结果会返回成功单号的pdf信息，但不会表示哪个单号失败，导致我们这边无法判断到底是否全部成功打印，所以单个请求
        for(LogisticsGetLabelVO logisticsGetLabelVO : labelVO){
            TongYouPrintLabelRequest request = TongYouPrintLabelRequest.builder()
                    .orderNo(logisticsGetLabelVO.getDeliveryNo())
                    .trackNo(logisticsGetLabelVO.getTrackNo())
                    .logisticsId(logisticsGetLabelVO.getLogisticsChannelEntity().getCode())
                    .isPaoc(logisticsGetLabelVO.getIsPdn())
                    .isPcd(logisticsGetLabelVO.getIsPcd())
                    .build();
            TongYouPrintLabel tongYouResponse = tongYouService.printLabel(request);
            //调用接口失败，不立刻返回，继续剩下的调用
            if(!tongYouResponse.getSuccess()){
                LogisticsPrintLabelResponse response = new LogisticsPrintLabelResponse();
                response.failure(getName(),logisticsGetLabelVO.getDeliveryNo(),tongYouResponse.getMsg());
                responseList.add(response);
                isSuccess = false;
                continue;
            }

            LogisticsPrintLabelResponse response = new LogisticsPrintLabelResponse();
            response.setDeliveryNoList(Collections.singletonList(logisticsGetLabelVO.getDeliveryNo()));
            response.setTransportNoList(Collections.singletonList(logisticsGetLabelVO.getTransportNo()));
            response.setTrackNoList(Collections.singletonList(logisticsGetLabelVO.getTrackNo()));
            //返回格式是base64
            if(tongYouResponse.getType().equals(0)){
                response.setBase64(tongYouResponse.getBase64());
            }else{
                //返回是url
                response.setBase64(FileUtil.convertPdfUrlToBase64(tongYouResponse.getUrl()));
            }
            response.success();
            responseList.add(response);
        }
        return isSuccess?success(responseList):failure(responseList);
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
        for(LogisticsQueryBaseVO logisticsQueryBaseVO : logisticsQueryVOList){
            TongYouGetOrderRequest request = TongYouGetOrderRequest.builder()
                    .orderNo(logisticsQueryBaseVO.getDeliveryNo())
                    .build();
            TongYouOrderInfo tongYouOrderInfo = tongYouService.getOrderInfo(request);
            LogisticsOrderResponseVO response = new LogisticsOrderResponseVO();
            if(!tongYouOrderInfo.getSuccess()){
                response.failure(getName(),logisticsQueryBaseVO.getDeliveryNo(),tongYouOrderInfo.getMsg());
                responseList.add(response);
                isSuccess = false;
                continue;
            }
            response = LogisticsOrderConverter.INSTANCE.orderQueryByTongYou(tongYouOrderInfo);
            responseList.add(response);
        }
        return isSuccess?success(responseList):failure(responseList);
    }

    private String getName(){return getPlatForm().getName();}


    @Override
    public LogisticsPlatformEnum getPlatForm() {
        return LogisticsPlatformEnum.TONG_YOU;
    }
}
