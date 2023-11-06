package com.erp.server.tms.service;

import com.common.core.controller.vo.ApiResult;
import com.erp.model.tms.entity.LogisticsAuthEntity;
import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
import com.erp.model.tms.vo.request.LogisticsOrderVO;
import com.erp.model.tms.vo.request.LogisticsQueryVO;
import com.erp.model.tms.vo.response.LogisticsOrderResponseVO;

import java.util.List;

/**
 * @author zdy
 * @ClassName LogisticsService
 * @description: 物流查询接口
 * @date 2023年10月30日
 * @version: 1.0
 */
public interface LogisticsService {
    /**
     * 获取授权信息
     *
     * @param authId
     * @return
     */
    LogisticsAuthEntity getLogisticsAuthConfig(String authId);

    /**
     * 创建订单
     *
     * @param logisticsOrderVO
     * @return
     */
    ApiResult<LogisticsOrderResponseVO> createOrder(LogisticsOrderVO logisticsOrderVO);

    /**
     * 确认订单
     *
     * @param logisticsQueryVO
     * @return
     */
    ApiResult<String> confirmOrder(LogisticsQueryVO logisticsQueryVO);

    /**
     * 取消订单
     *
     * @param logisticsQueryVO
     * @return
     */
    ApiResult<String> cancelOrder(LogisticsQueryVO logisticsQueryVO);

    /**
     * 拦截订单
     *
     * @param logisticsQueryVO
     * @return
     */
    ApiResult<String> interceptOrder(LogisticsQueryVO logisticsQueryVO);

    /**
     * 更新订单
     *
     * @param logisticsOrderVO
     * @return
     */
    ApiResult<String> updateOrder(LogisticsOrderVO logisticsOrderVO);

    /**
     * 查询订单
     *
     * @param logisticsQueryVO
     * @return
     */
    ApiResult queryOrder(LogisticsQueryVO logisticsQueryVO);

    /**
     * 获取标签
     *
     * @param logisticsQueryVO
     * @return
     */
    ApiResult getLabelUrl(LogisticsQueryVO logisticsQueryVO);

    /**
     * 轨迹查询
     *
     * @param logisticsQueryVO
     * @return
     */
    ApiResult getTrack(LogisticsQueryVO logisticsQueryVO);

    /**
     * 渠道查询
     *
     * @param logisticsQueryVO
     * @return
     */
    ApiResult<List<LogisticsSaleChannelEntity>> getChannel(LogisticsQueryVO logisticsQueryVO);
}
