package com.erp.server.wms.wdt;

import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.wms.entity.VirtualWarehouseAllocationHandleDetailEntity;
import com.erp.model.wms.enums.VwAllocationDirectionEnum;

import java.util.List;

/**
 * 旺店通虚拟仓分货单
 * @author hyj
 * @date 2024-06-14
 */
public interface SyncWdtVirtualWarehouseAllocationOrderService {

    /**
     * 保存推送旺店通虚拟仓分货单任务
     *
     * @param handleDetailList SKU明细列表
     * @param sourceCode       来源单据编号
     * @param operateCode      操作代码: 审核/反审核
     * @return DmpPushTaskEntity DMP返回的任务
     */
    List<DmpPushTaskEntity> saveTaskList(List<VirtualWarehouseAllocationHandleDetailEntity> handleDetailList,
                                         String sourceCode, String operateCode,String sourceType);
}
