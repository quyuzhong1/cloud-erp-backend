package com.erp.server.tms.service;

import com.common.core.controller.vo.ApiResult;
import com.erp.model.tms.entity.LogisticsAuthEntity;
import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
import com.erp.model.tms.vo.request.*;
import com.erp.model.tms.vo.response.InterceptResponseVO;
import com.erp.model.tms.vo.response.LogisticsOrderResponseVO;
import com.erp.model.tms.vo.response.LogisticsPrintLabelResponse;

import java.io.IOException;
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
    ApiResult<String> confirmOrder(LogisticsQueryBaseVO logisticsQueryVO);

    /**
     * 取消订单
     *
     * @param logisticsQueryVO
     * @return
     */
    ApiResult<String> cancelOrder(LogisticsCancelOrderVO logisticsQueryVO);

    /**
     * 拦截订单
     *
     * @param logisticsQueryVO
     * @return
     */
    ApiResult<List<InterceptResponseVO>> interceptOrder(LogisticsInterceptOrderVO logisticsQueryVO);

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
    ApiResult<LogisticsOrderResponseVO> queryOrder(LogisticsQueryBaseVO logisticsQueryVO);

    ApiResult<List<LogisticsOrderResponseVO>> queryOrderList(LogisticsQueryBaseVO logisticsQueryVOList);

    /**
     * 批量获取标签
     *
     * @param logisticsQueryVO
     * @return
     */
    ApiResult<List<LogisticsPrintLabelResponse>> getLabelList(LogisticsGetLabelVO logisticsQueryVO) throws IOException;
    /**
     * 轨迹查询
     *
     * @param logisticsQueryVO
     * @return
     */
    ApiResult getTrack(LogisticsQueryBaseVO logisticsQueryVO);

    /**
     * 渠道查询
     *
     * @return
     */
    ApiResult<List<LogisticsSaleChannelEntity>> getChannel(ChanelQueryVO chanelQueryVO);
}
