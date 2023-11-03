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
    SoInfoEntity getSoInfo(String soId);
    LogisticsAuthEntity getLogisticsAuthConfig(String authId);

    ApiResult<String> createOrder(String soId, String authId);
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
     * @param trackNumber
     * @param logisticsAuthEntity
     * @return
     */
    ApiResult<String> confirmOrder(String trackNumber,LogisticsAuthEntity logisticsAuthEntity);

    /**
     * 取消订单
     *
     * @param trackNumber
     * @param logisticsAuthEntity
     * @return
     */
    ApiResult<String> cancelOrder(String trackNumber,LogisticsAuthEntity logisticsAuthEntity);

    /**
     * 拦截订单
     *
     * @param trackNumber
     * @param logisticsAuthEntity
     * @return
     */
    ApiResult<String> interceptOrder(String trackNumber,LogisticsAuthEntity logisticsAuthEntity);

    /**
     * 更新订单
     *
     * @param trackNumber
     * @param logisticsAuthEntity
     * @return
     */
    ApiResult<String> updateOrder(String trackNumber,LogisticsAuthEntity logisticsAuthEntity);

    /**
     * 查询订单
     *
     * @param trackNumbers
     * @param logisticsAuthEntity
     * @return
     */
    ApiResult queryOrder(String trackNumbers,LogisticsAuthEntity logisticsAuthEntity);

    /**
     * 获取标签(打印标签)
     *
     * @param trackNumber
     * @param logisticsAuthEntity
     * @return
     */
    ApiResult getLabelUrl(String trackNumber,LogisticsAuthEntity logisticsAuthEntity);

    /**
     * 轨迹查询
     *
     * @param trackNumbers
     * @param logisticsAuthEntity
     * @return
     */
    ApiResult getTrack(String trackNumbers,LogisticsAuthEntity logisticsAuthEntity);

    /**
     * 渠道查询
     *
     * @param logisticsAuthEntity
     * @return
     */
    ApiResult<List<LogisticsSaleChannelEntity>> getChannel(LogisticsAuthEntity logisticsAuthEntity);
}
