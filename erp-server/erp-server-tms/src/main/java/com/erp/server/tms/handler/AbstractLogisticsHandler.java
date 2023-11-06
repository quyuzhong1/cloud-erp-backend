package com.erp.server.tms.handler;

import com.common.core.controller.vo.ApiResult;
import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
import com.erp.model.tms.vo.request.LogisticsOrderVO;
import com.erp.model.tms.vo.request.LogisticsQueryVO;
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
public abstract class AbstractLogisticsHandler implements LogisticsService {
    //对于一些公共方法可以进行封装

    /**
     * 创建订单
     *
     * @param logisticsOrderVO
     * @return
     */
    public ApiResult<LogisticsOrderResponseVO> createOrder(LogisticsOrderVO logisticsOrderVO) {
        return null;
    }

    /**
     * 确认订单
     *
     * @param logisticsQueryVO
     * @return
     */
    public ApiResult<String> confirmOrder(LogisticsQueryVO logisticsQueryVO) {
        return null;
    }

    /**
     * 取消订单
     *
     * @param logisticsQueryVO
     * @return
     */
    public ApiResult<String> cancelOrder(LogisticsQueryVO logisticsQueryVO) {
        return null;
    }

    /**
     * 拦截订单
     *
     * @param logisticsQueryVO
     * @return
     */
    public ApiResult<String> interceptOrder(LogisticsQueryVO logisticsQueryVO) {
        return null;
    }

    /**
     * 更新订单
     *
     * @param logisticsOrderVO
     * @return
     */
    public ApiResult<String> updateOrder(LogisticsOrderVO logisticsOrderVO) {
        return null;
    }

    /**
     * 查询订单
     *
     * @param logisticsQueryVO
     * @return
     */
    public ApiResult queryOrder(LogisticsQueryVO logisticsQueryVO) {
        return null;
    }

    /**
     * 获取标签
     *
     * @param logisticsQueryVO
     * @return
     */
    public ApiResult getLabelUrl(LogisticsQueryVO logisticsQueryVO) {
        return null;
    }

    /**
     * 轨迹查询
     *
     * @param logisticsQueryVO
     * @return
     */
    public ApiResult getTrack(LogisticsQueryVO logisticsQueryVO) {
        return null;
    }

    /**
     * 渠道查询
     *
     * @param logisticsQueryVO
     * @return
     */
    public ApiResult<List<LogisticsSaleChannelEntity>> getChannel(LogisticsQueryVO logisticsQueryVO) {
        return null;
    }
}
