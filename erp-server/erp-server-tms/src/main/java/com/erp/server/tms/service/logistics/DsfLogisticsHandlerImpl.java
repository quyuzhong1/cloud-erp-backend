package com.erp.server.tms.service.logistics;

import cn.hutool.json.JSONUtil;
import com.alibaba.nacos.api.utils.StringUtils;
import com.common.business.annotation.LogisticsPlatformType;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.core.controller.vo.ApiResult;
import com.common.core.utils.FileUtil;
import com.erp.model.tms.entity.LogisticsAuthEntity;
import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
import com.erp.model.tms.enums.BusinessTypeEnum;
import com.erp.model.tms.enums.RequestStatusEnums;
import com.erp.model.tms.vo.request.*;
import com.erp.model.tms.vo.response.CancelResponseVO;
import com.erp.model.tms.vo.response.LogisticsOrderResponseVO;
import com.erp.model.tms.vo.response.LogisticsPrintLabelResponse;
import com.erp.server.tms.convert.LogisticsChannelConverter;
import com.erp.server.tms.convert.LogisticsOrderConverter;
import com.erp.server.tms.handler.AbstractLogisticsHandler;
import com.erp.server.tms.service.LogisticsAuthService;
import com.erp.server.tms.service.LogisticsOrderOperateLogService;
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
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author zdy
 * @ClassName DsfLogisticsHandlerImpl
 * @description: TODO
 * @date 2023年11月02日
 * @version: 1.0
 */
@Slf4j
@Component
@LogisticsPlatformType(LogisticsPlatformEnum.DSF)
public class DsfLogisticsHandlerImpl extends AbstractLogisticsHandler {

    @Resource
    private LogisticsAuthService logisticsAuthService;
    @Resource
    private DsfShipperService dsfShipperService;
    @Resource
    private LogisticsOrderOperateLogService logisticsOrderOperateLogService;

    @Override
    public LogisticsAuthEntity getLogisticsAuthConfig(String authId) {
        //自定义渠道配置信息 支持 物流：渠道 = 1：n
        return logisticsAuthService.getById(authId);
    }

    private List<Parcel> getParcel(LogisticsOrderVO logisticsOrderVO) {
        if (CollectionUtils.isEmpty(logisticsOrderVO.getLogisticsProductVOList())) {
            return Collections.emptyList();
        }
        List<Parcel> parcelList = new ArrayList<>(logisticsOrderVO.getLogisticsProductVOList().size());
        logisticsOrderVO.getLogisticsProductVOList().forEach(logisticsProductVO -> {
            Parcel parcel = new Parcel();
            parcel.setWeight(logisticsProductVO.getWeight());
            parcel.setParcel_value(logisticsProductVO.getDeclarePrice());
            parcel.setCurrency(logisticsProductVO.getDeclareCurrency());
            if (logisticsProductVO.isElectric()) {
                parcel.setInclude_battery("Y");
            } else {
                parcel.setInclude_battery("N");
            }
            //海关申报信息
            DeclareProductInfo productInfo = LogisticsOrderConverter.INSTANCE.dsfProductMapping(logisticsProductVO);
            parcel.setDeclare_product_info(Collections.singletonList(productInfo));
            parcelList.add(parcel);
        });
        return parcelList;
    }

