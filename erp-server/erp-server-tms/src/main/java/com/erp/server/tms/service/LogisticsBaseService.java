package com.erp.server.tms.service;

import com.common.core.controller.vo.ApiResult;
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
    ApiResult syncLogisticsChannel(String platform);

    /**
     * 同步所有渠道
     *
     * @return
     */
    ApiResult syncAllLogisticsChannel();

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
     * @param trackNos
     */
    void processTrackData(String platformType, List<LogisticsBillDetailEntity> records);
}
