package com.erp.server.tms.service;

import com.common.business.dto.base.BatchResultDTO;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.oms.entity.ShopAuthEntity;
import com.erp.model.tms.entity.LogisticsBillDetailEntity;
import com.erp.model.tms.vo.request.LogisticsQueryBaseVO;
import com.erp.model.tms.vo.response.LogisticsOrderResponseVO;

import java.util.List;

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
    List<BatchResultDTO> processTrackData(String platformType, List<LogisticsBillDetailEntity> records);

    /**
     * 注册物流单号
     * @param platformType
     * @param records
     * @return
     */
    List<BatchResultDTO> processRegisterData(String platformType, List<LogisticsBillDetailEntity> records);

    List<BatchResultDTO> batchUpdateTrackInfo(List<LogisticsBillDetailEntity> logisticsBillDetailEntities);

    /**
     * 同步虾皮渠道
     * @param platform
     * @return
     */
    List<BatchResultDTO> syncShoppeeChannel(String platform);
    /**
     * 同步虾皮渠道
     * @param platform
     * @return
     */
    List<BatchResultDTO> syncAliExpressChannel(String platform);

    /**
     * 同步单一渠道
     * @param platform
     * @return
     */
    List<BatchResultDTO> syncSingleChannel(String platform);
}
