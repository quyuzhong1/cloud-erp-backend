package com.erp.server.tms.service;

import com.common.core.controller.vo.ApiResult;
import com.erp.model.oms.entity.SoInfoEntity;
import com.erp.model.plm.entity.ProductLogisticsEntity;
import com.erp.model.tms.entity.LogisticsAddressEntity;
import com.erp.model.tms.entity.LogisticsAuthEntity;
import com.erp.model.tms.entity.LogisticsSaleChannelEntity;

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
     * 创建订单
     * @param soInfo 销售订单 收货人信息
     * @param addressEntity 发货人信息
     * @param productLogisticsEntity 产品物流信息
     * @return
     */
    ApiResult<String> createOrder(SoInfoEntity soInfo, LogisticsAddressEntity addressEntity,
                                  ProductLogisticsEntity productLogisticsEntity, LogisticsAuthEntity logisticsAuthEntity);

    /**
     * 确认订单
     *
     * @param platformCode
     * @return
     */
    ApiResult<String> confirmOrder(String platformCode);

    /**
     * 取消订单
     *
     * @param platformCode
     * @return
     */
    ApiResult<String> cancelOrder(String platformCode);

    /**
     * 拦截订单
     *
     * @param platformCode
     * @return
     */
    ApiResult<String> interceptOrder(String platformCode);

    /**
     * 更新订单
     *
     * @param platformCode
     * @return
     */
    ApiResult<String> updateOrder(String platformCode);

    /**
     * 查询订单
     *
     * @param platformCode
     * @return
     */
    ApiResult queryOrder(String platformCode);

    /**
     * 获取标签
     *
     * @param platformCode
     * @return
     */
    ApiResult getLabelUrl(String platformCode);

    /**
     * 轨迹查询
     *
     * @param platformCode
     * @return
     */
    ApiResult getTrack(String platformCode);

    /**
     * 渠道查询
     *
     * @param platformCode
     * @return
     */
    ApiResult<List<LogisticsSaleChannelEntity>> getChannel(String platformCode);
}
