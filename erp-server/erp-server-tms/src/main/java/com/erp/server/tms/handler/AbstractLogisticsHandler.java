package com.erp.server.tms.handler;

import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.tms.entity.LogisticsAuthEntity;
import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
import com.erp.model.tms.vo.request.*;
import com.erp.model.tms.vo.response.LogisticsOrderResponseVO;
import com.erp.server.tms.service.LogisticsService;

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
        return ApiResult.error(-1, "功能未开放");
    }

    /**
     * 取消订单
     *
     * @param logisticsQueryVO
     * @return
     */
    public ApiResult<String> cancelOrder(LogisticsCancelOrderVO logisticsQueryVO) {
        return ApiResult.error(-1, "功能未开放");
    }

    /**
     * 拦截订单
     *
     * @param logisticsQueryVO
     * @return
     */
    public ApiResult<String> interceptOrder(LogisticsInterceptOrderVO logisticsQueryVO) {
        return ApiResult.error(-1, "功能未开放");
    }

    /**
     * 更新订单
     *
     * @param logisticsOrderVO
     * @return
     */
    public ApiResult<String> updateOrder(LogisticsOrderVO logisticsOrderVO) {
        return ApiResult.error(-1, "功能未开放");
    }

    /**
     * 查询订单
     *
     * @param logisticsQueryVO
     * @return
     */
    public ApiResult queryOrder(LogisticsQueryBaseVO logisticsQueryVO) {
        return ApiResult.error(-1, "功能未开放");
    }

    /**
     * 获取标签
     *
     * @param logisticsQueryVO
     * @return
     */
    public ApiResult getLabelUrl(LogisticsGetLabelVO logisticsQueryVO) {
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
     * @param authEntity
     * @return
     */
    public ApiResult<List<LogisticsSaleChannelEntity>> getChannel(LogisticsAuthEntity authEntity) {
        return ApiResult.error(-1, "功能未开放");
    }
}
