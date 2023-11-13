package com.erp.server.tms.handler;

import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.tms.entity.LogisticsAuthEntity;
import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
import com.erp.model.tms.vo.request.*;
import com.erp.model.tms.vo.response.*;
import com.erp.server.tms.service.LogisticsService;

import java.io.IOException;
import java.util.List;

/**
 * @author zdy
 * @ClassName AbstractLogisticsHandler
 * @description: 抽象类 封装公共方法
 * @date 2023年11月03日
 * @version: 1.0
 */
public abstract class AbstractLogisticsHandler extends BaseController implements LogisticsService {

    //对于一些公共方法可以进行封装
    @Override
    public LogisticsAuthEntity getLogisticsAuthConfig(String authId) {
        return null;
    }

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
    @Override
    public ApiResult<List<ConfirmResponseVO>> confirmOrder(List<LogisticsQueryBaseVO> logisticsQueryVO) {
        return ApiResult.error(-1, "功能未开放");
    }


    /**
     * 取消订单
     *
     * @param logisticsQueryVO
     * @return
     */
    public ApiResult<List<CancelResponseVO>> cancelOrder(List<LogisticsCancelOrderVO> logisticsQueryVO) {
        return ApiResult.error(-1, "功能未开放");
    }

    /**
     * 拦截订单
     *
     * @param logisticsQueryVO
     * @return
     */
    public ApiResult<List<InterceptResponseVO>> interceptOrder(List<LogisticsInterceptOrderVO> logisticsQueryVO) {
        return ApiResult.error(-1, "功能未开放");
    }

    /**
     * 更新订单
     *
     * @param logisticsOrderVOS
     * @return
     */
    public ApiResult<List<UpdateResponseVO>> updateOrder(List<LogisticsOrderVO> logisticsOrderVOS) {
        return ApiResult.error(-1, "功能未开放");
    }


    /**
     * 查询订单(批量)
     *
     * @param logisticsQueryVOList
     * @return
     */
    @Override
    public ApiResult<List<LogisticsOrderResponseVO>> queryOrderList(List<LogisticsQueryBaseVO> logisticsQueryVOList) {
        return ApiResult.error(-1, "功能未开放");
    }

    /**
     * 获取标签
     *
     * @param logisticsQueryVO
     * @return
     */
    public ApiResult<List<LogisticsPrintLabelResponse>> getLabelList(List<LogisticsGetLabelVO> logisticsQueryVO) throws IOException {
        return ApiResult.error(-1, "功能未开放");
    }

    /**
     * 轨迹查询
     *
     * @param logisticsQueryVO
     * @return
     */
    public ApiResult getTrack(LogisticsQueryBaseVO logisticsQueryVO) {
        return ApiResult.error(-1, "功能未开放");
    }

    /**
     * 渠道查询
     *
     * @param chanelQueryVO
     * @return
     */
    public ApiResult<List<LogisticsSaleChannelEntity>> getChannel(ChanelQueryVO chanelQueryVO) {
        return ApiResult.error(-1, "功能未开放");
    }

}
