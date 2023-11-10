package com.erp.server.tms.service.logistics;

import cn.hutool.json.JSONUtil;
import com.common.business.annotation.LogisticsPlatformType;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.tms.enums.BusinessTypeEnums;
import com.erp.model.tms.enums.RequestStatusEnums;
import com.erp.model.tms.vo.request.LogisticsCancelOrderVO;
import com.erp.model.tms.vo.request.LogisticsGetLabelVO;
import com.erp.model.tms.vo.request.LogisticsOrderVO;
import com.erp.model.tms.vo.request.LogisticsQueryBaseVO;
import com.erp.model.tms.vo.response.LogisticsOrderResponseVO;
import com.erp.model.tms.vo.response.LogisticsPrintLabelResponse;
import com.erp.server.tms.handler.AbstractLogisticsHandler;
import com.erp.server.tms.service.LogisticsOrderOperateLogService;
import com.sdk.tms.express.model.base.BaseResult;
import com.sdk.tms.express.model.order.request.OrderQueryRequest;
import com.sdk.tms.express.model.order.request.OrderUpdateRequest;
import com.sdk.tms.express.model.order.response.OrderUpdateResponse;
import com.sdk.tms.express.service.ExpressShipperService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * @author zdy
 * @ClassName ExpressLogisticsHandlerImpl
 * @description: TODO
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
     * @param logisticsQueryVO
     * @return
     */
    public ApiResult<String> confirmOrder(LogisticsQueryBaseVO logisticsQueryVO) {
        //支持单个取消
        OrderUpdateRequest orderUpdateRequest = OrderUpdateRequest.builder()
                .orderId(logisticsQueryVO.getDeliveryNo().get(0))
                .dealType(1)
                .build();
        boolean success;
        String msg = null;
        BaseResult baseResult = null;
        //只支持单个订单取消
        try {
            baseResult = expressShipperService.updateOrder(logisticsQueryVO.getLogisticsAuthEntity().getAccount(),
                    logisticsQueryVO.getLogisticsAuthEntity().getPassword(), orderUpdateRequest);
            //转换实体
            if (baseResult.isSuccess()) {
                OrderUpdateResponse orderUpdateResponse = JSONUtil.toBean(baseResult.getMsgData(), OrderUpdateResponse.class);
                if (2 == orderUpdateResponse.getResStatus()){
                    success = true;
                }else if (1 == orderUpdateResponse.getResStatus()){
                    success = true;
                    msg = "客户订单号与顺丰运单不匹配";
                }else {
                    success = true;
                    msg = "未知异常";
                }
            } else {
                success = false;
                msg = baseResult.getErrorMsg();
            }
        } catch (Exception e) {
            success = false;
            msg = e.getMessage();
        }
        if (success){
            logisticsOrderOperateLogService.addOperateLog(logisticsQueryVO.getLogisticsAuthEntity().getId(),
                    logisticsQueryVO.getTransportNo().get(0), BusinessTypeEnums.CANCEL_ORDER.getCode(), LogisticsPlatformEnum.SF_EXPRESS.getCode(),
                    RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(logisticsQueryVO), JSONUtil.toJsonStr(baseResult));
            return success(msg);
        }else {
            logisticsOrderOperateLogService.addOperateLog(logisticsQueryVO.getLogisticsAuthEntity().getId(),
                    logisticsQueryVO.getTransportNo().get(0), BusinessTypeEnums.CANCEL_ORDER.getCode(), LogisticsPlatformEnum.SF_EXPRESS.getCode(),
                    RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsQueryVO), JSONUtil.toJsonStr(msg));
            return failure(msg);
        }

    }

    /**
     * 取消订单
     *
     * @param logisticsQueryVO
     * @return
     */
    public ApiResult<String> cancelOrder(LogisticsCancelOrderVO logisticsQueryVO) {
        //支持单个取消
        OrderUpdateRequest orderUpdateRequest = OrderUpdateRequest.builder()
                .orderId(logisticsQueryVO.getDeliveryNo().get(0))
                .dealType(2)
                .build();
        boolean success;
        String msg = null;
        BaseResult baseResult = null;
        //只支持单个订单取消
        try {
            baseResult = expressShipperService.updateOrder(logisticsQueryVO.getLogisticsAuthEntity().getAccount(),
                    logisticsQueryVO.getLogisticsAuthEntity().getPassword(), orderUpdateRequest);
            //转换实体
            if (baseResult.isSuccess()) {
                OrderUpdateResponse orderUpdateResponse = JSONUtil.toBean(baseResult.getMsgData(), OrderUpdateResponse.class);
                if (2 == orderUpdateResponse.getResStatus()){
                    success = true;
                }else if (1 == orderUpdateResponse.getResStatus()){
                    success = true;
                    msg = "客户订单号与顺丰运单不匹配";
                }else {
                    success = true;
                    msg = "未知异常";
                }
            } else {
                success = false;
                msg = baseResult.getErrorMsg();
            }
        } catch (Exception e) {
            success = false;
            msg = e.getMessage();
        }
        if (success){
            logisticsOrderOperateLogService.addOperateLog(logisticsQueryVO.getLogisticsAuthEntity().getId(),
                    logisticsQueryVO.getTransportNo().get(0), BusinessTypeEnums.CANCEL_ORDER.getCode(), LogisticsPlatformEnum.SF_EXPRESS.getCode(),
                    RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(logisticsQueryVO), JSONUtil.toJsonStr(baseResult));
            return success(msg);
        }else {
            logisticsOrderOperateLogService.addOperateLog(logisticsQueryVO.getLogisticsAuthEntity().getId(),
                    logisticsQueryVO.getTransportNo().get(0), BusinessTypeEnums.CANCEL_ORDER.getCode(), LogisticsPlatformEnum.SF_EXPRESS.getCode(),
                    RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsQueryVO), JSONUtil.toJsonStr(msg));
            return failure(msg);
        }
    }

    /**
     * 查询订单(批量)
     *
     * @param logisticsQueryVOList
     * @return
     */
    @Override
    public ApiResult<List<LogisticsOrderResponseVO>> queryOrderList(LogisticsQueryBaseVO logisticsQueryVOList){
        return ApiResult.error(-1, "功能未开放");
    }

    @Override
    public ApiResult<LogisticsOrderResponseVO> queryOrder(LogisticsQueryBaseVO logisticsQueryVO) {
        OrderQueryRequest orderQueryRequest = OrderQueryRequest.builder().build();
        try {
            BaseResult baseResult = expressShipperService.queryOrder(logisticsQueryVO.getLogisticsAuthEntity().getAccount(),
                    logisticsQueryVO.getLogisticsAuthEntity().getPassword(), orderQueryRequest);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return ApiResult.error(-1, "功能未开放");
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
}
