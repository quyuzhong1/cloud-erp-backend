package com.erp.server.wms.service;

import com.common.business.service.SuperService;
import com.erp.model.wms.dto.PurchaseStockInDetailDTO;
import com.erp.model.wms.entity.PurchaseStockInDetailEntity;

import java.util.List;

/**
 * <p>
 * 采购入库明细表 服务类
 * </p>
 *
 * @author will
 * @since 2023-04-10
 */
public interface PurchaseStockInDetailService extends SuperService<PurchaseStockInDetailEntity> {
    /**
     * @description: 新增明细
     * @author Will
     * @date: 2023/4/13 15:49
     * @param details
     * @param mainId
     */
    void add(List<PurchaseStockInDetailDTO.AddDTO> details, String mainId);
    /**
     * @description: 修改明细
     * @author Will
     * @date: 2023/4/13 15:58
     * @param details
     * @param mainId
     */
    void update(List<PurchaseStockInDetailDTO.UpdateDTO> details, String mainId);
    /**
     * @description: 根据主表ids删除
     * @author Will
     * @date: 2023/4/13 16:05
     * @param mainIds
     */
    void removeByMainIds(List<String> mainIds);
    /**
     * @description: 根据主表id查询
     * @author Will
     * @date: 2023/4/13 17:35
     * @param mainId
     * @return List<PurchaseStockInDetailEntity>
     */
    List<PurchaseStockInDetailEntity> listByMainId(String mainId);
}
