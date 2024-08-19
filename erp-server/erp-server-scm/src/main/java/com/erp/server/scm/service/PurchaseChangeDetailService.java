package com.erp.server.scm.service;

import com.common.business.service.SuperService;
import com.erp.model.scm.dto.PurchaseChangeDetailDTO;
import com.erp.model.scm.entity.PurchaseChangeDetailEntity;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author will
 * @since 2023-03-16
 */
public interface PurchaseChangeDetailService extends SuperService<PurchaseChangeDetailEntity> {
    /**
     * @description: 新增变更单
     * @author Will
     * @date: 2023/3/30 19:41
     * @param details
     * @param purchaseChangeId
     */
    void add(List<PurchaseChangeDetailDTO.AddDTO> details, String purchaseChangeId);
    /**
     * @description: 修改变更单
     * @author Will
     * @date: 2023/3/31 10:07
     * @param details
     * @param id

     */
    void update(List<PurchaseChangeDetailDTO.UpdateDTO> details, String id);
    /**
     * @description: 根据采购变更单明细ids查询
     * @author Will
     * @date: 2023/3/31 10:18
     * @param asList
     * @return List<PurchaseChangeDetailEntity>
     */
    List<PurchaseChangeDetailEntity> listByPurchaseChangeIds(List<String> asList);
    /**
     * @description: 验证报价和关联数量
     * @author Will
     * @date: 2023/10/12 15:25
     * @param list
     * @param purchaseChangeId
     */
    void checkPurchasePrice (List<PurchaseChangeDetailEntity> list,String purchaseChangeId);
}
