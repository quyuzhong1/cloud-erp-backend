package com.erp.server.tms.service.logistics;

import cn.hutool.json.JSONUtil;
import com.common.business.annotation.LogisticsPlatformType;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.tms.enums.BusinessTypeEnum;
import com.erp.model.tms.enums.RequestStatusEnums;
import com.erp.model.tms.vo.request.LogisticsCancelOrderVO;
import com.erp.model.tms.vo.request.LogisticsGetLabelVO;
import com.erp.model.tms.vo.request.LogisticsOrderVO;
import com.erp.model.tms.vo.request.LogisticsQueryBaseVO;
import com.erp.model.tms.vo.response.CancelResponseVO;
import com.erp.model.tms.vo.response.ConfirmResponseVO;
import com.erp.model.tms.vo.response.LogisticsOrderResponseVO;
import com.erp.model.tms.vo.response.LogisticsPrintLabelResponse;
import com.erp.server.tms.handler.AbstractLogisticsHandler;
import com.erp.server.tms.service.LogisticsOrderOperateLogService;
import com.sdk.tms.express.model.base.BaseResult;
import com.sdk.tms.express.model.order.request.OrderQueryRequest;
import com.sdk.tms.express.model.order.request.OrderUpdateRequest;
import com.sdk.tms.express.model.order.response.OrderSearchRespDto;
import com.sdk.tms.express.model.order.response.OrderUpdateResponse;
import com.sdk.tms.express.model.order.response.WaybillNoInfoList;
import com.sdk.tms.express.service.ExpressShipperService;
import io.seata.common.util.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author zdy
 * @ClassName ExpressLogisticsHandlerImpl
 * @description: 顺丰物流接口开发
 * @date 2023年11月10日
 * @version: 1.0
 */
@Slf4j
@Component
@LogisticsPlatformType(LogisticsPlatformEnum.SF_EXPRESS)
public class ExpressLogisticsHandlerImpl extends AbstractLogisticsHandler {
    @Resource
    ExpressShipperService expressShipperService;
    @Resource
    private LogisticsOrderOperateLogService logisticsOrderOperateLogService;

    /**
     * 创建订单
     *
     * @param logisticsOrderVO
     * @return
     */
    public ApiResult<LogisticsOrderResponseVO> createOrder(LogisticsOrderVO logisticsOrderVO) {
        return ApiResult.error(-1, "功能未开放");
    }

