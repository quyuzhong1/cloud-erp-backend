package com.erp.server.tms.service;

import com.common.business.enums.LogisticsPlatformEnum;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.tms.dto.LogisticsTrackBaseDTO;
import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
import com.erp.model.tms.entity.LogisticsTrackEntity;
import com.erp.model.tms.vo.request.*;
import com.erp.model.tms.vo.response.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

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
    Map<String, String> getLogisticsAuthConfigByAuthId(String authId);
    /**
     * 获取授权信息
     *
     * @param shopId
     * @return
     */
    Map<String, String> getLogisticsAuthConfigByShopId(String shopId);

    /**
     * 根据平台获取授权列表
     * @param platform
     * @return
     */
    List<Map<String, String>> getLogisticsAuthConfigByPlatform(String platform);

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
    ApiResult<List<ConfirmResponseVO>> confirmOrder(List<LogisticsQueryBaseVO> logisticsQueryVO);

    /**
     * 取消订单
     *
     * @param logisticsQueryVO
     * @return
     */
    ApiResult<List<CancelResponseVO>> cancelOrder(List<LogisticsCancelOrderVO> logisticsQueryVO);

    /**
     * 拦截订单
     *
     * @param logisticsQueryVO
     * @return
     */
    ApiResult<List<InterceptResponseVO>> interceptOrder(List<LogisticsInterceptOrderVO> logisticsQueryVO);

    /**
     * 更新订单
     *
     * @param logisticsOrderVOS
     * @return
     */
    ApiResult<List<UpdateResponseVO>> updateOrder(List<LogisticsOrderVO> logisticsOrderVOS);

    /**
     * 获取订单列表
     * @param logisticsQueryVOList
     * @return
     */
    ApiResult<List<LogisticsOrderResponseVO>> queryOrderList(List<LogisticsQueryBaseVO> logisticsQueryVOList);

    /**
     * 批量获取标签
     *
     * @param logisticsQueryVO
     * @return
     */
    ApiResult<List<LogisticsPrintLabelResponse>> getLabelList(List<LogisticsGetLabelVO> logisticsQueryVO) throws IOException;

    /**
     * 小包轨迹查询
     *
     * @param logisticsTrackVO
     * @return
     */
    ApiResult<List<LogisticsTrackEntity>> getTrack(LogisticsTrackVO logisticsTrackVO);

    /**
     * 海运轨迹查询
     *
     * @param oceanTrackRequestList
     * @return
     */
    ApiResult<List<LogisticsTrackEntity>> getOceanTrack(List<LogisticsTrackBaseDTO.OceanTrackRequestDTO> oceanTrackRequestList);


    /**
     * 物流单号注册
     * @param registerTrackVO
     * @return
     */
    ApiResult<List<RegisterResponseVO>> registerLogisticsNumber(RegisterTrackVO registerTrackVO);

    /**
     * 海运物流单号注册
     * @param list
     * @return
     */
    ApiResult<List<RegisterResponseVO>> oceanRegisterLogisticsNumber(List<LogisticsTrackBaseDTO.OceanRegisterRequestDTO> list);

    /**
     * 渠道查询
     *
     * @return
     */
    ApiResult<List<LogisticsSaleChannelEntity>> getChannel(ChanelQueryVO chanelQueryVO);

    /**
     * 渠道查询
     *
     * @return
     */
    ApiResult<Object>authorization(Map<String, String> authMap);
    /**
     * 获取平台标识
     *
     * @return
     */
    LogisticsPlatformEnum getPlatForm();

    ApiResult<List<LogisticsServiceResponseVO>>  listLogisticsService(Map<String, String> authMap);

    /**
     * 更新重量
     * @return
     */
    ApiResult<String> updateWeight(LogisticsUpdateWeightVO logisticsUpdateWeightVO);
}
