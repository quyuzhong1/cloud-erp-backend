package com.erp.server.tms.service;

import com.common.business.dto.base.BatchResultDTO;
import com.erp.model.tms.dto.LogisticsTrackDTO;
import com.erp.model.tms.vo.request.LogisticsQueryBaseVO;
import com.erp.model.tms.vo.response.LogisticsOrderResponseVO;

import java.util.List;
import java.util.Map;

/**
 * @author zdy
 * @ClassName LogisticsBaseService
 * @description: TODO
 * @date 2023年11月15日
 * @version: 1.0
 */
public interface LogisticsBaseService {
    /**
     * 同步渠道
     *
     * @param platform
     * @return
     */
    List<BatchResultDTO> syncLogisticsChannel(String platform);


    /**
     * 查询订单信息
     *
     * @param logisticsQueryVOList
     */
    List<LogisticsOrderResponseVO> queryOrderList(List<LogisticsQueryBaseVO> logisticsQueryVOList);

    /**
     * 处理轨迹查询业务数据
     *
     * @param platformType
     */
    List<BatchResultDTO> processTrackData(String platformType, List<LogisticsTrackDTO.UpdateTrackDTO> records,String transportType);

    /**
     * 注册物流单号
     *
     * @param platformType
     * @param records
     * @return
     */
    List<BatchResultDTO> processRegisterData(String platformType, List<LogisticsTrackDTO.UpdateTrackDTO> records,String transportType);

    List<BatchResultDTO> batchUpdateTrackInfo(List<LogisticsTrackDTO.UpdateTrackDTO> dtos,String transportType);

    /**
     * 同步虾皮渠道
     *
     * @param platform
     * @return
     */
    List<BatchResultDTO> syncShoppeeChannel(String platform);

    /**
     * 同步虾皮渠道
     *
     * @param platform
     * @param map
     * @return
     */
    List<BatchResultDTO> syncAliExpressChannel(String platform, Map<String, String> map);

    /**
     * 同步单一渠道
     *
     * @param platform
     * @return
     */
    List<BatchResultDTO> syncSingleChannel(String platform);

    /**
     * 同步Shopify渠道
     *
     * @param platform
     * @return
     */
    List<BatchResultDTO> syncShopifyChannel(String platform);

    /**
     * 速卖通同步物流地址
     *
     * @param authMap
     */
    void syncLogisticsAddress(Map<String, String> authMap);

    /**
     * 清洗mongodb 物流轨迹数据
     * @param platformType
     * @param records
     * @param transportType
     */
    void processMongoTrackData(String platformType, List<LogisticsTrackDTO.UpdateTrackDTO> records, String transportType);
}
