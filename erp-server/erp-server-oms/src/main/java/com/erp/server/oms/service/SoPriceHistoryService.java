package com.erp.server.oms.service;

import com.common.business.service.SuperService;
import com.erp.model.oms.dto.SoPriceDetailDTO;
import com.erp.model.oms.entity.SoPriceHistoryEntity;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author will
 * @since 2025-03-24
 */
public interface SoPriceHistoryService extends SuperService<SoPriceHistoryEntity> {

    /**
     * 根据采购价目详情表id 获取历史数据
     * @author yl
     * @date 2023-03-29 10:28
     * @param priceDetailId
     * @return java.util.List<com.erp.model.scm.dto.SoPriceDetailDTO.HistoryDTO>
     */
    List<SoPriceDetailDTO.HistoryDTO> getHistory(String priceDetailId);

    /**
     * 根据变更详情id 获取
     * @author yl
     * @date 2023-10-23 16:13
     * @param changeDetailIdList
     * @return java.util.List<com.erp.model.scm.entity.SoPriceHistoryEntity>
     */
    List<SoPriceHistoryEntity> listByChangeDetailIdList(List<String> changeDetailIdList);


    }