    @Override
    public ApiResult<LogisticsOrderResponseVO> createOrder(LogisticsOrderVO logisticsOrderVO) {
        ApiResult apiResult = new ApiResult();
        OrderRequest orderRequest = LogisticsOrderConverter.INSTANCE.orderRequestToDsf(logisticsOrderVO);
        //包裹信息封装
        orderRequest.setParcelList(getParcel(logisticsOrderVO));
        //保险信息封装 暂时不做 默认为N
        orderRequest.setIs_insure("N");
        ResponseMsg responseMsg = dsfShipperService.createOrder(logisticsOrderVO.getLogisticsAuthEntity().getAccount(),
                logisticsOrderVO.getLogisticsAuthEntity().getPassword(), orderRequest);
        if (StringUtils.isBlank(responseMsg.getResult()) || !Objects.equals("1", responseMsg.getResult())) {
            apiResult.setMsg(responseMsg.getMsg());
            apiResult.setCode(-1);
            logisticsOrderOperateLogService.addOperateLog(logisticsOrderVO.getLogisticsAuthEntity().getId(),
                    logisticsOrderVO.getDeliveryNo(), BusinessTypeEnum.CREATE_ORDER.getCode(), LogisticsPlatformEnum.DSF.getCode(),
                    RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsOrderVO), JSONUtil.toJsonStr(responseMsg));
        } else {
            apiResult.setCode(200);
            OrderResponse orderResponse = JSONUtil.toBean(JSONUtil.parseObj(responseMsg.getData()), OrderResponse.class);
            apiResult.setData(LogisticsOrderResponseVO.builder()
                    .transportNo(orderResponse.getRef_no())
                    .trackNo(orderResponse.getTracking_no())
                    .transportNo(orderResponse.getDs_consignment_no())
                    .logisticsChannelNo(orderResponse.getLogistics_channel_no())
                    .odaResultSign(orderResponse.getOda_result_sign())
                    .build());
            apiResult.setMsg(responseMsg.getMsg());
            logisticsOrderOperateLogService.addOperateLog(logisticsOrderVO.getLogisticsAuthEntity().getId(),
                    logisticsOrderVO.getDeliveryNo(), BusinessTypeEnum.CREATE_ORDER.getCode(), LogisticsPlatformEnum.DSF.getCode(),
                    RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(logisticsOrderVO), JSONUtil.toJsonStr(responseMsg));
        }

