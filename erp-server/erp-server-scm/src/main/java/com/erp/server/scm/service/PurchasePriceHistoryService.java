package com.erp.server.scm.service;

import com.common.business.service.SuperService;
import com.erp.model.scm.dto.PurchasePriceDetailDTO;
import com.erp.model.scm.entity.PurchasePriceHistoryEntity;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Lambda
 * @since 2023-03-28
 */
public interface PurchasePriceHistoryService extends SuperService<PurchasePriceHistoryEntity> {


    /**
     * 根据采购价目详情表id 获取历史数据
     * @author yl
     * @date 2023-03-29 10:28
     * @param priceDetailId
     * @return java.util.List<com.erp.model.scm.dto.PurchasePriceDetailDTO.HistoryDTO>
     */
    List<PurchasePriceDetailDTO.HistoryDTO> getHistory(String priceDetailId);
}