    /**
     * 确认订单
     *
     * @param logisticsQueryBaseVOS
     * @return
     */
    @Override
    public ApiResult<List<ConfirmResponseVO>> confirmOrder(List<LogisticsQueryBaseVO> logisticsQueryBaseVOS) {
        List<ConfirmResponseVO> responseVOS = new ArrayList<>();
        boolean isSuccess = true;
        for (LogisticsQueryBaseVO logisticsQueryVO : logisticsQueryBaseVOS) {
            ConfirmResponseVO responseVO = new ConfirmResponseVO();
            //支持单个取消
            OrderUpdateRequest orderUpdateRequest = OrderUpdateRequest.builder()
                    .orderId(logisticsQueryVO.getDeliveryNo())
                    .dealType(1)
                    .build();
            //只支持单个订单取消
            boolean success = false;
            String msg = null;
            BaseResult baseResult = null;
            try {
                baseResult = expressShipperService.updateOrder(logisticsQueryVO.getLogisticsAuthEntity().getAccount(),
                        logisticsQueryVO.getLogisticsAuthEntity().getPassword(), orderUpdateRequest);
                //转换实体
                if (baseResult.isSuccess()) {
                    OrderUpdateResponse orderUpdateResponse = JSONUtil.toBean(baseResult.getMsgData(), OrderUpdateResponse.class);
                    if (2 == orderUpdateResponse.getResStatus()) {
                        success = true;
                        responseVO.success();
                    } else {
                        isSuccess = false;
                        responseVO.failure(getPlatForm().getName(), baseResult.getErrorCode(), baseResult.getErrorMsg());
                    }
                } else {
                    isSuccess = false;
                    responseVO.failure(getPlatForm().getName(), baseResult.getErrorCode(), baseResult.getErrorMsg());
                }
            } catch (Exception e) {
                isSuccess = false;
                responseVO.failure(getPlatForm().getName(), String.valueOf(-1), e.getMessage());
            }
            if (success) {
                logisticsOrderOperateLogService.addOperateLog(logisticsQueryVO.getLogisticsAuthEntity().getId(),
                        logisticsQueryVO.getTransportNo(), BusinessTypeEnum.CANCEL_ORDER.getCode(), LogisticsPlatformEnum.SF_EXPRESS.getCode(),
                        RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(logisticsQueryVO), JSONUtil.toJsonStr(baseResult));
            } else {
                logisticsOrderOperateLogService.addOperateLog(logisticsQueryVO.getLogisticsAuthEntity().getId(),
                        logisticsQueryVO.getTransportNo(), BusinessTypeEnum.CANCEL_ORDER.getCode(), LogisticsPlatformEnum.SF_EXPRESS.getCode(),
                        RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsQueryVO), JSONUtil.toJsonStr(baseResult));
            }
            responseVOS.add(responseVO);
        }
        return isSuccess ? success(responseVOS) : failure(responseVOS);
    }

    /**
     * 取消订单
     *
     * @param logisticsQueryVOS
     * @return
     */
    @Override
    public ApiResult<List<CancelResponseVO>> cancelOrder(List<LogisticsCancelOrderVO> logisticsQueryVOS) {
        List<CancelResponseVO> responseVOS = new ArrayList<>(logisticsQueryVOS.size());
        boolean isSuccess = true;
        for (LogisticsCancelOrderVO logisticsQueryVO : logisticsQueryVOS) {
            CancelResponseVO responseVO = new CancelResponseVO();
            boolean success = false;
            BaseResult baseResult = null;
            //支持单个取消
            OrderUpdateRequest orderUpdateRequest = OrderUpdateRequest.builder()
                    .orderId(logisticsQueryVO.getDeliveryNo())
                    .dealType(2)
                    .build();
            try {
                baseResult = expressShipperService.updateOrder(logisticsQueryVO.getLogisticsAuthEntity().getAccount(),
                        logisticsQueryVO.getLogisticsAuthEntity().getPassword(), orderUpdateRequest);
                //转换实体
                if (baseResult.isSuccess()) {
                    OrderUpdateResponse orderUpdateResponse = JSONUtil.toBean(baseResult.getMsgData(), OrderUpdateResponse.class);
                    if (2 == orderUpdateResponse.getResStatus()) {
                        success = true;
                        responseVO.success();
                    } else {
                        isSuccess = false;
                        responseVO.failure(getPlatForm().getName(), baseResult.getErrorCode(), baseResult.getErrorMsg());
                    }
                } else {
                    isSuccess = false;
                    responseVO.failure(getPlatForm().getName(), baseResult.getErrorCode(), baseResult.getErrorMsg());
                }
            } catch (Exception e) {
                isSuccess = false;
                responseVO.failure(getPlatForm().getName(), String.valueOf(-1), e.getMessage());
            }
            if (success) {
                logisticsOrderOperateLogService.addOperateLog(logisticsQueryVO.getLogisticsAuthEntity().getId(),
                        logisticsQueryVO.getTransportNo(), BusinessTypeEnum.CANCEL_ORDER.getCode(), LogisticsPlatformEnum.SF_EXPRESS.getCode(),
                        RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(logisticsQueryVO), JSONUtil.toJsonStr(baseResult));
            } else {
                logisticsOrderOperateLogService.addOperateLog(logisticsQueryVO.getLogisticsAuthEntity().getId(),
                        logisticsQueryVO.getTransportNo(), BusinessTypeEnum.CANCEL_ORDER.getCode(), LogisticsPlatformEnum.SF_EXPRESS.getCode(),
                        RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsQueryVO), JSONUtil.toJsonStr(baseResult));
            }
            responseVOS.add(responseVO);
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
        List<LogisticsOrderResponseVO> responseVOS = new ArrayList<>(logisticsQueryVOList.size());
        boolean isSuccess = true;
        for (LogisticsQueryBaseVO logisticsQueryVO : logisticsQueryVOList) {
            ApiResult<LogisticsOrderResponseVO> result = this.queryOrder(logisticsQueryVO);
            if (!result.isSuccess()) {
                isSuccess = false;
            }
            responseVOS.add(result.getData());
        }
        return isSuccess ? success(responseVOS) : failure(responseVOS);
    }

    private ApiResult<LogisticsOrderResponseVO> queryOrder(LogisticsQueryBaseVO logisticsQueryVO) {
        boolean success = false;
        String msg = null;
        BaseResult baseResult = null;
        LogisticsOrderResponseVO logisticsOrderResponseVO = new LogisticsOrderResponseVO();
        OrderQueryRequest orderQueryRequest = OrderQueryRequest.builder()
                .orderId(logisticsQueryVO.getDeliveryNo())
                .searchType(1)
                .language("zh-CN")
                .build();
        try {
            baseResult = expressShipperService.queryOrder(logisticsQueryVO.getLogisticsAuthEntity().getAccount(),
                    logisticsQueryVO.getLogisticsAuthEntity().getPassword(), orderQueryRequest);
            if (baseResult.isSuccess()) {
                OrderSearchRespDto orderSearchRespDto = JSONUtil.toBean(baseResult.getMsgData(), OrderSearchRespDto.class);
                logisticsOrderResponseVO.setDeliveryNo(orderSearchRespDto.getOrderId());
                //运单号列表
                List<WaybillNoInfoList> waybillNoInfoList = orderSearchRespDto.getWaybillNoInfoList();
                WaybillNoInfoList waybillNoInfoList1 = waybillNoInfoList.stream().filter(e -> e.getWaybillType() == 1).findFirst().orElse(null);
                if (Objects.nonNull(waybillNoInfoList1)) {
                    logisticsOrderResponseVO.setTransportNo(waybillNoInfoList1.getWaybillNo());
                    logisticsOrderResponseVO.setTrackNo(waybillNoInfoList1.getWaybillNo());
                }
                List<WaybillNoInfoList> collect = waybillNoInfoList.stream().filter(e -> e.getWaybillType() == 2).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(collect)) {
                    logisticsOrderResponseVO.setMore(true);
                    List<LogisticsOrderResponseVO> vos = new ArrayList<>(collect.size());
                    collect.forEach(waybillNoInfoList2 -> {
                        vos.add(LogisticsOrderResponseVO.builder().deliveryNo(waybillNoInfoList1.getWaybillNo())
                                .trackNo(waybillNoInfoList2.getWaybillNo())
                                .transportNo(waybillNoInfoList2.getWaybillNo()).build());
                    });
                    logisticsOrderResponseVO.setLogisticsOrderResponseVOS(vos);
                }
                logisticsOrderResponseVO.success();
                success = true;
            } else {
                logisticsOrderResponseVO.failure(getPlatForm().getName(), baseResult.getErrorCode(), baseResult.getErrorMsg());
            }
        } catch (UnsupportedEncodingException e) {
            logisticsOrderResponseVO.failure(getPlatForm().getName(), String.valueOf(-1), e.getMessage());
            log.error("查询订单异常：{}", e.getMessage());
        }
        if (success) {
            logisticsOrderOperateLogService.addOperateLog(logisticsQueryVO.getLogisticsAuthEntity().getId(),
                    logisticsQueryVO.getTransportNo(), BusinessTypeEnum.QUERY_ORDER.getCode(), LogisticsPlatformEnum.SF_EXPRESS.getCode(),
                    RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(logisticsQueryVO), JSONUtil.toJsonStr(baseResult));
        } else {
            logisticsOrderOperateLogService.addOperateLog(logisticsQueryVO.getLogisticsAuthEntity().getId(),
                    logisticsQueryVO.getTransportNo(), BusinessTypeEnum.QUERY_ORDER.getCode(), LogisticsPlatformEnum.SF_EXPRESS.getCode(),
                    RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsQueryVO), JSONUtil.toJsonStr(baseResult));
        }
        return success ? success(logisticsOrderResponseVO) : failure(logisticsOrderResponseVO);
    }

    /**
     * 获取标签
     *
     * @param logisticsQueryVO
     * @return
     */
    public ApiResult<List<LogisticsPrintLabelResponse>> getLabelList(LogisticsGetLabelVO logisticsQueryVO) throws IOException {
        return ApiResult.error(-1, "功能未开放");
    }

    @Override
    public LogisticsPlatformEnum getPlatForm() {
        return LogisticsPlatformEnum.SF_EXPRESS;
    }
}