        return apiResult;
    }

    /**
     * 取消订单
     *
     * @param logisticsCancelOrderVOS
     * @return
     */
    @Override
    public ApiResult<List<CancelResponseVO>> cancelOrder(List<LogisticsCancelOrderVO> logisticsCancelOrderVOS) {
        LogisticsCancelOrderVO logisticsCancelOrderVO = logisticsCancelOrderVOS.stream().filter(e -> Objects.nonNull(e.getLogisticsAuthEntity())).findFirst().orElse(null);
        List<CancelResponseVO> responseVOS = new ArrayList<>();
        boolean isSuccess = true;
        for (LogisticsCancelOrderVO logisticsQueryVO : logisticsCancelOrderVOS) {
            //只支持单个订单取消
            CancelResponseVO responseVO = new CancelResponseVO();
            try {
                OrderCancelRequest orderCancelRequest = OrderCancelRequest.builder().build();
                ResponseMsg orderResponse = dsfShipperService.cancelOrder(logisticsCancelOrderVO.getLogisticsAuthEntity().getAccount(),
                        logisticsCancelOrderVO.getLogisticsAuthEntity().getPassword(), orderCancelRequest);
                logisticsOrderOperateLogService.addOperateLog(logisticsQueryVO.getLogisticsAuthEntity().getId(),
                        logisticsQueryVO.getDeliveryNo(), BusinessTypeEnum.CANCEL_ORDER.getCode(), LogisticsPlatformEnum.DSF.getCode(),
                        RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(logisticsQueryVO), JSONUtil.toJsonStr(orderResponse));
                responseVO.setDeliveryNo(logisticsQueryVO.getDeliveryNo());
                responseVO.setTransportNo(logisticsQueryVO.getTransportNo());
                responseVO.setTrackNo(logisticsQueryVO.getTrackNo());
                if ("1".equalsIgnoreCase(orderResponse.getResult())){
                    responseVO.success();
                }else {
                    isSuccess = false;
                    logisticsOrderOperateLogService.addOperateLog(logisticsQueryVO.getLogisticsAuthEntity().getId(),
                            logisticsQueryVO.getDeliveryNo(), BusinessTypeEnum.CANCEL_ORDER.getCode(), LogisticsPlatformEnum.DSF.getCode(),
                            RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsQueryVO), JSONUtil.toJsonStr(orderResponse));
                    responseVO.failure(LogisticsPlatformEnum.DSF.getName(),"-1", orderResponse.getMsg());
                }
                responseVOS.add(responseVO);
            } catch (Exception e) {
                logisticsOrderOperateLogService.addOperateLog(logisticsQueryVO.getLogisticsAuthEntity().getId(),
                        logisticsQueryVO.getDeliveryNo(), BusinessTypeEnum.CANCEL_ORDER.getCode(), LogisticsPlatformEnum.DSF.getCode(),
                        RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsQueryVO), JSONUtil.toJsonStr(e.getMessage()));
                isSuccess = false;
            }
        }
        return isSuccess?success(responseVOS):failure(responseVOS);
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
        logisticsQueryVOList.forEach(logisticsQueryBaseVO -> {
            OrderQueryRequest orderQueryRequest = OrderQueryRequest.builder()
                    .request_no(logisticsQueryBaseVO.getDeliveryNo())
                    .build();
            ResponseMsg responseMsg = dsfShipperService.queryOrder(logisticsQueryBaseVO.getLogisticsAuthEntity().getAccount(),
                    logisticsQueryBaseVO.getLogisticsAuthEntity().getPassword(), orderQueryRequest);
            //失败
            if (!StringUtils.isBlank(responseMsg.getResult()) && Objects.equals("1", responseMsg.getResult())) {
                QueryOrderResponse queryOrderResponse = JSONUtil.toBean(JSONUtil.toJsonStr(responseMsg.getData()), QueryOrderResponse.class);
                LogisticsOrderResponseVO orderResponseVO = LogisticsOrderResponseVO.builder()
                        .transportNo(queryOrderResponse.getConsignmentInfo().getRef_no())
                        .trackNo(queryOrderResponse.getConsignmentInfo().getTracking_no())
                        .transportNo(queryOrderResponse.getConsignmentInfo().getDs_consignment_no())
                        .logisticsChannelNo(queryOrderResponse.getConsignmentInfo().getLogistics_channel_no())
                        .odaResultSign(queryOrderResponse.getConsignmentInfo().getOda_result_sign())
                        .build();
                logisticsOrderOperateLogService.addOperateLog(logisticsQueryBaseVO.getLogisticsAuthEntity().getId(),
                        logisticsQueryBaseVO.getTransportNo(), BusinessTypeEnum.QUERY_ORDER.getCode(), LogisticsPlatformEnum.DSF.getCode(),
                        RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(logisticsQueryBaseVO), JSONUtil.toJsonStr(responseMsg));
                list.add(orderResponseVO);
            }
        });
        return success(list);
    }

    /**
     * 查询订单
     *
     * @param logisticsQueryVO
     * @return
     */
    public ApiResult<LogisticsOrderResponseVO> queryOrder(LogisticsQueryBaseVO logisticsQueryVO) {
        OrderQueryRequest orderQueryRequest = OrderQueryRequest.builder()
                .request_no(logisticsQueryVO.getTransportNo())
                .build();
        ResponseMsg responseMsg = dsfShipperService.queryOrder(logisticsQueryVO.getLogisticsAuthEntity().getAccount(),
                logisticsQueryVO.getLogisticsAuthEntity().getPassword(), orderQueryRequest);
        //失败
        if (StringUtils.isBlank(responseMsg.getResult()) || !Objects.equals("1", responseMsg.getResult())) {
            logisticsOrderOperateLogService.addOperateLog(logisticsQueryVO.getLogisticsAuthEntity().getId(),
                    logisticsQueryVO.getTransportNo(), BusinessTypeEnum.QUERY_ORDER.getCode(), LogisticsPlatformEnum.DSF.getCode(),
                    RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsQueryVO), JSONUtil.toJsonStr(responseMsg));
            return failure(responseMsg.getMsg());
        } else {
            QueryOrderResponse queryOrderResponse = JSONUtil.toBean(JSONUtil.toJsonStr(responseMsg.getData()), QueryOrderResponse.class);
            LogisticsOrderResponseVO orderResponseVO = LogisticsOrderResponseVO.builder()
                    .transportNo(queryOrderResponse.getConsignmentInfo().getRef_no())
                    .trackNo(queryOrderResponse.getConsignmentInfo().getTracking_no())
                    .transportNo(queryOrderResponse.getConsignmentInfo().getDs_consignment_no())
                    .logisticsChannelNo(queryOrderResponse.getConsignmentInfo().getLogistics_channel_no())
                    .odaResultSign(queryOrderResponse.getConsignmentInfo().getOda_result_sign())
                    .build();
            logisticsOrderOperateLogService.addOperateLog(logisticsQueryVO.getLogisticsAuthEntity().getId(),
                    logisticsQueryVO.getTransportNo(), BusinessTypeEnum.QUERY_ORDER.getCode(), LogisticsPlatformEnum.DSF.getCode(),
                    RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(logisticsQueryVO), JSONUtil.toJsonStr(responseMsg));
            return success(orderResponseVO);
        }
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
        LogisticsGetLabelVO logisticsGetLabelVO = logisticsQueryVO.stream().filter(e -> Objects.nonNull(e.getLogisticsAuthEntity())).findFirst().orElse(null);
        assert logisticsGetLabelVO != null;
        LabelRequest labelRequest = LabelRequest.builder()
                .requestNo(logisticsQueryVO.stream().map(LogisticsGetLabelVO::getDeliveryNo).collect(Collectors.toList()))
                .logisticsProductCode(logisticsGetLabelVO.getLogisticsChannelEntity().getCode())
                .build();
        ResponseMsg responseMsg = dsfShipperService.getLabelList(logisticsGetLabelVO.getLogisticsAuthEntity().getAccount(),
                logisticsGetLabelVO.getLogisticsAuthEntity().getPassword(), labelRequest);
        LogisticsPrintLabelResponse response = new LogisticsPrintLabelResponse();
        //失败
        if (StringUtils.isBlank(responseMsg.getResult()) || !Objects.equals("1", responseMsg.getResult())) {
            logisticsOrderOperateLogService.addOperateLog(logisticsGetLabelVO.getLogisticsAuthEntity().getId(),
                    logisticsGetLabelVO.getDeliveryNo(), BusinessTypeEnum.GET_LABEL_LIST.getCode(), LogisticsPlatformEnum.DSF.getCode(),
                    RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsQueryVO), JSONUtil.toJsonStr(responseMsg));
            response.failure(LogisticsPlatformEnum.DSF.getName(),responseMsg.getResult(),responseMsg.getMsg());
            responses.add(response);
            return failure(responses);
        } else {
            logisticsOrderOperateLogService.addOperateLog(logisticsGetLabelVO.getLogisticsAuthEntity().getId(),
                    logisticsGetLabelVO.getDeliveryNo(), BusinessTypeEnum.GET_LABEL_LIST.getCode(), LogisticsPlatformEnum.DSF.getCode(),
                    RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(logisticsQueryVO), JSONUtil.toJsonStr(responseMsg));
            //TODO 结果："http://bss-fss.i4px.com/fpx-print-label-e1298724-0b8d-4be3-8238-bd7a96d9874b.pdf" 需要考虑 pdf转图片

            response =LogisticsPrintLabelResponse.builder()
                    .deliveryNoList(logisticsQueryVO.stream().map(LogisticsGetLabelVO::getDeliveryNo).collect(Collectors.toList()))
                    .base64(FileUtil.convertPdfUrlToBase64((String) responseMsg.getData())).build();
            response.success();
            responses.add(response);
            return success(responses);
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
        ResponseMsg responseMsg = dsfShipperService.getChanelList(chanelQueryVO.getLogisticsAuthEntity().getAccount(),
                chanelQueryVO.getLogisticsAuthEntity().getPassword(), chanelRequest);
        //失败
        if (StringUtils.isBlank(responseMsg.getResult()) || !Objects.equals("1", responseMsg.getResult())) {
            logisticsOrderOperateLogService.addOperateLog(chanelQueryVO.getLogisticsAuthEntity().getId(),
                    chanelQueryVO.getTransportMode(), BusinessTypeEnum.GET_CHANEL_LIST.getCode(), LogisticsPlatformEnum.DSF.getCode(),
                    RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(chanelQueryVO), JSONUtil.toJsonStr(responseMsg));
            return failure(responseMsg.getMsg());
        } else {
            List<ChanelInfo> chanelInfos = JSONUtil.toList(JSONUtil.toJsonStr(responseMsg.getData()), ChanelInfo.class);
            logisticsOrderOperateLogService.addOperateLog(chanelQueryVO.getLogisticsAuthEntity().getId(),
                    chanelQueryVO.getTransportMode(), BusinessTypeEnum.GET_CHANEL_LIST.getCode(), LogisticsPlatformEnum.DSF.getCode(),
                    RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(chanelQueryVO), JSONUtil.toJsonStr(responseMsg));
            return success(LogisticsChannelConverter.INSTANCE.channelConvertByDSFList(chanelInfos));
        }
    }

    @Override
    public LogisticsPlatformEnum getPlatForm() {
        return LogisticsPlatformEnum.DSF;
    }
}
